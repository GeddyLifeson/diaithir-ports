package cn.leolezury.eternalstarlight.common.client.handler;

import cn.leolezury.eternalstarlight.common.EternalStarlight;
import cn.leolezury.eternalstarlight.common.client.posteffect.PostEffectRenderer;
import cn.leolezury.eternalstarlight.common.client.posteffect.WorldPostEffectManager;
import cn.leolezury.eternalstarlight.common.client.resource.ESBookLoader;
import cn.leolezury.eternalstarlight.common.client.shader.ESShaders;
import cn.leolezury.eternalstarlight.common.client.sound.BossMusicSoundInstance;
import cn.leolezury.eternalstarlight.common.client.trail.TrailManager;
import cn.leolezury.eternalstarlight.common.client.visual.DelayedMultiBufferSource;
import cn.leolezury.eternalstarlight.common.client.visual.ScreenShake;
import cn.leolezury.eternalstarlight.common.client.weather.ClientWeatherState;
import cn.leolezury.eternalstarlight.common.config.ESConfig;
import cn.leolezury.eternalstarlight.common.data.ESBiomes;
import cn.leolezury.eternalstarlight.common.data.ESDimensions;
import cn.leolezury.eternalstarlight.common.entity.living.boss.ESBoss;
import cn.leolezury.eternalstarlight.common.entity.living.boss.ESServerBossEvent;
import cn.leolezury.eternalstarlight.common.entity.projectile.SoulitSpectator;
import cn.leolezury.eternalstarlight.common.item.combat.DualWieldingSwordItem;
import cn.leolezury.eternalstarlight.common.network.UpdateBookProgressionPacket;
import cn.leolezury.eternalstarlight.common.platform.ESClientPlatform;
import cn.leolezury.eternalstarlight.common.platform.ESPlatform;
import cn.leolezury.eternalstarlight.common.registry.*;
import cn.leolezury.eternalstarlight.common.util.ESBlockUtil;
import cn.leolezury.eternalstarlight.common.util.ESTags;
import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.shaders.FogShape;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.AttackIndicatorStatus;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.LerpingBossEvent;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.fog.FogRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.FogType;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import java.util.*;
import java.util.stream.Collectors;

public class ESClientHandler {
	public static ESBookLoader books;
	public static final Map<UUID, Integer> BOSS_BAR_TYPES = new HashMap<>();
	public static final List<ScreenShake> SCREEN_SHAKES = new ArrayList<>();
	private static final Identifier ETHER_EROSION_OVERLAY = EternalStarlight.id("textures/misc/ether_erosion.png");
	private static final Identifier ETHER_ARMOR_EMPTY = EternalStarlight.id("textures/gui/hud/ether_armor_empty.png");
	private static final Identifier ETHER_ARMOR_HALF = EternalStarlight.id("textures/gui/hud/ether_armor_half.png");
	private static final Identifier ETHER_ARMOR_FULL = EternalStarlight.id("textures/gui/hud/ether_armor_full.png");
	private static final Identifier CROSSHAIR_ATTACK_INDICATOR_FULL_SPRITE = Identifier.withDefaultNamespace("hud/crosshair_attack_indicator_full");
	private static final Identifier CROSSHAIR_ATTACK_INDICATOR_BACKGROUND_SPRITE = Identifier.withDefaultNamespace("hud/crosshair_attack_indicator_background");
	private static final Identifier CROSSHAIR_ATTACK_INDICATOR_PROGRESS_SPRITE = Identifier.withDefaultNamespace("hud/crosshair_attack_indicator_progress");
	private static final Identifier HOTBAR_ATTACK_INDICATOR_BACKGROUND_SPRITE = Identifier.withDefaultNamespace("hud/hotbar_attack_indicator_background");
	private static final Identifier HOTBAR_ATTACK_INDICATOR_PROGRESS_SPRITE = Identifier.withDefaultNamespace("hud/hotbar_attack_indicator_progress");
	private static final Identifier LUNARIS_CACTUS_BLUR_LOCATION = EternalStarlight.id("textures/misc/lunaris_cactus_blur.png");
	public static final Identifier WIP_LOCATION = EternalStarlight.id("textures/gui/wip.png");
	private static final List<DreamCatcherText> DREAM_CATCHER_TEXTS = new ArrayList<>();
	private static final Set<EntityType<?>> CROSSHAIR_PICKED_ENTITIES = new HashSet<>();
	public static int clientTickCount = 0;
	public static BossMusicSoundInstance bossMusicInstance = null;
	private static float oldPortalTicks;
	private static float portalTicks;
	private static float oldAuroraIntensity;
	private static float auroraIntensity;
	public static int resetCameraIn;
	public static float fogStartDecrement;
	public static float fogEndDecrement;
	public static float oldAbyssalFogModifier = 1;
	public static float abyssalFogModifier = 1;
	public static boolean oldTearyEffect;
	public static boolean tearyEffect;
	public static int oldSeedsLauncherAnimTicks;
	public static int seedsLauncherAnimTicks;
	public static boolean isHalloween;

