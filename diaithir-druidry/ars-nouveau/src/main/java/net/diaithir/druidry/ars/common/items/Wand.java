package net.diaithir.druidry.ars.common.items;

import net.diaithir.druidry.ars.api.item.ICasterTool;
import net.diaithir.druidry.ars.api.spell.*;
import net.diaithir.druidry.ars.common.spell.augment.AugmentAccelerate;
import net.diaithir.druidry.ars.common.spell.method.MethodProjectile;
import net.diaithir.druidry.ars.common.util.PortUtil;
import net.diaithir.druidry.ars.setup.registry.DataComponentRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class Wand extends ModItem implements ICasterTool {

    public Wand(Properties properties) {
        super(properties);
    }

    public Wand() {
        super(new Item.Properties().stacksTo(1).component(DataComponentRegistry.SPELL_CASTER, new SpellCaster()));
    }

    @Override
    public @NotNull InteractionResult use(@NotNull Level worldIn, Player playerIn, @NotNull InteractionHand handIn) {
        ItemStack stack = playerIn.getItemInHand(handIn);
        AbstractCaster<?> caster = getSpellCaster(stack);
        return caster.castSpell(worldIn, playerIn, handIn, Component.translatable("ars_nouveau.wand.invalid"));
    }





    @Override
    public boolean isScribedSpellValid(AbstractCaster<?> caster, Player player, InteractionHand hand, ItemStack stack, Spell spell) {
        return spell.mutable().recipe.stream().noneMatch(s -> s instanceof AbstractCastMethod);
    }

    @Override
    public void sendInvalidMessage(Player player) {
        PortUtil.sendMessageNoSpam(player, Component.translatable("ars_nouveau.wand.invalid"));
    }

    @Override
    public void scribeModifiedSpell(AbstractCaster<?> caster, Player player, InteractionHand hand, ItemStack stack, Spell.Mutable spell) {
        ArrayList<AbstractSpellPart> recipe = new ArrayList<>();
        recipe.add(MethodProjectile.INSTANCE);
        recipe.add(AugmentAccelerate.INSTANCE);
        recipe.addAll(spell.recipe);
        spell.recipe = recipe;
    }

    @Override
    public void appendHoverText(ItemStack stack, net.minecraft.world.item.Item.TooltipContext context, net.minecraft.world.item.component.TooltipDisplay tooltipDisplay, java.util.function.Consumer<net.minecraft.network.chat.Component> tooltip2, TooltipFlag flagIn) {
        { java.util.List<Component> info = new java.util.ArrayList<>(); getInformation(stack, context, info, flagIn); info.forEach(tooltip2); }
        super.appendHoverText(stack, context, tooltipDisplay, tooltip2, flagIn);
    }


}
