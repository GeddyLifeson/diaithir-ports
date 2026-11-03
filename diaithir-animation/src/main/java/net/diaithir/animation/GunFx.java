package net.diaithir.animation;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;

/**
 * Client-only gun effects timed to the clips: shell casings on every shot, belt links marching into belt-fed guns, magazine/drum/speedloader/cell
 * eject and insert, shotgun shells into the tube, musket ram, charging-handle rack. All are particles (casings are not items, per the munitions contract
 * plans/MUNITIONS_CONTAINERS.md); container particles use the diaithircrafting ids when that mod has them and an iron nugget otherwise.
 * The clip length (ticks) comes from the loaded animation, so the beats scale with each gun's real reload time.
 */
final class GunFx {
    private record Due(long tick, Runnable run) {}
    private static final List<Due> QUEUE = new ArrayList<>();

    /** Container ejected on reload, round loaded, and whether a fired casing is thrown, per gun class (contract section 2). */
    private record Spec(String container, String round, boolean casing, boolean belt) {}
    private static Spec spec(String cls) {
        return switch (cls) {
            case "pistol" -> new Spec("magazine_pistol", null, true, false);
            case "smg" -> new Spec("magazine_smg", null, true, false);
            case "rifle" -> new Spec("magazine_rifle", null, true, false);
            case "sniper" -> new Spec("magazine_sniper", null, true, false);
            case "lmg" -> new Spec("belt_lmg", "link_lmg", true, true);
            case "shotgun" -> new Spec(null, "shell_shotgun", true, false);
            case "musket" -> new Spec(null, "cartridge_musket", false, false);
            case "energy" -> new Spec("cell_energy", null, false, false);
            default -> new Spec(null, null, false, false); // launcher: round goes in directly
        };
    }

    static void init() { NeoForge.EVENT_BUS.addListener(ClientTickEvent.Post.class, e -> tick()); }

    private static void tick() {
        var mc = Minecraft.getInstance();
        if (mc.level == null || QUEUE.isEmpty()) return;
        long now = mc.level.getGameTime();
        for (var it = QUEUE.iterator(); it.hasNext(); ) {
            Due d = it.next();
            if (d.tick() <= now) { it.remove(); try { d.run().run(); } catch (RuntimeException ignored) { } }
        }
    }

    private static void later(Avatar a, int ticks, Runnable r) {
        if (ticks <= 0) { r.run(); return; }
        QUEUE.add(new Due(a.level().getGameTime() + ticks, r));
        if (QUEUE.size() > 400) QUEUE.remove(0);
    }

    private static ItemStack stack(String id, Item fallback) {
        if (id == null) return new ItemStack(fallback);
        return BuiltInRegistries.ITEM.getOptional(Identifier.fromNamespaceAndPath("diaithircrafting", id)).map(ItemStack::new).orElse(new ItemStack(fallback));
    }

    /** Where the right/left hand roughly is: eye height minus a little, a step to the side and forward. */
    private static Vec3 hand(Avatar a, boolean right, double fwd) {
        Vec3 look = a.getLookAngle();
        Vec3 side = new Vec3(-look.z, 0, look.x).normalize().scale(right ? -0.3 : 0.3);
        return a.getEyePosition().add(look.scale(fwd)).add(side).add(0, -0.35, 0);
    }

    private static void item(Avatar a, ItemStack s, Vec3 at, Vec3 v) {
        a.level().addParticle(new ItemParticleOption(ParticleTypes.ITEM, s.getItem()), at.x, at.y, at.z, v.x, v.y, v.z);
        System.out.println("[animfx] particle " + s.getItem() + " at " + String.format("%.2f,%.2f,%.2f", at.x, at.y, at.z) + " for entity " + a.getId());
    }

