package net.diaithir.ultimine;

import com.mojang.brigadier.CommandDispatcher;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.level.block.BreakBlockEvent;

@Mod(DiaithirUltimine.ID)
public final class DiaithirUltimine {
    public static final String ID = "diaithirultimine";
    public static final int MAX_BLOCKS = 64;
    static final Set<java.util.UUID> ON = new HashSet<>();
    private static boolean busy;

    public DiaithirUltimine(IEventBus bus) {
        NeoForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void commands(RegisterCommandsEvent e) {
        CommandDispatcher<CommandSourceStack> d = e.getDispatcher();
        d.register(Commands.literal("ultimine")
            .then(Commands.literal("selftest").requires(src -> src.permissions().hasPermission(net.minecraft.server.permissions.Permissions.COMMANDS_GAMEMASTER)).executes(c -> SelfTest.run(c.getSource().getPlayerOrException())))
            .executes(c -> {
            ServerPlayer p = c.getSource().getPlayerOrException();
            boolean on = !ON.remove(p.getUUID());
            if (on) {
                ON.add(p.getUUID());
            }
            p.sendSystemMessage(Component.literal("Ultimine " + (on ? "on" : "off")));
            return 1;
        }));
    }

    /** Connected blocks of the same kind as start, nearest first, including diagonals, at most max. */
    public static Set<BlockPos> vein(ServerLevel level, BlockPos start, int max) {
        Block kind = level.getBlockState(start).getBlock();
        Set<BlockPos> seen = new HashSet<>();
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        seen.add(start);
        queue.add(start);
        Set<BlockPos> out = new java.util.LinkedHashSet<>();
        while (!queue.isEmpty() && out.size() < max) {
            BlockPos at = queue.poll();
            out.add(at);
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        BlockPos n = at.offset(dx, dy, dz);
                        if (!seen.contains(n) && level.getBlockState(n).is(kind)) {
                            seen.add(n.immutable());
                            queue.add(n.immutable());
                        }
                    }
                }
            }
        }
        return out;
    }

    @SubscribeEvent
    public void onBreak(BreakBlockEvent e) {
        if (busy || !(e.getPlayer() instanceof ServerPlayer p) || !ON.contains(p.getUUID()) || !(e.getLevel() instanceof ServerLevel level)) {
            return;
        }
        ItemStack tool = p.getMainHandItem();
        BlockState state = e.getState();
        if (!p.hasCorrectToolForDrops(state) || tool.isEmpty()) {
            return;
        }
        busy = true;
        try {
            for (BlockPos pos : vein(level, e.getPos(), MAX_BLOCKS)) {
                if (pos.equals(e.getPos()) || p.getMainHandItem().isEmpty()) {
                    continue;
                }
                if (p.gameMode.destroyBlock(pos)) {
                    p.causeFoodExhaustion(0.005F);
                }
            }
        } finally {
            busy = false;
        }
    }
}
