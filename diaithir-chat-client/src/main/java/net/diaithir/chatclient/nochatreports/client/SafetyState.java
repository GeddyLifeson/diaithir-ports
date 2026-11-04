// Ported from No Chat Reports (WTFPL) https://github.com/Aizistral-Studios/No-Chat-Reports
// Modifications: trimmed ServerSafetyState; only the signing-suppression related state is kept.
package net.diaithir.chatclient.nochatreports.client;

import java.util.concurrent.atomic.AtomicBoolean;

public final class SafetyState {
    private static final AtomicBoolean ALLOW_CHAT_SIGNING = new AtomicBoolean(false);
    private static volatile boolean signingKeysPresent = false;
    private static volatile boolean realms = false;
    private static volatile boolean singleplayer = false;
    private static volatile boolean serverPreventsReports = false;

    private SafetyState() {}

    public static boolean allowChatSigning() {
        return ALLOW_CHAT_SIGNING.get();
    }

    public static void setAllowChatSigning(boolean allow) {
        ALLOW_CHAT_SIGNING.set(allow);
    }

    public static boolean areSigningKeysPresent() {
        return signingKeysPresent;
    }

    public static void setSigningKeysPresent(boolean present) {
        signingKeysPresent = present;
    }

    public static boolean isOnRealms() {
        return realms;
    }

    public static void setOnRealms(boolean value) {
        realms = value;
    }

    public static boolean isInSingleplayer() {
        return singleplayer;
    }

    public static void setInSingleplayer(boolean value) {
        singleplayer = value;
    }

    public static boolean serverPreventsReports() {
        return serverPreventsReports;
    }

    public static void setServerPreventsReports(boolean value) {
        serverPreventsReports = value;
    }

    public static void reset() {
        ALLOW_CHAT_SIGNING.set(false);
        realms = false;
        singleplayer = false;
        serverPreventsReports = false;
    }
}
