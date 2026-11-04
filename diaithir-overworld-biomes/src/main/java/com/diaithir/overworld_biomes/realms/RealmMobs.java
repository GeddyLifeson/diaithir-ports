package com.diaithir.overworld_biomes.realms;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.entity.monster.Guardian;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.entity.monster.Ravager;
import net.minecraft.world.entity.monster.Silverfish;
import net.minecraft.world.entity.monster.hoglin.Hoglin;
import net.minecraft.world.entity.monster.illager.Vindicator;
import net.minecraft.world.entity.monster.spider.Spider;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Creatures of the realms. Each is a small subclass of a vanilla creature (vanilla model, renderer and melee come free),
 * wearing its own generated texture, with stats from the manifest. Passive roles lose their target goals on join.
 */
public final class RealmMobs {
    public record Kind(EntityType.EntityFactory<? extends Mob> factory, Supplier<AttributeSupplier.Builder> attrs, float w, float h) {}

    public static final class RZombie extends Zombie {
        public RZombie(EntityType<? extends Zombie> t, Level l) { super(t, l); }
        @Override protected boolean isSunSensitive() { return false; }
        @Override protected boolean convertsInWater() { return false; }
    }
    public static final class RIllager extends Vindicator { public RIllager(EntityType<? extends Vindicator> t, Level l) { super(t, l); } }
    public static final class RGolem extends IronGolem { public RGolem(EntityType<? extends IronGolem> t, Level l) { super(t, l); } }
    public static final class RSpider extends Spider { public RSpider(EntityType<? extends Spider> t, Level l) { super(t, l); } }
    public static final class RRavager extends Ravager { public RRavager(EntityType<? extends Ravager> t, Level l) { super(t, l); } }
    public static final class RBlaze extends Blaze { public RBlaze(EntityType<? extends Blaze> t, Level l) { super(t, l); } }
    public static final class RSilverfish extends Silverfish { public RSilverfish(EntityType<? extends Silverfish> t, Level l) { super(t, l); } }
    public static final class RPhantom extends Phantom { public RPhantom(EntityType<? extends Phantom> t, Level l) { super(t, l); } }
    public static final class RGuardian extends Guardian { public RGuardian(EntityType<? extends Guardian> t, Level l) { super(t, l); } }
    public static final class RHoglin extends Hoglin {
        public RHoglin(EntityType<? extends Hoglin> t, Level l) { super(t, l); setImmuneToZombification(true); }
    }

    public static final Map<String, Kind> KINDS = new LinkedHashMap<>();
    private static <T extends Mob> void k(String id, EntityType.EntityFactory<T> f, Supplier<AttributeSupplier.Builder> a, float w, float h) {
        KINDS.put(id, new Kind(f, a, w, h));
    }
    static {
        k("zombie", RZombie::new, Zombie::createAttributes, 0.6F, 1.95F);
        k("illager", RIllager::new, Vindicator::createAttributes, 0.6F, 1.95F);
        k("golem", RGolem::new, IronGolem::createAttributes, 1.4F, 2.7F);
        k("spider", RSpider::new, Spider::createAttributes, 1.4F, 0.9F);
        k("ravager", RRavager::new, Ravager::createAttributes, 1.95F, 2.2F);
        k("blaze", RBlaze::new, Blaze::createAttributes, 0.6F, 1.8F);
        k("silverfish", RSilverfish::new, Silverfish::createAttributes, 0.4F, 0.3F);
        k("phantom", RPhantom::new, Monster::createMonsterAttributes, 0.9F, 0.5F);
        k("guardian", RGuardian::new, Guardian::createAttributes, 0.85F, 0.85F);
        k("hoglin", RHoglin::new, Hoglin::createAttributes, 1.3965F, 1.4F);
    }

    /** every mob registered by any realm: type, attributes, role */
    static final Map<String, DeferredHolder<EntityType<?>, ? extends EntityType<? extends Mob>>> TYPES = new LinkedHashMap<>();
    static final Set<String> PASSIVE = new java.util.HashSet<>();   // "modid:id"
    static final Map<String, String> KIND_OF = new HashMap<>();     // "modid:id" -> kind

    @SuppressWarnings({"unchecked", "rawtypes"})
    static void register(String modId, JsonObject manifest, IEventBus bus, DeferredRegister.Items items) {
        DeferredRegister.Entities ents = DeferredRegister.createEntities(modId);
        for (JsonElement e : manifest.getAsJsonArray("mobs")) {
            JsonObject m = e.getAsJsonObject();
            String id = m.get("id").getAsString();
            Kind k = KINDS.get(m.get("kind").getAsString());
            float sc = m.has("scale") ? m.get("scale").getAsFloat() : 1.0F;
            double hp = m.get("hp").getAsDouble(), atk = m.get("atk").getAsDouble();
            boolean passive = m.get("role").getAsString().equals("passive");
            DeferredHolder<EntityType<?>, EntityType<Mob>> t = ents.registerEntityType(id, (EntityType.EntityFactory) k.factory(),
                    passive ? MobCategory.CREATURE : MobCategory.MONSTER, b -> b.sized(k.w() * sc, k.h() * sc).clientTrackingRange(10));
            TYPES.put(modId + ":" + id, t);
            KIND_OF.put(modId + ":" + id, m.get("kind").getAsString());
            if (passive) PASSIVE.add(modId + ":" + id);
            ATTR_SRC.put(modId + ":" + id, () -> k.attrs().get().add(Attributes.MAX_HEALTH, hp).add(Attributes.ATTACK_DAMAGE, atk)
                    .add(Attributes.SCALE, sc).add(Attributes.STEP_HEIGHT, Math.max(0.6, 0.45 * sc)));
            items.registerItem(id + "_spawn_egg", props -> new SpawnEggItem(props.spawnEgg(t.get())));
        }
        ents.register(bus);
        if (!hooked) {
            hooked = true;
            bus.addListener(EntityAttributeCreationEvent.class, ev -> TYPES.forEach((key, t) -> ev.put((EntityType) t.get(), ATTR_SRC.get(key).get().build())));
            bus.addListener(RegisterSpawnPlacementsEvent.class, ev -> TYPES.values().forEach(t ->
                    ev.register((EntityType) t.get(), net.minecraft.world.entity.SpawnPlacementTypes.ON_GROUND,
                            net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                            (type, level, reason, pos, rand) -> true, RegisterSpawnPlacementsEvent.Operation.REPLACE)));
            NeoForge.EVENT_BUS.addListener(RealmMobs::onJoin);
            if (FMLEnvironment.getDist().isClient()) RealmMobsClient.init(bus);
        }
    }

    private static final Map<String, Supplier<AttributeSupplier.Builder>> ATTR_SRC = new HashMap<>();
    private static boolean hooked;

    private static void onJoin(EntityJoinLevelEvent e) {
        if (e.getLevel().isClientSide() || !(e.getEntity() instanceof Mob mob)) return;
        String key = net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getKey(mob.getType()).toString();
        if (PASSIVE.contains(key)) mob.targetSelector.removeAllGoals(g -> true);
    }
}
