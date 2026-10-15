package sonar.fluxnetworks.common.item;

import java.util.function.Consumer;

import net.minecraft.world.item.component.TooltipDisplay;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import sonar.fluxnetworks.FluxConfig;
import sonar.fluxnetworks.api.FluxTranslate;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class FluxDustItem extends Item {

    public FluxDustItem(@Nonnull Properties props) {
        super(props);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        if (FluxConfig.enableFluxRecipe) {
            tooltip.accept(FluxTranslate.FLUX_DUST_TOOLTIP.getComponent());
        }
        super.appendHoverText(stack, context, display, tooltip, flag);
    }
}
