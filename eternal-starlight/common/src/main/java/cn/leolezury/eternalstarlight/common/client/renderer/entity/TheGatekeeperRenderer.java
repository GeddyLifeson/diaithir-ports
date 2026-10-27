package cn.leolezury.eternalstarlight.common.client.renderer.entity;

import cn.leolezury.eternalstarlight.common.EternalStarlight;
import cn.leolezury.eternalstarlight.common.client.ESRenderType;
import cn.leolezury.eternalstarlight.common.client.compat.ESMobRenderer;
import cn.leolezury.eternalstarlight.common.client.compat.ESRender;
import cn.leolezury.eternalstarlight.common.client.compat.ESRenderState;
import cn.leolezury.eternalstarlight.common.client.compat.RecordingBufferSource;
import cn.leolezury.eternalstarlight.common.client.model.ESModelUtil;
import cn.leolezury.eternalstarlight.common.client.model.entity.TheGatekeeperModel;
import cn.leolezury.eternalstarlight.common.client.renderer.layer.GatekeeperHandItemLayer;
import cn.leolezury.eternalstarlight.common.client.renderer.layer.TheGatekeeperClothingLayer;
import cn.leolezury.eternalstarlight.common.entity.living.boss.gatekeeper.GatekeeperTeleportPhase;
import cn.leolezury.eternalstarlight.common.entity.living.boss.gatekeeper.TheGatekeeper;
import cn.leolezury.eternalstarlight.common.util.ModelPartPose;
import cn.leolezury.eternalstarlight.common.util.ModelSnapshot;
import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.datafixers.util.Pair;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.PlayerModelType;
import net.minecraft.world.entity.player.PlayerSkin;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class TheGatekeeperRenderer<T extends TheGatekeeper> extends ESMobRenderer<T, TheGatekeeperModel<T>> {
	private static final Identifier ENTITY_TEXTURE = EternalStarlight.id("textures/entity/the_gatekeeper.png");
	private static final Identifier SLIM_ENTITY_TEXTURE = EternalStarlight.id("textures/entity/the_gatekeeper_slim.png");
	private static final int PHANTOM_AMOUNT = 5;
	private static final int SNAPSHOT_LIFESPAN = 15;
	private static final Map<String, GameProfile> PROFILES = new HashMap<>();
	private static final Set<String> PENDING_PROFILES = new HashSet<>();
	private final TheGatekeeperModel<T> normalModel;
	private final TheGatekeeperModel<T> slimModel;
	private final TheGatekeeperClothingLayer<T> clothingLayer;

	public TheGatekeeperRenderer(EntityRendererProvider.Context context) {
		super(context, new TheGatekeeperModel<>(context.bakeLayer(TheGatekeeperModel.LAYER_LOCATION), false), 0.5f);
		normalModel = getModel();
		slimModel = new TheGatekeeperModel<>(context.bakeLayer(TheGatekeeperModel.SLIM_LAYER_LOCATION), true);
		this.addLayer(new GatekeeperHandItemLayer<>(this));
		this.clothingLayer = new TheGatekeeperClothingLayer<>(this, context.getModelSet());
		this.addLayer(this.clothingLayer);
	}

	@Override
	public void submit(ESRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
		T entity = cast(state);
		getGameProfile(entity).ifPresent(p -> this.model = getSkin(p).map(PlayerSkin::model).orElse(PlayerModelType.WIDE) == PlayerModelType.SLIM ? slimModel : normalModel);
		if (entity.getBehaviorState() == GatekeeperTeleportPhase.ID) {
			// the body is drawn several times with different opacities, so it is drawn by hand instead of through the vanilla model path
			this.currentState = state;
			this.collector = collector;
			this.camera = camera;
			RecordingBufferSource.submit(collector, poseStack, source -> {
				float partialTicks = state.partialTicks;
				float ageTicks = entity.tickCount + partialTicks;
				float progress = Math.min((1 - Math.abs(Mth.clamp((float) entity.teleportAnimationState.getTimeInMillis(ageTicks) / 1000f * 20f, 0, 37) / 37 - 0.5f) * 2) * 1.5f, 1);
				float radius = progress * 0.5f;
				drawBody(entity, state, poseStack, source, state.lightCoords, 1 - progress);
				for (int i = 0; i < PHANTOM_AMOUNT; i++) {
					poseStack.pushPose();
					float spin = progress * Mth.HALF_PI + i * Mth.TWO_PI / PHANTOM_AMOUNT;
					Vector3f offset = new Vector3f(radius * Mth.sin(spin), radius * Mth.cos(spin), 0);
					offset.rotate(ESRender.cameraOrientation());
					poseStack.translate(offset.x, offset.y, offset.z);
					drawBody(entity, state, poseStack, source, state.lightCoords, (1 - progress) / 3);
					poseStack.popPose();
				}
				getModel().alphaFactor = 1;
				render(entity, state.entityYaw, partialTicks, poseStack, source, state.lightCoords);
			});
		} else {
			super.submit(state, poseStack, collector, camera);
		}
	}

	private void drawBody(T entity, ESRenderState state, PoseStack stack, MultiBufferSource source, int light, float alpha) {
		TheGatekeeperModel<T> gatekeeperModel = getModel();
		gatekeeperModel.setupAnim(state);
		gatekeeperModel.alphaFactor = alpha;
		stack.pushPose();
		stack.scale(state.scale, state.scale, state.scale);
		setupRotations(state, stack, state.bodyRot, state.scale);
		stack.scale(-1.0F, -1.0F, 1.0F);
		scale(entity, stack, state.partialTicks);
		stack.translate(0.0F, -1.501F, 0.0F);
		VertexConsumer consumer = source.getBuffer(RenderTypes.entityTranslucent(getTextureLocation(entity)));
		gatekeeperModel.renderWithAlpha(stack, consumer, light, OverlayTexture.NO_OVERLAY, -1);
		this.clothingLayer.render(stack, source, light, entity, state.walkAnimationPos, state.walkAnimationSpeed, state.partialTicks, state.ageInTicks, state.yRot, state.xRot);
		stack.popPose();
	}

	@Override
	public void render(T entity, float entityYaw, float partialTicks, PoseStack stack, MultiBufferSource bufferSource, int packedLight) {
		if (entity.isAlive()) {
			double currentX = Mth.lerp(partialTicks, entity.xo, entity.getX());
			double currentY = Mth.lerp(partialTicks, entity.yo, entity.getY());
			double currentZ = Mth.lerp(partialTicks, entity.zo, entity.getZ());
			float currentTick = (entity.tickCount + partialTicks);
			if (entity.trailSnapshots.isEmpty() || currentTick - entity.lastTrailTick > 3) {
				if (entity.shouldAddTrailSnapshot()) {
					Map<String, ModelPartPose> snapshot = ESModelUtil.saveModelSnapshot(getModel().allPartNames, getModel()::getAnyDescendantWithName);
					entity.trailSnapshots.addFirst(Pair.of(new Vec3(currentX, currentY, currentZ), new ModelSnapshot(0, Mth.rotLerp(partialTicks, entity.yBodyRotO, entity.yBodyRot), currentTick, snapshot)));
					entity.lastTrailTick = currentTick;
				}
				entity.trailSnapshots.removeIf(p -> currentTick - p.getSecond().timestamp() > SNAPSHOT_LIFESPAN);
				while (entity.trailSnapshots.size() > 32) {
					entity.trailSnapshots.removeLast();
				}
			}
			for (int i = 0; i < entity.trailSnapshots.size(); i++) {
				stack.pushPose();
				Vec3 trailPos = entity.trailSnapshots.get(i).getFirst();
				ModelSnapshot snapshot = entity.trailSnapshots.get(i).getSecond();
				ESModelUtil.loadPoseFromSnapshot(snapshot.poses(), getModel()::getAnyDescendantWithName);
				stack.translate(trailPos.x - currentX, trailPos.y - currentY, trailPos.z - currentZ);
				getModel().alphaFactor = (1 - Mth.clamp(currentTick - snapshot.timestamp(), 0, SNAPSHOT_LIFESPAN) / SNAPSHOT_LIFESPAN) * 0.3F;
				stack.mulPose(Axis.YP.rotationDegrees(180.0F - snapshot.yRot()));
				stack.scale(-1.0F, -1.0F, 1.0F);
				this.scale(entity, stack, partialTicks);
				stack.translate(0.0F, -1.5F, 0.0F);
				RenderType renderType = ESRenderType.entityTranslucentNoDepth(getTextureLocation(entity));
				VertexConsumer vertexConsumer = bufferSource.getBuffer(renderType);
				getModel().renderWithAlpha(stack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY, -1);
				stack.popPose();
			}
			getModel().alphaFactor = 1;
		}
	}

	@Override
	protected float getShadowRadius(T mob) {
		return mob.getBehaviorState() == GatekeeperTeleportPhase.ID ? 0 : super.getShadowRadius(mob);
	}

	@Override
	public Identifier getTextureLocation(T entity) {
		Identifier texture = model == slimModel ? SLIM_ENTITY_TEXTURE : ENTITY_TEXTURE;
		Optional<GameProfile> profile = getGameProfile(entity);
		if (profile.isPresent()) {
			Optional<PlayerSkin> playerSkin = getSkin(profile.get());
			if (playerSkin.isPresent() && !playerSkin.get().body().texturePath().getPath().startsWith("textures/entity/player/")) {
				texture = playerSkin.get().body().texturePath();
			}
		}
		return texture;
	}

	public static Optional<PlayerSkin> getSkin(GameProfile profile) {
		Optional<PlayerSkin> skin = Minecraft.getInstance().getSkinManager().get(profile).getNow(Optional.empty());
		return skin == null ? Optional.empty() : skin;
	}

	public static Optional<GameProfile> getGameProfile(TheGatekeeper entity) {
		if (entity.getCustomName() != null) {
			String customName = entity.getCustomName().getString();
			if (!PROFILES.containsKey(customName) && PENDING_PROFILES.add(customName)) {
				CompletableFuture.supplyAsync(() -> Minecraft.getInstance().services().profileResolver().fetchByName(customName))
					.thenAccept(optional -> optional.ifPresent(p -> PROFILES.put(customName, p)));
			}
			if (PROFILES.containsKey(customName)) {
				return Optional.ofNullable(PROFILES.get(customName));
			}
		}
		return Optional.empty();
	}
}
