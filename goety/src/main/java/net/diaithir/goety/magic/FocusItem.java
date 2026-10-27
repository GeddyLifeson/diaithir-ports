package net.diaithir.goety.magic;

import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

/** A focus holds one spell; load it into a wand or staff of the matching school. */
public class FocusItem extends Item {
    private final String spellId;

    public FocusItem(String spellId, Properties props) {
        super(props);
        this.spellId = spellId;
    }

    public SpellDef spell() {
        return SpellDef.get(spellId);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext ctx, TooltipDisplay display, Consumer<Component> out, TooltipFlag flag) {
        SpellDef d = spell();
        if (d == null) return;
        out.accept(Component.translatable("goety.tooltip.cost", d.cost()).withStyle(ChatFormatting.AQUA));
        out.accept(Component.translatable("goety.tooltip.school", Component.translatable("goety.school." + d.type())).withStyle(ChatFormatting.LIGHT_PURPLE));
        out.accept(Component.translatable("goety.tooltip.load").withStyle(ChatFormatting.GRAY));
    }
}
