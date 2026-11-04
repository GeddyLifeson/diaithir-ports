package net.diaithir.aurorian;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.monster.skeleton.Skeleton;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** The four Aurorian creatures. Bodies are small subclasses of vanilla mobs (vanilla models, upstream textures). */
public final class AurorianMobs {
    private static final DeferredRegister.Entities ENTITIES = DeferredRegister.createEntities(DiaithirAurorian.ID);
    public static final Map<String, DeferredHolder<EntityType<?>, ? extends EntityType<? extends Mob>>> TYPES = new LinkedHashMap<>();
    private static final Map<String, java.util.function.Supplier<AttributeSupplier.Builder>> ATTRS = new LinkedHashMap<>();

    private static ItemStack item(String id) {
        return new ItemStack(DiaithirAurorian.item(id));
    }

    public static final class Hollow extends Zombie {
        public Hollow(EntityType<? extends Zombie> t, Level l) {
            super(t, l);
            this.xpReward = 2;
        }

        @Override
        public boolean doHurtTarget(ServerLevel level, Entity target) {
            boolean hit = super.doHurtTarget(level, target);
            if (hit && target instanceof LivingEntity le) {
                le.addEffect(new MobEffectInstance(MobEffects.HUNGER, 40), this);
            }
            return hit;
        }

        @Override
        protected boolean isSunSensitive() {
            return false;
        }

        @Override
        protected boolean convertsInWater() {
            return false;
        }

        @Override
        protected SoundEvent getAmbientSound() {
            return SoundEvents.HUSK_AMBIENT;
        }

        static boolean checkSpawn(EntityType<Hollow> t, ServerLevelAccessor l, EntitySpawnReason r, BlockPos p, RandomSource rnd) {
            return p.getY() < l.getSeaLevel() && Monster.checkMonsterSpawnRules(t, l, r, p, rnd);
        }
    }

    public static final class UndeadKnight extends Zombie {
        public UndeadKnight(EntityType<? extends Zombie> t, Level l) {
            super(t, l);
            this.xpReward = 20;
        }

        @Override
        protected void populateDefaultEquipmentSlots(RandomSource random, DifficultyInstance difficulty) {
            boolean elite = random.nextFloat() < 0.2F;
            setItemSlot(EquipmentSlot.MAINHAND, item(elite ? "moonstone_sword" : "aurorian_stone_sword"));
            if (elite) {
                setItemSlot(EquipmentSlot.HEAD, item("knight_helmet"));
            }
            setItemSlot(EquipmentSlot.CHEST, item("knight_chestplate"));
            setItemSlot(EquipmentSlot.LEGS, item("knight_leggings"));
            setItemSlot(EquipmentSlot.FEET, item("knight_boots"));
        }

        @Override
        public boolean doHurtTarget(ServerLevel level, Entity target) {
            boolean hit = super.doHurtTarget(level, target);
            if (hit && target instanceof LivingEntity le) {
                le.addEffect(new MobEffectInstance(MobEffects.MINING_FATIGUE, 100), this);
            }
            return hit;
        }

        @Override
        protected boolean isSunSensitive() {
            return false;
        }

        @Override
        protected boolean convertsInWater() {
            return false;
        }

        @Override
        protected SoundEvent getHurtSound(net.minecraft.world.damagesource.DamageSource s) {
            return SoundEvents.IRON_GOLEM_HURT;
        }

        @Override
        protected SoundEvent getDeathSound() {
            return SoundEvents.IRON_GOLEM_DEATH;
        }
    }

    public static final class DungeonSlime extends Slime {
        public DungeonSlime(EntityType<? extends Slime> t, Level l) {
            super(t, l);
        }
    }

    /** The runestone dungeon's boss body; the fight itself (bar, phases, attacks, loot) is the diaithir-bosses profile theaurorian_dungeon_keeper. */
    public static final class DungeonKeeper extends Skeleton {
        public DungeonKeeper(EntityType<? extends Skeleton> t, Level l) {
            super(t, l);
            this.xpReward = 350;
        }

        @Override
        protected void populateDefaultEquipmentSlots(RandomSource random, DifficultyInstance difficulty) {
            setItemSlot(EquipmentSlot.MAINHAND, item("moonstone_sword"));
        }
    }

    private static <T extends Mob> void reg(String id, EntityType.EntityFactory<T> f, float w, float h, java.util.function.Supplier<AttributeSupplier.Builder> attrs) {
        TYPES.put(id, ENTITIES.registerEntityType(id, f, MobCategory.MONSTER, b -> b.sized(w, h).clientTrackingRange(10)));
        ATTRS.put(id, attrs);
        DiaithirAurorian.addEgg(id, t -> new SpawnEggItem(t.spawnEgg(TYPES.get(id).get())));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void init(IEventBus bus) {
        reg("hollow", Hollow::new, 0.6F, 1.95F, () -> Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 10.0).add(Attributes.MOVEMENT_SPEED, 0.24)
            .add(Attributes.ATTACK_DAMAGE, 3.0).add(Attributes.ARMOR, 2.0).add(Attributes.FOLLOW_RANGE, 40.0).add(Attributes.SPAWN_REINFORCEMENTS_CHANCE, 0.0));
        reg("undead_knight", UndeadKnight::new, 0.6F, 1.95F, () -> Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 20.0).add(Attributes.MOVEMENT_SPEED, 0.2)
            .add(Attributes.ATTACK_DAMAGE, 3.0).add(Attributes.ARMOR, 2.0).add(Attributes.KNOCKBACK_RESISTANCE, 0.1).add(Attributes.FOLLOW_RANGE, 20.0)
            .add(Attributes.SPAWN_REINFORCEMENTS_CHANCE, 0.0));
        reg("dungeon_slime", DungeonSlime::new, 0.52F, 0.52F, () -> Monster.createMonsterAttributes().add(Attributes.MOVEMENT_SPEED, 0.35).add(Attributes.ATTACK_DAMAGE, 2.0));
        reg("dungeon_keeper", DungeonKeeper::new, 0.6F, 1.99F, () -> Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 175.0).add(Attributes.MOVEMENT_SPEED, 0.3)
            .add(Attributes.ATTACK_DAMAGE, 2.0).add(Attributes.ARMOR, 4.0).add(Attributes.KNOCKBACK_RESISTANCE, 0.1).add(Attributes.FOLLOW_RANGE, 50.0));
        ENTITIES.register(bus);
        bus.addListener(EntityAttributeCreationEvent.class, e -> TYPES.forEach((id, t) -> e.put((EntityType) t.get(), ATTRS.get(id).get().build())));
        bus.addListener(RegisterSpawnPlacementsEvent.class, e -> {
            e.register((EntityType<Hollow>) TYPES.get("hollow").get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Hollow::checkSpawn,
                RegisterSpawnPlacementsEvent.Operation.REPLACE);
        });
        if (net.neoforged.fml.loading.FMLEnvironment.getDist().isClient()) {
            AurorianClient.init(bus);
        }
    }

    private AurorianMobs() {}
}
