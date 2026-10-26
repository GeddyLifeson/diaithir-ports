package com.zurrtum.create.content.kinetics.motor;

import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.content.kinetics.base.GeneratingKineticBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class TideWheelBlockEntity extends GeneratingKineticBlockEntity implements WorldlyContainer {

    public static final TagKey<Item> FUEL = TagKey.create(Registries.ITEM, Identifier.parse("diaithir_integration:magic_fuel"));
    public static final int SPEED = 32;
    public static final int TICKS_PER_ITEM = 3000;
    public static final int MAX_BURN = TICKS_PER_ITEM * 8;

    private int burn;
    private final NonNullList<ItemStack> slot = NonNullList.withSize(1, ItemStack.EMPTY);

    public TideWheelBlockEntity(BlockPos pos, BlockState state) {
        super(AllBlockEntityTypes.TIDE_WHEEL, pos, state);
    }

    public static boolean isFuel(ItemStack stack) {
        return stack.is(FUEL);
    }

    public boolean feed(Player player, ItemStack stack) {
        if (burn + TICKS_PER_ITEM > MAX_BURN) {
            return false;
        }
        burnOne();
        if (!player.isCreative()) {
            stack.shrink(1);
        }
        return true;
    }

    private void burnOne() {
        boolean wasIdle = burn <= 0;
        burn += TICKS_PER_ITEM;
        if (wasIdle) {
            updateGeneratedRotation();
            setLit(true);
        }
        setChanged();
        sendData();
    }

    private void setLit(boolean lit) {
        BlockState state = getBlockState();
        if (level != null && state.is(AllBlocks.TIDE_WHEEL) && state.getValue(TideWheelBlock.LIT) != lit) {
            level.setBlock(worldPosition, state.setValue(TideWheelBlock.LIT, lit), 3);
        }
    }

    @Override
    public void initialize() {
        super.initialize();
        if (burn > 0 && (!hasSource() || getGeneratedSpeed() > getTheoreticalSpeed())) {
            updateGeneratedRotation();
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (level == null || level.isClientSide()) {
            return;
        }
        // fuel put in by a hopper, funnel or pipe waits in the slot until there is room
        ItemStack in = slot.get(0);
        if (!in.isEmpty() && burn + TICKS_PER_ITEM <= MAX_BURN) {
            in.shrink(1);
            burnOne();
        }
        if (burn <= 0) {
            return;
        }
        setLit(true);
        if (speed == 0 && !hasSource()) {
            updateGeneratedRotation(); // fuel arrived with the saved data, after the first initialize
        }
        if (--burn == 0) {
            updateGeneratedRotation();
            setLit(false);
            sendData();
        }
    }

    @Override
    public float getGeneratedSpeed() {
        if (!getBlockState().is(AllBlocks.TIDE_WHEEL) || burn <= 0) {
            return 0;
        }
        return convertToDirection(SPEED, getBlockState().getValue(TideWheelBlock.FACING));
    }

    @Override
    protected void write(ValueOutput view, boolean clientPacket) {
        view.putInt("Burn", burn);
        ContainerHelper.saveAllItems(view, slot);
        super.write(view, clientPacket);
    }

    @Override
    protected void read(ValueInput view, boolean clientPacket) {
        burn = view.getIntOr("Burn", 0);
        slot.clear();
        ContainerHelper.loadAllItems(view, slot);
        super.read(view, clientPacket);
    }

    @Override public int getContainerSize() { return 1; }
    @Override public boolean isEmpty() { return slot.get(0).isEmpty(); }
    @Override public ItemStack getItem(int i) { return slot.get(i); }
    @Override public ItemStack removeItem(int i, int n) { ItemStack r = slot.get(i).split(n); if (!r.isEmpty()) setChanged(); return r; }
    @Override public ItemStack removeItemNoUpdate(int i) { ItemStack r = slot.get(i); slot.set(i, ItemStack.EMPTY); return r; }
    @Override public void setItem(int i, ItemStack s) { slot.set(i, s); setChanged(); }
    @Override public boolean stillValid(Player p) { return Container.stillValidBlockEntity(this, p); }
    @Override public void clearContent() { slot.clear(); }
    @Override public int[] getSlotsForFace(Direction d) { return new int[] {0}; }
    @Override public boolean canPlaceItem(int i, ItemStack s) { return isFuel(s); }
    @Override public boolean canPlaceItemThroughFace(int i, ItemStack s, Direction d) { return isFuel(s); }
    @Override public boolean canTakeItemThroughFace(int i, ItemStack s, Direction d) { return false; }

}