	public static void onClientTick() {
		if (Minecraft.getInstance().level == null) {
			clearWorldSpecificVars();
		}
		LocalPlayer player = Minecraft.getInstance().player;
		ClientWeatherState.tickRainLevel();
		if (Minecraft.getInstance().level != null && Minecraft.getInstance().level.tickRateManager().runsNormally()) {
			if (!Minecraft.getInstance().isPaused()) {
				clientTickCount++;

				// halloween
				if (clientTickCount % 1000 == 5) {
					Calendar calendar = Calendar.getInstance();
					isHalloween = calendar.get(Calendar.MONTH) == Calendar.NOVEMBER && calendar.get(Calendar.DAY_OF_MONTH) == 1;
				}

				// seeds launcher
				oldSeedsLauncherAnimTicks = seedsLauncherAnimTicks;
				if (seedsLauncherAnimTicks > 0) {
					seedsLauncherAnimTicks--;
				}
			}

			// trails
			if (!Minecraft.getInstance().isPaused()) {
				TrailManager.tick(Minecraft.getInstance().level);
			}

			for (ScreenShake effect : SCREEN_SHAKES) {
				if (!effect.shouldRemove() && !Minecraft.getInstance().isPaused()) {
					effect.tick();
				}
			}
			SCREEN_SHAKES.removeIf(ScreenShake::shouldRemove);

			// post effects
			if (!Minecraft.getInstance().isPaused()) {
				WorldPostEffectManager.tick();
			}

			// weather
			if (!Minecraft.getInstance().isPaused() && ClientWeatherState.weather != null && Minecraft.getInstance().level.dimension().location().equals(ESDimensions.STARLIGHT_KEY.location())) {
				ClientWeatherState.weather.clientTick();
			}

			// boss music
			if (player != null) {
				if (player.tickCount % 20 == 0) {
					if (bossMusicInstance == null) {
						List<ESBoss> bosses = Minecraft.getInstance().level.getEntitiesOfClass(ESBoss.class, player.getBoundingBox().inflate(50));
						bosses.sort(Comparator.comparingDouble(b -> b.distanceToSqr(player)));
						bosses = bosses.stream().filter(ESBoss::shouldPlayBossMusic).filter(b -> b.tickCount > 20 && player.hasLineOfSight(b)).toList();
						if (!bosses.isEmpty()) {
							ESBoss boss = bosses.getFirst();
							bossMusicInstance = new BossMusicSoundInstance(boss.getBossMusic(), boss);
						}
					} else {
						if (bossMusicInstance.shouldStopMusic(player)) {
							Minecraft.getInstance().getSoundManager().stop(bossMusicInstance);
							bossMusicInstance.stopMusic();
							bossMusicInstance = null;
						}
					}
					if (bossMusicInstance != null && !Minecraft.getInstance().getSoundManager().isActive(bossMusicInstance)) {
						Minecraft.getInstance().getSoundManager().play(bossMusicInstance);
					}
				}
			} else if (bossMusicInstance != null) {
				Minecraft.getInstance().getSoundManager().stop(bossMusicInstance);
				bossMusicInstance.stopMusic();
				bossMusicInstance = null;
			}
		}

		if (player != null) {
			// portal animation
			oldPortalTicks = portalTicks;
			if (player.portalProcess != null && player.portalProcess.isSamePortal(ESBlocks.STARLIGHT_PORTAL.get()) && player.portalProcess.isInsidePortalThisTick()) {
				portalTicks++;
				player.portalProcess.setAsInsidePortalThisTick(false);
			} else {
				portalTicks -= 2;
			}
			portalTicks = Mth.clamp(portalTicks, 0, 80);

			// entity progression
			Entity entity = Minecraft.getInstance().crosshairPickEntity;
			if (entity != null && entity.getType().builtInRegistryHolder().is(ESTags.EntityTypes.AFFECTS_PROGRESSION)) {
				CROSSHAIR_PICKED_ENTITIES.add(entity.getType());
			}
			if (clientTickCount % 60 == 0) {
				ESClientPlatform.INSTANCE.sendToServer(new UpdateBookProgressionPacket(CROSSHAIR_PICKED_ENTITIES.stream().map(type -> BuiltInRegistries.ENTITY_TYPE.getKey(type).withPrefix("entity_seen_")).collect(Collectors.toSet())));
				CROSSHAIR_PICKED_ENTITIES.clear();
			}

			// aurora
			oldAuroraIntensity = auroraIntensity;
			if (player.level().dimension() == ESDimensions.STARLIGHT_KEY) {
				if (ClientWeatherState.weather == ESWeathers.AURORA.get()) {
					auroraIntensity += 0.05f;
				} else {
					auroraIntensity -= 0.05f;
				}
			} else {
				oldAuroraIntensity = 0;
				auroraIntensity = 0;
			}
			auroraIntensity = Mth.clamp(auroraIntensity, 0, 1);

			// soulit spectator
			if (resetCameraIn > 0) {
				resetCameraIn--;
				Minecraft.getInstance().options.hideGui = true;
				if (resetCameraIn <= 0) {
					Minecraft.getInstance().setCameraEntity(player);
					Minecraft.getInstance().options.hideGui = false;
					resetCameraIn = 0;
				}
			} else if (Minecraft.getInstance().getCameraEntity() instanceof SoulitSpectator) {
				Minecraft.getInstance().setCameraEntity(player);
				Minecraft.getInstance().options.hideGui = false;
			}

			// fog
			Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
			fogStartDecrement -= 0.5f;
			fogEndDecrement -= 0.5f;
			Holder<Biome> biomeHolder = player.level().getBiome(player.blockPosition());
			if (camera.getFluidInCamera() == FogType.NONE && player.level().getBlockState(camera.getBlockPosition()).getFluidState().isEmpty()) {
				if (biomeHolder.is(ESTags.Biomes.PERMAFROST)) {
					fogStartDecrement += (0.5f + 0.75f);
					fogEndDecrement += (0.5f + 0.5f);
					fogEndDecrement = Mth.clamp(fogEndDecrement, 0, 80);
				}
			}
			fogStartDecrement = Mth.clamp(fogStartDecrement, 0, 100);
			fogEndDecrement = Mth.clamp(fogEndDecrement, 0, 100);
			oldAbyssalFogModifier = abyssalFogModifier;
			if (biomeHolder.is(ESBiomes.THE_ABYSS)) {
				abyssalFogModifier -= 0.02f;
			} else {
				abyssalFogModifier += 0.02f;
			}
			abyssalFogModifier = Mth.clamp(abyssalFogModifier, 0, 1);

			// teary
			oldTearyEffect = tearyEffect;
			tearyEffect = player.hasEffect(ESMobEffects.TEARY.asHolder());

			// dream catcher
			if (player.hasEffect(ESMobEffects.DREAM_CATCHER.asHolder())) {
				for (DreamCatcherText text : DREAM_CATCHER_TEXTS) {
					text.updatePosition();
				}
				DREAM_CATCHER_TEXTS.removeIf(text -> text.getX() - Minecraft.getInstance().font.width(text.getText()) / 2 > Minecraft.getInstance().getWindow().getGuiScaledWidth() || text.getY() - Minecraft.getInstance().font.lineHeight / 2 > Minecraft.getInstance().getWindow().getGuiScaledHeight());
				if (player.getRandom().nextInt(40) == 0 && DREAM_CATCHER_TEXTS.size() < 20) {
					Component dreamCatcherText = Component.translatable("message." + EternalStarlight.ID + ".dream_catcher_" + player.getRandom().nextInt(7));
					DREAM_CATCHER_TEXTS.add(new DreamCatcherText(dreamCatcherText, (int) (player.getRandom().nextFloat() * 5 + 1), -Minecraft.getInstance().font.width(dreamCatcherText) / 2, player.getRandom().nextInt(Minecraft.getInstance().getWindow().getGuiScaledHeight())));
				}
			} else {
				DREAM_CATCHER_TEXTS.clear();
			}
		} else {
			DREAM_CATCHER_TEXTS.clear();
			tearyEffect = false;
		}
		if (oldTearyEffect != tearyEffect) {
			Minecraft.getInstance().gameRenderer.checkEntityPostEffect(Minecraft.getInstance().cameraEntity);
		}
	}

