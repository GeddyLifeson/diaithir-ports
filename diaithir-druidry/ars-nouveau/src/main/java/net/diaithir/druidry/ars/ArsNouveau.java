package net.diaithir.druidry.ars;

import net.diaithir.druidry.ars.api.perk.PerkAttributes;
import net.diaithir.druidry.ars.common.advancement.ANCriteriaTriggers;
import net.diaithir.druidry.ars.common.network.Networking;
import net.diaithir.druidry.ars.setup.config.Config;
import net.diaithir.druidry.ars.setup.config.ServerConfig;
import net.diaithir.druidry.ars.setup.config.StartupConfig;
import net.diaithir.druidry.ars.setup.registry.*;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.registries.RegisterEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.world.chunk.TicketController;

import java.util.ArrayList;
import java.util.List;

/**
 * Entry of the Ars Nouveau code port (LGPL-3.0). It lives inside the diaithir_druidry mod, so registry entries keep the
 * ars_nouveau namespace (MODID) while events and configs belong to the host mod (HOST).
 */
@Mod(ArsNouveau.HOST)
public class ArsNouveau {
    /** Namespace of every registry entry. */
    public static final String MODID = "ars_nouveau";
    /** Mod id of the jar this code ships in. */
    public static final String HOST = "diaithir_druidry";

    public ArsNouveau(IEventBus bus, ModContainer container) {
        ArsGlyphs.register();
        BlockRegistry.onBlocksRegistry();
        BlockRegistry.onBlockItemsRegistry();
        BlockRegistry.BLOCKS.register(bus);
        BlockRegistry.BLOCK_ENTITIES.register(bus);
        BlockRegistry.BS_PROVIDERS.register(bus);
        ItemsRegistry.ITEMS.register(bus);
        ModEntities.ENTITIES.register(bus);
        DataComponentRegistry.DATA.register(bus);
        DataSerializers.DS.register(bus);
        RecipeRegistry.RECIPE_SERIALIZERS.register(bus);
        RecipeRegistry.RECIPE_TYPES.register(bus);
        AttachmentsRegistry.ATTACHMENT_TYPES.register(bus);
        CreativeTabRegistry.TABS.register(bus);
        SoundRegistry.SOUND_REG.register(bus);
        ModPotions.EFFECTS.register(bus);
        ModPotions.POTIONS.register(bus);
        PerkAttributes.ATTRIBUTES.register(bus);
        ANCriteriaTriggers.TRIGGERS.register(bus);

        bus.addListener(RegisterEvent.class, e -> e.register(Registries.ITEM, ItemsRegistry::onItemRegistry));
        bus.addListener(CapabilityRegistry::registerCapabilities);
        bus.addListener(Networking::register);

        container.registerConfig(ModConfig.Type.SERVER, Config.COMMON_CONFIG, HOST + "/ars_common.toml");
        container.registerConfig(ModConfig.Type.SERVER, ServerConfig.SERVER_CONFIG, HOST + "/ars_server.toml");
        container.registerConfig(ModConfig.Type.STARTUP, StartupConfig.STARTUP_CONFIG, HOST + "/ars_startup.toml");

        if (Boolean.getBoolean("ars.selftest")) {
            NeoForge.EVENT_BUS.addListener(ServerStartedEvent.class, ArsSelfTest::run);
        }
    }

    public static final TicketController ticketController = new TicketController(prefix("chunk_ticket"));

    public static final List<String> postLoadWarnings = new ArrayList<>();

    /** Replaced by the client setup on the physical client. */
    public static IProxy proxy = new IProxy() {
    };

    public static Identifier prefix(String path) {
        return Identifier.fromNamespaceAndPath(MODID, path);
    }

    public interface IProxy {
        default boolean isClientSide() {
            return false;
        }

        default Player getPlayer() {
            return null;
        }

        default Level getClientWorld() {
            return null;
        }

        default Minecraft getMinecraft() {
            return null;
        }
    }
}
