package net.diaithir.materials;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/** A building-material block whose fire odds come from its table row. */
public final class MaterialBlock extends Block {
    private final int fireSpread;
    private final int flammability;

    public MaterialBlock(Properties properties, Materials.Row row) {
        super(row.flammability() > 0 ? properties.ignitedByLava() : properties);
        this.fireSpread = row.fireSpread();
        this.flammability = row.flammability();
    }

    @Override
    public int getFlammability(BlockState state, BlockGetter level, BlockPos pos, Direction direction) { return flammability; }

    @Override
    public int getFireSpreadSpeed(BlockState state, BlockGetter level, BlockPos pos, Direction direction) { return fireSpread; }
}
