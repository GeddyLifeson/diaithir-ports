package net.diaithir.tooltips;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

/** Puts the item's Diaithiris name under its normal name. The table is data, not a lang file, so the line is the same in every game language. */
@Mod(value = "diaithirtooltips", dist = Dist.CLIENT)
public final class DiaithirTooltips {
    private static final Map<String, String> NAMES = load();

    public DiaithirTooltips() {
        NeoForge.EVENT_BUS.addListener(ItemTooltipEvent.class, DiaithirTooltips::onTooltip);
    }

    private static Map<String, String> load() {
        try (var in = DiaithirTooltips.class.getResourceAsStream("/assets/diaithirtooltips/diaithiris_names.json")) {
            return new Gson().fromJson(new InputStreamReader(in, StandardCharsets.UTF_8), new TypeToken<Map<String, String>>() {}.getType());
        } catch (Exception e) {
            return Map.of();
        }
    }

    private static void onTooltip(ItemTooltipEvent event) {
        var lines = event.getToolTip();
        if (lines.isEmpty()) {
            return;
        }
        String name = NAMES.get(BuiltInRegistries.ITEM.getKey(event.getItemStack().getItem()).toString());
        if (name != null) {
            lines.add(1, Component.literal(name).withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        }
    }
}