	private static void clearWorldSpecificVars() {
		BOSS_BAR_TYPES.clear();
		TrailManager.clear();
		SCREEN_SHAKES.clear();
		WorldPostEffectManager.clear();
		DREAM_CATCHER_TEXTS.clear();
		if (bossMusicInstance != null) {
			Minecraft.getInstance().getSoundManager().stop(bossMusicInstance);
			bossMusicInstance.stopMusic();
			bossMusicInstance = null;
		}
		oldPortalTicks = 0;
		portalTicks = 0;
		oldAuroraIntensity = 0;
		auroraIntensity = 0;
		resetCameraIn = 0;
		fogStartDecrement = 0;
		fogEndDecrement = 0;
		oldAbyssalFogModifier = 1;
		abyssalFogModifier = 1;
		oldTearyEffect = false;
		tearyEffect = false;
		oldSeedsLauncherAnimTicks = 0;
		seedsLauncherAnimTicks = 0;
	}

	public static float getScreenShakeYawOffset() {
		if (!ESConfig.INSTANCE.enableScreenShake) {
			return 0;
		}
		float sum = 0;
		for (ScreenShake screenShake : SCREEN_SHAKES) {
			sum += screenShake.getYawOffset();
		}
		return sum;
	}

	public static float getScreenShakePitchOffset() {
		if (!ESConfig.INSTANCE.enableScreenShake) {
			return 0;
		}
		float sum = 0;
		for (ScreenShake screenShake : SCREEN_SHAKES) {
			sum += screenShake.getPitchOffset();
		}
		return sum;
	}

