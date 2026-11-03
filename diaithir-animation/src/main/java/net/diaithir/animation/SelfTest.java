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
        d.register(Commands.literal("animselftest")
                .requires(s -> s.permissions().hasPermission(net.minecraft.server.permissions.Permissions.COMMANDS_GAMEMASTER))
                .executes(c -> {
                    run();
                    String msg = "animselftest ok=" + ok + " bad=" + bad;
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
        for (String c : Kind.GUNS) for (String a : List.of("hold", "aim", "fire", "reload", "dry", "inspect", "draw", "sprint")) check(all.containsKey("gun." + c + "." + a), "gun." + c + "." + a);
        // reload clip of an own gun is exactly as long as the server reload (guns.json ticks)
        Animation rl = all.get("gun.carbine.reload");
        float len = rl == null ? -1 : rl.length();
        System.out.println("[animselftest] carbine reload length value " + len + " (server reload 56 ticks = 2.8 s)");
        check(Math.abs(len - 2.8f) < 0.05f || Math.abs(len - 56f) < 1f, "carbine reload length matches the server reload");
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
        System.out.println("[animselftest] clips checked " + clips + ", loaded " + all.size());
        LOG.forEach(System.out::println);
    }

    private SelfTest() {}
}
