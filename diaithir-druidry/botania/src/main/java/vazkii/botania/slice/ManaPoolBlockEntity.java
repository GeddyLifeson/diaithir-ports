package vazkii.botania.slice;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.energy.SimpleEnergyHandler;

public class ManaPoolBlockEntity extends BlockEntity {
    /** Botania pool size, 1,000,000 mana, held in joules. */
    public static final int CAPACITY_MANA = 1_000_000;
    private final SimpleEnergyHandler energy = new SimpleEnergyHandler(CAPACITY_MANA * BotaniaSlice.J_PER_MANA) {
        @Override
        protected void onEnergyChanged(int previous) { setChanged(); }
    };

    public ManaPoolBlockEntity(BlockPos pos, BlockState state) { super(BotaniaSlice.MANA_POOL_BE.get(), pos, state); }

    public SimpleEnergyHandler energy() { return energy; }

    @Override
    protected void saveAdditional(ValueOutput out) {
        super.saveAdditional(out);
        energy.serialize(out);
    }

    @Override
    protected void loadAdditional(ValueInput in) {
        super.loadAdditional(in);
        energy.deserialize(in);
    }
}
