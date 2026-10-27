package cn.leolezury.eternalstarlight.common.client.renderer.entity;

import cn.leolezury.eternalstarlight.common.EternalStarlight;
import cn.leolezury.eternalstarlight.common.client.model.ESArmorSets;
import cn.leolezury.eternalstarlight.common.client.model.entity.LonestarSkeletonModel;
import cn.leolezury.eternalstarlight.common.entity.living.monster.LonestarSkeleton;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.AbstractSkeletonRenderer;
import net.minecraft.client.renderer.entity.ArmorModelSet;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.SkeletonRenderState;
import net.minecraft.resources.Identifier;

public class LonestarSkeletonRenderer extends AbstractSkeletonRenderer<LonestarSkeleton, SkeletonRenderState> {
	private static final Identifier LONESTAR_SKELETON_LOCATION = EternalStarlight.id("textures/entity/lonestar_skeleton.png");

	public static final ModelLayerLocation LONESTAR = new ModelLayerLocation(EternalStarlight.id("lonestar_skeleton"), "main");
	public static final ArmorModelSet<ModelLayerLocation> LONESTAR_ARMOR = ESArmorSets.create("lonestar_skeleton");

	public LonestarSkeletonRenderer(EntityRendererProvider.Context context) {
		super(context, LONESTAR_ARMOR, new LonestarSkeletonModel(context.bakeLayer(LONESTAR)));
	}

	@Override
	public SkeletonRenderState createRenderState() {
		return new SkeletonRenderState();
	}

	@Override
	public Identifier getTextureLocation(SkeletonRenderState state) {
		return LONESTAR_SKELETON_LOCATION;
	}
}
