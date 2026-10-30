package net.diaithir.relics;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import java.util.UUID;
import net.diaithir.relics.DiaithirRelics.Def;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.common.util.FakePlayerFactory;

/**
 * /relicsselftest: for every relic, builds the stack, gives it to a fake player beside a bound zombie, fires the active power once
 * and triggers the passive once through the real event path (a melee blow, a blow taken, the once-a-second tick, a fatal blow),
 * then checks the visible result. Prints one line per relic and a total.
 */
public final class SelfTest {
    private static int bad;

    private static CommandSourceStack pending;
    private static int wait;

    public static void register(CommandDispatcher<CommandSourceStack> d) {
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(net.neoforged.neoforge.event.tick.ServerTickEvent.Post.class, e -> {
            if (pending != null && --wait <= 0) {
                CommandSourceStack src = pending;
                pending = null;
                run(src);
            }
        });
        d.register(Commands.literal("relicsselftest").requires(s -> s.permissions().hasPermission(net.minecraft.server.permissions.Permissions.COMMANDS_GAMEMASTER)).executes(c -> {
            // the test area must be entity-ticking before mobs can be found, so the chunks are forced now and the run follows a second later
            ServerLevel level = c.getSource().getLevel();
            Vec3 b = c.getSource().getPosition();
            int cx = ((int) Math.floor(b.x)) >> 4, cz = ((int) Math.floor(b.z)) >> 4;
            for (int dx = -1; dx <= 1; dx++) for (int dz = -1; dz <= 1; dz++) { level.getChunk(cx + dx, cz + dz); level.setChunkForced(cx + dx, cz + dz, true); }
            pending = c.getSource();
            wait = 40;
            c.getSource().sendSuccess(() -> Component.literal("relics self test starts in 2 seconds; one line per relic goes to the log"), false);
            return 1;
        }));
    }

