package io.github.tr100000.trutils.api.gui;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

public final class IconTypes {
    private IconTypes() {}

    public static final BiMap<String, Icon.IconType<?>> REGISTRY = HashBiMap.create();

    static {
        REGISTRY.put("blank", BlankIcon.TYPE);
        REGISTRY.put("item", ItemIcon.TYPE);
        REGISTRY.put("sprite", SpriteIcon.TYPE);
        REGISTRY.put("texture", TextureIcon.TYPE);
    }
}
