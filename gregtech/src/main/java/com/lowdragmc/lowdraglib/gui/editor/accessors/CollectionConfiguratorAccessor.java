package com.lowdragmc.lowdraglib.gui.editor.accessors;

import com.lowdragmc.lowdraglib.gui.editor.annotation.Configurable;
import com.lowdragmc.lowdraglib.gui.editor.configurator.ArrayConfiguratorGroup;
import com.lowdragmc.lowdraglib.gui.editor.configurator.Configurator;
import lombok.AllArgsConstructor;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @author KilaBash
 * @date 2022/12/2
 * @implNote ArrayConfiguratorAccessor
 */
@AllArgsConstructor
public class CollectionConfiguratorAccessor implements IConfiguratorAccessor<Collection> {
    private final Class<?> baseType;
    private final Class<?> childType;
    private final IConfiguratorAccessor childAccessor;

    @Override
    public boolean test(Class<?> type) {
        return type.isArray();
    }

    @Override
    public Collection defaultValue(Field field, Class<?> type) {
        if (type == List.class) {
            return new ArrayList<>();
        } else if (type == Set.class) {
            return new HashSet<>();
        }
        return new ArrayList<>();
    }

    @Override
    public Configurator create(String name, Supplier<Collection> supplier, Consumer<Collection> consumer, boolean forceUpdate, Field field) { return null; /* ported stub */ }

    public Collection updateCollection(Collection base, List<Object> objectList) {
        base.clear();
        base.addAll(objectList);
        return base;
    }
}
