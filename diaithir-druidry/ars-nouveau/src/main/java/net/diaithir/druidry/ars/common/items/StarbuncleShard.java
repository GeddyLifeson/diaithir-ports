package net.diaithir.druidry.ars.common.items;

import net.diaithir.druidry.ars.common.items.data.StarbuncleCharmData;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.TypedDataComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class StarbuncleShard extends ModItem{

    public StarbuncleShard(){
        super();
        withTooltip(Component.translatable("tooltip.starbuncle_shard"));
    }

    private static StarbuncleCharmData findData(ItemStack stack) {
        for (TypedDataComponent<?> comp : stack.getComponents()) {
            Object value = comp.value();
            if (value instanceof StarbuncleCharmData d) {
                return d;
            }
        }
        return null;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context, @NotNull TooltipDisplay display, @NotNull Consumer<Component> tooltip2, @NotNull TooltipFlag flagIn) {
        StarbuncleCharmData data = findData(stack);
        if(data != null){
            data.getName().ifPresent(tooltip2::accept);
            if(data.getAdopter() != null && !data.getAdopter().isEmpty()){
                tooltip2.accept(Component.translatable("ars_nouveau.adopter", data.getAdopter()).withStyle(Style.EMPTY.withColor(ChatFormatting.GOLD)));
            }
            if(data.getBio() != null && !data.getBio().isEmpty()){
                tooltip2.accept(Component.literal(data.getBio()).withStyle(Style.EMPTY.withColor(ChatFormatting.DARK_PURPLE)));
            }
        }else{
            super.appendHoverText(stack, context, display, tooltip2, flagIn);
        }
    }
}
