package net.diaithir.worldsys.economy;

import com.google.gson.Gson;

/** The economy's save format: EconState as JSON. Kept apart from EconSave so it runs without Minecraft on the classpath. */
public final class EconJson {
    private EconJson() {}

    private static final Gson GSON = new Gson();

    public static String toJson(EconState s) { return GSON.toJson(s); }

    public static EconState fromJson(String json) {
        EconState s = json == null || json.isEmpty() ? null : GSON.fromJson(json, EconState.class);
        return s != null ? s : new EconState();
    }
}
