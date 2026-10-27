package com.mrh0.createaddition.index;

import com.mrh0.createaddition.CreateAddition;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;

/** Damage types live in data/createaddition/damage_type/*.json. */
public class CADamageTypes {
    private static ResourceKey<DamageType> key(String name) {
        return ResourceKey.create(Registries.DAMAGE_TYPE, CreateAddition.asResource(name));
    }

    public static final ResourceKey<DamageType> BARBED_WIRE_KEY = key("barbed_wire"), TESLA_COIL_KEY = key("tesla_coil");

    private static DamageSource source(ResourceKey<DamageType> key, LevelReader level) {
        return new DamageSource(level.registryAccess().lookupOrThrow(Registries.DAMAGE_TYPE).getOrThrow(key));
    }

    public static DamageSource barbedWire(Level level) { return source(BARBED_WIRE_KEY, level); }
    public static DamageSource teslaCoil(Level level) { return source(TESLA_COIL_KEY, level); }

    public static void register() {}
}
