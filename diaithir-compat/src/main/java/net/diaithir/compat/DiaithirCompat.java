package net.diaithir.compat;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.item.crafting.Recipe;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.ModifyRecipeJsonsEvent;
import org.slf4j.Logger;

@Mod("diaithircompat")
public final class DiaithirCompat {
    private static final Logger LOG = LogUtils.getLogger();

    public DiaithirCompat() {
        // HIGHEST: upgrade before Almost Unified and other listeners read the recipe JSON, so they see 26.1 shapes.
        NeoForge.EVENT_BUS.addListener(EventPriority.HIGHEST, DiaithirCompat::upgradeLegacyRecipes);
    }

    // A recipe is only replaced when it fails to decode as shipped and its upgraded copy decodes, so working recipes are never touched.
    private static void upgradeLegacyRecipes(ModifyRecipeJsonsEvent event) {
        RegistryOps<JsonElement> ops = event.getOps();
        int upgraded = 0;
        for (var entry : event.getRecipeJsons().entrySet()) {
            if (!(entry.getValue() instanceof JsonObject json) || decodes(ops, json)) continue;
            JsonObject copy = json.deepCopy();
            if (LegacyRecipeJson.upgrade(copy) && decodes(ops, copy)) {
                entry.setValue(copy);
                upgraded++;
            }
        }
        LOG.info("Upgraded {} recipes from the 1.21.1 JSON format", upgraded);
    }

    // some recipe codecs throw instead of returning an error (an empty ItemStackTemplate, for one)
    private static boolean decodes(RegistryOps<JsonElement> ops, JsonObject json) {
        try {
            return Recipe.CODEC.parse(ops, json).isSuccess();
        } catch (RuntimeException e) {
            return false;
        }
    }
}
