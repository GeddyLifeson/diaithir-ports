package com.diaithir.nether.incendium;

import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;

public final class IncendiumAttributes {
    public static void onAttributes(EntityAttributeCreationEvent e) {
        e.put(IncendiumRegistries.EMBER_SENTRY.get(), Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 24.0)
                .add(Attributes.ATTACK_DAMAGE, 4.0)
                .add(Attributes.MOVEMENT_SPEED, 0.25)
                .add(Attributes.FOLLOW_RANGE, 32.0)
                .build());
        e.put(IncendiumRegistries.SLAG_BRUTE.get(), Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 40.0)
                .add(Attributes.ATTACK_DAMAGE, 7.0)
                .add(Attributes.MOVEMENT_SPEED, 0.22)
                .add(Attributes.FOLLOW_RANGE, 32.0)
                .build());
        e.put(IncendiumRegistries.ASH_LURKER.get(), Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.ATTACK_DAMAGE, 5.0)
                .add(Attributes.MOVEMENT_SPEED, 0.28)
                .add(Attributes.FOLLOW_RANGE, 32.0)
                .build());
    }

    private IncendiumAttributes() {}
}
