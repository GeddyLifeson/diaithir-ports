package com.zurrtum.create;

import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.content.kinetics.belt.BeltBlock;
import com.zurrtum.create.content.kinetics.crafter.MechanicalCrafterBlockEntity;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.CachedDirectionInventoryBehaviour;
import com.zurrtum.create.foundation.blockEntity.behaviour.CachedFluidInventoryBehaviour;
import com.zurrtum.create.foundation.blockEntity.behaviour.CachedInventoryBehaviour;
import com.zurrtum.create.foundation.item.ItemHelper;
import com.zurrtum.create.infrastructure.fluids.FluidInventory;
import com.zurrtum.create.infrastructure.fluids.FluidItemInventory;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Supplier;

public class AllTransfer {
    @Nullable
    public static Supplier<@Nullable Container> getCacheInventory(
        ServerLevel world,
        BlockPos pos,
        Direction direction,
        @Nullable BiPredicate<@Nullable BlockEntity, Direction> filter
    ) {
        return () -> {
            BlockEntity be = world.getBlockEntity(pos);
            if (be == null || (filter != null && !filter.test(be, direction))) {
                return null;
            }
            return getInventory(world, pos, be.getBlockState(), be, direction);
        };
    }

    @Nullable
    public static Container getInventory(
        Level world,
        BlockPos pos,
        @Nullable BlockState state,
        @Nullable BlockEntity blockEntity,
        @Nullable Direction direction
    ) {
        if (blockEntity == null) {
            blockEntity = world.getBlockEntity(pos);
        }
        if (blockEntity instanceof SmartBlockEntity be) {
            Container c = CachedInventoryBehaviour.get(be, direction);
            if (c != null) {
                return c;
            }
            return CachedDirectionInventoryBehaviour.get(be, direction);
        }
        return null;
    }

    public static boolean hasFluidInventory(
        Level world,
        BlockPos pos,
        @Nullable BlockState state,
        @Nullable BlockEntity blockEntity,
        @Nullable Direction direction
    ) {
        return getFluidInventory(world, pos, state, blockEntity, direction) != null;
    }

    @Nullable
    public static Supplier<@Nullable FluidInventory> getCacheFluidInventory(
        ServerLevel world,
        BlockPos pos,
        Direction direction
    ) {
        return () -> getFluidInventory(world, pos, null, null, direction);
    }

    @Nullable
    public static FluidInventory getFluidInventory(
        Level world,
        BlockPos pos,
        @Nullable BlockState state,
        @Nullable BlockEntity blockEntity,
        @Nullable Direction direction
    ) {
        if (blockEntity == null) {
            blockEntity = world.getBlockEntity(pos);
        }
        if (blockEntity instanceof SmartBlockEntity be) {
            return CachedFluidInventoryBehaviour.get(be, direction);
        }
        return null;
    }

    public static boolean hasFluidInventory(ItemStack stack) {
        return false;
    }

    @Nullable
    public static FluidItemInventory getFluidInventory(ItemStack stack) {
        return null;
    }

    private static <T extends SmartBlockEntity> void registerItemSide(
        BlockEntityType<T> type,
        Function<T, @Nullable Container> factory
    ) {
        BlockEntityBehaviour.add(type, (T be) -> new CachedInventoryBehaviour<>(be, factory));
    }

    private static <T extends SmartBlockEntity> void registerItemSide(
        BlockEntityType<T> type,
        BiFunction<T, @Nullable Direction, @Nullable Container> factory
    ) {
        BlockEntityBehaviour.add(type, (T be) -> new CachedDirectionInventoryBehaviour<>(be, factory));
    }

    private static <T extends SmartBlockEntity> void registerFluidSide(
        BlockEntityType<T> type,
        Function<T, @Nullable FluidInventory> factory
    ) {
        BlockEntityBehaviour.add(type, (T be) -> new CachedFluidInventoryBehaviour<>(be, factory));
    }

