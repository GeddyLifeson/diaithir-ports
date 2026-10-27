package cn.leolezury.eternalstarlight.common.registry;

import cn.leolezury.eternalstarlight.common.EternalStarlight;
import cn.leolezury.eternalstarlight.common.entity.attack.*;
import cn.leolezury.eternalstarlight.common.entity.attack.ray.GolemLaserBeam;
import cn.leolezury.eternalstarlight.common.entity.attack.ray.LunarMonstrosityBreath;
import cn.leolezury.eternalstarlight.common.entity.living.AethersentGolem;
import cn.leolezury.eternalstarlight.common.entity.living.GrimstoneGolem;
import cn.leolezury.eternalstarlight.common.entity.living.animal.*;
import cn.leolezury.eternalstarlight.common.entity.living.boss.creeper.SolarCreeper;
import cn.leolezury.eternalstarlight.common.entity.living.boss.gatekeeper.TheGatekeeper;
import cn.leolezury.eternalstarlight.common.entity.living.boss.golem.Permafrost;
import cn.leolezury.eternalstarlight.common.entity.living.boss.golem.StarlightGolem;
import cn.leolezury.eternalstarlight.common.entity.living.boss.monstrosity.LunarMonstrosity;
import cn.leolezury.eternalstarlight.common.entity.living.monster.*;
import cn.leolezury.eternalstarlight.common.entity.living.monster.ectostone.Ectostone;
import cn.leolezury.eternalstarlight.common.entity.living.npc.boarwarf.Boarwarf;
import cn.leolezury.eternalstarlight.common.entity.living.npc.boarwarf.golem.AstralGolem;
import cn.leolezury.eternalstarlight.common.entity.misc.*;
import cn.leolezury.eternalstarlight.common.entity.projectile.*;
import cn.leolezury.eternalstarlight.common.platform.registry.RegistrationProvider;
import cn.leolezury.eternalstarlight.common.platform.registry.RegistryObject;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.player.Player;

