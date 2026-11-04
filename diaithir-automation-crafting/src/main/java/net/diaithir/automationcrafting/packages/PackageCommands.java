package net.diaithir.automationcrafting.packages;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

/** /diaithirpackages list | check id | setresearched id true/false (ops). Same three jobs as the original mod's commands. */
public final class PackageCommands {
    private PackageCommands() {}

    public static void register(CommandDispatcher<CommandSourceStack> d) {
        d.register(Commands.literal("diaithirpackages")
                .then(Commands.literal("list").executes(c -> {
                    ResearchState rs = ResearchState.get(c.getSource().getServer());
                    for (PackageDef p : Packages.all()) {
                        c.getSource().sendSuccess(() -> Component.literal(p.id() + " (" + p.name() + "): " + (rs.has(p.id()) ? "researched" : "not researched")), false);
                    }
                    return Packages.all().size();
                }))
                .then(Commands.literal("check").then(Commands.argument("id", StringArgumentType.string()).executes(c -> {
                    String id = StringArgumentType.getString(c, "id");
                    boolean has = ResearchState.get(c.getSource().getServer()).has(id);
                    c.getSource().sendSuccess(() -> Component.literal(id + (has ? " is researched." : " is not researched.")), false);
                    return has ? 1 : 0;
                })))
                .then(Commands.literal("setresearched").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                        .then(Commands.argument("id", StringArgumentType.string()).then(Commands.argument("value", BoolArgumentType.bool()).executes(c -> {
                            String id = StringArgumentType.getString(c, "id");
                            if (Packages.get(id) == null) {
                                c.getSource().sendFailure(Component.literal("No package " + id));
                                return 0;
                            }
                            boolean v = BoolArgumentType.getBool(c, "value");
                            ResearchState.get(c.getSource().getServer()).set(id, v);
                            c.getSource().sendSuccess(() -> Component.literal(id + " set to " + (v ? "researched." : "not researched.")), true);
                            return 1;
                        })))));
    }
}
