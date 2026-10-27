package net.diaithir.goety;

import java.util.LinkedHashMap;
import java.util.Map;
import net.diaithir.goety.entity.servants.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.monster.spider.CaveSpider;
import net.minecraft.world.entity.monster.skeleton.Skeleton;
import net.minecraft.world.entity.monster.skeleton.Stray;
import net.minecraft.world.entity.monster.skeleton.WitherSkeleton;
import net.minecraft.world.entity.monster.spider.Spider;
import net.minecraft.world.entity.monster.zombie.Husk;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Entity types of Goety. Servants are vanilla creatures with an owner (see entity.ServantCore). */
public final class GoetyEntities {
    public static final DeferredRegister.Entities TYPES = DeferredRegister.createEntities(Goety.MOD_ID);
    public static final DeferredRegister.Items EGGS = DeferredRegister.createItems(Goety.MOD_ID);
    public static final Map<String, DeferredHolder<EntityType<?>, ? extends EntityType<? extends Mob>>> MOBS = new LinkedHashMap<>();
    public static final Map<String, DeferredItem<Item>> SPAWN_EGGS = new LinkedHashMap<>();
    public static final Map<String, DeferredHolder<EntityType<?>, EntityType<net.diaithir.goety.entity.BoltEntity>>> PROJECTILES = new LinkedHashMap<>();
    private static final Map<String, java.util.function.Supplier<AttributeSupplier.Builder>> ATTRS = new LinkedHashMap<>();

    private static <T extends Mob> void servant(String id, EntityType.EntityFactory<T> f, float w, float h, float eye,
                                                java.util.function.Supplier<AttributeSupplier.Builder> attrs) {
        DeferredHolder<EntityType<?>, EntityType<T>> t = TYPES.registerEntityType(id, f, MobCategory.MISC,
            b -> b.sized(w, h).eyeHeight(eye).clientTrackingRange(8).fireImmune());
        MOBS.put(id, t);
        ATTRS.put(id, attrs);
        SPAWN_EGGS.put(id, EGGS.registerItem(id + "_spawn_egg", p -> new SpawnEggItem(p.spawnEgg(t.get()))));
    }

    public record MobSpec(String id, String factory, float w, float h, float eye, double hp, double dmg, double speed,
                          boolean monster, boolean fire_immune, boolean servant, String render, String tex, String minion, boolean spawn) {}

    public static final java.util.List<MobSpec> EXTRA = new java.util.ArrayList<>();

