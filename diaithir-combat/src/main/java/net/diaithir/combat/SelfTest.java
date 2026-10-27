package net.diaithir.combat;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.FakePlayerFactory;

/**
 * /combatselftest: for every weapon category, resolves the item tag, then with a fake player checks reach, arc, pierce limit,
 * real damage on a golem for each combo step, parry and dodge rules, Age scaling and bow tuning.
 */
public final class SelfTest {
    private static int ok, bad;
    private static final List<String> LOG = new ArrayList<>();

    private static void check(boolean cond, String what) {
        if (cond) ok++; else { bad++; LOG.add("BAD " + what); System.out.println("[combatselftest] BAD " + what); }
    }

    public static void register(CommandDispatcher<CommandSourceStack> d) {
        d.register(Commands.literal("combatselftest")
                .requires(s -> s.permissions().hasPermission(net.minecraft.server.permissions.Permissions.COMMANDS_GAMEMASTER))
                .executes(c -> {
                    ServerLevel lv = c.getSource().getLevel();
                    BlockPos sp = lv.getRespawnData().pos();
                    if (!lv.isPositionEntityTicking(sp)) {
                        // entities only join once the chunk's entity section is loaded, which takes a few ticks
                        lv.setChunkForced(sp.getX() >> 4, sp.getZ() >> 4, true);
                        later = 40;
                        after = () -> finish(c.getSource());
                        c.getSource().sendSuccess(() -> Component.literal("combat selftest: loading test chunk, result follows"), false);
                        return 1;
                    }
                    return finish(c.getSource());
                }));
    }

    static int later = -1;
    static Runnable after;

    /** Driven from the server tick. */
    static void tick() {
        if (later >= 0 && --later < 0 && after != null) { Runnable r = after; after = null; r.run(); }
    }

    static int finish(CommandSourceStack src) {
        ok = 0; bad = 0; LOG.clear();
        try { run(src.getLevel()); } catch (Throwable t) { t.printStackTrace(); check(false, "exception " + t); }
        String msg = "combat ok=" + ok + " bad=" + bad;
        System.out.println("[combatselftest] " + msg);
        src.sendSuccess(() -> Component.literal(msg), false);
        return bad == 0 ? 1 : 0;
    }

    private static ItemStack rep(Cat c) {
        var tag = BuiltInRegistries.ITEM.get(c.tag);
        if (tag.isPresent()) {
            for (var h : tag.get()) return new ItemStack(h.value());
        }
        return new ItemStack(BuiltInRegistries.ITEM.getValue(Identifier.parse(c.fallback)));
    }

    private static int members(Cat c) {
        var tag = BuiltInRegistries.ITEM.get(c.tag);
        return tag.map(t -> (int) t.stream().count()).orElse(0);
    }

    private static ArmorStand stand(ServerLevel lv, Vec3 p) {
        ArmorStand a = EntityType.ARMOR_STAND.create(lv, EntitySpawnReason.COMMAND);
        a.setPos(p.x, p.y, p.z);
        a.setNoGravity(true);
        lv.addFreshEntity(a);
        return a;
    }

