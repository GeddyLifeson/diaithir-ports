package org.diaithir.connected.content.shearpin;

import com.zurrtum.create.content.kinetics.base.RotatedPillarKineticBlock;
import com.zurrtum.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.diaithir.connected.registries.CCBlockEntityTypes;

/** A shaft that gives way first: when its network is overstressed it breaks and drops the load. */
public class ShearPinBlock extends RotatedPillarKineticBlock implements IBE<ShearPinBlockEntity> {
    public ShearPinBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!(level.getBlockEntity(pos) instanceof ShearPinBlockEntity be) || !be.isOverStressed()) return;
        level.destroyBlock(pos, false);
        Vec3 c = pos.getCenter();
        level.sendParticles(ParticleTypes.LARGE_SMOKE, c.x, c.y, c.z, 5, 0.1, 0.1, 0.1, 0.05);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(AXIS)) {
            case X -> box(0, 5, 5, 16, 11, 11);
            case Y -> box(5, 0, 5, 11, 16, 11);
            case Z -> box(5, 5, 0, 11, 11, 16);
        };
    }

    @Override
    public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
        return face.getAxis() == state.getValue(AXIS);
    }

    @Override
    public Direction.Axis getRotationAxis(BlockState state) {
        return state.getValue(AXIS);
    }

    @Override
    public Class<ShearPinBlockEntity> getBlockEntityClass() {
        return ShearPinBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends ShearPinBlockEntity> getBlockEntityType() {
        return CCBlockEntityTypes.SHEAR_PIN.get();
    }
}
