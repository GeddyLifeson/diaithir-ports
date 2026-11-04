package net.diaithir.druidry.ars.common.items;

import net.diaithir.druidry.ars.api.event.EventQueue;
import net.diaithir.druidry.ars.common.event.timed.BuildPortalEvent;
import net.diaithir.druidry.ars.common.items.data.WarpScrollData;
import net.diaithir.druidry.ars.setup.registry.DataComponentRegistry;
import net.diaithir.druidry.ars.setup.registry.ItemsRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public class StableWarpScroll extends ModItem{

    public StableWarpScroll(Item.Properties properties) {
        super(properties.stacksTo(1).component(DataComponentRegistry.WARP_SCROLL, new WarpScrollData(true)));
    }

    @Override
    public boolean onEntityItemUpdate(@NotNull ItemStack stack, @NotNull ItemEntity entity) {
        return ItemsRegistry.WARP_SCROLL.get().onEntityItemUpdate(stack, entity);
    }

    @Override
    public @NotNull InteractionResult useOn(UseOnContext context) {
        if (!context.getLevel().isClientSide()) {
            WarpScrollData scrollData = context.getItemInHand().get(DataComponentRegistry.WARP_SCROLL);
            if(!scrollData.isValid())
                return InteractionResult.FAIL;
            EventQueue.getServerInstance().addEvent(new BuildPortalEvent(context.getLevel(), context.getClickedPos(), context.getPlayer().getDirection().getClockWise(), scrollData));
            context.getLevel().playSound(null, context.getClickedPos(), SoundEvents.ILLUSIONER_CAST_SPELL, context.getPlayer().getSoundSource(), 1.0F, 1.0F);
        }
        return super.useOn(context);
    }

    @Override
    public @NotNull InteractionResult use(@NotNull Level pLevel, @NotNull Player player, @NotNull InteractionHand pUsedHand) {
        if(pUsedHand != InteractionHand.MAIN_HAND)
            return InteractionResult.SUCCESS;
        ItemStack stack = player.getItemInHand(InteractionHand.MAIN_HAND);
        WarpScrollData data = stack.get(DataComponentRegistry.WARP_SCROLL);

        if (!(pLevel instanceof ServerLevel serverLevel))
            return InteractionResult.SUCCESS;
        if (player.isShiftKeyDown() && !data.isValid()) {
            stack.set(DataComponentRegistry.WARP_SCROLL, new WarpScrollData(Optional.of(player.blockPosition()), player.level().dimension().identifier().toString(), player.getRotationVector(), true));
            player.sendSystemMessage(Component.translatable("ars_nouveau.warp_scroll.recorded"));
        }else if(player.isShiftKeyDown() && data.isValid()){
            player.sendSystemMessage(Component.translatable("ars_nouveau.warp_scroll.already_recorded"));
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, net.minecraft.world.item.Item.TooltipContext context, net.minecraft.world.item.component.TooltipDisplay tooltipDisplay, java.util.function.Consumer<net.minecraft.network.chat.Component> tooltip2, TooltipFlag flagIn) {
        super.appendHoverText(stack, context, tooltipDisplay, tooltip2, flagIn);
        stack.addToTooltip(DataComponentRegistry.WARP_SCROLL, context, tooltipDisplay, tooltip2, flagIn);
    }
}
