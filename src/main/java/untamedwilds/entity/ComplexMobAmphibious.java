package untamedwilds.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.navigation.WaterBoundPathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.AmphibiousNodeEvaluator;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.pathfinder.PathFinder;
import untamedwilds.entity.ai.control.look.SmartSwimmerLookControl;
import untamedwilds.entity.ai.control.movement.SmartSwimmingMoveControl;

public abstract class ComplexMobAmphibious extends ComplexMobTerrestrial {

    protected boolean isAmphibious;

    public ComplexMobAmphibious(EntityType<? extends ComplexMob> type, Level worldIn) {
        super(type, worldIn);
        this.moveControl = new SmartSwimmingMoveControl(this, 40, 5, 0.25F, 0.3F, true);
        this.lookControl = new SmartSwimmerLookControl(this, 20);
    this.setPathfindingMalus(PathType.WATER, 0.0F);
    }

    protected PathNavigation createNavigation(Level worldIn) {
        return new ComplexMobAmphibious.AmphibiousPathNavigation(this, worldIn);
    }

    public boolean wantsToBeOnLand() { return true; }

    public boolean wantsToBeInWater() { return false; }

    public float getWaterSlowDown() {
    return this.onGround() ? 0.8F : 0.9F;
    }

    // canBreatheUnderwater is final in 1.21; prefer using mob attributes or MobType where applicable

    protected SoundEvent getSwimSound() {
        return SoundEvents.AXOLOTL_SWIM;
    }

    static class AmphibiousPathNavigation extends WaterBoundPathNavigation {
        private final ComplexMobAmphibious entityIn;

        AmphibiousPathNavigation(ComplexMobAmphibious entityIn, Level worldIn) {
            super(entityIn, worldIn);
            this.entityIn = entityIn;
        }

        protected boolean canUpdatePath() {
            return true;
        }

        protected PathFinder createPathFinder(int p_149222_) {
            this.nodeEvaluator = new AmphibiousNodeEvaluator(false);
            return new PathFinder(this.nodeEvaluator, p_149222_);
        }

        public boolean isStableDestination(BlockPos destinationIn) {
            if (this.entityIn.wantsToBeInWater()) {
                return !this.entityIn.level().getBlockState(destinationIn).isSolidRender() && this.entityIn.level().getFluidState(destinationIn) != null;
            }
            else if (this.entityIn.wantsToBeOnLand()) {
                BlockPos blockpos = destinationIn.below();
                return this.entityIn.level().getBlockState(blockpos).isSolidRender();
            }
            return !this.entityIn.level().getBlockState(destinationIn.below()).isAir();
        }
    }
}