package net.diaithir.entityanimation.notenoughanimations.client;

import java.util.List;
import net.neoforged.neoforge.common.ModConfigSpec;

public final class AnimationConfig {
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.BooleanValue ENABLED;
    public static final ModConfigSpec.BooleanValue HELD_ITEM_POSES;
    public static final ModConfigSpec.BooleanValue USE_ITEM_ANIMATIONS;
    public static final ModConfigSpec.BooleanValue SNEAK_ANIMATION;
    public static final ModConfigSpec.BooleanValue RIDING_ANIMATION;
    public static final ModConfigSpec.BooleanValue IDLE_BREATHING;
    public static final ModConfigSpec.BooleanValue HEAD_LAG;
    public static final ModConfigSpec.DoubleValue SMOOTHING_SPEED;
    public static final ModConfigSpec.DoubleValue INTENSITY;
    public static final ModConfigSpec.BooleanValue APPLY_TO_OTHER_PLAYERS;
    public static final ModConfigSpec.BooleanValue FIRST_PERSON_ARMS;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> ITEM_OVERRIDES;

    static {
        ModConfigSpec.Builder b = new ModConfigSpec.Builder();
        ENABLED = b.define("enabled", true);
        HELD_ITEM_POSES = b.define("heldItemPoses", true);
        USE_ITEM_ANIMATIONS = b.define("useItemAnimations", true);
        SNEAK_ANIMATION = b.define("sneakAnimation", true);
        RIDING_ANIMATION = b.define("ridingAnimation", true);
        IDLE_BREATHING = b.define("idleBreathing", true);
        HEAD_LAG = b.define("headLag", true);
        SMOOTHING_SPEED = b.defineInRange("smoothingSpeed", 0.35, 0.05, 1.0);
        INTENSITY = b.defineInRange("intensity", 1.0, 0.0, 2.0);
        APPLY_TO_OTHER_PLAYERS = b.define("applyToOtherPlayers", true);
        FIRST_PERSON_ARMS = b.define("firstPersonArms", false);
        ITEM_OVERRIDES = b.comment("Entries of the form modid:item=pose (poses: none, tool, block, ranged, food, shield, twohanded)")
                .defineListAllowEmpty("itemOverrides", List.of(), () -> "", o -> o instanceof String);
        SPEC = b.build();
    }

    private AnimationConfig() {}
}
