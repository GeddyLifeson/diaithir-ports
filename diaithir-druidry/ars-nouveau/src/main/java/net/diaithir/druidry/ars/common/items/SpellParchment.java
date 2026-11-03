package net.diaithir.druidry.ars.common.items;

import net.diaithir.druidry.ars.api.documentation.DocClientUtils;
import net.diaithir.druidry.ars.api.item.ICasterTool;
import net.diaithir.druidry.ars.api.particle.timelines.IParticleTimeline;
import net.diaithir.druidry.ars.api.registry.SpellCasterRegistry;
import net.diaithir.druidry.ars.api.spell.AbstractCaster;
import net.diaithir.druidry.ars.api.spell.ITimelined;
import net.diaithir.druidry.ars.api.spell.SpellCaster;
import net.diaithir.druidry.ars.api.util.IWololoable;
import net.diaithir.druidry.ars.client.gui.SpellTooltip;
import net.diaithir.druidry.ars.client.particle.ParticleColor;
import net.diaithir.druidry.ars.setup.config.Config;
import net.diaithir.druidry.ars.setup.registry.DataComponentRegistry;
import net.diaithir.druidry.ars.setup.registry.ItemsRegistry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public class SpellParchment extends ModItem implements ICasterTool {

    public SpellParchment(Properties properties) {
        super(properties);
    }

    public SpellParchment() {
        super(ItemsRegistry.defaultItemProperties().component(DataComponents.BASE_COLOR, DyeColor.PURPLE).component(DataComponentRegistry.SPELL_CASTER, new SpellCaster()));
    }

    @Override
    public @NotNull Component getName(@NotNull ItemStack pStack) {
        AbstractCaster<?> caster = getSpellCaster(pStack);
        return caster.getSpellName().isEmpty() ? super.getName(pStack) : Component.literal(caster.getSpellName());
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context, @NotNull List<Component> tooltip2, @NotNull TooltipFlag flagIn) {
        stack.addToTooltip(DataComponentRegistry.SPELL_CASTER, context, component -> tooltip2.addAll(DocClientUtils.splitTooltip(component, context)), flagIn);
        super.appendHoverText(stack, context, tooltip2, flagIn);
    }
    
    @Override
    public @NotNull InteractionResult useOn(UseOnContext pContext) {
        if (!(pContext.getPlayer() instanceof ServerPlayer player) || !(pContext.getLevel() instanceof ServerLevel level)) {
            return super.useOn(pContext);
        }

        var tile = level.getBlockEntity(pContext.getClickedPos());
        var hand = pContext.getItemInHand();
        
        InteractionResult result = copyTimeline(player, level, hand, tile);
        if (result.consumesAction()) {
            return result;
        }

        return super.useOn(pContext);
    }

    public InteractionResult copyTimeline(ServerPlayer player, ServerLevel level, ItemStack hand, BlockEntity tile) {
        if (!(tile instanceof ITimelined<?> timelined)) {
            return InteractionResult.PASS;
        }

        //noinspection rawtypes
        IParticleTimeline timeline = timelined.getTimeline();
        ParticleColor color = tile instanceof IWololoable wololoable ? wololoable.getColor() : ParticleColor.defaultParticleColor();

        ItemStack stack = hand.copyWithCount(1);
        AbstractCaster<?> caster = SpellCasterRegistry.from(stack);
        if (caster == null) {
            return InteractionResult.FAIL;
        }

        //noinspection unchecked
        var newTimeline = caster.getSpell().particleTimeline().put(timeline.getType(), timeline);
        caster.setSpell(caster.getSpell().withColor(color).withTimeline(newTimeline)).saveToStack(stack);
        
        if (!player.hasInfiniteMaterials()) {
            hand.shrink(1);
        }

        if (!player.addItem(stack)) {
            level.addFreshEntity(new ItemEntity(level, player.getX(), player.getY(), player.getZ(), stack));
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public @NotNull Optional<TooltipComponent> getTooltipImage(@NotNull ItemStack pStack) {
        AbstractCaster<?> caster = getSpellCaster(pStack);
        if (caster != null && Config.GLYPH_TOOLTIPS.get() && !Screen.hasShiftDown() && !caster.isSpellHidden() && !caster.getSpell().isEmpty())
            return Optional.of(new SpellTooltip(caster));
        return Optional.empty();
    }
}
