package net.diaithir.gellings;

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
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlimeBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Gellings: coloured slimes that live in the biome their colour suggests, drop a gel, and fall in swarms when it rains.
 * Clean-room port of the idea behind Terrarian Slimes (MPL-2.0): own names, own art, own code.
 */
@Mod(GellingMod.ID)
@EventBusSubscriber(modid = GellingMod.ID)
public final class GellingMod {
    public static final String ID = "diaithirgellings";
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(Registries.ENTITY_TYPE, ID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ID);
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(ID);

    public static final Map<String, DeferredHolder<EntityType<?>, EntityType<Gelling>>> TYPES = new LinkedHashMap<>();
    public static final Map<String, DeferredItem<Item>> GEL = new LinkedHashMap<>();
    public static final Map<String, DeferredBlock<Block>> GEL_BLOCK = new LinkedHashMap<>();

    static {
        for (Species s : Species.ALL) {
            TYPES.put(s.id(), ENTITIES.register(s.id() + "_gelling", id -> {
                EntityType.Builder<Gelling> b = EntityType.Builder.of(Gelling::new, MobCategory.MONSTER)
                        .sized(0.52F * 2.04F, 0.52F * 2.04F).eyeHeight(0.325F).spawnDimensionsScale(4.0F).clientTrackingRange(10);
                if (s.fireproof()) b.fireImmune();
                return b.build(ResourceKey.create(Registries.ENTITY_TYPE, id));
            }));
            GEL.put(s.id(), ITEMS.registerSimpleItem("gel_" + s.id()));
            GEL_BLOCK.put(s.id(), BLOCKS.registerBlock("gel_block_" + s.id(), SlimeBlock::new,
                    p -> p.mapColor(MapColor.COLOR_LIGHT_GREEN).friction(0.8F).sound(SoundType.SLIME_BLOCK).noOcclusion()));
            ITEMS.registerSimpleBlockItem("gel_block_" + s.id(), GEL_BLOCK.get(s.id()));
        }
    }

    public GellingMod(IEventBus bus) {
        ENTITIES.register(bus);
        ITEMS.register(bus);
        BLOCKS.register(bus);
    }

    static Species speciesOf(EntityType<?> type) {
        for (var e : TYPES.entrySet()) if (e.getValue().get() == type) return Species.byId(e.getKey());
        return null;
    }

    @SubscribeEvent
    static void attributes(EntityAttributeCreationEvent e) {
        TYPES.values().forEach(t -> e.put(t.get(), net.minecraft.world.entity.monster.Monster.createMonsterAttributes().build()));
    }

    @SubscribeEvent
    static void placements(RegisterSpawnPlacementsEvent e) {
        for (var en : TYPES.entrySet()) {
            Species s = Species.byId(en.getKey());
            e.register(en.getValue().get(), s.fireproof() ? SpawnPlacementTypes.ON_GROUND : SpawnPlacementTypes.ON_GROUND,
                    Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                    (type, level, reason, pos, random) -> level.getDifficulty() != net.minecraft.world.Difficulty.PEACEFUL
                            && Mob.checkMobSpawnRules(type, level, reason, pos, random)
                            && (!s.biomes().isEmpty() || level.getRawBrightness(pos, 0) < 8 || reason != EntitySpawnReason.NATURAL),
                    RegisterSpawnPlacementsEvent.Operation.REPLACE);
        }
    }

    @SubscribeEvent
    static void commands(RegisterCommandsEvent e) {
        e.getDispatcher().register(Commands.literal("gellings").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(Commands.literal("spawn").then(Commands.argument("species", StringArgumentType.word())
                        .suggests((c, b) -> SharedSuggestionProvider.suggest(TYPES.keySet(), b))
                        .executes(c -> {
                            String id = StringArgumentType.getString(c, "species");
                            var t = TYPES.get(id);
                            if (t == null) { c.getSource().sendFailure(Component.literal("No such gelling: " + id)); return 0; }
                            ServerLevel lvl = c.getSource().getLevel();
                            BlockPos pos = lvl.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, BlockPos.containing(c.getSource().getPosition()));
                            var g = t.get().spawn(lvl, pos, EntitySpawnReason.COMMAND);
                            c.getSource().sendSuccess(() -> Component.literal(g == null ? "spawn refused at " + pos : "spawned " + g.getType().getDescriptionId() + " size " + g.getSize() + " at " + pos), false);
                            return g != null ? 1 : 0;
                        })))
                .then(Commands.literal("swarm").executes(c -> {
                    ServerLevel lvl = c.getSource().getLevel();
                    var who = c.getSource().getEntity() instanceof net.minecraft.world.entity.player.Player pl ? pl
                            : net.neoforged.neoforge.common.util.FakePlayerFactory.getMinecraft(lvl);
                    int n = GelRain.drop(lvl, who);
                    c.getSource().sendSuccess(() -> Component.literal(n + " gellings fell"), false);
                    return n;
                })));
    }
}
