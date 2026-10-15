/*
 * This file is part of Industrial Foregoing.
 *
 * Copyright 2021, Buuz135
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in the
 * Software without restriction, including without limitation the rights to use, copy,
 * modify, merge, publish, distribute, sublicense, and/or sell copies of the Software,
 * and to permit persons to whom the Software is furnished to do so, subject to the
 * following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies
 * or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE
 * FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.buuz135.industrial.fluid;

import com.hrznstudio.titanium.fluid.TitaniumFluidRenderProperties;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.resources.model.sprite.Material;
import net.neoforged.neoforge.client.event.RegisterFluidModelsEvent;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;

public final class OreFluidClient {

    private OreFluidClient() {
    }

    public static void registerModels(RegisterFluidModelsEvent event) {
        for (OreFluidInstance instance : OreFluidInstance.getInstances()) {
            var properties = (TitaniumFluidRenderProperties) instance.getRenderProperties();
            event.register(
                    new FluidModel.Unbaked(
                            new Material(properties.getStillTexture()),
                            new Material(properties.getFlowingTexture()),
                            properties.getOverlayTexture() == null ? null : new Material(properties.getOverlayTexture()),
                            properties.getTintSource()
                    ),
                    instance.getSourceFluid(),
                    instance.getFlowingFluid()
            );
        }
    }

    public static void registerClientExtensions(RegisterClientExtensionsEvent event) {
        for (OreFluidInstance instance : OreFluidInstance.getInstances()) {
            event.registerFluidType(instance.getRenderProperties(), instance.getFluidType().get());
        }
    }
}
