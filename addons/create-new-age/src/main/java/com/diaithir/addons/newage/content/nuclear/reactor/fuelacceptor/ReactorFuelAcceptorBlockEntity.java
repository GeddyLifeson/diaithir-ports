package com.diaithir.addons.newage.content.nuclear.reactor.fuelacceptor;

import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Containers;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RodBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import com.diaithir.addons.newage.CreateNewAge;
import com.diaithir.addons.newage.CNATags;
import com.diaithir.addons.newage.content.nuclear.reactor.RodFindingReactorBlockEntity;
import com.diaithir.addons.newage.content.nuclear.reactor.rod.ReactorRodBlockEntity;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class ReactorFuelAcceptorBlockEntity extends RodFindingReactorBlockEntity {
    public SimpleContainer container;

    public ReactorFuelAcceptorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
        container = new FuelAcceptorContainer(1);
    }

    @Override
    protected void write(ValueOutput compound, boolean clientPacket) {
        ValueOutput.TypedOutputList<ItemStack> list = compound.list("contents", ItemStack.OPTIONAL_CODEC);
        for (int i = 0; i < container.getContainerSize(); i++)
            list.add(container.getItem(i));
        super.write(compound, clientPacket);
    }

    @Override
    protected void read(ValueInput compound, boolean clientPacket) {
        int slot = 0;
        for (ItemStack stack : compound.listOrEmpty("contents", ItemStack.OPTIONAL_CODEC)) {
            if (slot < container.getContainerSize())
                container.setItem(slot, stack);
            slot++;
        }
        super.read(compound, clientPacket);
    }

    @Override
    public void destroy() {
        super.destroy();
        for (int i = 0 ; i < container.getContainerSize() ; i++) {
            Containers.dropItemStack(level, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), container.getItem(i));
        }
    }

    int ticks = 0;

    public void tick(BlockPos pos, Level world, BlockState state) {
        ticks--;
        if (ticks <= 0) {
            ticks = 20;
            List<ReactorRodBlockEntity> rods = new LinkedList<>();
            for (Direction dir : Direction.values()) {
                findRods(rods, dir);
            }
            if (rods.isEmpty()) {
                return;
            }
            AtomicInteger have = new AtomicInteger();
            int hadBefore = 0;
            for (ReactorRodBlockEntity rod : rods) {
                hadBefore += rod.fuel;
            }

            AtomicInteger totalNeeded = new AtomicInteger(ReactorRodBlockEntity.MAX_FUEL * rods.size() - hadBefore);
            for (int i = 0 ; i < container.getContainerSize() ; i++) {
                ItemStack stack = container.getItem(i);
                if (stack.is(CNATags.Item.NUCLEAR_FUEL.tag)) {
                    stack.typeHolder().tags().forEach(itemTagKey -> {
                        if (itemTagKey.location().getNamespace().equals(CreateNewAge.MOD_ID)) {
                            String path = itemTagKey.location().getPath();
                            if (path.startsWith("nuclear/energy_")) {
                                try {
                                    int energy = Integer.parseInt(path.substring(15)); //  assert "nuclear/energy_".length() == 15;
                                    int total = (int) Math.min(stack.getCount(), (double) (totalNeeded.get() / energy));
                                    totalNeeded.addAndGet(-energy*total);
                                    have.addAndGet(energy*total);
                                    stack.shrink(total);
                                } catch (NumberFormatException e) {
                                    CreateNewAge.LOGGER.error("BAD TAG " + itemTagKey + " on item " + stack.getDisplayName().getString(), e);
                                }
                            }
                        }
                    });
                }
            }
            setChanged();
            int target = (hadBefore + have.get()) / rods.size(); // doesn't matter even if we loose a bit or gain a few.
            for (ReactorRodBlockEntity rod : rods) {
                rod.fuel = target;
            }
        }
    }

    private class FuelAcceptorContainer extends SimpleContainer {

        FuelAcceptorContainer(int size) {
            super(size);
        }

        @Override
        public boolean canPlaceItem(int index, ItemStack stack) {
            return stack.is(CNATags.Item.NUCLEAR_FUEL.tag);
        }
    }
}
