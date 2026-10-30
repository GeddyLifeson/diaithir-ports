package net.diaithir.activity;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.logging.LogUtils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.block.BreakBlockEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * The server decides what each player is doing from game events and sends a short id to the players who can see them.
 * Clients only draw it, see {@link ActivityRenderer}.
 */
@Mod(DiaithirActivity.ID)
public final class DiaithirActivity {
    public static final String ID = "diaithiractivity";
    static final Logger LOG = LogUtils.getLogger();
    /** Every jar may ship this file; they are merged. The user's config copy comes last and wins. */
    static final String DATA_PATH = "data/" + ID + "/activity.json";
    static final String USER_FILE = "diaithiractivity-mappings.json";

    private static ActivityData data = loadData();
    private static OptOut optOut;
    private static final Map<UUID, PlayerState> STATES = new HashMap<>();

    private static final class PlayerState {
        final Map<String, Long> last = new HashMap<>();
        double x, y, z;
        float yRot, xRot;
        long lastActive;
        String sent = "";
        boolean seen;
    }

    public DiaithirActivity(IEventBus modBus, ModContainer container) {
        container.registerConfig(ModConfig.Type.COMMON, ActivityConfig.COMMON);
        container.registerConfig(ModConfig.Type.CLIENT, ActivityConfig.CLIENT);
        modBus.addListener(RegisterPayloadHandlersEvent.class, e ->
            e.registrar("1").optional().playToClient(ActivityPayload.TYPE, ActivityPayload.CODEC,
                (payload, ctx) -> {
                    LOG.debug("Activity of entity {} is '{}'", payload.entityId(), payload.activity());
                    ClientActivity.set(payload.entityId(), payload.activity());
                }));
        IEventBus bus = NeoForge.EVENT_BUS;
        bus.addListener(ServerStartingEvent.class, e -> {
            data = loadData();
            optOut = new OptOut(e.getServer().getWorldPath(LevelResource.ROOT).resolve("data").resolve("diaithiractivity_optout.json"));
            LOG.info("Activity: {} activities, {} tool rules, {} menu rules", data.defs().size(), data.rules("tools").size(), data.rules("menus").size());
        });
        bus.addListener(ServerStoppedEvent.class, e -> STATES.clear());
        bus.addListener(ServerTickEvent.Post.class, e -> tick(e.getServer()));
        bus.addListener(RegisterCommandsEvent.class, e -> command(e.getDispatcher()));
        bus.addListener(PlayerEvent.PlayerLoggedOutEvent.class, e -> STATES.remove(e.getEntity().getUUID()));
        bus.addListener(PlayerEvent.StartTracking.class, e -> {
            if (e.getTarget() instanceof ServerPlayer target && e.getEntity() instanceof ServerPlayer watcher) {
                PlayerState st = STATES.get(target.getUUID());
                if (st != null && !st.sent.isEmpty()) PacketDistributor.sendToPlayer(watcher, new ActivityPayload(target.getId(), st.sent));
            }
        });
        bus.addListener(BreakBlockEvent.class, e -> {
            if (!(e.getPlayer() instanceof ServerPlayer p)) return;
            breaking(p, e.getState());
        });
        bus.addListener(PlayerInteractEvent.LeftClickBlock.class, e -> {
            if (e.getEntity() instanceof ServerPlayer p && e.getAction() == PlayerInteractEvent.LeftClickBlock.Action.START) {
                breaking(p, p.level().getBlockState(e.getPos()));
            }
        });
        bus.addListener(BlockEvent.EntityPlaceEvent.class, e -> {
            if (e.getEntity() instanceof ServerPlayer p) mark(p, "building");
        });
        bus.addListener(PlayerInteractEvent.RightClickBlock.class, e -> {
            if (!(e.getEntity() instanceof ServerPlayer p)) return;
            mark(p, data.resolve("stations", keys(p.level().getBlockState(e.getPos()))));
            mark(p, data.resolve("uses", keys(e.getItemStack())));
        });
        bus.addListener(PlayerInteractEvent.RightClickItem.class, e -> {
            if (e.getEntity() instanceof ServerPlayer p) mark(p, data.resolve("uses", keys(e.getItemStack())));
        });
        bus.addListener(AttackEntityEvent.class, e -> {
            if (e.getEntity() instanceof ServerPlayer p) mark(p, "fighting");
        });
        bus.addListener(LivingDamageEvent.Post.class, e -> {
            if (e.getSource().getEntity() instanceof ServerPlayer p && e.getEntity() != p) mark(p, "fighting");
            if (e.getEntity() instanceof ServerPlayer p && e.getSource().getEntity() instanceof LivingEntity && e.getSource().getEntity() != p) mark(p, "fighting");
        });
        if (net.neoforged.fml.loading.FMLEnvironment.getDist().isClient()) ActivityRenderer.register(bus, modBus);
    }

    // ---- data -------------------------------------------------------------