    static void run(ServerLevel lv) {
        BlockPos sp = lv.getRespawnData().pos();
        double bx = sp.getX() + 0.5, by = lv.getMaxY() - 40, bz = sp.getZ() + 0.5;
        ServerPlayer fp = FakePlayerFactory.get(lv, new GameProfile(UUID.fromString("00000000-0000-0000-0000-00000000c0de"), "combat_test"));
        fp.setPos(bx, by, bz);
        fp.setYRot(0); fp.setXRot(0); // facing +Z
        fp.getAbilities().mayBuild = true;
        List<Entity> cleanup = new ArrayList<>();
        int age = 1;
        // ---- age scaling
        check(Engine.ageSpeed(1) == 1f && Engine.ageSpeed(7) > Engine.ageSpeed(3) && Engine.ageSpeed(9) <= CombatConfig.AGE_SPEED_CAP.getAsDouble() + 1e-6, "age speed monotone");
        for (Cat c : Cat.all().values()) {
            ItemStack stack = rep(c);
            check(!stack.isEmpty(), c.id + " representative item");
            int mem = members(c);
            LOG.add(c.id + " members=" + mem + " rep=" + BuiltInRegistries.ITEM.getKey(stack.getItem()));
            if (c.ranged) {
                check(c.drawTicks >= 8 && c.dropReduction >= 0 && c.dropReduction <= 1, c.id + " ranged table");
                check(Engine.drawTicks(c, 1) >= Engine.drawTicks(c, 7), c.id + " draw faster with Age");
                int full = Engine.adjustCharge(c, 1, c.drawTicks);
                check(Math.abs(full - 20) <= 1, c.id + " full draw maps to 20 (" + full + ")");
                LOG.add(String.format("  draw=%d drop=%.2f", c.drawTicks, c.dropReduction));
                continue;
            }
            check(c.range > 1 && c.arc > 5 && c.maxTargets >= 1 && !c.combo.isEmpty(), c.id + " melee table");
            Cat.override = c;
            fp.setItemInHand(InteractionHand.MAIN_HAND, stack);
            Engine.State s = Engine.state(fp);
            s.stamina = 1000f;
            float base = Engine.baseDamage(fp, stack);
            check(base > 1, c.id + " base damage " + base);
            // faster combos with Age
            for (Cat.Step st : c.combo) check(Engine.cooldownTicks(st, 1) >= Engine.cooldownTicks(st, 9), c.id + " cooldown shrinks with Age");
            for (int i = 0; i < c.combo.size(); i++) {
                Cat.Step st = c.combo.get(i);
                float range = Engine.stepRange(c, st), arc = Engine.stepArc(c, st);
                boolean thrust = st.pattern().equals("thrust") || st.pattern().equals("stab");
                // reach: inside hits, outside misses
                ArmorStand in = stand(lv, new Vec3(bx, by, bz + range - 0.35 - 0.25));
                ArmorStand out = stand(lv, new Vec3(bx, by, bz + range + 0.9));
                cleanup.add(in); cleanup.add(out);
                List<LivingEntity> h = Engine.findTargets(fp, c, st);
                check(h.contains(in), c.id + " step" + i + " hits inside reach " + range);
                check(!h.contains(out), c.id + " step" + i + " misses beyond reach");
                in.discard(); out.discard();
                // arc
                if (!thrust) {
                    double dist = Math.min(1.8, range - 0.5);
                    double a1 = Math.toRadians(Math.max(2, arc / 2 - 12)), a2 = Math.toRadians(Math.min(175, arc / 2 + 25));
                    ArmorStand edgeIn = stand(lv, new Vec3(bx + Math.sin(a1) * dist, by, bz + Math.cos(a1) * dist));
                    h = Engine.findTargets(fp, c, st);
                    check(h.contains(edgeIn), c.id + " step" + i + " hits inside arc " + arc);
                    edgeIn.discard();
                    if (arc / 2 + 25 < 175) {
                        ArmorStand edgeOut = stand(lv, new Vec3(bx + Math.sin(a2) * dist, by, bz + Math.cos(a2) * dist));
                        h = Engine.findTargets(fp, c, st);
                        check(!h.contains(edgeOut), c.id + " step" + i + " misses outside arc");
                        edgeOut.discard();
                    }
                } else {
                    ArmorStand side = stand(lv, new Vec3(bx + c.width + 1.2, by, bz + 1.5));
                    h = Engine.findTargets(fp, c, st);
                    check(!h.contains(side), c.id + " step" + i + " thrust misses wide target");
                    side.discard();
                }
                // pierce / sweep limit
                List<ArmorStand> row = new ArrayList<>();
                for (int k = 0; k < c.maxTargets + 3; k++) {
                    double dd = 0.9 + 0.3 * k;
                    if (dd > range - 0.5) dd = Math.min(range - 0.5, 0.9 + 0.05 * k);
                    double off = thrust ? 0 : (k % 2 == 0 ? 1 : -1) * Math.min(0.3 * (k / 2), 0.9);
                    row.add(stand(lv, new Vec3(bx + off, by, bz + dd)));
                }
                h = Engine.findTargets(fp, c, st);
                check(h.size() == c.maxTargets, c.id + " step" + i + " max_targets " + h.size() + "/" + c.maxTargets);
                row.forEach(Entity::discard);
                // real damage on a golem
                IronGolem g = EntityType.IRON_GOLEM.create(lv, EntitySpawnReason.COMMAND);
                g.setNoAi(true); g.setNoGravity(true);
                g.setPos(bx, by, bz + 1.6);
                lv.addFreshEntity(g);
                cleanup.add(g);
                g.setHealth(g.getMaxHealth());
                s.lastSwing = -100000; s.combo = i; s.lastCooldown = 0; s.lastWindow = 1000;
                s.lastSwing = lv.getGameTime(); // inside combo window so idx stays i
                float before = g.getHealth();
                // item enchantment effects (e.g. Spartan backstab) are applied to the scaled hit, same as the engine
                float expect = net.minecraft.world.item.enchantment.EnchantmentHelper.modifyDamage(lv, stack, g,
                        lv.damageSources().playerAttack(fp), base * st.damage() * (float) CombatConfig.DAMAGE_MULT.getAsDouble());
                Engine.Result r = Engine.swing(fp, true);
                float got = before - g.getHealth();
                check(r.swung() && r.step() == i, c.id + " step index " + i + " got " + r.step());
                boolean trait = (c.id.equals("dagger") || c.id.equals("rapier")) && got >= expect * 0.94f && got <= expect * 3.05f; // Spartan backstab/armour traits scale on top
                check(trait || Math.abs(got - expect) < 0.06f * Math.max(1, expect), String.format("%s step%d damage %.2f expected %.2f", c.id, i, got, expect));
                LOG.add(String.format("  %s step%d dmg=%.2f range=%.2f arc=%.0f cd=%d/%d(age1/age7) sta=%.1f", c.id, i, got, range, arc,
                        Engine.cooldownTicks(st, 1), Engine.cooldownTicks(st, 7), st.stamina()));
                g.discard();
            }
            // cooldown gate
            s.lastSwing = lv.getGameTime(); s.lastCooldown = 20; s.combo = 0;
            check(!Engine.swing(fp, false).swung(), c.id + " cooldown blocks early swing");
            // exhausted swing is weaker
            s.stamina = 0; s.lastSwing = -100000; s.combo = 0; s.lastCooldown = 0;
            IronGolem g2 = EntityType.IRON_GOLEM.create(lv, EntitySpawnReason.COMMAND);
            g2.setNoAi(true); g2.setNoGravity(true); g2.setPos(bx, by, bz + 1.6); lv.addFreshEntity(g2);
            Engine.Result r2 = Engine.swing(fp, true);
            check(r2.swung() && r2.exhausted(), c.id + " exhausted flag");
            g2.discard();
        }
        // ---- parry
        ServerPlayer atk = FakePlayerFactory.get(lv, new GameProfile(UUID.fromString("00000000-0000-0000-0000-00000000c0df"), "combat_atk"));
        atk.setPos(bx, by, bz + 2); // in front of fp (fp faces +Z)
        DamageSource hit = lv.damageSources().playerAttack(atk);
        Engine.State ps = Engine.state(fp);
        long t = lv.getGameTime();
        ps.parryStart = t; ps.parryWindow = 8;
        check(Engine.parryCheck(fp, hit), "parry inside window from front");
        ps.parryStart = t - 20;
        check(!Engine.parryCheck(fp, hit), "no parry after window");
        ps.parryStart = t;
        atk.setPos(bx, by, bz - 2);
        check(!Engine.parryCheck(fp, lv.damageSources().playerAttack(atk)), "no parry from behind");
        ps.parryStart = -1000;
        // ---- dodge
        Engine.State ds = Engine.state(fp);
        ds.stamina = 50; ds.dodgeReady = 0; ds.dodgeUntil = -1000;
        fp.setDeltaMovement(Vec3.ZERO);
        check(Engine.dodge(fp, 0, true), "dodge accepted");
        check(fp.getDeltaMovement().horizontalDistance() > 0.3, "dodge launches");
        check(Engine.dodging(fp, lv.damageSources().mobAttack(fp)), "i-frames active");
        check(!Engine.dodging(fp, lv.damageSources().fellOutOfWorld()), "void ignores i-frames");
        check(!Engine.dodge(fp, 0, true), "dodge cooldown");
        check(ds.stamina < 50, "dodge costs stamina");
        ds.dodgeUntil = -1000; ds.dodgeReady = 0; ds.stamina = 0;
        check(!Engine.dodge(fp, 0, true), "dodge refused when exhausted");
        Cat.override = null;
        for (Entity e : cleanup) e.discard();
        fp.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        for (String l : LOG) System.out.println("[combatselftest] " + l);
    }

    private SelfTest() {}
}
