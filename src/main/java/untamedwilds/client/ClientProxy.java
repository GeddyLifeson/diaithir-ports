package untamedwilds.client;

import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import untamedwilds.client.render.*;
import untamedwilds.init.ModEntity;

public class ClientProxy {

    private static final Logger LOGGER = LogManager.getLogger();

    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        LOGGER.info("UntamedWilds renderer registration event fired");

        event.registerEntityRenderer(ModEntity.TARANTULA.get(), RendererTarantula::new);
        event.registerEntityRenderer(ModEntity.KING_CRAB.get(), RendererKingCrab::new);

        event.registerEntityRenderer(ModEntity.SOFTSHELL_TURTLE.get(), RendererSoftshellTurtle::new);
        event.registerEntityRenderer(ModEntity.SNAKE.get(), RendererSnake::new);
        event.registerEntityRenderer(ModEntity.TORTOISE.get(), RendererTortoise::new);
        event.registerEntityRenderer(ModEntity.ANACONDA.get(), RendererAnaconda::new);
        event.registerEntityRenderer(ModEntity.MONITOR.get(), RendererMonitor::new);

        event.registerEntityRenderer(ModEntity.GIANT_CLAM.get(), RendererGiantClam::new);

        event.registerEntityRenderer(ModEntity.BEAR.get(), RendererBear::new);
        event.registerEntityRenderer(ModEntity.BIG_CAT.get(), RendererBigCat::new);
        event.registerEntityRenderer(ModEntity.HIPPO.get(), RendererHippo::new);
        event.registerEntityRenderer(ModEntity.AARDVARK.get(), RendererAardvark::new);
        event.registerEntityRenderer(ModEntity.RHINO.get(), RendererRhino::new);
        event.registerEntityRenderer(ModEntity.HYENA.get(), RendererHyena::new);
        event.registerEntityRenderer(ModEntity.BOAR.get(), RendererBoar::new);
        event.registerEntityRenderer(ModEntity.BISON.get(), RendererBison::new);
        event.registerEntityRenderer(ModEntity.CAMEL.get(), RendererCamel::new);
        event.registerEntityRenderer(ModEntity.ELEPHANTID.get(), RendererElephantid::new);
        event.registerEntityRenderer(ModEntity.CERVID.get(), RendererCervid::new);
        event.registerEntityRenderer(ModEntity.MANATEE.get(), RendererManatee::new);
        event.registerEntityRenderer(ModEntity.BALEEN_WHALE.get(), RendererBaleenWhale::new);
        event.registerEntityRenderer(ModEntity.OPOSSUM.get(), RendererOpossum::new);

        event.registerEntityRenderer(ModEntity.SUNFISH.get(), RendererSunfish::new);
        event.registerEntityRenderer(ModEntity.TREVALLY.get(), RendererTrevally::new);
        event.registerEntityRenderer(ModEntity.AROWANA.get(), RendererArowana::new);
        event.registerEntityRenderer(ModEntity.SHARK.get(), RendererShark::new);
        event.registerEntityRenderer(ModEntity.FOOTBALL_FISH.get(), RendererFootballFish::new);
        event.registerEntityRenderer(ModEntity.WHALE_SHARK.get(), RendererWhaleShark::new);
        event.registerEntityRenderer(ModEntity.TRIGGERFISH.get(), RendererTriggerfish::new);
        event.registerEntityRenderer(ModEntity.CATFISH.get(), RendererCatfish::new);
        event.registerEntityRenderer(ModEntity.SPADEFISH.get(), RendererSpadefish::new);

        event.registerEntityRenderer(ModEntity.GIANT_SALAMANDER.get(), RendererGiantSalamander::new);
        event.registerEntityRenderer(ModEntity.NEWT.get(), RendererNewt::new);

        event.registerEntityRenderer(ModEntity.SPITTER.get(), RendererSpitter::new);

        event.registerEntityRenderer(ModEntity.SPIT.get(), RendererProjectileSpit::new);

        LOGGER.info("UntamedWilds entity renderer registration complete");
    }
}
