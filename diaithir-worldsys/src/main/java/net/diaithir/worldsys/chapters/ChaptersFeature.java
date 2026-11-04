package net.diaithir.worldsys.chapters;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.IdentifierArgument;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.TriState;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.EntityTravelToDimensionEvent;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.io.Reader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Chapters: stage gating folded from Chapters 2.2 by gabinx (MIT). Items, recipes and dimensions are locked until a
 * player is granted a stage. Stages are datapack JSON under {@code chapters/stages}; the pack ships none here (the
 * age tree supplies them). Server side only: no JEI, KubeJS, FTB or mixin hooks. Locked items cannot be picked up,
 * crafted or kept (they are dropped about once a second); locked dimensions cannot be entered.
 */
public final class ChaptersFeature {
    private static final DeferredRegister<AttachmentType<?>> ATTACHMENTS =
            DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, "diaithirworldsys");
    static final DeferredHolder<AttachmentType<?>, AttachmentType<List<Identifier>>> OWNED = ATTACHMENTS.register("chapter_stages",
            () -> AttachmentType.<List<Identifier>>builder(() -> List.<Identifier>of())
                    .serialize(Identifier.CODEC.listOf().fieldOf("stages")).copyOnDeath().build());

    static final ChapterBook BOOK = new ChapterBook();
    private static final FileToIdConverter LISTER = FileToIdConverter.json("chapters/stages");

    private ChaptersFeature() {}

    public static void register(IEventBus modBus) {
        ATTACHMENTS.register(modBus);
        NeoForge.EVENT_BUS.addListener(AddServerReloadListenersEvent.class, e ->
                e.addListener(Identifier.fromNamespaceAndPath("diaithirworldsys", "chapters"), new Loader()));
        NeoForge.EVENT_BUS.addListener(RegisterCommandsEvent.class, e -> commands(e.getDispatcher()));
        NeoForge.EVENT_BUS.addListener(ItemEntityPickupEvent.Pre.class, e -> {
            if (e.getPlayer() instanceof ServerPlayer p && itemLocked(p, e.getItemEntity().getItem())) e.setCanPickup(TriState.FALSE);
        });
        NeoForge.EVENT_BUS.addListener(PlayerEvent.ItemCraftedEvent.class, e -> {
            if (!(e.getEntity() instanceof ServerPlayer p)) return;
            ItemStack out = e.getCrafting();
            if (itemLocked(p, out)) {
                p.sendSystemMessage(Component.translatable("chapters.craft.blocked", out.getHoverName()), true);
                audit(p);
            }
        });
        NeoForge.EVENT_BUS.addListener(PlayerTickEvent.Post.class, e -> {
            if (e.getEntity() instanceof ServerPlayer p && p.tickCount % 20 == 0) audit(p);
        });
        NeoForge.EVENT_BUS.addListener(EntityTravelToDimensionEvent.class, ChaptersFeature::onTravel);
    }

    // ---- state ----
    static Set<Identifier> owned(ServerPlayer p) { return new LinkedHashSet<>(p.getData(OWNED)); }

    static boolean grant(ServerPlayer p, Identifier stage) {
        Set<Identifier> s = owned(p);
        if (!s.add(stage)) return false;
        p.setData(OWNED, new ArrayList<>(s));
        return true;
    }

    static boolean revoke(ServerPlayer p, Identifier stage) {
        Set<Identifier> s = owned(p);
        if (!s.remove(stage)) return false;
        p.setData(OWNED, new ArrayList<>(s));
        audit(p);
        return true;
    }

    // ---- queries ----
    static boolean itemLocked(ServerPlayer p, ItemStack stack) {
        if (stack.isEmpty()) return false;
        Identifier id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        List<Identifier> tags = BuiltInRegistries.ITEM.wrapAsHolder(stack.getItem()).tags().map(t -> t.location()).collect(Collectors.toList());
        return ChapterBook.isLocked(BOOK.definingItem(id, tags), owned(p));
    }

    static boolean dimensionLocked(ServerPlayer p, Identifier dim) {
        return ChapterBook.isLocked(BOOK.definingDimension(dim), owned(p));
    }

    /** Drop every locked stack from the inventory. */
    static void audit(ServerPlayer p) {
        var inv = p.getInventory();
        for (int slot = 0; slot < inv.getContainerSize(); slot++) {
            ItemStack stack = inv.getItem(slot);
            if (!stack.isEmpty() && itemLocked(p, stack)) {
                ItemStack drop = stack.copy();
                inv.setItem(slot, ItemStack.EMPTY);
                p.drop(drop, false);
            }
        }
    }

    private static void onTravel(EntityTravelToDimensionEvent e) {
        Entity entity = e.getEntity();
        if (entity.level().isClientSide()) return;
        Identifier dest = e.getDimension().identifier();
        List<Entity> riders = new ArrayList<>(entity.getPassengers());
        riders.add(entity);
        for (Entity r : riders) {
            if (r instanceof ServerPlayer p && dimensionLocked(p, dest)) {
                p.sendSystemMessage(Component.translatable("chapters.dimension.blocked", dest.toString()), true);
                e.setCanceled(true);
                return;
            }
        }
    }

    // ---- reload ----
    private static final class Loader extends SimplePreparableReloadListener<Map<Identifier, JsonObject>> {
        @Override
        protected Map<Identifier, JsonObject> prepare(ResourceManager rm, ProfilerFiller profiler) {
            Map<Identifier, JsonObject> out = new LinkedHashMap<>();
            for (var entry : LISTER.listMatchingResources(rm).entrySet()) {
                Identifier id = LISTER.fileToId(entry.getKey());
                try (Reader r = entry.getValue().openAsReader()) {
                    JsonElement el = JsonParser.parseReader(r);
                    if (el.isJsonObject()) out.put(id, el.getAsJsonObject());
                } catch (Exception ex) {
                    System.err.println("[chapters] bad stage " + id + ": " + ex);
                }
            }
            return out;
        }

        @Override
        protected void apply(Map<Identifier, JsonObject> in, ResourceManager rm, ProfilerFiller profiler) {
            List<ChapterStage> list = new ArrayList<>();
            in.forEach((id, json) -> list.add(ChapterStage.fromJson(id, json)));
            BOOK.replace(list);
        }
    }

    // ---- commands ----
    private static void commands(com.mojang.brigadier.CommandDispatcher<CommandSourceStack> d) {
        var stages = (com.mojang.brigadier.suggestion.SuggestionProvider<CommandSourceStack>)
                (c, b) -> SharedSuggestionProvider.suggestResource(BOOK.stageIds(), b);
        d.register(Commands.literal("chapters").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(Commands.literal("add").then(Commands.argument("player", EntityArgument.player())
                        .then(Commands.argument("stage", IdentifierArgument.id()).suggests(stages).executes(c -> {
                            ServerPlayer p = EntityArgument.getPlayer(c, "player");
                            Identifier s = IdentifierArgument.getId(c, "stage");
                            boolean ok = grant(p, s);
                            c.getSource().sendSuccess(() -> Component.literal((ok ? "Granted " : "Already had ") + s + " to " + p.getGameProfile().name()), true);
                            return ok ? 1 : 0;
                        }))))
                .then(Commands.literal("remove").then(Commands.argument("player", EntityArgument.player())
                        .then(Commands.argument("stage", IdentifierArgument.id()).suggests(stages).executes(c -> {
                            ServerPlayer p = EntityArgument.getPlayer(c, "player");
                            Identifier s = IdentifierArgument.getId(c, "stage");
                            boolean ok = revoke(p, s);
                            c.getSource().sendSuccess(() -> Component.literal((ok ? "Revoked " : "Did not have ") + s + " from " + p.getGameProfile().name()), true);
                            return ok ? 1 : 0;
                        }))))
                .then(Commands.literal("list").then(Commands.argument("player", EntityArgument.player()).executes(c -> {
                    ServerPlayer p = EntityArgument.getPlayer(c, "player");
                    Set<Identifier> o = owned(p);
                    c.getSource().sendSuccess(() -> Component.literal(p.getGameProfile().name() + ": " + (o.isEmpty() ? "-" : o.stream().map(Identifier::toString).collect(Collectors.joining(", ")))), false);
                    return o.size();
                })))
                .then(Commands.literal("check").then(Commands.argument("player", EntityArgument.player())
                        .then(Commands.argument("stage", IdentifierArgument.id()).suggests(stages).executes(c -> {
                            boolean has = owned(EntityArgument.getPlayer(c, "player")).contains(IdentifierArgument.getId(c, "stage"));
                            c.getSource().sendSuccess(() -> Component.literal(Boolean.toString(has)), false);
                            return has ? 1 : 0;
                        }))))
                .then(Commands.literal("stages").executes(c -> {
                    c.getSource().sendSuccess(() -> Component.literal(BOOK.stageIds().isEmpty() ? "no stages loaded" : BOOK.stageIds().stream().map(Identifier::toString).collect(Collectors.joining(", "))), false);
                    return BOOK.stageIds().size();
                })));
    }
}
