// SPDX-License-Identifier: GPL-3.0-only
// Folded from MrCrayfish's Vehicle Mod (Unofficial 1.20.1 port by bay4lly; original by MrCrayfish, GPL). Rewritten for NeoForge 26.1.2; art is not used.
package net.diaithir.vehicles.crayfish;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.phys.Vec3;

/** Sets a tank trailer down on the block you use it on. */
public class TrailerItem extends Item {
    public TrailerItem(Properties p) {
        super(p);
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        if (!(ctx.getLevel() instanceof ServerLevel level)) return InteractionResult.SUCCESS;
        TankTrailer t = CrayfishVehicles.TANK_TRAILER.get().create(level, EntitySpawnReason.SPAWN_ITEM_USE);
        if (t == null) return InteractionResult.FAIL;
        Vec3 at = Vec3.atBottomCenterOf(ctx.getClickedPos().relative(ctx.getClickedFace()));
        t.setPos(at);
        t.setYRot(ctx.getRotation());
        level.addFreshEntity(t);
        ctx.getItemInHand().shrink(1);
        return InteractionResult.CONSUME;
    }
}
