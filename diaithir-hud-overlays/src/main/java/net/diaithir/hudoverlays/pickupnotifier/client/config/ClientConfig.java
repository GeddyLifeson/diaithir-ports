// Ported from Pick Up Notifier (MPL-2.0) https://github.com/Fuzss/pickupnotifier
// Modifications: Puzzles Lib config replaced by ModConfigSpec; hidden items use plain id / #tag / modid:* strings; enums nested.
package net.diaithir.hudoverlays.pickupnotifier.client.config;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.navigation.ScreenAxis;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.ModConfigSpec;

public final class ClientConfig {
    public enum CombineEntries { ALWAYS, NEVER, EXCLUDE_NAMED }

    public enum EntryBackground { NONE, CHAT, TOOLTIP }

    public enum DisplayAmount {
        OFF(false, false),
        SPRITE(true, false),
        TEXT(false, true),
        BOTH(true, true);

        private final boolean sprite;
        private final boolean text;

        DisplayAmount(boolean sprite, boolean text) {
            this.sprite = sprite;
            this.text = text;
        }

        public boolean isSprite() {
            return this.sprite;
        }

        public boolean isText() {
            return this.text;
        }
    }

    public enum MoveOut {
        DISABLED {
            @Override
            public boolean move(ScreenAxis axis) {
                return false;
            }
        },
        HORIZONTALLY_ONLY {
            @Override
            public boolean move(ScreenAxis axis) {
                return axis == ScreenAxis.HORIZONTAL && !POSITION.get().isHorizontalCenter();
            }
        },
        VERTICALLY_ONLY {
            @Override
            public boolean move(ScreenAxis axis) {
                return axis == ScreenAxis.VERTICAL && !POSITION.get().isVerticalCenter();
            }
        },
        ENABLED {
            @Override
            public boolean move(ScreenAxis axis) {
                return POSITION.get() != AnchorPoint.CENTER;
            }
        };

        public abstract boolean move(ScreenAxis axis);
    }

    public static final ModConfigSpec SPEC;

    // general
    public static final ModConfigSpec.BooleanValue CLIENT_ONLY;
    public static final ModConfigSpec.BooleanValue INCLUDE_ITEMS;
    public static final ModConfigSpec.BooleanValue INCLUDE_EXPERIENCE;
    public static final ModConfigSpec.BooleanValue INCLUDE_ARROWS;
    public static final ModConfigSpec.BooleanValue EXPERIENCE_VALUE;
    public static final ModConfigSpec.BooleanValue DISABLE_IN_CREATIVE;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> HIDDEN_ITEMS;

    // behavior
    public static final ModConfigSpec.EnumValue<CombineEntries> COMBINE_ENTRIES;
    public static final ModConfigSpec.IntValue DISPLAY_TIME;
    public static final ModConfigSpec.EnumValue<MoveOut> MOVE_OUT;
    public static final ModConfigSpec.IntValue MOVE_TIME;
    public static final ModConfigSpec.BooleanValue FADE_OUT;

    // display
    public static final ModConfigSpec.BooleanValue DRAW_SPRITE;
    public static final ModConfigSpec.EnumValue<ChatFormatting> TEXT_COLOR;
    public static final ModConfigSpec.BooleanValue IGNORE_RARITY;
    public static final ModConfigSpec.EnumValue<AnchorPoint> POSITION;
    public static final ModConfigSpec.IntValue OFFSET_X;
    public static final ModConfigSpec.IntValue OFFSET_Y;
    public static final ModConfigSpec.DoubleValue MAX_HEIGHT;
    public static final ModConfigSpec.IntValue DISPLAY_SCALE;
    public static final ModConfigSpec.EnumValue<DisplayAmount> DISPLAY_AMOUNT;
    public static final ModConfigSpec.BooleanValue INVENTORY_COUNT;
    public static final ModConfigSpec.BooleanValue DISPLAY_SINGLE_COUNT;
    public static final ModConfigSpec.EnumValue<EntryBackground> ENTRY_BACKGROUND;
    public static final ModConfigSpec.BooleanValue DISPLAY_ITEM_NAME;

