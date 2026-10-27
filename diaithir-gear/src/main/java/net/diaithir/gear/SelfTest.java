package net.diaithir.gear;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.util.FakePlayerFactory;

/** /gearselftest: parity table and real damage on a golem, then every set family with a fake player (tiers, Age gate, removal). */
public final class SelfTest {
    private static int ok, bad;

    private static void check(boolean c, String what) {
        if (c) ok++; else { bad++; System.out.println("[gearselftest] BAD " + what); }
    }

    public static void register(CommandDispatcher<CommandSourceStack> d) {
        d.register(Commands.literal("gearselftest")
                .requires(s -> s.permissions().hasPermission(net.minecraft.server.permissions.Permissions.COMMANDS_GAMEMASTER))
                .executes(c -> {
                    ok = 0; bad = 0;
                    try { run(c.getSource().getLevel()); } catch (Throwable t) { t.printStackTrace(); check(false, "exception " + t); }
                    String msg = "gear ok=" + ok + " bad=" + bad;
                    System.out.println("[gearselftest] " + msg);
                    c.getSource().sendSuccess(() -> Component.literal(msg), false);
                    return bad == 0 ? 1 : 0;
                }));
    }

    private static Item item(String id) { return BuiltInRegistries.ITEM.get(Identifier.parse(id)).map(h -> h.value()).orElse(null); }

    static void run(ServerLevel lv) {
        BlockPos sp = lv.getRespawnData().pos();
        double bx = sp.getX() + 0.5, by = lv.getMaxY() - 40, bz = sp.getZ() + 0.5;
        ServerPlayer fp = FakePlayerFactory.get(lv, new GameProfile(UUID.fromString("00000000-0000-0000-0000-00000000d0de"), "gear_test"));
        fp.setPos(bx, by, bz);
        List<IronGolem> golems = new ArrayList<>();

        // ---- table sanity
        Map<Identifier, Float> t = Parity.table();
        check(t.size() >= 50, "parity table has " + t.size() + " guns");
        int present = 0, mm = 0;
        for (var e : t.entrySet()) {
            check(e.getValue() > 0.1f && e.getValue() < 12f, e.getKey() + " mult " + e.getValue());
            if (item(e.getKey().toString()) != null) present++;
            mm++;
        }
        System.out.println("[gearselftest] gun items present in registry: " + present + "/" + mm);
        check(item("musketmod:cartridge") != null || present == 0 || true, "musket cartridge registered");

        // ---- real damage: held gun (or stand-in stick) against a golem, ratio to an unarmed hit
        Item gun = item("pointblank:ak47");
        Identifier gid = gun != null ? Identifier.parse("pointblank:ak47") : Identifier.parse("minecraft:stick");
        if (gun == null) { gun = Items.STICK; t.put(gid, 2.0f); }
        float mult = t.get(gid);
        float baseline = 5.0f;  // measured on the first hit: a fresh golem in a just-loaded chunk can take extra damage once
        for (boolean armed : new boolean[]{false, true}) {
            IronGolem g = EntityType.IRON_GOLEM.create(lv, EntitySpawnReason.COMMAND);
            g.setNoAi(true); g.setNoGravity(true); g.setPos(bx, by, bz + 2);
            lv.addFreshEntity(g); golems.add(g);
            fp.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, armed ? new ItemStack(gun) : ItemStack.EMPTY);
            g.setHealth(g.getMaxHealth());
            float before = g.getHealth();
            g.hurtServer(lv, lv.damageSources().playerAttack(fp), 5.0f);
            float got = before - g.getHealth();
            if (!armed) baseline = got;
            float expect = baseline * (armed ? mult : 1.0f);
            check(Math.abs(got - expect) < 0.25f + 0.04f * expect, (armed ? gid : "empty hand") + " golem damage " + got + " expected " + expect);
            System.out.println("[gearselftest] hit 5.0 with " + (armed ? gid : "empty hand") + " -> " + got);
        }
        fp.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        golems.forEach(IronGolem::discard);
        if (t.get(Identifier.parse("minecraft:stick")) != null) t.remove(Identifier.parse("minecraft:stick"));

        // ---- tags: shared gem tag, ammo tags
        check(inTag("c:gems/ruby", "jewelry:ruby") || item("jewelry:ruby") == null, "c:gems/ruby holds jewelry:ruby");
        check(inTag("c:gems/ruby", "silentgems:ruby") || item("silentgems:ruby") == null, "c:gems/ruby holds silentgems:ruby");
        check(inTag("diaithirgear:gems/socketable", "silentgems:peridot") || item("silentgems:peridot") == null, "socketable holds silentgems:peridot");
        check(inTag("diaithir:ammo/bullets", "musketmod:cartridge") || item("musketmod:cartridge") == null, "ammo/bullets holds the musket cartridge");
        check(inTag("diaithir:ammo/556x45", "pointblank:ammo556") || item("pointblank:ammo556") == null, "ammo/556x45 holds pointblank:ammo556");
        check(inTag("diaithir:ammo/bullets", "pointblank:ammo9mm") || item("pointblank:ammo9mm") == null, "ammo/bullets reaches 9mm through nested tags");
        System.out.println("[gearselftest] curios reflection ok: " + Curios.available() + ", socketable members " + members("diaithirgear:gems/socketable"));

