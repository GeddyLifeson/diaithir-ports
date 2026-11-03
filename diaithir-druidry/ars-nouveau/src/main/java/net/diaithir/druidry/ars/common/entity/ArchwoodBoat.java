package net.diaithir.druidry.ars.common.entity;

import net.diaithir.druidry.ars.setup.registry.ItemsRegistry;
import net.diaithir.druidry.ars.setup.registry.ModEntities;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.boat.Boat;
import net.minecraft.world.level.Level;

/**
 * Ported from Ars Nouveau (LGPL-3.0). 26.1 boats no longer have a Type enum; the archwood variant is implied by the entity type.
 */
public class ArchwoodBoat extends Boat {

    public ArchwoodBoat(EntityType<? extends Boat> entityType, Level level) {
        super(entityType, level, ItemsRegistry.ARCHWOOD_BOAT::get);
    }

    public ArchwoodBoat(Level level, double x, double y, double z) {
        this(ModEntities.ARCHWOOD_BOAT.get(), level);
        this.setPos(x, y, z);
        this.xo = x;
        this.yo = y;
        this.zo = z;
    }
}
