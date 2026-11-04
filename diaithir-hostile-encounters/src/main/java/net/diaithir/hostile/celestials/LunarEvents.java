package net.diaithir.hostile.celestials;

import com.mojang.brigadier.arguments.StringArgumentType;
import java.util.List;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.tags.TagKey;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;
import net.neoforged.neoforge.event.AnvilUpdateEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.CanPlayerSleepEvent;
import net.neoforged.neoforge.event.entity.player.PlayerXpEvent;
import net.neoforged.neoforge.event.level.BlockDropsEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

/** Applies the active lunar event: announcements, horde pressure, mob effects, xp, harvest drops, sleep and anvil costs. */
public final class LunarEvents {
    private static String lastActive = Lunar.DEFAULT_ID;

    private static ServerLevel overworld(net.minecraft.server.MinecraftServer s) { return s.getLevel(Level.OVERWORLD); }

    private static Optional<Lunar.Event> active(Level level) {
        if (!(level instanceof ServerLevel sl) || sl.dimension() != Level.OVERWORLD) return Optional.empty();
        return Optional.of(Forecast.of(sl).current(sl));
    }

    /** Beacon range multiplier of the moon that is up now (1 outside the overworld or without an event). */
    public static double beaconMultiplier(Level level) {
        return active(level).map(ev -> ev.beacon_radius).orElse(1.0);
    }

    private static void sync(ServerLevel level, Lunar.Event ev, ServerPlayer only) {
        var pkt = new LunarSync(ev.id, LunarSync.tint(ev));
        if (only != null) net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(only, pkt);
        else for (ServerPlayer p : level.players()) net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(p, pkt);
    }

    @SubscribeEvent
    public static void join(net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent e) {
        if (e.getEntity() instanceof ServerPlayer sp && sp.level() instanceof ServerLevel sl && sl.dimension() == Level.OVERWORLD)
            sync(sl, Forecast.of(sl).current(sl), sp);
    }

    @SubscribeEvent
    public static void enchant(net.neoforged.neoforge.event.enchanting.EnchantmentLevelSetEvent e) {
        active(e.getLevel()).ifPresent(ev -> {
            if (ev.enchant_cost != 1.0) e.setEnchantLevel(Math.max(1, (int) Math.round(e.getEnchantLevel() * ev.enchant_cost)));
        });
    }

    @SubscribeEvent
    public static void reload(AddServerReloadListenersEvent e) {
        e.addListener(Identifier.fromNamespaceAndPath(DiaithirHostile.ID, "lunar_events"), new SimplePreparableReloadListener<Boolean>() {
            @Override protected Boolean prepare(ResourceManager manager, ProfilerFiller profiler) { return Boolean.TRUE; }
            @Override protected void apply(Boolean prepared, ResourceManager manager, ProfilerFiller profiler) { Lunar.reload(manager); }
        });
    }

    private static Component notice(String key, Lunar.Event ev) {
        ChatFormatting f = ChatFormatting.getByName(ev.chat_color);
        return Component.translatable(key).withStyle(f == null ? ChatFormatting.WHITE : f);
    }

    @SubscribeEvent
    public static void levelTick(LevelTickEvent.Post e) {
        if (!(e.getLevel() instanceof ServerLevel level) || level.dimension() != Level.OVERWORLD) return;
        Forecast fc = Forecast.of(level);
        if (level.getGameTime() % 100 == 3) fc.update(level);
        Lunar.Event ev = fc.current(level);
        if (!ev.id.equals(lastActive)) {
            Lunar.Event old = Lunar.get(lastActive);
            for (ServerPlayer p : level.players()) {
                if (old.set != null) p.sendSystemMessage(notice(old.set, old));
                if (ev.rise != null) p.sendSystemMessage(notice(ev.rise, ev));
            }
            lastActive = ev.id;
            if (!old.id.equals(Lunar.DEFAULT_ID)) net.neoforged.neoforge.common.NeoForge.EVENT_BUS.post(new LunarEventStarted(old.id, level, false));
            if (!ev.id.equals(Lunar.DEFAULT_ID)) net.neoforged.neoforge.common.NeoForge.EVENT_BUS.post(new LunarEventStarted(ev.id, level, true));
            sync(level, ev, null);
        }
        if (ev.meteor.interval_ticks > 0 && level.getGameTime() % ev.meteor.interval_ticks == 0) Meteors.strike(level, ev.meteor);
        if (ev.boost.phantoms > 0 && level.getGameTime() % 200 == 0) phantoms(level, ev, level.players());
        if (level.getGameTime() % 20 == 0 && ev.boost.chance > 0) redGlow(level);
        if (ev.horde.cap > 0 && level.getGameTime() % Math.max(1, ev.horde.interval_ticks) == 0) horde(level, ev);
    }

