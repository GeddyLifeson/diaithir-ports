package com.iafenvoy.jupiter.mixin;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.ConfigHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Pseudo
@Mixin(value = AutoConfig.class, remap = false)
public interface AutoConfigAccessor {
    @Accessor("holders")
    static Map<Class<? extends ConfigData>, ConfigHolder<?>> getAllConfigs() {
        throw new AssertionError("This method should be replaced by Mixin.");
    }
}
