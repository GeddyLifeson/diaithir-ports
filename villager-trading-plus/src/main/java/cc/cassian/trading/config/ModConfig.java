package cc.cassian.trading.config;

import folk.sisby.kaleido.api.WrappedConfig;
import folk.sisby.kaleido.lib.quiltconfig.api.annotations.Comment;
import folk.sisby.kaleido.lib.quiltconfig.api.annotations.DisplayName;

public class ModConfig extends WrappedConfig {

    @Comment("Whether the mod should be enabled.")
    @Comment("You can also temporarily disable the mod by holding CTRL.")
    public boolean enabled = true;

    @DisplayName("Make 'trade all' default")
    @Comment("If false, use shift to trade all.")
    @Comment("If true, normal click trades all, shift click trades once.")
    public boolean swapshift = false;



}