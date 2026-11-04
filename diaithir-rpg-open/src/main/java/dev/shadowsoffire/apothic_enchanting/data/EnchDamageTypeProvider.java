package dev.shadowsoffire.apothic_enchanting.data;

import dev.shadowsoffire.apothic_enchanting.Ench;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.world.damagesource.DamageScaling;
import net.minecraft.world.damagesource.DamageType;

public class EnchDamageTypeProvider {

    public static void bootstrap(BootstrapContext<DamageType> context) {
        context.register(Ench.DamageTypes.CORRUPTED, new DamageType("apothic_enchanting:corrupted", DamageScaling.NEVER, 0.1F));
    }
}
