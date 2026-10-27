package cn.leolezury.eternalstarlight.common.client.renderer.entity;

import cn.leolezury.eternalstarlight.common.client.compat.ESMobRenderer;
import cn.leolezury.eternalstarlight.common.EternalStarlight;
import cn.leolezury.eternalstarlight.common.client.model.entity.ThirstWalkerModel;
import cn.leolezury.eternalstarlight.common.client.renderer.layer.ThirstWalkerEyesLayer;
import cn.leolezury.eternalstarlight.common.entity.living.monster.ThirstWalker;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;

public class ThirstWalkerRenderer<T extends ThirstWalker> extends ESMobRenderer<T, ThirstWalkerModel<T>> {
	private static final Identifier ENTITY_TEXTURE = EternalStarlight.id("textures/entity/thirst_walker.png");

	public ThirstWalkerRenderer(EntityRendererProvider.Context context) {
		super(context, new ThirstWalkerModel<>(context.bakeLayer(ThirstWalkerModel.LAYER_LOCATION)), 0.3f);
		this.addLayer(new ThirstWalkerEyesLayer<>(this));
	}

	@Override
	public Identifier getTextureLocation(T entity) {
		return ENTITY_TEXTURE;
	}
}
