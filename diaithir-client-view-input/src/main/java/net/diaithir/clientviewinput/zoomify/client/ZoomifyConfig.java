// Ported from Zoomify (LGPL-3.0-only) https://github.com/isXander/Zoomify
// Modifications: YACL/JSON settings replaced by a ModConfigSpec; presets and relativeViewBobbing (needs a mixin) omitted.
package net.diaithir.clientviewinput.zoomify.client;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class ZoomifyConfig {
    public enum ZoomKeyBehaviour { HOLD, TOGGLE }

    public enum SpyglassBehaviour { COMBINE, OVERRIDE, ONLY_ZOOM_WHILE_HOLDING, ONLY_ZOOM_WHILE_CARRYING }

    public enum OverlayVisibility { NEVER, HOLDING, CARRYING, ALWAYS }

    public enum SoundBehaviour { NEVER, ALWAYS, ONLY_SPYGLASS, WITH_OVERLAY }

    public enum TransitionType { LINEAR, EASE_IN_EXP, EASE_OUT_EXP }

    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.IntValue INITIAL_ZOOM;
    public static final ModConfigSpec.DoubleValue ZOOM_IN_TIME;
    public static final ModConfigSpec.DoubleValue ZOOM_OUT_TIME;
    public static final ModConfigSpec.EnumValue<TransitionType> ZOOM_IN_TRANSITION;
    public static final ModConfigSpec.EnumValue<TransitionType> ZOOM_OUT_TRANSITION;
    public static final ModConfigSpec.BooleanValue AFFECT_HAND_FOV;

    public static final ModConfigSpec.BooleanValue RETAIN_ZOOM_STEPS;
    public static final ModConfigSpec.BooleanValue SCROLL_ZOOM;
    public static final ModConfigSpec.IntValue SCROLL_STEP_COUNT;
    public static final ModConfigSpec.IntValue ZOOM_PER_STEP;
    public static final ModConfigSpec.IntValue SCROLL_ZOOM_SMOOTHNESS;

    public static final ModConfigSpec.EnumValue<ZoomKeyBehaviour> ZOOM_KEY_BEHAVIOUR;
    public static final ModConfigSpec.BooleanValue KEYBIND_SCROLLING;
    public static final ModConfigSpec.IntValue RELATIVE_SENSITIVITY;
    public static final ModConfigSpec.IntValue CINEMATIC_CAMERA;

    public static final ModConfigSpec.EnumValue<SpyglassBehaviour> SPYGLASS_BEHAVIOUR;
    public static final ModConfigSpec.EnumValue<OverlayVisibility> SPYGLASS_OVERLAY_VISIBILITY;
    public static final ModConfigSpec.EnumValue<SoundBehaviour> SPYGLASS_SOUND_BEHAVIOUR;

    public static final ModConfigSpec.IntValue SECONDARY_ZOOM_AMOUNT;
    public static final ModConfigSpec.DoubleValue SECONDARY_ZOOM_IN_TIME;
    public static final ModConfigSpec.DoubleValue SECONDARY_ZOOM_OUT_TIME;
    public static final ModConfigSpec.BooleanValue SECONDARY_HIDE_HUD_ON_ZOOM;

    static {
        ModConfigSpec.Builder b = new ModConfigSpec.Builder();

        b.push("behaviour");
        INITIAL_ZOOM = b.comment("The zoom level when you first press the keybind.")
                .defineInRange("initialZoom", 4, 1, 100);
        ZOOM_IN_TIME = b.comment("Seconds to complete zooming in.")
                .defineInRange("zoomInTime", 1.0, 0.0, 60.0);
        ZOOM_OUT_TIME = b.comment("Seconds to complete zooming out.")
                .defineInRange("zoomOutTime", 0.5, 0.0, 60.0);
        ZOOM_IN_TRANSITION = b.comment("Transition type used when zooming in.")
                .defineEnum("zoomInTransition", TransitionType.EASE_OUT_EXP);
        ZOOM_OUT_TRANSITION = b.comment("Transition type used when zooming out.")
                .defineEnum("zoomOutTransition", TransitionType.EASE_OUT_EXP);
        AFFECT_HAND_FOV = b.comment("Also zoom the hand. NOTE: the FOV-modifier event used by this port drives world and hand together, so the hand is always affected.")
                .define("affectHandFov", true);
        b.pop();

        b.push("scrolling");
        RETAIN_ZOOM_STEPS = b.comment("Keep the scroll zoom steps between zooms.")
                .define("retainZoomSteps", false);
        SCROLL_ZOOM = b.comment("Allow zooming in and out more with the scroll wheel while zooming.")
                .define("scrollZoom", true);
        SCROLL_STEP_COUNT = b.comment("How many scroll steps in each direction.")
                .defineInRange("scrollStepCount", 10, 1, 100);
        ZOOM_PER_STEP = b.comment("Zoom multiplier per scroll step in percent (150 = 1.5x per step).")
                .defineInRange("zoomPerStep", 150, 100, 1000);
        SCROLL_ZOOM_SMOOTHNESS = b.comment("How smooth scroll zoom is; 0 = instant, 100 = very slow.")
                .defineInRange("scrollZoomSmoothness", 70, 0, 100);
        b.pop();

        b.push("controls");
        ZOOM_KEY_BEHAVIOUR = b.comment("Whether the zoom key is held or toggled.")
                .defineEnum("zoomKeyBehaviour", ZoomKeyBehaviour.HOLD);
        KEYBIND_SCROLLING = b.comment("Allow the zoom keybind to be scrolled.")
                .define("keybindScrolling", false);
        RELATIVE_SENSITIVITY = b.comment("Mouse sensitivity reduction while zoomed, in percent (0 = unchanged, 100 = fully relative to zoom).")
                .defineInRange("relativeSensitivity", 100, 0, 100);
        CINEMATIC_CAMERA = b.comment("Cinematic camera smoothing while zoomed, in percent (0 = off).")
                .defineInRange("cinematicCamera", 0, 0, 100);
        b.pop();

        b.push("spyglass");
        SPYGLASS_BEHAVIOUR = b.comment("How zooming interacts with the spyglass.")
                .defineEnum("spyglassBehaviour", SpyglassBehaviour.COMBINE);
        SPYGLASS_OVERLAY_VISIBILITY = b.comment("When the spyglass overlay is shown.")
                .defineEnum("spyglassOverlayVisibility", OverlayVisibility.HOLDING);
        SPYGLASS_SOUND_BEHAVIOUR = b.comment("When spyglass sounds play.")
                .defineEnum("spyglassSoundBehaviour", SoundBehaviour.WITH_OVERLAY);
        b.pop();

        b.push("secondary");
        SECONDARY_ZOOM_AMOUNT = b.comment("Zoom amount of the secondary zoom.")
                .defineInRange("secondaryZoomAmount", 4, 1, 100);
        SECONDARY_ZOOM_IN_TIME = b.comment("Seconds to complete the secondary zoom in.")
                .defineInRange("secondaryZoomInTime", 10.0, 0.0, 60.0);
        SECONDARY_ZOOM_OUT_TIME = b.comment("Seconds to complete the secondary zoom out.")
                .defineInRange("secondaryZoomOutTime", 1.0, 0.0, 60.0);
        SECONDARY_HIDE_HUD_ON_ZOOM = b.comment("Hide the HUD during the secondary zoom.")
                .define("secondaryHideHudOnZoom", true);
        b.pop();

        SPEC = b.build();
    }

    private ZoomifyConfig() {
    }
}
