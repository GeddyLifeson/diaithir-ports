package com.github.alexmodguy.alexscaves.server.entity.ai;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.level.Level;

/** Ground navigation stand-in for Citadel's advanced pathfinder, which is not part of this port. */
public class AdvancedPathNavigateNoTeleport extends GroundPathNavigation {

    public enum MovementType {
        WALKING, FLYING, CLIMBING
    }

    public AdvancedPathNavigateNoTeleport(Mob entity, Level world, MovementType type) {
        super(entity, world);
    }

    public AdvancedPathNavigateNoTeleport(Mob entity, Level world) {
        this(entity, world, MovementType.WALKING);
    }
}