    static ActivityData loadData() {
        List<JsonObject> docs = new ArrayList<>();
        try {
            // the jar's own file first so that other mods' files and then the user's file override it
            List<URL> urls = java.util.Collections.list(DiaithirActivity.class.getClassLoader().getResources(DATA_PATH));
            urls.sort((a, b) -> Boolean.compare(b.toString().contains("diaithir-activity"), a.toString().contains("diaithir-activity")));
            for (URL u : urls) {
                try (Reader r = new InputStreamReader(u.openStream(), StandardCharsets.UTF_8)) {
                    docs.add(JsonParser.parseReader(r).getAsJsonObject());
                } catch (RuntimeException | IOException e) {
                    LOG.warn("Skipping unreadable activity mapping {}: {}", u, e.toString());
                }
            }
            Path user = FMLPaths.CONFIGDIR.get().resolve(USER_FILE);
            if (Files.exists(user)) {
                try (Reader r = Files.newBufferedReader(user, StandardCharsets.UTF_8)) {
                    docs.add(JsonParser.parseReader(r).getAsJsonObject());
                } catch (RuntimeException e) {
                    LOG.warn("Skipping unreadable {}: {}", user, e.toString());
                }
            }
            LOG.info("Activity mappings read from {} file(s)", docs.size());
        } catch (IOException e) {
            LOG.warn("Could not list activity mappings", e);
        }
        return ActivityData.merge(docs);
    }

    static ActivityData data() {
        return data;
    }

    private static Set<String> keys(BlockState state) {
        Set<String> k = new HashSet<>();
        k.add(BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString());
        state.typeHolder().tags().forEach(t -> k.add("#" + t.location()));
        return k;
    }

    private static Set<String> keys(ItemStack stack) {
        Set<String> k = new HashSet<>();
        if (stack.isEmpty()) return k;
        k.add(BuiltInRegistries.ITEM.getKey(stack.getItem()).toString());
        stack.typeHolder().tags().forEach(t -> k.add("#" + t.location()));
        return k;
    }

    // ---- events -----------------------------------------------------------

    private static PlayerState state(Player p) {
        return STATES.computeIfAbsent(p.getUUID(), u -> new PlayerState());
    }

    private static void mark(ServerPlayer p, String id) {
        if (id == null) return;
        PlayerState st = state(p);
        long now = p.level().getServer().getTickCount();
        st.last.put(id, now);
        st.lastActive = now;
    }

    private static void breaking(ServerPlayer p, BlockState block) {
        String id = data.resolve("breaks", keys(block));
        if (id == null) id = data.resolve("tools", keys(p.getMainHandItem()));
        mark(p, id);
    }

    // ---- per tick decision ------------------------------------------------

    private static void tick(MinecraftServer server) {
        long now = server.getTickCount();
        if (now % ActivityConfig.UPDATE_TICKS.get() != 0) return;
        Set<String> disabled = new HashSet<>(ActivityConfig.DISABLED.get());
        long afk = ActivityConfig.AFK_SECONDS.get() * 20L;
        for (ServerPlayer p : server.getPlayerList().getPlayers()) {
            PlayerState st = state(p);
            if (!st.seen) {
                st.seen = true;
                st.lastActive = now;
            } else if (p.distanceToSqr(st.x, st.y, st.z) > 0.01 || Math.abs(p.getYRot() - st.yRot) > 1F || Math.abs(p.getXRot() - st.xRot) > 1F) {
                st.lastActive = now;
            }
            st.x = p.getX(); st.y = p.getY(); st.z = p.getZ(); st.yRot = p.getYRot(); st.xRot = p.getXRot();
            String now_ = "";
            if (!p.isSpectator() && !p.isInvisible() && (optOut == null || !optOut.isHidden(p.getUUID()))) {
                st.last.values().removeIf(t -> now - t > 1200);
                now_ = ActivityLogic.decide(new ActivityLogic.Snapshot(now, p.isSleeping(), p.isPassenger(), p.fishing != null, menu(p), st.last, now - st.lastActive),
                    data.defs(), disabled, afk);
            }
            if (!now_.equals(st.sent)) {
                st.sent = now_;
                LOG.info("Activity of {} is now '{}'", p.getGameProfile().name(), now_);
                PacketDistributor.sendToPlayersTrackingEntity(p, new ActivityPayload(p.getId(), now_));
            }
        }
    }

    private static String menu(ServerPlayer p) {
        AbstractContainerMenu m = p.containerMenu;
        if (m == null || m == p.inventoryMenu) return null;
        String key = "menu";
        try {
            var type = m.getType();
            String id = BuiltInRegistries.MENU.getKey(type).toString();
            String found = data.resolve("menus", Set.of(id));
            if (found != null) key = found;
        } catch (RuntimeException ignored) {
            // some menus have no registered type; they count as a plain menu
        }
        return key;
    }

    // ---- /activity ---------------------------------------------------------

    private static void command(CommandDispatcher<CommandSourceStack> d) {
        d.register(Commands.literal("activity")
            .executes(c -> status(c.getSource()))
            .then(Commands.literal("status").executes(c -> status(c.getSource())))
            .then(Commands.literal("off").executes(c -> set(c.getSource(), true)))
            .then(Commands.literal("on").executes(c -> set(c.getSource(), false))));
    }

    private static int status(CommandSourceStack src) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer p = src.getPlayerOrException();
        boolean hidden = optOut != null && optOut.isHidden(p.getUUID());
        src.sendSuccess(() -> Component.literal(hidden ? "Others cannot see what you are doing. Use /activity on to show it." : "Others can see what you are doing. Use /activity off to hide it."), false);
        return 1;
    }

    private static int set(CommandSourceStack src, boolean hide) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer p = src.getPlayerOrException();
        if (optOut != null) optOut.set(p.getUUID(), hide);
        src.sendSuccess(() -> Component.literal(hide ? "Your activity is now hidden." : "Your activity is now shown."), false);
        return 1;
    }
}
