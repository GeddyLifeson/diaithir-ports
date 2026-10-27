package net.diaithir.fauna;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.LinkedHashMap;
import java.util.Map;

@Mod(FaunaMod.ID)
@EventBusSubscriber(modid = FaunaMod.ID)
public final class FaunaMod {
    public static final String ID = "diaithirfauna";
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(Registries.ENTITY_TYPE, ID);
    public static final Map<String, DeferredHolder<EntityType<?>, EntityType<FaunaMob>>> TYPES = new LinkedHashMap<>();

    static {
        for (Plan p : Plan.ALL.values()) {
            MobCategory cat = p.water() ? MobCategory.WATER_CREATURE : MobCategory.CREATURE;
            TYPES.put(p.name, ENTITIES.register(p.name, id -> EntityType.Builder.of(FaunaMob::new, cat)
                    .sized(p.width, p.height).clientTrackingRange(p.width > 2.5 ? 10 : 8)
                    .build(ResourceKey.create(Registries.ENTITY_TYPE, id))));
        }
    }

    public FaunaMod(IEventBus bus) {
        ENTITIES.register(bus);
    }

    @SubscribeEvent
    static void attributes(EntityAttributeCreationEvent e) {
        TYPES.values().forEach(t -> e.put(t.get(), FaunaMob.attributes().build()));
    }

    @SubscribeEvent
    static void placements(RegisterSpawnPlacementsEvent e) {
        for (var en : TYPES.entrySet()) {
            Plan p = Plan.ALL.get(en.getKey());
            e.register(en.getValue().get(), p.water() ? SpawnPlacementTypes.IN_WATER : SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                    (type, level, reason, pos, random) -> canSpawn(p, level, reason, pos, random), RegisterSpawnPlacementsEvent.Operation.REPLACE);
        }
    }

    private static boolean canSpawn(Plan p, net.minecraft.world.level.ServerLevelAccessor level, EntitySpawnReason reason, BlockPos pos, net.minecraft.util.RandomSource random) {
        if (reason != EntitySpawnReason.NATURAL && reason != EntitySpawnReason.CHUNK_GENERATION) return true;
        if (p.water()) {
            if (!level.getFluidState(pos).is(net.minecraft.tags.FluidTags.WATER)) return false;
        } else {
            if (!level.getBlockState(pos.below()).isValidSpawn(level, pos.below(), TYPES.get(p.name).get())) return false;
        }
        return Habitat.candidates(p.name, level, pos).size() > 0;
    }

    @SubscribeEvent
    static void commands(RegisterCommandsEvent e) {
        e.getDispatcher().register(Commands.literal("fauna").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(Commands.literal("summon").then(Commands.argument("species", StringArgumentType.word())
                        .suggests((c, b) -> SharedSuggestionProvider.suggest(Species.ALL.stream().map(s -> s.id), b))
                        .executes(c -> {
                            Species s = Species.byId(StringArgumentType.getString(c, "species"));
                            if (s == null) {
                                c.getSource().sendFailure(Component.literal("No such species"));
                                return 0;
                            }
                            ServerLevel level = c.getSource().getLevel();
                            FaunaMob m = TYPES.get(s.plan).get().create(level, EntitySpawnReason.COMMAND);
                            m.setPos(c.getSource().getPosition());
                            m.setSpecies(s);
                            level.addFreshEntity(m);
                            return 1;
                        }))));
    }
}