    public static void register() {
        registerItemSide(AllBlockEntityTypes.DEPOT, be -> be.depotBehaviour.itemHandler);
        registerItemSide(AllBlockEntityTypes.WEIGHTED_EJECTOR, be -> be.depotBehaviour.itemHandler);
        registerItemSide(
            AllBlockEntityTypes.BELT, be -> {
                if (!BeltBlock.canTransportObjects(be.getBlockState())) {
                    return null;
                }
                if (!be.isRemoved() && be.itemHandler == null) {
                    be.initializeItemHandler();
                }
                return be.itemHandler;
            }
        );
        registerItemSide(AllBlockEntityTypes.MILLSTONE, be -> be.capability);
        registerItemSide(AllBlockEntityTypes.SAW, be -> be.inventory);
        registerItemSide(AllBlockEntityTypes.BASIN, be -> be.itemCapability);
        registerItemSide(
            AllBlockEntityTypes.ANDESITE_TUNNEL, be -> {
                if (be.cap == null) {
                    Level world = be.getLevel();
                    BlockPos pos = be.getBlockPos();
                    BlockState state = world.getBlockState(pos.below());
                    if (state.is(AllBlocks.BELT)) {
                        BlockEntity beBelow = world.getBlockEntity(pos.below());
                        if (beBelow != null) {
                            Container capBelow = ItemHelper.getInventory(
                                world,
                                pos.below(),
                                state,
                                beBelow,
                                Direction.UP
                            );
                            if (capBelow != null) {
                                be.cap = capBelow;
                            }
                        }
                    }
                }
                return be.cap;
            }
        );
        registerItemSide(AllBlockEntityTypes.BRASS_TUNNEL, be -> be.tunnelCapability);
        registerItemSide(AllBlockEntityTypes.CHUTE, be -> be.itemHandler);
        registerItemSide(AllBlockEntityTypes.SMART_CHUTE, be -> be.itemHandler);
        registerItemSide(AllBlockEntityTypes.PORTABLE_STORAGE_INTERFACE, be -> be.capability);
        registerItemSide(
            AllBlockEntityTypes.ITEM_DRAIN, (be, context) -> {
                if (context != null && context.getAxis().isHorizontal()) {
                    return be.itemHandlers.get(context);
                }
                return null;
            }
        );
        registerItemSide(
            AllBlockEntityTypes.DEPLOYER, be -> {
                if (be.invHandler == null) {
                    be.initHandler();
                }
                return be.invHandler;
            }
        );
        registerItemSide(AllBlockEntityTypes.CRUSHING_WHEEL_CONTROLLER, be -> be.inventory);
        registerItemSide(AllBlockEntityTypes.MECHANICAL_CRAFTER, MechanicalCrafterBlockEntity::getInvCapability);
        registerItemSide(AllBlockEntityTypes.CREATIVE_CRATE, be -> be.inv);
        registerItemSide(AllBlockEntityTypes.PACKAGER, be -> be.inventory);
        registerItemSide(AllBlockEntityTypes.REPACKAGER, be -> be.inventory);
        registerItemSide(AllBlockEntityTypes.PACKAGE_POSTBOX, be -> be.inventory);
        registerItemSide(AllBlockEntityTypes.PACKAGE_FROGPORT, be -> be.inventory);
        registerItemSide(AllBlockEntityTypes.TOOLBOX, be -> be.inventory);
        registerItemSide(AllBlockEntityTypes.TRACK_STATION, be -> be.depotBehaviour.itemHandler);
        registerFluidSide(
            AllBlockEntityTypes.FLUID_TANK, be -> {
                if (be.fluidCapability == null) {
                    be.refreshCapability();
                }
                return be.fluidCapability;
            }
        );
        registerFluidSide(AllBlockEntityTypes.BASIN, be -> be.fluidCapability);
        registerFluidSide(AllBlockEntityTypes.PORTABLE_FLUID_INTERFACE, be -> be.capability);
        registerFluidSide(AllBlockEntityTypes.HOSE_PULLEY, be -> be.handler);
        registerFluidSide(AllBlockEntityTypes.SPOUT, be -> be.tank.getCapability());
    }
}
