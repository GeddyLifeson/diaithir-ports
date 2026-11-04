package net.diaithir.druidry.ars.common.items;

import net.diaithir.druidry.ars.api.entity.IDecoratable;
import net.diaithir.druidry.ars.api.item.IRadialProvider;
import net.diaithir.druidry.ars.api.item.IWandable;
import net.diaithir.druidry.ars.common.items.data.DominionWandData;
import net.diaithir.druidry.ars.common.util.PortUtil;
import net.diaithir.druidry.ars.setup.registry.CapabilityRegistry;
import net.diaithir.druidry.ars.setup.registry.DataComponentRegistry;
import net.diaithir.druidry.ars.setup.registry.ItemsRegistry;
import net.diaithir.druidry.ars.setup.registry.SoundRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class DominionWand extends ModItem  {
    public DominionWand() {
        super(ItemsRegistry.defaultItemProperties().stacksTo(1).component(DataComponentRegistry.DOMINION_WAND, new DominionWandData()));
    }

    @Override
    public @NotNull InteractionResult interactLivingEntity(@NotNull ItemStack doNotUseStack, Player playerEntity, @NotNull LivingEntity target, @NotNull InteractionHand hand) {

        if (playerEntity.level().isClientSide() || hand != InteractionHand.MAIN_HAND)
            return InteractionResult.PASS;

        ItemStack stack = playerEntity.getItemInHand(hand);
        DominionWandData data = stack.getOrDefault(DataComponentRegistry.DOMINION_WAND, new DominionWandData());
        Level level = playerEntity.level();
        if (playerEntity.isShiftKeyDown() && target instanceof IWandable wandable) {
            var result = wandable.onClearConnections(playerEntity);
            playSoundFromResult(level, target.getOnPos(), result);
            clear(stack, playerEntity);
            return InteractionResult.SUCCESS;
        }
        // If the wand has nothing, store it
        if (!data.hasStoredData()) {
            stack.set(DataComponentRegistry.DOMINION_WAND, data.storeEntity(target.getId()));
            PortUtil.sendMessage(playerEntity, Component.translatable("ars_nouveau.dominion_wand.stored_entity"));
            return InteractionResult.SUCCESS;
        }
        Level world = data.storedPos().isPresent() ? playerEntity.level().getServer().getLevel(data.storedPos().get().dimension()) : playerEntity.level();

        if (data.storedPos().isPresent() && world.getBlockEntity(data.storedPos().get().pos()) instanceof IWandable wandable) {
            var result = wandable.onFirstConnection(data.storedPos().orElse(null), data.face().orElse(null), target, playerEntity);
            playSoundFromResult(world, target.getOnPos(), result);
        }

        if (target instanceof IWandable wandable) {
            var result = wandable.onLastConnection(data.storedPos().orElse(null), data.face().orElse(null), target, playerEntity);
            playSoundFromResult(world, target.getOnPos(), result);
            clear(stack, playerEntity);
        }

        if (playerEntity.isShiftKeyDown() && target instanceof IDecoratable coolBoy) {
            coolBoy.setCosmeticItem(ItemStack.EMPTY);
        }

        return InteractionResult.SUCCESS;
    }

    public boolean doesSneakBypassUse(@NotNull ItemStack stack, @NotNull LevelReader world, @NotNull BlockPos pos, @NotNull Player player) {
        return false;
    }

    public void clear(ItemStack stack, Player player) {
        DominionWandData data = stack.getOrDefault(DataComponentRegistry.DOMINION_WAND, new DominionWandData())
                .setFace(null)
                .storeEntity(DominionWandData.NULL_ENTITY)
                .storePos(null);
        stack.set(DataComponentRegistry.DOMINION_WAND, data);
    }

    @Override
    public @NotNull InteractionResult useOn(UseOnContext context) {
        if (context.getLevel().isClientSide() || context.getPlayer() == null)
            return super.useOn(context);
        BlockPos pos = context.getClickedPos();
        Direction face = context.getClickedFace();
        Player playerEntity = context.getPlayer();
        ItemStack stack = context.getItemInHand();
        DominionWandData data = stack.getOrDefault(DataComponentRegistry.DOMINION_WAND, new DominionWandData());
        Level world = data.storedPos().isPresent() ? playerEntity.level().getServer().getLevel(data.storedPos().get().dimension()) : context.getLevel();

        if (playerEntity.isShiftKeyDown() && world.getBlockEntity(pos) instanceof IWandable wandable && !data.hasStoredData()) {
            var result = wandable.onClearConnections(playerEntity);
            playSoundFromResult(world, pos, result);
            clear(stack, playerEntity);
            world.playSound(null, pos, SoundRegistry.DOMINION_WAND_CLEAR.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
            return InteractionResult.CONSUME;
        }

        if (!data.hasStoredData()) {
            data = data.storePos(new GlobalPos(context.getLevel().dimension(), pos));
            if (data.strict()) data = data.setFace(face);
            stack.set(DataComponentRegistry.DOMINION_WAND, data);
            PortUtil.sendMessage(playerEntity, Component.translatable("ars_nouveau.dominion_wand.position_set"));
            playSoundFromResult(playerEntity.level(), pos, IWandable.Result.SELECT);
            return InteractionResult.SUCCESS;
        }

        GlobalPos storedPos = data.storedPos().orElse(null);
        Direction storedDirection = data.face().orElse(null);

        // TODO: Make interface accept Entity
        Entity storedEntity = world.getEntity(data.storedEntityId());
        if (storedPos != null) {
            IWandable wandableStoredBlock = getWandableBlock(context, storedPos.pos(), storedDirection);

            if (wandableStoredBlock != null) {
                var result = wandableStoredBlock.onFirstConnection(new GlobalPos(world.dimension(), pos), storedDirection, storedEntity instanceof LivingEntity livingEntity ? livingEntity : null, playerEntity);
                playSoundFromResult(world, pos, result);
            }
        }

        IWandable wandableAtClickedPos = getWandableBlock(context, pos, face);

        if (wandableAtClickedPos != null) {
            var result = wandableAtClickedPos.onLastConnection(storedPos, storedDirection, storedEntity instanceof LivingEntity livingEntity ? livingEntity : null, playerEntity);
            playSoundFromResult(world, pos, result);
        }


        if (data.storedEntityId() != DominionWandData.NULL_ENTITY && world.getEntity(data.storedEntityId()) instanceof IWandable wandable) {
            var result = wandable.onFirstConnection(new GlobalPos(world.dimension(), pos), data.strict() ? face : null, null, playerEntity);
            playSoundFromResult(world, pos, result);
        }

        clear(stack, playerEntity);
        return super.useOn(context);
    }

    public @Nullable IWandable getWandableBlock(UseOnContext context, BlockPos atPos, Direction direction) {
        Player playerEntity = context.getPlayer();
        ItemStack stack = context.getItemInHand();
        DominionWandData data = stack.getOrDefault(DataComponentRegistry.DOMINION_WAND, new DominionWandData());
        Level world = data.storedPos().isPresent() ? playerEntity.level().getServer().getLevel(data.storedPos().get().dimension()) : context.getLevel();

        if (world.getBlockEntity(atPos) instanceof IWandable wandable) {
            return wandable;
        }

        if (world.getCapability(CapabilityRegistry.WANDABLE_BLOCK_CAPABILITY, atPos, direction) instanceof IWandable wandable) {
            return wandable;
        }
        return null;
    }

    @Override
    public @NotNull Component getName(@NotNull ItemStack pStack) {
        DominionWandData data = pStack.getOrDefault(DataComponentRegistry.DOMINION_WAND, new DominionWandData());
        if (data.strict()) return Component.translatable(this.getDescriptionId() + ".strict");
        return super.getName(pStack);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext world, net.minecraft.world.item.component.TooltipDisplay display, java.util.function.Consumer<Component> tooltip, @NotNull TooltipFlag p_77624_4_) {
        DominionWandData data = stack.getOrDefault(DataComponentRegistry.DOMINION_WAND, new DominionWandData());
        if (data.storedEntityId() == DominionWandData.NULL_ENTITY) {
            tooltip.accept(Component.translatable("ars_nouveau.dominion_wand.no_entity"));
        } else {
            tooltip.accept(Component.translatable("ars_nouveau.dominion_wand.entity_stored"));
        }

        if (data.storedPos().isEmpty()) {
            tooltip.accept(Component.translatable("ars_nouveau.dominion_wand.no_location"));
        } else {
            tooltip.accept(Component.translatable("ars_nouveau.dominion_wand.position_stored", getGlobalPosString(data.getValidPos())));
        }

        if (data.strict()) tooltip.accept(Component.literal("Side-Sensitive"));
    }

    public static String getPosString(BlockPos pos) {
        return Component.translatable("ars_nouveau.position", pos.getX(), pos.getY(), pos.getZ()).getString();
    }

    public static String getGlobalPosString(GlobalPos globalPos) {
        BlockPos pos = globalPos.pos();
        Identifier dimLoc = globalPos.dimension().identifier();
        String translationKey = dimLoc.getPath() + "." + dimLoc.getNamespace() + ".name";
        String translation = Component.translatable(translationKey).getString();
        return Component.translatable("ars_nouveau.global_position", pos.getX(), pos.getY(), pos.getZ(), translation.equals(translationKey) ? dimLoc.toString() : translation).getString();
    }

    public enum DominionSlots {
        CLEAR("ars_nouveau.dominion_wand.clear"),
        NORMAL("ars_nouveau.dominion_wand.normal"),
        STRICT("ars_nouveau.dominion_wand.strict");

        public final String key;

        DominionSlots(String key) {
            this.key = key;
        }

        public Component translatable() {
            return Component.translatable(key);
        }
    }

    public void playSoundFromResult(Level level, BlockPos at, IWandable.Result result) {
        switch (result) {
            case SUCCESS ->
                    level.playSound(null, at, SoundRegistry.DOMINION_WAND_SUCCESS.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
            case FAIL ->
                    level.playSound(null, at, SoundRegistry.DOMINION_WAND_FAIL.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
            case SELECT ->
                    level.playSound(null, at, SoundRegistry.DOMINION_WAND_SELECT.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
            case CLEAR ->
                    level.playSound(null, at, SoundRegistry.DOMINION_WAND_CLEAR.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
            default -> {
            }
        }
    }

}
