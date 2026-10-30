package net.diaithir.bosses;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;

/** /boss spawn|spawnall|list|age|champion, and /bossesselftest. */
public final class BossCommands {
    private static boolean gm(CommandSourceStack s) { return s.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER); }

    public static void register(CommandDispatcher<CommandSourceStack> d) {
        d.register(Commands.literal("boss").requires(BossCommands::gm)
            .then(Commands.literal("list").executes(c -> {
                Data.profiles().values().forEach(p -> c.getSource().sendSuccess(() -> Component.literal(
                    p.id + " (Age " + Ages.name(p.age) + ", " + p.entity + (p.mythic ? ", kit of " + p.creature_id : "") + ")"), false));
                return Data.profiles().size();
            }))
            .then(Commands.literal("spawn").then(Commands.argument("id", StringArgumentType.word())
                .suggests((c, b) -> net.minecraft.commands.SharedSuggestionProvider.suggest(Data.profiles().keySet(), b))
                .executes(c -> {
                    Data.Profile p = Data.byId(StringArgumentType.getString(c, "id"));
                    if (p == null) { c.getSource().sendFailure(Component.literal("no such boss")); return 0; }
                    Mob m = DiaithirBosses.spawn(c.getSource().getLevel(), p, c.getSource().getPosition(), null);
                    if (m == null) { c.getSource().sendFailure(Component.literal(p.entity + " is not installed")); return 0; }
                    return 1;
                })))
            .then(Commands.literal("spawnall").executes(c -> {
                int n = 0, i = 0;
                for (Data.Profile p : Data.profiles().values()) {
                    double a = i++ * 0.7;
                    Vec3 at = c.getSource().getPosition().add(Math.cos(a) * 30, 0, Math.sin(a) * 30);
                    if (DiaithirBosses.spawn(c.getSource().getLevel(), p, at, null) != null) n++;
                }
                int fn = n;
                c.getSource().sendSuccess(() -> Component.literal("spawned " + fn), true);
                return n;
            }))
            .then(Commands.literal("age").then(Commands.argument("n", IntegerArgumentType.integer(0, 8)).executes(c -> {
                int n = IntegerArgumentType.getInteger(c, "n");
                Ages.forced = n == 0 ? null : n;
                c.getSource().sendSuccess(() -> Component.literal(n == 0 ? "Age follows diaithir-ages" : "Age forced to " + Ages.name(n)), true);
                return 1;
            })))
            .then(Commands.literal("champion").executes(c -> {
                ServerLevel l = c.getSource().getLevel();
                var self = c.getSource().getEntityOrException();
                Mob best = null;
                double bd = 100;
                for (Mob m : l.getEntitiesOfClass(Mob.class, self.getBoundingBox().inflate(10))) {
                    if (Elites.isElite(m)) continue;
                    double dd = m.distanceToSqr(self);
                    if (dd < bd) { bd = dd; best = m; }
                }
                if (best == null) { c.getSource().sendFailure(Component.literal("no creature near")); return 0; }
                Elites.make(best, Ages.current(l.getServer()), RandomSource.create(), null);
                return 1;
            })));
        d.register(Commands.literal("bossesselftest").requires(BossCommands::gm).executes(c -> SelfTest.start(c.getSource())));
    }

    private BossCommands() {}
}
