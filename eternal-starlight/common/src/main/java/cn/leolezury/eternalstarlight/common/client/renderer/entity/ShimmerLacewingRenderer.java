package cn.leolezury.eternalstarlight.common.client.renderer.entity;

import cn.leolezury.eternalstarlight.common.client.compat.ESMobRenderer;
import cn.leolezury.eternalstarlight.common.client.model.entity.ShimmerLacewingModel;
import cn.leolezury.eternalstarlight.common.client.renderer.layer.ShimmerLacewingGlowLayer;
import cn.leolezury.eternalstarlight.common.entity.living.animal.ShimmerLacewing;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;

public class ShimmerLacewingRenderer<T extends ShimmerLacewing> extends ESMobRenderer<T, ShimmerLacewingModel<T>> {
	public ShimmerLacewingRenderer(EntityRendererProvider.Context context) {
		super(context, new ShimmerLacewingModel<>(context.bakeLayer(ShimmerLacewingModel.LAYER_LOCATION)), 0.3f);
		this.addLayer(new ShimmerLacewingGlowLayer<>(this, context.getModelSet()));
	}

	@Override
	public Identifier getTextureLocation(T entity) {
		return entity.getVariant().value().textureFull();
	}
}
