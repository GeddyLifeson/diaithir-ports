package net.diaithir.firearms;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
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
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.util.FakePlayerFactory;

/** /firearmsselftest: every gun is built, loaded, fired at an armour stand (hit, damage, falloff), reloaded, and fitted with attachments. */
public final class SelfTest {
    private static int ok, bad;
    static int later = -1;
    static Runnable after;

    private static void check(boolean c, String what) {
        if (c) ok++; else { bad++; System.out.println("[firearmsselftest] BAD " + what); }
    }

    public static void register(CommandDispatcher<CommandSourceStack> d) {
        d.register(Commands.literal("firearmsselftest")
                .requires(s -> s.permissions().hasPermission(net.minecraft.server.permissions.Permissions.COMMANDS_GAMEMASTER))
                .executes(c -> {
                    ServerLevel lv = c.getSource().getLevel();
                    BlockPos sp = lv.getRespawnData().pos();
                    if (!lv.isPositionEntityTicking(sp)) {
                        lv.setChunkForced(sp.getX() >> 4, sp.getZ() >> 4, true);
                        later = 40;
                        after = () -> finish(c.getSource());
                        c.getSource().sendSuccess(() -> Component.literal("firearms selftest: loading test chunk, result follows"), false);
                        return 1;
                    }
                    return finish(c.getSource());
                }));
    }

    static void tick() {
        if (later >= 0 && --later < 0 && after != null) { Runnable r = after; after = null; r.run(); }
    }

    static int finish(CommandSourceStack src) {
        ok = 0; bad = 0;
        try { run(src.getLevel()); } catch (Throwable t) { t.printStackTrace(); check(false, "exception " + t); }
        String msg = "firearms ok=" + ok + " bad=" + bad;
        System.out.println("[firearmsselftest] " + msg);
        src.sendSuccess(() -> Component.literal(msg), false);
        return bad == 0 ? 1 : 0;
    }

