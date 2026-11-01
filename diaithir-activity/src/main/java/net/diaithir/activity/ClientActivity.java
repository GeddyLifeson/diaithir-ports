package net.diaithir.activity;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** What this client has been told about other players, by entity id. */
public final class ClientActivity {
    private static final Map<Integer, String> STATE = new ConcurrentHashMap<>();

    private ClientActivity() {}

    public static void set(int entityId, String activity) {
        if (activity.isEmpty()) STATE.remove(entityId); else STATE.put(entityId, activity);
    }

    public static String get(int entityId) {
        return STATE.get(entityId);
    }

    public static void clear() {
        STATE.clear();
    }
}
