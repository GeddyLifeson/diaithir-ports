package net.diaithir.animation.neo;

import net.diaithir.animation.Controller;
import net.diaithir.animation.Pose;
import net.diaithir.animation.Rules;
import net.diaithir.animation.State;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Entry point and API. Machine, block and vehicle mods call {@link #setState} when their working flag changes
 * (client side, from the synced block entity or entity data); renderers call {@link #pose} each frame.
 * Keys are "dimension|x,y,z" for blocks or "uuid" for entities, so one map serves both.
 */
@Mod(DiaithirAnimation.ID)
public final class DiaithirAnimation {
    public static final String ID = "diaithiranimation";
    private static final Logger LOG = LoggerFactory.getLogger(ID);
    private static final Map<String, Controller> LIVE = new ConcurrentHashMap<>();
    private static volatile Rules rules = Rules.parse("");

    public DiaithirAnimation() {
        try (InputStream in = DiaithirAnimation.class.getResourceAsStream("/data/diaithir_animation/rules.tsv")) {
            rules = Rules.parse(new String(in.readAllBytes(), StandardCharsets.UTF_8));
            LOG.info("Loaded {} animation rules", rules.size());
        } catch (IOException | RuntimeException e) {
            throw new IllegalStateException("animation rules failed to load", e);
        }
    }

    public static Rules rules() { return rules; }
    public static int live() { return LIVE.size(); }

    /** Sets the state of the thing keyed by key; creates its controller from the rule for typeId. No rule: no-op. */
    public static void setState(String key, String typeId, State state) {
        Controller c = LIVE.get(key);
        if (c == null) {
            var rule = rules.match(typeId);
            if (rule.isEmpty()) return;
            c = LIVE.computeIfAbsent(key, k -> new Controller(rule.get().spec()));
        }
        c.setState(state);
    }

    /** Advances and reads the pose; Pose.NONE when the key has no controller. */
    public static Pose pose(String key, float dtSeconds) {
        Controller c = LIVE.get(key);
        if (c == null) return Pose.NONE;
        c.tick(dtSeconds);
        return c.pose();
    }

    private static final Map<String, State> OVERRIDE = new ConcurrentHashMap<>();

    /** Machine mods force a state for a block ("dimension|packedPos") when a block property cannot tell: null clears. */
    public static void setOverride(String key, State state) {
        if (state == null) OVERRIDE.remove(key); else OVERRIDE.put(key, state);
    }

    public static State override(String key) { return OVERRIDE.get(key); }

    public static void forget(String key) { LIVE.remove(key); }
    public static void clear() { LIVE.clear(); }
}
