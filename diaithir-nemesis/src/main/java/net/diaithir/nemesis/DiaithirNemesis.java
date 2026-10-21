package net.diaithir.nemesis;

import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.slf4j.Logger;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

@Mod(DiaithirNemesis.ID)
public final class DiaithirNemesis {
    public static final String ID = "diaithirnemesis";
    private static final Logger LOG = LogUtils.getLogger();
    static final Rules RULES = Rules.load();
    static final Random RANDOM = new Random();

    public DiaithirNemesis() {
        NeoForge.EVENT_BUS.addListener(LivingDeathEvent.class, DiaithirNemesis::onDeath);
        NeoForge.EVENT_BUS.addListener(LivingDamageEvent.Post.class, DiaithirNemesis::onDamage);
        NeoForge.EVENT_BUS.addListener(EntityJoinLevelEvent.class, DiaithirNemesis::onJoin);
        NeoForge.EVENT_BUS.addListener(ServerTickEvent.Post.class, DiaithirNemesis::onTick);
        NeoForge.EVENT_BUS.addListener(PlayerEvent.PlayerRespawnEvent.class, DiaithirNemesis::onRespawn);
        NeoForge.EVENT_BUS.addListener(RegisterCommandsEvent.class, e -> NemesisCommands.register(e.getDispatcher()));
        listenToAges();
    }

    // diaithir-ages posts a StoryEvent for Age Turns and House changes; read it by reflection so neither mod needs the other.
    @SuppressWarnings("unchecked")
    private static void listenToAges() {
        try {
            Class<? extends Event> cls = (Class<? extends Event>) Class.forName("net.diaithir.ages.mc.StoryEvent");
            Field kind = cls.getField("kind"), key = cls.getField("key"), value = cls.getField("value");
            NeoForge.EVENT_BUS.addListener(EventPriority.NORMAL, false, cls, e -> {
                try {
                    onStory((String) kind.get(e), (String) key.get(e), (String) value.get(e));
                } catch (IllegalAccessException ex) {
                    LOG.warn("Could not read a diaithir-ages story event", ex);
                }
            });
            LOG.info("Listening to diaithir-ages for Age Turns and Houses");
        } catch (ClassNotFoundException e) {
            LOG.info("diaithir-ages not loaded; nemeses stay in Age I and hold grudges against players only");
        } catch (NoSuchFieldException e) {
            LOG.warn("diaithir-ages StoryEvent has changed shape; Age Turns and Houses are not followed", e);
        }
    }

