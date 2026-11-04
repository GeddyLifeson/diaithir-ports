package net.diaithir.druidry.ars.common.items;

import net.diaithir.druidry.ars.api.util.SourceUtil;
import net.diaithir.druidry.ars.common.advancement.ANCriteriaTriggers;
import net.diaithir.druidry.ars.common.items.data.WarpScrollData;
import net.diaithir.druidry.ars.common.network.Networking;
import net.diaithir.druidry.ars.common.network.PacketWarpPosition;
import net.diaithir.druidry.ars.setup.registry.BlockRegistry;
import net.diaithir.druidry.ars.setup.registry.DataComponentRegistry;
import net.diaithir.druidry.ars.setup.registry.ItemsRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec2;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public class WarpScroll extends ModItem {
    public WarpScroll() {
        super(ItemsRegistry.defaultItemProperties().component(DataComponentRegistry.WARP_SCROLL, new WarpScrollData(false)));
    }

    @Override
    public boolean onEntityItemUpdate(@NotNull ItemStack stack, ItemEntity entity) {
        if (entity.level().isClientSide())
            return false;

        String displayName = stack.get(DataComponents.CUSTOM_NAME) != null ? stack.getHoverName().getString() : "";
        WarpScrollData data = stack.get(DataComponentRegistry.WARP_SCROLL);
        if (data.isValid()
            && data.canTeleportWithDim(entity.level().dimension().identifier().toString())
            && SourceUtil.hasSourceNearby(entity.blockPosition(), entity.level(), 10, 9000)
            && BlockRegistry.PORTAL_BLOCK.get().trySpawnPortal(entity.level(), entity.blockPosition(), data, displayName)
            && SourceUtil.takeSourceWithParticles(entity.blockPosition(), entity.level(), 10, 9000) != null) {
            BlockPos pos = entity.blockPosition();
            ServerLevel world = (ServerLevel) entity.level();
            world.sendParticles(ParticleTypes.PORTAL, pos.getX(), pos.getY() + 1.0, pos.getZ(),
                    10, (world.getRandom().nextDouble() - 0.5D) * 2.0D, -world.getRandom().nextDouble(), (world.getRandom().nextDouble() - 0.5D) * 2.0D, 0.1f);
            world.playSound(null, pos, SoundEvents.ILLUSIONER_CAST_SPELL, SoundSource.NEUTRAL, 1.0f, 1.0f);
            ANCriteriaTriggers.rewardNearbyPlayers(ANCriteriaTriggers.CREATE_PORTAL.get(), world, pos, 4);
            stack.shrink(1);
            return true;
        }
        return false;
    }

    @Override
    public @NotNull InteractionResult use(@NotNull Level world, Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        WarpScrollData data = stack.get(DataComponentRegistry.WARP_SCROLL);
        if (hand == InteractionHand.OFF_HAND)
            return InteractionResult.SUCCESS;

        if (!(world instanceof ServerLevel serverLevel))
            return InteractionResult.SUCCESS;

        if (data.isValid()) {
            if (!data.canTeleportWithDim(player.level().dimension().identifier().toString())) {
                player.sendSystemMessage(Component.translatable("ars_nouveau.warp_scroll.wrong_dim"));
                return InteractionResult.FAIL;
            }
            BlockPos pos = data.pos().get();
            player.teleportTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
            Vec2 rotation = data.rotation();
            player.setXRot(rotation.x);
            player.setYRot(rotation.y);
            Networking.sendToNearbyClient(world, player, new PacketWarpPosition(player.getId(),pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, rotation.x, rotation.y));
            serverLevel.sendParticles(ParticleTypes.PORTAL, pos.getX(), pos.getY() + 1.0, pos.getZ(),
                    10, (world.getRandom().nextDouble() - 0.5D) * 2.0D, -world.getRandom().nextDouble(), (world.getRandom().nextDouble() - 0.5D) * 2.0D, 0.1f);
            world.playSound(null, pos, SoundEvents.ILLUSIONER_CAST_SPELL, SoundSource.NEUTRAL, 1.0f, 1.0f);
            stack.shrink(1);
            return InteractionResult.PASS;
        }
        if (player.isShiftKeyDown()) {
            ItemStack newWarpStack = new ItemStack(ItemsRegistry.WARP_SCROLL.get());
            newWarpStack.set(DataComponentRegistry.WARP_SCROLL, new WarpScrollData(Optional.of(player.blockPosition()), player.level().dimension().identifier().toString(), player.getRotationVector(), false));
            boolean didAdd;
            if (stack.getCount() == 1) {
                stack = newWarpStack;
                didAdd = true;
            } else {
                didAdd = player.addItem(newWarpStack);
                if (didAdd)
                    stack.shrink(1);
            }
            if (!didAdd) {
                player.sendSystemMessage(Component.translatable("ars_nouveau.warp_scroll.inv_full"));
                return InteractionResult.FAIL;
            } else {
                player.sendSystemMessage(Component.translatable("ars_nouveau.warp_scroll.recorded"));
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, net.minecraft.world.item.Item.TooltipContext context, net.minecraft.world.item.component.TooltipDisplay tooltipDisplay, java.util.function.Consumer<net.minecraft.network.chat.Component> tooltip2, TooltipFlag flagIn) {
        super.appendHoverText(stack, context, tooltipDisplay, tooltip2, flagIn);
        stack.addToTooltip(DataComponentRegistry.WARP_SCROLL, context, tooltipDisplay, tooltip2, flagIn);
    }
}