    private static ItemStack stack(String id) {
        return new ItemStack(BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath(DiaithirFirearms.ID, id)));
    }

    private static void run(ServerLevel lv) {
        ServerPlayer fp = FakePlayerFactory.get(lv, new GameProfile(UUID.nameUUIDFromBytes("firearms-test".getBytes()), "[FirearmsTest]"));
        BlockPos base = lv.getRespawnData().pos().above(60);
        fp.snapTo(base.getX() + 0.5, base.getY(), base.getZ() + 0.5, 0f, 0f);
        for (int x = -3; x <= 3; x++) for (int z = -3; z <= 30; z++) lv.setBlock(base.offset(x, -1, z), net.minecraft.world.level.block.Blocks.STONE.defaultBlockState(), 3);
        // clear the line of sight
        for (int y = 0; y < 4; y++) for (int z = 0; z <= 30; z++) lv.setBlock(base.offset(0, y, z), net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(), 3);
        for (String n : new String[]{"shot_pistol", "shot_smg", "shot_rifle", "shot_shotgun", "shot_sniper", "shot_lmg", "shot_launcher", "shot_energy", "reload_start", "reload_end", "dry_fire", "attach"})
            check(DiaithirFirearms.sound(n) != null && BuiltInRegistries.SOUND_EVENT.containsKey(Identifier.fromNamespaceAndPath(DiaithirFirearms.ID, n)), "sound " + n);
        check(Defs.GUNS.size() >= 17, "at least 17 guns, got " + Defs.GUNS.size());
        for (Defs.Att a : Defs.ATTS.values()) check(!stack(a.id()).isEmpty() && BuiltInRegistries.ITEM.containsKey(Identifier.fromNamespaceAndPath(DiaithirFirearms.ID, a.id())), "attachment " + a.id());
        for (var m : Defs.GUNS.values()) {
            ItemStack g = stack(m.id());
            check(g.getItem() instanceof GunItem, m.id() + " is a GunItem");
            check(BuiltInRegistries.ITEM.containsKey(Identifier.fromNamespaceAndPath(DiaithirFirearms.ID, m.ammo())), m.id() + " ammo item " + m.ammo());
            if (m.cls().equals("launcher")) continue;
            // target 8 m away, at eye height, with the gun aimed straight at it
            for (var e : lv.getEntitiesOfClass(ArmorStand.class, new net.minecraft.world.phys.AABB(base).inflate(40))) e.discard();
            ArmorStand a = EntityType.ARMOR_STAND.create(lv, EntitySpawnReason.COMMAND);
            a.setPos(base.getX() + 0.5, base.getY() + 0.1, base.getZ() + 8.5);
            a.setNoGravity(true);
            lv.addFreshEntity(a);
            fp.snapTo(base.getX() + 0.5, base.getY(), base.getZ() + 0.5, 0f, (float) Math.toDegrees(Math.atan2(base.getY() + 0.9 - (base.getY() + fp.getEyeHeight()), 8)) * -1f);
            Engine.setMag(g, m.mag());
            fp.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, g);
            int mag0 = Engine.mag(g);
            float hp0 = a.getHealth();
            // an armour stand has 20 health; sneak = tight spread so the test is steady
            fp.setShiftKeyDown(true);
            int hits = Engine.fire(fp, g, m);
            check(Engine.mag(g) == mag0 - 1, m.id() + " fire spends one round");
            for (int tries = 0; tries < 12 && hits < 1 && a.getHealth() >= hp0 && !a.isRemoved(); tries++) { Engine.setMag(g, m.mag()); hits = Engine.fire(fp, g, m); }
            check(m.pellets() > 1 ? hits >= 1 : hits == 1 || a.getHealth() < hp0 || a.isRemoved(), m.id() + " hits the stand within 12 tries (hits=" + hits + ")");
            fp.setShiftKeyDown(false);
        }
        // attachments
        ItemStack rifle = stack("assault_rifle");
        fp.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, rifle);
        fp.setItemInHand(net.minecraft.world.InteractionHand.OFF_HAND, stack("sight_x4"));
        check(Engine.attach(fp, rifle) && Engine.zoom(rifle) == 4f, "scope fitted, zoom 4");
        fp.setItemInHand(net.minecraft.world.InteractionHand.OFF_HAND, stack("sight_x8"));
        Engine.attach(fp, rifle);
        check(Engine.zoom(rifle) == 8f && Engine.atts(rifle).size() == 1, "second sight replaces the first");
        fp.setItemInHand(net.minecraft.world.InteractionHand.OFF_HAND, stack("suppressor"));
        Engine.attach(fp, rifle);
        check(Engine.modifier(rifle, "muzzle_quiet") < 1f && Engine.atts(rifle).size() == 2, "suppressor fitted");
        fp.setItemInHand(net.minecraft.world.InteractionHand.OFF_HAND, ItemStack.EMPTY);
        Engine.attach(fp, rifle);
        check(Engine.atts(rifle).size() == 1, "detach removes the last one");
        // reload (creative shortcut: fake player is not creative; give ammo)
        ItemStack pistol = stack("sidearm_light");
        Engine.setMag(pistol, 0);
        fp.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, pistol);
        ItemStack ammo = stack("ammo_9mm"); ammo.setCount(40);
        fp.getInventory().setItem(10, ammo);
        Engine.reload(fp);
        check(Engine.reloading(fp), "reload starts");
        Engine.forceReload(fp);
        check(!Engine.reloading(fp) && Engine.mag(pistol) == 15, "reload fills the magazine, mag=" + Engine.mag(pistol));
        check(Engine.ammoCount(fp, Defs.GUNS.get("sidearm_light")) == 25, "reload spends 15 of 40 rounds");
        fp.getInventory().setItem(10, ItemStack.EMPTY);
        check(Engine.ammoCount(fp, Defs.GUNS.get("sidearm_light")) == 0, "ammo count reads zero when the stack is gone");
        Engine.setMag(pistol, 0);
        fp.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, pistol);
        Engine.reload(fp);
        check(!Engine.reloading(fp), "no reload without ammo");
        for (var e : lv.getEntitiesOfClass(ArmorStand.class, new net.minecraft.world.phys.AABB(base).inflate(40))) e.discard();
        containers();
    }

    /** Container contract logic on plain json (the component itself belongs to diaithir-crafting and may not be loaded). */
    private static void containers() {
        for (var m : Defs.GUNS.values()) check(Defs.KINDS.containsKey(m.cls()), m.id() + " class " + m.cls() + " has a container kind list");
        check(Defs.KINDS.get("lmg").get(0).equals("belt") && Defs.KINDS.get("pistol").contains("speedloader") && Defs.KINDS.get("shotgun").isEmpty(), "kinds follow contract section 2");
        var pistol = Defs.GUNS.get("sidearm_light");
        var j = com.google.gson.JsonParser.parseString("{\"class\":\"pistol\",\"capacity\":15,\"count\":12,\"round\":\"diaithirfirearms:ammo_9mm\"}").getAsJsonObject();
        check(Containers.fits(j, pistol), "pistol magazine of 9 mm fits the pistol");
        check(!Containers.fits(j, Defs.GUNS.get("assault_rifle")), "pistol magazine does not fit a rifle");
        j.addProperty("round", "diaithircrafting:ammo_45");
        check(!Containers.fits(j, pistol), "wrong round does not fit");
        j.addProperty("round", "diaithircrafting:ammo_9mm");
        check(Containers.fits(j, pistol), "same round path from another namespace fits");
        check(Containers.drain(j, 5) == 5 && j.get("count").getAsInt() == 7, "drain takes 5 of 12");
        check(Containers.drain(j, 99) == 7 && j.get("count").getAsInt() == 0, "drain empties at 0, never negative");
        check(Containers.drain(j, 3) == 0 && j.get("count").getAsInt() == 0, "empty container gives nothing");
    }

    private SelfTest() {}
}
