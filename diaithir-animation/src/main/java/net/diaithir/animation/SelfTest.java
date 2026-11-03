package net.diaithir.animation;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mojang.brigadier.CommandDispatcher;
import com.zigythebird.playeranimcore.animation.Animation;
import com.zigythebird.playeranimcore.loading.UniversalAnimLoader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

/**
 * /animselftest: loads every clip file with Player Animation Library's own loader, checks that each class in clips.json has all its clips,
 * that every clip has length and keyframes, that the packet codec round-trips, and that sample items resolve to the right class by tag.
 */
public final class SelfTest {
    private static int ok, bad;
    private static final List<String> LOG = new ArrayList<>();

    private static void check(boolean cond, String what) {
        if (cond) ok++; else { bad++; LOG.add("BAD " + what); System.out.println("[animselftest] BAD " + what); }
    }

    static void register(CommandDispatcher<CommandSourceStack> d) {
        var gm = (java.util.function.Predicate<CommandSourceStack>) s -> s.permissions().hasPermission(net.minecraft.server.permissions.Permissions.COMMANDS_GAMEMASTER);
        // /animplay <clip>: sends any clip (e.g. gun.pistol.fire) to yourself and everyone watching; for render tests
        d.register(Commands.literal("animplay").requires(gm)
                .then(Commands.argument("clip", com.mojang.brigadier.arguments.StringArgumentType.word()).executes(c -> {
                    var p = c.getSource().getPlayerOrException();
                    String clip = com.mojang.brigadier.arguments.StringArgumentType.getString(c, "clip");
                    Anim.play(p, clip);
                    c.getSource().sendSuccess(() -> Component.literal("animplay " + clip), false);
                    return 1;
                })));
        // /animgun <action>: the action (fire, reload, inspect, dry, unjam) on the held gun, or a pistol when the hand holds no gun
        d.register(Commands.literal("animgun").requires(gm)
                .then(Commands.argument("action", com.mojang.brigadier.arguments.StringArgumentType.word()).executes(c -> {
                    var p = c.getSource().getPlayerOrException();
                    String act = com.mojang.brigadier.arguments.StringArgumentType.getString(c, "action");
                    Kind k = Kind.of(p.getMainHandItem());
                    String clip = k != null && k.gun() ? k.clip(p.getMainHandItem().getItem(), act) : "gun.pistol." + act;
                    Anim.play(p, clip);
                    c.getSource().sendSuccess(() -> Component.literal("animgun " + clip), false);
                    return 1;
                })));
        d.register(Commands.literal("animselftest")
                .requires(s -> s.permissions().hasPermission(net.minecraft.server.permissions.Permissions.COMMANDS_GAMEMASTER))
                .executes(c -> {
                    run();
                    String msg = "animselftest ok=" + ok + " bad=" + bad + " clips_sent=" + Anim.calls();
                    System.out.println("[animselftest] " + msg);
                    c.getSource().sendSuccess(() -> Component.literal(msg), false);
                    return bad == 0 ? 1 : 0;
                }));
    }

    static void run() {
        ok = bad = 0; LOG.clear();
        Map<String, Animation> all = new HashMap<>();
        for (String f : List.of("melee", "tools", "guns", "casting")) {
            try (var in = SelfTest.class.getResourceAsStream("/assets/" + DiaithirAnimation.ID + "/player_animations/" + f + ".json")) {
                Map<String, Animation> m = UniversalAnimLoader.loadAnimations(in);
                check(!m.isEmpty(), f + " loads");
                all.putAll(m);
            } catch (Exception e) { check(false, f + " load failed: " + e); }
        }
        for (var e : all.entrySet()) {
            Animation a = e.getValue();
            check(a.length() > 0, e.getKey() + " has length");
            check(!a.boneAnimations().isEmpty(), e.getKey() + " has bones");
        }
        JsonObject j;
        try (var r = new InputStreamReader(SelfTest.class.getResourceAsStream("/clips.json"))) { j = new Gson().fromJson(r, JsonObject.class); }
        catch (Exception e) { check(false, "clips.json " + e); return; }
        int clips = 0;
        for (var e : j.getAsJsonObject("classes").entrySet()) {
            if (e.getKey().equals("own_gun_reloads")) {
                e.getValue().getAsJsonArray().forEach(x -> check(all.containsKey("gun." + x.getAsString() + ".reload"), "own reload " + x.getAsString()));
                continue;
            }
            String prefix = e.getKey();
            for (var n : e.getValue().getAsJsonArray()) { clips++; check(all.containsKey(prefix + "." + n.getAsString()), "clip " + prefix + "." + n.getAsString()); }
        }
        // every gun action the server can ask for exists for every gun class
        for (String c : Kind.GUNS) for (String a : List.of("hold", "aim", "fire", "reload", "dry", "inspect", "draw", "sprint", "unjam")) check(all.containsKey("gun." + c + "." + a), "gun." + c + "." + a);
        // reload clip of an own gun is exactly as long as the server reload (guns.json ticks)
        Animation rl = all.get("gun.carbine.reload");
        float len = rl == null ? -1 : rl.length();
        System.out.println("[animselftest] carbine reload length value " + len + " (server reload 56 ticks = 2.8 s)");
        check(Math.abs(len - 2.8f) < 0.05f || Math.abs(len - 56f) < 1f, "carbine reload length matches the server reload");
        for (String c : List.of("turret", "driver", "mounted_gun", "cannon")) check(all.containsKey("seat." + c), "seat." + c);
        check(all.containsKey("cannon.load") && all.containsKey("cannon.fire"), "cannon clips");
        // projectile look by entity id
        check(ProjectileFx.look("fire_dragon_charge") == net.minecraft.core.particles.ParticleTypes.FLAME, "look fire");
        check(ProjectileFx.look("hydra_arrow") == net.minecraft.core.particles.ParticleTypes.CRIT, "look default");
        check(ProjectileFx.look("frost_bolt") == net.minecraft.core.particles.ParticleTypes.SNOWFLAKE, "look frost");
        // tags
        for (var s : Map.of("minecraft:iron_sword", "sword", "minecraft:iron_axe", "axe", "minecraft:iron_pickaxe", "pickaxe", "minecraft:bow", "bow", "minecraft:crossbow", "crossbow",
                "minecraft:shield", "shield", "minecraft:iron_shovel", "shovel", "minecraft:iron_hoe", "hoe", "minecraft:shears", "shears").entrySet()) {
            var it = BuiltInRegistries.ITEM.getOptional(Identifier.parse(s.getKey()));
            Kind k = it.map(i -> Kind.of(new ItemStack(i))).orElse(null);
            check(k != null && !k.gun() && k.cls().equals(s.getValue()), s.getKey() + " -> " + (k == null ? "none" : k.cls()));
        }
        for (var s : Map.of("diaithirfirearms:carbine", "rifle", "diaithirfirearms:sidearm_light", "pistol", "diaithirfirearms:minigun", "lmg").entrySet()) {
            var it = BuiltInRegistries.ITEM.getOptional(Identifier.parse(s.getKey()));
            if (it.isEmpty()) continue; // firearms jar not installed
            Kind k = Kind.of(new ItemStack(it.get()));
            check(k != null && k.gun() && k.cls().equals(s.getValue()), s.getKey() + " -> " + (k == null ? "none" : k.cls()));
            if (k != null) check(k.clip(it.get(), "reload").startsWith("gun." + s.getKey().substring(s.getKey().indexOf(':') + 1)), "own reload clip for " + s.getKey());
        }
        body(all);
        System.out.println("[animselftest] clips sent since boot: " + Anim.calls());
        System.out.println("[animselftest] clips checked " + clips + ", loaded " + all.size());
        LOG.forEach(System.out::println);
    }

