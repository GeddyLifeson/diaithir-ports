package net.diaithir.tooltips.durabilitytooltip;

import com.mojang.blaze3d.platform.InputConstants;
import java.util.List;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

final class DurabilityClient {
    private static final String PREFIX_KEY = "tooltip.diaithir_tooltips.durability";
    private static KeyMapping toggleKey;

    private DurabilityClient() {}

    static void init(IEventBus modBus, ModContainer container) {
        modBus.addListener(ModConfigEvent.Loading.class, DurabilityConfig::onLoad);
        modBus.addListener(ModConfigEvent.Reloading.class, DurabilityConfig::onLoad);
        modBus.addListener(DurabilityClient::registerKeys);
        NeoForge.EVENT_BUS.addListener(DurabilityClient::onTooltip);
        NeoForge.EVENT_BUS.addListener(DurabilityClient::onTick);
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    private static void registerKeys(RegisterKeyMappingsEvent event) {
        KeyMapping.Category category = new KeyMapping.Category(
                net.minecraft.resources.Identifier.fromNamespaceAndPath("diaithir_tooltips", "main"));
        toggleKey = new KeyMapping("key.diaithir_tooltips.toggle_durability",
                InputConstants.Type.KEYSYM, InputConstants.UNKNOWN.getValue(), category);
        event.registerCategory(category);
        event.register(toggleKey);
    }

    private static void onTick(ClientTickEvent.Post event) {
        if (toggleKey == null) return;
        while (toggleKey.consumeClick()) {
            boolean now = !DurabilityConfig.enabled;
            DurabilityConfig.setEnabled(now);
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null) {
                mc.gui.setOverlayMessage(Component.translatable(
                        now ? "message.diaithir_tooltips.durability.on" : "message.diaithir_tooltips.durability.off"), false);
            }
        }
    }

    private static void onTooltip(ItemTooltipEvent event) {
        if (!DurabilityConfig.enabled) return;
        ItemStack stack = event.getItemStack();
        if (!stack.isDamageableItem()) {
            if (DurabilityConfig.showUnbreakable && stack.has(DataComponents.UNBREAKABLE)
                    && (!DurabilityConfig.onlyAdvanced || event.getFlags().isAdvanced())) {
                add(event.getToolTip(), Component.translatable("tooltip.diaithir_tooltips.unbreakable"));
            }
            return;
        }
        if (DurabilityConfig.onlyAdvanced && !event.getFlags().isAdvanced()) return;
        int max = stack.getMaxDamage();
        if (max <= 0) return;
        int damage = stack.getDamageValue();
        if (DurabilityConfig.hideFull && damage <= 0) return;
        int remaining = Math.max(0, max - damage);
        int percent = (int) Math.round(100.0 * remaining / max);
        if (remaining > 0 && percent < 1) percent = 1;

        List<Component> tip = event.getToolTip();
        String prefix = Component.translatable(PREFIX_KEY, "").getString();
        String head = prefix.contains(":") ? prefix.substring(0, prefix.indexOf(':')) : prefix.strip();
        if (!head.isEmpty()) {
            for (Component line : tip) {
                if (line.getString().startsWith(head)) return;
            }
        }

        MutableComponent value = Component.literal(switch (DurabilityConfig.format) {
            case FULL -> remaining + " / " + max + (DurabilityConfig.showPercent ? " (" + percent + "%)" : "");
            case COMPACT -> remaining + "/" + max + (DurabilityConfig.showPercent ? " (" + percent + "%)" : "");
            case PERCENT_ONLY -> percent + "%";
        });
        value.withColor(DurabilityConfig.color ? color(remaining, max, percent) : 0xAAAAAA);
        add(tip, Component.translatable(PREFIX_KEY, value));
    }

    private static int color(int remaining, int max, int percent) {
        if (percent <= DurabilityConfig.low) return 0xFF5555;
        float f = Mth.clamp((float) remaining / max, 0f, 1f);
        return Mth.hsvToRgb(f / 3f, 1f, 1f) & 0xFFFFFF;
    }

    private static void add(List<Component> tip, Component line) {
        if (DurabilityConfig.position == DurabilityConfig.Position.AFTER_NAME && !tip.isEmpty()) {
            tip.add(1, line);
        } else {
            tip.add(line);
        }
    }
}
