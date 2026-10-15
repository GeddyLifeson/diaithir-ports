package mcjty.rftoolsbase.modules.various.items;

import java.util.function.Consumer;

import net.minecraft.world.item.component.TooltipDisplay;

import mcjty.lib.builder.TooltipBuilder;
import mcjty.lib.tooltips.ITooltipSettings;
import mcjty.lib.varia.Tools;
import mcjty.rftoolsbase.RFToolsBase;
import mcjty.rftoolsbase.setup.Registration;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.util.Lazy;

import javax.annotation.Nonnull;
import java.util.List;

import static mcjty.lib.builder.TooltipBuilder.header;
import static mcjty.lib.builder.TooltipBuilder.key;

public class ManualItem extends Item implements ITooltipSettings {

    private final Lazy<TooltipBuilder> tooltipBuilder = Lazy.of(() -> new TooltipBuilder()
            .info(key("message.rftoolsbase.shiftmessage"))
            .infoShift(header()));

    public ManualItem() {
        super(Registration.createStandardProperties().stacksTo(1));
    }


    @Override
    public void appendHoverText(@Nonnull ItemStack itemStack, Item.TooltipContext context, TooltipDisplay display, @Nonnull Consumer<Component> list, @Nonnull TooltipFlag flags) {
        super.appendHoverText(itemStack, context, display, list, flags);
        tooltipBuilder.get().makeTooltip(Tools.getId(this), itemStack, list, flags);
    }
}
