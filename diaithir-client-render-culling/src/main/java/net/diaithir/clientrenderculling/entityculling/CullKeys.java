package net.diaithir.clientrenderculling.entityculling;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;

final class CullKeys {
    static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(Identifier.fromNamespaceAndPath("diaithir", "main"));
    static final KeyMapping TOGGLE = new KeyMapping("key.diaithir.entityculling.toggle", InputConstants.UNKNOWN.getValue(), CATEGORY);
    static final KeyMapping DEBUG = new KeyMapping("key.diaithir.entityculling.debug", InputConstants.UNKNOWN.getValue(), CATEGORY);

    private CullKeys() {}
}
