package io.github.tr100000.trutils.api.utils;

import net.minecraft.resources.Identifier;
import net.neoforged.fml.ModList;

/** Derived from TrUtils (MIT, Tr100000). */
public final class GameUtils {
    private GameUtils() {}

    public static String getModName(Identifier id) {
        return getModName(id.getNamespace());
    }

    public static String getModName(String modid) {
        if (modid.equals("c")) {
            return "Common";
        }
        return ModList.get().getModContainerById(modid).map(mod -> mod.getModInfo().getDisplayName()).orElse(modid);
    }
}
