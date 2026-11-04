package com.lowdragmc.lowdraglib.gui.editor.runtime;

import com.google.common.base.Strings;
import com.lowdragmc.lowdraglib.gui.editor.annotation.ConfigSetter;
import com.lowdragmc.lowdraglib.gui.editor.annotation.Configurable;
import com.lowdragmc.lowdraglib.syncdata.ManagedFieldUtils;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.utils.ReflectionUtils;
import com.lowdragmc.lowdraglib.utils.TagUtils;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;


import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

/**
 * @author KilaBash
 * @date 2022/12/6
 * @implNote PersistedParser
 */
public class PersistedParser {
    public static void serializeNBT(CompoundTag tag, Class<?> clazz, Object object, HolderLookup.Provider provider) {  /* ported stub */ }

    public static void deserializeNBT(CompoundTag tag, Map<String, Method> setters, Class<?> clazz, Object object, HolderLookup.Provider provider) {  /* ported stub */ }

}
