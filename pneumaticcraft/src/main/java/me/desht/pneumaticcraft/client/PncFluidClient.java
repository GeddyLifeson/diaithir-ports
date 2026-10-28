package me.desht.pneumaticcraft.client;

import me.desht.pneumaticcraft.common.fluid.*;
import me.desht.pneumaticcraft.common.registry.ModFluids;
import me.desht.pneumaticcraft.api.lib.Names;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterFluidModelsEvent;
import net.neoforged.neoforge.client.fluid.FluidTintSource;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Client-side fluid model registration (26.1: fluid rendering is data-driven through {@link FluidModel}).
 */
@EventBusSubscriber(modid = Names.MOD_ID, value = Dist.CLIENT)
public class PncFluidClient {
    private static final Map<Fluid, PNCFluidRenderProps> PROPS = new HashMap<>();

    private record Entry(PNCFluidRenderProps props, Supplier<? extends Fluid> still, Supplier<? extends Fluid> flowing) {}

    private static Entry[] entries() {
        return new Entry[]{
                new Entry(FluidOil.RENDER_PROPS, ModFluids.OIL, ModFluids.OIL_FLOWING),
                new Entry(FluidBiodiesel.RENDER_PROPS, ModFluids.BIODIESEL, ModFluids.BIODIESEL_FLOWING),
                new Entry(FluidDiesel.RENDER_PROPS, ModFluids.DIESEL, ModFluids.DIESEL_FLOWING),
                new Entry(FluidEtchingAcid.RENDER_PROPS, ModFluids.ETCHING_ACID, ModFluids.ETCHING_ACID_FLOWING),
                new Entry(FluidEthanol.RENDER_PROPS, ModFluids.ETHANOL, ModFluids.ETHANOL_FLOWING),
                new Entry(FluidGasoline.RENDER_PROPS, ModFluids.GASOLINE, ModFluids.GASOLINE_FLOWING),
                new Entry(FluidKerosene.RENDER_PROPS, ModFluids.KEROSENE, ModFluids.KEROSENE_FLOWING),
                new Entry(FluidLPG.RENDER_PROPS, ModFluids.LPG, ModFluids.LPG_FLOWING),
                new Entry(FluidLubricant.RENDER_PROPS, ModFluids.LUBRICANT, ModFluids.LUBRICANT_FLOWING),
                new Entry(FluidMemoryEssence.RENDER_PROPS, ModFluids.MEMORY_ESSENCE, ModFluids.MEMORY_ESSENCE_FLOWING),
                new Entry(FluidPlastic.RENDER_PROPS, ModFluids.PLASTIC, ModFluids.PLASTIC_FLOWING),
                new Entry(FluidVegetableOil.RENDER_PROPS, ModFluids.VEGETABLE_OIL, ModFluids.VEGETABLE_OIL_FLOWING),
                new Entry(FluidYeastCulture.RENDER_PROPS, ModFluids.YEAST_CULTURE, ModFluids.YEAST_CULTURE_FLOWING),
        };
    }

    @SubscribeEvent
    public static void registerFluidModels(RegisterFluidModelsEvent event) {
        for (Entry e : entries()) {
            PNCFluidRenderProps p = e.props();
            PROPS.put(e.still().get(), p);
            PROPS.put(e.flowing().get(), p);
            FluidTintSource tint = new FluidTintSource() {
                @Override
                public int color(FluidState state) {
                    return p.getTintColor() >>> 24 == 0 ? p.getTintColor() | 0xFF000000 : p.getTintColor();
                }
            };
            event.register(new FluidModel.Unbaked(new Material(p.getStillTexture(), translucent(p)), new Material(p.getFlowingTexture(), translucent(p)), null, tint),
                    e.still().get(), e.flowing().get());
        }
    }

    private static boolean translucent(PNCFluidRenderProps p) {
        int a = p.getTintColor() >>> 24;
        return a != 0 && a < 255;
    }

    /** @return the identifier of the still sprite (in the blocks atlas) for the fluid, or null if unknown */
    public static net.minecraft.resources.Identifier stillSprite(Fluid fluid) {
        PNCFluidRenderProps p = PROPS.get(fluid);
        if (p != null) return p.getStillTexture();
        if (fluid == net.minecraft.world.level.material.Fluids.WATER) return net.minecraft.resources.Identifier.withDefaultNamespace("block/water_still");
        if (fluid == net.minecraft.world.level.material.Fluids.LAVA) return net.minecraft.resources.Identifier.withDefaultNamespace("block/lava_still");
        return null;
    }

    public static int tintColor(FluidStack stack) {
        PNCFluidRenderProps p = PROPS.get(stack.getFluid());
        if (p != null) return p.getTintColor() | 0xFF000000;
        if (stack.getFluid() == net.minecraft.world.level.material.Fluids.WATER) return 0xFF3F76E4;
        return 0xFFFFFFFF;
    }
}
