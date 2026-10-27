package cn.leolezury.eternalstarlight.common.registry;

import cn.leolezury.eternalstarlight.common.EternalStarlight;
import cn.leolezury.eternalstarlight.common.block.entity.*;
import cn.leolezury.eternalstarlight.common.block.entity.spawner.LunarMonstrositySpawnerBlockEntity;
import cn.leolezury.eternalstarlight.common.block.entity.spawner.PermafrostSpawnerBlockEntity;
import cn.leolezury.eternalstarlight.common.block.entity.spawner.StarlightGolemSpawnerBlockEntity;
import cn.leolezury.eternalstarlight.common.block.entity.spawner.TheGatekeeperSpawnerBlockEntity;
import cn.leolezury.eternalstarlight.common.platform.registry.RegistrationProvider;
import cn.leolezury.eternalstarlight.common.platform.registry.RegistryObject;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class ESBlockEntities {
	public static final RegistrationProvider<BlockEntityType<?>> BLOCK_ENTITIES = RegistrationProvider.get(Registries.BLOCK_ENTITY_TYPE, EternalStarlight.ID);
	public static final RegistryObject<BlockEntityType<?>, BlockEntityType<ESSignBlockEntity>> SIGN = BLOCK_ENTITIES.register("sign", () -> new BlockEntityType<>(ESSignBlockEntity::new, ESBlocks.LUNAR_WALL_SIGN.get(), ESBlocks.LUNAR_SIGN.get(), ESBlocks.NORTHLAND_WALL_SIGN.get(), ESBlocks.NORTHLAND_SIGN.get(), ESBlocks.BANYIN_WALL_SIGN.get(), ESBlocks.BANYIN_SIGN.get(), ESBlocks.SCARLET_WALL_SIGN.get(), ESBlocks.SCARLET_SIGN.get(), ESBlocks.TORREYA_WALL_SIGN.get(), ESBlocks.TORREYA_SIGN.get(), ESBlocks.JINGLESTEM_WALL_SIGN.get(), ESBlocks.JINGLESTEM_SIGN.get(), ESBlocks.CRADLEWOOD_WALL_SIGN.get(), ESBlocks.CRADLEWOOD_SIGN.get()));
	public static final RegistryObject<BlockEntityType<?>, BlockEntityType<ESHangingSignBlockEntity>> HANGING_SIGN = BLOCK_ENTITIES.register("hanging_sign", () -> new BlockEntityType<>(ESHangingSignBlockEntity::new, ESBlocks.LUNAR_WALL_HANGING_SIGN.get(), ESBlocks.LUNAR_HANGING_SIGN.get(), ESBlocks.NORTHLAND_WALL_HANGING_SIGN.get(), ESBlocks.NORTHLAND_HANGING_SIGN.get(), ESBlocks.BANYIN_WALL_HANGING_SIGN.get(), ESBlocks.BANYIN_HANGING_SIGN.get(), ESBlocks.SCARLET_WALL_HANGING_SIGN.get(), ESBlocks.SCARLET_HANGING_SIGN.get(), ESBlocks.TORREYA_WALL_HANGING_SIGN.get(), ESBlocks.TORREYA_HANGING_SIGN.get(), ESBlocks.JINGLESTEM_WALL_HANGING_SIGN.get(), ESBlocks.JINGLESTEM_HANGING_SIGN.get(), ESBlocks.CRADLEWOOD_WALL_HANGING_SIGN.get(), ESBlocks.CRADLEWOOD_HANGING_SIGN.get()));
	public static final RegistryObject<BlockEntityType<?>, BlockEntityType<TorreyaCampfireBlockEntity>> TORREYA_CAMPFIRE = BLOCK_ENTITIES.register("torreya_campfire", () -> new BlockEntityType<>(TorreyaCampfireBlockEntity::new, ESBlocks.TORREYA_CAMPFIRE.get()));
	public static final RegistryObject<BlockEntityType<?>, BlockEntityType<AbyssalGeyserBlockEntity>> ABYSSAL_GEYSER = BLOCK_ENTITIES.register("abyssal_geyser", () -> new BlockEntityType<>(AbyssalGeyserBlockEntity::new, ESBlocks.ABYSSAL_GEYSER.get(), ESBlocks.THERMABYSSAL_GEYSER.get(), ESBlocks.CRYOBYSSAL_GEYSER.get()));
	public static final RegistryObject<BlockEntityType<?>, BlockEntityType<ESSkullBlockEntity>> SKULL = BLOCK_ENTITIES.register("skull", () -> new BlockEntityType<>(ESSkullBlockEntity::new, ESBlocks.TANGLED_SKULL.get(), ESBlocks.TANGLED_WALL_SKULL.get()));
	public static final RegistryObject<BlockEntityType<?>, BlockEntityType<ESBrushableBlockEntity>> BRUSHABLE_BLOCK = BLOCK_ENTITIES.register("brushable_block", () -> new BlockEntityType<>(ESBrushableBlockEntity::new, ESBlocks.SUSPICIOUS_DUSTED_GRAVEL.get(), ESBlocks.SUSPICIOUS_DIMSLAG.get()));
	public static final RegistryObject<BlockEntityType<?>, BlockEntityType<CrystalbornCatalystBlockEntity>> CRYSTALBORN_CATALYST = BLOCK_ENTITIES.register("crystalborn_catalyst", () -> new BlockEntityType<>(CrystalbornCatalystBlockEntity::new, ESBlocks.CRYSTALBORN_CATALYST.get()));
	public static final RegistryObject<BlockEntityType<?>, BlockEntityType<DryingRackBlockEntity>> DRYING_RACK = BLOCK_ENTITIES.register("drying_rack", () -> new BlockEntityType<>(DryingRackBlockEntity::new, ESBlocks.DRYING_RACK.get()));
	public static final RegistryObject<BlockEntityType<?>, BlockEntityType<StarfireBirdNestBlockEntity>> STARFIRE_BIRD_NEST = BLOCK_ENTITIES.register("starfire_bird_nest", () -> new BlockEntityType<>(StarfireBirdNestBlockEntity::new,
		ESBlocks.STARFIRE_BIRD_NEST.get(),
		ESBlocks.OAK_STARFIRE_BIRD_AVIARY.get(),
		ESBlocks.SPRUCE_STARFIRE_BIRD_AVIARY.get(),
		ESBlocks.BIRCH_STARFIRE_BIRD_AVIARY.get(),
		ESBlocks.ACACIA_STARFIRE_BIRD_AVIARY.get(),
		ESBlocks.CHERRY_STARFIRE_BIRD_AVIARY.get(),
		ESBlocks.JUNGLE_STARFIRE_BIRD_AVIARY.get(),
		ESBlocks.DARK_OAK_STARFIRE_BIRD_AVIARY.get(),
		ESBlocks.CRIMSON_STARFIRE_BIRD_AVIARY.get(),
		ESBlocks.WARPED_STARFIRE_BIRD_AVIARY.get(),
		ESBlocks.MANGROVE_STARFIRE_BIRD_AVIARY.get(),
		ESBlocks.BAMBOO_STARFIRE_BIRD_AVIARY.get(),
		ESBlocks.LUNAR_STARFIRE_BIRD_AVIARY.get(),
		ESBlocks.NORTHLAND_STARFIRE_BIRD_AVIARY.get(),
		ESBlocks.BANYIN_STARFIRE_BIRD_AVIARY.get(),
		ESBlocks.SCARLET_STARFIRE_BIRD_AVIARY.get(),
		ESBlocks.TORREYA_STARFIRE_BIRD_AVIARY.get(),
		ESBlocks.JINGLESTEM_STARFIRE_BIRD_AVIARY.get(),
		ESBlocks.CRADLEWOOD_STARFIRE_BIRD_AVIARY.get()
	));
	public static final RegistryObject<BlockEntityType<?>, BlockEntityType<LootChestBlockEntity>> LOOT_CHEST = BLOCK_ENTITIES.register("loot_chest", () -> new BlockEntityType<>(LootChestBlockEntity::new, ESBlocks.LOOT_CHEST.get()));
	public static final RegistryObject<BlockEntityType<?>, BlockEntityType<TheGatekeeperSpawnerBlockEntity>> THE_GATEKEEPER_SPAWNER = BLOCK_ENTITIES.register("the_gatekeeper_spawner", () -> new BlockEntityType<>(TheGatekeeperSpawnerBlockEntity::new, ESBlocks.THE_GATEKEEPER_SPAWNER.get()));
	public static final RegistryObject<BlockEntityType<?>, BlockEntityType<StarlightGolemSpawnerBlockEntity>> STARLIGHT_GOLEM_SPAWNER = BLOCK_ENTITIES.register("starlight_golem_spawner", () -> new BlockEntityType<>(StarlightGolemSpawnerBlockEntity::new, ESBlocks.STARLIGHT_GOLEM_SPAWNER.get()));
	public static final RegistryObject<BlockEntityType<?>, BlockEntityType<PermafrostSpawnerBlockEntity>> PERMAFROST_SPAWNER = BLOCK_ENTITIES.register("permafrost_spawner", () -> new BlockEntityType<>(PermafrostSpawnerBlockEntity::new, ESBlocks.PERMAFROST_SPAWNER.get()));
	public static final RegistryObject<BlockEntityType<?>, BlockEntityType<LunarMonstrositySpawnerBlockEntity>> LUNAR_MONSTROSITY_SPAWNER = BLOCK_ENTITIES.register("lunar_monstrosity_spawner", () -> new BlockEntityType<>(LunarMonstrositySpawnerBlockEntity::new, ESBlocks.LUNAR_MONSTROSITY_SPAWNER.get()));
	public static final RegistryObject<BlockEntityType<?>, BlockEntityType<GolemSteelJetBlockEntity>> GOLEM_STEEL_JET = BLOCK_ENTITIES.register("golem_steel_jet", () -> new BlockEntityType<>(GolemSteelJetBlockEntity::new, ESBlocks.GOLEM_STEEL_JET.get(), ESBlocks.WAXED_GOLEM_STEEL_JET.get(), ESBlocks.OXIDIZED_GOLEM_STEEL_JET.get()));
	public static final RegistryObject<BlockEntityType<?>, BlockEntityType<EnergyTransmitterBlockEntity>> ENERGY_TRANSMITTER = BLOCK_ENTITIES.register("energy_transmitter", () -> new BlockEntityType<>(EnergyTransmitterBlockEntity::new, ESBlocks.ENERGY_TRANSMITTER.get()));
	public static final RegistryObject<BlockEntityType<?>, BlockEntityType<MechanicalSpawnerBlockEntity>> MECHANICAL_SPAWNER = BLOCK_ENTITIES.register("mechanical_spawner", () -> new BlockEntityType<>(MechanicalSpawnerBlockEntity::new, ESBlocks.MECHANICAL_SPAWNER.get()));
	public static final RegistryObject<BlockEntityType<?>, BlockEntityType<AlloyFurnaceBlockEntity>> ALLOY_FURNACE = BLOCK_ENTITIES.register("alloy_furnace", () -> new BlockEntityType<>(AlloyFurnaceBlockEntity::new, ESBlocks.ALLOY_FURNACE.get(), ESBlocks.WAXED_ALLOY_FURNACE.get(), ESBlocks.OXIDIZED_ALLOY_FURNACE.get()));
	public static final RegistryObject<BlockEntityType<?>, BlockEntityType<SolarEggBlockEntity>> SOLAR_EGG = BLOCK_ENTITIES.register("solar_egg", () -> new BlockEntityType<>(SolarEggBlockEntity::new, ESBlocks.SOLAR_EGG.get()));
	public static final RegistryObject<BlockEntityType<?>, BlockEntityType<LunarVineBlockEntity>> LUNAR_VINE = BLOCK_ENTITIES.register("lunar_vine", () -> new BlockEntityType<>(LunarVineBlockEntity::new, ESBlocks.LUNAR_VINE.get()));
	public static final RegistryObject<BlockEntityType<?>, BlockEntityType<CrateBlockEntity>> CRATE = BLOCK_ENTITIES.register("crate", () -> new BlockEntityType<>(CrateBlockEntity::new, ESBlocks.GOLEM_STEEL_CRATE.get()));
	public static final RegistryObject<BlockEntityType<?>, BlockEntityType<DuskLightBlockEntity>> DUSK_LIGHT = BLOCK_ENTITIES.register("dusk_light", () -> new BlockEntityType<>(DuskLightBlockEntity::new, ESBlocks.DUSK_LIGHT.get(), ESBlocks.REINFORCED_DUSK_LIGHT.get()));
	public static final RegistryObject<BlockEntityType<?>, BlockEntityType<DuskEmitterBlockEntity>> DUSK_EMITTER = BLOCK_ENTITIES.register("dusk_emitter", () -> new BlockEntityType<>(DuskEmitterBlockEntity::new, ESBlocks.DUSK_EMITTER.get()));
	public static final RegistryObject<BlockEntityType<?>, BlockEntityType<DuskLockboxBlockEntity>> DUSK_LOCKBOX = BLOCK_ENTITIES.register("dusk_lockbox", () -> new BlockEntityType<>(DuskLockboxBlockEntity::new, ESBlocks.DUSK_LOCKBOX.get()));
	public static final RegistryObject<BlockEntityType<?>, BlockEntityType<FlareSpawnerBlockEntity>> FLARE_SPAWNER = BLOCK_ENTITIES.register("flare_spawner", () -> new BlockEntityType<>(FlareSpawnerBlockEntity::new, ESBlocks.FLARE_SPAWNER.get()));
	public static final RegistryObject<BlockEntityType<?>, BlockEntityType<EclipseCoreBlockEntity>> ECLIPSE_CORE = BLOCK_ENTITIES.register("eclipse_core", () -> new BlockEntityType<>(EclipseCoreBlockEntity::new, ESBlocks.ECLIPSE_CORE.get()));
	public static final RegistryObject<BlockEntityType<?>, BlockEntityType<ESPortalBlockEntity>> STARLIGHT_PORTAL = BLOCK_ENTITIES.register("starlight_portal", () -> new BlockEntityType<>(ESPortalBlockEntity::new, ESBlocks.STARLIGHT_PORTAL.get()));

	public static void loadClass() {
	}
}
