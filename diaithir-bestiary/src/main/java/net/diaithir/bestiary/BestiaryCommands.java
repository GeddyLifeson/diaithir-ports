package net.diaithir.bestiary;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.IdentifierArgument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.phys.Vec3;

/** /bestiary list for everyone, summon for operators, selftest for owners. */
final class BestiaryCommands {
    private BestiaryCommands() {}

    static void register(CommandDispatcher<CommandSourceStack> d) {
        d.register(Commands.literal("bestiary")
                .then(Commands.literal("list").executes(c -> {
                    var creatures = DiaithirBestiary.creatures(c.getSource().getLevel());
                    creatures.entrySet().forEach(e -> c.getSource().sendSuccess(() -> Component.literal(e.getKey().identifier() + " (" + e.getValue().preset().getSerializedName() + ", ")
                            .append(Component.translatable(e.getValue().nameKey(e.getKey().identifier()))).append(")"), false));
                    return creatures.size();
                }))
                .then(Commands.literal("summon").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                        .then(Commands.argument("creature", IdentifierArgument.id())
                                .suggests((c, b) -> SharedSuggestionProvider.suggestResource(DiaithirBestiary.creatures(c.getSource().getLevel()).keySet(), b))
                                .executes(c -> summon(c, c.getSource().getPosition()))
                                .then(Commands.argument("pos", Vec3Argument.vec3()).executes(c -> summon(c, Vec3Argument.getVec3(c, "pos"))))))
                .then(Commands.literal("selftest").requires(Commands.hasPermission(Commands.LEVEL_OWNERS)).executes(c -> {
                    SelfTest.start(c.getSource());
                    return 1;
                })));
    }

    private static int summon(CommandContext<CommandSourceStack> c, Vec3 pos) {
        Identifier id = IdentifierArgument.getId(c, "creature");
        Creature creature = DiaithirBestiary.spawn(c.getSource().getLevel(), id, pos, EntitySpawnReason.COMMAND);
        if (creature == null) {
            c.getSource().sendFailure(Component.literal("No creature " + id));
            return 0;
        }
        c.getSource().sendSuccess(() -> Component.literal("Summoned ").append(creature.getDisplayName()), true);
        return 1;
    }
}
