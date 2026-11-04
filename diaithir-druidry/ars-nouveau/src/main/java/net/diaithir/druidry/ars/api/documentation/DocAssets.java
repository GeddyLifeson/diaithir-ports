package net.diaithir.druidry.ars.api.documentation;

/** Port stub: the Patchouli-free port has no documentation GUI, so icons are inert placeholders. */
public class DocAssets {
    public record BlitInfo(String name) {
    }

    private static BlitInfo b(String n) {
        return new BlitInfo(n);
    }

    public static final BlitInfo NA_ICON = b("na");
    public static final BlitInfo AIR_ICON = b("air");
    public static final BlitInfo EARTH_ICON = b("earth");
    public static final BlitInfo FIRE_ICON = b("fire");
    public static final BlitInfo WATER_ICON = b("water");
    public static final BlitInfo ALCHEMANCY_ICON = b("alchemancy");
    public static final BlitInfo CONJURATION_ICON = b("conjuration");
    public static final BlitInfo MANIPULATION_ICON = b("manipulation");
    public static final BlitInfo FORM_ICON = b("form");
    public static final BlitInfo EFFECT_ICON = b("effect");
    public static final BlitInfo AUGMENT_ICON = b("augment");
    public static final BlitInfo TIER_ONE = b("tier_one");
    public static final BlitInfo TIER_TWO = b("tier_two");
    public static final BlitInfo TIER_THREE = b("tier_three");
    public static final BlitInfo ICON_THREAD_TIER1 = b("thread_1");
    public static final BlitInfo ICON_THREAD_TIER2 = b("thread_2");
    public static final BlitInfo ICON_THREAD_TIER3 = b("thread_3");
    public static final BlitInfo ICON_THREAD_TIER = ICON_THREAD_TIER3;
}
