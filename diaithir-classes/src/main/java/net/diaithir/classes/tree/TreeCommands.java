package net.diaithir.classes.tree;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import java.util.UUID;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerXpEvent;

/** /skills class|show|unlock|reset (anyone), /skills give (ops), /skilltreeselftest (ops); plus the events that earn points and re-apply nodes. */
public final class TreeCommands {
    private static void say(CommandSourceStack s, String m) { s.sendSuccess(() -> Component.literal(m), false); }

    public static void register(CommandDispatcher<CommandSourceStack> d) {
        d.register(Commands.literal("skills")
            .then(Commands.literal("class").then(Commands.argument("name", StringArgumentType.word())
                .suggests((c, b) -> SharedSuggestionProvider.suggest(Trees.DATA.classes().keySet(), b))
                .executes(c -> {
                    ServerPlayer p = c.getSource().getPlayerOrException();
                    String n = StringArgumentType.getString(c, "name");
                    if (!Trees.DATA.classes().containsKey(n)) { c.getSource().sendFailure(Component.literal("Unknown class " + n)); return 0; }
                    TreeData.get(c.getSource().getServer()).choose(p.getUUID(), n);
                    Trees.apply(p);
                    say(c.getSource(), "Class set to " + n + "; points refunded.");
                    return 1;
                })))
            .then(Commands.literal("show").executes(c -> {
                ServerPlayer p = c.getSource().getPlayerOrException();
                var e = TreeData.get(c.getSource().getServer()).entry(p.getUUID());
                if (e.cls() == null) { say(c.getSource(), "Choose a class: /skills class <fian|sleagh|ceilg|cleirech>"); return 0; }
                say(c.getSource(), e.cls() + ", " + e.points() + " points");
                for (var n : Trees.DATA.classes().get(e.cls()))
                    say(c.getSource(), (e.unlocked().contains(n.id()) ? "[x] " : "[ ] ") + n.id() + " (" + n.cost() + ")" + (n.parent() == null ? "" : " after " + n.parent()) + ": " + n.attr() + " " + n.amount());
                return 1;
            }))
            .then(Commands.literal("unlock").then(Commands.argument("node", StringArgumentType.word()).executes(c -> {
                ServerPlayer p = c.getSource().getPlayerOrException();
                var td = TreeData.get(c.getSource().getServer());
                var e = td.entry(p.getUUID());
                String id = StringArgumentType.getString(c, "node");
                String why = Trees.whyNot(e, e.cls() == null ? null : Trees.node(e.cls(), id));
                if (why != null) { c.getSource().sendFailure(Component.literal("Cannot unlock: " + why)); return 0; }
                td.unlock(p.getUUID(), id);
                Trees.apply(p);
                say(c.getSource(), "Unlocked " + id);
                return 1;
            })))
            .then(Commands.literal("reset").executes(c -> {
                ServerPlayer p = c.getSource().getPlayerOrException();
                TreeData.get(c.getSource().getServer()).reset(p.getUUID());
                Trees.apply(p);
                say(c.getSource(), "Skills reset; points refunded.");
                return 1;
            }))
            .then(Commands.literal("give").requires(s -> s.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER))
                .then(Commands.argument("player", EntityArgument.player()).then(Commands.argument("points", IntegerArgumentType.integer(1, 100)).executes(c -> {
                    ServerPlayer p = EntityArgument.getPlayer(c, "player");
                    TreeData.get(c.getSource().getServer()).addPoints(p.getUUID(), IntegerArgumentType.getInteger(c, "points"));
                    return 1;
                })))));
        d.register(Commands.literal("skilltreeselftest").requires(s -> s.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER)).executes(c -> {
            int[] r = selftest(c.getSource().getServer());
            say(c.getSource(), "skill trees ok=" + r[0] + " bad=" + r[1]);
            return r[1] == 0 ? 1 : 0;
        }));
    }

    static int[] selftest(net.minecraft.server.MinecraftServer srv) {
        int ok = 0, bad = 0;
        var classes = Trees.DATA.classes();
        boolean data = classes.keySet().equals(java.util.Set.of("fian", "sleagh", "ceilg", "cleirech"));
        for (var e : classes.entrySet()) {
            var ids = new java.util.HashSet<String>();
            e.getValue().forEach(n -> ids.add(n.id()));
            data &= e.getValue().size() == 8 && ids.size() == 8;
            for (var n : e.getValue()) {
                data &= n.cost() > 0 && (n.parent() == null || ids.contains(n.parent()));
                // cross-module attributes (diaithircombat) may be absent when this jar runs alone
                data &= net.minecraft.core.registries.BuiltInRegistries.ATTRIBUTE.containsKey(net.minecraft.resources.Identifier.parse(n.attr())) || n.attr().startsWith("diaithircombat:");
            }
        }
        if (data) ok++; else bad++;
        TreeData td = new TreeData();
        UUID u = UUID.randomUUID();
        boolean a = td.unlock(u, "hardy") == false;              // no class yet
        td.choose(u, "fian"); td.addPoints(u, 3);
        boolean b = !td.unlock(u, "shieldwall");                  // parent missing
        boolean c = td.unlock(u, "hardy") && td.entry(u).points() == 2;
        boolean d = td.unlock(u, "shieldwall") && td.entry(u).points() == 1;
        boolean e = !td.unlock(u, "ironhide");                    // cost 2 > 1 point
        td.reset(u);
        boolean f = td.entry(u).points() == 3 && td.entry(u).unlocked().isEmpty();
        td.unlock(u, "hardy"); td.choose(u, "ceilg");
        boolean g = td.entry(u).points() == 3 && "ceilg".equals(td.entry(u).cls());
        for (boolean x : new boolean[] {a, b, c, d, e, f, g}) if (x) ok++; else bad++;
        return new int[] {ok, bad};
    }

    @SubscribeEvent
    public void xp(PlayerXpEvent.LevelChange e) {
        if (e.getEntity() instanceof ServerPlayer p && e.getLevels() > 0) TreeData.get(p.level().getServer()).addPoints(p.getUUID(), e.getLevels());
    }

    @SubscribeEvent public void login(PlayerEvent.PlayerLoggedInEvent e) { if (e.getEntity() instanceof ServerPlayer p) Trees.apply(p); }
    @SubscribeEvent public void respawn(PlayerEvent.PlayerRespawnEvent e) { if (e.getEntity() instanceof ServerPlayer p) Trees.apply(p); }
    @SubscribeEvent public void clone(PlayerEvent.Clone e) { if (e.getEntity() instanceof ServerPlayer p) Trees.apply(p); }
}
