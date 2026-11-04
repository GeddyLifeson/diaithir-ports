package net.diaithir.vehicles.tracks;

import net.diaithir.vehicles.DiaithirVehicles;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.phys.Vec3;

/** Sets a tracked chassis down on the block you use it on. */
public class ChassisItem extends Item {
    public ChassisItem(Properties p) {
        super(p);
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        if (!(ctx.getLevel() instanceof ServerLevel level)) return InteractionResult.SUCCESS;
        TrackedChassis c = DiaithirVehicles.TRACKED_CHASSIS.get().create(level, EntitySpawnReason.SPAWN_ITEM_USE);
        if (c == null) return InteractionResult.FAIL;
        c.setPos(Vec3.atBottomCenterOf(ctx.getClickedPos().relative(ctx.getClickedFace())));
        c.setYRot(ctx.getRotation());
        level.addFreshEntity(c);
        ctx.getItemInHand().shrink(1);
        return InteractionResult.CONSUME;
    }
}
