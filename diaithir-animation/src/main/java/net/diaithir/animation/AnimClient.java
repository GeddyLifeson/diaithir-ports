package net.diaithir.animation;

import com.mojang.blaze3d.platform.InputConstants;
import com.zigythebird.playeranim.animation.PlayerAnimResources;
import com.zigythebird.playeranim.animation.PlayerAnimationController;
import com.zigythebird.playeranim.api.PlayerAnimationAccess;
import com.zigythebird.playeranim.api.PlayerAnimationFactory;
import com.zigythebird.playeranimcore.animation.Animation;
import com.zigythebird.playeranimcore.animation.RawAnimation;
import com.zigythebird.playeranimcore.api.firstPerson.FirstPersonConfiguration;
import com.zigythebird.playeranimcore.api.firstPerson.FirstPersonMode;
import com.zigythebird.playeranimcore.enums.PlayState;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.common.NeoForge;

/**
 * Client side. Two PAL layers: "main" (weapon and tool clips, both arms) and "off" (shield arm, so it can stack with a main-hand clip).
 * Every player, local or remote, is read each tick from what the client already knows (held item, swing, use state, sneak, sprint):
 * draw on switch, hold/aim/sprint loops for guns, swing combos for melee and tools, nock/draw/release for bows, load/aim/fire for crossbows,
 * shield raise/lower, cast loops for staffs. Gun fire/reload/dry/inspect and skill casts come from the server as Net.Play.
 */
public final class AnimClient {
    static final Identifier MAIN = Identifier.fromNamespaceAndPath(DiaithirAnimation.ID, "main");
    static final Identifier OFF = Identifier.fromNamespaceAndPath(DiaithirAnimation.ID, "off");
    private static KeyMapping inspect;

    /** Ordered one-shot swing clips per class; the combo index walks through them. */
    private static final Map<String, List<String>> SWINGS = Map.ofEntries(
            Map.entry("sword", List.of("swing1", "swing2", "swing3")), Map.entry("axe", List.of("swing1", "swing2", "swing3")),
            Map.entry("spear", List.of("thrust")), Map.entry("dagger", List.of("stab", "slash")), Map.entry("mace", List.of("overhead", "smash")),
            Map.entry("staff", List.of("release")), Map.entry("pickaxe", List.of("mine")), Map.entry("shovel", List.of("dig")),
            Map.entry("hoe", List.of("till")), Map.entry("shears", List.of("snip")));
    /** Ticks after a swing in which the next swing continues the combo. */
    private static final int COMBO_TICKS = 25;

    private static final class S {
        String item = "", mode = "";
        boolean swinging, using, charged, shield;
        int combo;
        long lastSwing = -1000, lastFire = -1000;
    }
    private static final Map<Integer, S> STATE = new ConcurrentHashMap<>();

    static void init(IEventBus bus) {
        bus.addListener(FMLClientSetupEvent.class, e -> e.enqueueWork(() -> {
            PlayerAnimationFactory.ANIMATION_DATA_FACTORY.registerFactory(MAIN, 1500, AnimClient::controller);
            PlayerAnimationFactory.ANIMATION_DATA_FACTORY.registerFactory(OFF, 1400, AnimClient::controller);
        }));
        bus.addListener(RegisterKeyMappingsEvent.class, e -> {
            inspect = new KeyMapping("key." + DiaithirAnimation.ID + ".inspect", InputConstants.Type.KEYSYM, org.lwjgl.glfw.GLFW.GLFW_KEY_H,
                    KeyMapping.Category.register(Identifier.fromNamespaceAndPath(DiaithirAnimation.ID, "main")));
            e.register(inspect);
        });
        NeoForge.EVENT_BUS.addListener(ClientTickEvent.Post.class, e -> tick());
    }

    /** First person: while a clip plays, the third-person arms and held items are shown so gun and swing clips are visible from the player's own view. */
    private static PlayerAnimationController controller(Avatar a) {
        var c = new PlayerAnimationController(a, (ctl, d, s) -> PlayState.STOP);
        c.setFirstPersonConfiguration(new FirstPersonConfiguration(true, true, true, true));
        c.setFirstPersonModeHandler(ctl -> ctl.isPlayingTriggeredAnimation() ? FirstPersonMode.THIRD_PERSON_MODEL : FirstPersonMode.NONE);
        return c;
    }

    private static PlayerAnimationController layer(Avatar a, Identifier id) {
        try {
            return PlayerAnimationAccess.getPlayerAnimationLayer(a, id) instanceof PlayerAnimationController c ? c : null;
        } catch (RuntimeException ex) {
            return null;
        }
    }

    private static Animation clip(String name) {
        return name == null ? null : PlayerAnimResources.getAnimation(Identifier.fromNamespaceAndPath(DiaithirAnimation.ID, name));
    }

    /** Plays an optional one-shot, then an optional loop, on a layer. Unknown clips are skipped; returns false when nothing could play. */
    static boolean play(Avatar a, Identifier layer, String once, String loop) {
        PlayerAnimationController c = layer(a, layer);
        if (c == null) return false;
        RawAnimation r = RawAnimation.begin();
        Animation o = clip(once), l = clip(loop);
        if (o != null) r.thenPlay(o);
        if (l != null) r.thenLoop(l);
        if (r.getStageCount() == 0) return false;
        c.triggerAnimation(r);
        return true;
    }

    private static void stop(Avatar a, Identifier layer) {
        PlayerAnimationController c = layer(a, layer);
        if (c != null) c.stopTriggeredAnimation();
    }

