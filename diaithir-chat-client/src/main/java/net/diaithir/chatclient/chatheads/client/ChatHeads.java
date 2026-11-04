// Ported from Chat Heads (MPL-2.0) https://github.com/dzwdz/chat_heads
// Modifications: static state and detection logic only; owner/head data mixin interfaces, blended textures and suggestion heads removed.
package net.diaithir.chatclient.chatheads.client;

import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import java.util.List;
import java.util.regex.Pattern;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ChatComponent.ChatGraphicsAccess;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.multiplayer.chat.GuiMessage;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.Objects;

public final class ChatHeads {
    public static final Pattern FORMAT_REGEX = Pattern.compile("\u00a7.");
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final Identifier DISABLE_RESOURCE = Identifier.fromNamespaceAndPath("chat_heads", "disable");

    public enum RenderPosition { BEFORE_LINE, BEFORE_NAME }

    public enum SenderDetection { UUID_ONLY, HEURISTIC_ONLY, UUID_THEN_HEURISTIC }

    // fixed defaults (config-independent)
    public static final int RIGHT_PADDING = 1;
    public static final float THREE_DEE_NESS = 0f;
    private static final RenderPosition RENDER_POSITION = RenderPosition.BEFORE_NAME;
    private static final SenderDetection SENDER_DETECTION = SenderDetection.UUID_THEN_HEURISTIC;
    private static final boolean SMART_HEURISTICS = true;
    private static final boolean OFFSET_NON_PLAYER_TEXT = true;

    public static volatile boolean serverSentUuid = false;
    public static volatile boolean serverDisabledChatHeads = false;

    // set by the render-target mixins; affects glyph padding, 3Dness and shadow
    public static boolean customHeadRendering;

    // captured during ChatComponent rendering for the "before line" offset
    public static ChatGraphicsAccess chatGraphicsAccess = null;

    private ChatHeads() {}

    public static void resetServerKnowledge() {
        serverSentUuid = false;
        serverDisabledChatHeads = false;
    }

    private static void refreshServerDisabled() {
        boolean disabled = false;
        try {
            disabled = Minecraft.getInstance().getResourceManager().getResource(DISABLE_RESOURCE).isPresent();
        } catch (RuntimeException ignored) {
            // resource manager not ready
        }
        if (disabled && !serverDisabledChatHeads) {
            LOGGER.info("Chat Heads disabled by resource pack / server request");
        }
        serverDisabledChatHeads = disabled;
    }

    // On EssentialsX, /realname returns "<nickname> is <profile name>"; alias auto-detection is not supported here
    public static void autoDetectAlias(Component message) {
        // intentionally empty (no alias config available)
    }

    /** Returns the message that should be displayed (the same instance if nothing changed). */
    public static Component handleAddedMessage(Component originalMessage, @Nullable PlayerInfo playerInfo) {
        refreshServerDisabled();
        if (serverDisabledChatHeads)
            return originalMessage;

        boolean forceHeuristic = isShowcaseItemMessage(originalMessage);
        SenderDetection detection = SENDER_DETECTION;

        if (detection == SenderDetection.HEURISTIC_ONLY || forceHeuristic) {
            // don't use any prior knowledge
            playerInfo = null;
        } else {
            if (playerInfo != null) {
                serverSentUuid = true;
            } else {
                // no PlayerInfo/UUID, message is either not from a player or the server didn't wanna tell
                if (detection == SenderDetection.UUID_ONLY || serverSentUuid && SMART_HEURISTICS) {
                    return originalMessage;
                }
            }
        }

        var messageAndData = detectPlayerAndAddChatHead(originalMessage, playerInfo);
        if (messageAndData == null)
            return originalMessage;

        var decoratedMessage = messageAndData.getFirst();
        var headData = messageAndData.getSecond();

        if (RENDER_POSITION == RenderPosition.BEFORE_LINE) {
            return ComponentProcessor.prependChatHead(originalMessage, headData.playerInfo());
        }
        return decoratedMessage;
    }

