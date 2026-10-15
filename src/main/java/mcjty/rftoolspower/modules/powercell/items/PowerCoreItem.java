package mcjty.rftoolspower.modules.powercell.items;

import java.util.function.Consumer;

import net.minecraft.world.item.component.TooltipDisplay;

import mcjty.lib.builder.TooltipBuilder;
import mcjty.lib.tooltips.ITooltipSettings;
import mcjty.lib.varia.Tools;
import mcjty.rftoolspower.RFToolsPower;
import mcjty.lib.gui.ManualEntry;
import mcjty.rftoolsbase.tools.ManualHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.neoforge.common.util.Lazy;

import javax.annotation.Nonnull;

import static mcjty.lib.builder.TooltipBuilder.header;

public class PowerCoreItem extends Item implements ITooltipSettings {

    private final Lazy<TooltipBuilder> tooltipBuilder = Lazy.of(() -> new TooltipBuilder()
            .info(header()));

    public PowerCoreItem() {
        super(RFToolsPower.setup.defaultProperties());
    }

    @Override
    public void appendHoverText(@Nonnull ItemStack stack, Item.TooltipContext worldIn, TooltipDisplay display, @Nonnull Consumer<Component> tooltip, @Nonnull TooltipFlag flagIn) {
        super.appendHoverText(stack, worldIn, display, tooltip, flagIn);
        tooltipBuilder.get().makeTooltip(Tools.getId(this), stack, tooltip, flagIn);
    }

    @Override
    public ManualEntry getManualEntry() {
        return ManualHelper.create("rftoolspower:powerstorage/powercell");
    }

}
