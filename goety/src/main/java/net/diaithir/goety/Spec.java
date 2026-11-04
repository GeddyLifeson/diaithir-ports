package net.diaithir.goety;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;

/** Reads the generated registration tables in resources/goety_spec (written by tools/gen.py). */
public final class Spec {
    private Spec() {}

    public record BlockSpec(String id, String kind, String like, String set, String base, boolean axe) {}

    public static <T> T read(String name, TypeToken<T> type) {
        try (Reader r = new InputStreamReader(Spec.class.getResourceAsStream("/goety_spec/" + name))) {
            return new Gson().fromJson(r, type.getType());
        } catch (Exception e) {
            throw new IllegalStateException(name + " missing", e);
        }
    }

    public static List<BlockSpec> blocks() {
        return read("blocks.json", new TypeToken<List<BlockSpec>>() {});
    }
}
