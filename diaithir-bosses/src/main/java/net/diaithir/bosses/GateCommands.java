package net.diaithir.bosses;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.server.permissions.Permissions;

/** /gate status|grant|revoke|reset, and /gateselftest. Quests and modules call grant when a requirement is met. */
public final class GateCommands {
    private static boolean gm(CommandSourceStack s) { return s.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER); }

    public static void register(CommandDispatcher<CommandSourceStack> d) {
        d.register(Commands.literal("gate").requires(GateCommands::gm)
            .then(Commands.literal("status").executes(c -> {
                var s = c.getSource().getServer();
                for (String id : Gate.NEEDS.keySet()) {
                    boolean m = Gate.met(s, id);
                    c.getSource().sendSuccess(() -> Component.literal((m ? "[x] " : "[ ] ") + Gate.NEEDS.get(id) + " (" + id + ")"), false);
                }
                c.getSource().sendSuccess(() -> Component.literal(GateData.get(s).isOpen() ? "The Gate is open." : "The Gate is closed."), false);
                return Gate.NEEDS.size() - Gate.missing(s).size();
            }))
            .then(Commands.literal("grant").then(Commands.argument("id", StringArgumentType.word())
                .suggests((c, b) -> SharedSuggestionProvider.suggest(Gate.NEEDS.keySet(), b))
                .executes(c -> {
                    String id = StringArgumentType.getString(c, "id");
                    if (!Gate.NEEDS.containsKey(id)) { c.getSource().sendFailure(Component.literal("Not a Gate requirement: " + id)); return 0; }
                    return GateData.get(c.getSource().getServer()).grant(id) ? 1 : 0;
                })))
            .then(Commands.literal("revoke").then(Commands.argument("id", StringArgumentType.word())
                .suggests((c, b) -> SharedSuggestionProvider.suggest(Gate.NEEDS.keySet(), b))
                .executes(c -> GateData.get(c.getSource().getServer()).revoke(StringArgumentType.getString(c, "id")) ? 1 : 0)))
            .then(Commands.literal("reset").executes(c -> { GateData.get(c.getSource().getServer()).reset(); return 1; })));
        d.register(Commands.literal("gateselftest").requires(GateCommands::gm).executes(c -> {
            var s = c.getSource().getServer();
            GateData gd = GateData.get(s);
            gd.reset();
            int ok = 0, bad = 0;
            StringBuilder why = new StringBuilder();
            boolean[] r = new boolean[8];
            r[0] = Gate.NEEDS.size() == 18;
            r[1] = Gate.missing(s).size() == 18;
            gd.grant("planner");
            r[2] = Gate.met(s, "planner") && Gate.missing(s).size() == 17;
            r[3] = !gd.grant("planner");
            for (String id : Gate.NEEDS.keySet()) gd.grant(id);
            r[4] = Gate.missing(s).isEmpty();
            gd.revoke("feast.shard");
            r[5] = Gate.missing(s).equals(java.util.List.of("feast.shard"));
            gd.grant("feast.shard");
            Gate.begin(c.getSource().getLevel(), net.minecraft.core.BlockPos.containing(c.getSource().getPosition()));
            r[6] = Gate.running() && !gd.isOpen();
            for (int i = 0; i < r.length - 1; i++) if (r[i]) ok++; else { bad++; why.append(" | check ").append(i); }
            if (r[6]) ok++; else { bad++; why.append(" | ceremony start"); }
            final String msg = "gate ok=" + ok + " bad=" + bad + why + " (ceremony running; wait about 21 s, then /gate status should say open)";
            c.getSource().sendSuccess(() -> Component.literal(msg), false);
            return bad == 0 ? 1 : 0;
        }));
    }
}
