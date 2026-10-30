package diaithir.myth;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Diaithir Myth Beasts: mythic creatures of the Mediterranean and Near Eastern courts. Creature stats and
 * behaviours are adapted from Greek Fantasy by skyjay1 (GPLv3); every model, texture and name here is new.
 * All creatures share one entity class and are described by data/diaithir_myth/myth/creatures.json.
 */
@Mod(DiaithirMyth.MODID)
public class DiaithirMyth {
    public static final String MODID = "diaithir_myth";

    private static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(Registries.ENTITY_TYPE, MODID);
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);

    public static final Map<String, DeferredHolder<EntityType<?>, EntityType<MythBeast>>> TYPES = new LinkedHashMap<>();
    public static final Map<String, DeferredItem<Item>> EGGS = new LinkedHashMap<>();

    public DiaithirMyth(IEventBus bus) {
        for (Creature c : Creature.all()) {
            var type = ENTITY_TYPES.register(c.id, () -> {
                EntityType.Builder<MythBeast> b = EntityType.Builder.<MythBeast>of(
                        (t, level) -> c.hostile ? new MythBeast.Hostile(t, level, c) : new MythBeast(t, level, c),
                        c.hostile ? MobCategory.MONSTER : MobCategory.CREATURE)
                        .sized((float) c.width, (float) c.height).clientTrackingRange(c.boss ? 12 : 8);
                if (c.hostile) {
                    b.notInPeaceful();
                }
                return b.build(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(MODID, c.id)));
            });
            TYPES.put(c.id, type);
            EGGS.put(c.id, ITEMS.registerItem(c.id + "_spawn_egg", props -> new SpawnEggItem(props.spawnEgg(type.get()))));
        }
        ENTITY_TYPES.register(bus);
        ITEMS.register(bus);
        bus.addListener(DiaithirMyth::attributes);
        bus.addListener(DiaithirMyth::placements);
        bus.addListener(DiaithirMyth::tabs);
        NeoForge.EVENT_BUS.addListener(AddServerReloadListenersEvent.class, SpawnGate::onAddReloadListeners);
        NeoForge.EVENT_BUS.addListener(FinalizeSpawnEvent.class, SpawnGate::onFinalizeSpawn);
        SelfTest.init();
        if (FMLEnvironment.getDist() == Dist.CLIENT) {
            diaithir.myth.client.MythClient.init(bus);
        }
    }

    private static void attributes(EntityAttributeCreationEvent event) {
        for (Creature c : Creature.all()) {
            AttributeSupplier.Builder b = net.minecraft.world.entity.Mob.createMobAttributes()
                    .add(Attributes.MAX_HEALTH, c.health)
                    .add(Attributes.MOVEMENT_SPEED, c.speed)
                    .add(Attributes.ATTACK_DAMAGE, c.attack)
                    .add(Attributes.FOLLOW_RANGE, c.follow_range)
                    .add(Attributes.KNOCKBACK_RESISTANCE, c.knockback_resistance)
                    .add(Attributes.ARMOR, c.armor);
            if (c.flyer) {
                b.add(Attributes.FLYING_SPEED, c.flying_speed);
            }
            event.put(TYPES.get(c.id).get(), b.build());
        }
    }

    private static void placements(RegisterSpawnPlacementsEvent event) {
        for (Creature c : Creature.all()) {
            event.register(TYPES.get(c.id).get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                    (type, level, reason, pos, random) -> MythBeast.checkSpawn(c, type, level, reason, pos, random),
                    RegisterSpawnPlacementsEvent.Operation.REPLACE);
        }
    }

    private static void tabs(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey().equals(CreativeModeTabs.SPAWN_EGGS)) {
            EGGS.values().forEach(e -> event.accept(e.get()));
        }
    }
}
