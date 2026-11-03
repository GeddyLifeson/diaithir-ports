package net.diaithir.druidry.ars.api.registry;

import net.diaithir.druidry.ars.api.familiar.AbstractFamiliarHolder;
import net.diaithir.druidry.ars.common.items.FamiliarScript;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class FamiliarRegistry {
    private static ConcurrentHashMap<ResourceLocation, AbstractFamiliarHolder> familiarHolderMap = new ConcurrentHashMap<>();

    private static ConcurrentHashMap<ResourceLocation, FamiliarScript> familiarScriptMap = new ConcurrentHashMap<>();

    public static AbstractFamiliarHolder registerFamiliar(AbstractFamiliarHolder familiar) {
        return familiarHolderMap.put(familiar.getRegistryName(), familiar);
    }

    public static Map<ResourceLocation, AbstractFamiliarHolder> getFamiliarHolderMap() {
        return familiarHolderMap;
    }

    public static Map<ResourceLocation, FamiliarScript> getFamiliarScriptMap() {
        return familiarScriptMap;
    }
}