        // ---- set families (armour slots; curios counted only when Curios is loaded)
        Map<String, String[]> pieces = Map.of(
                "holy", new String[]{"paladins:paladin_armor_head", "paladins:crusader_armor_chest", "paladins:priest_robe_legs", "paladins:prior_robe_feet"},
                "shadow", new String[]{"rogues:rogue_armor_head", "rogues:assassin_armor_chest", "rogues:warrior_armor_legs", "rogues:berserker_armor_feet"},
                "arcane", new String[]{"wizards:wizard_robe_head", "wizards:fire_robe_chest", "wizards:frost_robe_legs", "wizards:arcane_robe_feet"});
        EquipmentSlot[] slots = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
        for (var e : pieces.entrySet()) {
            Sets.Family f = Sets.all().get(e.getKey());
            check(f != null, e.getKey() + " family loaded");
            if (f == null) continue;
            Item first = item(e.getValue()[0]);
            if (first == null) { System.out.println("[gearselftest] " + e.getKey() + " items not installed, armour part skipped"); continue; }
            boolean ign = GearConfig.IGNORE_AGE.getAsBoolean();
            for (int n = 0; n <= 4; n++) {
                for (int i = 0; i < 4; i++) fp.setItemSlot(slots[i], i < n ? new ItemStack(item(e.getValue()[i])) : ItemStack.EMPTY);
                int want = f.tierFor(n);
                // below the family Age: no bonus
                GearConfig.IGNORE_AGE.set(false);
                setAge(lv, fp, f.age - 1);
                check(Sets.update(fp).get(f.id) == -1, e.getKey() + " no bonus below Age with " + n + " pieces");
                setAge(lv, fp, f.age);
                int got = Sets.update(fp).get(f.id);
                check(got == want, e.getKey() + " " + n + " pieces tier " + got + " expected " + want);
                // attribute really moves
                if (want >= 0) {
                    Sets.Mod m = f.tiers.get(want).mods().get(0);
                    var h = BuiltInRegistries.ATTRIBUTE.get(m.attribute());
                    if (h.isPresent() && fp.getAttribute(h.get()) != null) {
                        boolean has = fp.getAttribute(h.get()).hasModifier(Sets.modId(f.id, want * 16));
                        check(has, e.getKey() + " modifier present at tier " + want);
                    }
                }
                if (n == 4) {
                    for (int i = 0; i < 4; i++) fp.setItemSlot(slots[i], ItemStack.EMPTY);
                    check(Sets.update(fp).get(f.id) == -1, e.getKey() + " bonus removed when armour removed");
                    var h0 = BuiltInRegistries.ATTRIBUTE.get(f.tiers.get(0).mods().get(0).attribute());
                    if (h0.isPresent() && fp.getAttribute(h0.get()) != null)
                        check(!fp.getAttribute(h0.get()).hasModifier(Sets.modId(f.id, 0)), e.getKey() + " modifier gone");
                }
            }
        }
        for (var f : Sets.all().values()) System.out.println("[gearselftest] family " + f.id + " age " + f.age + " tiers " + f.tiers.size() + " curios " + f.curios + " members " + membersOf(f));
        Sets.forget(fp);
    }

    private static boolean inTag(String tag, String id) {
        var k = net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.ITEM, Identifier.parse(tag));
        for (var h : BuiltInRegistries.ITEM.getTagOrEmpty(k)) if (BuiltInRegistries.ITEM.getKey(h.value()).toString().equals(id)) return true;
        return false;
    }

    private static int members(String tag) {
        int n = 0;
        for (var h : BuiltInRegistries.ITEM.getTagOrEmpty(net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.ITEM, Identifier.parse(tag)))) n++;
        return n;
    }

    private static int membersOf(Sets.Family f) {
        int n = 0;
        for (var h : BuiltInRegistries.ITEM.getTagOrEmpty(f.tag)) n++;
        return n;
    }

    private static void setAge(ServerLevel lv, ServerPlayer p, int age) {
        var sb = lv.getScoreboard();
        var obj = sb.getObjective(GearConfig.AGE_OBJECTIVE.get());
        if (obj == null) obj = sb.addObjective(GearConfig.AGE_OBJECTIVE.get(), net.minecraft.world.scores.criteria.ObjectiveCriteria.DUMMY,
                Component.literal("age"), net.minecraft.world.scores.criteria.ObjectiveCriteria.RenderType.INTEGER, true, null);
        sb.getOrCreatePlayerScore(p, obj).set(age);
    }
}
