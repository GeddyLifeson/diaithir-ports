// Ported from Chat Heads (MPL-2.0) https://github.com/dzwdz/chat_heads
// Modifications: Cloth Config/AutoConfig data class replaced by a NeoForge ModConfigSpec (aliases stored as "nick -> profile" strings).
package net.diaithir.chatclient.chatheads.config;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.neoforged.neoforge.common.ModConfigSpec;

public final class ChatHeadsConfig {
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.EnumValue<RenderPosition> RENDER_POSITION;
    public static final ModConfigSpec.EnumValue<SenderDetection> SENDER_DETECTION;
    public static final ModConfigSpec.BooleanValue SMART_HEURISTICS;
    public static final ModConfigSpec.BooleanValue HANDLE_SYSTEM_MESSAGES;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> NAME_ALIASES;
    public static final ModConfigSpec.BooleanValue DETECT_ALIASES;

    static {
        ModConfigSpec.Builder b = new ModConfigSpec.Builder();
        RENDER_POSITION = b.comment("Where the head is placed: BEFORE_LINE puts it at the start of the chat line, BEFORE_NAME right before the sender's name.")
                .defineEnum("renderPosition", RenderPosition.BEFORE_NAME);
        SENDER_DETECTION = b.comment("UUID_ONLY: only trust the sender UUID sent by the server.",
                        "UUID_AND_HEURISTIC: use the UUID when available, otherwise guess from the message content.",
                        "HEURISTIC_ONLY: always guess from the message content (for servers sending fake UUIDs).")
                .defineEnum("senderDetection", SenderDetection.UUID_AND_HEURISTIC);
        SMART_HEURISTICS = b.comment("Turn off heuristics once the server is known to send UUIDs (applies to UUID_AND_HEURISTIC).")
                .define("smartHeuristics", true);
        HANDLE_SYSTEM_MESSAGES = b.comment("Whether heads can be added to pure server (system) messages. Many servers turn player messages into system messages.")
                .define("handleSystemMessages", true);
        NAME_ALIASES = b.comment("Nickname aliases, one per entry, formatted as: nickname -> profilename")
                .defineListAllowEmpty("nameAliases", List.of(), () -> "nickname -> profilename",
                        o -> o instanceof String s && s.contains("->"));
        DETECT_ALIASES = b.comment("Look for responses to /realname on EssentialsX servers and add an alias automatically.")
                .define("detectNameAliases", true);
        SPEC = b.build();
    }

    private ChatHeadsConfig() {}

    /** nickname -> profile name */
    public static Map<String, String> nameAliases() {
        Map<String, String> out = new LinkedHashMap<>();
        if (!SPEC.isLoaded()) {
            return out;
        }
        for (String s : NAME_ALIASES.get()) {
            String[] parts = s.split("\\s*->\\s*", 2);
            if (parts.length != 2) {
                continue;
            }
            String nick = parts[0].strip();
            String profile = parts[1].strip();
            if (nick.isEmpty() || profile.isEmpty()) {
                continue;
            }
            out.put(nick, profile);
        }
        return out;
    }

    public static void addNameAlias(String nickname, String profileName) {
        if (!SPEC.isLoaded()) {
            return;
        }
        Map<String, String> aliases = nameAliases();
        if (profileName.equals(aliases.get(nickname))) {
            return;
        }
        aliases.put(nickname, profileName);
        List<String> list = new ArrayList<>();
        aliases.forEach((k, v) -> list.add(k + " -> " + v));
        NAME_ALIASES.set(list);
        SPEC.save();
    }
}
