package net.diaithir.worldsys.villageguards;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.List;

public final class VillageGuardsFeature {
    private static final String ID = "diaithirworldsys";
    private static final long COOLDOWN_TICKS = 6000L;
    private static final double AREA = 48.0;

    private static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(Registries.ENTITY_TYPE, ID);
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ID);

    public static final DeferredHolder<EntityType<?>, EntityType<VillageGuard>> GUARD = ENTITIES.register("village_guard",
            () -> EntityType.Builder.of(VillageGuard::new, MobCategory.MISC).sized(0.6F, 1.95F).clientTrackingRange(10)
                    .build(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(ID, "village_guard"))));

    public static final DeferredItem<SpawnEggItem> GUARD_EGG = ITEMS.registerItem("village_guard_spawn_egg",
            p -> new SpawnEggItem(p.spawnEgg(GUARD.get())));

    private static final List<long[]> recent = new ArrayList<>();

    private VillageGuardsFeature() {}

    public static void register(IEventBus modBus) {
        ENTITIES.register(modBus);
        ITEMS.register(modBus);
        modBus.addListener(EntityAttributeCreationEvent.class, e -> e.put(GUARD.get(), VillageGuard.attributes().build()));
        modBus.addListener(BuildCreativeModeTabContentsEvent.class, e -> {
            if (e.getTabKey() == CreativeModeTabs.SPAWN_EGGS) e.accept((Item) GUARD_EGG.get());
        });
        if (net.neoforged.fml.loading.FMLEnvironment.getDist().isClient()) {
            modBus.addListener(net.neoforged.neoforge.client.event.EntityRenderersEvent.RegisterRenderers.class, e -> e.registerEntityRenderer(GUARD.get(), GuardRenderer::new));
        }
        NeoForge.EVENT_BUS.addListener(EntityJoinLevelEvent.class, VillageGuardsFeature::onJoin);
    }

    private static void onJoin(EntityJoinLevelEvent e) {
        if (!(e.getEntity() instanceof Villager villager) || !(e.getLevel() instanceof ServerLevel level) || e.loadedFromDisk()) return;
        AABB box = villager.getBoundingBox().inflate(AREA);
        if (!level.getEntitiesOfClass(VillageGuard.class, box).isEmpty()) return;
        long now = level.getGameTime();
        int x = villager.blockPosition().getX();
        int z = villager.blockPosition().getZ();
        recent.removeIf(r -> now - r[0] > COOLDOWN_TICKS);
        for (long[] r : recent) {
            if (Math.abs(r[1] - x) < AREA && Math.abs(r[2] - z) < AREA) return;
        }
        recent.add(new long[] {now, x, z});
        level.getServer().execute(() -> {
            VillageGuard guard = GUARD.get().create(level, EntitySpawnReason.EVENT);
            if (guard == null) return;
            guard.snapTo(villager.getX() + 1.0, villager.getY(), villager.getZ(), villager.getYRot(), 0.0F);
            level.addFreshEntity(guard);
        });
    }
}
