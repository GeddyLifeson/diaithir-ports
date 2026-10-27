package com.github.alexmodguy.alexscaves.client;

import com.github.alexmodguy.alexscaves.AlexsCaves;
import net.minecraft.resources.Identifier;

/** Client constants. Post-effect shaders are not ported to 26.1, the ids are kept so callers compile. */
public class ClientProxy {
    public static final Identifier HOLOGRAM_SHADER = Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "shaders/post/hologram.json");
    public static final Identifier IRRADIATED_SHADER = Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "shaders/post/irradiated.json");
    public static final Identifier PURPLE_WITCH_SHADER = Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "shaders/post/purple_witch.json");
}
