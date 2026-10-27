package com.mrh0.createaddition.index;

import java.util.function.Supplier;

import com.mrh0.createaddition.CreateAddition;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;

public class CASounds {
    public static final Supplier<SoundEvent> ELECTRIC_MOTOR_BUZZ = sound("electric_motor_buzz");
    public static final Supplier<SoundEvent> TESLA_COIL = sound("tesla_coil");
    public static final Supplier<SoundEvent> ELECTRIC_CHARGE = sound("electric_charge");
    public static final Supplier<SoundEvent> LOUD_ZAP = sound("loud_zap");
    public static final Supplier<SoundEvent> LITTLE_ZAP = sound("little_zap");

    private static Supplier<SoundEvent> sound(String name) {
        SoundEvent e = Registry.register(BuiltInRegistries.SOUND_EVENT, CreateAddition.asResource(name),
                SoundEvent.createVariableRangeEvent(CreateAddition.asResource(name)));
        return () -> e;
    }

    public static void register() {}
}
