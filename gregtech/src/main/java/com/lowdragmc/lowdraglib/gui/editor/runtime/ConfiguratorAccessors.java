package com.lowdragmc.lowdraglib.gui.editor.runtime;

import com.lowdragmc.lowdraglib.gui.editor.accessors.ArrayConfiguratorAccessor;
import com.lowdragmc.lowdraglib.gui.editor.accessors.CollectionConfiguratorAccessor;
import com.lowdragmc.lowdraglib.gui.editor.accessors.IConfiguratorAccessor;
import com.lowdragmc.lowdraglib.utils.ReflectionUtils;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author KilaBash
 * @date 2022/12/1
 * @implNote ConfiguratorAccessors
 */
public class ConfiguratorAccessors {
    private static final Map<Class<?>, IConfiguratorAccessor<?>> ACCESSOR_MAP = new ConcurrentHashMap<>();

    public static IConfiguratorAccessor<?> findByType(Type clazz) { return null; /* ported stub */ }

    public static IConfiguratorAccessor<?> findByClass(Class<?> clazz) {
        return ACCESSOR_MAP.computeIfAbsent(clazz, c -> {
            for (IConfiguratorAccessor<?> accessor : AnnotationDetector.CONFIGURATOR_ACCESSORS) {
                if (accessor.test(c)) {
                    return accessor;
                }
            }
            return IConfiguratorAccessor.DEFAULT;
        });
    }

}
