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

    public static void register(IEventBus bus) {
        servant("zombie_servant", ZombieServant::new, 0.6F, 1.95F, 1.74F, Zombie::createAttributes);
        servant("husk_servant", HuskServant::new, 0.6F, 1.95F, 1.74F, Husk::createAttributes);
        servant("skeleton_servant", SkeletonServant::new, 0.6F, 1.99F, 1.74F, Skeleton::createAttributes);
        servant("stray_servant", StrayServant::new, 0.6F, 1.99F, 1.74F, Stray::createAttributes);
        servant("wither_skeleton_servant", WitherSkeletonServant::new, 0.7F, 2.4F, 2.1F, WitherSkeleton::createAttributes);
        servant("spider_servant", SpiderServant::new, 1.4F, 0.9F, 0.65F, Spider::createAttributes);
        servant("cave_spider_servant", CaveSpiderServant::new, 0.7F, 0.5F, 0.45F, CaveSpider::createAttributes);
        for (String id : new String[] {"soul_bolt", "magic_bolt"}) {
            PROJECTILES.put(id, TYPES.registerEntityType(id, net.diaithir.goety.entity.BoltEntity::new, MobCategory.MISC,
                b -> b.sized(0.3125F, 0.3125F).clientTrackingRange(4).updateInterval(10)));
        }
        TYPES.register(bus);
        EGGS.register(bus);
        bus.addListener(EntityAttributeCreationEvent.class, e -> MOBS.forEach((id, t) -> e.put(castType(t.get()), ATTRS.get(id).get().build())));
    }

    @SuppressWarnings("unchecked")
    private static EntityType<? extends LivingEntity> castType(EntityType<?> t) {
        return (EntityType<? extends LivingEntity>) t;
    }

    private GoetyEntities() {}
}
