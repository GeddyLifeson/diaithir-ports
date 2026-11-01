package com.github.alexmodguy.alexscaves.client;

import com.github.alexmodguy.alexscaves.AlexsCaves;
//CE_IMPORT
import com.github.alexmodguy.alexscaves.client.particle.*;
import com.github.alexmodguy.alexscaves.client.render.entity.*;
import com.github.alexmodguy.alexscaves.client.sound.*;
import com.github.alexmodguy.alexscaves.server.CommonProxy;
import com.github.alexmodguy.alexscaves.server.block.ACBlockRegistry;
import com.github.alexmodguy.alexscaves.server.block.AcidBlock;
import com.github.alexmodguy.alexscaves.server.block.ActivatedByAltar;
import com.github.alexmodguy.alexscaves.server.block.FrostedChocolateBlock;
import com.github.alexmodguy.alexscaves.server.block.blockentity.*;
import com.github.alexmodguy.alexscaves.server.block.fluid.ACFluidRegistry;
import com.github.alexmodguy.alexscaves.server.entity.ACEntityRegistry;
import com.github.alexmodguy.alexscaves.server.entity.item.BeholderEyeEntity;
import com.github.alexmodguy.alexscaves.server.entity.item.QuarrySmasherEntity;
import com.github.alexmodguy.alexscaves.server.entity.item.SubmarineEntity;
import com.github.alexmodguy.alexscaves.server.entity.living.*;
import com.github.alexmodguy.alexscaves.server.inventory.ACMenuRegistry;
import com.github.alexmodguy.alexscaves.server.item.*;
import com.github.alexmodguy.alexscaves.server.item.tooltip.SackOfSatingTooltip;
import com.github.alexmodguy.alexscaves.server.misc.ACSoundRegistry;
import com.github.alexthe666.citadel.client.shader.PostEffectRegistry;
import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.renderer.entity.FallingBlockRenderer;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.ModContainer;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.*;

public class ClientProxy extends CommonProxy {

    private static final List<String> FULLBRIGHTS = ImmutableList.of("alexscaves:ambersol#",
            "alexscaves:radrock_uranium_ore#", "alexscaves:acidic_radrock#", "alexscaves:uranium_rod#axis=x",
            "alexscaves:uranium_rod#axis=y", "alexscaves:uranium_rod#axis=z", "alexscaves:block_of_uranium#",
            "alexscaves:abyssal_altar#active=true", "alexscaves:abyssmarine_", "alexscaves:peering_coprolith#",
            "alexscaves:forsaken_idol#", "alexscaves:magnetic_light#", "alexscaves:tremorzilla_egg#");
    public static final Identifier BOMB_FLASH = Identifier.fromNamespaceAndPath(AlexsCaves.MODID,
            "textures/misc/bomb_flash.png");
    public static final Identifier WATCHER_EFFECT = Identifier.fromNamespaceAndPath(AlexsCaves.MODID,
            "textures/misc/watcher_effect.png");
    public static final Identifier IRRADIATED_SHADER = Identifier.fromNamespaceAndPath(AlexsCaves.MODID,
            "shaders/post/irradiated.json");
    public static final Identifier HOLOGRAM_SHADER = Identifier.fromNamespaceAndPath(AlexsCaves.MODID,
            "shaders/post/hologram.json");
    public static final Identifier PURPLE_WITCH_SHADER = Identifier.fromNamespaceAndPath(AlexsCaves.MODID,
            "shaders/post/purple_witch.json");
    public static final RandomSource random = RandomSource.create();
    public static int lastTremorTick = -1;
    public static float[] randomTremorOffsets = new float[3];
    public static List<UUID> blockedEntityRenders = new ArrayList<>();
    public static Map<ClientLevel, List<BlockPos>> blockedParticleLocations = new HashMap<>();
    public static Map<LivingEntity, Vec3[]> darknessTrailPosMap = new HashMap<>();
    public static Map<LivingEntity, Integer> darknessTrailPointerMap = new HashMap<>();
    public static int muteNonNukeSoundsFor = 0;
    public static int renderNukeFlashFor = 0;
    public static boolean primordialBossActive = false;
    public static float prevPrimordialBossActiveAmount = 0;
    public static float primordialBossActiveAmount = 0;
    public static ClientLevel lastBossLevel;
    public static float prevNukeFlashAmount = 0;
    public static float nukeFlashAmount = 0;
    public static float prevPossessionStrengthAmount = 0;
    public static float possessionStrengthAmount = 0;
    public static int renderNukeSkyDarkFor = 0;
    public static float masterVolumeNukeModifier = 0.0F;
    // Client-side tracking for bubbled effect visuals (entity ID -> remaining ticks)
    private static final it.unimi.dsi.fastutil.ints.Int2IntMap BUBBLED_EFFECT_TICKS = new it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap();
    public static final Int2ObjectMap<AbstractTickableSoundInstance> ENTITY_SOUND_INSTANCE_MAP = new Int2ObjectOpenHashMap<>();
    public static final Map<BlockEntity, AbstractTickableSoundInstance> BLOCK_ENTITY_SOUND_INSTANCE_MAP = new HashMap<>();
    public static boolean spelunkeryTutorialComplete;
    public static boolean hasACSplashText = false;
    public static CameraType lastPOV = CameraType.FIRST_PERSON;
    public static int shaderLoadAttemptCooldown = 0;
    public static Vec3 lastBiomeLightColor = Vec3.ZERO;
    public static float lastBiomeAmbientLightAmount = 0;
    public static Vec3 lastBiomeLightColorPrev = Vec3.ZERO;
    public static float lastBiomeAmbientLightAmountPrev = 0;
    public static Map<UUID, Integer> bossBarRenderTypes = new HashMap<>();
    private static Entity lastCameraEntity;
    public static float acSkyOverrideAmount;
    public static Vec3 acSkyOverrideColor = Vec3.ZERO;
    public static boolean disabledBiomeAmbientLightByOtherMod = false;

