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
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.world.chunk.RegisterTicketControllersEvent;
import com.github.alexmodguy.alexscaves.client.particle.ACParticleRegistry;
import com.github.alexmodguy.alexscaves.server.block.ACBlockRegistry;
import com.github.alexmodguy.alexscaves.server.block.blockentity.ACBlockEntityRegistry;
import com.github.alexmodguy.alexscaves.server.block.fluid.ACFluidRegistry;
import com.github.alexmodguy.alexscaves.server.block.poi.ACPOIRegistry;
import com.github.alexmodguy.alexscaves.server.entity.ACEntityDataRegistry;
import com.github.alexmodguy.alexscaves.server.entity.ACEntityRegistry;
import com.github.alexmodguy.alexscaves.server.entity.util.ACAttachmentRegistry;
import com.github.alexmodguy.alexscaves.server.event.CommonEvents;
import com.github.alexmodguy.alexscaves.server.inventory.ACMenuRegistry;
import com.github.alexmodguy.alexscaves.server.item.ACItemRegistry;
import com.github.alexmodguy.alexscaves.server.level.biome.ACBiomeRegistry;
import com.github.alexmodguy.alexscaves.server.level.carver.ACCarverRegistry;
import com.github.alexmodguy.alexscaves.server.level.feature.ACFeatureRegistry;
import com.github.alexmodguy.alexscaves.server.level.structure.ACStructureRegistry;
import com.github.alexmodguy.alexscaves.server.level.structure.piece.ACStructurePieceRegistry;
import com.github.alexmodguy.alexscaves.server.level.structure.processor.ACStructureProcessorRegistry;
import com.github.alexmodguy.alexscaves.server.level.surface.ACSurfaceRuleConditionRegistry;
import com.github.alexmodguy.alexscaves.server.level.surface.ACSurfaceRules;
import com.github.alexmodguy.alexscaves.server.misc.*;
import com.github.alexmodguy.alexscaves.server.potion.ACEffectRegistry;
import org.slf4j.Logger;

@Mod(AlexsCaves.MODID)
public class AlexsCaves {
    public static final String VERSION = "1";
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
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::loadComplete);
        modEventBus.addListener(this::registerTicketControllers);
        modEventBus.addListener(ACBlockEntityRegistry::expandVanillaDefinitions);
        NeoForge.EVENT_BUS.register(new CommonEvents());
        ACBlockRegistry.DEF_REG.register(modEventBus);
        ACBlockEntityRegistry.DEF_REG.register(modEventBus);
        ACItemRegistry.DEF_REG.register(modEventBus);
        ACParticleRegistry.DEF_REG.register(modEventBus);
        ACDataSerializers.DEF_REG.register(modEventBus);
        ACEntityRegistry.DEF_REG.register(modEventBus);
        ACEntityDataRegistry.DEF_REG.register(modEventBus);
        ACAttachmentRegistry.DEF_REG.register(modEventBus);
        ACPOIRegistry.DEF_REG.register(modEventBus);
        ACFeatureRegistry.DEF_REG.register(modEventBus);
        ACSurfaceRuleConditionRegistry.DEF_REG.register(modEventBus);
        ACCarverRegistry.DEF_REG.register(modEventBus);
        ACSoundRegistry.DEF_REG.register(modEventBus);
        ACStructureRegistry.DEF_REG.register(modEventBus);
        ACStructurePieceRegistry.DEF_REG.register(modEventBus);
        ACStructureProcessorRegistry.DEF_REG.register(modEventBus);
        ACEffectRegistry.DEF_REG.register(modEventBus);
        ACEffectRegistry.POTION_DEF_REG.register(modEventBus);
        ACMenuRegistry.DEF_REG.register(modEventBus);
        ACAdvancementTriggerRegistry.DEF_REG.register(modEventBus);
        ACFluidRegistry.FLUID_TYPE_DEF_REG.register(modEventBus);
        ACFluidRegistry.FLUID_DEF_REG.register(modEventBus);
        ACLootTableRegistry.GLOBAL_LOOT_MODIFIER_DEF_REG.register(modEventBus);
        ACLootTableRegistry.LOOT_FUNCTION_DEF_REG.register(modEventBus);
        ACCreativeTabRegistry.DEF_REG.register(modEventBus);
        ACDataComponentRegistry.init(modEventBus);
        ACPotPatternRegistry.init();
        PROXY.commonInit(modEventBus);
        ACBiomeRegistry.init();
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            ACSurfaceRules.setup();
            ACEffectRegistry.setup();
            ACBlockRegistry.setup();
            ACItemRegistry.setup();
        });
    }

    private void registerTicketControllers(RegisterTicketControllersEvent event) {
        event.register(com.github.alexmodguy.alexscaves.server.entity.item.NuclearExplosionEntity.TICKET_CONTROLLER);
        event.register(com.github.alexmodguy.alexscaves.server.entity.item.BeholderEyeEntity.TICKET_CONTROLLER);
        event.register(com.github.alexmodguy.alexscaves.server.item.OccultGemItem.TICKET_CONTROLLER);
        event.register(com.github.alexmodguy.alexscaves.server.item.RemoteDetonatorItem.TICKET_CONTROLLER);
    }

    private void loadComplete(FMLLoadCompleteEvent event) {
        event.enqueueWork(ACFluidRegistry::postInit);
        event.enqueueWork(ACLoadedMods::afterAllModsLoaded);
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
        registrar.playBidirectional(UpdateItemTagMessage.TYPE, UpdateItemTagMessage.CODEC, UpdateItemTagMessage::handle, UpdateItemTagMessage::handle);
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
        net.neoforged.neoforge.client.network.ClientPacketDistributor.sendToServer(message);
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
