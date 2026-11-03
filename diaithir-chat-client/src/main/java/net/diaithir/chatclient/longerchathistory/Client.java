package net.diaithir.chatclient.longerchathistory;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.multiplayer.chat.GuiMessage;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.client.event.ClientChatReceivedEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

final class Client {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int VANILLA_LINES = 100;

    private static final ModConfigSpec SPEC;
    private static final ModConfigSpec.BooleanValue ENABLED;
    private static final ModConfigSpec.IntValue CHAT_LINES;
    private static final ModConfigSpec.IntValue SENT_LINES;
    private static final ModConfigSpec.BooleanValue PERSIST;

    static {
        ModConfigSpec.Builder b = new ModConfigSpec.Builder();
        ENABLED = b.comment("Master switch. When false, vanilla caps are used.")
                .define("enabled", true);
        CHAT_LINES = b.comment("Max stored chat lines. Applies to wrapped lines, so narrow windows store fewer messages.")
                .defineInRange("chatHistoryLines", 1000, 100, 10000);
        SENT_LINES = b.comment("Max recalled sent messages (Up/Down arrow history).")
                .defineInRange("sentHistoryLines", 200, 10, 2000);
        PERSIST = b.comment("Keep chat lines when disconnecting.")
                .define("persistAcrossWorlds", false);
        SPEC = b.build();
    }

    private static final List<GuiMessage> persisted = new ArrayList<>();
    private static final List<String> persistedSent = new ArrayList<>();
    private static boolean warned;

    private static Field allMessagesField;
    private static Field trimmedMessagesField;
    private static Field recentChatField;

    static void init(IEventBus modBus, ModContainer container) {
        container.registerConfig(ModConfig.Type.CLIENT, SPEC, "diaithir-chat-client.toml");
        modBus.addListener(Client::onConfig);
        NeoForge.EVENT_BUS.addListener(Client::onLogout);
        NeoForge.EVENT_BUS.addListener(Client::onLogin);
        NeoForge.EVENT_BUS.addListener(Client::onChat);
    }

    private static void onConfig(ModConfigEvent event) {
        if (event.getConfig().getSpec() != SPEC) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc != null) mc.execute(Client::trim);
    }

    private static void onChat(ClientChatReceivedEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc != null) mc.execute(Client::trim);
    }

    private static int chatCap() {
        return ENABLED.get() ? CHAT_LINES.get() : VANILLA_LINES;
    }

    private static int sentCap() {
        return ENABLED.get() ? SENT_LINES.get() : VANILLA_LINES;
    }

    @SuppressWarnings("unchecked")
    private static List<Object> list(ChatComponent chat, int index) {
        try {
            if (allMessagesField == null) {
                List<Field> fields = new ArrayList<>();
                for (Field f : ChatComponent.class.getDeclaredFields()) {
                    if (List.class.isAssignableFrom(f.getType())) {
                        f.setAccessible(true);
                        fields.add(f);
                    }
                }
                allMessagesField = fields.get(0);
                trimmedMessagesField = fields.get(1);
                recentChatField = fields.size() > 2 ? fields.get(2) : null;
            }
            Field f = index == 0 ? allMessagesField : index == 1 ? trimmedMessagesField : recentChatField;
            return f == null ? null : (List<Object>) f.get(chat);
        } catch (Throwable t) {
            warn(t);
            return null;
        }
    }

    private static void warn(Throwable t) {
        if (!warned) {
            warned = true;
            LOGGER.warn("Could not adjust chat history limits; vanilla behaviour applies", t);
        }
    }

    private static void trim() {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.gui == null) return;
        ChatComponent chat = mc.gui.getChat();
        trimList(list(chat, 0), chatCap());
        trimList(list(chat, 1), chatCap());
        trimList(list(chat, 2), sentCap());
    }

    private static void trimList(List<Object> l, int cap) {
        if (l == null) return;
        while (l.size() > cap) l.remove(l.size() - 1);
    }

    private static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        persisted.clear();
        persistedSent.clear();
        if (!ENABLED.get() || !PERSIST.get()) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.gui == null) return;
        ChatComponent chat = mc.gui.getChat();
        List<Object> all = list(chat, 0);
        if (all != null) for (Object o : all) if (o instanceof GuiMessage m) persisted.add(m);
        List<Object> sent = list(chat, 2);
        if (sent != null) for (Object o : sent) if (o instanceof String s) persistedSent.add(s);
    }

    @SuppressWarnings("unchecked")
    private static void onLogin(ClientPlayerNetworkEvent.LoggingIn event) {
        if (!ENABLED.get() || !PERSIST.get() || (persisted.isEmpty() && persistedSent.isEmpty())) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc == null) return;
        mc.execute(() -> {
            if (mc.gui == null) return;
            ChatComponent chat = mc.gui.getChat();
            List<Object> all = list(chat, 0);
            if (all != null) {
                List<Object> merged = new ArrayList<>(persisted);
                merged.addAll(0, List.of());
                all.addAll(all.isEmpty() ? 0 : all.size(), merged);
            }
            List<Object> sent = list(chat, 2);
            if (sent != null && sent.isEmpty()) sent.addAll(persistedSent);
            persisted.clear();
            persistedSent.clear();
            trim();
        });
    }
}
