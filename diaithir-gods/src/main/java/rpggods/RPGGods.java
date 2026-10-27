package rpggods;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rpggods.deity.Altar;
import rpggods.deity.Deity;
import rpggods.deity.DeityHelper;
import rpggods.deity.Offering;
import rpggods.deity.Sacrifice;
import rpggods.favor.IFavor;
import rpggods.perk.Affinity;
import rpggods.perk.Perk;
import rpggods.tameable.ITameable;
import rpggods.util.CodecJsonDataManager;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Diaithir Gods: a NeoForge 26.1 port of RPG Gods by skyjay1 (GPLv3).
 */
@Mod(RPGGods.MODID)
public class RPGGods {
    public static final String MODID = "diaithir_gods";

    private static final ModConfigSpec.Builder CONFIG_BUILDER = new ModConfigSpec.Builder();
    public static RGConfig CONFIG = new RGConfig(CONFIG_BUILDER);
    private static final ModConfigSpec CONFIG_SPEC = CONFIG_BUILDER.build();

    // Map of Deity ID to DeityHelper
    public static final Map<Identifier, DeityHelper> DEITY_HELPER = new HashMap<>();
    // Map of Entity ID to Perk IDs of perks that affect affinity
    public static final Map<Identifier, Map<Affinity.Type, List<Identifier>>> AFFINITY = new HashMap<>();

    // Reloadable data resource listeners
    protected static final CodecJsonDataManager<Altar> ALTAR_JSON_MANAGER = new CodecJsonDataManager<>("deity/altar", Altar.CODEC, RGData::updateAltars);
    public static final Map<Identifier, Altar> ALTAR_MAP = new HashMap<>();

    protected static final CodecJsonDataManager<Deity> DEITY_JSON_MANAGER = new CodecJsonDataManager<>("deity/deity", Deity.CODEC, RGData::updateDeities);
    public static final Map<Identifier, Deity> DEITY_MAP = new HashMap<>();

    protected static final CodecJsonDataManager<Offering> OFFERING_JSON_MANAGER = new CodecJsonDataManager<>("deity/offering", Offering.CODEC, RGData::updateOfferings);
    public static final Map<Identifier, Offering> OFFERING_MAP = new HashMap<>();

    protected static final CodecJsonDataManager<Perk> PERK_JSON_MANAGER = new CodecJsonDataManager<>("deity/perk", Perk.CODEC, RGData::updatePerks);
    public static final Map<Identifier, Perk> PERK_MAP = new HashMap<>();

    protected static final CodecJsonDataManager<Sacrifice> SACRIFICE_JSON_MANAGER = new CodecJsonDataManager<>("deity/sacrifice", Sacrifice.CODEC, RGData::updateSacrifices);
    public static final Map<Identifier, Sacrifice> SACRIFICE_MAP = new HashMap<>();

    public static final Logger LOGGER = LoggerFactory.getLogger(RPGGods.MODID);

    public RPGGods(final IEventBus modBus, final ModContainer container) {
        // Deferred registers
        RGRegistry.register(modBus);
        // Mod event bus listeners
        modBus.register(RGEvents.ModEvents.class);
        modBus.addListener(RGNetwork::registerPayloads);
        // Config file
        container.registerConfig(ModConfig.Type.COMMON, CONFIG_SPEC);
        modBus.addListener((ModConfigEvent.Loading event) -> CONFIG.bake());
        modBus.addListener((ModConfigEvent.Reloading event) -> CONFIG.bake());
        // Required for data pack sync
        NeoForge.EVENT_BUS.register(RGData.class);
        // Events that affect Favor and Perks
        NeoForge.EVENT_BUS.register(RGEvents.ForgeEvents.class);
        // Client-only
        if (FMLEnvironment.getDist() == Dist.CLIENT) {
            rpggods.client.ClientSetup.init(modBus);
        }
    }

    /**
     * Determines the favor to use for the given entity
     * @param entity the entity, or null for global favor
     * @return an optional containing the favor, or empty
     */
    public static Optional<IFavor> getFavor(@Nullable final Entity entity) {
        if (CONFIG.useGlobalFavor()) {
            return Optional.ofNullable(ServerLifecycleHooks.getCurrentServer()).map(s -> RGSavedData.get(s).getFavor());
        }
        if (CONFIG.useTeamFavor() && entity != null && entity.getTeam() != null) {
            final String team = entity.getTeam().getName();
            return Optional.ofNullable(ServerLifecycleHooks.getCurrentServer()).map(s -> RGSavedData.get(s).getTeamFavor(team));
        }
        if (entity instanceof net.minecraft.world.entity.player.Player) {
            return Optional.of(entity.getData(RGRegistry.FAVOR.get()));
        }
        return Optional.empty();
    }

    /**
     * @param entity the entity
     * @return the tameable data if the entity is a mob that can be tamed with affinity perks, otherwise empty
     */
    public static Optional<ITameable> getTameable(@Nullable final Entity entity) {
        if (entity instanceof Mob && !(entity instanceof TamableAnimal) && !(entity instanceof AbstractHorse)) {
            return Optional.of(entity.getData(RGRegistry.TAMEABLE.get()));
        }
        return Optional.empty();
    }
}
