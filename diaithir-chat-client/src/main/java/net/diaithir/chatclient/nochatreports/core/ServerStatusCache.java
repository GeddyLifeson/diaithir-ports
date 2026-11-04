// Ported from No Chat Reports (WTFPL) https://github.com/Aizistral-Studios/No-Chat-Reports
// Modifications: relocated package; default false instead of NPE on unboxing.
package net.diaithir.chatclient.nochatreports.core;

public final class ServerStatusCache {
    private static final ThreadLocal<Boolean> PREVENTS_REPORTS = ThreadLocal.withInitial(() -> Boolean.FALSE);

    private ServerStatusCache() {}

    public static boolean doesPreventReports() {
        return PREVENTS_REPORTS.get();
    }

    public static void setPreventsReports(boolean value) {
        PREVENTS_REPORTS.set(value);
    }
}
