package net.diaithir.travel.laser;

import net.diaithir.travel.laser.blocks.BridgeSourceBlock;
import net.diaithir.travel.laser.blocks.FenceSourceBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.dispenser.OptionalDispenseItemBehavior;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;

import static net.diaithir.travel.laser.blocks.BridgeSourceBlock.COLOR;

public class CommonClass {
    public static void init() {
        DefaultDispenseItemBehavior dyeBehavior = new OptionalDispenseItemBehavior() {
            protected ItemStack execute(BlockSource blockSource, ItemStack stack) {
                ServerLevel serverLevel = blockSource.level();
                Direction direction = (Direction)blockSource.state().getValue(DispenserBlock.FACING);
                BlockPos blockPos = blockSource.pos().relative(direction);
                BlockState blockState = serverLevel.getBlockState(blockPos);
                Block block = blockState.getBlock();
                if (block instanceof BridgeSourceBlock || block instanceof FenceSourceBlock) {
                    int col = stack.get(DataComponents.DYE).getId();
                    BlockState newState = blockState.setValue(COLOR, col);
                    serverLevel.setBlock(blockPos, newState, 3);
                    // recolour the existing beam straight away, as hand-dyeing does
                    int power = serverLevel.getBestNeighborSignal(blockPos);
                    if (block instanceof BridgeSourceBlock bridge) bridge.generateBridge(serverLevel, power, blockPos, newState);
                    else ((FenceSourceBlock) block).generateBridge(serverLevel, power, blockPos, newState);
                    stack.shrink(1);
                    serverLevel.gameEvent((Entity)null, GameEvent.BLOCK_CHANGE, blockPos);
                    this.setSuccess(true);
                    return stack;
                }
                return super.execute(blockSource, stack);
            }
        };

        DispenserBlock.registerBehavior(Items.WHITE_DYE, dyeBehavior);
        DispenserBlock.registerBehavior(Items.ORANGE_DYE, dyeBehavior);
        DispenserBlock.registerBehavior(Items.MAGENTA_DYE, dyeBehavior);
        DispenserBlock.registerBehavior(Items.LIGHT_BLUE_DYE, dyeBehavior);
        DispenserBlock.registerBehavior(Items.YELLOW_DYE, dyeBehavior);
        DispenserBlock.registerBehavior(Items.LIME_DYE, dyeBehavior);
        DispenserBlock.registerBehavior(Items.PINK_DYE, dyeBehavior);
        DispenserBlock.registerBehavior(Items.GRAY_DYE, dyeBehavior);
        DispenserBlock.registerBehavior(Items.LIGHT_GRAY_DYE, dyeBehavior);
        DispenserBlock.registerBehavior(Items.CYAN_DYE, dyeBehavior);
        DispenserBlock.registerBehavior(Items.PURPLE_DYE, dyeBehavior);
        DispenserBlock.registerBehavior(Items.BLUE_DYE, dyeBehavior);
        DispenserBlock.registerBehavior(Items.BROWN_DYE, dyeBehavior);
        DispenserBlock.registerBehavior(Items.GREEN_DYE, dyeBehavior);
        DispenserBlock.registerBehavior(Items.RED_DYE, dyeBehavior);
        DispenserBlock.registerBehavior(Items.BLACK_DYE, dyeBehavior);
    }
}
