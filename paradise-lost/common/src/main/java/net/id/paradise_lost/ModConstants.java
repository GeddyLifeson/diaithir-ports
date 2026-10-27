package net.id.paradise_lost;

import net.minecraft.resources.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class ModConstants {
    public static final String MOD_ID = "paradise_lost";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    private ModConstants() {}

    public static Identifier locate(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }
}
