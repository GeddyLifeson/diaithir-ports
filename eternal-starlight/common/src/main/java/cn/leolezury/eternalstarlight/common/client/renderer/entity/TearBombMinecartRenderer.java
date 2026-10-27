package cn.leolezury.eternalstarlight.common.client.renderer.entity;

import cn.leolezury.eternalstarlight.common.EternalStarlight;
import cn.leolezury.eternalstarlight.common.entity.misc.TearBombMinecart;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.entity.AbstractMinecartRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.TntMinecartRenderer;
import net.minecraft.client.renderer.entity.state.MinecartTntRenderState;
import net.minecraft.util.Mth;

public class TearBombMinecartRenderer extends AbstractMinecartRenderer<TearBombMinecart, MinecartTntRenderState> {
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(EternalStarlight.id("tear_bomb_minecart"), "main");

	public TearBombMinecartRenderer(EntityRendererProvider.Context context) {
		super(context, LAYER_LOCATION);
	}

	@Override
	public MinecartTntRenderState createRenderState() {
		return new MinecartTntRenderState();
	}

	@Override
	public void extractRenderState(TearBombMinecart entity, MinecartTntRenderState state, float partialTicks) {
		super.extractRenderState(entity, state, partialTicks);
		state.fuseRemainingInTicks = entity.getFuse() > -1 ? entity.getFuse() - partialTicks + 1.0F : -1.0F;
	}

	@Override
	protected void submitMinecartContents(MinecartTntRenderState state, BlockModelRenderState blockModel, PoseStack poseStack, SubmitNodeCollector collector, int lightCoords) {
		float fuse = state.fuseRemainingInTicks;
		if (fuse > -1.0F && fuse < 10.0F) {
			float f = 1.0F - fuse / 10.0F;
			f = Mth.clamp(f, 0.0F, 1.0F);
			f *= f;
			f *= f;
			float f1 = 1.0F + f * 0.3F;
			poseStack.scale(f1, f1, f1);
		}
		TntMinecartRenderer.submitWhiteSolidBlock(blockModel, poseStack, collector, lightCoords, fuse > -1.0F && (int) fuse / 5 % 2 == 0, state.outlineColor);
	}
}