    /** A server-sent clip. After it the current gun loop resumes. */
    static void oneShot(Avatar a, String name) {
        if (name.startsWith("body.")) { BodyClient.oneShot(a, name); return; }
        S s = STATE.computeIfAbsent(a.getId(), k -> new S());
        long now = a.level().getGameTime();
        boolean gun = name.startsWith("gun.");
        if (gun) { Kind gk = Kind.of(a.getMainHandItem()); if (gk != null && gk.gun()) GunFx.clip(a, name, gk.cls()); }
        if (name.endsWith(".fire")) {
            if (now - s.lastFire < 3) return; // autos: keep the aim loop, no restart jitter
            s.lastFire = now;
        }
        String loop = null;
        if (gun) {
            Kind k = Kind.of(a.getMainHandItem());
            if (k != null && k.gun()) loop = k.clip(a.getMainHandItem().getItem(), loopFor(a, s));
        } else if (name.equals("cast.skill")) {
            loop = null;
        }
        play(a, MAIN, name, loop);
    }

    private static String loopFor(Avatar a, S s) {
        return a.isSprinting() ? "sprint" : a.isShiftKeyDown() ? "aim" : "hold";
    }

    private static void tick() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.isPaused()) return;
        if (inspect != null && mc.screen == null) {
            while (inspect.consumeClick()) {
                Kind k = mc.player == null ? null : Kind.of(mc.player.getMainHandItem());
                if (k != null && k.gun()) ClientPacketDistributor.sendToServer(new Net.Inspect());
            }
        }
        var ids = new java.util.HashSet<Integer>();
        for (Player p : mc.level.players()) {
            ids.add(p.getId());
            try { update(p, STATE.computeIfAbsent(p.getId(), k -> new S())); } catch (RuntimeException ignored) { }
        }
        STATE.keySet().retainAll(ids);
    }

    private static void update(Player p, S s) {
        long now = p.level().getGameTime();
        ItemStack hand = p.getMainHandItem();
        Kind k = Kind.of(hand);
        String key = k == null ? "" : (k.gun() ? "gun." : "") + k.cls() + ":" + hand.getItem();
        boolean changed = !key.equals(s.item);
        s.item = key;
        // ---- shield (off layer) ----
        Kind ok = Kind.of(p.getOffhandItem());
        boolean shield = p.isBlocking() && ok != null && !ok.gun() && ok.cls().equals("shield") || p.isBlocking() && k != null && !k.gun() && k.cls().equals("shield");
        if (shield != s.shield) {
            s.shield = shield;
            if (shield) play(p, OFF, "shield.raise", "shield.hold");
            else play(p, OFF, "shield.lower", null);
        }
        // ---- seats: riders of turret / tank / mounted gun / cannon entities ----
        String seat = null;
        if (p.getVehicle() != null) for (String sk : List.of("turret", "driver", "mounted_gun", "cannon"))
            if (p.getVehicle().is(net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.ENTITY_TYPE,
                    Identifier.fromNamespaceAndPath(DiaithirAnimation.ID, "seat/" + sk)))) { seat = "seat." + sk; break; }
        if (seat != null) {
            if (!seat.equals(s.mode)) { s.mode = seat; s.item = ""; play(p, MAIN, null, seat); }
            return;
        } else if (s.mode.startsWith("seat.")) { s.mode = ""; stop(p, MAIN); }
        // ---- main layer ----
        if (k == null) {
            if (changed) { stop(p, MAIN); s.mode = ""; }
            s.swinging = p.swinging;
            return;
        }
        if (k.gun()) {
            String mode = loopFor(p, s);
            if (changed) { s.mode = mode; play(p, MAIN, k.clip(hand.getItem(), "draw"), k.clip(hand.getItem(), mode)); }
            else if (!mode.equals(s.mode)) { s.mode = mode; play(p, MAIN, null, k.clip(hand.getItem(), mode)); }
            return;
        }
        String c = k.cls();
        if (changed) { s.mode = ""; s.using = false; s.charged = false; stop(p, MAIN); if (hasClip(c, "draw")) play(p, MAIN, c + ".draw", null); }
        switch (c) {
            case "bow" -> {
                boolean use = p.isUsingItem();
                if (use && !s.using) play(p, MAIN, "bow.nock", "bow.hold");
                else if (!use && s.using) play(p, MAIN, "bow.release", null);
                s.using = use;
            }
            case "crossbow" -> {
                boolean use = p.isUsingItem();
                var ch = hand.get(DataComponents.CHARGED_PROJECTILES);
                boolean charged = ch != null && !ch.isEmpty();
                if (use && !s.using && !charged) play(p, MAIN, "crossbow.load", null);
                if (charged && !s.charged) play(p, MAIN, null, "crossbow.aim");
                if (!charged && s.charged) play(p, MAIN, "crossbow.fire", null);
                s.using = use; s.charged = charged;
            }
            case "staff" -> {
                boolean use = p.isUsingItem();
                if (use && !s.using) play(p, MAIN, "staff.cast_raise", "staff.channel");
                else if (!use && s.using) play(p, MAIN, "staff.release", null);
                s.using = use;
            }
            case "shield" -> { }
            default -> { }
        }
        boolean sw = p.swinging;
        if (sw && !s.swinging && SWINGS.containsKey(c) && !(c.equals("staff") && p.isUsingItem())) {
            List<String> seq = SWINGS.get(c);
            s.combo = now - s.lastSwing <= COMBO_TICKS ? (s.combo + 1) % seq.size() : 0;
            s.lastSwing = now;
            play(p, MAIN, c + "." + seq.get(s.combo), null);
        }
        s.swinging = sw;
    }

    private static boolean hasClip(String cls, String action) { return clip(cls + "." + action) != null; }

    private AnimClient() {}
}
