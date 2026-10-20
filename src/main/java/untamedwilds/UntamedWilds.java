package untamedwilds;

import net.minecraft.world.level.block.DispenserBlock;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import untamedwilds.block.CageBlock;
import untamedwilds.compat.CompatBridge;
import untamedwilds.config.ConfigBase;
import untamedwilds.init.ModAdvancementTriggers;
import untamedwilds.init.ModBlock;
import untamedwilds.init.ModEntity;
import untamedwilds.init.ModItems;
import untamedwilds.init.ModParticles;
import untamedwilds.init.ModSounds;
import untamedwilds.network.UntamedInstance;
import untamedwilds.util.ModCreativeModeTab;
import untamedwilds.world.UntamedWildsBiomeModifier;
import untamedwilds.world.UntamedWildsGenerator;

@Mod(UntamedWilds.MOD_ID)
public class UntamedWilds {

    // TODO: Abstract Herd logic to be functional with any LivingEntity (instead of being limited to IPackEntity ComplexMob)
    // TODO: Store the children's UUID in their mother's NBT, to allow checking for Children without constant AABB checking
    // TODO: Have carnivorous mobs gain hunger when attacking
    // TODO: Move Variants from Int to String to prevent shuffling?
    // TODO: Gillie suit bauble, to prevent animals from spotting you

    public static final Logger LOGGER = LogManager.getLogger();
    public static final String MOD_ID = "untamedwilds";
    public static final boolean DEBUG = false;

    public UntamedWilds(IEventBus modBus, ModContainer modContainer) {
        // Register all deferred registers to the mod event bus
        ModBlock.BLOCKS.register(modBus);
        ModBlock.TILE_ENTITIES.register(modBus);
        ModItems.ITEMS.register(modBus);
        ModEntity.ENTITIES.register(modBus);
        ModSounds.SOUNDS.register(modBus);
        ModParticles.PARTICLES.register(modBus);
        UntamedWildsGenerator.FEATURES.register(modBus);
        UntamedWildsGenerator.TREE_DECORATION.register(modBus);
        UntamedWildsGenerator.CONFIGURED_FEATURES.register(modBus);
        UntamedWildsGenerator.PLACED_FEATURES.register(modBus);
        UntamedWildsBiomeModifier.BIOME_MODIFIER_SERIALIZERS.register(modBus);
        ModCreativeModeTab.CREATIVE_MODE_TABS.register(modBus);
        ModAdvancementTriggers.TRIGGERS.register(modBus);

        modContainer.registerConfig(ModConfig.Type.COMMON, ConfigBase.common_config);

        // Register lifecycle event listeners
        modBus.addListener(this::setupCommon);
        modBus.addListener(this::setupClient);
        modBus.addListener(UntamedInstance::registerMessages);
        if (FMLEnvironment.getDist().isClient()) {
            modBus.addListener(this::registerEntityRenderers);
        }

        // Register spawn items
        ModItems.registerSpawnItems();

        // Register compatibility
        CompatBridge.RegisterCompat();
    }

    private void setupCommon(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            DispenserBlock.registerBehavior(
                ModBlock.TRAP_CAGE.get().asItem(),
                new CageBlock.DispenserBehaviorTrapCage()
            );
        });
    }

    private void setupClient(final FMLClientSetupEvent event) {
        //ModBlock.registerBlockColors();
        //ModParticles.setupParticles();

        //ModBlock.registerBlockColors();
        //ModParticles.registerParticles(); Handled through events
    }

    private void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        untamedwilds.client.ClientProxy.registerEntityRenderers(event);
    }
}
