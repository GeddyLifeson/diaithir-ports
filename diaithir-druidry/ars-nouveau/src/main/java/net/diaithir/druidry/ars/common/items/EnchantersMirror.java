package net.diaithir.druidry.ars.common.items;

import net.diaithir.druidry.ars.api.item.ICasterTool;
import net.diaithir.druidry.ars.api.item.ISpellModifierItem;
import net.diaithir.druidry.ars.api.mana.IManaDiscountEquipment;
import net.diaithir.druidry.ars.api.spell.*;
import net.diaithir.druidry.ars.client.renderer.item.MirrorRenderer;
import net.diaithir.druidry.ars.common.spell.method.MethodSelf;
import net.diaithir.druidry.ars.common.util.PortUtil;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class EnchantersMirror extends ModItem implements ICasterTool, ISpellModifierItem, IManaDiscountEquipment {

    public EnchantersMirror(Properties properties) {
        super(properties);
    }

    public EnchantersMirror() {
        super();
    }





    @Override
    public @NotNull InteractionResult use(@NotNull Level worldIn, Player playerIn, @NotNull InteractionHand handIn) {
        ItemStack stack = playerIn.getItemInHand(handIn);
        AbstractCaster<?> caster = getSpellCaster(stack);
        return caster.castSpell(worldIn, playerIn, handIn, Component.translatable("ars_nouveau.mirror.invalid"), caster.getSpell());
    }

    @Override
    public int getManaDiscount(ItemStack i, Spell spell) {
        return (int) (spell.getCost() * .25);
    }

    @Override
    public boolean isScribedSpellValid(AbstractCaster<?> caster, Player player, InteractionHand hand, ItemStack stack, Spell spell) {
        return spell.unsafeList().stream().noneMatch(s -> s instanceof AbstractCastMethod);
    }

    @Override
    public void sendInvalidMessage(Player player) {
        PortUtil.sendMessageNoSpam(player, Component.translatable("ars_nouveau.mirror.invalid"));
    }

    @Override
    public void scribeModifiedSpell(AbstractCaster<?> caster, Player player, InteractionHand hand, ItemStack stack, Spell.Mutable spell) {
        ArrayList<AbstractSpellPart> recipe = new ArrayList<>();
        recipe.add(MethodSelf.INSTANCE);
        recipe.addAll(spell.recipe);
        spell.recipe = recipe;
    }

    @Override
    public void appendHoverText(ItemStack stack, net.minecraft.world.item.Item.TooltipContext context, net.minecraft.world.item.component.TooltipDisplay tooltipDisplay, java.util.function.Consumer<net.minecraft.network.chat.Component> tooltip2, TooltipFlag flagIn) {
        getInformation(stack, context, tooltip2, flagIn);
        new SpellStats.Builder().addDurationModifier(1.0).build().addTooltip(tooltip2);
        super.appendHoverText(stack, context, tooltipDisplay, tooltip2, flagIn);
    }

    @Override
    public SpellStats.Builder applyItemModifiers(ItemStack stack, SpellStats.Builder builder, AbstractSpellPart spellPart, HitResult rayTraceResult, Level world, @Nullable LivingEntity shooter, SpellContext spellContext) {
        builder.addDurationModifier(1.0D);
        return builder;
    }



}
