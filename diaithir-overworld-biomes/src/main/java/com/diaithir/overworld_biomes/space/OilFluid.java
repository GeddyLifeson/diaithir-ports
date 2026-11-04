package com.diaithir.overworld_biomes.space;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/** Crude oil, the one fluid the pack's Speuran worlds name (as an ocean fill). Nothing pumps it yet. */
public final class OilFluid {
    private static final DeferredRegister<FluidType> TYPES = DeferredRegister.create(NeoForgeRegistries.Keys.FLUID_TYPES, StellarisRealm.MOD_ID);
    private static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(Registries.FLUID, StellarisRealm.MOD_ID);
    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(StellarisRealm.MOD_ID);

    public static final DeferredHolder<FluidType, FluidType> TYPE = TYPES.register("oil", () -> new FluidType(FluidType.Properties.create().density(900).viscosity(4000).canSwim(false).canDrown(true)));
    public static final DeferredHolder<Fluid, BaseFlowingFluid> SOURCE = FLUIDS.register("oil", () -> new BaseFlowingFluid.Source(props()));
    public static final DeferredHolder<Fluid, BaseFlowingFluid> FLOWING = FLUIDS.register("flowing_oil", () -> new BaseFlowingFluid.Flowing(props()));
    public static final DeferredBlock<LiquidBlock> BLOCK = BLOCKS.registerBlock("oil", p -> new LiquidBlock(SOURCE.get(), p),
            () -> BlockBehaviour.Properties.of().replaceable().noCollision().strength(100f).pushReaction(PushReaction.DESTROY).noLootTable().liquid().sound(SoundType.EMPTY));

    private OilFluid() {}

    private static BaseFlowingFluid.Properties props() {
        return new BaseFlowingFluid.Properties(TYPE, SOURCE, FLOWING).block(BLOCK).slopeFindDistance(2).levelDecreasePerBlock(2).tickRate(30);
    }

    public static Item bucket(Item.Properties p) {
        return new BucketItem(SOURCE.get(), p.craftRemainder(Items.BUCKET).stacksTo(1));
    }

    public static void register(IEventBus bus) {
        TYPES.register(bus);
        FLUIDS.register(bus);
        BLOCKS.register(bus);
    }
}
