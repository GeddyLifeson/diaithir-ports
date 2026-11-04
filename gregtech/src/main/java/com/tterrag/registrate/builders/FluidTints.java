package com.tterrag.registrate.builders;

import net.minecraft.world.level.material.FluidState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.fluid.FluidTintSource;

/** Client-only helper: tints fluid models with the colour carried by the fluid type's client extension. */
@OnlyIn(Dist.CLIENT)
final class FluidTints {
    private FluidTints() {}

    static FluidTintSource create() {
        return new FluidTintSource() {
            @Override
            public int color(FluidState state) {
                int c = IClientFluidTypeExtensions.of(state) instanceof com.gregtechceu.gtceu.api.registry.registrate.GTClientFluidTypeExtensions g ? g.getTintColor() : 0;
                return c == 0 ? 0xFFFFFFFF : c;
            }
        };
    }
}
