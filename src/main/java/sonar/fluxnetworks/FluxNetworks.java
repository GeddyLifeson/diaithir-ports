package sonar.fluxnetworks;

import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sonar.fluxnetworks.api.FluxDataComponents;
import sonar.fluxnetworks.register.DataAttachments;

import javax.annotation.Nonnull;

@Mod(FluxNetworks.MODID)
public class FluxNetworks {

    public static final String MODID = "fluxnetworks";
    public static final String NAME = "Flux Networks";
    public static final String NAME_CPT = "FluxNetworks";

    public static final Logger LOGGER = LogManager.getLogger(NAME_CPT);

    private static boolean sCuriosLoaded;

    public FluxNetworks(IEventBus bus) {
        sCuriosLoaded = ModList.get().isLoaded("curios");

        FluxConfig.init();


        DataAttachments.REGISTER.register(bus);
        FluxDataComponents.REGISTRY.register(bus);
    }

    public static boolean isCuriosLoaded() {
        return sCuriosLoaded;
    }

    @Nonnull
    public static Identifier location(String path) {
        return Identifier.fromNamespaceAndPath(MODID, path);
    }
}
