package com.iafenvoy.jupiter.render.screen;

import com.iafenvoy.jupiter.JupiterProxies;
import com.iafenvoy.jupiter.config.entry.EntryBaseEntry;
import com.iafenvoy.jupiter.config.entry.ListBaseEntry;
import com.iafenvoy.jupiter.config.entry.MapBaseEntry;
import com.iafenvoy.jupiter.config.interfaces.ConfigEntry;
import com.iafenvoy.jupiter.config.interfaces.ConfigMetaProvider;
import com.iafenvoy.jupiter.config.type.ConfigType;
import com.iafenvoy.jupiter.config.type.ConfigTypes;
import com.iafenvoy.jupiter.render.widget.WidgetBuilder;
import com.iafenvoy.jupiter.render.widget.builder.*;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.network.chat.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

public class WidgetBuilderManager {
    private static final Map<ConfigType<?>, BiFunction<ConfigMetaProvider, ConfigEntry<?>, WidgetBuilder<?>>> BUILDERS = new HashMap<>();

    @SuppressWarnings("unchecked")
    public static <T> void register(ConfigType<T> type, BiFunction<ConfigMetaProvider, ConfigEntry<T>, WidgetBuilder<T>> builder) {
        BUILDERS.put(type, (BiFunction<ConfigMetaProvider, ConfigEntry<?>, WidgetBuilder<?>>) (Object) builder);
    }

    @SuppressWarnings("unchecked")
    public static <T> WidgetBuilder<T> get(ConfigMetaProvider provider, ConfigEntry<T> entry) {
        return (WidgetBuilder<T>) BUILDERS.getOrDefault(entry.getType(), Fallback::new).apply(provider, entry);
    }

    static {
        register(ConfigTypes.SEPARATOR, SeparatorWidgetBuilder::new);
        register(ConfigTypes.CONFIG_GROUP, ConfigGroupWidgetBuilder::new);
        register(ConfigTypes.BOOLEAN, (provider, config) -> new ButtonWidgetBuilder<>(provider, config, button -> config.setValue(!config.getValue()), () -> Component.literal(config.getValue() ? "§atrue" : "§cfalse")));
        register(ConfigTypes.INTEGER, TextFieldWidgetBuilder::new);
        register(ConfigTypes.LONG, TextFieldWidgetBuilder::new);
        register(ConfigTypes.DOUBLE, TextFieldWidgetBuilder::new);
        register(ConfigTypes.FLOAT, TextFieldWidgetBuilder::new);
        register(ConfigTypes.STRING, TextFieldWidgetBuilder::new);
        //noinspection rawtypes,unchecked
        register(ConfigTypes.ENUM, (provider, config) -> new EnumWidgetBuilder<>(provider, (ConfigEntry) config));
        register(ConfigTypes.LIST_BOOLEAN, (provider, config) -> new ListWidgetBuilder<>(provider, (ListBaseEntry<Boolean>) config));
        register(ConfigTypes.LIST_INTEGER, (provider, config) -> new ListWidgetBuilder<>(provider, (ListBaseEntry<Integer>) config));
        register(ConfigTypes.LIST_LONG, (provider, config) -> new ListWidgetBuilder<>(provider, (ListBaseEntry<Long>) config));
        register(ConfigTypes.LIST_DOUBLE, (provider, config) -> new ListWidgetBuilder<>(provider, (ListBaseEntry<Double>) config));
        register(ConfigTypes.LIST_STRING, (provider, config) -> new ListWidgetBuilder<>(provider, (ListBaseEntry<String>) config));
        register(ConfigTypes.LIST_ENUM, (provider, config) -> new ListWidgetBuilder<>(provider, (ListBaseEntry<Enum<?>>) config));
        register(ConfigTypes.MAP_STRING, (provider, config) -> new MapWidgetBuilder<>(provider, (MapBaseEntry<String>) config));
        register(ConfigTypes.MAP_INTEGER, (provider, config) -> new MapWidgetBuilder<>(provider, (MapBaseEntry<Integer>) config));
        register(ConfigTypes.MAP_DOUBLE, (provider, config) -> new MapWidgetBuilder<>(provider, (MapBaseEntry<Double>) config));
        register(ConfigTypes.ENTRY_STRING, (provider, config) -> new EntryWidgetBuilder<>(provider, (EntryBaseEntry<String>) config));
        register(ConfigTypes.ENTRY_INTEGER, (provider, config) -> new EntryWidgetBuilder<>(provider, (EntryBaseEntry<Integer>) config));
        register(ConfigTypes.ENTRY_DOUBLE, (provider, config) -> new EntryWidgetBuilder<>(provider, (EntryBaseEntry<Double>) config));
        register(ConfigTypes.RESOURCE_LOCATION, TextFieldWidgetBuilder::new);
    }

    //When a widget not found, it should navigate to this one
    private static class Fallback<T> extends WidgetBuilder<T> {
        public Fallback(ConfigMetaProvider configMetaProvider, ConfigEntry<T> config) {
            super(configMetaProvider, config);
        }

        @Override
        public void addElements(Context context, int x, int y, int width, int height) {
            Font textRenderer = this.minecraft.font;
            Component text = Component.translatable("jupiter.screen.unregistered_widget", this.config.getClass().getSimpleName(), this.provider.getSource().jupiterCapability() ? "Jupiter" : JupiterProxies.PLATFORM.resolveModName(this.provider.getConfigId().getNamespace()));
            this.textWidget = new StringWidget(20, y, textRenderer.width(text), height, text, textRenderer);
            context.addWidget(this.textWidget);
        }

        @Override
        public void addCustomElements(Context context, int x, int y, int width, int height) {
            //No Need
        }

        @Override
        public void updateCustom(boolean visible, int y) {
            //No Need
        }

        @Override
        public void refresh() {
            //No Need
        }
    }
}
