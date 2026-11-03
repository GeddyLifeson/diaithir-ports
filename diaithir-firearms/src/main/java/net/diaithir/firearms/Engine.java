package net.diaithir.firearms;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundPlayerRotationPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.entity.projectile.ProjectileUtil;

/** Server-side gun rules: fire, reload, attachments. The client only says "reload". */
public final class Engine {
    private static final class S { long lastShot = -100, lastCall = -100, reloadEnd = -1; int reloadSlot; }
    private static final Map<UUID, S> STATE = new ConcurrentHashMap<>();
    public static int defaultAge = 9;

    private static S st(Player p) { return STATE.computeIfAbsent(p.getUUID(), k -> new S()); }
    static void forget(Player p) { STATE.remove(p.getUUID()); }

    public static int mag(ItemStack s) { return s.getOrDefault(DiaithirFirearms.MAG.get(), 0); }
    public static void setMag(ItemStack s, int n) { s.set(DiaithirFirearms.MAG.get(), n); }
    public static List<String> atts(ItemStack s) { return s.getOrDefault(DiaithirFirearms.ATT.get(), List.of()); }

    public static float modifier(ItemStack s, String kind) {
        float v = 1f;
        for (String a : atts(s)) { var d = Defs.ATTS.get(a); if (d != null && d.kind().equals(kind)) v *= d.value(); }
        return v;
    }

    /** Multiplier a parts station stored on the gun (custom data diaithirgear.<key&gt;); 1 when the gun was not assembled there. */
    static float crafted(ItemStack s, String key) {
        var cd = s.get(net.minecraft.core.component.DataComponents.CUSTOM_DATA);
        if (cd == null) return 1f;
        return cd.copyTag().getCompound("diaithirgear").flatMap(c -> c.getFloat(key)).orElse(1f);
    }

    public static float zoom(ItemStack s) {
        float z = 1f;
        for (String a : atts(s)) { var d = Defs.ATTS.get(a); if (d != null && d.kind().equals("sight")) z = Math.max(z, d.value()); }
        return z;
    }

    public static int ageOf(Player p) {
        try {
            var sb = p.level().getScoreboard();
            var obj = sb.getObjective("age");
            if (obj != null) { var i = sb.getPlayerScoreInfo(p, obj); if (i != null && i.value() > 0) return i.value(); }
        } catch (Exception ignored) { }
        return defaultAge;
    }