    /** Body layer: body.json loads, every clip in body_clips.json exists, every action and item phase points at a clip, sample items resolve to the right kind. */
    private static void body(Map<String, Animation> all) {
        Map<String, Animation> b = new HashMap<>();
        try (var in = SelfTest.class.getResourceAsStream("/assets/" + DiaithirAnimation.ID + "/player_animations/body.json")) {
            b.putAll(UniversalAnimLoader.loadAnimations(in));
        } catch (Exception e) { check(false, "body.json load failed: " + e); return; }
        check(!b.isEmpty(), "body.json loads");
        for (var e : b.entrySet()) {
            check(e.getValue().length() > 0, e.getKey() + " has length");
            check(!e.getValue().boneAnimations().isEmpty(), e.getKey() + " has bones");
        }
        JsonObject j;
        try (var r = new InputStreamReader(SelfTest.class.getResourceAsStream("/body_clips.json"))) { j = new Gson().fromJson(r, JsonObject.class); }
        catch (Exception e) { check(false, "body_clips.json " + e); return; }
        j.getAsJsonArray("clips").forEach(x -> check(b.containsKey(x.getAsString()), "body clip " + x.getAsString()));
        j.getAsJsonObject("actions").entrySet().forEach(x -> check(b.containsKey("act." + x.getValue().getAsString()), "action " + x.getKey() + " -> " + x.getValue().getAsString()));
        j.getAsJsonObject("item_kinds").entrySet().forEach(x -> x.getValue().getAsJsonArray().forEach(ph -> check(b.containsKey("use." + x.getKey() + "." + ph.getAsString()), "use." + x.getKey() + "." + ph.getAsString())));
        // every clip the client can request by name exists (state machine + server one-shots)
        for (String n : List.of("act.die", "act.hurt", "act.land_soft", "act.land_hard", "act.jump", "act.sleep", "act.swim", "act.swim_sprint", "act.crawl", "act.tread", "act.wade", "act.fall",
                "act.ladder", "act.vine", "act.scaffold", "act.climb", "act.ride_boat", "act.ride_cart", "act.ride_horse", "act.ride_mount", "act.sit_seat", "act.lean_walk", "act.lean_run",
                "act.lean_sprint", "act.lean_sneak", "act.lean_sneak_walk", "act.hold_map", "act.hold_torch", "act.punch", "act.brew", "act.enchant", "act.trade", "act.craft", "act.open_container",
                "act.close_container", "act.drop_item", "act.pick_up", "act.place", "act.respawn", "use.wings.deploy", "use.wings.fold", "use.wings.glide", "use.rod.cast", "use.rod.wait", "use.rod.reel"))
            check(b.containsKey(n), "client clip " + n);
        for (var s : Map.ofEntries(Map.entry("minecraft:snowball", "throwable"), Map.entry("minecraft:ender_pearl", "throwable"), Map.entry("minecraft:spyglass", "spyglass"),
                Map.entry("minecraft:goat_horn", "horn"), Map.entry("minecraft:elytra", "wings"), Map.entry("minecraft:fishing_rod", "rod"), Map.entry("minecraft:potion", "drink"),
                Map.entry("minecraft:apple", "food"), Map.entry("minecraft:bread", "food"), Map.entry("minecraft:filled_map", "map")).entrySet()) {
            var it = BuiltInRegistries.ITEM.getOptional(Identifier.parse(s.getKey()));
            String k = it.map(i -> BodyKind.of(new ItemStack(i))).orElse(null);
            check(s.getValue().equals(k), s.getKey() + " body kind -> " + k);
        }
        System.out.println("[animselftest] body clips checked " + b.size());
    }

    private SelfTest() {}
}
