/*
 * This file is part of pnc-repressurized.
 *
 *     pnc-repressurized is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     pnc-repressurized is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with pnc-repressurized.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.desht.pneumaticcraft.client.event;

import me.desht.pneumaticcraft.api.lib.Names;
import me.desht.pneumaticcraft.client.PneumaticCraftRepressurizedClient;
import me.desht.pneumaticcraft.common.block.AbstractPneumaticCraftBlock;
import me.desht.pneumaticcraft.common.fluid.*;
import me.desht.pneumaticcraft.common.registry.ModBlocks;
import me.desht.pneumaticcraft.common.registry.ModFluids;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterFluidModelsEvent;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.client.fluid.FluidTintSource;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.function.Supplier;

// dropped: camouflage block models (CamoModel), custom geometry loaders (camouflaged, fluid_container_item, rendered_item,
// pressure_tube), the minigun custom item renderer and pneumatic armor dye layer colours. All need baked/dynamic model
// APIs that 26.1 removed; the blocks and items use static JSON models instead.
@EventBusSubscriber(modid = Names.MOD_ID, value = Dist.CLIENT)
public class ModClientEventHandler {
    @SubscribeEvent
    public static void registerFluidModels(RegisterFluidModelsEvent event) {
        register(event, FluidOil.RENDER_PROPS, ModFluids.OIL, ModFluids.OIL_FLOWING);
        register(event, FluidBiodiesel.RENDER_PROPS, ModFluids.BIODIESEL, ModFluids.BIODIESEL_FLOWING);
        register(event, FluidDiesel.RENDER_PROPS, ModFluids.DIESEL, ModFluids.DIESEL_FLOWING);
        register(event, FluidEtchingAcid.RENDER_PROPS, ModFluids.ETCHING_ACID, ModFluids.ETCHING_ACID_FLOWING);
        register(event, FluidEthanol.RENDER_PROPS, ModFluids.ETHANOL, ModFluids.ETHANOL_FLOWING);
        register(event, FluidGasoline.RENDER_PROPS, ModFluids.GASOLINE, ModFluids.GASOLINE_FLOWING);
        register(event, FluidKerosene.RENDER_PROPS, ModFluids.KEROSENE, ModFluids.KEROSENE_FLOWING);
        register(event, FluidLPG.RENDER_PROPS, ModFluids.LPG, ModFluids.LPG_FLOWING);
        register(event, FluidLubricant.RENDER_PROPS, ModFluids.LUBRICANT, ModFluids.LUBRICANT_FLOWING);
        register(event, FluidMemoryEssence.RENDER_PROPS, ModFluids.MEMORY_ESSENCE, ModFluids.MEMORY_ESSENCE_FLOWING);
        register(event, FluidPlastic.RENDER_PROPS, ModFluids.PLASTIC, ModFluids.PLASTIC_FLOWING);
        register(event, FluidVegetableOil.RENDER_PROPS, ModFluids.VEGETABLE_OIL, ModFluids.VEGETABLE_OIL_FLOWING);
        register(event, FluidYeastCulture.RENDER_PROPS, ModFluids.YEAST_CULTURE, ModFluids.YEAST_CULTURE_FLOWING);
    }

    private static void register(RegisterFluidModelsEvent event, PNCFluidRenderProps props, Supplier<? extends Fluid> still, Supplier<? extends Fluid> flowing) {
        int tint = props.getTintColor();
        FluidTintSource tintSource = tint == 0xFFFFFFFF ? null : (FluidTintSource) state -> tint;
        event.register(new FluidModel.Unbaked(new Material(props.getStillTexture()), new Material(props.getFlowingTexture()), null, tintSource), still, flowing);
    }

    @SubscribeEvent
    public static void registerClientExtensions(RegisterClientExtensionsEvent event) {
        Block[] toAdd = ModBlocks.BLOCKS.getEntries().stream()
                .filter(h -> h.get() instanceof AbstractPneumaticCraftBlock)
                .map(DeferredHolder::get)
                .toArray(Block[]::new);
        event.registerBlock(PneumaticCraftRepressurizedClient.PARTICLE_HANDLER, toAdd);
    }
}
