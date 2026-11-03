package net.diaithir.aurorian;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

/** /aurorianselftest portal x y z: finds the frame next to (x,y,z), lights it and reports the interior size. */
final class SelfTest {
    static void register(CommandDispatcher<CommandSourceStack> d) {
        d.register(Commands.literal("aurorianselftest").requires(s -> s.permissions().hasPermission(net.minecraft.server.permissions.Permissions.COMMANDS_GAMEMASTER))
            .then(Commands.literal("portal").then(Commands.argument("pos", net.minecraft.commands.arguments.coordinates.BlockPosArgument.blockPos()).executes(c -> {
                BlockPos pos = net.minecraft.commands.arguments.coordinates.BlockPosArgument.getLoadedBlockPos(c, "pos");
                PortalFrameShape shape = PortalFrameShape.find(c.getSource().getLevel(), pos);
                if (shape == null) {
                    c.getSource().sendFailure(Component.literal("portal: no complete frame"));
                    return 0;
                }
                PortalFrameBlock.light(c.getSource().getLevel(), shape);
                c.getSource().sendSuccess(() -> Component.literal("portal: lit " + shape.interior.size() + " blocks, axis " + shape.axis), false);
                return shape.interior.size();
            }))));
    }
}
