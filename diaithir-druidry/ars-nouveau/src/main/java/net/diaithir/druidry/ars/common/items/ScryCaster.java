package net.diaithir.druidry.ars.common.items;

import net.diaithir.druidry.ars.api.item.ICasterTool;
import net.diaithir.druidry.ars.api.spell.AbstractCaster;
import net.diaithir.druidry.ars.common.block.ScryerCrystal;
import net.diaithir.druidry.ars.common.items.data.ScryPosData;
import net.diaithir.druidry.ars.common.util.PortUtil;
import net.diaithir.druidry.ars.setup.registry.DataComponentRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Consumer;

public class ScryCaster extends ModItem implements ICasterTool {

    public ScryCaster(Properties properties) {
        super(properties);
    }

    public ScryCaster() {
        super();
    }

    @Override
    public @NotNull InteractionResult useOn(UseOnContext pContext) {
        BlockPos pos = pContext.getClickedPos();
        ItemStack stack = pContext.getItemInHand();
        if(pContext.getLevel().getBlockState(pos).getBlock() instanceof ScryerCrystal){
            if(!pContext.getLevel().isClientSide()) {
                stack.set(DataComponentRegistry.SCRY_DATA, new ScryPosData(net.minecraft.core.GlobalPos.of(pContext.getLevel().dimension(), pos)));
                PortUtil.sendMessage(pContext.getPlayer(), Component.translatable("ars_nouveau.dominion_wand.position_set"));
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Override
    public @NotNull InteractionResult use(@NotNull Level pLevel, Player pPlayer, @NotNull InteractionHand pUsedHand) {
        ItemStack stack = pPlayer.getItemInHand(pUsedHand);
        AbstractCaster<?> caster = getSpellCaster(stack);
        return caster.castSpell(pLevel, pPlayer, pUsedHand, Component.translatable("ars_nouveau.invalid_spell"));
    }

    @Override
    public void appendHoverText(ItemStack stack, net.minecraft.world.item.Item.TooltipContext context, net.minecraft.world.item.component.TooltipDisplay tooltipDisplay, java.util.function.Consumer<net.minecraft.network.chat.Component> tooltip2, TooltipFlag flagIn) {
        { java.util.List<Component> info = new java.util.ArrayList<>(); getInformation(stack, context, info, flagIn); info.forEach(tooltip2); }
        ScryPosData data = stack.get(DataComponentRegistry.SCRY_DATA);
        var pos = data.pos().orElse(null);

        if(pos == null){
            tooltip2.accept(Component.translatable("ars_nouveau.scry_caster.no_pos"));
        }else{
            tooltip2.accept(Component.translatable("ars_nouveau.scryer_scroll.bound", pos.pos().getX() + ", " + pos.pos().getY() + ", " + pos.pos().getZ()));
        }
        super.appendHoverText(stack, context, tooltipDisplay, tooltip2, flagIn);
    }






}
