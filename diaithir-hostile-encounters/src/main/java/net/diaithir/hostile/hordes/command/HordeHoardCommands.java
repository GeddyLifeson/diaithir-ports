package net.diaithir.hostile.hordes.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.diaithir.hostile.hordes.procedures.HordeSpawnerProcedure;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collection;

/**
 * Server-side commands for Horde Hoard. Wired in per loader:
 *   - Fabric:   CommandRegistrationCallback (fabric-command-api-v2)
 *   - NeoForge: RegisterCommandsEvent
 *
 * Both callers route to {@link #register(CommandDispatcher)}.
 *
 * Ring (default — 30-80 block ring around the player):
 *   /horde                            100 mobs around self
 *   /horde <count>                    N mobs around self (1-1000)
 *   /horde at <player>                100 mobs around target(s)
 *   /horde at <player> <count>        N mobs around target(s)
 *
 * Clump (single tight cluster ~12 blocks ahead of where you're looking):
 *   /horde clump                      100 mobs in a 3-block clump
 *   /horde clump <count>              N mobs in a clump
 *   /horde clump at <player>          100 mobs in a clump in front of target(s)
 *   /horde clump at <player> <count>  N mobs in a clump in front of target(s)
 *
 * Op level 2 (gamemasters) required, consistent with /summon.
 */
public final class HordeHoardCommands {

    private enum Shape { RING, CLUMP }

    private HordeHoardCommands() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("horde")
                        .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                        // /horde [count] [at <player> [count]]
                        .executes(ctx -> spawnAtSelf(ctx, HordeSpawnerProcedure.DEFAULT_HORDE_SIZE, Shape.RING))
                        .then(Commands.argument("count", IntegerArgumentType.integer(1, 1000))
                                .executes(ctx -> spawnAtSelf(ctx, IntegerArgumentType.getInteger(ctx, "count"), Shape.RING)))
                        .then(Commands.literal("at")
                                .then(Commands.argument("targets", EntityArgument.players())
                                        .executes(ctx -> spawnAtTargets(ctx, HordeSpawnerProcedure.DEFAULT_HORDE_SIZE, Shape.RING))
                                        .then(Commands.argument("count", IntegerArgumentType.integer(1, 1000))
                                                .executes(ctx -> spawnAtTargets(ctx, IntegerArgumentType.getInteger(ctx, "count"), Shape.RING)))))
                        // /horde clump [count] [at <player> [count]]
                        .then(Commands.literal("clump")
                                .executes(ctx -> spawnAtSelf(ctx, HordeSpawnerProcedure.DEFAULT_HORDE_SIZE, Shape.CLUMP))
                                .then(Commands.argument("count", IntegerArgumentType.integer(1, 1000))
                                        .executes(ctx -> spawnAtSelf(ctx, IntegerArgumentType.getInteger(ctx, "count"), Shape.CLUMP)))
                                .then(Commands.literal("at")
                                        .then(Commands.argument("targets", EntityArgument.players())
                                                .executes(ctx -> spawnAtTargets(ctx, HordeSpawnerProcedure.DEFAULT_HORDE_SIZE, Shape.CLUMP))
                                                .then(Commands.argument("count", IntegerArgumentType.integer(1, 1000))
                                                        .executes(ctx -> spawnAtTargets(ctx, IntegerArgumentType.getInteger(ctx, "count"), Shape.CLUMP))))))
        );
    }

    private static int spawnAtSelf(CommandContext<CommandSourceStack> ctx, int count, Shape shape) {
        try {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            return spawnFor(ctx, player, count, shape);
        } catch (CommandSyntaxException e) {
            ctx.getSource().sendFailure(Component.literal("/horde must be executed by a player (or use /horde at <player>)"));
            return 0;
        }
    }

    private static int spawnAtTargets(CommandContext<CommandSourceStack> ctx, int count, Shape shape) throws CommandSyntaxException {
        Collection<ServerPlayer> targets = EntityArgument.getPlayers(ctx, "targets");
        int total = 0;
        for (ServerPlayer target : targets) {
            total += spawnFor(ctx, target, count, shape);
        }
        return total;
    }

    private static int spawnFor(CommandContext<CommandSourceStack> ctx, ServerPlayer target, int count, Shape shape) {
        ServerLevel level = target.level();
        int spawned = switch (shape) {
            case RING -> HordeSpawnerProcedure.spawnHordeAround(level, target, count);
            case CLUMP -> HordeSpawnerProcedure.spawnHordeClump(level, target, count);
        };
        String shapeName = shape == Shape.CLUMP ? "clump" : "ring";
        ctx.getSource().sendSuccess(
                () -> Component.literal("Spawned " + shapeName + " of " + spawned + " mobs around " + target.getName().getString()),
                true
        );
        return spawned;
    }
}
