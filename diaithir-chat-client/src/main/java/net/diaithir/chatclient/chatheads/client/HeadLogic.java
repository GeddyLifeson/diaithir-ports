// Ported from Chat Heads (MPL-2.0) https://github.com/dzwdz/chat_heads
// Modifications: sender detection/heuristics of ChatHeads.java without mixin state (chat events provide the message and sender).
package net.diaithir.chatclient.chatheads.client;

import java.util.List;
import java.util.Objects;
import net.diaithir.chatclient.chatheads.config.ChatHeadsConfig;
import net.diaithir.chatclient.chatheads.config.RenderPosition;
import net.diaithir.chatclient.chatheads.config.SenderDetection;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class HeadLogic {
    /** Servers can disable chat heads by shipping a resource with this id in their resource pack. */
    public static final Identifier DISABLE_RESOURCE = Identifier.fromNamespaceAndPath("chat_heads", "disable");

    private static volatile boolean serverSentUuid = false;

    private HeadLogic() {}

    public record Decorated(Component message, PlayerInfo playerInfo) {}

    public static void resetServerKnowledge() {
        serverSentUuid = false;
    }

    public static boolean serverDisabledChatHeads() {
        Minecraft mc = Minecraft.getInstance();
        return mc.getResourceManager().getResource(DISABLE_RESOURCE).isPresent();
    }

    // On EssentialsX, /realname returns "<nickname> is <profile name>", which we can use to auto-add name aliases
    public static void autoDetectAlias(Component message) {
        String text = message.getString();

        int i = text.indexOf(" ");
        if (i == -1) return;

        String nickname = text.substring(0, i);
        if (!text.substring(i).startsWith(" is ")) return;

        String profileName = text.substring(i + " is ".length());
        if (profileName.contains(" ")) return;

        ChatHeadsConfig.addNameAlias(nickname, profileName);
    }

    /** Returns the message to display (with a head if one could be placed). */
    public static Component handleAddedMessage(Component originalMessage, @Nullable PlayerInfo playerInfo) {
        if (ChatHeadsConfig.DETECT_ALIASES.get()) {
            autoDetectAlias(originalMessage);
        }

        if (serverDisabledChatHeads()) {
            return originalMessage;
        }

        boolean forceHeuristic = isShowcaseItemMessage(originalMessage);
        SenderDetection detection = ChatHeadsConfig.SENDER_DETECTION.get();

        if (detection == SenderDetection.HEURISTIC_ONLY || forceHeuristic) {
            playerInfo = null; // don't use any prior knowledge
        } else if (playerInfo != null) {
            serverSentUuid = true;
        } else if (detection == SenderDetection.UUID_ONLY || (serverSentUuid && ChatHeadsConfig.SMART_HEURISTICS.get())) {
            return originalMessage;
        }

        Decorated decorated = detectPlayerAndAddChatHead(originalMessage, playerInfo);
        if (decorated == null) {
            return originalMessage;
        }

        if (ChatHeadsConfig.RENDER_POSITION.get() == RenderPosition.BEFORE_LINE) {
            return ComponentProcessor.prependChatHead(originalMessage, decorated.playerInfo());
        }
        return decorated.message();
    }

    /** Returns null if no head was/should be added. */
    @Nullable
    public static Decorated detectPlayerAndAddChatHead(Component message, @Nullable PlayerInfo givenPlayerInfo) {
        var connection = Minecraft.getInstance().getConnection();
        if (connection == null) {
            return null;
        }

        PlayerInfoCache cache = new PlayerInfoCache(connection);
        if (givenPlayerInfo != null) {
            cache.addProfileName(givenPlayerInfo);
        } else {
            cache.collectProfileNames();
        }

        var split = ComponentProcessor.split(message);

        // (A4) don't place a head if there already is one
        if (ComponentProcessor.containsPlayerSprite(split)) {
            return null;
        }

        PlayerInfo found = ComponentProcessor.addChatHeadForClickTellCommand(split, cache);
        if (found != null) {
            return new Decorated(ComponentProcessor.join(split), found);
        }

        if (givenPlayerInfo != null) {
            cache.add(givenPlayerInfo);
        } else {
            cache.collectAllNames();
        }

        found = ComponentProcessor.addChatHeadForPlayerName(split, cache);
        if (found != null) {
            return new Decorated(ComponentProcessor.join(split), found);
        }

        // fallback: put head at the front
        if (givenPlayerInfo != null) {
            return new Decorated(ComponentProcessor.prependChatHead(message, givenPlayerInfo), givenPlayerInfo);
        }

        return null;
    }

    private static boolean isShowcaseItemMessage(Component message) {
        return message.getContents() instanceof TranslatableContents contents
                && Objects.equals(contents.getKey(), "showcaseitem.misc.shared_item");
    }

    @NotNull
    public static HeadData scanForPlayerName(@NotNull String message, PlayerInfoCache playerInfoCache) {
        // prepare a names lookup to improve worst case runtime of the nested loops
        var namesByFirstCharacter = playerInfoCache.createNamesByFirstCharacterMap();

        boolean insideWord = false;

        int[] messageSeq = message.codePoints().toArray();
        for (int i = 0; i < messageSeq.length; i++) {
            int c = messageSeq[i];

            // don't match words inside words ("tom" shouldn't match "custom")
            if (insideWord && isWordCharacter(c)) {
                continue;
            }

            for (String name : namesByFirstCharacter.getOrDefault(c, List.of())) {
                int[] nameSeq = name.codePoints().toArray();

                if (i + nameSeq.length - 1 >= messageSeq.length) {
                    continue;
                }

                // don't match word ending with more word characters ("tom" shouldn't match "tomato")
                boolean nameEndsAsWord = isWordCharacter(nameSeq[nameSeq.length - 1]);
                boolean nameIsFollowedByWord = i + nameSeq.length < messageSeq.length && isWordCharacter(messageSeq[i + nameSeq.length]);
                if (nameEndsAsWord && nameIsFollowedByWord) {
                    continue;
                }

                if (containsSubsequenceAt(messageSeq, i, nameSeq)) {
                    PlayerInfo info = playerInfoCache.get(name);
                    if (info != null) {
                        return new HeadData(info, i);
                    }
                }
            }

            insideWord = isWordCharacter(c);
        }

        return HeadData.EMPTY;
    }

    private static boolean isWordCharacter(int codePoint) {
        return Character.isLetterOrDigit(codePoint) || codePoint == '_' || Character.getNumericValue(codePoint) != -1;
    }

    private static boolean containsSubsequenceAt(int[] sequence, int startIndex, int[] subsequence) {
        for (int j = 0; j < subsequence.length; j++) {
            if (sequence[startIndex + j] != subsequence[j]) {
                return false;
            }
        }
        return true;
    }
}