    /**
    /**
     * Ticks down all bubbled effect timers. Called from ClientEvents.
     */
    public static void tickBubbledEffects() {
        if (BUBBLED_EFFECT_TICKS.isEmpty()) return;
        var iterator = BUBBLED_EFFECT_TICKS.int2IntEntrySet().iterator();
        while (iterator.hasNext()) {
            var entry = iterator.next();
            int newValue = entry.getIntValue() - 1;
            if (newValue <= 0) {
                iterator.remove();
            } else {
                entry.setValue(newValue);
            }
        }
    }

    @Override
    public void commonInit(IEventBus modEventBus) {
    }

    @Override
    public void clientInit(IEventBus modEventBus) {
//CE_REG
        blockedParticleLocations.clear();
        hasACSplashText = random.nextInt(300) == 0;
    }

    public Player getClientSidePlayer() {
        return Minecraft.getInstance().player;
    }

    public void blockRenderingEntity(UUID id) {
        blockedEntityRenders.add(id);
    }

    public void releaseRenderingEntity(UUID id) {
        blockedEntityRenders.remove(id);
    }

    public void setVisualFlag(int flag) {
    }

    public float getNukeFlashAmount(float partialTicks) {
        return prevNukeFlashAmount + (nukeFlashAmount - prevNukeFlashAmount) * partialTicks;
    }

    public float getPrimordialBossActiveAmount(float partialTicks) {
        return prevPrimordialBossActiveAmount
                + (primordialBossActiveAmount - prevPrimordialBossActiveAmount) * partialTicks;
    }

    public float getPossessionStrengthAmount(float partialTicks) {
        return prevPossessionStrengthAmount + (possessionStrengthAmount - prevPossessionStrengthAmount) * partialTicks;
    }

    public boolean checkIfParticleAt(SimpleParticleType simpleParticleType, BlockPos at) {
        if (!blockedParticleLocations.containsKey(Minecraft.getInstance().level)) {
            blockedParticleLocations.clear();
            blockedParticleLocations.put(Minecraft.getInstance().level, new ArrayList<>());
        }
        List blocked = blockedParticleLocations.get(Minecraft.getInstance().level);
        if (blocked.contains(at)) {
            return false;
        } else {
            blocked.add(new BlockPos(at));
            return true;
        }
    }

    public void removeParticleAt(BlockPos at) {
        if (!blockedParticleLocations.containsKey(Minecraft.getInstance().level)) {
            blockedParticleLocations.clear();
            blockedParticleLocations.put(Minecraft.getInstance().level, new ArrayList<>());
        }
        blockedParticleLocations.get(Minecraft.getInstance().level).remove(at);
    }

    public boolean isKeyDown(int keyType) {
        if (keyType == -1) {
            return Minecraft.getInstance().options.keyLeft.isDown() || Minecraft.getInstance().options.keyRight.isDown()
                    || Minecraft.getInstance().options.keyUp.isDown()
                    || Minecraft.getInstance().options.keyDown.isDown()
                    || Minecraft.getInstance().options.keyJump.isDown();
        }
        if (keyType == 0) {
            return Minecraft.getInstance().options.keyJump.isDown();
        }
        if (keyType == 1) {
            return Minecraft.getInstance().options.keySprint.isDown();
        }
        if (keyType == 2) {
            return false;
        }
        if (keyType == 3) {
            return Minecraft.getInstance().options.keyAttack.isDown();
        }
        if (keyType == 4) {
            return Minecraft.getInstance().options.keyShift.isDown();
        }
        return false;
    }



