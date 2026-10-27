package cn.leolezury.eternalstarlight.common.client.renderer.entity;

import cn.leolezury.eternalstarlight.common.EternalStarlight;
import cn.leolezury.eternalstarlight.common.entity.misc.ESBoat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.object.boat.BoatModel;
import net.minecraft.client.model.object.boat.RaftModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.BoatRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.util.Unit;
import org.joml.Quaternionf;

import java.util.EnumMap;
import java.util.Map;

public class ESBoatRenderer extends EntityRenderer<ESBoat, ESBoatRenderer.State> {
	public static class State extends BoatRenderState {
		public ESBoat.Type type = ESBoat.Type.LUNAR;
	}

	private final Map<ESBoat.Type, Identifier> textures = new EnumMap<>(ESBoat.Type.class);
	private final Map<ESBoat.Type, EntityModel<BoatRenderState>> models = new EnumMap<>(ESBoat.Type.class);
	private final Model.Simple waterPatchModel;

	public ESBoatRenderer(EntityRendererProvider.Context context, boolean chest) {
		super(context);
		this.shadowRadius = 0.8F;
		this.waterPatchModel = new Model.Simple(context.bakeLayer(ModelLayers.BOAT_WATER_PATCH), t -> RenderTypes.waterMask());
		for (ESBoat.Type type : ESBoat.Type.values()) {
			textures.put(type, EternalStarlight.id(getTextureLocation(type, chest)));
			models.put(type, createBoatModel(context, type, chest));
		}
	}

	private static ModelLayerLocation createLocation(String path, String model) {
		return new ModelLayerLocation(EternalStarlight.id(path), model);
	}

	public static ModelLayerLocation createBoatModelName(ESBoat.Type pType) {
		return createLocation("boat/" + pType.getName(), "main");
	}

	public static ModelLayerLocation createChestBoatModelName(ESBoat.Type type) {
		return createLocation("chest_boat/" + type.getName(), "main");
	}

	private EntityModel<BoatRenderState> createBoatModel(EntityRendererProvider.Context context, ESBoat.Type type, boolean chest) {
		ModelLayerLocation modellayerlocation = chest ? createChestBoatModelName(type) : createBoatModelName(type);
		if (type == ESBoat.Type.JINGLESTEM) {
			return new RaftModel(context.bakeLayer(modellayerlocation));
		}
		return new BoatModel(context.bakeLayer(modellayerlocation));
	}

	private static String getTextureLocation(ESBoat.Type type, boolean chest) {
		return chest ? "textures/entity/chest_boat/" + type.getName() + ".png" : "textures/entity/boat/" + type.getName() + ".png";
	}

	@Override
	public State createRenderState() {
		return new State();
	}

	@Override
	public void extractRenderState(ESBoat entity, State state, float partialTicks) {
		super.extractRenderState(entity, state, partialTicks);
		state.type = entity.getESBoatType();
		state.yRot = entity.getYRot(partialTicks);
		state.hurtTime = entity.getHurtTime() - partialTicks;
		state.hurtDir = entity.getHurtDir();
		state.damageTime = Math.max(entity.getDamage() - partialTicks, 0.0F);
		state.bubbleAngle = entity.getBubbleAngle(partialTicks);
		state.isUnderWater = entity.isUnderWater();
		state.rowingTimeLeft = entity.getRowingTime(0, partialTicks);
		state.rowingTimeRight = entity.getRowingTime(1, partialTicks);
	}

	@Override
	public void submit(State state, PoseStack stack, SubmitNodeCollector collector, CameraRenderState camera) {
		stack.pushPose();
		stack.translate(0.0F, 0.375F, 0.0F);
		stack.mulPose(Axis.YP.rotationDegrees(180.0F - state.yRot));
		float hurt = state.hurtTime;
		if (hurt > 0.0F) {
			stack.mulPose(Axis.XP.rotationDegrees(Mth.sin(hurt) * hurt * state.damageTime / 10.0F * state.hurtDir));
		}
		if (!state.isUnderWater && !Mth.equal(state.bubbleAngle, 0.0F)) {
			stack.mulPose(new Quaternionf().setAngleAxis(state.bubbleAngle * ((float) Math.PI / 180F), 1.0F, 0.0F, 1.0F));
		}
		stack.scale(-1.0F, -1.0F, 1.0F);
		stack.mulPose(Axis.YP.rotationDegrees(90.0F));
		Identifier texture = textures.get(state.type);
		EntityModel<BoatRenderState> model = models.get(state.type);
		collector.submitModel(model, state, stack, texture, state.lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor, null);
		if (!state.isUnderWater && model instanceof BoatModel) {
			collector.submitModel(this.waterPatchModel, Unit.INSTANCE, stack, texture, state.lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor, null);
		}
		stack.popPose();
		super.submit(state, stack, collector, camera);
	}
}