    private static Item ammoItem(Defs.Gun g) { return BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath(DiaithirFirearms.ID, g.ammo())); }

    public static int ammoCount(ServerPlayer p, Defs.Gun g) {
        Item a = ammoItem(g); int n = 0;
        var inv = p.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) { ItemStack s = inv.getItem(i); if (s.is(a)) n += s.getCount(); }
        return n;
    }

    private static int takeAmmo(ServerPlayer p, Defs.Gun g, int want) {
        if (p.isCreative()) return want;
        Item a = ammoItem(g); int got = 0;
        var inv = p.getInventory();
        for (int i = 0; i < inv.getContainerSize() && got < want; i++) {
            ItemStack s = inv.getItem(i);
            if (!s.is(a)) continue;
            int t = Math.min(s.getCount(), want - got); s.shrink(t); got += t;
        }
        return got;
    }

    private static void say(ServerPlayer p, Component c) { p.sendSystemMessage(c, true); }

    /** Called every tick while the use key is held with a gun. */
    static void trigger(ServerPlayer sp, ItemStack stack, Defs.Gun g, int used) {
        S s = st(sp); long now = sp.level().getGameTime();
        boolean fresh = now - s.lastCall > 1; s.lastCall = now;
        if (s.reloadEnd >= 0) return;
        if (!g.auto() && !fresh) return;
        if (now - s.lastShot < g.cooldown()) return;
        if (mag(stack) <= 0) {
            if (fresh) { if (ammoCount(sp, g) > 0 || sp.isCreative()) reload(sp); else say(sp, Component.translatable("diaithirfirearms.empty")); }
            if (fresh) sp.level().playSound(null, sp.getX(), sp.getY(), sp.getZ(), DiaithirFirearms.sound("dry_fire"), SoundSource.PLAYERS, 0.6f, 1f);
            return;
        }
        s.lastShot = now;
        fire(sp, stack, g);
    }

    /** Fires one trigger pull (all pellets); public for the selftest. */
    public static int fire(ServerPlayer sp, ItemStack stack, Defs.Gun g) {
        ServerLevel lv = sp.level();
        setMag(stack, mag(stack) - 1);
        boolean under = ageOf(sp) < g.ageNo();
        float spread = g.spread() * (under ? 2f : 1f) * (sp.isShiftKeyDown() ? 0.6f : 1f) * modifier(stack, "grip") * crafted(stack, "spread_mul");
        float quiet = modifier(stack, "muzzle_quiet");
        Vec3 eye = sp.getEyePosition();
        int hits = 0;
        for (int i = 0; i < g.pellets(); i++) {
            float yaw = sp.getYRot() + (float) (sp.getRandom().nextGaussian() * spread * 0.5);
            float pitch = sp.getXRot() + (float) (sp.getRandom().nextGaussian() * spread * 0.5);
            Vec3 dir = Vec3.directionFromRotation(pitch, yaw);
            hits += shoot(sp, lv, g, eye, dir, quiet, i == 0) ? 1 : 0;
        }
        Vec3 muzzle = eye.add(Vec3.directionFromRotation(sp.getXRot(), sp.getYRot()).scale(0.8));
        lv.sendParticles(ParticleTypes.SMOKE, muzzle.x, muzzle.y - 0.15, muzzle.z, 2, 0.02, 0.02, 0.02, 0.0);
        float vol = switch (g.cls()) { case "pistol" -> 1.2f; case "smg" -> 1.4f; case "shotgun", "launcher" -> 2.2f; case "sniper" -> 3f; case "energy" -> 1.0f; default -> 2f; } * quiet * quiet * quiet;
        String key = switch (g.cls()) { case "pistol" -> "shot_pistol"; case "smg" -> "shot_smg"; case "shotgun" -> "shot_shotgun"; case "sniper" -> "shot_sniper";
            case "lmg" -> "shot_lmg"; case "launcher" -> "shot_launcher"; case "energy" -> "shot_energy"; default -> "shot_rifle"; };
        lv.playSound(null, sp.getX(), sp.getY(), sp.getZ(), DiaithirFirearms.sound(key), SoundSource.PLAYERS, vol, 0.95f + sp.getRandom().nextFloat() * 0.1f);
        float recoil = g.recoil() * modifier(stack, "muzzle_recoil") * (sp.isShiftKeyDown() ? 0.7f : 1f);
        sp.connection.send(new ClientboundPlayerRotationPacket(0f, true, -recoil, true));
        say(sp, Component.translatable("diaithirfirearms.mag").append(": " + mag(stack) + "/" + g.mag()));
        return hits;
    }

    private static boolean shoot(ServerPlayer sp, ServerLevel lv, Defs.Gun g, Vec3 eye, Vec3 dir, float quiet, boolean tracer) {
        Vec3 end = eye.add(dir.scale(g.range()));
        BlockHitResult bh = lv.clip(new ClipContext(eye, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, sp));
        if (bh.getType() != HitResult.Type.MISS) end = bh.getLocation();
        EntityHitResult eh = ProjectileUtil.getEntityHitResult(lv, sp, eye, end, new AABB(eye, end).inflate(1.0),
                e -> !e.isSpectator() && e.isPickable() && e != sp, 0.0f);
        Vec3 impact = end;
        boolean hit = false;
        if (tracer && g.pellets() == 1) for (double d = 2; d < eye.distanceTo(end); d += 4) { Vec3 p = eye.add(dir.scale(d)); lv.sendParticles(ParticleTypes.CRIT, p.x, p.y, p.z, 1, 0, 0, 0, 0); }
        if (eh != null) {
            impact = eh.getLocation();
            Entity t = eh.getEntity();
            double dist = eye.distanceTo(impact);
            float fall = dist <= g.range() * 0.5 ? 1f : (float) (1.0 - 0.5 * (dist - g.range() * 0.5) / (g.range() * 0.5));
            float dmg = g.damage() * crafted(stack, "damage_mul") * Math.max(0.4f, fall) * (quiet < 1f ? 0.9f : 1f);
            if (t instanceof LivingEntity le) {
                if (impact.y > le.getEyeY() - 0.25) dmg *= 1.5f;
                le.invulnerableTime = 0;
                hit = le.hurtServer(lv, sp.damageSources().playerAttack(sp), dmg);
                if (hit) lv.sendParticles(ParticleTypes.DAMAGE_INDICATOR, impact.x, impact.y, impact.z, 1, 0.05, 0.05, 0.05, 0.0);
            } else hit = t.hurtServer(lv, sp.damageSources().playerAttack(sp), dmg);
        }
        if (g.cls().equals("launcher")) {
            lv.explode(sp, impact.x, impact.y, impact.z, g.ammo().equals("rocket") ? 4.0f : 2.5f, Level.ExplosionInteraction.MOB);
        } else if (bh.getType() == HitResult.Type.BLOCK && eh == null) {
            lv.sendParticles(ParticleTypes.SMOKE, impact.x, impact.y, impact.z, 2, 0.05, 0.05, 0.05, 0.01);
        }
        return hit;
    }

    public static boolean reloading(Player p) { return st(p).reloadEnd >= 0; }

    public static void reload(ServerPlayer sp) {
        ItemStack s = sp.getMainHandItem();
        if (!(s.getItem() instanceof GunItem gi)) return;
        S x = st(sp);
        if (x.reloadEnd >= 0 || mag(s) >= gi.gun.mag()) return;
        if (ammoCount(sp, gi.gun) == 0 && !sp.isCreative()) { say(sp, Component.translatable("diaithirfirearms.empty")); return; }
        boolean under = ageOf(sp) < gi.gun.ageNo();
        x.reloadEnd = sp.level().getGameTime() + (long) (gi.gun.reload() * (under ? 1.5 : 1.0));
        x.reloadSlot = sp.getInventory().getSelectedSlot();
        say(sp, Component.translatable("diaithirfirearms.reload"));
        sp.level().playSound(null, sp.getX(), sp.getY(), sp.getZ(), DiaithirFirearms.sound("reload_start"), SoundSource.PLAYERS, 0.8f, 1f);
    }

    /** Selftest: finish a started reload now. */
    static void forceReload(ServerPlayer p) { st(p).reloadEnd = 0; tick(p); }

    static void tick(ServerPlayer sp) {
        S x = STATE.get(sp.getUUID());
        if (x == null || x.reloadEnd < 0) return;
        ItemStack s = sp.getMainHandItem();
        if (!(s.getItem() instanceof GunItem gi) || sp.getInventory().getSelectedSlot() != x.reloadSlot) { x.reloadEnd = -1; return; }
        if (sp.level().getGameTime() < x.reloadEnd) return;
        x.reloadEnd = -1;
        int got = takeAmmo(sp, gi.gun, gi.gun.mag() - mag(s));
        setMag(s, mag(s) + got);
        sp.level().playSound(null, sp.getX(), sp.getY(), sp.getZ(), DiaithirFirearms.sound("reload_end"), SoundSource.PLAYERS, 0.8f, 1f);
        say(sp, Component.translatable("diaithirfirearms.mag").append(": " + mag(s) + "/" + gi.gun.mag()));
    }

    /** Sneak + use with an attachment in the off hand fits it; with an empty off hand removes the last one. */
    public static boolean attach(ServerPlayer sp, ItemStack gun) {
        ItemStack off = sp.getOffhandItem();
        List<String> cur = new ArrayList<>(atts(gun));
        if (off.isEmpty()) {
            if (cur.isEmpty()) return false;
            String last = cur.remove(cur.size() - 1);
            gun.set(DiaithirFirearms.ATT.get(), List.copyOf(cur));
            sp.getInventory().placeItemBackInInventory(new ItemStack(BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath(DiaithirFirearms.ID, last))));
            say(sp, Component.translatable("diaithirfirearms.detached"));
            return true;
        }
        Identifier id = BuiltInRegistries.ITEM.getKey(off.getItem());
        if (!id.getNamespace().equals(DiaithirFirearms.ID) || !Defs.ATTS.containsKey(id.getPath())) return false;
        Defs.Att a = Defs.ATTS.get(id.getPath());
        cur.removeIf(o -> Defs.ATTS.get(o) != null && Defs.ATTS.get(o).kind().equals(a.kind()));
        cur.add(a.id());
        gun.set(DiaithirFirearms.ATT.get(), List.copyOf(cur));
        off.shrink(1);
        sp.level().playSound(null, sp.getX(), sp.getY(), sp.getZ(), DiaithirFirearms.sound("attach"), SoundSource.PLAYERS, 0.6f, 1f);
        say(sp, Component.translatable("diaithirfirearms.attached").append(": " + a.id()));
        return true;
    }

    private Engine() {}
}
