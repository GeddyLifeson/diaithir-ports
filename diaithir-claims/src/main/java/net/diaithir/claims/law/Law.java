package net.diaithir.claims.law;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.block.BreakBlockEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/**
 * Law: handcuffs and bounties. Clean-room, from the behaviour of Cuffed (GPL-3.0) and Bountycraft (MIT) as described on their pages; no code taken.
 * Handcuffs: use on a player to restrain them (slow, cannot attack, break or use things). A cuff key frees them. Cuffs are lost on death.
 * Bounties: /bounty place PLAYER puts the item in your hand on that player's head; whoever kills them collects every bounty on them.
 */
public final class Law {
    public static final String ID = "diaithirclaims";
    private static final Identifier SLOW = Identifier.fromNamespaceAndPath(ID, "cuffed");

    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ID);
    private static final DeferredRegister<AttachmentType<?>> ATTACH = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, ID);

    public static final DeferredItem<Item> CUFFS = ITEMS.registerItem("handcuffs", Item::new, () -> new Item.Properties().stacksTo(16));
    public static final DeferredItem<Item> KEY = ITEMS.registerItem("cuff_key", Item::new, () -> new Item.Properties().stacksTo(16));
    /** On a player: restrained. Not copied on death, so death frees them. */
    public static final Supplier<AttachmentType<Boolean>> CUFFED = ATTACH.register("cuffed",
        () -> AttachmentType.<Boolean>builder(() -> false).serialize(Codec.BOOL.fieldOf("c")).build());

    private Law() {}

    public static void init(IEventBus bus) {
        ITEMS.register(bus);
        ATTACH.register(bus);
        NeoForge.EVENT_BUS.register(new Law());
        NeoForge.EVENT_BUS.addListener((RegisterCommandsEvent e) -> register(e.getDispatcher()));
    }

    static boolean cuffed(Player p) {
        return p.getData(CUFFED);
    }

    private static boolean deny(Player p) {
        if (cuffed(p)) {
            if (p instanceof ServerPlayer sp) sp.sendOverlayMessage(Component.literal("Your hands are bound."));
            return true;
        }
        return false;
    }

    @net.neoforged.bus.api.SubscribeEvent
    public void onTick(PlayerTickEvent.Post e) {
        if (!(e.getEntity() instanceof ServerPlayer p)) return;
        var attr = p.getAttribute(Attributes.MOVEMENT_SPEED);
        if (attr == null) return;
        boolean has = attr.getModifier(SLOW) != null;
        if (cuffed(p) && !has) attr.addTransientModifier(new AttributeModifier(SLOW, -0.6, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        if (!cuffed(p) && has) attr.removeModifier(SLOW);
    }

    @net.neoforged.bus.api.SubscribeEvent
    public void onUseOnPlayer(PlayerInteractEvent.EntityInteract e) {
        if (!(e.getEntity() instanceof ServerPlayer user) || !(e.getTarget() instanceof ServerPlayer target) || e.getHand() != InteractionHand.MAIN_HAND) return;
        if (cuffed(user)) {
            e.setCanceled(true);
            return;
        }
        ItemStack held = user.getMainHandItem();
        if (held.is(CUFFS.get()) && !cuffed(target)) {
            target.setData(CUFFED, true);
            held.shrink(1);
            target.sendSystemMessage(Component.literal(user.getGameProfile().name() + " has cuffed you."));
            user.sendSystemMessage(Component.literal("You cuffed " + target.getGameProfile().name() + "."));
            e.setCancellationResult(InteractionResult.SUCCESS);
            e.setCanceled(true);
        } else if (held.is(KEY.get()) && cuffed(target)) {
            target.setData(CUFFED, false);
            held.shrink(1);
            ItemStack back = new ItemStack(CUFFS.get());
            if (!user.getInventory().add(back)) user.drop(back, false);
            user.sendSystemMessage(Component.literal("You freed " + target.getGameProfile().name() + "."));
            e.setCancellationResult(InteractionResult.SUCCESS);
            e.setCanceled(true);
        }
    }

    @net.neoforged.bus.api.SubscribeEvent
    public void onAttack(AttackEntityEvent e) {
        if (deny(e.getEntity())) e.setCanceled(true);
    }

    @net.neoforged.bus.api.SubscribeEvent
    public void onBlock(PlayerInteractEvent.RightClickBlock e) {
        if (deny(e.getEntity())) e.setCanceled(true);
    }

    @net.neoforged.bus.api.SubscribeEvent
    public void onItem(PlayerInteractEvent.RightClickItem e) {
        if (deny(e.getEntity())) e.setCanceled(true);
    }

    @net.neoforged.bus.api.SubscribeEvent
    public void onBreak(BreakBlockEvent e) {
        if (deny(e.getPlayer())) e.setCanceled(true);
    }

    @net.neoforged.bus.api.SubscribeEvent
    public void onDeath(LivingDeathEvent e) {
        if (!(e.getEntity() instanceof ServerPlayer victim)) return;
        if (cuffed(victim)) victim.setData(CUFFED, false);
        if (!(e.getSource().getEntity() instanceof ServerPlayer killer) || killer == victim) return;
        List<ItemStack> prize = Bounties.get(victim.level().getServer()).claim(victim.getUUID());
        if (prize.isEmpty()) return;
        for (ItemStack s : prize) if (!killer.getInventory().add(s)) killer.drop(s, false);
        killer.sendSystemMessage(Component.literal("You collected the bounty on " + victim.getGameProfile().name() + "."));
    }

    private static void register(CommandDispatcher<CommandSourceStack> d) {
        d.register(Commands.literal("bounty")
            .then(Commands.literal("place").then(Commands.argument("who", net.minecraft.commands.arguments.EntityArgument.player()).executes(c -> {
                ServerPlayer p = c.getSource().getPlayerOrException();
                ServerPlayer t = net.minecraft.commands.arguments.EntityArgument.getPlayer(c, "who");
                ItemStack held = p.getMainHandItem();
                if (held.isEmpty()) return say(c.getSource(), "Hold the reward in your hand.");
                if (t == p) return say(c.getSource(), "Not on yourself.");
                Bounties.get(c.getSource().getServer()).add(t.getUUID(), held.copy());
                held.setCount(0);
                c.getSource().getServer().getPlayerList().broadcastSystemMessage(Component.literal("A bounty is set on " + t.getGameProfile().name() + "."), false);
                return 1;
            })))
            .then(Commands.literal("list").executes(c -> {
                Bounties b = Bounties.get(c.getSource().getServer());
                for (UUID id : b.targets()) {
                    ServerPlayer t = c.getSource().getServer().getPlayerList().getPlayer(id);
                    say(c.getSource(), (t == null ? id.toString().substring(0, 8) : t.getGameProfile().name()) + ": " + b.count(id) + " items");
                }
                return 1;
            })));
    }

    private static int say(CommandSourceStack s, String t) {
        s.sendSystemMessage(Component.literal(t));
        return 0;
    }

    /** Rewards waiting on each target's head. */
    public static final class Bounties extends SavedData {
        public record Entry(UUID target, List<ItemStack> items) {
            static final Codec<Entry> CODEC = RecordCodecBuilder.create(i -> i.group(UUIDUtil.CODEC.fieldOf("target").forGetter(Entry::target),
                ItemStack.CODEC.listOf().fieldOf("items").forGetter(Entry::items)).apply(i, Entry::new));
        }

        public static final SavedDataType<Bounties> TYPE = new SavedDataType<>(Identifier.fromNamespaceAndPath(ID, "bounties"), Bounties::new,
            RecordCodecBuilder.create(i -> i.group(Entry.CODEC.listOf().fieldOf("entries").forGetter(b -> b.list)).apply(i, Bounties::new)), DataFixTypes.LEVEL);

        final List<Entry> list = new ArrayList<>();

        public Bounties() {}

        private Bounties(List<Entry> l) {
            for (Entry e : l) list.add(new Entry(e.target(), new ArrayList<>(e.items())));
        }

        public static Bounties get(MinecraftServer s) {
            return s.getLevel(Level.OVERWORLD).getDataStorage().computeIfAbsent(TYPE);
        }

        void add(UUID t, ItemStack s) {
            for (Entry e : list) {
                if (e.target().equals(t)) {
                    e.items().add(s);
                    setDirty();
                    return;
                }
            }
            list.add(new Entry(t, new ArrayList<>(List.of(s))));
            setDirty();
        }

        List<ItemStack> claim(UUID t) {
            for (Entry e : list) {
                if (e.target().equals(t)) {
                    list.remove(e);
                    setDirty();
                    return e.items();
                }
            }
            return List.of();
        }

        List<UUID> targets() {
            return list.stream().map(Entry::target).toList();
        }

        int count(UUID t) {
            return list.stream().filter(e -> e.target().equals(t)).mapToInt(e -> e.items().size()).sum();
        }
    }
}