    /** Called for every gun clip the client starts on an avatar: "gun.<class or gun id>.<action>". */
    static void clip(Avatar a, String name, String cls) {
        String[] p = name.split("\\.");
        if (p.length < 3) return;
        String action = p[p.length - 1];
        Spec sp = spec(cls);
        System.out.println("[animfx] clip " + name + " class " + cls + " entity " + a.getId());
        switch (action) {
            case "fire" -> {
                var m = hand(a, true, 0.5);  // visible muzzle puff next to the casing
                a.level().addParticle(ParticleTypes.SMOKE, m.x + a.getLookAngle().x * 0.4, m.y, m.z + a.getLookAngle().z * 0.4, 0, 0.01, 0);
                if (sp.casing()) {
                    Vec3 side = new Vec3(-a.getLookAngle().z, 0, a.getLookAngle().x).normalize();
                    item(a, new ItemStack(Items.GOLD_NUGGET), hand(a, true, 0.5), side.scale(-0.12).add(0, 0.12, 0));
                }
                if (sp.belt()) for (int i = 0; i < 3; i++) {
                    int n = i;
                    later(a, i, () -> item(a, stack(sp.round(), Items.IRON_NUGGET), hand(a, false, 0.2 + 0.25 * n), a.getLookAngle().scale(0.12)));
                }
            }
            case "reload" -> reload(a, name, cls, sp);
            case "inspect", "unjam" -> later(a, 20, () -> item(a, new ItemStack(Items.GOLD_NUGGET), hand(a, true, 0.5), new Vec3(0, 0.1, 0)));
            default -> { }
        }
    }

    private static void reload(Avatar a, String name, String cls, Spec sp) {
        var an = com.zigythebird.playeranim.animation.PlayerAnimResources.getAnimation(Identifier.fromNamespaceAndPath(DiaithirAnimation.ID, name));
        int len = an == null ? 60 : Math.max(10, (int) an.length());
        Vec3 down = new Vec3(0, -0.05, 0);
        switch (cls) {
            case "shotgun" -> { // one shell into the tube per beat
                int n = Math.max(2, len / 14);
                for (int i = 0; i < n; i++) later(a, (int) (len * (0.2 + 0.65 * i / n)), () -> item(a, stack(sp.round(), Items.GOLD_NUGGET), hand(a, false, 0.6), new Vec3(0, 0.03, 0)));
                later(a, (int) (len * 0.9), () -> rack(a));
            }
            case "musket" -> {
                later(a, (int) (len * 0.2), () -> a.level().addParticle(ParticleTypes.SMOKE, hand(a, false, 0.7).x, hand(a, false, 0.7).y, hand(a, false, 0.7).z, 0, 0.02, 0));
                later(a, (int) (len * 0.4), () -> item(a, stack(sp.round(), Items.PAPER), hand(a, true, 0.5), down));
                later(a, (int) (len * 0.7), () -> a.level().addParticle(ParticleTypes.POOF, hand(a, true, 0.8).x, hand(a, true, 0.8).y, hand(a, true, 0.8).z, 0, 0, 0));
            }
            case "energy" -> {
                later(a, (int) (len * 0.25), () -> item(a, stack(sp.container(), Items.AMETHYST_SHARD), hand(a, false, 0.4), down));
                later(a, (int) (len * 0.7), () -> a.level().addParticle(ParticleTypes.END_ROD, hand(a, false, 0.5).x, hand(a, false, 0.5).y, hand(a, false, 0.5).z, 0, 0.02, 0));
            }
            case "launcher" -> later(a, (int) (len * 0.5), () -> item(a, new ItemStack(Items.FIREWORK_ROCKET), hand(a, false, 0.6), new Vec3(0, 0.02, 0.0)));
            case "lmg" -> { // belt feed: links walk in from the left hand to the receiver, then the cover closes
                int n = Math.max(3, len / 10);
                for (int i = 0; i < n; i++) {
                    int k = i;
                    later(a, (int) (len * (0.25 + 0.55 * i / n)), () -> item(a, stack(sp.round(), Items.IRON_NUGGET), hand(a, false, 0.1 + 0.5 * k / n), a.getLookAngle().scale(0.1)));
                }
                later(a, (int) (len * 0.9), () -> rack(a));
            }
            default -> { // magazine swap: old one drops, new one seats, slide racked
                later(a, (int) (len * 0.2), () -> item(a, stack(sp.container(), Items.IRON_INGOT), hand(a, false, 0.35), down));
                later(a, (int) (len * 0.6), () -> a.level().addParticle(ParticleTypes.CRIT, hand(a, false, 0.35).x, hand(a, false, 0.35).y, hand(a, false, 0.35).z, 0, 0.05, 0));
                later(a, (int) (len * 0.85), () -> rack(a));
            }
        }
    }

    private static void rack(Avatar a) {
        Vec3 h = hand(a, true, 0.55);
        item(a, new ItemStack(Items.GOLD_NUGGET), h, new Vec3(-a.getLookAngle().z * -0.1, 0.1, a.getLookAngle().x * 0.1));
    }

    private GunFx() {}
}
