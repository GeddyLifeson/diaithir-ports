// Ported from Packet Fixer (MIT) https://github.com/TonimatasDEV/PacketFixer
// Modifications: ModConfigSpec definition of the upstream properties options; values are mirrored into PacketFixerSettings.
package net.diaithir.networkcompat.packetfixer.config;

import net.diaithir.networkcompat.packetfixer.util.PacketFixerSettings;
import net.neoforged.neoforge.common.ModConfigSpec;

public final class PacketFixerConfig {
    public static final ModConfigSpec SPEC;
    private static final ModConfigSpec.BooleanValue ALL_SIZES_UNLIMITED;
    private static final ModConfigSpec.LongValue NBT_MAX_SIZE;
    private static final ModConfigSpec.IntValue PACKET_SIZE;
    private static final ModConfigSpec.IntValue DECODER_SIZE;
    private static final ModConfigSpec.IntValue VAR_INT_21;
    private static final ModConfigSpec.IntValue VAR_INT;
    private static final ModConfigSpec.IntValue VAR_LONG;
    private static final ModConfigSpec.IntValue STRING_SIZE;
    private static final ModConfigSpec.IntValue CHUNK_PACKET_DATA;
    private static final ModConfigSpec.IntValue READ_TIMEOUT;
    private static final ModConfigSpec.IntValue LOGIN_TIMEOUT;
    private static final ModConfigSpec.IntValue KEEP_ALIVE_TIMEOUT;
    private static final ModConfigSpec.IntValue PLAYER_IDLE_TIMEOUT;
    private static final ModConfigSpec.BooleanValue FORCE_UNLIMITED_NBT;

    static {
        ModConfigSpec.Builder b = new ModConfigSpec.Builder();
        b.comment("Vanilla defaults: nbtMaxSize 2097152, packetSize 1048576, decoderSize 8388608, varInt21 3.");
        ALL_SIZES_UNLIMITED = b.comment("Ignore all size limits below and use the maximum values")
                .define("allSizesUnlimited", true);
        NBT_MAX_SIZE = b.defineInRange("nbtMaxSize", 2097152L, 1L, Long.MAX_VALUE);
        PACKET_SIZE = b.defineInRange("packetSize", 1048576, 1, Integer.MAX_VALUE);
        DECODER_SIZE = b.defineInRange("decoderSize", 8388608, 1, Integer.MAX_VALUE);
        VAR_INT_21 = b.defineInRange("varInt21", 3, 1, Integer.MAX_VALUE);
        VAR_INT = b.defineInRange("varInt", 5, 1, Integer.MAX_VALUE);
        VAR_LONG = b.defineInRange("varLong", 10, 1, Integer.MAX_VALUE);
        STRING_SIZE = b.defineInRange("stringSize", 32767, 1, Integer.MAX_VALUE);
        CHUNK_PACKET_DATA = b.defineInRange("chunkPacketData", 2097152, 1, Integer.MAX_VALUE);
        READ_TIMEOUT = b.comment("Seconds").defineInRange("readTimeout", 120, 1, Integer.MAX_VALUE);
        LOGIN_TIMEOUT = b.comment("Seconds").defineInRange("loginTimeout", 120, 1, Integer.MAX_VALUE);
        KEEP_ALIVE_TIMEOUT = b.comment("Seconds").defineInRange("keepAliveTimeout", 120, 1, Integer.MAX_VALUE);
        PLAYER_IDLE_TIMEOUT = b.comment("Minutes; only raises an already enabled idle timeout").defineInRange("playerIdleTimeout", 0, 0, Integer.MAX_VALUE);
        FORCE_UNLIMITED_NBT = b.define("forceUnlimitedNbtEnabled", false);
        SPEC = b.build();
    }

    private PacketFixerConfig() {}

    public static void refresh() {
        PacketFixerSettings.apply(
                ALL_SIZES_UNLIMITED.get(),
                NBT_MAX_SIZE.get(),
                PACKET_SIZE.get(),
                DECODER_SIZE.get(),
                VAR_INT_21.get(),
                VAR_INT.get(),
                VAR_LONG.get(),
                STRING_SIZE.get(),
                CHUNK_PACKET_DATA.get(),
                READ_TIMEOUT.get(),
                LOGIN_TIMEOUT.get(),
                KEEP_ALIVE_TIMEOUT.get(),
                PLAYER_IDLE_TIMEOUT.get(),
                FORCE_UNLIMITED_NBT.get());
    }
}
