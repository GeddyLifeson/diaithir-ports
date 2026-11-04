package net.diaithir.entityanimation.skinlayers3d.client;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class SkinLayersConfig {
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.BooleanValue ENABLED;
    public static final ModConfigSpec.BooleanValue HEAD;
    public static final ModConfigSpec.BooleanValue BODY;
    public static final ModConfigSpec.BooleanValue ARMS;
    public static final ModConfigSpec.BooleanValue LEGS;
    public static final ModConfigSpec.BooleanValue FIRST_PERSON;
    public static final ModConfigSpec.DoubleValue LOD_DISTANCE;
    public static final ModConfigSpec.DoubleValue VOXEL_SCALE;
    public static final ModConfigSpec.BooleanValue OTHER_PLAYERS;
    public static final ModConfigSpec.BooleanValue NON_PLAYER_MODELS;
    public static final ModConfigSpec.IntValue MAX_CACHED_MESHES;
    public static final ModConfigSpec.BooleanValue HIDE_UNDER_ARMOR;

    static {
        ModConfigSpec.Builder b = new ModConfigSpec.Builder();
        ENABLED = b.define("enabled", true);
        HEAD = b.define("renderHead", true);
        BODY = b.define("renderBody", true);
        ARMS = b.define("renderArms", true);
        LEGS = b.define("renderLegs", true);
        FIRST_PERSON = b.define("renderInFirstPerson", true);
        LOD_DISTANCE = b.defineInRange("lodDistance", 16.0, 2.0, 64.0);
        VOXEL_SCALE = b.defineInRange("voxelScale", 1.0, 1.0, 1.15);
        OTHER_PLAYERS = b.define("renderOnOtherPlayers", true);
        NON_PLAYER_MODELS = b.define("renderOnNonPlayerModels", true);
        MAX_CACHED_MESHES = b.defineInRange("maxCachedMeshes", 64, 1, 1024);
        HIDE_UNDER_ARMOR = b.define("hideUnderArmor", true);
        SPEC = b.build();
    }

    private SkinLayersConfig() {}
}
