package net.diaithir.crashtools.simplebackups;

import net.diaithir.crashtools.simplebackups.client.ClientInit;
import net.diaithir.crashtools.simplebackups.config.CommonConfig;
import net.diaithir.crashtools.simplebackups.config.ServerConfig;
import net.diaithir.crashtools.simplebackups.network.Pause;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(SimpleBackups.MODID)
public class SimpleBackups {

    public static final Logger LOGGER = LoggerFactory.getLogger(SimpleBackups.class);
    public static final String MODID = "simplebackups";

    public SimpleBackups(IEventBus modEventBus, ModContainer modContainer, Dist dist) {
        ToolsLoader.init();
        modContainer.registerConfig(ModConfig.Type.COMMON, CommonConfig.CONFIG, SimpleBackups.MODID + "/common.toml");
        modContainer.registerConfig(ModConfig.Type.SERVER, ServerConfig.CONFIG, SimpleBackups.MODID + "/server.toml");
        NeoForge.EVENT_BUS.register(new EventListener());
        modEventBus.addListener(this::setup);
        modEventBus.addListener(this::onRegisterPayloadHandler);

        if (CommonConfig.backupsDisabledByJvmArg()) {
            LOGGER.info("##########################################");
            LOGGER.info("#  Backups are disabled by JVM argument  #");
            LOGGER.info("##########################################");
        }

        if (dist.isClient()) {
            ClientInit.init(modEventBus, modContainer);
        }
    }

    private void onRegisterPayloadHandler(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(SimpleBackups.MODID)
                .versioned("1.0")
                .optional();

        registrar.playToClient(Pause.TYPE, Pause.CODEC, Pause::handle);
    }

    private void setup(FMLCommonSetupEvent event) {
        // NO-OP
    }
}
