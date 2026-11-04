package net.diaithir.druidry.ars.api.registry;

import net.diaithir.druidry.ars.api.familiar.AbstractFamiliarHolder;
import net.diaithir.druidry.ars.common.items.FamiliarScript;
import net.minecraft.resources.Identifier;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class FamiliarRegistry {
    private static ConcurrentHashMap<Identifier, AbstractFamiliarHolder> familiarHolderMap = new ConcurrentHashMap<>();

    private static ConcurrentHashMap<Identifier, FamiliarScript> familiarScriptMap = new ConcurrentHashMap<>();

    public static AbstractFamiliarHolder registerFamiliar(AbstractFamiliarHolder familiar) {
        return familiarHolderMap.put(familiar.getRegistryName(), familiar);
    }

    public static Map<Identifier, AbstractFamiliarHolder> getFamiliarHolderMap() {
        return familiarHolderMap;
    }

    public static Map<Identifier, FamiliarScript> getFamiliarScriptMap() {
        return familiarScriptMap;
    }
}
