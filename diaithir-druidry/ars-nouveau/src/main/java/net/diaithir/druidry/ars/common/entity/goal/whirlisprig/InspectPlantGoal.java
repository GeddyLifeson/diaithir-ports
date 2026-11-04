package net.diaithir.druidry.ars.common.entity.goal.whirlisprig;

import net.diaithir.druidry.ars.api.util.BlockUtil;
import net.diaithir.druidry.ars.client.particle.ParticleUtil;
import net.diaithir.druidry.ars.common.block.tile.WhirlisprigTile;
import net.diaithir.druidry.ars.common.entity.EntityFlyingItem;
import net.diaithir.druidry.ars.common.entity.Whirlisprig;
import net.diaithir.druidry.ars.common.entity.goal.DistanceRestrictedGoal;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.function.Supplier;

public class InspectPlantGoal extends DistanceRestrictedGoal {
    Whirlisprig entity;
    BlockPos pos;
    int timeLooking;
    int timePerforming;

    public InspectPlantGoal(Whirlisprig entity, Supplier<BlockPos> from, int maxDistanceFrom) {
        super(from, maxDistanceFrom);
        this.setFlags(EnumSet.of(Flag.LOOK, Flag.MOVE));
        this.entity = entity;
    }

    public boolean hasVisibleSide(BlockPos pos) {
        for (Direction d : Direction.values()) {
            if (entity.level().getBlockState(pos.relative(d)).isAir())
                return true;
        }
        return false;
    }

    @Override
    public void tick() {
        if (this.pos == null) {
            return;
        }
        timePerforming--;
        if (BlockUtil.distanceFrom(entity.blockPosition(), pos) > 1.5) {
            entity.getNavigation().moveTo(this.pos.getX(), this.pos.getY(), this.pos.getZ(), 1.2);
        } else {
            ServerLevel world = (ServerLevel) entity.level();
            entity.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3(this.pos.getX(), this.pos.getY(), this.pos.getZ()));
            if (world.getRandom().nextInt(20) == 0)
                world.sendParticles(ParticleTypes.HEART, this.pos.getX() + 0.5, this.pos.getY() + 1.1, this.pos.getZ() + 0.5, 1, ParticleUtil.inRange(-0.2, 0.2), 0, ParticleUtil.inRange(-0.2, 0.2), 0.01);
            this.timeLooking--;
        }
    }

    @Override
    public boolean canContinueToUse() {
        return pos != null && timeLooking > 0 && timePerforming > 0;
    }

    @Override
    public boolean canUse() {
        WhirlisprigTile tile = entity.getTile();
        if (tile != null && !tile.isOff) {
            if (entity.timeSinceGen > 300) {
                return true;
            } else if (entity.level().getRandom().nextInt(100) <= 2) {
                return entity.level().getGameTime() % 10 == 0;
            }
        }
        return false;
    }

    @Override
    public void start() {
        int range = 4;
        List<BlockPos> list = new ArrayList<>();
        BlockPos.betweenClosedStream(entity.blockPosition().offset(range, range, range), entity.blockPosition().offset(-range, -range, -range)).forEach(bp -> {
            if (WhirlisprigTile.getScore(entity.level().getBlockState(bp)) > 0 && hasVisibleSide(bp) && isInRange(bp)) {
                list.add(bp.immutable());
            }
        });
        if (list.isEmpty())
            return;
        pos = list.get(entity.level().getRandom().nextInt(list.size()));
        this.timeLooking = 120;
        this.timePerforming = 240;
        EntityFlyingItem flyingItem = new EntityFlyingItem(entity.level(), pos, entity.flowerPos, 50, 255, 40);
        flyingItem.getEntityData().set(EntityFlyingItem.HELD_ITEM, entity.level().getBlockState(pos).getBlock().asItem().getDefaultInstance());
        entity.level().addFreshEntity(flyingItem);
        entity.timeSinceGen = 0;
        entity.getTile().addProgress();
    }
}
