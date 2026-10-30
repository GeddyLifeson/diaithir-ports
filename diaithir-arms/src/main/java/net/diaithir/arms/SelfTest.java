package net.diaithir.arms;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/** /armsselftest: builds every stack and checks the components that matter for its kind. */
public final class SelfTest {
    public static void register(CommandDispatcher<CommandSourceStack> d) {
        d.register(Commands.literal("armsselftest").requires(s -> s.permissions().hasPermission(net.minecraft.server.permissions.Permissions.COMMANDS_GAMEMASTER)).executes(c -> {
            int ok = 0, bad = 0;
            for (var s : DiaithirArms.DATA.items()) {
                ItemStack st = new ItemStack(DiaithirArms.all().get(s.id()).get());
                boolean good = st.getMaxDamage() > 0 && switch (s.kind()) {
                    case "armor" -> st.has(DataComponents.EQUIPPABLE) && st.has(DataComponents.ATTRIBUTE_MODIFIERS);
                    case "sword", "axe", "spear" -> st.has(DataComponents.ATTRIBUTE_MODIFIERS) && st.has(DataComponents.WEAPON);
                    case "shield" -> st.has(DataComponents.BLOCKS_ATTACKS) && st.has(DataComponents.EQUIPPABLE);
                    case "crossbow" -> st.has(DataComponents.CHARGED_PROJECTILES);
                    default -> true;
                };
                if (good) ok++; else { bad++; System.out.println("[armsselftest] BAD " + s.id()); }
            }
            System.out.println("[armsselftest] ok=" + ok + " bad=" + bad + " parts=" + DiaithirArms.DATA.parts().size());
            int fo = ok, fb = bad;
            c.getSource().sendSuccess(() -> Component.literal("arms ok=" + fo + " bad=" + fb), false);
            return fb == 0 ? 1 : 0;
        }));
    }
}
