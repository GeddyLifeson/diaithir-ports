package vectorwing.farmersdelight.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.StandingSignRenderer;
import net.minecraft.client.renderer.blockentity.state.StandingSignRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import vectorwing.farmersdelight.common.block.state.CanvasSign;
import vectorwing.farmersdelight.common.registry.ModAtlases;

/** Vanilla's spruce sign, drawn with the canvas texture of the sign's dye. */
public class CanvasSignRenderer extends StandingSignRenderer
{
	private @Nullable DyeColor dye;

	public CanvasSignRenderer(BlockEntityRendererProvider.Context context) {
		super(context);
	}

	@Override
	public StandingSignRenderState createRenderState() {
		return new State();
	}

	@Override
	public void extractRenderState(SignBlockEntity sign, StandingSignRenderState state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
		super.extractRenderState(sign, state, partialTicks, cameraPosition, breakProgress);
		((State) state).dye = sign.getBlockState().getBlock() instanceof CanvasSign canvas ? canvas.getBackgroundColor() : null;
	}

	// getSignSprite only gets the wood type, so the dye of the sign being drawn is held here; submits run one at a time
	@Override
	public void submit(StandingSignRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
		this.dye = ((State) state).dye;
		super.submit(state, poseStack, collector, camera);
	}

	@Override
	protected SpriteId getSignSprite(WoodType type) {
		return ModAtlases.getCanvasSignMaterial(dye);
	}

	public static class State extends StandingSignRenderState
	{
		@Nullable DyeColor dye;
	}
}