public class ESEntities {
	public static final RegistrationProvider<EntityType<?>> ENTITIES = RegistrationProvider.get(Registries.ENTITY_TYPE, EternalStarlight.ID);
	public static final RegistryObject<EntityType<?>, EntityType<ESFallingBlock>> FALLING_BLOCK = ENTITIES.register(
		"falling_block",
		() -> EntityType.Builder.<ESFallingBlock>of(ESFallingBlock::new, MobCategory.MISC)
			.sized(0.98F, 0.98F)
			.clientTrackingRange(10)
			.updateInterval(1)
			.build(ResourceKey.create(Registries.ENTITY_TYPE, EternalStarlight.id("falling_block")))
	);
	public static final RegistryObject<EntityType<?>, EntityType<ESPainting>> PAINTING = ENTITIES.register(
		"painting",
		() -> EntityType.Builder.<ESPainting>of(ESPainting::new, MobCategory.MISC)
			.sized(0.5F, 0.5F)
			.clientTrackingRange(10)
			.updateInterval(Integer.MAX_VALUE)
			.build(ResourceKey.create(Registries.ENTITY_TYPE, EternalStarlight.id("painting")))
	);
	public static final RegistryObject<EntityType<?>, EntityType<AethersentMeteor>> AETHERSENT_METEOR = ENTITIES.register(
		"aethersent_meteor",
		() -> EntityType.Builder.<AethersentMeteor>of(AethersentMeteor::new, MobCategory.MISC)
			.sized(0.98F, 0.98F)
			.clientTrackingRange(10)
			.updateInterval(1)
			.build(ResourceKey.create(Registries.ENTITY_TYPE, EternalStarlight.id("aethersent_meteor")))
	);
	public static final RegistryObject<EntityType<?>, EntityType<AetherstrikeRocketEntity>> AETHERSTRIKE_ROCKET = ENTITIES.register(
		"aetherstrike_rocket",
		() -> EntityType.Builder.<AetherstrikeRocketEntity>of(AetherstrikeRocketEntity::new, MobCategory.MISC)
			.sized(0.25F, 0.25F)
			.clientTrackingRange(4)
			.updateInterval(10)
			.build(ResourceKey.create(Registries.ENTITY_TYPE, EternalStarlight.id("aetherstrike_rocket")))
	);
	public static final RegistryObject<EntityType<?>, EntityType<ESBoat>> BOAT = ENTITIES.register(
		"boat",
		() -> EntityType.Builder.<ESBoat>of(ESBoat::new, MobCategory.MISC)
			.sized(1.375F, 0.5625F)
			.eyeHeight(0.5625F)
			.clientTrackingRange(10)
			.build(ResourceKey.create(Registries.ENTITY_TYPE, EternalStarlight.id("boat")))
	);
	public static final RegistryObject<EntityType<?>, EntityType<ESChestBoat>> CHEST_BOAT = ENTITIES.register(
		"chest_boat",
		() -> EntityType.Builder.<ESChestBoat>of(ESChestBoat::new, MobCategory.MISC)
			.sized(1.375F, 0.5625F)
			.eyeHeight(0.5625F)
			.clientTrackingRange(10)
			.build(ResourceKey.create(Registries.ENTITY_TYPE, EternalStarlight.id("chest_boat")))
	);
	public static final RegistryObject<EntityType<?>, EntityType<Boarwarf>> BOARWARF = ENTITIES.register(
		"boarwarf",
		() -> EntityType.Builder.of(Boarwarf::new, MobCategory.CREATURE)
			.sized(0.6F, 1.7F)
			.clientTrackingRange(8)
			.build(ResourceKey.create(Registries.ENTITY_TYPE, EternalStarlight.id("boarwarf")))
	);
	public static final RegistryObject<EntityType<?>, EntityType<AstralGolem>> ASTRAL_GOLEM = ENTITIES.register(
		"astral_golem",
		() -> EntityType.Builder.of(AstralGolem::new, MobCategory.CREATURE)
			.sized(0.5F, 1.25F)
			.ridingOffset(-0.625F)
			.build(ResourceKey.create(Registries.ENTITY_TYPE, EternalStarlight.id("astral_golem")))
	);
	public static final RegistryObject<EntityType<?>, EntityType<Gleech>> GLEECH = ENTITIES.register(
		"gleech",
		() -> EntityType.Builder.of(Gleech::new, MobCategory.MONSTER)
			.sized(0.5F, 0.4F)
			.eyeHeight(0.13F)
			.passengerAttachments(0.2375F)
			.ridingOffset(-0.0625F)
			.clientTrackingRange(8)
			.build(ResourceKey.create(Registries.ENTITY_TYPE, EternalStarlight.id("gleech")))
	);
	public static final RegistryObject<EntityType<?>, EntityType<GleechEgg>> GLEECH_EGG = ENTITIES.register(
		"gleech_egg",
		() -> EntityType.Builder.<GleechEgg>of(GleechEgg::new, MobCategory.MISC)
			.sized(0.3f, 0.3f)
			.clientTrackingRange(10)
			.updateInterval(1)
			.build(ResourceKey.create(Registries.ENTITY_TYPE, EternalStarlight.id("gleech_egg")))
	);
	public static final RegistryObject<EntityType<?>, EntityType<LonestarSkeleton>> LONESTAR_SKELETON = ENTITIES.register(
		"lonestar_skeleton",
		() -> EntityType.Builder.of(LonestarSkeleton::new, MobCategory.MONSTER)
			.sized(0.6F, 1.99F)
			.eyeHeight(1.74F)
			.ridingOffset(-0.7F)
			.clientTrackingRange(8)
			.build(ResourceKey.create(Registries.ENTITY_TYPE, EternalStarlight.id("lonestar_skeleton")))
	);
	public static final RegistryObject<EntityType<?>, EntityType<NightfallSpider>> NIGHTFALL_SPIDER = ENTITIES.register(
		"nightfall_spider",
		() -> EntityType.Builder.of(NightfallSpider::new, MobCategory.MONSTER)
			.sized(0.75F, 0.75F)
			.clientTrackingRange(8)
			.build(ResourceKey.create(Registries.ENTITY_TYPE, EternalStarlight.id("nightfall_spider")))
	);
	public static final RegistryObject<EntityType<?>, EntityType<Seeker>> SEEKER = ENTITIES.register(
		"seeker",
		() -> EntityType.Builder.of(Seeker::new, MobCategory.MONSTER)
			.sized(1.0F, 1.0F)
			.clientTrackingRange(8)
			.fireImmune()
			.build(ResourceKey.create(Registries.ENTITY_TYPE, EternalStarlight.id("seeker")))
	);
	public static final RegistryObject<EntityType<?>, EntityType<Ectostone>> ECTOSTONE = ENTITIES.register(
		"ectostone",
		() -> EntityType.Builder.of(Ectostone::new, MobCategory.MONSTER)
			.sized(0.75F, 1.0F)
			.clientTrackingRange(4)
			.fireImmune()
			.build(ResourceKey.create(Registries.ENTITY_TYPE, EternalStarlight.id("ectostone")))
	);
	public static final RegistryObject<EntityType<?>, EntityType<ThirstWalker>> THIRST_WALKER = ENTITIES.register(
		"thirst_walker",
		() -> EntityType.Builder.of(ThirstWalker::new, MobCategory.MONSTER)
			.sized(0.6F, 2.5F)
			.ridingOffset(-0.125F)
			.clientTrackingRange(8)
			.build(ResourceKey.create(Registries.ENTITY_TYPE, EternalStarlight.id("thirst_walker")))
	);
	public static final RegistryObject<EntityType<?>, EntityType<Creteor>> CRETEOR = ENTITIES.register(
		"creteor",
		() -> EntityType.Builder.of(Creteor::new, MobCategory.MONSTER)
			.sized(0.6F, 1.5F)
			.clientTrackingRange(8)
			.build(ResourceKey.create(Registries.ENTITY_TYPE, EternalStarlight.id("creteor")))
	);
	public static final RegistryObject<EntityType<?>, EntityType<TinyCreteor>> TINY_CRETEOR = ENTITIES.register(
		"tiny_creteor",
		() -> EntityType.Builder.of(TinyCreteor::new, MobCategory.MONSTER)
			.sized(0.5F, 0.5F)
			.clientTrackingRange(8)
			.build(ResourceKey.create(Registries.ENTITY_TYPE, EternalStarlight.id("tiny_creteor")))
	);
	public static final RegistryObject<EntityType<?>, EntityType<Stranghoul>> STRANGHOUL = ENTITIES.register(
		"stranghoul",
		() -> EntityType.Builder.of(Stranghoul::new, MobCategory.MONSTER)
			.sized(0.6F, 1.99F)
			.eyeHeight(1.74F)
			.ridingOffset(-0.7F)
			.clientTrackingRange(8)
			.build(ResourceKey.create(Registries.ENTITY_TYPE, EternalStarlight.id("stranghoul")))
	);
	public static final RegistryObject<EntityType<?>, EntityType<Ent>> ENT = ENTITIES.register(
		"ent",
		() -> EntityType.Builder.of(Ent::new, MobCategory.CREATURE)
			.sized(0.5F, 0.6875F)
			.passengerAttachments(0.35F)
			.clientTrackingRange(8)
			.build(ResourceKey.create(Registries.ENTITY_TYPE, EternalStarlight.id("ent")))
	);
	public static final RegistryObject<EntityType<?>, EntityType<Ratlin>> RATLIN = ENTITIES.register(
		"ratlin",
		() -> EntityType.Builder.of(Ratlin::new, MobCategory.CREATURE)
			.sized(0.9F, 0.775F)
			.clientTrackingRange(8)
			.build(ResourceKey.create(Registries.ENTITY_TYPE, EternalStarlight.id("ratlin")))
	);
	public static final RegistryObject<EntityType<?>, EntityType<ZombifiedRatlin>> ZOMBIFIED_RATLIN = ENTITIES.register(
		"zombified_ratlin",
		() -> EntityType.Builder.of(ZombifiedRatlin::new, MobCategory.CREATURE)
			.sized(0.9F, 0.775F)
			.clientTrackingRange(8)
			.build(ResourceKey.create(Registries.ENTITY_TYPE, EternalStarlight.id("zombified_ratlin")))
	);
	public static final RegistryObject<EntityType<?>, EntityType<ShadowSnail>> SHADOW_SNAIL = ENTITIES.register(
		"shadow_snail",
		() -> EntityType.Builder.of(ShadowSnail::new, MobCategory.CREATURE)
			.sized(0.5F, 0.4F)
			.eyeHeight(0.13F)
			.passengerAttachments(0.2375F)
			.ridingOffset(-0.0625F)
			.clientTrackingRange(8)
			.build(ResourceKey.create(Registries.ENTITY_TYPE, EternalStarlight.id("shadow_snail")))
	);
	public static final RegistryObject<EntityType<?>, EntityType<Yeti>> YETI = ENTITIES.register(
		"yeti",
		() -> EntityType.Builder.of(Yeti::new, MobCategory.CREATURE)
			.sized(0.9F, 0.9F)
			.clientTrackingRange(8)
			.build(ResourceKey.create(Registries.ENTITY_TYPE, EternalStarlight.id("yeti")))
	);
	public static final RegistryObject<EntityType<?>, EntityType<AuroraDeer>> AURORA_DEER = ENTITIES.register(
		"aurora_deer",
		() -> EntityType.Builder.of(AuroraDeer::new, MobCategory.CREATURE)
			.sized(0.9F, 1.75F)
			.passengerAttachments(1F)
			.clientTrackingRange(8)
			.build(ResourceKey.create(Registries.ENTITY_TYPE, EternalStarlight.id("aurora_deer")))
	);
	public static final RegistryObject<EntityType<?>, EntityType<CrystallizedMoth>> CRYSTALLIZED_MOTH = ENTITIES.register(
		"crystallized_moth",
		() -> EntityType.Builder.of(CrystallizedMoth::new, MobCategory.CREATURE)
			.sized(0.5F, 0.4375F)
			.clientTrackingRange(8)
			.build(ResourceKey.create(Registries.ENTITY_TYPE, EternalStarlight.id("crystallized_moth")))
	);
	public static final RegistryObject<EntityType<?>, EntityType<ShimmerLacewing>> SHIMMER_LACEWING = ENTITIES.register(
		"shimmer_lacewing",
		() -> EntityType.Builder.of(ShimmerLacewing::new, MobCategory.AMBIENT)
			.sized(0.5F, 0.5F)
			.clientTrackingRange(8)
			.build(ResourceKey.create(Registries.ENTITY_TYPE, EternalStarlight.id("shimmer_lacewing")))
	);
	public static final RegistryObject<EntityType<?>, EntityType<StarfireBird>> STARFIRE_BIRD = ENTITIES.register(
		"starfire_bird",
		() -> EntityType.Builder.of(StarfireBird::new, MobCategory.AMBIENT)
			.sized(0.5F, 0.625F)
			.clientTrackingRange(8)
			.build(ResourceKey.create(Registries.ENTITY_TYPE, EternalStarlight.id("starfire_bird")))
	);
	public static final RegistryObject<EntityType<?>, EntityType<GrimstoneGolem>> GRIMSTONE_GOLEM = ENTITIES.register(
		"grimstone_golem",
		() -> EntityType.Builder.of(GrimstoneGolem::new, MobCategory.CREATURE)
			.sized(0.5F, 1F)
			.clientTrackingRange(8)
			.build(ResourceKey.create(Registries.ENTITY_TYPE, EternalStarlight.id("grimstone_golem")))
	);
	public static final RegistryObject<EntityType<?>, EntityType<AethersentGolem>> AETHERSENT_GOLEM = ENTITIES.register(
		"aethersent_golem",
		() -> EntityType.Builder.of(AethersentGolem::new, MobCategory.CREATURE)
			.sized(0.5F, 1.75F)
			.clientTrackingRange(8)
			.build(ResourceKey.create(Registries.ENTITY_TYPE, EternalStarlight.id("aethersent_golem")))
	);
	public static final RegistryObject<EntityType<?>, EntityType<Rookfish>> ROOKFISH = ENTITIES.register(
		"rookfish",
		() -> EntityType.Builder.of(Rookfish::new, MobCategory.WATER_CREATURE)
			.sized(0.6F, 0.6F)
			.eyeHeight(0.4F)
			.clientTrackingRange(8)
			.build(ResourceKey.create(Registries.ENTITY_TYPE, EternalStarlight.id("rookfish")))
	);
	public static final RegistryObject<EntityType<?>, EntityType<Luminofish>> LUMINOFISH = ENTITIES.register(
		"luminofish",
		() -> EntityType.Builder.of(Luminofish::new, MobCategory.WATER_AMBIENT)
			.sized(0.5F, 0.3F)
			.clientTrackingRange(10)
			.build(ResourceKey.create(Registries.ENTITY_TYPE, EternalStarlight.id("luminofish")))
	);
	public static final RegistryObject<EntityType<?>, EntityType<Luminaris>> LUMINARIS = ENTITIES.register(
		"luminaris",
		() -> EntityType.Builder.of(Luminaris::new, MobCategory.WATER_AMBIENT)
			.sized(0.5F, 0.3F)
			.clientTrackingRange(10)
			.build(ResourceKey.create(Registries.ENTITY_TYPE, EternalStarlight.id("luminaris")))
	);
	public static final RegistryObject<EntityType<?>, EntityType<TwilightGaze>> TWILIGHT_GAZE = ENTITIES.register(
		"twilight_gaze",
		() -> EntityType.Builder.of(TwilightGaze::new, MobCategory.WATER_CREATURE)
			.sized(0.9F, 0.6F)
			.clientTrackingRange(10)
			.build(ResourceKey.create(Registries.ENTITY_TYPE, EternalStarlight.id("twilight_gaze")))
	);
	public static final RegistryObject<EntityType<?>, EntityType<Coniceras>> CONICERAS = ENTITIES.register(
		"coniceras",
		() -> EntityType.Builder.of(Coniceras::new, MobCategory.WATER_CREATURE)
			.sized(1.0F, 0.5F)
			.clientTrackingRange(10)
			.build(ResourceKey.create(Registries.ENTITY_TYPE, EternalStarlight.id("coniceras")))
	);
	public static final RegistryObject<EntityType<?>, EntityType<EyeOfSeeking>> EYE_OF_SEEKING = ENTITIES.register(
		"eye_of_seeking",
		() -> EntityType.Builder.<EyeOfSeeking>of(EyeOfSeeking::new, MobCategory.MISC)
			.sized(0.25F, 0.25F)
			.clientTrackingRange(4)
			.updateInterval(1)
			.build(ResourceKey.create(Registries.ENTITY_TYPE, EternalStarlight.id("eye_of_seeking")))
	);
	public static final RegistryObject<EntityType<?>, EntityType<TheGatekeeper>> THE_GATEKEEPER = ENTITIES.register(
		"the_gatekeeper",
		() -> EntityType.Builder.of(TheGatekeeper::new, MobCategory.MONSTER)
			.sized(0.75f, 1.99f)
			.clientTrackingRange(32)
			.eyeHeight(1.74F)
			.fireImmune()
			.build(ResourceKey.create(Registries.ENTITY_TYPE, EternalStarlight.id("the_gatekeeper")))
	);
	public static final RegistryObject<EntityType<?>, EntityType<GatekeeperFireball>> GATEKEEPER_FIREBALL = ENTITIES.register(
		"gatekeeper_fireball",
		() -> EntityType.Builder.<GatekeeperFireball>of(GatekeeperFireball::new, MobCategory.MISC)
			.sized(1.0F, 1.0F)
			.clientTrackingRange(4)
			.updateInterval(1)
			.build(ResourceKey.create(Registries.ENTITY_TYPE, EternalStarlight.id("gatekeeper_fireball")))
	);
	public static final RegistryObject<EntityType<?>, EntityType<StarlightGolem>> STARLIGHT_GOLEM = ENTITIES.register(
		"starlight_golem",
		() -> EntityType.Builder.of(StarlightGolem::new, MobCategory.MONSTER)
			.sized(1.25f, 2.5f)
			.clientTrackingRange(32)
			.fireImmune()
			.build(ResourceKey.create(Registries.ENTITY_TYPE, EternalStarlight.id("starlight_golem")))
	);
	public static final RegistryObject<EntityType<?>, EntityType<GolemLaserBeam>> GOLEM_LASER_BEAM = ENTITIES.register(
		"golem_laser_beam",
		() -> EntityType.Builder.<GolemLaserBeam>of(GolemLaserBeam::new, MobCategory.MISC)
			.sized(0f, 0f)
			.clientTrackingRange(32)
			.build(ResourceKey.create(Registries.ENTITY_TYPE, EternalStarlight.id("golem_laser_beam")))
	);
	public static final RegistryObject<EntityType<?>, EntityType<EnergizedFlame>> ENERGIZED_FLAME = ENTITIES.register(
		"energized_flame",
		() -> EntityType.Builder.of(EnergizedFlame::new, MobCategory.MISC)
			.sized(0f, 0f)
			.build(ResourceKey.create(Registries.ENTITY_TYPE, EternalStarlight.id("energized_flame")))
	);
	public static final RegistryObject<EntityType<?>, EntityType<Freeze>> FREEZE = ENTITIES.register(
		"freeze",
		() -> EntityType.Builder.of(Freeze::new, MobCategory.MONSTER)
			.sized(0.5f, 1.25f)
			.fireImmune()
			.build(ResourceKey.create(Registries.ENTITY_TYPE, EternalStarlight.id("freeze")))
	);
	public static final RegistryObject<EntityType<?>, EntityType<FrozenTube>> FROZEN_TUBE = ENTITIES.register(
		"frozen_tube",
		() -> EntityType.Builder.<FrozenTube>of(FrozenTube::new, MobCategory.MISC)
			.sized(0.3f, 0.3f)
			.clientTrackingRange(10)
			.updateInterval(1)
			.build(ResourceKey.create(Registries.ENTITY_TYPE, EternalStarlight.id("frozen_tube")))
	);
	public static final RegistryObject<EntityType<?>, EntityType<Permafrost>> PERMAFROST = ENTITIES.register(
		"permafrost",
		() -> EntityType.Builder.of(Permafrost::new, MobCategory.MONSTER)
			.sized(0.75f, 2.25f)
			.clientTrackingRange(32)
			.fireImmune()
			.build(ResourceKey.create(Registries.ENTITY_TYPE, EternalStarlight.id("permafrost")))
	);
	public static final RegistryObject<EntityType<?>, EntityType<PermafrostSpit>> PERMAFROST_SPIT = ENTITIES.register(
		"permafrost_spit",
		() -> EntityType.Builder.<PermafrostSpit>of(PermafrostSpit::new, MobCategory.MISC)
			.sized(0.3f, 0.3f)
			.clientTrackingRange(10)
			.updateInterval(1)
			.build(ResourceKey.create(Registries.ENTITY_TYPE, EternalStarlight.id("permafrost_spit")))
	);
	public static final RegistryObject<EntityType<?>, EntityType<PermafrostCloud>> PERMAFROST_CLOUD = ENTITIES.register(
		"permafrost_cloud",
		() -> EntityType.Builder.of(PermafrostCloud::new, MobCategory.MISC)
			.sized(3.5f, 0.3f)
			.clientTrackingRange(10)
			.build(ResourceKey.create(Registries.ENTITY_TYPE, EternalStarlight.id("permafrost_cloud")))
	);
	public static final RegistryObject<EntityType<?>, EntityType<Coldsnap>> COLDSNAP = ENTITIES.register(
		"coldsnap",
		() -> EntityType.Builder.<Coldsnap>of(Coldsnap::new, MobCategory.MISC)
			.sized(0.0f, 0.0f)
			.clientTrackingRange(10)
			.updateInterval(1)
			.build(ResourceKey.create(Registries.ENTITY_TYPE, EternalStarlight.id("coldsnap")))
	);
	public static final RegistryObject<EntityType<?>, EntityType<LunarMonstrosity>> LUNAR_MONSTROSITY = ENTITIES.register(
		"lunar_monstrosity",
		() -> EntityType.Builder.of(LunarMonstrosity::new, MobCategory.MONSTER)
			.sized(0.9f, 3.5f)
			.clientTrackingRange(32)
			.build(ResourceKey.create(Registries.ENTITY_TYPE, EternalStarlight.id("lunar_monstrosity")))
	);
	public static final RegistryObject<EntityType<?>, EntityType<LunarMonstrosityBreath>> LUNAR_MONSTROSITY_BREATH = ENTITIES.register(
		"lunar_monstrosity_breath",
		() -> EntityType.Builder.<LunarMonstrosityBreath>of(LunarMonstrosityBreath::new, MobCategory.MISC)
			.sized(0f, 0f)
			.clientTrackingRange(32)
			.build(ResourceKey.create(Registries.ENTITY_TYPE, EternalStarlight.id("lunar_monstrosity_breath")))
	);
	public static final RegistryObject<EntityType<?>, EntityType<LunarSpore>> LUNAR_SPORE = ENTITIES.register(
		"lunar_spore",
		() -> EntityType.Builder.<LunarSpore>of(LunarSpore::new, MobCategory.MISC)
			.sized(0.3f, 0.3f)
			.clientTrackingRange(6)
			.updateInterval(1)
			.build(ResourceKey.create(Registries.ENTITY_TYPE, EternalStarlight.id("lunar_spore")))
	);
	public static final RegistryObject<EntityType<?>, EntityType<LunarThorn>> LUNAR_THORN = ENTITIES.register(
		"lunar_thorn",
		() -> EntityType.Builder.of(LunarThorn::new, MobCategory.MISC)
			.sized(0.3f, 1f)
			.build(ResourceKey.create(Registries.ENTITY_TYPE, EternalStarlight.id("lunar_thorn")))
	);
	public static final RegistryObject<EntityType<?>, EntityType<PoisonousCloud>> POISONOUS_CLOUD = ENTITIES.register(
		"poisonous_cloud",
		() -> EntityType.Builder.of(PoisonousCloud::new, MobCategory.MISC)
			.sized(5f, 1f)
			.clientTrackingRange(10)
			.build(ResourceKey.create(Registries.ENTITY_TYPE, EternalStarlight.id("poisonous_cloud")))
	);
	public static final RegistryObject<EntityType<?>, EntityType<Tangled>> TANGLED = ENTITIES.register(
		"tangled",
		() -> EntityType.Builder.of(Tangled::new, MobCategory.MONSTER)
			.sized(0.6F, 1.99F)
			.eyeHeight(1.74F)
			.ridingOffset(-0.7F)
			.clientTrackingRange(8)
			.build(ResourceKey.create(Registries.ENTITY_TYPE, EternalStarlight.id("tangled")))
	);
	public static final RegistryObject<EntityType<?>, EntityType<TangledSkull>> TANGLED_SKULL = ENTITIES.register(
		"tangled_skull",
		() -> EntityType.Builder.of(TangledSkull::new, MobCategory.MONSTER)
			.sized(0.5F, 0.5F)
			.clientTrackingRange(8)
			.build(ResourceKey.create(Registries.ENTITY_TYPE, EternalStarlight.id("tangled_skull")))
	);
	public static final RegistryObject<EntityType<?>, EntityType<SolarCreeper>> SOLAR_CREEPER = ENTITIES.register(
		"solar_creeper",
		() -> EntityType.Builder.of(SolarCreeper::new, MobCategory.MONSTER)
			.sized(1.5f, 2.75f)
			.eyeHeight(2.1f)
			.clientTrackingRange(32)
			.fireImmune()
			.build(ResourceKey.create(Registries.ENTITY_TYPE, EternalStarlight.id("solar_creeper")))
	);
	public static final RegistryObject<EntityType<?>, EntityType<TangledHusk>> TANGLED_HUSK = ENTITIES.register(
		"tangled_husk",
		() -> EntityType.Builder.of(TangledHusk::new, MobCategory.MISC)
			.sized(0.6F, 1.8F)
			.eyeHeight(1.62F)
			.vehicleAttachment(Player.DEFAULT_VEHICLE_ATTACHMENT)
			.clientTrackingRange(32)
			.updateInterval(2)
			.build(ResourceKey.create(Registries.ENTITY_TYPE, EternalStarlight.id("tangled_husk")))
	);
	public static final RegistryObject<EntityType<?>, EntityType<ThrownShatteredBlade>> SHATTERED_BLADE = ENTITIES.register(
		"shattered_blade",
		() -> EntityType.Builder.<ThrownShatteredBlade>of(ThrownShatteredBlade::new, MobCategory.MISC)
			.sized(0.3f, 0.3f)
			.clientTrackingRange(6)
			.updateInterval(1)
			.build(ResourceKey.create(Registries.ENTITY_TYPE, EternalStarlight.id("shattered_blade")))
	);
	public static final RegistryObject<EntityType<?>, EntityType<ThrownMalariteSpear>> MALARITE_SPEAR = ENTITIES.register(
		"malarite_spear",
		() -> EntityType.Builder.<ThrownMalariteSpear>of(ThrownMalariteSpear::new, MobCategory.MISC)
			.sized(0.3f, 0.3f)
			.clientTrackingRange(6)
			.updateInterval(1)
			.build(ResourceKey.create(Registries.ENTITY_TYPE, EternalStarlight.id("malarite_spear")))
	);
	public static final RegistryObject<EntityType<?>, EntityType<TearBomb>> TEAR_BOMB = ENTITIES.register(
		"tear_bomb",
		() -> EntityType.Builder.<TearBomb>of(TearBomb::new, MobCategory.MISC)
			.fireImmune()
			.sized(0.98F, 0.98F)
			.eyeHeight(0.15F)
			.clientTrackingRange(10)
			.updateInterval(10)
			.build(ResourceKey.create(Registries.ENTITY_TYPE, EternalStarlight.id("tear_bomb")))
	);
	public static final RegistryObject<EntityType<?>, EntityType<TearBombMinecart>> TEAR_BOMB_MINECART = ENTITIES.register(
		"tear_bomb_minecart",
		() -> EntityType.Builder.<TearBombMinecart>of(TearBombMinecart::new, MobCategory.MISC)
			.sized(0.98F, 0.7F)
			.passengerAttachments(0.1875F)
			.clientTrackingRange(8)
			.build(ResourceKey.create(Registries.ENTITY_TYPE, EternalStarlight.id("tear_bomb_minecart")))
	);
	public static final RegistryObject<EntityType<?>, EntityType<ThrownPungencyFruitSpear>> PUNGENCY_FRUIT_SPEAR = ENTITIES.register(
		"pungency_fruit_spear",
		() -> EntityType.Builder.<ThrownPungencyFruitSpear>of(ThrownPungencyFruitSpear::new, MobCategory.MISC)
			.sized(0.3f, 0.3f)
			.clientTrackingRange(6)
			.updateInterval(1)
			.build(ResourceKey.create(Registries.ENTITY_TYPE, EternalStarlight.id("pungency_fruit_spear")))
	);
	public static final RegistryObject<EntityType<?>, EntityType<ThioquartzArrow>> THIOQUARTZ_ARROW = ENTITIES.register(
		"thioquartz_arrow",
		() -> EntityType.Builder.<ThioquartzArrow>of(ThioquartzArrow::new, MobCategory.MISC)
			.sized(0.5F, 0.5F)
			.clientTrackingRange(4)
			.updateInterval(20)
			.build(ResourceKey.create(Registries.ENTITY_TYPE, EternalStarlight.id("thioquartz_arrow")))
	);
	public static final RegistryObject<EntityType<?>, EntityType<ThioquartzShard>> THIOQUARTZ_SHARD = ENTITIES.register(
		"thioquartz_shard",
		() -> EntityType.Builder.<ThioquartzShard>of(ThioquartzShard::new, MobCategory.MISC)
			.sized(0.2f, 0.2f)
			.clientTrackingRange(10)
			.updateInterval(1)
			.build(ResourceKey.create(Registries.ENTITY_TYPE, EternalStarlight.id("thioquartz_shard")))
	);
	public static final RegistryObject<EntityType<?>, EntityType<AethersentArrow>> AETHERSENT_ARROW = ENTITIES.register(
		"aethersent_arrow",
		() -> EntityType.Builder.<AethersentArrow>of(AethersentArrow::new, MobCategory.MISC)
			.sized(0.5F, 0.5F)
			.clientTrackingRange(4)
			.updateInterval(20)
			.build(ResourceKey.create(Registries.ENTITY_TYPE, EternalStarlight.id("aethersent_arrow")))
	);
	public static final RegistryObject<EntityType<?>, EntityType<GlaciteArrow>> GLACITE_ARROW = ENTITIES.register(
		"glacite_arrow",
		() -> EntityType.Builder.<GlaciteArrow>of(GlaciteArrow::new, MobCategory.MISC)
			.sized(0.5F, 0.5F)
			.clientTrackingRange(4)
			.updateInterval(20)
			.build(ResourceKey.create(Registries.ENTITY_TYPE, EternalStarlight.id("glacite_arrow")))
	);
	public static final RegistryObject<EntityType<?>, EntityType<MalariteArrow>> MALARITE_ARROW = ENTITIES.register(
		"malarite_arrow",
		() -> EntityType.Builder.<MalariteArrow>of(MalariteArrow::new, MobCategory.MISC)
			.sized(0.5F, 0.5F)
			.clientTrackingRange(4)
			.updateInterval(20)
			.build(ResourceKey.create(Registries.ENTITY_TYPE, EternalStarlight.id("malarite_arrow")))
	);
	public static final RegistryObject<EntityType<?>, EntityType<AmaramberArrow>> AMARAMBER_ARROW = ENTITIES.register(
		"amaramber_arrow",
		() -> EntityType.Builder.<AmaramberArrow>of(AmaramberArrow::new, MobCategory.MISC)
			.sized(0.5F, 0.5F)
			.clientTrackingRange(4)
			.updateInterval(20)
			.build(ResourceKey.create(Registries.ENTITY_TYPE, EternalStarlight.id("amaramber_arrow")))
	);
	public static final RegistryObject<EntityType<?>, EntityType<VoraciousArrow>> VORACIOUS_ARROW = ENTITIES.register(
		"voracious_arrow",
		() -> EntityType.Builder.<VoraciousArrow>of(VoraciousArrow::new, MobCategory.MISC)
			.sized(0.5F, 0.5F)
			.clientTrackingRange(4)
			.updateInterval(20)
			.build(ResourceKey.create(Registries.ENTITY_TYPE, EternalStarlight.id("voracious_arrow")))
	);
	public static final RegistryObject<EntityType<?>, EntityType<AirSacArrow>> AIR_SAC_ARROW = ENTITIES.register(
		"air_sac_arrow",
		() -> EntityType.Builder.<AirSacArrow>of(AirSacArrow::new, MobCategory.MISC)
			.sized(0.5F, 0.5F)
			.clientTrackingRange(4)
			.updateInterval(20)
			.build(ResourceKey.create(Registries.ENTITY_TYPE, EternalStarlight.id("air_sac_arrow")))
	);
	public static final RegistryObject<EntityType<?>, EntityType<SpiralArrow>> SPIRAL_ARROW = ENTITIES.register(
		"spiral_arrow",
		() -> EntityType.Builder.<SpiralArrow>of(SpiralArrow::new, MobCategory.MISC)
			.sized(0.5F, 0.5F)
			.clientTrackingRange(4)
			.updateInterval(20)
			.build(ResourceKey.create(Registries.ENTITY_TYPE, EternalStarlight.id("spiral_arrow")))
	);
	public static final RegistryObject<EntityType<?>, EntityType<SonarBomb>> SONAR_BOMB = ENTITIES.register(
		"sonar_bomb",
		() -> EntityType.Builder.<SonarBomb>of(SonarBomb::new, MobCategory.MISC)
			.sized(0.3f, 0.3f)
			.clientTrackingRange(10)
			.updateInterval(1)
			.build(ResourceKey.create(Registries.ENTITY_TYPE, EternalStarlight.id("sonar_bomb")))
	);
	public static final RegistryObject<EntityType<?>, EntityType<MagicStone>> MAGIC_STONE = ENTITIES.register(
		"magic_stone",
		() -> EntityType.Builder.<MagicStone>of(MagicStone::new, MobCategory.MISC)
			.sized(0.3f, 0.3f)
			.clientTrackingRange(10)
			.updateInterval(1)
			.build(ResourceKey.create(Registries.ENTITY_TYPE, EternalStarlight.id("magic_stone")))
	);
	public static final RegistryObject<EntityType<?>, EntityType<AshenSnowball>> ASHEN_SNOWBALL = ENTITIES.register(
		"ashen_snowball",
		() -> EntityType.Builder.<AshenSnowball>of(AshenSnowball::new, MobCategory.MISC)
			.sized(0.3f, 0.3f)
			.clientTrackingRange(10)
			.updateInterval(1)
			.build(ResourceKey.create(Registries.ENTITY_TYPE, EternalStarlight.id("ashen_snowball")))
	);
	public static final RegistryObject<EntityType<?>, EntityType<FrozenBomb>> FROZEN_BOMB = ENTITIES.register(
		"frozen_bomb",
		() -> EntityType.Builder.<FrozenBomb>of(FrozenBomb::new, MobCategory.MISC)
			.sized(0.3f, 0.3f)
			.clientTrackingRange(10)
			.updateInterval(1)
			.build(ResourceKey.create(Registries.ENTITY_TYPE, EternalStarlight.id("frozen_bomb")))
	);
	public static final RegistryObject<EntityType<?>, EntityType<WiltedPetal>> WILTED_PETAL = ENTITIES.register(
		"wilted_petal",
		() -> EntityType.Builder.<WiltedPetal>of(WiltedPetal::new, MobCategory.MISC)
			.sized(0.2f, 0.2f)
			.clientTrackingRange(10)
			.updateInterval(1)
			.build(ResourceKey.create(Registries.ENTITY_TYPE, EternalStarlight.id("wilted_petal")))
	);
	public static final RegistryObject<EntityType<?>, EntityType<ShotSeeds>> SHOT_SEEDS = ENTITIES.register(
		"shot_seeds",
		() -> EntityType.Builder.<ShotSeeds>of(ShotSeeds::new, MobCategory.MISC)
			.sized(0.2f, 0.2f)
			.clientTrackingRange(10)
			.updateInterval(1)
			.build(ResourceKey.create(Registries.ENTITY_TYPE, EternalStarlight.id("shot_seeds")))
	);
	public static final RegistryObject<EntityType<?>, EntityType<ThrownStarfire>> STARFIRE = ENTITIES.register(
		"starfire",
		() -> EntityType.Builder.<ThrownStarfire>of(ThrownStarfire::new, MobCategory.MISC)
			.sized(0.3f, 0.3f)
			.clientTrackingRange(10)
			.updateInterval(1)
			.build(ResourceKey.create(Registries.ENTITY_TYPE, EternalStarlight.id("starfire")))
	);
	public static final RegistryObject<EntityType<?>, EntityType<Candlash>> CANDLASH = ENTITIES.register(
		"candlash",
		() -> EntityType.Builder.<Candlash>of(Candlash::new, MobCategory.MISC)
			.sized(0.0f, 0.0f)
			.clientTrackingRange(10)
			.updateInterval(1)
			.build(ResourceKey.create(Registries.ENTITY_TYPE, EternalStarlight.id("candlash")))
	);
	public static final RegistryObject<EntityType<?>, EntityType<TentacleSpike>> TENTACLE_SPIKE = ENTITIES.register(
		"tentacle_spike",
		() -> EntityType.Builder.<TentacleSpike>of(TentacleSpike::new, MobCategory.MISC)
			.sized(0.0f, 0.0f)
			.clientTrackingRange(10)
			.updateInterval(1)
			.build(ResourceKey.create(Registries.ENTITY_TYPE, EternalStarlight.id("tentacle_spike")))
	);
	public static final RegistryObject<EntityType<?>, EntityType<EnergySpark>> ENERGY_SPARK = ENTITIES.register(
		"energy_spark",
		() -> EntityType.Builder.<EnergySpark>of(EnergySpark::new, MobCategory.MISC)
			.sized(0.5f, 0.5f)
			.clientTrackingRange(10)
			.updateInterval(1)
			.build(ResourceKey.create(Registries.ENTITY_TYPE, EternalStarlight.id("energy_spark")))
	);
	public static final RegistryObject<EntityType<?>, EntityType<BallLightning>> BALL_LIGHTNING = ENTITIES.register(
		"ball_lightning",
		() -> EntityType.Builder.<BallLightning>of(BallLightning::new, MobCategory.MISC)
			.sized(0.5f, 0.5f)
			.clientTrackingRange(10)
			.updateInterval(1)
			.build(ResourceKey.create(Registries.ENTITY_TYPE, EternalStarlight.id("ball_lightning")))
	);
	public static final RegistryObject<EntityType<?>, EntityType<CrystalCluster>> CRYSTAL_CLUSTER = ENTITIES.register(
		"crystal_cluster",
		() -> EntityType.Builder.of(CrystalCluster::new, MobCategory.MISC)
			.sized(0.75f, 0.75f)
			.clientTrackingRange(10)
			.updateInterval(1)
			.build(ResourceKey.create(Registries.ENTITY_TYPE, EternalStarlight.id("crystal_cluster")))
	);
	public static final RegistryObject<EntityType<?>, EntityType<ThrownEnergyBoomerang>> ENERGY_BOOMERANG = ENTITIES.register(
		"energy_boomerang",
		() -> EntityType.Builder.<ThrownEnergyBoomerang>of(ThrownEnergyBoomerang::new, MobCategory.MISC)
			.sized(0.3f, 0.3f)
			.clientTrackingRange(6)
			.updateInterval(1)
			.build(ResourceKey.create(Registries.ENTITY_TYPE, EternalStarlight.id("energy_boomerang")))
	);
	public static final RegistryObject<EntityType<?>, EntityType<SoulitSpectator>> SOULIT_SPECTATOR = ENTITIES.register(
		"soulit_spectator",
		() -> EntityType.Builder.<SoulitSpectator>of(SoulitSpectator::new, MobCategory.MISC)
			.sized(0.3f, 0.3f)
			.clientTrackingRange(10)
			.updateInterval(1)
			.build(ResourceKey.create(Registries.ENTITY_TYPE, EternalStarlight.id("soulit_spectator")))
	);
	public static final RegistryObject<EntityType<?>, EntityType<ChainOfSouls>> CHAIN_OF_SOULS = ENTITIES.register(
		"chain_of_souls",
		() -> EntityType.Builder.<ChainOfSouls>of(ChainOfSouls::new, MobCategory.MISC)
			.sized(0.3f, 0.3f)
			.clientTrackingRange(10)
			.updateInterval(1)
			.build(ResourceKey.create(Registries.ENTITY_TYPE, EternalStarlight.id("chain_of_souls")))
	);
	public static final RegistryObject<EntityType<?>, EntityType<BouncyStar>> BOUNCY_STAR = ENTITIES.register(
		"bouncy_star",
		() -> EntityType.Builder.of(BouncyStar::new, MobCategory.MISC)
			.sized(0.5f, 0.5f)
			.clientTrackingRange(10)
			.updateInterval(1)
			.build(ResourceKey.create(Registries.ENTITY_TYPE, EternalStarlight.id("bouncy_star")))
	);
	public static final RegistryObject<EntityType<?>, EntityType<ShiningStar>> SHINING_STAR = ENTITIES.register(
		"shining_star",
		() -> EntityType.Builder.of(ShiningStar::new, MobCategory.MISC)
			.sized(0.3f, 0.3f)
			.clientTrackingRange(10)
			.updateInterval(1)
			.build(ResourceKey.create(Registries.ENTITY_TYPE, EternalStarlight.id("shining_star")))
	);
	public static final RegistryObject<EntityType<?>, EntityType<SolarStrikeEntity>> SOLAR_STRIKE = ENTITIES.register(
		"solar_strike",
		() -> EntityType.Builder.of(SolarStrikeEntity::new, MobCategory.MISC)
			.sized(0.0f, 0.0f)
			.clientTrackingRange(32)
			.updateInterval(1)
			.build(ResourceKey.create(Registries.ENTITY_TYPE, EternalStarlight.id("solar_strike")))
	);
	public static final RegistryObject<EntityType<?>, EntityType<OrbitalPlanet>> ORBITAL_PLANET = ENTITIES.register(
		"orbital_planet",
		() -> EntityType.Builder.of(OrbitalPlanet::new, MobCategory.MISC)
			.sized(0.5f, 0.5f)
			.clientTrackingRange(10)
			.updateInterval(1)
			.build(ResourceKey.create(Registries.ENTITY_TYPE, EternalStarlight.id("orbital_planet")))
	);

	public static void loadClass() {
	}
}
