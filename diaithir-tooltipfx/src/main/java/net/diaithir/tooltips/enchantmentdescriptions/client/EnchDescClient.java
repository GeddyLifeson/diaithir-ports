// Ported from Enchantment Descriptions (LGPL-2.1-only) https://github.com/Darkhax-Minecraft/Enchantment-Descriptions
// Modifications: mixins replaced by ItemTooltipEvent; enchantment lines are located by matching Enchantment.getFullname and descriptions inserted after them.
package net.diaithir.tooltips.enchantmentdescriptions.client;

import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.diaithir.tooltips.enchantmentdescriptions.config.EnchDescConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.EnchantmentScreen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import org.slf4j.Logger;

public final class EnchDescClient {
    private static final Logger LOG = LogUtils.getLogger();
    private static final String[] KEY_TYPES = {"desc", "description", "info"};
    private static final Set<String> MISSING = ConcurrentHashMap.newKeySet();

    private record Entry(Component line, Holder<Enchantment> enchantment, int level) {}

    private EnchDescClient() {}

    public static void init() {
        NeoForge.EVENT_BUS.addListener(ItemTooltipEvent.class, EnchDescClient::onTooltip);
    }

    private static boolean hasEnchantments(ItemStack stack) {
        ItemEnchantments e = stack.get(DataComponents.ENCHANTMENTS);
        ItemEnchantments s = stack.get(DataComponents.STORED_ENCHANTMENTS);
        return (e != null && !e.isEmpty()) || (s != null && !s.isEmpty());
    }

    private static boolean canDisplay(ItemStack stack) {
        if (!EnchDescConfig.ENABLED.get() || !hasEnchantments(stack)) return false;
        if (EnchDescConfig.ONLY_ON_BOOKS.get() && stack.getItem() != Items.ENCHANTED_BOOK) return false;
        return !EnchDescConfig.ONLY_IN_ENCHANTING_TABLE.get() || Minecraft.getInstance().screen instanceof EnchantmentScreen;
    }

    private static boolean keybindMet() {
        return !EnchDescConfig.REQUIRE_KEYBIND.get() || Minecraft.getInstance().hasShiftDown();
    }

    private static void collect(ItemEnchantments enchantments, List<Entry> out) {
        if (enchantments == null || enchantments.isEmpty()) return;
        for (Object2IntMap.Entry<Holder<Enchantment>> e : enchantments.entrySet()) {
            Holder<Enchantment> holder = e.getKey();
            int level = e.getIntValue();
            out.add(new Entry(Enchantment.getFullname(holder, level), holder, level));
        }
    }

    private static Style buildStyle() {
        Style style = Style.EMPTY;
        String color = EnchDescConfig.STYLE_COLOR.get().trim();
        if (color.startsWith("#")) {
            try {
                style = style.withColor(Integer.parseInt(color.substring(1), 16) & 0xFFFFFF);
            } catch (NumberFormatException ignored) {
                // invalid color, leave uncolored
            }
        } else if (!color.isEmpty()) {
            ChatFormatting fmt = ChatFormatting.getByName(color);
            if (fmt != null && fmt.isColor()) {
                style = style.withColor(fmt);
            }
        }
        return style
                .withBold(EnchDescConfig.STYLE_BOLD.get() ? Boolean.TRUE : null)
                .withItalic(EnchDescConfig.STYLE_ITALIC.get() ? Boolean.TRUE : null)
                .withUnderlined(EnchDescConfig.STYLE_UNDERLINED.get() ? Boolean.TRUE : null)
                .withStrikethrough(EnchDescConfig.STYLE_STRIKETHROUGH.get() ? Boolean.TRUE : null)
                .withObfuscated(EnchDescConfig.STYLE_OBFUSCATED.get() ? Boolean.TRUE : null);
    }

    private static void onTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if (!canDisplay(stack)) return;
        List<Component> tooltip = event.getToolTip();

        if (!keybindMet()) {
            if (EnchDescConfig.DISPLAY_ACTIVATE_TEXT.get()) {
                String key = EnchDescConfig.ACTIVATE_TEXT.get();
                tooltip.add(Component.translatableWithFallback(key, key).withStyle(ChatFormatting.DARK_GRAY));
            }
            return;
        }

        List<Entry> entries = new ArrayList<>();
        collect(stack.get(DataComponents.ENCHANTMENTS), entries);
        collect(stack.get(DataComponents.STORED_ENCHANTMENTS), entries);
        if (entries.isEmpty()) return;

        Style style = buildStyle();
        Component prefix = Component.literal(EnchDescConfig.PREFIX.get());
        Component suffix = Component.literal(EnchDescConfig.SUFFIX.get());

        List<Component> result = new ArrayList<>(tooltip.size() + entries.size());
        for (Component line : tooltip) {
            result.add(line);
            for (int i = 0; i < entries.size(); i++) {
                Entry entry = entries.get(i);
                if (entry.line().equals(line)) {
                    entries.remove(i);
                    MutableComponent description = getDescription(entry.enchantment(), entry.level());
                    if (description != null) {
                        description.withStyle(style);
                        result.add(prefix.copy().append(description).append(suffix));
                    }
                    break;
                }
            }
        }
        tooltip.clear();
        tooltip.addAll(result);
    }

    private static MutableComponent getDescription(Holder<Enchantment> enchantment, int level) {
        Identifier id = enchantment.unwrapKey().map(k -> k.identifier()).orElse(null);
        MutableComponent description = null;
        if (id != null) {
            description = lookup("enchantment." + id.getNamespace() + "." + id.getPath() + ".", level);
        }
        if (description == null && enchantment.value().description().getContents() instanceof TranslatableContents translatable) {
            description = lookup(translatable.getKey() + ".", level);
        }
        return description;
    }

    private static MutableComponent lookup(String baseKey, int level) {
        for (String keyType : KEY_TYPES) {
            String key = baseKey + keyType;
            if (I18n.exists(key)) {
                return Component.translatable(key);
            }
            key = key + "." + level;
            if (I18n.exists(key)) {
                return Component.translatable(key);
            }
        }
        if (MISSING.add(baseKey)) {
            LOG.warn("Enchantment {} does not have a description!", baseKey);
        }
        return null;
    }
}