    private static void redGlow(ServerLevel level) {
        for (ServerPlayer p : level.players())
            for (Mob m : level.getEntitiesOfClass(Mob.class, p.getBoundingBox().inflate(48), x -> x.entityTags().contains("lunar_boosted")))
                level.sendParticles(net.minecraft.core.particles.ParticleTypes.CRIMSON_SPORE, m.getX(), m.getY() + m.getBbHeight() / 2, m.getZ(), 6, 0.3, 0.5, 0.3, 0.02);
    }

    private static void horde(ServerLevel level, Lunar.Event ev) { horde(level, ev, level.players()); }

    /** One spawn attempt per listed player; returns how many mobs came. */
    static int horde(ServerLevel level, Lunar.Event ev, List<? extends ServerPlayer> players) {
        int spawned = 0;
        for (ServerPlayer p : players) {
            if (p.isSpectator() || !level.canSeeSky(p.blockPosition())) continue;
            int near = level.getEntitiesOfClass(Mob.class, p.getBoundingBox().inflate(ev.horde.max_distance + 16),
                m -> m.getType().getCategory() == MobCategory.MONSTER || m.entityTags().contains("lunar_horde")).size();
            if (near >= ev.horde.cap) continue;
            double a = level.getRandom().nextDouble() * Math.PI * 2;
            double d = ev.horde.min_distance + level.getRandom().nextDouble() * (ev.horde.max_distance - ev.horde.min_distance);
            int x = (int) (p.getX() + Math.cos(a) * d), z = (int) (p.getZ() + Math.sin(a) * d);
            if (!level.hasChunkAt(new BlockPos(x, 0, z))) continue;
            BlockPos pos = new BlockPos(x, level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z), z);
            EntityType<?> type = pick(level, ev, pos);
            if (type == null || !SpawnPlacements.checkSpawnRules(type, level, EntitySpawnReason.EVENT, pos, level.getRandom())) continue;
            Object o = type.create(level, EntitySpawnReason.EVENT);
            if (!(o instanceof Mob mob)) continue;
            mob.snapTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, level.getRandom().nextFloat() * 360F, 0F);
            if (!level.noCollision(mob)) continue;
            mob.addTag("lunar_horde");
            mob.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), EntitySpawnReason.EVENT, null);
            level.addFreshEntityWithPassengers(mob);
            spawned++;
        }
        return spawned;
    }

    private static EntityType<?> pick(ServerLevel level, Lunar.Event ev, BlockPos pos) {
        if (!ev.horde.pool.isEmpty()) {
            String id = ev.horde.pool.get(level.getRandom().nextInt(ev.horde.pool.size()));
            return BuiltInRegistries.ENTITY_TYPE.getOptional(Identifier.parse(id)).orElse(null);
        }
        if (!ev.horde.biome_monsters) return null;
        var list = level.getBiome(pos).value().getMobSettings().getMobs(MobCategory.MONSTER);
        return list.getRandom(level.getRandom()).map(d -> d.type()).orElse(null);
    }

    @SubscribeEvent
    public static void effects(EntityTickEvent.Post e) {
        if (!(e.getEntity() instanceof LivingEntity le) || le.level().isClientSide() || le.tickCount % 40 != 0) return;
        active(le.level()).ifPresent(ev -> applyEffects(le, ev));
    }

    static void applyEffects(LivingEntity le, Lunar.Event ev) {
        for (Lunar.Effect fx : ev.effects) {
            boolean ok = switch (fx.target) {
                case "player" -> le instanceof Player;
                case "monster" -> le.getType().getCategory() == MobCategory.MONSTER;
                default -> true;
            };
            if (!ok) continue;
            BuiltInRegistries.MOB_EFFECT.get(Identifier.parse(fx.effect)).ifPresent(h ->
                le.addEffect(new MobEffectInstance(h, fx.duration_in_ticks, fx.amplifier, fx.ambient, fx.visible, fx.show_icon)));
        }
    }

    // ---- Blood Moon (KS) behaviour: boosted mobs, essence, creeper blasts, phantoms -------------------------------------

    @SubscribeEvent
    public static void boostOnJoin(net.neoforged.neoforge.event.entity.EntityJoinLevelEvent e) {
        if (!(e.getEntity() instanceof Mob m) || e.getLevel().isClientSide() || m.getType().getCategory() != MobCategory.MONSTER) return;
        active(e.getLevel()).ifPresent(ev -> boost(m, ev));
    }

    /** Marks and strengthens a hostile mob when the event asks for it. Returns true when it was boosted. */
    static boolean boost(Mob m, Lunar.Event ev) {
        Lunar.Boost b = ev.boost;
        if (b.chance <= 0 || b.effects.isEmpty() || m.entityTags().contains("lunar_boosted") || m.getRandom().nextDouble() >= b.chance) return false;
        String id = b.effects.get(m.getRandom().nextInt(b.effects.size()));
        var fx = BuiltInRegistries.MOB_EFFECT.get(Identifier.parse(id));
        if (fx.isEmpty()) return false;
        m.addTag("lunar_boosted");
        m.addEffect(new MobEffectInstance(fx.get(), 12000, b.amplifier, false, true, true));
        return true;
    }

    @SubscribeEvent
    public static void essence(net.neoforged.neoforge.event.entity.living.LivingDropsEvent e) {
        LivingEntity le = e.getEntity();
        if (le.level().isClientSide() || !le.entityTags().contains("lunar_boosted") || !e.isRecentlyHit()) return;
        active(le.level()).ifPresent(ev -> {
            if (le.getRandom().nextDouble() < ev.boost.essence_chance)
                e.getDrops().add(new net.minecraft.world.entity.item.ItemEntity(le.level(), le.getX(), le.getY(), le.getZ(), new net.minecraft.world.item.ItemStack(MoonEssence.ITEM.get())));
        });
    }

    @SubscribeEvent
    public static void creeperBlast(net.neoforged.neoforge.event.level.ExplosionEvent.Detonate e) {
        if (!(e.getLevel() instanceof ServerLevel sl) || !(e.getExplosion().getDirectSourceEntity() instanceof net.minecraft.world.entity.monster.Creeper)) return;
        active(sl).ifPresent(ev -> {
            for (Player p : sl.getEntitiesOfClass(Player.class, new net.minecraft.world.phys.AABB(e.getExplosion().center(), e.getExplosion().center()).inflate(6))) creeperEffects(p, ev);
        });
    }

    static void creeperEffects(Player p, Lunar.Event ev) {
        for (String id : ev.boost.creeper_effects)
            BuiltInRegistries.MOB_EFFECT.get(Identifier.parse(id)).ifPresent(h -> p.addEffect(new MobEffectInstance(h, 200, 0)));
    }

    /** Keeps phantoms above each player up to the event's cap; returns how many came. */
    static int phantoms(ServerLevel level, Lunar.Event ev, List<? extends ServerPlayer> players) {
        int made = 0;
        for (ServerPlayer p : players) {
            if (ev.boost.phantoms <= 0 || p.isSpectator()) continue;
            int near = level.getEntitiesOfClass(net.minecraft.world.entity.monster.Phantom.class, p.getBoundingBox().inflate(48)).size();
            if (near >= ev.boost.phantoms) continue;
            net.minecraft.world.entity.monster.Phantom ph = EntityType.PHANTOM.create(level, EntitySpawnReason.EVENT);
            if (ph == null) continue;
            ph.snapTo(p.getX() + level.getRandom().nextInt(-8, 9), p.getY() + 16 + level.getRandom().nextInt(8), p.getZ() + level.getRandom().nextInt(-8, 9), 0F, 0F);
            ph.finalizeSpawn(level, level.getCurrentDifficultyAt(ph.blockPosition()), EntitySpawnReason.EVENT, null);
            level.addFreshEntity(ph);
            made++;
        }
        return made;
    }

    @SubscribeEvent
    public static void xp(PlayerXpEvent.XpChange e) {
        if (e.getAmount() <= 0) return;
        active(e.getEntity().level()).ifPresent(ev -> { if (ev.xp != 1.0) e.setAmount((int) Math.round(e.getAmount() * ev.xp)); });
    }

    @SubscribeEvent
    public static void drops(BlockDropsEvent e) {
        active(e.getLevel()).ifPresent(ev -> {
            if (ev.drop_tag == null || ev.drop_multiplier == 1.0) return;
            TagKey<Item> tag = TagKey.create(Registries.ITEM, Identifier.parse(ev.drop_tag));
            e.getDrops().forEach(d -> {
                if (d.getItem().is(tag)) d.getItem().setCount((int) Math.round(d.getItem().getCount() * ev.drop_multiplier));
            });
        });
    }

    @SubscribeEvent
    public static void sleep(CanPlayerSleepEvent e) {
        active(e.getEntity().level()).ifPresent(ev -> {
            if (!ev.block_sleeping) return;
            e.setProblem(Player.BedSleepingProblem.OTHER_PROBLEM);
            e.getEntity().sendOverlayMessage(Component.translatable("diaithirhostile.sleep.fail").withStyle(ChatFormatting.RED));
        });
    }

    @SubscribeEvent
    public static void anvil(AnvilUpdateEvent e) {
        active(e.getPlayer().level()).ifPresent(ev -> {
            if (ev.anvil_cost != 1.0 && e.getXpCost() > 0) e.setXpCost(Math.max(1, (int) Math.round(e.getXpCost() * ev.anvil_cost)));
        });
    }

    @SubscribeEvent
    public static void commands(RegisterCommandsEvent e) {
        e.getDispatcher().register(Commands.literal("lunar").requires(s -> s.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER))
            .then(Commands.literal("forecast").executes(c -> {
                ServerLevel lvl = overworld(c.getSource().getServer());
                List<Forecast.Entry> up = Forecast.of(lvl).upcoming(Forecast.dayOf(lvl), 12);
                if (up.isEmpty()) c.getSource().sendSuccess(() -> Component.translatable("diaithirhostile.forecast.empty"), false);
                for (Forecast.Entry en : up) {
                    Lunar.Event ev = Lunar.get(en.event());
                    long in = en.day() - Forecast.dayOf(lvl);
                    c.getSource().sendSuccess(() -> Component.translatable("diaithirhostile.name." + ev.id.substring(ev.id.indexOf(':') + 1))
                        .append(Component.translatable("diaithirhostile.forecast.days", in)), false);
                }
                return up.size();
            }))
            .then(Commands.literal("recompute").executes(c -> {
                ServerLevel lvl = overworld(c.getSource().getServer());
                Forecast.of(lvl).recompute(lvl);
                c.getSource().sendSuccess(() -> Component.translatable("diaithirhostile.forecast.recomputed"), true);
                return 1;
            }))
            .then(Commands.literal("set").then(Commands.argument("event", StringArgumentType.word())
                .suggests((c, b) -> SharedSuggestionProvider.suggest(Lunar.events().keySet().stream().map(s -> s.substring(s.indexOf(':') + 1)), b))
                .executes(c -> {
                    String arg = StringArgumentType.getString(c, "event");
                    String id = arg.contains(":") ? arg : DiaithirHostile.ID + ":" + arg;
                    if (!Lunar.events().containsKey(id)) { c.getSource().sendFailure(Component.translatable("diaithirhostile.command.unknown", arg)); return 0; }
                    ServerLevel lvl = overworld(c.getSource().getServer());
                    Forecast.of(lvl).force(lvl, id);
                    return 1;
                }))));
    }

    private LunarEvents() {}
}
