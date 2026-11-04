package net.diaithir.gantries;

import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.content.kinetics.gantry.GantryShaftBlock;
import com.zurrtum.create.content.kinetics.gantry.GantryShaftBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * A gantry shaft that does not join the shaft next to it visually and does not pull its neighbours along when a carriage moves, so a long gantry
 * can be cut into separate runs. For everything else (carriages, power, pinion speed) it counts as a gantry shaft; the mixins make Create agree.
 * Behaviour after Create: Extended Gantries by MR-Spagetty (GPL-3.0); this class is a fresh implementation on the Create port.
 */
public class GantrySplitBlock extends GantryShaftBlock {
    public GantrySplitBlock(Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntityType<? extends GantryShaftBlockEntity> getBlockEntityType() {
        return AllBlockEntityTypes.GANTRY_SHAFT;
    }

    @Override
    public BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos, Direction dir, BlockPos neighbourPos,
            BlockState neighbour, RandomSource random) {
        BlockState s = super.updateShape(state, level, ticks, pos, dir, neighbourPos, neighbour, random);
        // a split never shows itself as joined to another split
        if (s.is(this) && neighbour.is(this) && dir.getAxis() == s.getValue(FACING).getAxis() && s.getValue(PART) != Part.SINGLE) {
            Direction facing = s.getValue(FACING);
            boolean positiveSide = dir.getAxisDirection() == facing.getAxisDirection();
            Part part = s.getValue(PART);
            if (positiveSide) part = part == Part.MIDDLE ? Part.END : part == Part.START ? Part.SINGLE : part;
            else part = part == Part.MIDDLE ? Part.START : part == Part.END ? Part.SINGLE : part;
            return s.setValue(PART, part);
        }
        return s;
    }
}
