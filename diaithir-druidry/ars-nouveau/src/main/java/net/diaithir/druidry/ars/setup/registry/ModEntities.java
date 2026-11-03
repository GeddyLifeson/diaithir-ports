package net.diaithir.druidry.ars.setup.registry;

import net.diaithir.druidry.ars.api.spell.Spell;
import net.diaithir.druidry.ars.client.particle.ParticleColor;
import net.diaithir.druidry.ars.common.entity.*;
import net.diaithir.druidry.ars.common.lib.LibEntityNames;
import net.diaithir.druidry.ars.common.light.LightManager;
import net.diaithir.druidry.ars.common.spell.augment.AugmentAmplify;
import net.diaithir.druidry.ars.common.spell.augment.AugmentSensitive;
import net.diaithir.druidry.ars.common.spell.effect.*;
import net.diaithir.druidry.ars.common.spell.method.MethodProjectile;
import net.diaithir.druidry.ars.setup.config.Config;
import net.minecraft.core.BlockPos;
import net.diaithir.druidry.ars.ArsNouveau;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Vex;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import static net.diaithir.druidry.ars.ArsNouveau.MODID;

public class ModEntities {

    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(BuiltInRegistries.ENTITY_TYPE, MODID);

    static <T extends Entity> DeferredHolder<EntityType<?>, EntityType<T>> registerEntity(String name, EntityType.Builder<T> builder) {
        return ENTITIES.register(name, () -> builder.build(ResourceKey.create(Registries.ENTITY_TYPE, ArsNouveau.prefix(name))));
    }

    public static final DeferredHolder<EntityType<?>, EntityType<EntityProjectileSpell>> SPELL_PROJ = registerEntity(
            LibEntityNames.SPELL_PROJ,
            EntityType.Builder.<EntityProjectileSpell>of(EntityProjectileSpell::new, MobCategory.MISC)
                    .noSave()
                    .sized(0.5F, 0.5F)
                    .eyeHeight(0.13F)
                    .clientTrackingRange(4)
                    .updateInterval(20)
                    .fireImmune());
    public static final DeferredHolder<EntityType<?>, EntityType<EntityProjectileSpell>> SPELL_PROJ_ARC = registerEntity(
            LibEntityNames.SPELL_PROJ_ARC,
            EntityType.Builder.<EntityProjectileSpell>of((entityType, world) -> new EntityProjectileSpell(entityType, world) {
                        @Override
                        public EntityType<?> getType() {
                            return SPELL_PROJ_ARC.get();
                        }

                        @Override
                        public boolean isNoGravity() {
                            return false;
                        }
                    }, MobCategory.MISC)
                    .sized(0.5f, 0.5f).noSave()
                    .setTrackingRange(20).fireImmune()
                    .setShouldReceiveVelocityUpdates(true));


    public static final DeferredHolder<EntityType<?>, EntityType<EntityFollowProjectile>> ENTITY_FOLLOW_PROJ = registerEntity(
            LibEntityNames.FOLLOW_PROJ,
            EntityType.Builder.<EntityFollowProjectile>of(EntityFollowProjectile::new, MobCategory.MISC)
                    .sized(0.5f, 0.5f).noSave().fireImmune()
                    .setTrackingRange(10)
                    .setShouldReceiveVelocityUpdates(true));



    public static final DeferredHolder<EntityType<?>, EntityType<EntityFlyingItem>> ENTITY_FLYING_ITEM = registerEntity(
            LibEntityNames.FLYING_ITEM,
            EntityType.Builder.<EntityFlyingItem>of(EntityFlyingItem::new, MobCategory.MISC)
                    .sized(0.5f, 0.5f).fireImmune()
                    .setTrackingRange(10).setUpdateInterval(60)
                    .setShouldReceiveVelocityUpdates(true).noSave());


    public static final DeferredHolder<EntityType<?>, EntityType<LightningEntity>> LIGHTNING_ENTITY = registerEntity(LibEntityNames.AN_LIGHTNING, EntityType.Builder.of(LightningEntity::new, MobCategory.MISC)
            .sized(0.0F, 0.0F)
            .clientTrackingRange(16)
            .updateInterval(Integer.MAX_VALUE
            ).setShouldReceiveVelocityUpdates(true).setUpdateInterval(60));



