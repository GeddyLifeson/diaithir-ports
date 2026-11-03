package io.github.tr100000.trutils.api.gui;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.jetbrains.annotations.Contract;

import java.util.Map;

public final class IconRenderers {
    private static final Logger LOGGER = LoggerFactory.getLogger("TrUtils");
    private IconRenderers() {}

    private static final BiMap<Icon.IconType<?>, IconRenderer<?>> REGISTRY = HashBiMap.create();

    static {
        register(BlankIcon.TYPE, BlankIconRenderer.INSTANCE);
        register(ItemIcon.TYPE, ItemIconRenderer.INSTANCE);
        register(SpriteIcon.TYPE, SpriteIconRenderer.INSTANCE);
        register(TextureIcon.TYPE, TextureIconRenderer.INSTANCE);

        for (Map.Entry<String, Icon.IconType<?>> entry : IconTypes.REGISTRY.entrySet()) {
            if (!REGISTRY.containsKey(entry.getValue())) {
                LOGGER.warn("Icon type {} doesn't have a renderer!", entry.getKey());
            }
        }
    }

    public static <T extends Icon> void register(Icon.IconType<T> type, IconRenderer<T> renderer) {
        REGISTRY.put(type, renderer);
    }

    @SuppressWarnings("unchecked")
    @Contract(pure = true)
    public static <T extends Icon> IconRenderer<T> fromIcon(T icon) {
        return (IconRenderer<T>)REGISTRY.get(icon.getType());
    }

    public static <T extends Icon> void draw(T icon, GuiGraphicsExtractor graphics, int x, int y) {
        fromIcon(icon).draw(icon, graphics, x, y);
    }

    public static <T extends Icon> void draw(T icon, GuiGraphicsExtractor graphics, int x, int y, float delta) {
        fromIcon(icon).draw(icon, graphics, x, y, delta);
    }

    public static <T extends Icon> void drawWithSize(T icon, GuiGraphicsExtractor graphics, int x, int y, int size) {
        fromIcon(icon).drawWithSize(icon, graphics, x, y, size);
    }

    public static <T extends Icon> void drawWithSize(T icon, GuiGraphicsExtractor graphics, int x, int y, int size, float delta) {
        fromIcon(icon).drawWithSize(icon, graphics, x, y, size, delta);
    }
}
