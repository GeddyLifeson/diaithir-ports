package net.diaithir.creatureutilities.bees;

import com.mojang.logging.LogUtils;
import net.diaithir.creatureutilities.CreatureUtilities;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.animal.bee.Bee;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.slf4j.Logger;

import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

/** Species bees and their combs. The species are rows in data/diaithircreatureutilities/bees/species.json. */
public final class Bees {
    public static final String ID = CreatureUtilities.ID;
    private static final Logger LOG = LogUtils.getLogger();
    public static final Map<String, BeeSpecies> SPECIES = load();
    public static final Map<String, DeferredItem<Item>> COMBS = new LinkedHashMap<>();

    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ID);
    private static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(Registries.ENTITY_TYPE, ID);

    public static final DeferredHolder<EntityType<?>, EntityType<SpeciesBee>> SPECIES_BEE = ENTITIES.register("species_bee",
        id -> EntityType.Builder.<SpeciesBee>of(SpeciesBee::new, MobCategory.CREATURE).sized(0.7F, 0.6F).eyeHeight(0.3F)
            .clientTrackingRange(8).build(ResourceKey.create(Registries.ENTITY_TYPE, id)));

    private Bees() {}

    public static void init(IEventBus bus) {
        for (String id : SPECIES.keySet()) {
            COMBS.put(id, ITEMS.registerSimpleItem("comb_" + id));
        }
        ITEMS.register(bus);
        ENTITIES.register(bus);
        bus.addListener((EntityAttributeCreationEvent e) -> e.put(SPECIES_BEE.get(), Bee.createAttributes().build()));
        bus.addListener((RegisterSpawnPlacementsEvent e) -> e.register(SPECIES_BEE.get(), SpawnPlacementTypes.NO_RESTRICTIONS,
            Heightmap.Types.MOTION_BLOCKING, Mob::checkMobSpawnRules, RegisterSpawnPlacementsEvent.Operation.OR));
        bus.addListener((BuildCreativeModeTabContentsEvent e) -> {
            if (e.getTabKey() == CreativeModeTabs.INGREDIENTS) {
                COMBS.values().forEach(e::accept);
            }
        });
        LOG.info("Bee species: {} ({} wild)", SPECIES.size(), SPECIES.values().stream().filter(BeeSpecies::wild).count());
    }

    /** Species of a young bee: parents of a listed pair give the bred species half the time, otherwise a parent's own. */
    public static String offspring(String a, String b, RandomSource random) {
        if (b != null && !a.equals(b)) {
            for (BeeSpecies s : SPECIES.values()) {
                if (s.parents().size() == 2 && ((s.parents().get(0).equals(a) && s.parents().get(1).equals(b))
                    || (s.parents().get(0).equals(b) && s.parents().get(1).equals(a)))) {
                    if (random.nextBoolean()) {
                        return s.id();
                    }
                    break;
                }
            }
            return random.nextBoolean() ? a : b;
        }
        return a;
    }

    private static Map<String, BeeSpecies> load() {
        try (Reader r = new InputStreamReader(Bees.class.getResourceAsStream("/data/" + ID + "/bees/species.json"), StandardCharsets.UTF_8)) {
            return BeeSpecies.parse(r);
        } catch (Exception e) {
            LOG.error("Could not read bee species", e);
            return Map.of();
        }
    }
}