    public float getPartialTicks() {
        return Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false);
    }

    public void setSpelunkeryTutorialComplete(boolean completedTutorial) {
        spelunkeryTutorialComplete = completedTutorial;
    }

    public boolean isSpelunkeryTutorialComplete() {
        return spelunkeryTutorialComplete;
    }

    public void setRenderViewEntity(Player player, Entity entity) {
        if (player == Minecraft.getInstance().player
                && Minecraft.getInstance().getCameraEntity() == Minecraft.getInstance().player) {
            lastPOV = Minecraft.getInstance().options.getCameraType();
            Minecraft.getInstance().setCameraEntity(entity);
            Minecraft.getInstance().options.setCameraType(CameraType.FIRST_PERSON);
        }
        if (lastCameraEntity != Minecraft.getInstance().getCameraEntity()) {
            Minecraft.getInstance().levelRenderer.allChanged();
            lastCameraEntity = Minecraft.getInstance().getCameraEntity();
        }
    }

    public void resetRenderViewEntity(Player player) {
        if (player == Minecraft.getInstance().player) {
            Minecraft.getInstance().level = (ClientLevel) Minecraft.getInstance().player.level();
            Minecraft.getInstance().setCameraEntity(Minecraft.getInstance().player);
            Minecraft.getInstance().options.setCameraType(lastPOV);
        }
        if (lastCameraEntity != Minecraft.getInstance().getCameraEntity()) {
            Minecraft.getInstance().levelRenderer.allChanged();
            lastCameraEntity = Minecraft.getInstance().getCameraEntity();
        }
    }

    @Override
    public boolean hasBubbledEffectVisual(int entityId) {
        return BUBBLED_EFFECT_TICKS.getOrDefault(entityId, 0) > 0;
    }

    @Override
    public void setBubbledEffectTicks(int entityId, int ticks) {
        if (ticks <= 0) {
            BUBBLED_EFFECT_TICKS.remove(entityId);
        } else {
            BUBBLED_EFFECT_TICKS.put(entityId, ticks);
        }
    }

    @Override
    public void handleBeholderSync(int beholderId, boolean active, double x, double y, double z, float yRot, float xRot, UUID usingPlayerUUID) {
        Player playerSided = getClientSidePlayer();
        if (playerSided != null && playerSided.level() instanceof ClientLevel clientLevel) {
            Entity watcher = clientLevel.getEntity(beholderId);
            // If entity doesn't exist on client and we have spawn data, create it
            // This is necessary when viewing a Beholder from far away (unloaded chunks)
            if (watcher == null && active && usingPlayerUUID != null) {
                BeholderEyeEntity beholderEye = ACEntityRegistry.BEHOLDER_EYE.get().create(clientLevel, net.minecraft.world.entity.EntitySpawnReason.LOAD);
                if (beholderEye != null) {
                    beholderEye.setId(beholderId);
                    beholderEye.setPos(x, y, z);
                    beholderEye.setEyeYRot(yRot);
                    beholderEye.setEyeXRot(xRot);
                    beholderEye.setUsingPlayerUUID(usingPlayerUUID);
                    beholderEye.hasTakenFullControlOfCamera = true;
                    clientLevel.addEntity(beholderEye);
                    watcher = beholderEye;
                }
            }
            if (watcher instanceof BeholderEyeEntity beholderEye) {
                Entity beholderEyePlayer = beholderEye.getUsingPlayer();
                beholderEye.hasTakenFullControlOfCamera = true;
                if (beholderEyePlayer != null && beholderEyePlayer instanceof Player && beholderEyePlayer.equals(playerSided)) {
                    if (active) {
                        setRenderViewEntity(playerSided, beholderEye);
                    } else {
                        resetRenderViewEntity(playerSided);
                    }
                }
            }
        }
    }

    @Override
    public void playWorldSound(@Nullable Object soundEmitter, byte type) {
        if (soundEmitter instanceof Entity entity && !entity.level().isClientSide()) {
            return;
        }
        switch (type) {
            case 0:
                if (soundEmitter instanceof NuclearSirenBlockEntity nuclearSiren) {
                    NuclearSirenSound sound;
                    AbstractTickableSoundInstance old = BLOCK_ENTITY_SOUND_INSTANCE_MAP.get(nuclearSiren);
                    if (old == null || !(old instanceof NuclearSirenSound nuclearSirenSound
                            && nuclearSirenSound.isSameBlockEntity(nuclearSiren)) || old.isStopped()) {
                        sound = new NuclearSirenSound(nuclearSiren);
                        BLOCK_ENTITY_SOUND_INSTANCE_MAP.put(nuclearSiren, sound);
                    } else {
                        sound = (NuclearSirenSound) old;
                    }
                    if (!isSoundPlaying(sound) && sound.canPlaySound()) {
                        Minecraft.getInstance().getSoundManager().queueTickingSound(sound);
                    }
                }
                break;
            case 1:
                if (soundEmitter instanceof NucleeperEntity nucleeper) {
                    NucleeperSound sound;
                    AbstractTickableSoundInstance old = ENTITY_SOUND_INSTANCE_MAP.get(nucleeper.getId());
                    if (old == null || !(old instanceof NucleeperSound nucleeperSound
                            && nucleeperSound.isSameEntity(nucleeper))) {
                        sound = new NucleeperSound(nucleeper);
                        ENTITY_SOUND_INSTANCE_MAP.put(nucleeper.getId(), sound);
                    } else {
                        sound = (NucleeperSound) old;
                    }
                    if (!isSoundPlaying(sound) && sound.canPlaySound()) {
                        Minecraft.getInstance().getSoundManager().queueTickingSound(sound);
                    }
                }
                break;
            case 2:
                if (soundEmitter instanceof NotorEntity notor) {
                    NotorHologramSound sound;
                    AbstractTickableSoundInstance old = ENTITY_SOUND_INSTANCE_MAP.get(notor.getId());
                    if (old == null || !(old instanceof NotorHologramSound hologramSound
                            && hologramSound.isSameEntity(notor))) {
                        sound = new NotorHologramSound(notor);
                        ENTITY_SOUND_INSTANCE_MAP.put(notor.getId(), sound);
                    } else {
                        sound = (NotorHologramSound) old;
                    }
                    if (!isSoundPlaying(sound) && sound.canPlaySound()) {
                        Minecraft.getInstance().getSoundManager().queueTickingSound(sound);
                    }
                }
                break;
            case 3:
                if (soundEmitter instanceof HologramProjectorBlockEntity hologramProjector) {
                    HologramProjectorSound sound;
                    AbstractTickableSoundInstance old = BLOCK_ENTITY_SOUND_INSTANCE_MAP.get(hologramProjector);
                    if (old == null || !(old instanceof HologramProjectorSound hologramSound
                            && hologramSound.isSameBlockEntity(hologramProjector)) || old.isStopped()) {
                        sound = new HologramProjectorSound(hologramProjector);
                        BLOCK_ENTITY_SOUND_INSTANCE_MAP.put(hologramProjector, sound);
                    } else {
                        sound = (HologramProjectorSound) old;
                    }
                    if (!isSoundPlaying(sound) && sound.canPlaySound()) {
                        Minecraft.getInstance().getSoundManager().queueTickingSound(sound);
                    }
                }
                break;
            case 4:
                if (soundEmitter instanceof MagnetBlockEntity magnet) {
                    MagnetSound sound;
                    AbstractTickableSoundInstance old = BLOCK_ENTITY_SOUND_INSTANCE_MAP.get(magnet);
                    if (old == null
                            || !(old instanceof MagnetSound magnetSound && magnetSound.isSameBlockEntity(magnet))
                            || old.isStopped()) {
                        sound = new MagnetSound(magnet);
                        BLOCK_ENTITY_SOUND_INSTANCE_MAP.put(magnet, sound);
                    } else {
                        sound = (MagnetSound) old;
                    }
                    if (!isSoundPlaying(sound) && sound.canPlaySound()) {
                        Minecraft.getInstance().getSoundManager().queueTickingSound(sound);
                    }
                }
                break;
            case 5:
                if (soundEmitter instanceof UnderzealotEntity underzealot) {
                    UnderzealotSound sound;
                    AbstractTickableSoundInstance old = ENTITY_SOUND_INSTANCE_MAP.get(underzealot.getId());
                    if (old == null || !(old instanceof UnderzealotSound underzealotSound
                            && underzealotSound.isSameEntity(underzealot))) {
                        sound = new UnderzealotSound(underzealot);
                        ENTITY_SOUND_INSTANCE_MAP.put(underzealot.getId(), sound);
                    } else {
                        sound = (UnderzealotSound) old;
                    }
                    if (!isSoundPlaying(sound) && sound.canPlaySound()) {
                        Minecraft.getInstance().getSoundManager().queueTickingSound(sound);
                    }
                }
                break;
            case 6:
                if (soundEmitter instanceof CorrodentEntity corrodent) {
                    CorrodentSound sound;
                    AbstractTickableSoundInstance old = ENTITY_SOUND_INSTANCE_MAP.get(corrodent.getId());
                    if (old == null || !(old instanceof CorrodentSound corrodentSound
                            && corrodentSound.isSameEntity(corrodent))) {
                        sound = new CorrodentSound(corrodent);
                        ENTITY_SOUND_INSTANCE_MAP.put(corrodent.getId(), sound);
                    } else {
                        sound = (CorrodentSound) old;
                    }
                    if (!isSoundPlaying(sound) && sound.canPlaySound()) {
                        Minecraft.getInstance().getSoundManager().queueTickingSound(sound);
                    }
                }
                break;
            case 7:
                if (soundEmitter instanceof com.github.alexmodguy.alexscaves.server.block.blockentity.NuclearFurnaceBlockEntity nuclearFurnace) {
                    com.github.alexmodguy.alexscaves.client.sound.NuclearFurnaceSound sound;
                    AbstractTickableSoundInstance old = BLOCK_ENTITY_SOUND_INSTANCE_MAP.get(nuclearFurnace);
                    if (old == null || !(old instanceof com.github.alexmodguy.alexscaves.client.sound.NuclearFurnaceSound furnaceSound
                            && furnaceSound.isSameBlockEntity(nuclearFurnace)) || old.isStopped()) {
                        sound = new com.github.alexmodguy.alexscaves.client.sound.NuclearFurnaceSound(nuclearFurnace);
                        BLOCK_ENTITY_SOUND_INSTANCE_MAP.put(nuclearFurnace, sound);
                    } else {
                        sound = (com.github.alexmodguy.alexscaves.client.sound.NuclearFurnaceSound) old;
                    }
                    if (!isSoundPlaying(sound) && sound.canPlaySound()) {
                        Minecraft.getInstance().getSoundManager().queueTickingSound(sound);
                    }
                }
                break;
            case 8:
                if (soundEmitter instanceof LivingEntity livingEntity) {
                    RaygunSound sound;
                    AbstractTickableSoundInstance old = ENTITY_SOUND_INSTANCE_MAP.get(livingEntity.getId());
                    if (old == null
                            || !(old instanceof RaygunSound raygunSound && raygunSound.isSameEntity(livingEntity))) {
                        sound = new RaygunSound(livingEntity);
                        ENTITY_SOUND_INSTANCE_MAP.put(livingEntity.getId(), sound);
                    } else {
                        sound = (RaygunSound) old;
                    }
                    if (!isSoundPlaying(sound) && sound.canPlaySound()) {
                        Minecraft.getInstance().getSoundManager().queueTickingSound(sound);
                    }
                }
                break;
            case 9:
                if (soundEmitter instanceof LivingEntity livingEntity) {
                    ResistorShieldSound sound;
                    AbstractTickableSoundInstance old = ENTITY_SOUND_INSTANCE_MAP.get(livingEntity.getId());
                    if (old == null || !(old instanceof ResistorShieldSound resistorShieldSound
                            && resistorShieldSound.isSameEntity(livingEntity) && !resistorShieldSound.isAzure())) {
                        sound = new ResistorShieldSound(livingEntity, false);
                        ENTITY_SOUND_INSTANCE_MAP.put(livingEntity.getId(), sound);
                    } else {
                        sound = (ResistorShieldSound) old;
                    }
                    if (!isSoundPlaying(sound) && sound.canPlaySound()) {
                        Minecraft.getInstance().getSoundManager().queueTickingSound(sound);
                    }
                }
                break;
            case 10:
                if (soundEmitter instanceof LivingEntity livingEntity) {
                    ResistorShieldSound sound;
                    AbstractTickableSoundInstance old = ENTITY_SOUND_INSTANCE_MAP.get(livingEntity.getId());
                    if (old == null || !(old instanceof ResistorShieldSound resistorShieldSound
                            && resistorShieldSound.isSameEntity(livingEntity) && resistorShieldSound.isAzure())) {
                        sound = new ResistorShieldSound(livingEntity, true);
                        ENTITY_SOUND_INSTANCE_MAP.put(livingEntity.getId(), sound);
                    } else {
                        sound = (ResistorShieldSound) old;
                    }
                    if (!isSoundPlaying(sound) && sound.canPlaySound()) {
                        Minecraft.getInstance().getSoundManager().queueTickingSound(sound);
                    }
                }
                break;
            case 11:
                if (soundEmitter instanceof LivingEntity livingEntity) {
                    GalenaGauntletSound sound;
                    AbstractTickableSoundInstance old = ENTITY_SOUND_INSTANCE_MAP.get(livingEntity.getId());
                    if (old == null || !(old instanceof GalenaGauntletSound gauntletSound
                            && gauntletSound.isSameEntity(livingEntity))) {
                        sound = new GalenaGauntletSound(livingEntity);
                        ENTITY_SOUND_INSTANCE_MAP.put(livingEntity.getId(), sound);
                    } else {
                        sound = (GalenaGauntletSound) old;
                    }
                    if (!isSoundPlaying(sound) && sound.canPlaySound()) {
                        Minecraft.getInstance().getSoundManager().queueTickingSound(sound);
                    }
                }
                break;
            case 12:
                if (soundEmitter instanceof BoundroidEntity boundroid) {
                    BoundroidSound sound;
                    AbstractTickableSoundInstance old = ENTITY_SOUND_INSTANCE_MAP.get(boundroid.getId());
                    if (old == null || !(old instanceof BoundroidSound boundroidSound
                            && boundroidSound.isSameEntity(boundroid))) {
                        sound = new BoundroidSound(boundroid);
                        ENTITY_SOUND_INSTANCE_MAP.put(boundroid.getId(), sound);
                    } else {
                        sound = (BoundroidSound) old;
                    }
                    if (!isSoundPlaying(sound) && sound.canPlaySound()) {
                        Minecraft.getInstance().getSoundManager().queueTickingSound(sound);
                    }
                }
                break;
            case 13:
                if (soundEmitter instanceof FerrouslimeEntity ferrouslime) {
                    FerrouslimeSound sound;
                    AbstractTickableSoundInstance old = ENTITY_SOUND_INSTANCE_MAP.get(ferrouslime.getId());
                    if (old == null || !(old instanceof FerrouslimeSound ferrouslimeSound
                            && ferrouslimeSound.isSameEntity(ferrouslime))) {
                        sound = new FerrouslimeSound(ferrouslime);
                        ENTITY_SOUND_INSTANCE_MAP.put(ferrouslime.getId(), sound);
                    } else {
                        sound = (FerrouslimeSound) old;
                    }
                    if (!isSoundPlaying(sound) && sound.canPlaySound()) {
                        Minecraft.getInstance().getSoundManager().queueTickingSound(sound);
                    }
                }
                break;
            case 14:
                if (soundEmitter instanceof QuarrySmasherEntity quarrySmasher) {
                    QuarrySmasherSound sound;
                    AbstractTickableSoundInstance old = ENTITY_SOUND_INSTANCE_MAP.get(quarrySmasher.getId());
                    if (old == null || !(old instanceof QuarrySmasherSound quarrySmasherSound
                            && quarrySmasherSound.isSameEntity(quarrySmasher))) {
                        sound = new QuarrySmasherSound(quarrySmasher);
                        ENTITY_SOUND_INSTANCE_MAP.put(quarrySmasher.getId(), sound);
                    } else {
                        sound = (QuarrySmasherSound) old;
                    }
                    if (!isSoundPlaying(sound) && sound.canPlaySound()) {
                        Minecraft.getInstance().getSoundManager().queueTickingSound(sound);
                    }
                }
                break;
            case 15:
                if (soundEmitter instanceof SubmarineEntity submarine) {
                    SubmarineSound sound;
                    AbstractTickableSoundInstance old = ENTITY_SOUND_INSTANCE_MAP.get(submarine.getId());
                    if (old == null || !(old instanceof SubmarineSound submarineSound
                            && submarineSound.isSameEntity(submarine))) {
                        sound = new SubmarineSound(submarine);
                        ENTITY_SOUND_INSTANCE_MAP.put(submarine.getId(), sound);
                    } else {
                        sound = (SubmarineSound) old;
                    }
                    if (!isSoundPlaying(sound) && sound.canPlaySound()) {
                        Minecraft.getInstance().getSoundManager().queueTickingSound(sound);
                    }
                }
                break;
            case 16:
                if (soundEmitter instanceof TremorzillaEntity tremorzilla) {
                    TremorzillaBeamSound sound;
                    AbstractTickableSoundInstance old = ENTITY_SOUND_INSTANCE_MAP.get(tremorzilla.getId());
                    if (old == null || !(old instanceof TremorzillaBeamSound tremorzillaBeamSound
                            && tremorzillaBeamSound.isSameEntity(tremorzilla))) {
                        sound = new TremorzillaBeamSound(tremorzilla);
                        ENTITY_SOUND_INSTANCE_MAP.put(tremorzilla.getId(), sound);
                    } else {
                        sound = (TremorzillaBeamSound) old;
                    }
                    if (!isSoundPlaying(sound) && sound.canPlaySound()) {
                        Minecraft.getInstance().getSoundManager().queueTickingSound(sound);
                    }
                }
                break;
            case 17:
                if (soundEmitter instanceof GumWormEntity gumWorm) {
                    GumWormSound sound;
                    AbstractTickableSoundInstance old = ENTITY_SOUND_INSTANCE_MAP.get(gumWorm.getId());
                    if (old == null
                            || !(old instanceof GumWormSound gumWormSound && gumWormSound.isSameEntity(gumWorm))) {
                        sound = new GumWormSound(gumWorm);
                        ENTITY_SOUND_INSTANCE_MAP.put(gumWorm.getId(), sound);
                    } else {
                        sound = (GumWormSound) old;
                    }
                    if (!isSoundPlaying(sound) && sound.canPlaySound()) {
                        Minecraft.getInstance().getSoundManager().queueTickingSound(sound);
                    }
                }
                break;
            case 18:
                if (soundEmitter instanceof LivingEntity livingEntity) {
                    SugarRushSound sound;
                    AbstractTickableSoundInstance old = ENTITY_SOUND_INSTANCE_MAP.get(livingEntity.getId());
                    if (old == null || !(old instanceof SugarRushSound sugarRushSound
                            && sugarRushSound.isSameEntity(livingEntity))) {
                        sound = new SugarRushSound(livingEntity);
                        ENTITY_SOUND_INSTANCE_MAP.put(livingEntity.getId(), sound);
                    } else {
                        sound = (SugarRushSound) old;
                    }
                    if (!isSoundPlaying(sound) && sound.canPlaySound()) {
                        Minecraft.getInstance().getSoundManager().queueTickingSound(sound);
                    }
                }
                break;
            case 19:
                if (soundEmitter instanceof CandicornEntity candicorn) {
                    CandicornSound sound;
                    AbstractTickableSoundInstance old = ENTITY_SOUND_INSTANCE_MAP.get(candicorn.getId());
                    if (old == null || !(old instanceof CandicornSound candicornSound
                            && candicornSound.isSameEntity(candicorn))) {
                        sound = new CandicornSound(candicorn);
                        ENTITY_SOUND_INSTANCE_MAP.put(candicorn.getId(), sound);
                    } else {
                        sound = (CandicornSound) old;
                    }
                    if (!isSoundPlaying(sound) && sound.canPlaySound()) {
                        Minecraft.getInstance().getSoundManager().queueTickingSound(sound);
                    }
                }
                break;
        }
    }

    private static final Set<AbstractTickableSoundInstance> QUEUED_SOUNDS = Collections.newSetFromMap(new IdentityHashMap<>());

    /** True when this instance was already queued and has not stopped; otherwise marks it queued and returns false. */
    private boolean isSoundPlaying(AbstractTickableSoundInstance sound) {
        QUEUED_SOUNDS.removeIf(AbstractTickableSoundInstance::isStopped);
        return !QUEUED_SOUNDS.add(sound);
    }

    public void playWorldEvent(int messageId, Level level, BlockPos pos) {
        if (messageId == 0 && AcidBlock.doesBlockCorrode(level.getBlockState(pos))) {
            level.playLocalSound((double) pos.getX() + 0.5D, (double) pos.getY() + 0.5D, (double) pos.getZ() + 0.5D,
                    ACSoundRegistry.ACID_CORROSION.get(), SoundSource.BLOCKS, 0.5F,
                    level.getRandom().nextFloat() * 0.4F + 0.8F, false);
        }
        if (messageId == 1 && level.getBlockState(pos).getBlock() instanceof ActivatedByAltar) {
            level.playLocalSound((double) pos.getX() + 0.5D, (double) pos.getY() + 0.5D, (double) pos.getZ() + 0.5D,
                    ACSoundRegistry.ABYSSMARINE_GLOW_ON.get(), SoundSource.BLOCKS, 1.5F,
                    level.getRandom().nextFloat() * 0.4F + 0.8F, false);
        }
        if (messageId == 2 && level.getBlockState(pos).getBlock() instanceof ActivatedByAltar) {
            level.playLocalSound((double) pos.getX() + 0.5D, (double) pos.getY() + 0.5D, (double) pos.getZ() + 0.5D,
                    ACSoundRegistry.ABYSSMARINE_GLOW_OFF.get(), SoundSource.BLOCKS, 1.5F,
                    level.getRandom().nextFloat() * 0.4F + 0.8F, false);
        }
        if (messageId == 3 && level.getBlockState(pos).is(ACBlockRegistry.DRAIN.get())) {
            level.playLocalSound((double) pos.getX() + 0.5D, (double) pos.getY() + 0.5D, (double) pos.getZ() + 0.5D,
                    ACSoundRegistry.DRAIN_START.get(), SoundSource.BLOCKS, 1.5F, level.getRandom().nextFloat() * 0.4F + 0.8F,
                    false);
        }
        if (messageId == 4 && level.getBlockState(pos).is(ACBlockRegistry.DRAIN.get())) {
            level.playLocalSound((double) pos.getX() + 0.5D, (double) pos.getY() + 0.5D, (double) pos.getZ() + 0.5D,
                    ACSoundRegistry.DRAIN_STOP.get(), SoundSource.BLOCKS, 1.5F, level.getRandom().nextFloat() * 0.4F + 0.8F,
                    false);
        }
        if (messageId == 5 && level.getBlockState(pos).is(ACBlockRegistry.SPELUNKERY_TABLE.get())) {
            level.playLocalSound((double) pos.getX() + 0.5D, (double) pos.getY() + 0.5D, (double) pos.getZ() + 0.5D,
                    ACSoundRegistry.SPELUNKERY_TABLE_FAIL.get(), SoundSource.BLOCKS, 1.5F,
                    level.getRandom().nextFloat() * 0.4F + 0.8F, false);
            BlockParticleOption blockparticleoption = new BlockParticleOption(ParticleTypes.BLOCK,
                    Blocks.STONE.defaultBlockState());
            for (int i = 0; i < 8; i++) {
                level.addParticle(blockparticleoption, pos.getX() + level.getRandom().nextFloat(), pos.getY() + 1.0F,
                        pos.getZ() + level.getRandom().nextFloat(), 0, 0, 0);
            }
        }
        if (messageId == 6 && level.getBlockState(pos).is(ACBlockRegistry.ABYSSAL_ALTAR.get())
                && level.getBlockEntity(pos) instanceof AbyssalAltarBlockEntity altarBlock) {
            altarBlock.resetSlideAnimation();
        }
        if (messageId == 7) {
            for (int i = 0; i < 8; i++) {
                level.addParticle(ACParticleRegistry.PURPLE_WITCH_EXPLOSION.get(),
                        pos.getX() + level.getRandom().nextFloat(), pos.getY() + level.getRandom().nextFloat(),
                        pos.getZ() + level.getRandom().nextFloat(), 0, 0, 0);
            }
        }
        if (messageId == 8) {
            for (int i = 0; i < 15; i++) {
                float particleX = random.nextFloat() * 8 - 4;
                float particleY = random.nextFloat() * 8 - 4;
                float particleZ = random.nextFloat() * 8 - 4;
                level.addAlwaysVisibleParticle(
                        random.nextInt(5) == 0 ? ACParticleRegistry.FROSTMINT_EXPLOSION.get() : ParticleTypes.SNOWFLAKE,
                        true, pos.getX() + particleX, pos.getY() + particleY, pos.getZ() + particleZ, 0, 0, 0);
            }
        }
        if (messageId == 9) {
            for (int i = 0; i < 30; i++) {
                float particleX = random.nextFloat() * 4 - 2;
                float particleY = random.nextFloat() * 4 - 2;
                float particleZ = random.nextFloat() * 4 - 2;
                level.addAlwaysVisibleParticle(ParticleTypes.SNOWFLAKE, true, pos.getX() + particleX,
                        pos.getY() + particleY, pos.getZ() + particleZ, 0, 0, 0);
            }
        }
    }

    public void clearSoundCacheFor(Entity entity) {
        ENTITY_SOUND_INSTANCE_MAP.remove(entity.getId());
    }

    public void clearSoundCacheFor(BlockEntity entity) {
        BLOCK_ENTITY_SOUND_INSTANCE_MAP.remove(entity);
    }

    public Vec3 getDarknessTrailPosFor(LivingEntity living, int pointer, float partialTick) {
        if (living.isRemoved()) {
            partialTick = 1.0F;
        }
        Vec3[] trailPositions = darknessTrailPosMap.get(living);
        if (trailPositions == null || !darknessTrailPointerMap.containsKey(living)) {
            return living.position();
        }
        int trailPointer = darknessTrailPointerMap.get(living);
        int i = trailPointer - pointer & 63;
        int j = trailPointer - pointer - 1 & 63;
        Vec3 d0 = trailPositions[j];
        Vec3 d1 = trailPositions[i].subtract(d0);
        return d0.add(d1.scale(partialTick));
    }

    public int getPlayerTime() {
        return Minecraft.getInstance().player == null ? 0 : Minecraft.getInstance().player.tickCount;
    }

    /** Full-screen nuke flash and watcher possession tint, drawn by the GUI layer registered in ClientModEvents. */
    public void renderScreenEffects(net.minecraft.client.gui.GuiGraphicsExtractor graphics, float partialTick) {
        float screenEffectIntensity = Minecraft.getInstance().options.screenEffectScale().get().floatValue();
        float watcherPossessionStrength = getPossessionStrengthAmount(partialTick);
        float flash = getNukeFlashAmount(partialTick);
        int w = Minecraft.getInstance().getWindow().getGuiScaledWidth();
        int h = Minecraft.getInstance().getWindow().getGuiScaledHeight();
        if (flash > 0 && AlexsCaves.CLIENT_CONFIG.nuclearBombFlash.get()) {
            int a = Math.min(255, (int) (flash * screenEffectIntensity * 255));
            graphics.blit(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, BOMB_FLASH, 0, 0, 0, 0, w, h, w, h, w, h, (a << 24) | 0xFFFFFF);
        }
        if (watcherPossessionStrength > 0) {
            int a = Math.min(255, (int) (watcherPossessionStrength * screenEffectIntensity * 255));
            graphics.blit(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, WATCHER_EFFECT, 0, 0, 0, 0, w, h, w, h, w, h, (a << 24) | 0xFFFFFF);
        }
    }

    public boolean isFirstPersonPlayer(Entity entity) {
        return entity.equals(Minecraft.getInstance().getCameraEntity())
                && Minecraft.getInstance().options.getCameraType().isFirstPerson();
    }

    public void openBookGUI(ItemStack itemStackIn) {
        Minecraft.getInstance().setScreen(new com.github.alexmodguy.alexscaves.client.gui.book.CaveBookScreen());
    }

    @Override
    public Vec3 getCameraRotation() {
        return Vec3.ZERO;
    }

    public void setPrimordialBossActive(Level level, int id, boolean active) {
        if (level.isClientSide()) {
            primordialBossActive = active;
            if (!active && id != -1) {
                Minecraft.getInstance().getMusicManager().stopPlaying(ACMusics.luxtructosaurusBossMusic());
            }
        } else {
            super.setPrimordialBossActive(level, id, active);
        }
    }

    public boolean isPrimordialBossActive(Level level) {
        return level.isClientSide() ? primordialBossActive : super.isPrimordialBossActive(level);
    }

    public static Vec3 processSkyColor(Vec3 colorIn, float partialTick) {
        float primordialAmount = AlexsCaves.PROXY.getPrimordialBossActiveAmount(partialTick);
        if (primordialAmount > 0.0F) {
            Vec3 targetColor = new Vec3(0.2F, 0.15F, 0.1F);
            colorIn = colorIn.add(targetColor.subtract(colorIn).scale(primordialAmount));
        }
        return colorIn;
    }

    public void removeBossBarRender(UUID bossBar) {
        bossBarRenderTypes.remove(bossBar);
    }

    public void setBossBarRender(UUID bossBar, int renderType) {
        bossBarRenderTypes.put(bossBar, renderType);
    }

    public boolean isTickRateModificationActive(Level level) {
        return false;
    }

    @Override
    public boolean isFarFromCamera(double x, double y, double z) {
        return Minecraft.getInstance().gameRenderer.getMainCamera().position().distanceToSqr(x, y, z) >= 256.0D;
    }

    public void renderVanillaMapDecoration(MapDecoration mapDecoration, int index) {

    }
}
