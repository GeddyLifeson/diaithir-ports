package net.diaithir.forcefield;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/** /diaithirforcefield: guests, team access and status for the projector you look at; purge and selftest for operators. */
final class FieldCommands {
    private FieldCommands() {}

    static void register(CommandDispatcher<CommandSourceStack> d) {
        d.register(Commands.literal("diaithirforcefield")
                .then(Commands.literal("status").executes(c -> withProjector(c.getSource(), (be, p) -> {
                    msg(c.getSource(), be.summary() + ", modules " + modules(be) + ", guests " + be.guests().values() + ", team access "
                            + be.teamAccess());
                    return 1;
                })))
                .then(Commands.literal("allow").then(Commands.argument("player", EntityArgument.player()).executes(c -> {
                    ServerPlayer target = EntityArgument.getPlayer(c, "player");
                    return withProjector(c.getSource(), (be, p) -> {
                        if (be.moduleCount(Module.GUEST_LIST) == 0) return fail(c.getSource(), "Fit a guest list module first.");
                        be.addGuest(target.getUUID(), target.getGameProfile().name());
                        msg(c.getSource(), target.getGameProfile().name() + " may pass.");
                        return 1;
                    });
                })))
                .then(Commands.literal("deny").then(Commands.argument("player", EntityArgument.player()).executes(c -> {
                    ServerPlayer target = EntityArgument.getPlayer(c, "player");
                    return withProjector(c.getSource(), (be, p) -> {
                        msg(c.getSource(), be.removeGuest(target.getUUID()) ? target.getGameProfile().name() + " may no longer pass."
                                : "They were not on the list.");
                        return 1;
                    });
                })))
                .then(Commands.literal("team").then(Commands.argument("on", BoolArgumentType.bool()).executes(c -> withProjector(c.getSource(), (be, p) -> {
                    be.setTeamAccess(BoolArgumentType.getBool(c, "on"));
                    msg(c.getSource(), "Team access " + be.teamAccess());
                    return 1;
                }))))
                .then(Commands.literal("purge").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                        .then(Commands.argument("radius", IntegerArgumentType.integer(1, 64)).executes(FieldCommands::purge)))
                .then(Commands.literal("selftest").requires(Commands.hasPermission(Commands.LEVEL_OWNERS)).executes(c -> {
                    List<String> fails = FieldSelfTest.run(c.getSource().getServer());
                    String line = fails.isEmpty() ? "Forcefield self test PASS" : "Forcefield self test FAIL: " + String.join("; ", fails);
                    msg(c.getSource(), line);
                    return fails.isEmpty() ? 1 : 0;
                })));
    }

    private interface Use {
        int apply(ProjectorBlockEntity be, ServerPlayer player);
    }

    private static int withProjector(CommandSourceStack src, Use use) {
        ServerPlayer p = src.getPlayer();
        if (p == null) return fail(src, "Players only.");
        HitResult hit = p.pick(8.0, 0.0F, false);
        if (hit instanceof BlockHitResult bh && p.level().getBlockEntity(bh.getBlockPos()) instanceof ProjectorBlockEntity be) {
            if (!be.canManage(p)) return fail(src, "This projector is not yours.");
            return use.apply(be, p);
        }
        return fail(src, "Look at a projector within 8 blocks.");
    }

    private static String modules(ProjectorBlockEntity be) {
        StringBuilder sb = new StringBuilder();
        for (Module m : Module.values()) sb.append(m.name().toLowerCase()).append(' ').append(be.moduleCount(m)).append(' ');
        return sb.toString().trim();
    }

    private static int purge(com.mojang.brigadier.context.CommandContext<CommandSourceStack> c) {
        int r = IntegerArgumentType.getInteger(c, "radius");
        var level = c.getSource().getLevel();
        BlockPos o = BlockPos.containing(c.getSource().getPosition());
        int n = 0;
        for (BlockPos p : BlockPos.betweenClosed(o.offset(-r, -r, -r), o.offset(r, r, r))) {
            if (level.isInsideBuildHeight(p.getY()) && level.hasChunkAt(p) && level.getBlockState(p).is(DiaithirForcefield.FIELD.get())) {
                level.setBlock(p, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(), 2 | 16);
                n++;
            }
        }
        int removed = n;
        c.getSource().sendSuccess(() -> Component.literal("Removed " + removed + " field blocks"), true);
        return n;
    }

    private static void msg(CommandSourceStack src, String s) {
        src.sendSuccess(() -> Component.literal(s), false);
    }

    private static int fail(CommandSourceStack src, String s) {
        src.sendFailure(Component.literal(s));
        return 0;
    }
}