    private static void onStory(String kind, String key, String value) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return;
        NemesisData data = NemesisData.get(server);
        switch (kind) {
            case "age" -> { if (!value.equals(data.age)) succession(server, value); }
            case "house_found", "house_join" -> data.houses.put(key, value);
            case "house_leave" -> data.houses.remove(key);
            default -> { return; }
        }
        data.setDirty();
    }

    private static void onDeath(LivingDeathEvent event) {
        MinecraftServer server = event.getEntity().level().getServer();
        if (server == null) return;
        if (event.getEntity() instanceof ServerPlayer player
                && event.getSource().getEntity() instanceof Mob killer && killer instanceof Enemy && killer.isAlive()) {
            promote(server, NemesisData.get(server), killer, player);
        } else if (event.getEntity() instanceof Mob mob && idOf(mob) > 0) {
            NemesisData data = NemesisData.get(server);
            if (data.byId(idOf(mob)) instanceof Nemesis n) fall(server, data, mob, n, event);
        }
    }

    private static void promote(MinecraftServer server, NemesisData data, Mob killer, ServerPlayer player) {
        String uuid = player.getStringUUID(), house = data.house(uuid);
        Nemesis n = find(data, killer);
        if (n != null) {
            RULES.rankUp(n, uuid, house, RANDOM);
        } else {
            if (data.nemeses.stream().filter(x -> x.players.contains(uuid)).count() >= RULES.maxPerPlayer) return;
            n = RULES.promote(data.nextId++, BuiltInRegistries.ENTITY_TYPE.getKey(killer.getType()).toString(), data.age, uuid, house, RANDOM);
            n.entity = killer.getStringUUID();
            data.nemeses.add(n);
        }
        String took = "";
        if (data.trophy(n).isEmpty() && !player.getMainHandItem().isEmpty()) {
            ItemStack stack = player.getMainHandItem().copy();
            player.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
            data.setTrophy(n, stack);
            took = " It took their " + stack.getHoverName().getString() + ".";
        }
        Traits.apply(killer, n, RULES);
        arm(killer, data, n);
        n.seen = seen(killer);
        data.shaken.put(uuid, n.id);
        data.setDirty();
        broadcast(server, player.getName().getString() + " was slain by " + n.fullName() + ", now " + RULES.title(n) + "." + took);
    }

    private static void fall(MinecraftServer server, NemesisData data, Mob mob, Nemesis n, LivingDeathEvent event) {
        if (!n.cheated && RANDOM.nextDouble() < RULES.cheatDeathChance) {
            event.setCanceled(true);
            n.cheated = true;
            RULES.addTrait(n, RANDOM);
            n.entity = "";
            n.seen = seen(mob);
            mob.discard();
            broadcast(server, n.fullName() + " escapes, scarred. It will be back.");
        } else {
            List<String> freed = data.shaken.entrySet().stream().filter(e -> e.getValue() == n.id).map(Map.Entry::getKey).toList();
            data.remove(n);
            for (String p : freed) {
                ServerPlayer sp = server.getPlayerList().getPlayer(UUID.fromString(p));
                if (sp != null) sp.removeEffect(MobEffects.WEAKNESS);
            }
            Entity by = event.getSource().getEntity();
            broadcast(server, n.fullName() + " has fallen" + (by instanceof ServerPlayer p ? " to " + p.getName().getString() : "") + ".");
        }
        data.setDirty();
    }

    private static void onDamage(LivingDamageEvent.Post event) {
        if (!(event.getSource().getEntity() instanceof Mob attacker) || idOf(attacker) < 0) return;
        MinecraftServer server = attacker.level().getServer();
        if (server != null && NemesisData.get(server).byId(idOf(attacker)) instanceof Nemesis n) Traits.onHit(attacker, event.getEntity(), n);
    }

    // A copy left in an unloaded chunk after its nemesis died, escaped or ambushed elsewhere is dropped when it loads.
    private static void onJoin(EntityJoinLevelEvent event) {
        if (event.loadedFromDisk() && event.getEntity() instanceof Mob mob && idOf(mob) > 0 && mob.level().getServer() != null) {
            Nemesis n = find(NemesisData.get(mob.level().getServer()), mob);
            if (n == null || !n.entity.equals(mob.getStringUUID())) event.setCanceled(true);
        }
    }

    private static void onTick(ServerTickEvent.Post event) {
        MinecraftServer server = event.getServer();
        long now = server.overworld().getGameTime();
        if (now % RULES.ambushCheckTicks != 0) return;
        NemesisData data = NemesisData.get(server);
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (player.isSpectator() || player.isCreative() || nearSpawn(player)) continue;
            for (Nemesis n : data.against(player.getStringUUID())) {
                Entity body = loaded(server, n);
                if (body != null) n.seen = seen(body);
                if (now - n.lastAmbush < RULES.ambushCooldownTicks || body != null) continue;
                if (RANDOM.nextDouble() < RULES.ambushChance && ambush(server, data, n, player)) break;
            }
        }
    }

    // Shaken: weakened until the nemesis that last killed you falls. Death clears effects, so it is put back on every respawn.
    private static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && player.level().getServer() instanceof MinecraftServer server
                && NemesisData.get(server).shaken.containsKey(player.getStringUUID())) {
            player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, MobEffectInstance.INFINITE_DURATION));
            player.sendSystemMessage(Component.literal("You are Shaken until the one who felled you falls."));
        }
    }

    static String seen(Entity e) { return e.getBlockX() + ", " + e.getBlockZ(); }

    private static boolean nearSpawn(ServerPlayer player) {
        BlockPos spawn = player.level().getRespawnData().pos();
        return player.blockPosition().distSqr(spawn) < (double) RULES.safeRadius * RULES.safeRadius;
    }

    /** Brings the nemesis back near the player, with a pack of its kind if it is a caller. */
    static boolean ambush(MinecraftServer server, NemesisData data, Nemesis n, ServerPlayer player) {
        ServerLevel level = player.level();
        EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.getOptional(Identifier.parse(n.type)).orElse(null);
        BlockPos pos = spotNear(level, player.blockPosition());
        if (type == null || pos == null) return false;
        despawn(server, n);
        if (!(type.spawn(level, pos, EntitySpawnReason.EVENT) instanceof Mob mob)) return false;
        n.entity = mob.getStringUUID();
        n.seen = seen(mob);
        n.lastAmbush = server.overworld().getGameTime();
        Traits.apply(mob, n, RULES);
        arm(mob, data, n);
        mob.setTarget(player);
        if (Traits.has(n, "caller")) {
            for (int i = 0; i < 1 + n.rank / 2; i++) {
                if (type.spawn(level, pos, EntitySpawnReason.EVENT) instanceof Mob kin) kin.setTarget(player);
            }
        }
        data.setDirty();
        player.sendSystemMessage(Component.literal(n.fullName() + ", " + RULES.title(n) + ", has found you."));
        return true;
    }

    /** At an Age Turn every nemesis is succeeded by its heir, who keeps the grudge. */
    static int succession(MinecraftServer server, String newAge) {
        NemesisData data = NemesisData.get(server);
        List<Nemesis> sires = List.copyOf(data.nemeses);
        data.nemeses.clear();
        for (Nemesis sire : sires) {
            despawn(server, sire);
            Nemesis heir = RULES.heir(sire, data.nextId++, newAge, RANDOM);
            data.inherit(sire, heir);
            data.nemeses.add(heir);
        }
        data.age = newAge;
        data.setDirty();
        if (!sires.isEmpty()) broadcast(server, "The old grudges pass to " + sires.size() + (sires.size() == 1 ? " heir." : " heirs."));
        return sires.size();
    }

    /** The trophy rides in the off hand, so a bow-user keeps its bow, and always drops when the nemesis dies. */
    private static void arm(Mob mob, NemesisData data, Nemesis n) {
        ItemStack trophy = data.trophy(n);
        if (trophy.isEmpty()) return;
        mob.setItemSlot(EquipmentSlot.OFFHAND, trophy.copy());
        mob.setGuaranteedDrop(EquipmentSlot.OFFHAND);
    }

    static Nemesis find(NemesisData data, Mob mob) { return data.byId(idOf(mob)); }

    /** The nemesis id a mob carries in its tag, or -1. */
    static int idOf(Mob mob) {
        for (String t : mob.entityTags()) {
            if (t.startsWith(Nemesis.TAG)) {
                try {
                    return Integer.parseInt(t.substring(Nemesis.TAG.length()));
                } catch (NumberFormatException e) {
                    return -1;
                }
            }
        }
        return -1;
    }

    static void despawn(MinecraftServer server, Nemesis n) {
        Entity e = loaded(server, n);
        if (e != null) e.discard();
    }

    static Entity loaded(MinecraftServer server, Nemesis n) {
        if (n.entity.isEmpty()) return null;
        UUID id = UUID.fromString(n.entity);
        for (ServerLevel level : server.getAllLevels()) {
            Entity e = level.getEntity(id);
            if (e != null && e.isAlive()) return e;
        }
        return null;
    }

    private static BlockPos spotNear(ServerLevel level, BlockPos at) {
        for (int attempt = 0; attempt < 16; attempt++) {
            double a = RANDOM.nextDouble() * Math.PI * 2;
            int d = 16 + RANDOM.nextInt(12);
            int x = at.getX() + (int) (Math.cos(a) * d), z = at.getZ() + (int) (Math.sin(a) * d);
            if (!level.isLoaded(new BlockPos(x, at.getY(), z))) continue;
            BlockPos top = new BlockPos(x, level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z), z);
            if (level.getFluidState(top.below()).isEmpty() && Math.abs(top.getY() - at.getY()) < 12) return top;
        }
        return null;
    }

    private static void broadcast(MinecraftServer server, String text) {
        server.getPlayerList().broadcastSystemMessage(Component.literal(text), false);
    }
}
