package net.diaithir.nemesis;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import java.util.List;

/** /nemesis roster and chronicle for every player; list, forget, ambush and turn for operators; selftest for owners. */
final class NemesisCommands {
    private NemesisCommands() {}

    static void register(CommandDispatcher<CommandSourceStack> d) {
        d.register(Commands.literal("nemesis")
                .then(Commands.literal("roster").executes(c -> show(c, NemesisData.get(c.getSource().getServer())
                        .against(c.getSource().getPlayerOrException().getStringUUID()), "No one hunts you yet.")))
                .then(Commands.literal("chronicle").executes(c -> {
                    List<String> lines = NemesisData.get(c.getSource().getServer()).chronicle;
                    if (lines.isEmpty()) c.getSource().sendSuccess(() -> Component.literal("Nothing told yet."), false);
                    for (String line : lines) c.getSource().sendSuccess(() -> Component.literal(line), false);
                    return lines.size();
                }))
                .then(Commands.literal("list").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                        .executes(c -> show(c, NemesisData.get(c.getSource().getServer()).nemeses, "No nemeses.")))
                .then(Commands.literal("forget").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                        .then(Commands.argument("id", IntegerArgumentType.integer(1)).executes(NemesisCommands::forget)))
                .then(Commands.literal("ambush").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                        .then(Commands.argument("player", EntityArgument.player()).executes(NemesisCommands::ambush)))
                .then(Commands.literal("selftest").requires(Commands.hasPermission(Commands.LEVEL_OWNERS)).executes(c -> {
                    List<String> fails = NemesisSelfTest.run(c.getSource().getServer());
                    String line = fails.isEmpty() ? "Nemesis self test PASS" : "Nemesis self test FAIL: " + String.join("; ", fails);
                    c.getSource().sendSuccess(() -> Component.literal(line), false);
                    return fails.isEmpty() ? 1 : 0;
                }))
                .then(Commands.literal("turn").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                        .then(Commands.argument("age", StringArgumentType.word()).executes(c -> {
                            String age = StringArgumentType.getString(c, "age");
                            int n = DiaithirNemesis.succession(c.getSource().getServer(), age);
                            c.getSource().sendSuccess(() -> Component.literal(n + " heirs in Age " + age), true);
                            return n;
                        }))));
    }

    private static int show(CommandContext<CommandSourceStack> c, List<Nemesis> list, String none) {
        NemesisData data = NemesisData.get(c.getSource().getServer());
        if (list.isEmpty()) c.getSource().sendSuccess(() -> Component.literal(none), false);
        for (Nemesis n : list) {
            ItemStack trophy = data.trophy(n);
            String line = "#" + n.id + " " + n.fullName() + ", " + DiaithirNemesis.RULES.title(n) + " of " + n.warband
                    + (data.chief(n.warband) == n ? " (its chief)" : "") + " (" + n.type + "), kills " + n.kills
                    + (n.traits.isEmpty() ? "" : ", " + String.join(", ", n.traits).replace('_', ' '))
                    + (n.sire.isEmpty() ? "" : ", " + DiaithirNemesis.RULES.age(n.age).heir() + " " + n.sire)
                    + (n.cheated ? ", scarred" : "")
                    + (n.seen.isEmpty() ? "" : ", last seen near " + n.seen)
                    + (trophy.isEmpty() ? "" : ", carries " + trophy.getHoverName().getString());
            c.getSource().sendSuccess(() -> Component.literal(line), false);
        }
        return list.size();
    }

    private static int forget(CommandContext<CommandSourceStack> c) {
        NemesisData data = NemesisData.get(c.getSource().getServer());
        Nemesis n = data.byId(IntegerArgumentType.getInteger(c, "id"));
        if (n == null) {
            c.getSource().sendFailure(Component.literal("No such nemesis"));
            return 0;
        }
        DiaithirNemesis.despawn(c.getSource().getServer(), n);
        DiaithirNemesis.fallen(c.getSource().getServer(), data, n);
        c.getSource().sendSuccess(() -> Component.literal("Forgot " + n.fullName()), true);
        return 1;
    }

    // Testing: ignores the chance, the cooldown and the safe zone.
    private static int ambush(CommandContext<CommandSourceStack> c) throws CommandSyntaxException {
        ServerPlayer player = EntityArgument.getPlayer(c, "player");
        NemesisData data = NemesisData.get(c.getSource().getServer());
        for (Nemesis n : data.against(player.getStringUUID())) {
            if (DiaithirNemesis.ambush(c.getSource().getServer(), data, n, player)) {
                c.getSource().sendSuccess(() -> Component.literal(n.fullName() + " ambushes " + player.getName().getString()), true);
                return 1;
            }
        }
        c.getSource().sendFailure(Component.literal("No nemesis could reach " + player.getName().getString()));
        return 0;
    }
}
