package com.zurrtum.create.content.kinetics.motor;

import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.content.kinetics.base.GeneratingKineticBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class TideWheelBlockEntity extends GeneratingKineticBlockEntity {

    public static final TagKey<Item> FUEL = TagKey.create(Registries.ITEM, Identifier.parse("diaithir_integration:magic_fuel"));
    public static final int SPEED = 32;
    public static final int TICKS_PER_ITEM = 3000;
    public static final int MAX_BURN = TICKS_PER_ITEM * 8;

    private int burn;

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
        boolean wasIdle = burn <= 0;
        burn += TICKS_PER_ITEM;
        if (!player.isCreative()) {
            stack.shrink(1);
        }
        if (wasIdle) {
            updateGeneratedRotation();
        }
        setChanged();
        sendData();
        return true;
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
        if (level == null || level.isClientSide() || burn <= 0) {
            return;
        }
        if (speed == 0 && !hasSource()) {
            updateGeneratedRotation(); // fuel arrived with the saved data, after the first initialize
        }
        if (--burn == 0) {
            updateGeneratedRotation();
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
        super.write(view, clientPacket);
    }

    @Override
    protected void read(ValueInput view, boolean clientPacket) {
        burn = view.getIntOr("Burn", 0);
        super.read(view, clientPacket);
    }

}
