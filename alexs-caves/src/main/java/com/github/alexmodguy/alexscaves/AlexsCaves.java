package com.github.alexmodguy.alexscaves;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;
import com.github.alexmodguy.alexscaves.server.CommonProxy;
import com.github.alexmodguy.alexscaves.server.config.ACServerConfig;
import com.github.alexmodguy.alexscaves.server.message.*;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.slf4j.Logger;

@Mod(AlexsCaves.MODID)
public class AlexsCaves {
    public static final String MODID = "alexscaves";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static CommonProxy PROXY = new CommonProxy();
    public static final ACServerConfig COMMON_CONFIG;
    private static final ModConfigSpec COMMON_CONFIG_SPEC;
    public static final java.util.List<String> MOD_GENERATION_CONFLICTS = new java.util.ArrayList<>();

    static {
        final Pair<ACServerConfig, ModConfigSpec> serverPair = new ModConfigSpec.Builder().configure(ACServerConfig::new);
        COMMON_CONFIG = serverPair.getLeft();
        COMMON_CONFIG_SPEC = serverPair.getRight();
    }

    public AlexsCaves(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.COMMON, COMMON_CONFIG_SPEC, "alexscaves-general.toml");
        modEventBus.addListener(this::registerPayloads);
        // registries are attached stage by stage, see PORT_PLAN.md
    }

    private void registerPayloads(RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(MODID).versioned(VERSION).optional();
        // Server-to-client messages
        registrar.playToClient(WorldEventMessage.TYPE, WorldEventMessage.CODEC, WorldEventMessage::handle);
        registrar.playToClient(UpdateCaveBiomeMapTagMessage.TYPE, UpdateCaveBiomeMapTagMessage.CODEC,
                UpdateCaveBiomeMapTagMessage::handle);
        registrar.playToClient(UpdateBossEruptionStatus.TYPE, UpdateBossEruptionStatus.CODEC,
                UpdateBossEruptionStatus::handle);
        registrar.playToClient(UpdateBossBarMessage.TYPE, UpdateBossBarMessage.CODEC, UpdateBossBarMessage::handle);
        registrar.playToClient(UpdateEffectVisualityEntityMessage.TYPE, UpdateEffectVisualityEntityMessage.CODEC,
                UpdateEffectVisualityEntityMessage::handle);
        registrar.playBidirectional(UpdateItemTagMessage.TYPE, UpdateItemTagMessage.CODEC, UpdateItemTagMessage::handle);
        registrar.playToClient(BeholderSyncMessage.TYPE, BeholderSyncMessage.CODEC, BeholderSyncMessage::handle);
        registrar.playToClient(SundropRainbowMessage.TYPE, SundropRainbowMessage.CODEC, SundropRainbowMessage::handle);
        registrar.playToClient(SpelunkeryTableCompleteTutorialMessage.TYPE,
                SpelunkeryTableCompleteTutorialMessage.CODEC, SpelunkeryTableCompleteTutorialMessage::handle);
        registrar.playToClient(UpdateMagneticDataMessage.TYPE, UpdateMagneticDataMessage.CODEC,
                UpdateMagneticDataMessage::handle);
        // Client-to-server messages
        registrar.playToServer(MultipartEntityMessage.TYPE, MultipartEntityMessage.CODEC,
                MultipartEntityMessage::handle);
        registrar.playToServer(SpelunkeryTableChangeMessage.TYPE, SpelunkeryTableChangeMessage.CODEC,
                SpelunkeryTableChangeMessage::handle);
        registrar.playToServer(PlayerJumpFromMagnetMessage.TYPE, PlayerJumpFromMagnetMessage.CODEC,
                PlayerJumpFromMagnetMessage::handle);
        registrar.playToServer(MountedEntityKeyMessage.TYPE, MountedEntityKeyMessage.CODEC,
                MountedEntityKeyMessage::handle);
        registrar.playToServer(PossessionKeyMessage.TYPE, PossessionKeyMessage.CODEC, PossessionKeyMessage::handle);
        registrar.playToServer(BeholderRotateMessage.TYPE, BeholderRotateMessage.CODEC, BeholderRotateMessage::handle);
        registrar.playToServer(ArmorKeyMessage.TYPE, ArmorKeyMessage.CODEC, ArmorKeyMessage::handle);
    }

    public static <MSG extends CustomPacketPayload> void sendMSGToServer(MSG message) {
        PacketDistributor.sendToServer(message);
    }

    public static <MSG extends CustomPacketPayload> void sendMSGToAll(MSG message) {
        for (ServerPlayer player : ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayers()) {
            sendNonLocal(message, player);
        }
    }

    public static <MSG extends CustomPacketPayload> void sendNonLocal(MSG msg, ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, msg);
    }

}
