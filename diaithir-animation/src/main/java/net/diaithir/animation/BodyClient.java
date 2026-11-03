package net.diaithir.animation;

import com.zigythebird.playeranim.animation.PlayerAnimResources;
import com.zigythebird.playeranim.animation.PlayerAnimationController;
import com.zigythebird.playeranim.api.PlayerAnimationAccess;
import com.zigythebird.playeranim.api.PlayerAnimationFactory;
import com.zigythebird.playeranimcore.animation.Animation;
import com.zigythebird.playeranimcore.animation.RawAnimation;
import com.zigythebird.playeranimcore.api.firstPerson.FirstPersonConfiguration;
import com.zigythebird.playeranimcore.api.firstPerson.FirstPersonMode;
import com.zigythebird.playeranimcore.enums.PlayState;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;

/**
 * Vanilla-body pose layer (clean-room replacement for Not Enough Animations and the firstperson mod's poses), on Player Animation Library.
 * One PAL layer "body" under the weapon layers of AnimClient. Every player, local or remote, is read each tick from what the client already knows
 * (pose, water, climbing, riding, gliding, fall, sleeping, held and used item, fishing hook, hurt/death) and a clip is started only when the state changes.
 * Clips are data (body.json); held-item kinds come from item tags (BodyKind). Server one-shots (container opened, item crafted, ...) arrive as Net.Play "body.*".
 */
public final class BodyClient {
    static final Identifier BODY = Identifier.fromNamespaceAndPath(DiaithirAnimation.ID, "body");
    private static final int LAND_SOFT = 3, LAND_HARD = 9, FALL_LOOP = 4;

    private static final class S {
        String state = "";          // current looping/held state clip (without "act."/"use." prefix handling, full clip id)
        long lockUntil;             // a one-shot is playing until this game time
        boolean ground = true, swinging, fishing, flying, using;
        int hurt;
        double maxFall;
        String item = "";
    }
    private static final Map<Integer, S> STATE = new ConcurrentHashMap<>();

    static void init(IEventBus bus) {
        bus.addListener(FMLClientSetupEvent.class, e -> e.enqueueWork(() ->
                PlayerAnimationFactory.ANIMATION_DATA_FACTORY.registerFactory(BODY, 1000, BodyClient::controller)));
        NeoForge.EVENT_BUS.addListener(ClientTickEvent.Post.class, e -> tick());
    }

    /** Own view: the arms and held item show while a clip that uses them plays; otherwise vanilla first person. */
    private static PlayerAnimationController controller(Avatar a) {
        var c = new PlayerAnimationController(a, (ctl, d, s) -> PlayState.STOP);
        c.setFirstPersonConfiguration(new FirstPersonConfiguration(true, true, true, true));
        c.setFirstPersonModeHandler(ctl -> ctl.isPlayingTriggeredAnimation() && armClip(a) ? FirstPersonMode.THIRD_PERSON_MODEL : FirstPersonMode.NONE);
        return c;
    }

    private static boolean armClip(Avatar a) {
        S s = STATE.get(a.getId());
        if (s == null) return false;
        String n = s.state;
        return n.startsWith("use.") || n.startsWith("act.hold") || n.equals("act.interact") || n.equals("act.mine") || n.equals("act.place");
    }

    private static PlayerAnimationController layer(Avatar a) {
        try {
            return PlayerAnimationAccess.getPlayerAnimationLayer(a, BODY) instanceof PlayerAnimationController c ? c : null;
        } catch (RuntimeException ex) {
            return null;
        }
    }

    static Animation clip(String name) {
        return name == null ? null : PlayerAnimResources.getAnimation(Identifier.fromNamespaceAndPath(DiaithirAnimation.ID, name));
    }

    /** once then (optionally) a loop; false when no clip could play. */
    private static boolean play(Avatar a, String once, String loop) {
        PlayerAnimationController c = layer(a);
        if (c == null) return false;
        RawAnimation r = RawAnimation.begin();
        Animation o = clip(once), l = clip(loop);
        if (o != null) r.thenPlay(o);
        if (l != null) r.thenLoop(l);
        if (r.getStageCount() == 0) return false;
        c.triggerAnimation(r);
        return true;
    }

    private static void stop(Avatar a) {
        PlayerAnimationController c = layer(a);
        if (c != null) c.stopTriggeredAnimation();
    }

