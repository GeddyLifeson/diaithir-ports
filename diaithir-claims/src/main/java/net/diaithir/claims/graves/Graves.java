package net.diaithir.claims.graves;

import com.mojang.brigadier.CommandDispatcher;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.living.LivingExperienceDropEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/**
 * Death recovery: when a player dies, what they carried is set into a grave block where they fell (or the nearest free spot) instead of
 * lying on the ground. Clean-room: written from how grave mods behave (a grave where you die, a list of your graves), no code taken.
 */
public final class Graves {
    public static final String ID = "diaithirclaims";
    public static final int MAX_LISTED = 20;

    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(ID);
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ID);
    private static final DeferredRegister<BlockEntityType<?>> ENTITIES = DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, ID);
    private static final DeferredRegister<AttachmentType<?>> ATTACH = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, ID);

    public static final DeferredBlock<GraveBlock> GRAVE = BLOCKS.registerBlock("grave", GraveBlock::new,
        () -> BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(3.0F, 3600000.0F).sound(SoundType.DEEPSLATE).noOcclusion());
    public static final DeferredItem<?> GRAVE_ITEM = ITEMS.registerSimpleBlockItem(GRAVE);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<GraveEntity>> GRAVE_ENTITY = ENTITIES.register("grave",
        () -> new BlockEntityType<>(GraveEntity::new, GRAVE.get()));
    /** On a player: where their graves are, newest last. Survives death. */
    public static final Supplier<AttachmentType<List<GlobalPos>>> LIST = ATTACH.register("graves",
        () -> AttachmentType.<List<GlobalPos>>builder(() -> new ArrayList<>()).serialize(GlobalPos.CODEC.listOf().fieldOf("g")).copyOnDeath().build());

    private Graves() {}

    public static void init(IEventBus bus) {
        BLOCKS.register(bus);
        ITEMS.register(bus);
        ENTITIES.register(bus);
        ATTACH.register(bus);
        NeoForge.EVENT_BUS.addListener(Graves::onDrops);
        NeoForge.EVENT_BUS.addListener(Graves::onXp);
        NeoForge.EVENT_BUS.addListener((net.neoforged.neoforge.event.RegisterCommandsEvent e) -> register(e.getDispatcher()));
    }

    /** Nearest spot within 6 blocks that a grave can stand in; null if none (the items then drop as usual). */
    static BlockPos spot(ServerLevel level, BlockPos from) {
        int min = level.getMinY() + 1;
        int max = level.getMaxY() - 1;
        BlockPos start = new BlockPos(from.getX(), Math.max(min, Math.min(max, from.getY())), from.getZ());
        BlockPos best = null;
        double bestD = Double.MAX_VALUE;
        for (BlockPos p : BlockPos.betweenClosed(start.offset(-6, -6, -6), start.offset(6, 6, 6))) {
            if (p.getY() < min || p.getY() > max) continue;
            if (!level.getBlockState(p).canBeReplaced() || !level.getFluidState(p).isEmpty()) continue;
            double d = p.distSqr(start);
            if (d < bestD) {
                bestD = d;
                best = p.immutable();
            }
        }
        return best;
    }

    static void onDrops(LivingDropsEvent e) {
        if (!(e.getEntity() instanceof ServerPlayer p) || e.getDrops().isEmpty()) return;
        ServerLevel level = p.level();
        BlockPos pos = spot(level, p.blockPosition());
        if (pos == null) return;
        level.setBlockAndUpdate(pos, GRAVE.get().defaultBlockState());
        if (!(level.getBlockEntity(pos) instanceof GraveEntity g)) return;
        g.owner = p.getUUID();
        g.ownerName = p.getGameProfile().name();
        g.died = level.getGameTime();
        for (ItemEntity d : e.getDrops()) {
            if (!d.getItem().isEmpty()) g.items.add(d.getItem().copy());
        }
        g.setChanged();
        e.setCanceled(true);
        List<GlobalPos> list = p.getData(LIST);
        List<GlobalPos> next = new ArrayList<>(list);
        next.add(GlobalPos.of(level.dimension(), pos));
        while (next.size() > MAX_LISTED) next.remove(0);
        p.setData(LIST, next);
        p.sendSystemMessage(Component.literal("Your belongings rest in a grave at " + pos.getX() + " " + pos.getY() + " " + pos.getZ() + "."));
    }

    static void onXp(LivingExperienceDropEvent e) {
        if (!(e.getEntity() instanceof ServerPlayer p) || e.getDroppedExperience() <= 0) return;
        List<GlobalPos> list = p.getData(LIST);
        if (list.isEmpty()) return;
        GlobalPos last = list.get(list.size() - 1);
        ServerLevel level = p.level().getServer().getLevel(last.dimension());
        if (level != null && level.getBlockEntity(last.pos()) instanceof GraveEntity g && p.getUUID().equals(g.owner) && g.died == level.getGameTime()) {
            g.xp += e.getDroppedExperience();
            g.setChanged();
            e.setDroppedExperience(0);
        }
    }

    /** Gives everything in the grave to the player (what does not fit lands at their feet), then removes the grave. */
    static void empty(ServerLevel level, BlockPos pos, GraveEntity g, ServerPlayer taker) {
        for (ItemStack s : g.items) {
            ItemStack rest = s.copy();
            if (!taker.getInventory().add(rest) && !rest.isEmpty()) taker.drop(rest, false);
        }
        if (g.xp > 0) taker.giveExperiencePoints(g.xp);
        if (g.owner != null) {
            ServerPlayer o = level.getServer().getPlayerList().getPlayer(g.owner);
            if (o != null) {
                List<GlobalPos> next = new ArrayList<>(o.getData(LIST));
                next.remove(GlobalPos.of(level.dimension(), pos));
                o.setData(LIST, next);
            }
        }
        g.items.clear();
        level.removeBlock(pos, false);
    }

    private static void register(CommandDispatcher<CommandSourceStack> d) {
        d.register(Commands.literal("grave")
            .then(Commands.literal("list").executes(c -> {
                ServerPlayer p = c.getSource().getPlayerOrException();
                List<GlobalPos> l = p.getData(LIST);
                if (l.isEmpty()) c.getSource().sendSystemMessage(Component.literal("You have no graves."));
                for (GlobalPos g : l) {
                    c.getSource().sendSystemMessage(Component.literal(g.dimension().identifier() + " " + g.pos().getX() + " " + g.pos().getY() + " " + g.pos().getZ()));
                }
                return l.size();
            }))
            .then(Commands.literal("latest").executes(c -> {
                ServerPlayer p = c.getSource().getPlayerOrException();
                List<GlobalPos> l = p.getData(LIST);
                if (l.isEmpty()) {
                    c.getSource().sendSystemMessage(Component.literal("You have no graves."));
                    return 0;
                }
                GlobalPos g = l.get(l.size() - 1);
                int dist = g.dimension().equals(p.level().dimension()) ? (int) Math.sqrt(g.pos().distSqr(p.blockPosition())) : -1;
                c.getSource().sendSystemMessage(Component.literal("Latest grave: " + g.dimension().identifier() + " " + g.pos().getX() + " " + g.pos().getY() + " " + g.pos().getZ()
                    + (dist >= 0 ? ", " + dist + " m away." : ", in another dimension.")));
                return 1;
            })));
    }
}
