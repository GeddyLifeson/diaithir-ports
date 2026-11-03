package net.diaithir.bosses;

import com.google.gson.Gson;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.server.MinecraftServer;

/** Boss difficulty modes from data/diaithirbosses/difficulty.json. The world's mode is stored in AscendantData and only goes up. */
public final class Difficulty {
    public static final class Mode {
        public String id = "standard", quest;
        public double hp = 1, damage = 1, cooldown = 1, elite_chance = 1, loot = 1, xp = 1;
        public boolean no_regen_in_arena, one_life_per_attempt;
    }

    private static final class File { List<Mode> modes = new ArrayList<>(); }

    private static final List<Mode> MODES = new ArrayList<>();
    private static final Mode STANDARD = new Mode();

    static {
        try (var r = new InputStreamReader(Difficulty.class.getResourceAsStream("/data/" + DiaithirBosses.ID + "/difficulty.json"), StandardCharsets.UTF_8)) {
            MODES.addAll(new Gson().fromJson(r, File.class).modes);
        } catch (Exception e) {
            throw new IllegalStateException("difficulty.json missing", e);
        }
    }

    public static List<Mode> all() { return MODES; }

    public static int rank(String id) {
        for (int i = 0; i < MODES.size(); i++) if (MODES.get(i).id.equals(id)) return i;
        return -1;
    }

    public static Mode byId(String id) {
        int r = rank(id);
        return r < 0 ? STANDARD : MODES.get(r);
    }

    public static Mode current(MinecraftServer server) {
        return server == null ? STANDARD : byId(AscendantData.get(server).mode());
    }

    private Difficulty() {}
}
