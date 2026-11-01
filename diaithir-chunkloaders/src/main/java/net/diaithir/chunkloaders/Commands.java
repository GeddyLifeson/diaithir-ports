package net.diaithir.chunkloaders;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

/** /diaithirchunkloaders list (your own and your team's), list all (operators), selftest (owners). */
final class Commands {
    private Commands() {}

    static void register(CommandDispatcher<CommandSourceStack> d) {
        d.register(net.minecraft.commands.Commands.literal(DiaithirChunkLoaders.ID)
                .then(net.minecraft.commands.Commands.literal("list").executes(c -> list(c.getSource(), false))
                        .then(net.minecraft.commands.Commands.literal("all").requires(net.minecraft.commands.Commands.hasPermission(net.minecraft.commands.Commands.LEVEL_GAMEMASTERS))
                                .executes(c -> list(c.getSource(), true))))
                .then(net.minecraft.commands.Commands.literal("selftest").requires(net.minecraft.commands.Commands.hasPermission(net.minecraft.commands.Commands.LEVEL_OWNERS)).executes(c -> {
                    List<String> fails = SelfTest.run(c.getSource().getServer());
                    String line = fails.isEmpty() ? "Chunkloaders self test PASS" : "Chunkloaders self test FAIL: " + String.join("; ", fails);
                    c.getSource().sendSuccess(() -> Component.literal(line), false);
                    return fails.isEmpty() ? 1 : 0;
                })));
    }

    private static int list(CommandSourceStack src, boolean all) {
        MinecraftServer server = src.getServer();
        ServerPlayer me = src.getPlayer();
        int shown = 0;
        for (LoaderData.Entry e : List.copyOf(LoaderData.get(server).loaders)) {
            ServerLevel level = Carts.levelOf(server, e.dim());
            LoaderBlockEntity be = level != null && level.getChunkSource().getChunkNow(e.pos().getX() >> 4, e.pos().getZ() >> 4) != null
                    && level.getBlockEntity(e.pos()) instanceof LoaderBlockEntity b ? b : null;
            if (!all && me != null && be != null && !be.canManage(me)) continue;
            if (!all && me != null && be == null && !e.owner().equals(me.getStringUUID())) continue;
            String line = "T" + e.tier() + " " + e.dim() + " " + e.pos().getX() + " " + e.pos().getY() + " " + e.pos().getZ()
                    + (e.owner().isEmpty() ? "" : ", " + e.ownerName())
                    + (be == null ? ", unloaded" : be.fuel > 0 ? ", " + (2 * be.radius() + 1) + "x" + (2 * be.radius() + 1) + " chunks, " + LoaderBlock.duration(be.secondsLeft()) + " left" : ", out of fuel");
            src.sendSuccess(() -> Component.literal(line), false);
            shown++;
        }
        for (var en : Carts.ACTIVE.entrySet()) {
            if (!all && me != null && !en.getValue().owner.equals(me.getStringUUID())) continue;
            src.sendSuccess(() -> Component.literal("Cart " + en.getKey() + " in " + en.getValue().dim + " keeping " + Carts.side() + "x" + Carts.side() + " chunks"), false);
            shown++;
        }
        if (shown == 0) src.sendSuccess(() -> Component.literal("No loaders."), false);
        return shown;
    }
}