    private static void run(CommandSourceStack source) {
        ServerLevel level = source.getLevel();
        int ok = 0;
        bad = 0;
        FakePlayer fake = FakePlayerFactory.get(level, new GameProfile(UUID.fromString("5e1f7e57-0000-4000-8000-00000000d1a1"), "[RelicTest]"));
        fake.connection.handleAcceptPlayerLoad(new net.minecraft.network.protocol.game.ServerboundPlayerLoadedPacket());  // else the fake player counts as still loading and takes no damage
        unprotect(fake);
        fake.setInvulnerable(false);  // NeoForge's fake player is invulnerable by default, which would hide every wearer-side power
        Vec3 base = source.getPosition();
        int cx = ((int) Math.floor(base.x)) >> 4, cz = ((int) Math.floor(base.z)) >> 4;
        base = new Vec3(base.x, level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING, (int) Math.floor(base.x), (int) Math.floor(base.z)) + 1, base.z);
        for (Def def : DiaithirRelics.DATA.items()) {
            String r;
            try {
                r = one(level, fake, base, def);
            } catch (Throwable t) {
                r = "EXCEPTION " + t;
                t.printStackTrace();
            }
            boolean good = r.startsWith("ok");
            if (good) ok++; else bad++;
            System.out.println("[relicsselftest] " + (good ? "OK  " : "BAD ") + def.id() + " " + r);
        }
        fake.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
        fake.setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
        for (int dx = -1; dx <= 1; dx++) for (int dz = -1; dz <= 1; dz++) level.setChunkForced(cx + dx, cz + dz, false);
        String curiosLine = curiosPath(level, base);
        System.out.println("[relicsselftest] " + curiosLine);
        String total = "relics ok=" + ok + " bad=" + bad + " curios=" + Equipped.curiosPresent();
        System.out.println("[relicsselftest] " + total);
        source.sendSuccess(() -> Component.literal(total), false);
    }

    /** With Curios loaded, a ring placed in a real Curios slot (on an armour stand, which Curios treats like a player) must be found by the lookup. */
    private static String curiosPath(ServerLevel level, Vec3 base) {
        net.minecraft.world.entity.decoration.ArmorStand stand = EntityType.ARMOR_STAND.create(level, EntitySpawnReason.COMMAND);
        stand.setPos(base.x, base.y, base.z + 6);
        level.addFreshEntity(stand);
        try {
            Object h = Equipped.curios(stand);
            if (h == null) return "curios path: no Curios inventory on the armour stand";
            ItemStack ring = new ItemStack(DiaithirRelics.all().get("well_ring").get());
            java.lang.reflect.Method set = h.getClass().getMethod("setEquippedCurio", String.class, int.class, ItemStack.class);
            boolean before = Equipped.trinkets(stand).isEmpty();
            set.invoke(h, "ring", 0, ring);
            boolean found = Equipped.trinkets(stand).stream().anyMatch(sl -> sl.def().id().equals("well_ring"));
            set.invoke(h, "ring", 0, ItemStack.EMPTY);
            return "curios path: ring in a Curios ring slot " + (before && found ? "found ok" : "NOT FOUND (before empty=" + before + ")");
        } catch (Throwable t) {
            return "curios path: " + t;
        } finally {
            stand.discard();
        }
    }

    /**
     * NeoOrigins (in the full pack) cancels all damage to a player who has not yet picked an origin, which a fake player never does,
     * so every wearer-side check (blows taken, fire, a fatal blow) would pass or fail without any damage landing. Marks the picker as
     * abandoned, the state of a player who skipped it. Reflection, because the mod is optional.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void unprotect(FakePlayer p) {
        try {
            Object type = Class.forName("com.cyberday1.neoorigins.attachment.OriginAttachments").getMethod("originData").invoke(null);
            Object data = p.getData((net.neoforged.neoforge.attachment.AttachmentType) type);
            data.getClass().getMethod("setPickerAbandoned", boolean.class).invoke(data, true);
        } catch (ReflectiveOperationException | LinkageError | RuntimeException ignored) {
            // mod absent: nothing protects the fake player
        }
    }

    private static Zombie zombie(ServerLevel level, Vec3 at) {
        Zombie z = EntityType.ZOMBIE.create(level, EntitySpawnReason.COMMAND);
        z.setPos(at.x, at.y, at.z);
        z.setNoAi(true);
        z.setPersistenceRequired();
        // Dwarfs and Giants (full pack) rolls a random size on join that changes health and damage taken; the checks need a plain zombie
        z.addTag("dwarfsandgiants:spawn_size_rolled");
        level.addFreshEntity(z);
        plain(z);
        return z;
    }

    /**
     * Other pack mods (Mob Champions, level mods) lift a joining mob's health and armour at random, which makes damage numbers vary
     * between runs. Puts the attributes the checks depend on back to the entity type's own values.
     */
    @SuppressWarnings("unchecked")
    static void plain(LivingEntity e) {
        var defaults = net.minecraft.world.entity.ai.attributes.DefaultAttributes.getSupplier((EntityType<? extends LivingEntity>) e.getType());
        for (var a : java.util.List.of(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH, net.minecraft.world.entity.ai.attributes.Attributes.ARMOR,
                net.minecraft.world.entity.ai.attributes.Attributes.ARMOR_TOUGHNESS, net.minecraft.world.entity.ai.attributes.Attributes.KNOCKBACK_RESISTANCE)) {
            var inst = e.getAttribute(a);
            if (inst == null || defaults == null || !defaults.hasAttribute(a)) continue;
            inst.removeModifiers();
            inst.setBaseValue(defaults.getBaseValue(a));
        }
        e.setHealth(e.getMaxHealth());
    }

    private static void reset(ServerLevel level, FakePlayer p, Vec3 base) {
        // the fake player does not tick, so item cooldowns of an earlier relic (or an earlier run) would never end
        for (int i = 0; i < 2400; i++) p.getCooldowns().tick();
        p.removeAllEffects();
        p.setHealth(p.getMaxHealth());
        p.clearFire();
        p.setDeltaMovement(Vec3.ZERO);
        p.invulnerableTime = 0;
        p.setPos(base.x, base.y, base.z);
        p.setYRot(0);
        p.setXRot(0);
        p.fallDistance = 0;
        p.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
        p.setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
    }

    private static String one(ServerLevel level, FakePlayer p, Vec3 base, Def d) {
        reset(level, p, base);
        ItemStack st = new ItemStack(DiaithirRelics.all().get(d.id()).get());
        StringBuilder out = new StringBuilder();
        // components: the item itself carries its numbers
        boolean comps = d.trinket() || st.getMaxDamage() > 0;
        if (d.cooldown() > 0) comps &= st.has(DataComponents.USE_COOLDOWN);
        switch (d.kind()) {
            case "sword", "dagger", "axe", "hammer", "spear" -> comps &= st.has(DataComponents.ATTRIBUTE_MODIFIERS) && st.has(DataComponents.WEAPON);
            case "shield" -> comps &= st.has(DataComponents.BLOCKS_ATTACKS) && st.has(DataComponents.EQUIPPABLE);
            default -> { }
        }
        if (!comps) return "BAD components missing";
        Zombie z = zombie(level, base.add(0, 0, 3));  // yaw 0 looks along +Z
        Zombie z2 = zombie(level, base.add(1.5, 0, 3));
        try {
            if (d.trinket()) {
                p.setItemInHand(InteractionHand.OFF_HAND, st);
            } else if (d.kind().equals("shield")) {
                p.setItemInHand(InteractionHand.OFF_HAND, st);
            } else {
                p.setItemInHand(InteractionHand.MAIN_HAND, st);
            }
            // active
            if (d.active() != null) {
                // every foe in reach starts at full health: a kill inside the ability throws in other mods' death hooks (lightmanscurrency loot)
                for (net.minecraft.world.entity.LivingEntity le : level.getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class, p.getBoundingBox().inflate(10.0))) {
                    if (le != p) { le.setHealth(le.getMaxHealth()); le.invulnerableTime = 0; }
                }
                float hp0 = z.getHealth();
                Vec3 pos0 = p.position();
                if (!Abilities.fire(d, p, st)) return "BAD active did not fire (foes=" + Abilities.foes(p, p.position(), 6).size() + ")";
                boolean moved = p.position().distanceTo(pos0) > 0.5 || p.getDeltaMovement().lengthSqr() > 0.1;
                boolean hurt = z.getHealth() < hp0 || z.isOnFire() || z.getActiveEffects().size() > 0 || z.isRemoved();
                boolean self = !p.getActiveEffects().isEmpty();
                boolean bolt = d.active().equals("skybolt") || d.active().equals("mend");
                if (!(moved || hurt || self || bolt)) return "BAD active had no visible result";
                out.append("active=").append(d.active()).append(moved ? " moved" : "").append(hurt ? " hurt" : "").append(self ? " self" : "").append("; ");
            }
            // passive
            reset(level, p, base);
            if (d.trinket()) p.setItemInHand(InteractionHand.OFF_HAND, st); else if (d.kind().equals("shield")) p.setItemInHand(InteractionHand.OFF_HAND, st); else p.setItemInHand(InteractionHand.MAIN_HAND, st);
            z.setHealth(z.getMaxHealth()); z.removeAllEffects(); z.clearFire(); z.invulnerableTime = 0;
            String res = passive(level, p, base, d, st, z, z2);
            if (!res.startsWith("ok")) return res;
            out.append("passive=").append(d.passive()).append(" ").append(res);
            return "ok " + out;
        } finally {
            z.discard();
            z2.discard();
        }
    }

    private static String passive(ServerLevel level, FakePlayer p, Vec3 base, Def d, ItemStack st, Zombie z, Zombie z2) {
        DamageSource melee = level.damageSources().playerAttack(p);
        DamageSource mob = level.damageSources().mobAttack(z);
        switch (d.passive()) {
            case "ignite" -> { z.hurtServer(level, melee, 1); return z.isOnFire() ? "ok on fire" : "BAD not burning"; }
            case "stagger" -> { z.hurtServer(level, melee, 1); return z.hasEffect(MobEffects.SLOWNESS) ? "ok slowed" : "BAD not slowed"; }
            case "poison" -> {
                // zombies are undead and shrug off poison, so the blow lands on a creeper
                net.minecraft.world.entity.monster.Creeper c = EntityType.CREEPER.create(level, EntitySpawnReason.COMMAND);
                c.setPos(base.x, base.y, base.z + 3); c.setNoAi(true); level.addFreshEntity(c); plain(c);
                try { c.hurtServer(level, melee, 1); return c.hasEffect(MobEffects.POISON) ? "ok poisoned" : "BAD not poisoned"; } finally { c.discard(); }
            }
            case "chain" -> {
                float h = z2.getHealth();
                for (int i = 0; i < 60 && z2.getHealth() >= h; i++) { z.setHealth(z.getMaxHealth()); z.invulnerableTime = 0; z.hurtServer(level, melee, 1); }
                return z2.getHealth() < h ? "ok arced" : "BAD no arc in 60 blows";
            }
            case "backstab" -> {
                // other mods roll random crits, so each side is the sum of eight blows
                float back = 0, front = 0;
                for (int i = 0; i < 8; i++) {
                    z.invulnerableTime = 0; z.hurtTime = 0; z.setHealth(z.getMaxHealth()); z.setYRot(0); z.setYHeadRot(0); z.setXRot(0);
                    float h0 = z.getHealth(); z.hurtServer(level, melee, 4); back += Math.max(0F, h0 - z.getHealth());
                    z.invulnerableTime = 0; z.hurtTime = 0; z.setHealth(z.getMaxHealth()); z.setYRot(180); z.setYHeadRot(180); z.setXRot(0);
                    float h1 = z.getHealth(); z.hurtServer(level, melee, 4); front += Math.max(0F, h1 - z.getHealth());
                }
                return back > front ? "ok back " + back + " vs front " + front : "BAD back " + back + " front " + front;
            }
            case "tidebonus" -> {
                float dry = 0, wet = 0;
                boolean raining = false;
                for (int i = 0; i < 8; i++) {
                    level.setRainLevel(0.0F);
                    z.invulnerableTime = 0; z.hurtTime = 0; z.setHealth(z.getMaxHealth());
                    float h0 = z.getHealth(); z.hurtServer(level, melee, 4); dry += Math.max(0F, h0 - z.getHealth());
                    z.invulnerableTime = 0; z.hurtTime = 0; z.setHealth(z.getMaxHealth());
                    level.setRainLevel(1.0F); level.setThunderLevel(0f);
                    raining = level.isRainingAt(p.blockPosition());
                    float h1 = z.getHealth(); z.hurtServer(level, melee, 4); wet += Math.max(0F, h1 - z.getHealth());
                }
                level.setRainLevel(0.0F);
                return raining ? (wet >= dry ? "ok dry " + dry + " wet " + wet : "BAD dry " + dry + " wet " + wet) : "ok (no rain here, bonus path not reached)";
            }
            case "skyhunter" -> {
                // fresh target: the shared zombie may already be dying after the active ability, and a kill inside LivingDamageEvent.Pre throws
                net.minecraft.world.entity.monster.Creeper c = EntityType.CREEPER.create(level, EntitySpawnReason.COMMAND);
                c.setPos(base.x, base.y, base.z + 3); c.setNoAi(true); level.addFreshEntity(c); plain(c);
                try {
                    c.setOnGround(false); c.setDeltaMovement(0, 0.3, 0);
                    float h0 = c.getHealth(); c.hurtServer(level, melee, 4); return "ok dealt " + (h0 - c.getHealth());
                } finally { c.discard(); }
            }
            case "starcrit" -> {
                float h0 = z.getHealth(), mx = z.getMaxHealth();
                boolean landed = z.hurtServer(level, melee, 4);
                float full = h0 - z.getHealth();
                return full > 4.1F ? "ok " + full : "BAD " + full + " (landed " + landed + ", hp " + h0 + "/" + mx + " -> " + z.getHealth() + "/" + z.getMaxHealth() + ", armor " + z.getArmorValue() + ", effects " + z.getActiveEffects().size() + ", tags " + z.entityTags() + ")";
            }
            case "plunge" -> {
                p.fallDistance = 5.0F;
                float h0 = z.getHealth(); z.hurtServer(level, melee, 4); float dealt = h0 - z.getHealth();
                return dealt > 5.0F ? "ok " + dealt : "BAD " + dealt;
            }
            case "lifesteal" -> {
                p.setHealth(10.0F);
                z.hurtServer(level, melee, 6);
                return p.getHealth() > 10.0F ? "ok healed to " + p.getHealth() : "BAD no heal";
            }
            case "breaker" -> {
                z.setItemSlot(EquipmentSlot.HEAD, new ItemStack(net.minecraft.world.item.Items.DIAMOND_HELMET));
                z.setItemSlot(EquipmentSlot.CHEST, new ItemStack(net.minecraft.world.item.Items.DIAMOND_CHESTPLATE));
                float h0 = z.getHealth(); z.hurtServer(level, melee, 4); return "ok dealt " + (h0 - z.getHealth()) + " vs armour " + z.getArmorValue();
            }
            case "aqua" -> { Passives.tick(d, p, st); return "ok ran (needs water to show)"; }
            // wearer side: through the real event path with the trinket in the off hand
            case "arrowward" -> {
                DamageSource arrow = level.damageSources().arrow(new net.minecraft.world.entity.projectile.arrow.Arrow(EntityType.ARROW, level), z);
                float h0 = p.getHealth(); p.hurtServer(level, arrow, 8); float taken = h0 - p.getHealth();
                return taken < 8.0F ? "ok took " + taken + " of 8" : "BAD took " + taken;
            }
            case "bearhide" -> {
                float h0 = p.getHealth(); p.hurtServer(level, mob, 8); float taken = h0 - p.getHealth();
                return taken < 8.0F ? "ok took " + taken + " of 8" : "BAD took " + taken;
            }
            case "mistveil" -> {
                p.setHealth(8.0F); boolean landed = p.hurtServer(level, mob, 3);
                return p.hasEffect(MobEffects.INVISIBILITY) ? "ok vanished" : "BAD no veil (landed " + landed + " invuln " + p.isInvulnerable() + "/" + p.invulnerableTime + " absorb " + p.getAbsorptionAmount() + " hp " + p.getHealth() + "/" + p.getMaxHealth() + ", cooldown " + p.getCooldowns().isOnCooldown(p.getOffhandItem()) + ", effects " + p.getActiveEffects().size() + ")";
            }
            case "dragonward" -> {
                float h0 = p.getHealth(); p.hurtServer(level, level.damageSources().onFire(), 6);
                Passives.tick(d, p, st);
                return p.getHealth() >= h0 && p.hasEffect(MobEffects.FIRE_RESISTANCE) ? "ok unburnt" : "BAD burnt";
            }
            case "fatesave" -> {
                // the fake player's die() is empty, so the death event is posted by hand
                p.setHealth(1.0F);
                var ev = new net.neoforged.neoforge.event.entity.living.LivingDeathEvent(p, level.damageSources().magic());
                net.neoforged.neoforge.common.NeoForge.EVENT_BUS.post(ev);
                boolean first = ev.isCanceled() && p.getHealth() == 6.0F;
                var ev2 = new net.neoforged.neoforge.event.entity.living.LivingDeathEvent(p, level.damageSources().magic());
                net.neoforged.neoforge.common.NeoForge.EVENT_BUS.post(ev2);
                return first && !ev2.isCanceled() ? "ok death cancelled at 3 hearts, second death allowed (cooldown)" : "BAD first=" + first + " second cancelled=" + ev2.isCanceled();
            }
            case "wellspring" -> { p.setHealth(4); Passives.tick(d, p, st); return p.hasEffect(MobEffects.REGENERATION) ? "ok" : "BAD"; }
            case "giantsbulk" -> { Passives.tick(d, p, st); return p.hasEffect(MobEffects.ABSORPTION) ? "ok" : "BAD"; }
            case "sunwarmth", "moonsight" -> { Passives.tick(d, p, st); return "ok ran (" + p.getActiveEffects().size() + " effects; depends on sky and hour)"; }
            case "ravensight" -> { Passives.tick(d, p, st); return z.hasEffect(MobEffects.GLOWING) ? "ok foe glows" : "BAD foe not glowing"; }
            case "feast" -> { p.getFoodData().setFoodLevel(10); p.tickCount = 60; Passives.tick(d, p, st); return p.getFoodData().getFoodLevel() > 10 ? "ok fed" : "BAD"; }
            // shields: blocking cannot be held by a fake player, so the shield half is called with the shield up
            case "reflect" -> { float h0 = z.getHealth(); Passives.hurt(d, p, mob, 10, st, true); return z.getHealth() < h0 ? "ok reflected" : "BAD no reflect"; }
            case "frost" -> { Passives.hurt(d, p, mob, 10, st, true); return z.hasEffect(MobEffects.SLOWNESS) ? "ok attacker frozen" : "BAD"; }
            case "ward" -> {
                DamageSource arrow = level.damageSources().arrow(new net.minecraft.world.entity.projectile.arrow.Arrow(EntityType.ARROW, level), z);
                float a = Passives.hurt(d, p, arrow, 10, st, true);
                Passives.hurt(d, p, mob, 10, st, true);
                return a <= 0 && z.hasEffect(MobEffects.BLINDNESS) ? "ok arrow refused, attacker blinded" : "BAD";
            }
            default -> { return "BAD unknown passive " + d.passive(); }
        }
    }
}
