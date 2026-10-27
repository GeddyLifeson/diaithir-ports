package net.diaithir.butchery;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/** Holds a stack to process and the finished stack. One item is processed at a time; the product keeps the species and kill time of its input. */
public class StationEntity extends BlockEntity {
    /** Shelf life percent this station gives anything it holds (the cold room and stasis case override this); 0 for stations that are not cold containers. */
    public int containerShelf() {
        return 0;
    }

    private String station;
    private ItemStack input = ItemStack.EMPTY;
    private ItemStack output = ItemStack.EMPTY;
    private int progress;

    public StationEntity(BlockPos pos, BlockState state) {
        this(pos, state, "");
    }

    StationEntity(BlockPos pos, BlockState state, String station) {
        this(Butchery.STATION_ENTITY.get(), pos, state, station);
    }

    StationEntity(net.minecraft.world.level.block.entity.BlockEntityType<?> type, BlockPos pos, BlockState state, String station) {
        super(type, pos, state);
        this.station = station;
    }

    boolean busy() {
        return !input.isEmpty();
    }

    boolean accept(ItemStack held) {
        ButcheryData.Process p = ButcheryData.recipeFor(station, held);
        if (p == null || !input.isEmpty()) return false;
        input = held.copyWithCount(1);
        held.shrink(1);
        progress = 0;
        setChanged();
        return true;
    }

    ItemStack takeOutput() {
        ItemStack out = output;
        output = ItemStack.EMPTY;
        if (!out.isEmpty()) setChanged();
        return out;
    }

    void tick(Level level) {
        if (input.isEmpty()) return;
        ButcheryData.Process p = ButcheryData.recipeFor(station, input);
        if (p == null) {
            output = input;
            input = ItemStack.EMPTY;
            return;
        }
        if (++progress < p.ticks()) return;
        ItemStack made = p.make(input);
        if (output.isEmpty() || ItemStack.isSameItemSameComponents(output, made) && output.getCount() + made.getCount() <= output.getMaxStackSize()) {
            if (output.isEmpty()) output = made; else output.grow(made.getCount());
            input = ItemStack.EMPTY;
            progress = 0;
            setChanged();
        }
    }

    @Override
    protected void saveAdditional(ValueOutput o) {
        super.saveAdditional(o);
        o.putString("station", station);
        o.store("input", ItemStack.OPTIONAL_CODEC, input);
        o.store("output", ItemStack.OPTIONAL_CODEC, output);
        o.putInt("progress", progress);
    }

    @Override
    protected void loadAdditional(ValueInput i) {
        super.loadAdditional(i);
        station = i.getStringOr("station", station);
        input = i.read("input", ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY);
        output = i.read("output", ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY);
        progress = i.getIntOr("progress", 0);
    }
}
