// Ported from Packet Fixer (MIT) https://github.com/TonimatasDEV/PacketFixer
// Modifications: Plain value holder (safe to read from mixins before/after config load) fed by ModConfigEvent instead of a properties file.
package net.diaithir.networkcompat.packetfixer;

public final class Limits {
    private static volatile boolean allSizesUnlimited = true;
    private static volatile long nbtMaxSize = 2097152L;
    private static volatile int packetSize = 1048576;
    private static volatile int decoderSize = 8388608;
    private static volatile int varInt21 = 3;
    private static volatile int varInt = 5;
    private static volatile int varLong = 10;
    private static volatile int stringSize = 32767;
    private static volatile int chunkPacketData = 2097152;
    private static volatile int readTimeout = 120;
    private static volatile int loginTimeout = 120;
    private static volatile int keepAliveTimeout = 120;
    private static volatile int playerIdleTimeout = 0;
    private static volatile boolean forceUnlimitedNbtEnabled = false;

    private Limits() {}

    static void update(boolean unlimited, long nbt, int packet, int decoder, int v21, int vi, int vl, int str,
                       int chunk, int read, int login, int keepAlive, int idle, boolean forceNbt) {
        allSizesUnlimited = unlimited;
        nbtMaxSize = nbt;
        packetSize = packet;
        decoderSize = decoder;
        varInt21 = v21;
        varInt = vi;
        varLong = vl;
        stringSize = str;
        chunkPacketData = chunk;
        readTimeout = read;
        loginTimeout = login;
        keepAliveTimeout = keepAlive;
        playerIdleTimeout = idle;
        forceUnlimitedNbtEnabled = forceNbt;
    }

    public static long getNbtMaxSize() {
        return allSizesUnlimited ? Long.MAX_VALUE : nbtMaxSize;
    }

    public static int getPacketSize() {
        return allSizesUnlimited ? Integer.MAX_VALUE : packetSize;
    }

    public static int getDecoderSize() {
        return allSizesUnlimited ? Integer.MAX_VALUE : decoderSize;
    }

    public static int getVarInt21Size() {
        return allSizesUnlimited ? Integer.MAX_VALUE : varInt21;
    }

    public static int getVarIntSize() {
        return allSizesUnlimited ? (Integer.MAX_VALUE / 2 - 1) : varInt;
    }

    public static int getVarLong() {
        return allSizesUnlimited ? (Integer.MAX_VALUE / 2 - 1) : varLong;
    }

    public static int getStringSize() {
        return allSizesUnlimited ? 327670000 : stringSize;
    }

    public static int getChunkPacketData() {
        return allSizesUnlimited ? Integer.MAX_VALUE : chunkPacketData;
    }

    public static int getReadTimeout() {
        return readTimeout;
    }

    public static int getLoginTimeout() {
        return loginTimeout;
    }

    public static int getKeepAliveTimeout() {
        return keepAliveTimeout;
    }

    public static int getPlayerIdleTimeout() {
        return playerIdleTimeout;
    }

    public static boolean isForceUnlimitedNbtEnabled() {
        return forceUnlimitedNbtEnabled;
    }

    public static String getPayloadMessage() {
        return "Payload may not be larger than " + getPacketSize() + " bytes. You can modify it in the Packet Fixer config.";
    }
}