    public static final DeferredHolder<EntityType<?>, EntityType<ScryerCamera>> SCRYER_CAMERA = registerEntity(LibEntityNames.SCRYER_CAMERA, EntityType.Builder.<ScryerCamera>of(ScryerCamera::new, MobCategory.MISC)
            .sized(1.0E-4F, 1.0E-4F).setTrackingRange(256).setUpdateInterval(20).setShouldReceiveVelocityUpdates(true));

    public static final DeferredHolder<EntityType<?>, EntityType<EnchantedFallingBlock>> ENCHANTED_FALLING_BLOCK = registerEntity(
            "enchanted_falling_block", EntityType.Builder.<EnchantedFallingBlock>of(EnchantedFallingBlock::new, MobCategory.MISC).sized(0.98F, 0.98F)
                    .setShouldReceiveVelocityUpdates(true)
                    .setTrackingRange(256));
    public static final DeferredHolder<EntityType<?>, EntityType<EnchantedMageblock>> ENCHANTED_MAGE_BLOCK = registerEntity(
            "enchanted_mage_block", EntityType.Builder.<EnchantedMageblock>of(EnchantedMageblock::new, MobCategory.MISC).sized(0.98F, 0.98F)
                    .setShouldReceiveVelocityUpdates(true)
                    .setTrackingRange(256));
    public static final DeferredHolder<EntityType<?>, EntityType<EnchantedSkull>> ENCHANTED_HEAD_BLOCK = registerEntity(
            "enchanted_head_block", EntityType.Builder.<EnchantedSkull>of(EnchantedSkull::new, MobCategory.MISC).sized(0.98F, 0.98F)
                    .setShouldReceiveVelocityUpdates(true)
                    .setTrackingRange(256));









    public static final DeferredHolder<EntityType<?>, EntityType<ArchwoodBoat>> ARCHWOOD_BOAT = registerEntity(
            LibEntityNames.ARCHWOOD_BOAT,
            EntityType.Builder.<ArchwoodBoat>of(ArchwoodBoat::new, MobCategory.MISC)
                    .sized(1.375F, 0.5625F)
                    .clientTrackingRange(10)
    );

    @SubscribeEvent
    public static void registerPlacements(RegisterSpawnPlacementsEvent event) {


        LightManager.init();
    }

    public static boolean wildenSpawnRules(EntityType<? extends Monster> type, ServerLevelAccessor worldIn, EntitySpawnReason reason, BlockPos pos, RandomSource randomIn) {
        return worldIn.getDifficulty() != Difficulty.PEACEFUL && Monster.checkMonsterSpawnRules(type, worldIn, reason, pos, randomIn)
                && !Config.DIMENSION_BLACKLIST.get().contains(worldIn.getLevel().dimension().identifier().toString());
    }

    @EventBusSubscriber(modid = MODID)
    public static class RegistrationHandler {

        @SubscribeEvent
        public static void registerEntityAttributes(final EntityAttributeCreationEvent event) {
        }
    }

    public static boolean genericGroundSpawn(EntityType<? extends Entity> animal, LevelAccessor worldIn, EntitySpawnReason reason, BlockPos pos, RandomSource random) {
        return worldIn.getBlockState(pos.below()).is(Blocks.GRASS_BLOCK) && worldIn.getRawBrightness(pos, 0) > 8;
    }

    public static boolean beachSpawn(EntityType<? extends Entity> animal, LevelAccessor worldIn, EntitySpawnReason reason, BlockPos pos, RandomSource random) {
        return (worldIn.getBlockState(pos.below()).is(Blocks.GRASS_BLOCK) || worldIn.getBlockState(pos.below()).is(Blocks.SAND) || worldIn.getBlockState(pos.below()).is(Blocks.SANDSTONE)) && worldIn.getRawBrightness(pos, 0) > 8;
    }

}
