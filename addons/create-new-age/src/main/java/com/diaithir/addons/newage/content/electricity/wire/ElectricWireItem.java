package com.diaithir.addons.newage.content.electricity.wire;

import net.minecraft.world.entity.EquipmentSlot;
import org.jspecify.annotations.Nullable;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import com.diaithir.addons.newage.config.CNAConfig;
import com.diaithir.addons.newage.content.electricity.connector.AbstractElectricalConnector;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.diaithir.addons.newage.CNADataComponents.BOUND_TO;

public class ElectricWireItem extends Item {
    private final WireType wireType;

    public ElectricWireItem(Properties properties, WireType wireType) {
        super(properties);
        this.wireType = wireType;
    }

    public static ElectricWireItem newCopperWire(Properties properties) {
        return new ElectricWireItem(properties, WireType.COPPER);
    }

    public static ElectricWireItem newIronWire(Properties properties) {
        return new ElectricWireItem(properties, WireType.OVERCHARGED_IRON);
    }

    public static ElectricWireItem newGoldenWire(Properties properties) {
        return new ElectricWireItem(properties, WireType.OVERCHARGED_GOLD);
    }

    public static ElectricWireItem newDiamondWire(Properties properties) {
        return new ElectricWireItem(properties, WireType.OVERCHARGED_DIAMOND);
    }

    @Override
    public InteractionResult use(@NotNull Level level, @NotNull Player player, InteractionHand usedHand) {
        ItemStack item = player.getItemInHand(usedHand);
        BlockPos boundToPos = getBoundConnector(item);

        if (boundToPos != null && player.isShiftKeyDown()) {
            playUnboundSound(player);
            player.sendOverlayMessage(Component.translatable("item.create_new_age.wire.message.unbound"));
            item.remove(BOUND_TO);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Override
    public void inventoryTick(ItemStack stack, net.minecraft.server.level.ServerLevel level, Entity entity, @Nullable EquipmentSlot slot) {
        if (slot == EquipmentSlot.MAINHAND) {
            BlockPos boundToPos = getBoundConnector(stack);
            if (boundToPos == null)
                return;
            if (!(level.getBlockEntity(boundToPos) instanceof AbstractElectricalConnector)) {
                stack.remove(BOUND_TO);
            }

            int maxLength = CNAConfig.getServer().maxWireLength.get();

            if (entity.distanceToSqr(boundToPos.getX(), boundToPos.getY(), boundToPos.getZ()) > (maxLength * maxLength * 3)) {
                stack.remove(BOUND_TO);
                playUnboundSound(entity);
                if (entity instanceof Player pl)
                    pl.sendOverlayMessage(Component.translatable("item.create_new_age.wire.message.too_far", maxLength));
            }
        }
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        BlockEntity clickedEntity = context.getLevel().getBlockEntity(context.getClickedPos());
        BlockPos boundToPos = getBoundConnector(context.getItemInHand());

        if (clickedEntity instanceof AbstractElectricalConnector clickedConnector) {
            if (boundToPos == null) {
                setBoundConnector(context.getItemInHand(), clickedConnector);
                playBoundSound(context.getPlayer());
                return InteractionResult.SUCCESS;
            } else {
                BlockPos clickedPos = clickedConnector.getBlockPos();
                int maxLength = CNAConfig.getServer().maxWireLength.get();

                if (boundToPos.equals(clickedPos)) {
                    context.getPlayer().sendOverlayMessage(Component.translatable("item.create_new_age.wire.message.self_connect"));
                    context.getItemInHand().remove(BOUND_TO);
                    return InteractionResult.FAIL;
                } else if (clickedPos.distSqr(boundToPos) > Mth.square(maxLength)) {
                    context.getPlayer().sendOverlayMessage(Component.translatable("item.create_new_age.wire.message.too_far", maxLength));
                    return InteractionResult.FAIL;
                } else if (clickedConnector.isConnected(boundToPos)) {
                    context.getPlayer().sendOverlayMessage(Component.translatable("item.create_new_age.wire.message.already_connected"));
                    context.getItemInHand().remove(BOUND_TO);
                    return InteractionResult.FAIL;
                }

                BlockEntity boundToEntity = context.getLevel().getBlockEntity(boundToPos);

                if (boundToEntity instanceof AbstractElectricalConnector boundToConnector) {
                    context.getItemInHand().remove(BOUND_TO);
                    boundToConnector.connect(clickedConnector, wireType);

                    if (!context.getPlayer().isCreative())
                        context.getItemInHand().shrink(1);

                    playBoundSound(context.getPlayer());

                    context.getPlayer().sendOverlayMessage(Component.translatable("item.create_new_age.wire.message.connected"));

                    return InteractionResult.CONSUME;
                } else
                    return InteractionResult.FAIL;
            }
        }

        return InteractionResult.PASS;
    }

    private void playBoundSound(Entity entity) {
        entity.playSound(SoundEvents.LEAD_TIED, 1.0f, 1.0f);
    }

    private void playUnboundSound(Entity entity) {
        entity.playSound(SoundEvents.LEAD_UNTIED, 1.0f, 1.0f);
    }

    public BlockPos getBoundConnector(ItemStack stack) {
        return stack.get(BOUND_TO);
    }

    public WireType getWireType() {
        return wireType;
    }

    private void setBoundConnector(ItemStack stack, AbstractElectricalConnector connector) {
        stack.set(BOUND_TO, connector.getBlockPos());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, net.minecraft.world.item.component.TooltipDisplay display, java.util.function.Consumer<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.accept(CreateLang.translate("tooltip.create_new_age.transfers").style(ChatFormatting.GRAY)
                .component());
        tooltipComponents.accept(CreateLang.text(" ").translate("tooltip.create_new_age.energy_per_tick", String.format("%,d", wireType.getConductivity())).style(ChatFormatting.AQUA).component());
    }
}
