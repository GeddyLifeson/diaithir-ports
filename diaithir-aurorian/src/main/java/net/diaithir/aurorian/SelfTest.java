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
            })))
            .then(Commands.literal("steel").executes(c -> {
                net.minecraft.world.item.ItemStack st = new net.minecraft.world.item.ItemStack(DiaithirAurorian.item("aurorian_steel_sword"));
                var ench = c.getSource().getLevel().registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT);
                net.minecraft.world.item.enchantment.EnchantmentHelper.updateEnchantments(st, m -> m.set(ench.getOrThrow(net.minecraft.world.item.enchantment.Enchantments.SHARPNESS), 1));
                var user = c.getSource().getEntityOrException() instanceof net.minecraft.world.entity.LivingEntity l ? l : null;
                for (int i = 0; i < 100; i++) {
                    Abilities.damage(st, 1, user, "aurorian_steel_sword");
                }
                String r = "steel: " + st.getEnchantments() .entrySet().stream().map(e -> e.getKey().getRegisteredName() + " " + e.getIntValue()).toList() + " " + st.get(net.minecraft.core.component.DataComponents.CUSTOM_DATA);
                c.getSource().sendSuccess(() -> Component.literal(r), false);
                return 1;
            })));
    }
}
