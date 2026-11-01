package org.diaithir.connected.content.shearpin;

import com.zurrtum.create.content.kinetics.base.IRotate;
import com.zurrtum.create.content.kinetics.base.KineticBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.ticks.TickPriority;
import org.diaithir.connected.registries.CCBlocks;

public class ShearPinBlockEntity extends KineticBlockEntity {
    private static final int RANDOM_DELAY = 5;

    public ShearPinBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void initialize() {
        check();
        super.initialize();
    }

    private void check() {
        if (level != null && IRotate.StressImpact.isEnabled() && isOverStressed())
            level.scheduleTick(getBlockPos(), CCBlocks.SHEAR_PIN.get(), level.getRandom().nextInt(RANDOM_DELAY), TickPriority.EXTREMELY_HIGH);
    }

    @Override
    public void updateFromNetwork(float maxStress, float currentStress, int networkSize) {
        super.updateFromNetwork(maxStress, currentStress, networkSize);
        check();
    }
}
