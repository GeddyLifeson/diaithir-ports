package net.diaithir.spartan;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.ai.attributes.Attributes;

/** /spartanselftest: builds every weapon stack and checks its components. */
public final class SelfTest {
    public static void register(CommandDispatcher<CommandSourceStack> d) {
        d.register(Commands.literal("spartanselftest").requires(s -> s.permissions().hasPermission(net.minecraft.server.permissions.Permissions.COMMANDS_GAMEMASTER)).executes(c -> {
            int ok = 0, bad = 0;
            for (var en : SpartanWeaponry.weapons().entrySet()) {
                ItemStack st = new ItemStack(en.getValue().get());
                var attrs = st.get(DataComponents.ATTRIBUTE_MODIFIERS);
                boolean good = attrs != null && st.getMaxDamage() > 0 && st.has(DataComponents.WEAPON)
                    && attrs.modifiers().stream().anyMatch(m -> m.attribute().equals(Attributes.ATTACK_DAMAGE) && m.modifier().amount() > 0);
                if (good) ok++; else { bad++; System.out.println("[spartanselftest] BAD " + en.getKey()); }
            }
            System.out.println("[spartanselftest] ok=" + ok + " bad=" + bad);
            int fo = ok, fb = bad;
            c.getSource().sendSuccess(() -> Component.literal("spartan weapons ok=" + fo + " bad=" + fb), false);
            return fb == 0 ? 1 : 0;
        }));
    }
}
