package net.diaithir.druidry.ars.api.documentation;

import net.minecraft.network.chat.Component;

import java.util.List;

/** Port stub: tooltip splitting without client font access. */
public class DocClientUtils {
    public static List<Component> splitTooltip(Component component, Object context) {
        return List.of(component);
    }
}
