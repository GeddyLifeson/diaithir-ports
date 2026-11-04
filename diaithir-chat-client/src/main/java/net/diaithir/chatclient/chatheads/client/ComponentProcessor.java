// Ported from Chat Heads (MPL-2.0) https://github.com/dzwdz/chat_heads
// Modifications: removed the VisibleInLog mixin interface and the Pair dependency; otherwise same component-tree logic.
package net.diaithir.chatclient.chatheads.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import net.minecraft.ChatFormatting;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.contents.ObjectContents;
import net.minecraft.network.chat.contents.PlainTextContents;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.network.chat.contents.objects.ObjectInfo;
import net.minecraft.network.chat.contents.objects.PlayerSprite;
import net.minecraft.util.StringDecomposer;
import net.minecraft.world.item.component.ResolvableProfile;
import org.jetbrains.annotations.Nullable;

/*
 * Assumptions (same as upstream):
 *  (A1)  a player name can only appear in literals and translatables.
 *  (A2)  a name can span a consecutive sequence of literals.
 *  (A3)  names are never split between a translatable and another component, nor over translatable arguments;
 *        for the default "<%s> %s" template only the first argument is checked.
 *  (A4)  an existing PlayerSprite is probably the sender's head, so no second one is added.
 *  (A5)  only one head is added.
 */
public final class ComponentProcessor {
    private ComponentProcessor() {}

    public interface StyledComponentConsumer {
        void accept(Component component, Style style);
    }

    /** Walk the component-sibling tree in depth first order (render order). */
    public static void walkTree(Component component, Style currentStyle, StyledComponentConsumer consumer) {
        currentStyle = component.getStyle().applyTo(currentStyle);
        consumer.accept(component, currentStyle);
        for (Component sibling : component.getSiblings()) {
            walkTree(sibling, currentStyle, consumer);
        }
    }

    public static void walkTree(Component component, StyledComponentConsumer consumer) {
        walkTree(component, Style.EMPTY, consumer);
    }

    /** Turns the component-sibling tree into a flat list of components without siblings. */
    public static ArrayList<Component> split(Component component) {
        ArrayList<Component> components = new ArrayList<>();

        walkTree(component, (c, cStyle) -> {
            if (c.getContents() instanceof PlainTextContents literal) {
                // preprocess formatting codes
                Style[] prevStyle = new Style[] {cStyle};
                StringBuilder b = new StringBuilder();

                StringDecomposer.iterateFormatted(literal.text(), prevStyle[0], (index, style, cp) -> {
                    if (!style.equals(prevStyle[0])) {
                        if (!b.isEmpty()) {
                            components.add(Component.literal(b.toString()).setStyle(prevStyle[0]));
                        }
                        b.setLength(0);
                        prevStyle[0] = style;
                    }
                    b.appendCodePoint(cp);
                    return true;
                });

                if (!b.isEmpty()) {
                    components.add(Component.literal(b.toString()).setStyle(prevStyle[0]));
                }
            } else {
                components.add(c.plainCopy().setStyle(cStyle));
            }
        });

        return components;
    }

    /** Joins split components back together. */
    public static Component join(List<Component> components) {
        if (components.size() == 1) {
            return components.getFirst();
        }
        Component combined = Component.empty();
        for (Component c : components) {
            combined.getSiblings().add(c);
        }
        return combined;
    }

    public record FoundLiteralSequence(String text, int startIndex, int endIndex) {}

    public record FoundTranslatable(TranslatableContents contents, int index) {}

    public record SplitPair(MutableComponent left, MutableComponent right) {}

    public static void walkLiteralSequencesAndTranslatables(ArrayList<Component> components,
            Predicate<FoundLiteralSequence> literalsCallback, Predicate<FoundTranslatable> translatableCallback) {
        StringBuilder textSequence = new StringBuilder();
        int startIndex = -1;
        int endIndex = -1; // inclusive

        for (int i = 0; i < components.size(); i++) {
            var contents = components.get(i).getContents();

            if (contents instanceof PlainTextContents textContents) {
                if (startIndex == -1) {
                    startIndex = i;
                }
                endIndex = i;
                textSequence.append(textContents.text());
            } else {
                if (!textSequence.isEmpty()) {
                    if (literalsCallback.test(new FoundLiteralSequence(textSequence.toString(), startIndex, endIndex))) {
                        return;
                    }
                }

                textSequence = new StringBuilder();
                startIndex = -1;

                if (contents instanceof TranslatableContents translatableContents) {
                    if (translatableCallback.test(new FoundTranslatable(translatableContents, i))) {
                        return;
                    }
                }
            }
        }

        endIndex = components.size() - 1;

        if (!textSequence.isEmpty()) {
            literalsCallback.test(new FoundLiteralSequence(textSequence.toString(), startIndex, endIndex));
        }
    }

    public static String codePointSubstring(String string, int start) {
        return string.substring(string.offsetByCodePoints(0, start));
    }

    public static String codePointSubstring(String string, int start, int end) {
        int i = string.offsetByCodePoints(0, start);
        int j = string.offsetByCodePoints(i, end - start);
        return string.substring(i, j);
    }

    /** Splits a literal into two at codepoint index i. */
    @Nullable
    public static SplitPair splitLiteral(Component literal, int i) {
        if (i == 0) {
            return null;
        }

        String text = ((PlainTextContents) literal.getContents()).text();
        String leftText = codePointSubstring(text, 0, i);
        String rightText = codePointSubstring(text, i);

        MutableComponent left = Component.literal(leftText).setStyle(literal.getStyle());
        MutableComponent right = Component.literal(rightText).setStyle(literal.getStyle());
        literal.getSiblings().forEach(right::append);

        return new SplitPair(left, right);
    }

