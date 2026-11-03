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
                c.getSource().sendSuccess(() -> Component.literal(Data.planetBosses().size() + " planet bosses (use /boss spawn <id>; ids are lower-case Library entry ids)"), false);
                return Data.profiles().size() + Data.planetBosses().size();
            }))
            .then(Commands.literal("spawn").then(Commands.argument("id", StringArgumentType.word())
                .suggests((c, b) -> net.minecraft.commands.SharedSuggestionProvider.suggest(
                    java.util.stream.Stream.concat(Data.profiles().keySet().stream(), Data.planetBosses().keySet().stream()), b))
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
            .then(Commands.literal("mode").executes(c -> {
                var m = Difficulty.current(c.getSource().getServer());
                c.getSource().sendSuccess(() -> Component.literal("Boss mode: " + m.id), false);
                return Difficulty.rank(m.id);
            }).then(Commands.argument("id", StringArgumentType.word())
                .suggests((c, b) -> net.minecraft.commands.SharedSuggestionProvider.suggest(Difficulty.all().stream().map(m -> m.id), b))
                .executes(c -> {
                    String id = StringArgumentType.getString(c, "id");
                    if (!AscendantData.get(c.getSource().getServer()).raise(id)) { c.getSource().sendFailure(Component.literal("unknown mode, or not above the current one")); return 0; }
                    c.getSource().sendSuccess(() -> Component.literal("Boss mode raised to " + id), true);
                    return 1;
                })))
            .then(Commands.literal("rush")
                .then(Commands.literal("start").executes(c -> {
                    var src = c.getSource();
                    Component why = Rush.refusal(src.getServer());
                    if (why != null) { src.sendFailure(why); return 0; }
                    var ps = src.getLevel().players().stream().filter(p -> p.isAlive() && !p.isSpectator() && p.distanceToSqr(src.getPosition()) < 64 * 64).toList();
                    if (ps.isEmpty()) { src.sendFailure(Component.literal("no fighters near")); return 0; }
                    Rush.start(src.getLevel(), src.getPosition(), ps);
                    return ps.size();
                }))
                .then(Commands.literal("stop").executes(c -> { Rush.stop("The rush was stopped."); return 1; })))
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