    private static <T extends Mob> void extra(MobSpec s, EntityType.EntityFactory<T> f, java.util.function.Supplier<AttributeSupplier.Builder> base) {
        DeferredHolder<EntityType<?>, EntityType<T>> t = TYPES.registerEntityType(s.id(), f, s.monster() ? MobCategory.MONSTER : MobCategory.MISC,
            b -> { var x = b.sized(s.w(), s.h()).eyeHeight(s.eye()).clientTrackingRange(s.monster() ? 10 : 8); return s.fire_immune() ? x.fireImmune() : x; });
        MOBS.put(s.id(), t);
        ATTRS.put(s.id(), () -> {
            AttributeSupplier.Builder b = base.get();
            if (s.hp() > 0) b.add(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH, s.hp());
            if (s.dmg() > 0) b.add(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE, s.dmg());
            if (s.speed() > 0) b.add(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED, s.speed());
            return b;
        });
        SPAWN_EGGS.put(s.id(), EGGS.registerItem(s.id() + "_spawn_egg", p -> new SpawnEggItem(p.spawnEgg(t.get()))));
        EXTRA.add(s);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void registerExtra() {
        var specs = Spec.read("mobs.json", new com.google.gson.reflect.TypeToken<java.util.List<MobSpec>>() {});
        for (MobSpec s : specs) {
            switch (s.factory()) {
                case "zombie_servant" -> extra(s, ZombieServant::new, Zombie::createAttributes);
                case "skeleton_servant" -> extra(s, SkeletonServant::new, Skeleton::createAttributes);
                case "evoker_servant" -> extra(s, net.diaithir.goety.entity.more.Mobs.EvokerServant::new, net.minecraft.world.entity.monster.illager.Evoker::createAttributes);
                case "vindicator_servant" -> extra(s, net.diaithir.goety.entity.more.Mobs.VindicatorServant::new, net.minecraft.world.entity.monster.illager.Vindicator::createAttributes);
                case "golem_servant" -> extra(s, net.diaithir.goety.entity.more.Mobs.GolemServant::new, net.minecraft.world.entity.animal.golem.IronGolem::createAttributes);
                case "wraith_servant" -> extra(s, net.diaithir.goety.entity.more.Mobs.WraithServant::new, net.minecraft.world.entity.monster.Vex::createAttributes);
                case "hostile_zombie" -> extra(s, net.diaithir.goety.entity.more.Mobs.HostileZombie::new, Zombie::createAttributes);
                case "hostile_skeleton" -> extra(s, net.diaithir.goety.entity.more.Mobs.HostileSkeleton::new, Skeleton::createAttributes);
                case "hostile_vindicator" -> extra(s, net.diaithir.goety.entity.more.Mobs.HostileVindicator::new, net.minecraft.world.entity.monster.illager.Vindicator::createAttributes);
                case "hostile_evoker" -> extra(s, net.diaithir.goety.entity.more.Mobs.HostileEvoker::new, net.minecraft.world.entity.monster.illager.Evoker::createAttributes);
                case "wraith" -> extra(s, net.diaithir.goety.entity.more.Mobs.Wraith::new, net.minecraft.world.entity.monster.Vex::createAttributes);
                case "necromancer" -> extra(s, (t, l) -> new net.diaithir.goety.entity.more.Mobs.Necromancer((EntityType) t, l).minion(s.minion()), Zombie::createAttributes);
                case "skull_lord" -> extra(s, net.diaithir.goety.entity.more.Mobs.SkullLord::new, Skeleton::createAttributes);
                default -> throw new IllegalStateException("unknown mob factory " + s.factory());
            }
        }
    }

    public static void register(IEventBus bus) {
        servant("zombie_servant", ZombieServant::new, 0.6F, 1.95F, 1.74F, Zombie::createAttributes);
        servant("husk_servant", HuskServant::new, 0.6F, 1.95F, 1.74F, Husk::createAttributes);
        servant("skeleton_servant", SkeletonServant::new, 0.6F, 1.99F, 1.74F, Skeleton::createAttributes);
        servant("stray_servant", StrayServant::new, 0.6F, 1.99F, 1.74F, Stray::createAttributes);
        servant("wither_skeleton_servant", WitherSkeletonServant::new, 0.7F, 2.4F, 2.1F, WitherSkeleton::createAttributes);
        servant("spider_servant", SpiderServant::new, 1.4F, 0.9F, 0.65F, Spider::createAttributes);
        servant("cave_spider_servant", CaveSpiderServant::new, 0.7F, 0.5F, 0.45F, CaveSpider::createAttributes);
        registerExtra();
        for (String id : new String[] {"soul_bolt", "magic_bolt"}) {
            PROJECTILES.put(id, TYPES.registerEntityType(id, net.diaithir.goety.entity.BoltEntity::new, MobCategory.MISC,
                b -> b.sized(0.3125F, 0.3125F).clientTrackingRange(4).updateInterval(10)));
        }
        bus.addListener(net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent.class, e -> {
            for (MobSpec s : EXTRA) if (s.spawn()) e.register(castType2(MOBS.get(s.id()).get()), net.minecraft.world.entity.SpawnPlacementTypes.ON_GROUND,
                net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, (t, lv, r, pos, rnd) -> net.minecraft.world.entity.monster.Monster.checkMonsterSpawnRules((EntityType) t, lv, r, pos, rnd),
                net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent.Operation.REPLACE);
        });
        TYPES.register(bus);
        EGGS.register(bus);
        bus.addListener(EntityAttributeCreationEvent.class, e -> MOBS.forEach((id, t) -> e.put(castType(t.get()), ATTRS.get(id).get().build())));
    }

    @SuppressWarnings("unchecked")
    private static EntityType<? extends LivingEntity> castType(EntityType<?> t) {
        return (EntityType<? extends LivingEntity>) t;
    }

    @SuppressWarnings("unchecked")
    private static <T extends Mob> EntityType<T> castType2(EntityType<?> t) { return (EntityType<T>) t; }

    private GoetyEntities() {}
}
