package net.diaithir.druidry.ars.common.items;

import net.diaithir.druidry.ars.common.items.data.StarbuncleCharmData;
import net.diaithir.druidry.ars.setup.registry.DataComponentRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class StarbuncleShard extends ModItem{

    public StarbuncleShard(){
        super();
        withTooltip(Component.translatable("tooltip.starbuncle_shard"));
    }

    @Override
    public void appendHoverText(ItemStack stack, net.minecraft.world.item.Item.TooltipContext context, net.minecraft.world.item.component.TooltipDisplay tooltipDisplay, java.util.function.Consumer<net.minecraft.network.chat.Component> tooltip2, TooltipFlag flagIn) {
        StarbuncleCharmData data = stack.get(DataComponentRegistry.STARBUNCLE_DATA);
        if(data != null){
            data.getName().ifPresent(tooltip2::add);
            if(data.getAdopter() != null && !data.getAdopter().isEmpty()){
                tooltip2.accept(Component.translatable("ars_nouveau.adopter", data.getAdopter()).withStyle(Style.EMPTY.withColor(ChatFormatting.GOLD)));
            }
            if(data.getBio() != null && !data.getBio().isEmpty()){
                tooltip2.accept(Component.literal(data.getBio()).withStyle(Style.EMPTY.withColor(ChatFormatting.DARK_PURPLE)));
            }
        }else{
            super.appendHoverText(stack, context, tooltipDisplay, tooltip2, flagIn);
        }
    }
}
