package net.diaithir.bestiary;

import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jspecify.annotations.Nullable;

import java.util.Map;

@Mod(DiaithirBestiary.ID)
public final class DiaithirBestiary {
    public static final String ID = "diaithirbestiary";
    public static final ResourceKey<Registry<CreatureDef>> CREATURES = ResourceKey.createRegistryKey(Identifier.fromNamespaceAndPath(ID, "creature"));
    private static final DeferredRegister.Entities ENTITIES = DeferredRegister.createEntities(ID);
    public static final DeferredHolder<EntityType<?>, EntityType<Creature>> BEAST = type("beast", Creature::new);
    public static final DeferredHolder<EntityType<?>, EntityType<Creature>> FLYER = type("flyer", Creature::new);
    public static final DeferredHolder<EntityType<?>, EntityType<Mech>> MECH = type("mech", Mech::new);
    public static final DeferredHolder<EntityType<?>, EntityType<Avatar>> AVATAR = type("avatar", Avatar::new);
    public static final DeferredHolder<EntityType<?>, EntityType<Stalker>> STALKER = type("stalker", Stalker::new);
    private static final Map<Preset, DeferredHolder<EntityType<?>, ? extends EntityType<? extends Creature>>> TYPES =
            Map.of(Preset.BEAST, BEAST, Preset.FLYER, FLYER, Preset.MECH, MECH, Preset.AVATAR, AVATAR, Preset.STALKER, STALKER);

    public DiaithirBestiary(IEventBus modBus) {
        ENTITIES.register(modBus);
        modBus.addListener(DataPackRegistryEvent.NewRegistry.class, e -> e.dataPackRegistry(CREATURES, CreatureDef.CODEC, CreatureDef.CODEC));
        modBus.addListener(EntityAttributeCreationEvent.class, e -> TYPES.values().forEach(t -> e.put(t.get(), Creature.attributes().build())));
        NeoForge.EVENT_BUS.addListener(RegisterCommandsEvent.class, e -> BestiaryCommands.register(e.getDispatcher()));
        NeoForge.EVENT_BUS.addListener(LevelTickEvent.Post.class, e -> {
            if (e.getLevel() instanceof ServerLevel level) Spawner.tick(level);
        });
        NeoForge.EVENT_BUS.addListener(ServerTickEvent.Post.class, e -> SelfTest.tick(e.getServer()));
    }

    private static <T extends Creature> DeferredHolder<EntityType<?>, EntityType<T>> type(String name, EntityType.EntityFactory<T> factory) {
        return ENTITIES.registerEntityType(name, factory, MobCategory.CREATURE, b -> b.sized(0.9F, 1.4F).clientTrackingRange(10));
    }

    public static EntityType<? extends Creature> typeOf(Preset preset) {
        return TYPES.get(preset).get();
    }

    public static Registry<CreatureDef> creatures(Level level) {
        return level.registryAccess().lookupOrThrow(CREATURES);
    }

    /** Spawns the creature of the given id, or returns null when the id is unknown or a natural spawn has no room. */
    public static @Nullable Creature spawn(ServerLevel level, Identifier id, Vec3 pos, EntitySpawnReason reason) {
        CreatureDef def = creatures(level).getValue(id);
        if (def == null) return null;
        Creature creature = typeOf(def.preset()).create(level, reason);
        if (creature == null) return null;
        creature.snapTo(pos.x, pos.y, pos.z, level.getRandom().nextFloat() * 360F, 0F);
        creature.setKind(id, def, true);
        if (reason == EntitySpawnReason.NATURAL && !level.noCollision(creature)) return null;
        creature.finalizeSpawn(level, level.getCurrentDifficultyAt(creature.blockPosition()), reason, null);
        level.addFreshEntity(creature);
        return creature;
    }
}
