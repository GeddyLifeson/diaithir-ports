package li.cil.oc2.common.config.common;

import li.cil.oc2.common.config.Config;
import net.neoforged.neoforge.common.ModConfigSpec;

public class GameplaySpec {
    // net.minecraft.world.item.Tiers no longer exists (ToolMaterial is a record, not an enum),
    // so the config keeps an equivalent enum with the same constant names.
    public enum ToolTier {
        WOOD,
        STONE,
        IRON,
        DIAMOND,
        GOLD,
        NETHERITE
    }

    public final ModConfigSpec.EnumValue<ToolTier> blockOperationsModuleToolTier;
    public final ModConfigSpec.LongValue soundCardCoolDownSeconds;

    GameplaySpec(ModConfigSpec.Builder builder) {
        blockOperationsModuleToolTier = builder.comment(
            "The mining tool equivalent of the block operations module"
        ).defineEnum("blockOperationsModuleToolTier", ToolTier.DIAMOND);

        soundCardCoolDownSeconds = builder.comment(
            "The number of seconds between sound card uses, to prevent spam/abuse"
        ).defineInRange("soundCardCoolDownSeconds", 2, 1, Long.MAX_VALUE);
    }

    public void loadValues() {
        Config.blockOperationsModuleToolTier = blockOperationsModuleToolTier.get().name();
        Config.soundCardCoolDownSeconds = soundCardCoolDownSeconds.get();
    }
}
