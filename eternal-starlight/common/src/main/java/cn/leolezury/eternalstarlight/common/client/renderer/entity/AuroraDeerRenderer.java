package cn.leolezury.eternalstarlight.common.client.renderer.entity;

import cn.leolezury.eternalstarlight.common.client.compat.ESMobRenderer;
import cn.leolezury.eternalstarlight.common.EternalStarlight;
import cn.leolezury.eternalstarlight.common.client.model.entity.AuroraDeerModel;
import cn.leolezury.eternalstarlight.common.client.renderer.layer.AuroraDeerSnowLayer;
import cn.leolezury.eternalstarlight.common.entity.living.animal.AuroraDeer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;

public class AuroraDeerRenderer<T extends AuroraDeer> extends ESMobRenderer<T, AuroraDeerModel<T>> {
	private static final Identifier ENTITY_TEXTURE = EternalStarlight.id("textures/entity/aurora_deer.png");

	public AuroraDeerRenderer(EntityRendererProvider.Context context) {
		super(context, new AuroraDeerModel<>(context.bakeLayer(AuroraDeerModel.LAYER_LOCATION)), 0.8f);
		this.addLayer(new AuroraDeerSnowLayer<>(this));
	}

	@Override
	public Identifier getTextureLocation(T entity) {
		return ENTITY_TEXTURE;
	}
}
