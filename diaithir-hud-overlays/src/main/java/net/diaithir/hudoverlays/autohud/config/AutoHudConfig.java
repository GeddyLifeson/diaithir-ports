// Ported from AutoHUD (LGPL-3.0-only) https://github.com/Crendgrim/AutoHUD
// Modifications: ModConfigSpec replacement of the YACL config; per-component policy/direction/distance/fade options.
package net.diaithir.hudoverlays.autohud.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class AutoHudConfig {
    public enum RevealPolicy { ALWAYS, DISABLED, CHANGING, INCREASING, DECREASING, NOT_FULL, LOW }
    public enum EventPolicy { REVEAL, HIDE, NOTHING }
    public enum RevealType { INDIVIDUAL, STACKED, GROUPED, HIDE_COMBINED, COMBINED }
    public enum Direction { UP, DOWN, LEFT, RIGHT }

    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.BooleanValue DYNAMIC_ON_LOAD;
    public static final ModConfigSpec.IntValue TICKS_REVEALED;
    public static final ModConfigSpec.BooleanValue ANIMATION_MOVE;
    public static final ModConfigSpec.BooleanValue ANIMATION_FADE;
    public static final ModConfigSpec.DoubleValue ANIMATION_SPEED;
    public static final ModConfigSpec.DoubleValue SPEED_MOVE_IN;
    public static final ModConfigSpec.DoubleValue SPEED_MOVE_OUT;
    public static final ModConfigSpec.DoubleValue SPEED_FADE_IN;
    public static final ModConfigSpec.DoubleValue SPEED_FADE_OUT;
    public static final ModConfigSpec.EnumValue<RevealType> REVEAL_TYPE;

    public static final ModConfigSpec.EnumValue<EventPolicy> ON_SCREEN_OPEN;
    public static final ModConfigSpec.EnumValue<EventPolicy> ON_MOVING;
    public static final ModConfigSpec.EnumValue<EventPolicy> ON_STANDING_STILL;
    public static final ModConfigSpec.EnumValue<EventPolicy> ON_PAUSE_SCREEN;
    public static final ModConfigSpec.EnumValue<EventPolicy> ON_SNEAKING;
    public static final ModConfigSpec.EnumValue<EventPolicy> ON_FLYING;
    public static final ModConfigSpec.BooleanValue ON_USING_ITEM;
    public static final ModConfigSpec.BooleanValue ON_MINING;
    public static final ModConfigSpec.BooleanValue ON_SWINGING;
    public static final ModConfigSpec.BooleanValue ON_ATTACKING;

    public static final ModConfigSpec.DoubleValue DEFAULT_SPEED_MULTIPLIER;
    public static final ModConfigSpec.IntValue DEFAULT_DISTANCE;
    public static final ModConfigSpec.DoubleValue DEFAULT_MAXIMUM_FADE;

    public static final ModConfigSpec.EnumValue<RevealPolicy> HEALTH_POLICY;
    public static final ModConfigSpec.EnumValue<RevealPolicy> HUNGER_POLICY;
    public static final ModConfigSpec.EnumValue<RevealPolicy> AIR_POLICY;
    public static final ModConfigSpec.EnumValue<RevealPolicy> ARMOR_POLICY;
    public static final ModConfigSpec.EnumValue<RevealPolicy> MOUNT_HEALTH_POLICY;
    public static final ModConfigSpec.BooleanValue REVEAL_BARS_WITH_CONSUMABLE;

    public static final ModConfigSpec.BooleanValue EXPERIENCE_ACTIVE;
    public static final ModConfigSpec.BooleanValue EXPERIENCE_ON_CHANGE;
    public static final ModConfigSpec.BooleanValue EXPERIENCE_BAR_ACTIVE;
    public static final ModConfigSpec.BooleanValue EXPERIENCE_BAR_ON_CHANGE;
    public static final ModConfigSpec.BooleanValue REVEAL_XP_ON_ENCHANT_BLOCK;
    public static final ModConfigSpec.BooleanValue LOCATOR_BAR_ACTIVE;
    public static final ModConfigSpec.BooleanValue LOCATOR_BAR_ON_CHANGE;
    public static final ModConfigSpec.BooleanValue MOUNT_JUMP_ACTIVE;
    public static final ModConfigSpec.BooleanValue MOUNT_JUMP_ON_CHANGE;

    public static final ModConfigSpec.BooleanValue STATUS_EFFECTS_ACTIVE;
    public static final ModConfigSpec.BooleanValue STATUS_EFFECTS_ON_CHANGE;
    public static final ModConfigSpec.BooleanValue HIDE_PERSISTENT_EFFECTS;
    public static final ModConfigSpec.BooleanValue SHOW_HIDDEN_EFFECTS;

    public static final ModConfigSpec.BooleanValue HOTBAR_ACTIVE;
    public static final ModConfigSpec.BooleanValue HOTBAR_ON_CHANGE;
    public static final ModConfigSpec.BooleanValue HOTBAR_ON_DAMAGE_CHANGE;
    public static final ModConfigSpec.BooleanValue HOTBAR_ON_STACK_CHANGE;
    public static final ModConfigSpec.BooleanValue HOTBAR_ON_SLOT_CHANGE;
    public static final ModConfigSpec.BooleanValue HOTBAR_ON_LOW_DURABILITY;
    public static final ModConfigSpec.IntValue HOTBAR_DURABILITY_PERCENT;
    public static final ModConfigSpec.IntValue HOTBAR_DURABILITY_TOTAL;
    public static final ModConfigSpec.DoubleValue HOTBAR_ITEMS_MAX_FADE;
    public static final ModConfigSpec.BooleanValue XP_WITH_HOTBAR;
    public static final ModConfigSpec.BooleanValue HIDE_TOOLTIP;

    public static final ModConfigSpec.BooleanValue SCOREBOARD_ACTIVE;
    public static final ModConfigSpec.BooleanValue SCOREBOARD_ON_CHANGE;

    public static final ModConfigSpec.BooleanValue CROSSHAIR_ACTIVE;
    public static final ModConfigSpec.BooleanValue CROSSHAIR_ON_CHANGE;
    public static final ModConfigSpec.DoubleValue CROSSHAIR_MAX_FADE;

    public static final ModConfigSpec.BooleanValue CHAT_ACTIVE;
    public static final ModConfigSpec.BooleanValue CHAT_INDICATOR;
    public static final ModConfigSpec.BooleanValue ACTION_BAR_ACTIVE;
    public static final ModConfigSpec.BooleanValue BOSS_BAR_ACTIVE;

    public static final ModConfigSpec.EnumValue<Direction> DIR_HOTBAR;
    public static final ModConfigSpec.EnumValue<Direction> DIR_HEALTH;
    public static final ModConfigSpec.EnumValue<Direction> DIR_ARMOR;
    public static final ModConfigSpec.EnumValue<Direction> DIR_HUNGER;
    public static final ModConfigSpec.EnumValue<Direction> DIR_AIR;
    public static final ModConfigSpec.EnumValue<Direction> DIR_EXPERIENCE;
    public static final ModConfigSpec.EnumValue<Direction> DIR_LOCATOR;
    public static final ModConfigSpec.EnumValue<Direction> DIR_MOUNT_JUMP;
    public static final ModConfigSpec.EnumValue<Direction> DIR_MOUNT_HEALTH;
    public static final ModConfigSpec.EnumValue<Direction> DIR_STATUS_EFFECTS;
    public static final ModConfigSpec.EnumValue<Direction> DIR_SCOREBOARD;
    public static final ModConfigSpec.EnumValue<Direction> DIR_CHAT;
    public static final ModConfigSpec.EnumValue<Direction> DIR_ACTION_BAR;
    public static final ModConfigSpec.EnumValue<Direction> DIR_BOSS_BAR;

    private static ModConfigSpec.EnumValue<Direction> dir(ModConfigSpec.Builder b, String name, Direction def) {
        return b.comment("Direction the component moves when hiding").defineEnum(name, def);
    }

    static {
        ModConfigSpec.Builder b = new ModConfigSpec.Builder();

        b.push("main");
        DYNAMIC_ON_LOAD = b.comment("Enable dynamic HUD on game load").define("dynamicOnLoad", true);
        TICKS_REVEALED = b.comment("Ticks that components stay revealed").defineInRange("ticksRevealed", 40, 10, 200);
        ANIMATION_MOVE = b.comment("Hide components by moving them off screen").define("animationMove", true);
        ANIMATION_FADE = b.comment("Hide components by fading them out").define("animationFade", true);
        ANIMATION_SPEED = b.defineInRange("animationSpeed", 2.0, 1.0, 4.0);
        SPEED_MOVE_IN = b.comment("0 = use animationSpeed").defineInRange("speedMoveIn", 0.0, 0.0, 4.0);
        SPEED_MOVE_OUT = b.comment("0 = use animationSpeed").defineInRange("speedMoveOut", 0.0, 0.0, 4.0);
        SPEED_FADE_IN = b.comment("0 = use animationSpeed").defineInRange("speedFadeIn", 0.0, 0.0, 4.0);
        SPEED_FADE_OUT = b.comment("0 = use animationSpeed").defineInRange("speedFadeOut", 0.0, 0.0, 4.0);
        REVEAL_TYPE = b.comment("How revealing one component affects others").defineEnum("revealType", RevealType.STACKED);
        b.pop();

        b.push("events");
        ON_SCREEN_OPEN = b.defineEnum("onScreenOpen", EventPolicy.NOTHING);
        ON_MOVING = b.defineEnum("onMoving", EventPolicy.NOTHING);
        ON_STANDING_STILL = b.defineEnum("onStandingStill", EventPolicy.NOTHING);
        ON_PAUSE_SCREEN = b.defineEnum("onPauseScreen", EventPolicy.NOTHING);
        ON_SNEAKING = b.defineEnum("onSneaking", EventPolicy.NOTHING);
        ON_FLYING = b.defineEnum("onFlying", EventPolicy.NOTHING);
        ON_USING_ITEM = b.define("onUsingItem", false);
        ON_MINING = b.define("onMining", false);
        ON_SWINGING = b.define("onSwinging", false);
        ON_ATTACKING = b.define("onAttacking", false);
        b.pop();

        b.push("defaults");
        DEFAULT_SPEED_MULTIPLIER = b.defineInRange("speedMultiplier", 1.0, 0.1, 5.0);
        DEFAULT_DISTANCE = b.defineInRange("distance", 60, 0, 200);
        DEFAULT_MAXIMUM_FADE = b.defineInRange("maximumFade", 0.0, 0.0, 1.0);
        b.pop();

        b.push("statusBars");
        HEALTH_POLICY = b.defineEnum("health", RevealPolicy.CHANGING);
        HUNGER_POLICY = b.defineEnum("hunger", RevealPolicy.LOW);
        AIR_POLICY = b.defineEnum("air", RevealPolicy.NOT_FULL);
        ARMOR_POLICY = b.defineEnum("armor", RevealPolicy.CHANGING);
        MOUNT_HEALTH_POLICY = b.defineEnum("mountHealth", RevealPolicy.CHANGING);
        REVEAL_BARS_WITH_CONSUMABLE = b.define("revealBarsWhenHoldingConsumableItem", true);
        EXPERIENCE_ACTIVE = b.define("experienceActive", true);
        EXPERIENCE_ON_CHANGE = b.define("experienceOnChange", true);
        EXPERIENCE_BAR_ACTIVE = b.define("experienceBarActive", true);
        EXPERIENCE_BAR_ON_CHANGE = b.define("experienceBarOnChange", true);
        REVEAL_XP_ON_ENCHANT_BLOCK = b.define("revealExperienceOnTargetingEnchantingBlock", true);
        LOCATOR_BAR_ACTIVE = b.define("locatorBarActive", true);
        LOCATOR_BAR_ON_CHANGE = b.define("locatorBarOnChange", true);
        MOUNT_JUMP_ACTIVE = b.define("mountJumpBarActive", true);
        MOUNT_JUMP_ON_CHANGE = b.define("mountJumpBarOnChange", true);
        b.pop();

        b.push("statusEffects");
        STATUS_EFFECTS_ACTIVE = b.define("active", true);
        STATUS_EFFECTS_ON_CHANGE = b.define("onChange", true);
        HIDE_PERSISTENT_EFFECTS = b.define("hidePersistent", true);
        SHOW_HIDDEN_EFFECTS = b.define("showHidden", false);
        b.pop();

        b.push("hotbar");
        HOTBAR_ACTIVE = b.define("active", true);
        HOTBAR_ON_CHANGE = b.define("onChange", true);
        HOTBAR_ON_DAMAGE_CHANGE = b.define("onDamageChange", false);
        HOTBAR_ON_STACK_CHANGE = b.define("onStackSizeChange", true);
        HOTBAR_ON_SLOT_CHANGE = b.define("onSlotChange", true);
        HOTBAR_ON_LOW_DURABILITY = b.define("onLowDurability", true);
        HOTBAR_DURABILITY_PERCENT = b.defineInRange("durabilityPercentage", 10, 0, 100);
        HOTBAR_DURABILITY_TOTAL = b.defineInRange("durabilityTotal", 20, 0, 100000);
        HOTBAR_ITEMS_MAX_FADE = b.defineInRange("maximumFadeHotbarItems", 0.0, 0.0, 1.0);
        XP_WITH_HOTBAR = b.define("revealExperienceTextWithHotbar", true);
        HIDE_TOOLTIP = b.define("hideTooltip", false);
        b.pop();

        b.push("scoreboard");
        SCOREBOARD_ACTIVE = b.define("active", true);
        SCOREBOARD_ON_CHANGE = b.define("onChange", true);
        b.pop();

        b.push("crosshair");
        CROSSHAIR_ACTIVE = b.define("active", true);
        CROSSHAIR_ON_CHANGE = b.define("onChange", true);
        CROSSHAIR_MAX_FADE = b.defineInRange("maximumFade", 0.1, 0.0, 1.0);
        b.pop();

        b.push("misc");
        CHAT_ACTIVE = b.define("hideChat", false);
        CHAT_INDICATOR = b.define("chatIndicator", true);
        ACTION_BAR_ACTIVE = b.define("hideActionBar", false);
        BOSS_BAR_ACTIVE = b.define("hideBossBar", false);
        b.pop();

        b.push("directions");
        DIR_HOTBAR = dir(b, "hotbar", Direction.DOWN);
        DIR_HEALTH = dir(b, "health", Direction.DOWN);
        DIR_ARMOR = dir(b, "armor", Direction.DOWN);
        DIR_HUNGER = dir(b, "hunger", Direction.DOWN);
        DIR_AIR = dir(b, "air", Direction.DOWN);
        DIR_EXPERIENCE = dir(b, "experience", Direction.DOWN);
        DIR_LOCATOR = dir(b, "locatorBar", Direction.DOWN);
        DIR_MOUNT_JUMP = dir(b, "mountJumpBar", Direction.DOWN);
        DIR_MOUNT_HEALTH = dir(b, "mountHealth", Direction.DOWN);
        DIR_STATUS_EFFECTS = dir(b, "statusEffects", Direction.UP);
        DIR_SCOREBOARD = dir(b, "scoreboard", Direction.RIGHT);
        DIR_CHAT = dir(b, "chat", Direction.LEFT);
        DIR_ACTION_BAR = dir(b, "actionBar", Direction.DOWN);
        DIR_BOSS_BAR = dir(b, "bossBar", Direction.UP);
        b.pop();

        SPEC = b.build();
    }

    private AutoHudConfig() {}
}
