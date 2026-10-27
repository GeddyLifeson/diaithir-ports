package com.github.alexmodguy.alexscaves.server.block.blockentity;

import com.github.alexmodguy.alexscaves.AlexsCaves;
import com.github.alexmodguy.alexscaves.server.block.ACBlockRegistry;
import com.google.common.collect.ImmutableSet;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.minecraft.core.registries.Registries;

public class ACBlockEntityRegistry {

    public static final DeferredRegister<BlockEntityType<?>> DEF_REG = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, AlexsCaves.MODID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<VolcanicCoreBlockEntity>> VOLCANIC_CORE = DEF_REG.register("volcanic_core", () -> new BlockEntityType<>(VolcanicCoreBlockEntity::new, ACBlockRegistry.VOLCANIC_CORE.get()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MagnetBlockEntity>> MAGNET = DEF_REG.register("magnet", () -> new BlockEntityType<>(MagnetBlockEntity::new, ACBlockRegistry.SCARLET_MAGNET.get(), ACBlockRegistry.AZURE_MAGNET.get()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TeslaBulbBlockEntity>> TESLA_BULB = DEF_REG.register("tesla_bulb", () -> new BlockEntityType<>(TeslaBulbBlockEntity::new, ACBlockRegistry.TESLA_BULB.get()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<HologramProjectorBlockEntity>> HOLOGRAM_PROJECTOR = DEF_REG.register("hologram_projector", () -> new BlockEntityType<>(HologramProjectorBlockEntity::new, ACBlockRegistry.HOLOGRAM_PROJECTOR.get()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<QuarryBlockEntity>> QUARRY = DEF_REG.register("quarry", () -> new BlockEntityType<>(QuarryBlockEntity::new, ACBlockRegistry.QUARRY.get()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AmbersolBlockEntity>> AMBERSOL = DEF_REG.register("ambersol", () -> new BlockEntityType<>(AmbersolBlockEntity::new, ACBlockRegistry.AMBERSOL.get()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AmberMonolithBlockEntity>> AMBER_MONOLITH = DEF_REG.register("amber_monolith", () -> new BlockEntityType<>(AmberMonolithBlockEntity::new, ACBlockRegistry.AMBER_MONOLITH.get()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<GeothermalVentBlockEntity>> GEOTHERMAL_VENT = DEF_REG.register("geothermal_vent", () -> new BlockEntityType<>(GeothermalVentBlockEntity::new, ACBlockRegistry.GEOTHERMAL_VENT.get(), ACBlockRegistry.GEOTHERMAL_VENT_MEDIUM.get(), ACBlockRegistry.GEOTHERMAL_VENT_THIN.get()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SirenLightBlockEntity>> SIREN_LIGHT = DEF_REG.register("siren_light", () -> new BlockEntityType<>(SirenLightBlockEntity::new, ACBlockRegistry.SIREN_LIGHT.get()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<NuclearSirenBlockEntity>> NUCLEAR_SIREN = DEF_REG.register("nuclear_siren", () -> new BlockEntityType<>(NuclearSirenBlockEntity::new, ACBlockRegistry.NUCLEAR_SIREN.get()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MetalBarrelBlockEntity>> METAL_BARREL = DEF_REG.register("metal_barrel", () -> new BlockEntityType<>(MetalBarrelBlockEntity::new, ACBlockRegistry.METAL_BARREL.get(), ACBlockRegistry.RUSTY_BARREL.get()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AbyssalAltarBlockEntity>> ABYSSAL_ALTAR = DEF_REG.register("abyssal_altar", () -> new BlockEntityType<>(AbyssalAltarBlockEntity::new, ACBlockRegistry.ABYSSAL_ALTAR.get()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CopperValveBlockEntity>> COPPER_VALVE = DEF_REG.register("copper_valve", () -> new BlockEntityType<>(CopperValveBlockEntity::new, ACBlockRegistry.COPPER_VALVE.get()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<EnigmaticEngineBlockEntity>> ENIGMATIC_ENGINE = DEF_REG.register("enigmatic_engine", () -> new BlockEntityType<>(EnigmaticEngineBlockEntity::new, ACBlockRegistry.ENIGMATIC_ENGINE.get()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BeholderBlockEntity>> BEHOLDER = DEF_REG.register("beholder", () -> new BlockEntityType<>(BeholderBlockEntity::new, ACBlockRegistry.BEHOLDER.get()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<GobthumperBlockEntity>> GOBTHUMPER = DEF_REG.register("gobthumper", () -> new BlockEntityType<>(GobthumperBlockEntity::new, ACBlockRegistry.GOBTHUMPER.get()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ConversionCrucibleBlockEntity>> CONVERSION_CRUCIBLE = DEF_REG.register("conversion_crucible", () -> new BlockEntityType<>(ConversionCrucibleBlockEntity::new, ACBlockRegistry.CONVERSION_CRUCIBLE.get()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<GingerbarrelBlockEntity>> GINGERBARREL = DEF_REG.register("gingerbarrel", () -> new BlockEntityType<>(GingerbarrelBlockEntity::new, ACBlockRegistry.GINGERBARREL.get()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ConfectionOvenBlockEntity>> CONFECTION_OVEN = DEF_REG.register("confection_oven", () -> new BlockEntityType<>(ConfectionOvenBlockEntity::new, ACBlockRegistry.CONFECTION_OVEN.get()));

    // Custom sign blocks are added to vanilla BlockEntityType.SIGN and BlockEntityType.HANGING_SIGN
    // using access transformers to make validBlocks accessible (public-f in accesstransformer.cfg)
    public static void expandVanillaDefinitions(net.neoforged.neoforge.event.BlockEntityTypeAddBlocksEvent event) {
        event.modify(BlockEntityType.SIGN, ACBlockRegistry.PEWEN_SIGN.get(), ACBlockRegistry.PEWEN_WALL_SIGN.get(), ACBlockRegistry.THORNWOOD_SIGN.get(), ACBlockRegistry.THORNWOOD_WALL_SIGN.get());
        event.modify(BlockEntityType.HANGING_SIGN, ACBlockRegistry.PEWEN_HANGING_SIGN.get(), ACBlockRegistry.PEWEN_WALL_HANGING_SIGN.get(), ACBlockRegistry.THORNWOOD_HANGING_SIGN.get(), ACBlockRegistry.THORNWOOD_WALL_HANGING_SIGN.get());
    }
}