    /** A server-sent "body.<name>" one-shot (container opened, crafted, enchant, trade, brew, pick up, drop, place, respawn). */
    static void oneShot(Avatar a, String name) {
        String clip = "act." + name.substring("body.".length());
        S s = STATE.computeIfAbsent(a.getId(), k -> new S());
        Animation an = clip(clip);
        if (an == null) return;
        s.lockUntil = a.level().getGameTime() + Math.round(an.length() * 20);
        s.state = clip;
        play(a, clip, null);
    }

    private static void tick() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.isPaused()) return;
        var ids = new HashSet<Integer>();
        for (Player p : mc.level.players()) {
            ids.add(p.getId());
            try { update(p, STATE.computeIfAbsent(p.getId(), k -> new S())); } catch (RuntimeException ignored) { }
        }
        STATE.keySet().retainAll(ids);
    }

    private static boolean chainHas(Entity e, String... parts) {
        for (Class<?> c = e.getClass(); c != null && c != Object.class; c = c.getSuperclass()) {
            String n = c.getSimpleName().toLowerCase();
            for (String p : parts) if (n.contains(p)) return true;
        }
        return false;
    }

    private static String ride(Entity v) {
        if (chainHas(v, "boat", "raft", "ship")) return "act.ride_boat";
        if (chainHas(v, "minecart", "cart", "train")) return "act.ride_cart";
        if (chainHas(v, "horse", "donkey", "mule", "camel", "llama", "pig", "strider")) return "act.ride_horse";
        if (chainHas(v, "seat", "chair", "turret", "vehicle")) return "act.sit_seat";
        return "act.ride_mount";
    }

    private static String climb(Player p) {
        var b = p.level().getBlockState(p.blockPosition());
        String id = net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(b.getBlock()).getPath();
        if (id.contains("scaffold")) return "act.scaffold";
        if (id.contains("vine") || id.contains("weeping") || id.contains("twisting") || id.contains("kelp") || id.contains("cave_vines")) return "act.vine";
        if (b.is(BlockTags.CLIMBABLE) && id.contains("ladder")) return "act.ladder";
        return "act.climb";
    }

    /** Clip for an in-use item, one-shot and loop. Null = none. */
    private static String[] useClip(String kind) {
        return switch (kind) {
            case "food" -> new String[]{null, "use.food.eat"};
            case "drink" -> new String[]{"use.drink.tilt", "use.drink.sip"};
            case "horn" -> new String[]{null, "use.horn.play"};
            case "spyglass" -> new String[]{"use.spyglass.raise", "use.spyglass.read"};
            case "map" -> new String[]{"use.map.raise", "use.map.read"};
            case "scroll" -> new String[]{"use.scroll.open", "use.scroll.read"};
            default -> null;
        };
    }

    private static void set(Avatar a, S s, String state, String once, String loop) {
        if (state.equals(s.state)) return;
        s.state = state;
        if (state.isEmpty()) { stop(a); return; }
        play(a, once, loop);
    }

    private static void update(Player p, S s) {
        long now = p.level().getGameTime();
        // ---- one-shots by edge (hurt, land, jump, throw, tool, mount, rod) ----
        boolean ground = p.onGround();
        if (!ground && !p.isInWater()) s.maxFall = Math.max(s.maxFall, p.fallDistance);
        if (p.deathTime > 0 || !p.isAlive()) { set(p, s, "act.die", "act.die", null); s.lockUntil = now + 100; return; }
        if (s.state.equals("act.die")) s.state = "";
        boolean locked = now < s.lockUntil;
        if (!locked) {
            if (p.hurtTime > s.hurt) { shot(p, s, "act.hurt"); }
            else if (ground && !s.ground && s.maxFall >= LAND_SOFT && !p.isInWater()) { shot(p, s, s.maxFall >= LAND_HARD ? "act.land_hard" : "act.land_soft"); }
            else if (!ground && s.ground && p.getDeltaMovement().y > 0.2 && !p.isPassenger() && !p.onClimbable() && !p.isInWater()) { shot(p, s, "act.jump"); }
            else if (!p.isFallFlying() && s.flying) { s.flying = false; shot(p, s, "use.wings.fold"); }
            else if (p.isFallFlying() && !s.flying) { s.flying = true; shot(p, s, "use.wings.deploy"); }
        }
        if (ground) s.maxFall = 0;
        s.hurt = p.hurtTime; s.ground = ground;
        ItemStack main = p.getMainHandItem(), use = p.isUsingItem() ? p.getUseItem() : ItemStack.EMPTY;
        String mk = BodyKind.of(main), uk = BodyKind.of(use);
        boolean sw = p.swinging;
        if (!locked && sw && !s.swinging && !p.isPassenger()) {
            if ("throwable".equals(mk)) { playTwo(p, s, "use.throwable.windup", "use.throwable.throw"); }
            else if ("tool".equals(mk)) { shot(p, s, "use.tool.use"); }
            else if ("mount".equals(mk)) { shot(p, s, "use.mount.equip"); }
            else if (mk == null && main.isEmpty() && p.getVehicle() == null && !p.isUsingItem()) { shot(p, s, "act.punch"); }
        }
        s.swinging = sw;
        boolean fishing = p.fishing != null;
        if (fishing != s.fishing) { s.fishing = fishing; if (fishing) { s.state = "use.rod.cast"; s.lockUntil = now + 12; play(p, "use.rod.cast", "use.rod.wait"); } else { shot(p, s, "use.rod.reel"); } }
        if (fishing) return;
        if (now < s.lockUntil) return;
        // ---- held state, by priority ----
        if (p.isSleeping()) { set(p, s, "act.sleep", null, "act.sleep"); return; }
        Entity v = p.getVehicle();
        if (v != null) { String c = ride(v); set(p, s, c, null, c); return; }
        if (p.isFallFlying()) { set(p, s, "use.wings.glide", null, "use.wings.glide"); return; }
        if (p.isUsingItem() && uk != null) {
            String[] c = useClip(uk);
            if (c != null) { set(p, s, "use." + uk, c[0], c[1]); s.using = true; return; }
        }
        if (s.using) { s.using = false; if (s.state.equals("use.scroll")) { shot(p, s, "use.scroll.cast"); return; } s.state = ""; stop(p); }
        if (p.onClimbable() && !ground) { String c = climb(p); set(p, s, c, null, c); return; }
        if (p.isSwimming()) { String c = p.isSprinting() ? "act.swim_sprint" : "act.swim"; set(p, s, c, null, c); return; }
        if (p.getPose() == Pose.SWIMMING || p.isVisuallyCrawling()) { set(p, s, "act.crawl", null, "act.crawl"); return; }
        if (p.isInWater() && !ground) { set(p, s, "act.tread", null, "act.tread"); return; }
        if (p.isInWater() && ground && p.getDeltaMovement().horizontalDistanceSqr() > 1.0E-4) { set(p, s, "act.wade", null, "act.wade"); return; }
        if (!ground && p.fallDistance > FALL_LOOP && p.getDeltaMovement().y < -0.5) { set(p, s, "act.fall", null, "act.fall"); return; }
        boolean moving = p.getDeltaMovement().horizontalDistanceSqr() > 2.5E-4;
        if (p.isCrouching()) { String c = moving ? "act.lean_sneak_walk" : "act.lean_sneak"; set(p, s, c, null, c); return; }
        if (moving && ground) {
            String c = p.isSprinting() ? "act.lean_sprint" : p.getDeltaMovement().horizontalDistanceSqr() > 0.02 ? "act.lean_run" : "act.lean_walk";
            set(p, s, c, null, c); return;
        }
        String held = heldClip(p);
        if (held != null) { set(p, s, held, null, held); return; }
        set(p, s, "", null, null);
    }

    private static String heldClip(Player p) {
        String m = BodyKind.of(p.getMainHandItem());
        if ("map".equals(m)) return "act.hold_map";
        String id = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(p.getMainHandItem().getItem()).getPath();
        if (id.endsWith("torch") || id.endsWith("lantern")) return "act.hold_torch";
        return null;
    }

    private static void shot(Avatar a, S s, String clip) {
        Animation an = clip(clip);
        if (an == null) return;
        s.lockUntil = a.level().getGameTime() + Math.round(an.length() * 20);
        s.state = clip;
        play(a, clip, null);
    }

    private static void playTwo(Avatar a, S s, String first, String second) {
        Animation f = clip(first), t = clip(second);
        if (f == null || t == null) return;
        s.lockUntil = a.level().getGameTime() + Math.round((f.length() + t.length()) * 20);
        s.state = first;
        PlayerAnimationController c = layer(a);
        if (c != null) c.triggerAnimation(RawAnimation.begin().thenPlay(f).thenPlay(t));
    }

    private BodyClient() {}
}