	public static Vec3 onComputeCameraAngles(Vec3 angles) {
		return angles.add(Mth.clamp(getScreenShakePitchOffset(), -0.5, 0.5), Mth.clamp(getScreenShakeYawOffset(), -0.5, 0.5), 0);
	}

	public static OptionalDouble onComputeFovModifier(float original) {
		float modified = original;
		LocalPlayer player = Minecraft.getInstance().player;
		if (player != null && player.isUsingItem()) {
			ItemStack itemStack = player.getUseItem();
			if (BuiltInRegistries.ITEM.getKey(itemStack.getItem()).getNamespace().equals(EternalStarlight.ID) && itemStack.getItem() instanceof BowItem) {
				float f = player.getTicksUsingItem() / 20.0F;
				f = f > 1.0F ? 1.0F : f * f;
				modified = (float) Mth.lerp(Minecraft.getInstance().options.fovEffectScale().get(), 1.0F, (modified * (1.0F - f * 0.15F)));
			}
		}
		float portal = Mth.lerp(Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(Minecraft.getInstance().level != null && Minecraft.getInstance().level.tickRateManager().runsNormally()), oldPortalTicks, portalTicks) / 80f;
		if (portal > 0) {
			modified = (float) Mth.lerp(Minecraft.getInstance().options.fovEffectScale().get(), 1.0F, (modified * (1.0F - portal)));
		}
		return modified == original ? OptionalDouble.empty() : OptionalDouble.of(modified);
	}

	public static boolean onRenderBlockOverlay(Player player, BlockState state) {
		if (player.hasEffect(ESMobEffects.OBLIVION.asHolder()) && !state.is(ESTags.Blocks.UNAFFECTED_BY_OBLIVION)) {
			return false;
		}
		return true;
	}

	public static int getEtherTint(BlockAndTintGetter getter, BlockPos pos) {
		double progress = getter != null && pos != null ? (ESClientSetupHandler.COLOR_NOISE.getValue(pos.getX() / 15.0, pos.getY() / 15.0, pos.getZ() / 15.0) + 1) / 2 : (Math.sin((ESClientHandler.clientTickCount + Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(Minecraft.getInstance().level != null && Minecraft.getInstance().level.tickRateManager().runsNormally())) / 30.0) + 1) / 2;
		return ARGB.color((int) Mth.lerp(progress, 190, 255), (int) Mth.lerp(progress, 240, 255), (int) Mth.lerp(progress, 230, 255));
	}

	public static int getAirBubbleYOffset() {
		LocalPlayer player = Minecraft.getInstance().player;
		if (player != null && player.level().getBiome(player.blockPosition()).is(ESBiomes.THE_ABYSS) && player.getY() < 0) {
			return player.getRandom().nextInt(2);
		}
		return 0;
	}

	private static class DreamCatcherText {
		private final Component text;
		private final int speed;
		private int x;
		private final int y;

		public DreamCatcherText(Component component, int speed, int x, int y) {
			this.text = component;
			this.speed = speed;
			this.x = x;
			this.y = y;
		}

		public Component getText() {
			return text;
		}

		public int getX() {
			return x;
		}

		public int getY() {
			return y;
		}

		public void updatePosition() {
			x += speed;
		}
	}
}