    @Nullable
    public static PlayerInfo addChatHeadForClickTellCommand(ArrayList<Component> components, PlayerInfoCache playerInfoCache) {
        for (int i = 0; i < components.size(); i++) {
            Component c = components.get(i);

            String profileName = getTellReceiver(c);
            if (profileName != null) {
                PlayerInfo playerInfo = playerInfoCache.get(profileName);
                if (playerInfo != null) {
                    var chatHead = createChatHeadComponent(playerInfo, c);
                    components.set(i, Component.empty().append(chatHead).append(c));
                    return playerInfo; // (A5)
                }
            }

            if (c.getContents() instanceof TranslatableContents translatable) {
                int finalI = i;
                PlayerInfo playerInfo = processTranslatableArguments(c, translatable,
                        splitArg -> addChatHeadForClickTellCommand(splitArg, playerInfoCache),
                        decorated -> components.set(finalI, decorated));
                if (playerInfo != null) {
                    return playerInfo; // (A5)
                }
            }
        }

        return null;
    }

    @Nullable
    public static <T> T processTranslatableArguments(Component translatable, TranslatableContents contents,
            Function<ArrayList<Component>, @Nullable T> processSplitArg, Consumer<Component> processedCallback) {
        Object[] args = contents.getArgs().clone();

        // (A3c) for the default "<%s> %s" template, only check the first argument
        int argsLength = Objects.equals(contents.getKey(), "chat.type.text") ? Math.min(1, args.length) : args.length;

        for (int i = 0; i < argsLength; i++) {
            if (args[i] instanceof String text) {
                args[i] = Component.literal(text);
            }

            if (args[i] instanceof Component argComponent) {
                // (A3b) treat each argument separately
                ArrayList<Component> splitArg = split(argComponent);
                T returnValue = processSplitArg.apply(splitArg);
                if (returnValue != null) {
                    Component processedArg = join(splitArg);

                    Object[] processedArgs = Arrays.copyOf(args, args.length);
                    processedArgs[i] = processedArg;

                    MutableComponent processed = Component.translatableWithFallback(contents.getKey(), contents.getFallback(), processedArgs);
                    processed.setStyle(translatable.getStyle());
                    translatable.getSiblings().forEach(processed::append);

                    processedCallback.accept(processed);
                    return returnValue; // (A5)
                }
            }
        }

        return null;
    }

    @Nullable
    public static PlayerInfo addChatHeadForPlayerName(ArrayList<Component> components, PlayerInfoCache playerInfoCache) {
        PlayerInfo[] returnValue = new PlayerInfo[1];

        walkLiteralSequencesAndTranslatables(components, literals -> {
            HeadData headData = HeadLogic.scanForPlayerName(literals.text(), playerInfoCache);
            if (headData == HeadData.EMPTY) {
                return false;
            }

            // find the literal that contains the head position
            int codePointIndex = headData.codePointIndex();
            for (int i = literals.startIndex(); i <= literals.endIndex(); i++) {
                Component literal = components.get(i);
                var contents = (PlainTextContents) literal.getContents();

                int codePointCount = (int) contents.text().codePoints().count();
                if (codePointIndex >= codePointCount) {
                    codePointIndex -= codePointCount;
                    continue;
                }

                var chatHead = createChatHeadComponent(headData.playerInfo(), literal);

                Component decorated;
                SplitPair pair = splitLiteral(literal, codePointIndex);
                if (pair == null) {
                    decorated = Component.empty().append(chatHead).append(literal);
                } else {
                    decorated = Component.empty().append(pair.left()).append(chatHead).append(pair.right());
                }

                components.set(i, decorated);
                returnValue[0] = headData.playerInfo(); // (A5)
                return true;
            }

            return false;
        }, foundTranslatable -> {
            Component translatable = components.get(foundTranslatable.index());
            TranslatableContents contents = foundTranslatable.contents();

            PlayerInfo found = processTranslatableArguments(translatable, contents,
                    splitArg -> addChatHeadForPlayerName(splitArg, playerInfoCache),
                    decorated -> components.set(foundTranslatable.index(), decorated));

            if (found != null) {
                returnValue[0] = found; // (A5)
                return true;
            }
            return false;
        });

        return returnValue[0];
    }

    public static boolean containsPlayerSprite(ArrayList<Component> components) {
        for (Component c : components) {
            if (c.getContents() instanceof ObjectContents(ObjectInfo objectInfo, Optional<Component> _)
                    && objectInfo instanceof PlayerSprite) {
                return true;
            }
        }
        return false;
    }

    @Nullable
    public static String getTellReceiver(Component component) {
        if (component.getStyle().getClickEvent() instanceof ClickEvent.SuggestCommand(String command)) {
            if (command != null && command.startsWith("/tell ")) {
                return command.substring("/tell ".length()).trim();
            }
        }
        return null;
    }

    public static MutableComponent createChatHeadComponent(PlayerInfo playerInfo) {
        var playerSprite = new PlayerSprite(ResolvableProfile.createResolved(playerInfo.getProfile()), playerInfo.showHat());
        return Component.object(playerSprite).withStyle(ChatFormatting.WHITE);
    }

    public static MutableComponent createChatHeadComponent(PlayerInfo playerInfo, Component message) {
        MutableComponent chatHead = createChatHeadComponent(playerInfo);
        if (message.getStyle().isStrikethrough()) {
            return chatHead.withStyle(ChatFormatting.STRIKETHROUGH);
        }
        return chatHead;
    }

    public static Component prependChatHead(Component message, PlayerInfo playerInfo) {
        MutableComponent chatHead = createChatHeadComponent(playerInfo, message);
        return Component.empty().append(chatHead).append(message);
    }
}