    static {
        ModConfigSpec.Builder b = new ModConfigSpec.Builder();

        b.push("general");
        CLIENT_ONLY = b.comment(
                "Force the mod to run client-side only.",
                "Only enable this when there are problems, e. g. the same pick-up being logged multiple times. Partial item stack pick-ups (when the inventory is full) won't show, and backpack compatibility won't work.",
                "When playing on a server without this mod this option will be used automatically.")
                .define("clientOnly", false);
        INCLUDE_ITEMS = b.comment("Show item entities the player has collected in the pick-up notifications.")
                .define("includeItems", true);
        INCLUDE_EXPERIENCE = b.comment("Show experience orbs the player has collected in the pick-up notifications.")
                .define("includeExperience", true);
        INCLUDE_ARROWS = b.comment("Show shot arrows the player has collected in the pick-up notifications.")
                .define("includeArrows", true);
        EXPERIENCE_VALUE = b.comment("Show the value of experience points collected instead of the amount of individual orbs.")
                .define("experienceValue", true);
        DISABLE_IN_CREATIVE = b.comment("Prevent items from being added to the pick-up log when in creative mode.")
                .define("disableInCreative", false);
        HIDDEN_ITEMS = b.comment("Disable specific items or content from whole mods from showing.",
                "Entries: item id (minecraft:stick), item tag (#minecraft:logs) or whole mod (modid:*).")
                .defineListAllowEmpty("hidden_items", List.of(), () -> "", o -> o instanceof String);
        b.pop();

        b.push("behavior");
        COMBINE_ENTRIES = b.comment("Combine entries of the same type instead of showing each one individually.")
                .defineEnum("combineEntries", CombineEntries.EXCLUDE_NAMED);
        DISPLAY_TIME = b.comment("Amount of ticks each entry will be shown for. Set to zero to only remove entries when space for new ones is needed.")
                .defineInRange("displayTime", 80, 0, Integer.MAX_VALUE);
        MOVE_OUT = b.comment("Make outdated entries slowly move out of the screen.",
                "Not necessarily supported by all position presets.")
                .defineEnum("moveOut", MoveOut.VERTICALLY_ONLY);
        MOVE_TIME = b.comment("Amount of ticks it takes for an entry to move out of the screen.")
                .defineInRange("moveTime", 20, 0, Integer.MAX_VALUE);
        FADE_OUT = b.comment("Make outdated entry names slowly fade away instead of simply vanishing.")
                .define("fadeOut", true);
        b.pop();

        b.push("display");
        DRAW_SPRITE = b.comment("Show a small sprite next to the name of each entry showing its contents.")
                .define("draw_sprite", true);
        List<ChatFormatting> colors = new ArrayList<>();
        for (ChatFormatting f : ChatFormatting.values()) {
            if (f.isColor()) {
                colors.add(f);
            }
        }
        TEXT_COLOR = b.comment("Color of the entry name text.")
                .defineEnum("default_text_color", ChatFormatting.WHITE, colors);
        IGNORE_RARITY = b.comment("Ignore rarity when determining item name color.")
                .define("ignore_rarity", false);
        POSITION = b.comment("Screen corner for entry list to be drawn in.")
                .defineEnum("screen_corner", AnchorPoint.BOTTOM_RIGHT);
        OFFSET_X = b.comment("Offset on x-axis from screen border.")
                .defineInRange("offset_x", 8, 0, Integer.MAX_VALUE);
        OFFSET_Y = b.comment("Offset on y-axis from screen border.")
                .defineInRange("offset_y", 4, 0, Integer.MAX_VALUE);
        MAX_HEIGHT = b.comment("Percentage of relative screen height entries are allowed to fill at max.")
                .defineInRange("max_height", 0.5, 0.0, 1.0);
        DISPLAY_SCALE = b.comment("Scale of entries. A lower scale will make room for more rows to show. Works together with \"GUI Scale\" option in \"Video Settings\".")
                .defineInRange("display_scale", 4, 1, 24);
        DISPLAY_AMOUNT = b.comment("Where and if to display the amount of items picked up. 'SPRITE' will render the amount on the item sprite like in inventories, 'TEXT' will add a dedicated text including the amount to the item name display.")
                .defineEnum("displayAmount", DisplayAmount.TEXT);
        INVENTORY_COUNT = b.comment("Add the total amount of an item in your inventory to the entry.")
                .define("inventoryCount", false);
        DISPLAY_SINGLE_COUNT = b.comment("Should the picked up amount be shown when it's just a single item.")
                .define("displaySingleCount", true);
        ENTRY_BACKGROUND = b.comment("Mode for drawing a background behind entries for better visibility. 'CHAT' is similar to the chat background, 'TOOLTIP' uses the tooltip background rendering instead.")
                .defineEnum("entryBackground", EntryBackground.CHAT);
        DISPLAY_ITEM_NAME = b.comment("Add the name of the item to the entry.")
                .define("displayItemName", true);
        b.pop();

        SPEC = b.build();
    }

    private ClientConfig() {}

    public static int getMoveTime() {
        return Math.min(MOVE_TIME.get(), DISPLAY_TIME.get());
    }

    public static float getDisplayScale() {
        return DISPLAY_SCALE.get() / 6.0F;
    }

    public static boolean isHidden(ItemStack stack) {
        Identifier id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        for (String entry : HIDDEN_ITEMS.get()) {
            if (entry.startsWith("#")) {
                Identifier tag = Identifier.tryParse(entry.substring(1));
                if (tag != null && stack.is(TagKey.create(Registries.ITEM, tag))) {
                    return true;
                }
            } else if (entry.endsWith(":*")) {
                if (id.getNamespace().equals(entry.substring(0, entry.length() - 2))) {
                    return true;
                }
            } else if (id.toString().equals(entry)) {
                return true;
            }
        }
        return false;
    }
}
