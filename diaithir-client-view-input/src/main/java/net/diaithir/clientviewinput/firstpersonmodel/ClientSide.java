package net.diaithir.clientviewinput.firstpersonmodel;

import com.mojang.blaze3d.platform.InputConstants;
import java.util.List;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.level.GameType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.fml.event.config.ModConfigEvent;

final class ClientSide {
    private static final KeyMapping.Category CATEGORY =
            new KeyMapping.Category(Identifier.fromNamespaceAndPath("clientviewinput", "main"));
    static final KeyMapping TOGGLE_BODY = new KeyMapping("key.clientviewinput.toggle_body", KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM, InputConstants.UNKNOWN.getValue(), CATEGORY);
    static final KeyMapping TOGGLE_HELMET = new KeyMapping("key.clientviewinput.toggle_helmet", KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM, InputConstants.UNKNOWN.getValue(), CATEGORY);

    private static final List<String> CONFLICTS = List.of("epicfight", "playeranimator", "bettercombat");

    private static volatile boolean enabled = true, showHelmet, showHead, vehicles = true, sleeping, gliding = true, swimming = true, spectator = true;
    private static volatile double offX, offZ, lookDown = 0.25;
    private static volatile List<String> blacklist = List.of();
    private static volatile boolean pitchOffsetAllowed = true;

    private ClientSide() {}

    static void init(IEventBus modBus, ModContainer container) {
        container.registerConfig(ModConfig.Type.CLIENT, FpmConfig.SPEC, "clientviewinput-firstpersonmodel.toml");
        modBus.addListener(ClientSide::onRegisterKeys);
        modBus.addListener(ClientSide::onConfigLoad);
        modBus.addListener(ClientSide::onConfigReload);
        NeoForge.EVENT_BUS.register(GameEvents.class);
    }

    private static void onRegisterKeys(RegisterKeyMappingsEvent e) {
        e.register(TOGGLE_BODY);
        e.register(TOGGLE_HELMET);
    }

    private static void onConfigLoad(ModConfigEvent.Loading e) {
        if (e.getConfig().getSpec() == FpmConfig.SPEC) cache();
    }

    private static void onConfigReload(ModConfigEvent.Reloading e) {
        if (e.getConfig().getSpec() == FpmConfig.SPEC) cache();
    }

    private static void cache() {
        enabled = FpmConfig.ENABLED.get();
        showHelmet = FpmConfig.SHOW_HELMET.get();
        showHead = FpmConfig.SHOW_HEAD.get();
        offX = FpmConfig.OFFSET_X.get();
        offZ = FpmConfig.OFFSET_Z.get();
        lookDown = FpmConfig.LOOK_DOWN_OFFSET.get();
        vehicles = FpmConfig.IN_VEHICLES.get();
        sleeping = FpmConfig.WHILE_SLEEPING.get();
        gliding = FpmConfig.WHILE_GLIDING.get();
        swimming = FpmConfig.WHILE_SWIMMING.get();
        spectator = FpmConfig.HIDE_IN_SPECTATOR.get();
        blacklist = List.copyOf(FpmConfig.BLACKLISTED_SCREENS.get());
        pitchOffsetAllowed = CONFLICTS.stream().noneMatch(id -> ModList.get().isLoaded(id));
    }

    private static boolean shouldRender(Minecraft mc, LocalPlayer p) {
        if (!enabled || mc.options.getCameraType() == null || !mc.options.getCameraType().isFirstPerson()) return false;
        if (mc.getCameraEntity() != p || !p.isAlive()) return false;
        if (spectator && (p.isSpectator() || (mc.gameMode != null && mc.gameMode.getPlayerMode() == GameType.SPECTATOR))) return false;
        if (p.isPassenger() && !vehicles) return false;
        if (p.isSleeping() && !sleeping) return false;
        if (p.isFallFlying() && !gliding) return false;
        if (p.isSwimming() && !swimming) return false;
        if (mc.screen != null && blacklist.contains(mc.screen.getClass().getName())) return false;
        return true;
    }

    private static void message(Minecraft mc, String text) {
        mc.gui.setOverlayMessage(Component.literal(text), false);
    }

    static final class GameEvents {
        private GameEvents() {}

        @SubscribeEvent
        static void onTick(ClientTickEvent.Post e) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null) return;
            while (TOGGLE_BODY.consumeClick()) {
                boolean v = !FpmConfig.ENABLED.get();
                FpmConfig.ENABLED.set(v);
                enabled = v;
                FpmConfig.SPEC.save();
                message(mc, "First Person Body: " + (v ? "ON" : "OFF"));
            }
            while (TOGGLE_HELMET.consumeClick()) {
                boolean v = !FpmConfig.SHOW_HELMET.get();
                FpmConfig.SHOW_HELMET.set(v);
                showHelmet = v;
                FpmConfig.SPEC.save();
                message(mc, "First Person Helmet: " + (v ? "ON" : "OFF"));
            }
        }
    }
}
