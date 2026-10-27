package cn.leolezury.eternalstarlight.common.client.renderer.entity;

import cn.leolezury.eternalstarlight.common.client.compat.ESMobRenderer;
import cn.leolezury.eternalstarlight.common.EternalStarlight;
import cn.leolezury.eternalstarlight.common.client.model.entity.RookfishModel;
import cn.leolezury.eternalstarlight.common.entity.living.animal.Rookfish;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;

public class RookfishRenderer<T extends Rookfish> extends ESMobRenderer<T, RookfishModel<T>> {
	private static final Identifier ENTITY_TEXTURE = EternalStarlight.id("textures/entity/rookfish.png");

	public RookfishRenderer(EntityRendererProvider.Context context) {
		super(context, new RookfishModel<>(context.bakeLayer(RookfishModel.LAYER_LOCATION)), 0.2f);
	}

	@Override
	public Identifier getTextureLocation(T entity) {
		return ENTITY_TEXTURE;
	}
}