    /**
     * Returns <code>message</code> decorated with a chat head and the <code>PlayerInfo</code> used for the head.
     * Returns <code>null</code> if no head was/should be added.
     */
    @Nullable
    public static Pair<Component, HeadData> detectPlayerAndAddChatHead(Component message, @Nullable PlayerInfo givenPlayerInfo) {
        var connection = Minecraft.getInstance().getConnection();
        if (connection == null)
            return null;

        var playerInfoCache = new PlayerInfoCache(connection);

        if (givenPlayerInfo != null) {
            playerInfoCache.addProfileName(givenPlayerInfo);
        } else {
            playerInfoCache.collectProfileNames();
        }

        // the component tree graph is turned into a list of lone components, without siblings
        var split = ComponentProcessor.split(message);

        // (A4) don't place a head if there already is one
        if (ComponentProcessor.containsPlayerSprite(split))
            return null;

        PlayerInfo foundPlayerInfo = ComponentProcessor.addChatHeadForClickTellCommand(split, playerInfoCache);
        if (foundPlayerInfo != null)
            return new Pair<>(ComponentProcessor.join(split), HeadData.of(foundPlayerInfo));

        if (givenPlayerInfo != null) {
            playerInfoCache.add(givenPlayerInfo);
        } else {
            playerInfoCache.collectAllNames();
        }

        foundPlayerInfo = ComponentProcessor.addChatHeadForPlayerName(split, playerInfoCache);
        if (foundPlayerInfo != null)
            return new Pair<>(ComponentProcessor.join(split), HeadData.of(foundPlayerInfo));

        // fallback: put head at the front
        if (givenPlayerInfo != null) {
            return new Pair<>(ComponentProcessor.prependChatHead(message, givenPlayerInfo), HeadData.of(givenPlayerInfo));
        }

        return null;
    }

    private static boolean isShowcaseItemMessage(Component message) {
        return message.getContents() instanceof TranslatableContents contents
                && Objects.equals(contents.getKey(), "showcaseitem.misc.shared_item");
    }

    private static boolean offsetEnabled() {
        return RENDER_POSITION == RenderPosition.BEFORE_LINE
                && OFFSET_NON_PLAYER_TEXT
                && !serverDisabledChatHeads;
    }

    private static boolean startsWithHead(@NotNull GuiMessage.Line line) {
        boolean[] r = new boolean[1];

        line.content().accept((position, style, codepoint) -> {
            if (style.getFont() instanceof FontDescription.PlayerSprite)
                r[0] = true;

            return false;
        });

        return r[0];
    }

    public static int getChatOffset(@NotNull GuiMessage.Line line) {
        return offsetEnabled() && !startsWithHead(line) ? headWidth() : 0;
    }

    public static int getTextWidthDifference() {
        return offsetEnabled() ? headWidth() : 0;
    }

    // pixels the head takes up (including padding)
    public static int headWidth() {
        return 8 + RIGHT_PADDING + Math.round(2 * THREE_DEE_NESS);
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
            if (insideWord && isWordCharacter(c))
                continue;

            for (var name : namesByFirstCharacter.getOrDefault(c, List.of())) {
                int[] nameSeq = name.codePoints().toArray();

                // nothing left to match
                if (i + nameSeq.length - 1 >= messageSeq.length)
                    continue;

                // don't match word ending with more word characters ("tom" shouldn't match "tomato")
                boolean nameEndsAsWord = isWordCharacter(nameSeq[nameSeq.length - 1]);
                boolean nameIsFollowedByWord = i + nameSeq.length < messageSeq.length && isWordCharacter(messageSeq[i + nameSeq.length]);
                if (nameEndsAsWord && nameIsFollowedByWord)
                    continue;

                if (containsSubsequenceAt(messageSeq, i, nameSeq)) {
                    return new HeadData(playerInfoCache.get(name), i);
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
