package net.diaithir.druidry.ars.common.items;

import net.diaithir.druidry.ars.api.item.ICasterTool;
import net.diaithir.druidry.ars.api.mana.IManaDiscountEquipment;
import net.diaithir.druidry.ars.api.spell.AbstractCaster;
import net.diaithir.druidry.ars.api.spell.Spell;
import net.diaithir.druidry.ars.client.gui.SpellTooltip;
import net.diaithir.druidry.ars.common.items.data.TomeCasterData;
import net.diaithir.druidry.ars.setup.config.Config;
import net.diaithir.druidry.ars.setup.registry.DataComponentRegistry;
import net.diaithir.druidry.ars.setup.registry.ItemsRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public class CasterTome extends ModItem implements ICasterTool, IManaDiscountEquipment {

    public CasterTome(Properties properties) {
        super(properties);
    }

    public CasterTome() {
        super(ItemsRegistry.defaultItemProperties().component(DataComponentRegistry.TOME_CASTER, new TomeCasterData()));
    }

    @Override
    public boolean onScribe(Level world, BlockPos pos, Player player, InteractionHand handIn, ItemStack tableStack) {
        return player.isCreative() && ICasterTool.super.onScribe(world, pos, player, handIn, tableStack);
    }

    @Override
    public @NotNull InteractionResult use(@NotNull Level worldIn, Player playerIn, @NotNull InteractionHand handIn) {
        ItemStack stack = playerIn.getItemInHand(handIn);
        AbstractCaster<?> caster = getSpellCaster(stack);
        Spell spell = caster.getSpell();
        return caster.castSpell(worldIn, playerIn, handIn, Component.empty(), spell);
    }

    @Override
    public void appendHoverText(ItemStack stack, net.minecraft.world.item.Item.TooltipContext context, net.minecraft.world.item.component.TooltipDisplay tooltipDisplay, java.util.function.Consumer<net.minecraft.network.chat.Component> tooltip2, TooltipFlag flagIn) {
        AbstractCaster<?> caster = getSpellCaster(stack);

        if (Config.GLYPH_TOOLTIPS.get() || Screen.hasShiftDown()) {
            if (caster.isSpellHidden()) {
                tooltip2.accept(Component.literal(caster.getHiddenRecipe()).withStyle(Style.EMPTY.withFont(Identifier.fromNamespaceAndPath("minecraft", "alt")).withColor(ChatFormatting.GOLD)));
            }
            if (!caster.getFlavorText().isEmpty())
                tooltip2.accept(Component.literal(caster.getFlavorText()).withStyle(Style.EMPTY.withItalic(true).withColor(ChatFormatting.BLUE)));
        } else{
            getInformation(stack, context, tooltip2, flagIn);
        }

        tooltip2.accept(Component.translatable("tooltip.ars_nouveau.caster_tome"));

        super.appendHoverText(stack, context, tooltipDisplay, tooltip2, flagIn);
    }

    @Override
    public int getManaDiscount(ItemStack i, Spell spell) {
        return spell.getCost() / 2;
    }

    @Override
    public @NotNull Optional<TooltipComponent> getTooltipImage(@NotNull ItemStack pStack) {
        AbstractCaster<?> caster = getSpellCaster(pStack);
        if (!Screen.hasShiftDown() && Config.GLYPH_TOOLTIPS.get() && !caster.isSpellHidden() && !caster.getSpell().isEmpty())
            return Optional.of(new SpellTooltip(caster));
        return Optional.empty();
    }
}
