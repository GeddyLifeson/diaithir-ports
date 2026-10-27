package cn.leolezury.eternalstarlight.common.client.renderer.entity;

import cn.leolezury.eternalstarlight.common.client.compat.ESMobRenderer;
import cn.leolezury.eternalstarlight.common.EternalStarlight;
import cn.leolezury.eternalstarlight.common.client.model.entity.GleechModel;
import cn.leolezury.eternalstarlight.common.entity.living.monster.Gleech;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;

public class GleechRenderer<T extends Gleech> extends ESMobRenderer<T, GleechModel<T>> {
	private static final Identifier ENTITY_TEXTURE = EternalStarlight.id("textures/entity/gleech.png");

	public GleechRenderer(EntityRendererProvider.Context context) {
		super(context, new GleechModel<>(context.bakeLayer(GleechModel.LAYER_LOCATION)), 0.1f);
	}

	@Override
	public Identifier getTextureLocation(T entity) {
		return ENTITY_TEXTURE;
	}
}
