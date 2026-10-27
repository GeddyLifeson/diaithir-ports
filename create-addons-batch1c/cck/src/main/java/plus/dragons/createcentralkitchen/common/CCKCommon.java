package plus.dragons.createcentralkitchen.common;

import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import plus.dragons.createcentralkitchen.integration.farmersdelight.registry.FDArmInteractionPointTypes;

public final class CCKCommon {
    public static final String ID = "create_central_kitchen";
    public static final String NAME = "Create: Central Kitchen";
    public static final Logger LOGGER = LoggerFactory.getLogger(NAME);

    private CCKCommon() {}

    /** Runs just before vanilla registries freeze, after Create's own registration. */
    public static void register() {
        FDArmInteractionPointTypes.register();
    }

    public static Identifier asResource(String path) {
        return Identifier.fromNamespaceAndPath(ID, path);
    }
}
