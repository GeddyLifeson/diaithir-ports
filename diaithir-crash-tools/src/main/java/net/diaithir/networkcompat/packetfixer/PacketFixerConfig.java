// Ported from Packet Fixer (MIT) https://github.com/TonimatasDEV/PacketFixer
// Modifications: Config options of the upstream packetfixer.properties expressed as a NeoForge ModConfigSpec.
package net.diaithir.networkcompat.packetfixer;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class PacketFixerConfig {
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.BooleanValue ALL_SIZES_UNLIMITED;
    public static final ModConfigSpec.LongValue NBT_MAX_SIZE;
    public static final ModConfigSpec.IntValue PACKET_SIZE;
    public static final ModConfigSpec.IntValue DECODER_SIZE;
    public static final ModConfigSpec.IntValue VAR_INT_21;
    public static final ModConfigSpec.IntValue VAR_INT;
    public static final ModConfigSpec.IntValue VAR_LONG;
    public static final ModConfigSpec.IntValue STRING_SIZE;
    public static final ModConfigSpec.IntValue CHUNK_PACKET_DATA;
    public static final ModConfigSpec.IntValue READ_TIMEOUT;
    public static final ModConfigSpec.IntValue LOGIN_TIMEOUT;
    public static final ModConfigSpec.IntValue KEEP_ALIVE_TIMEOUT;
    public static final ModConfigSpec.IntValue PLAYER_IDLE_TIMEOUT;
    public static final ModConfigSpec.BooleanValue FORCE_UNLIMITED_NBT;

    static {
        ModConfigSpec.Builder b = new ModConfigSpec.Builder();
        ALL_SIZES_UNLIMITED = b.comment("Ignore every size value below and use the maximum possible limits")
                .define("allSizesUnlimited", true);
        NBT_MAX_SIZE = b.comment("Max NBT size in bytes (vanilla 2097152)")
                .defineInRange("nbtMaxSize", 2097152L, 1L, Long.MAX_VALUE);
        PACKET_SIZE = b.comment("Custom payload size limit (vanilla 1048576)")
                .defineInRange("packetSize", 1048576, 1, Integer.MAX_VALUE);
        DECODER_SIZE = b.comment("Compression decoder/encoder size limit (vanilla 8388608)")
                .defineInRange("decoderSize", 8388608, 1, Integer.MAX_VALUE);
        VAR_INT_21 = b.comment("Varint21 length field size (vanilla 3)")
                .defineInRange("varInt21", 3, 1, Integer.MAX_VALUE);
        VAR_INT = b.comment("VarInt max byte size (vanilla 5)")
                .defineInRange("varInt", 5, 1, Integer.MAX_VALUE);
        VAR_LONG = b.comment("VarLong max byte size (vanilla 10)")
                .defineInRange("varLong", 10, 1, Integer.MAX_VALUE);
        STRING_SIZE = b.comment("Max string length (vanilla 32767)")
                .defineInRange("stringSize", 32767, 1, Integer.MAX_VALUE);
        CHUNK_PACKET_DATA = b.comment("Chunk packet data size limit (vanilla 2097152)")
                .defineInRange("chunkPacketData", 2097152, 1, Integer.MAX_VALUE);
        READ_TIMEOUT = b.comment("Read timeout in seconds (vanilla 30)")
                .defineInRange("readTimeout", 120, 1, Integer.MAX_VALUE);
        LOGIN_TIMEOUT = b.comment("Login timeout in seconds (vanilla 30)")
                .defineInRange("loginTimeout", 120, 1, Integer.MAX_VALUE / 20);
        KEEP_ALIVE_TIMEOUT = b.comment("Keep alive timeout in seconds (vanilla 15)")
                .defineInRange("keepAliveTimeout", 120, 1, Integer.MAX_VALUE / 1000);
        PLAYER_IDLE_TIMEOUT = b.comment("Minimum player idle timeout (minutes) applied when the server idle timeout is enabled")
                .defineInRange("playerIdleTimeout", 0, 0, Integer.MAX_VALUE);
        FORCE_UNLIMITED_NBT = b.comment("Force the NBT accounter quota to the nbt max size")
                .define("forceUnlimitedNbtEnabled", false);
        SPEC = b.build();
    }

    private PacketFixerConfig() {}

    static void apply() {
        Limits.update(
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
