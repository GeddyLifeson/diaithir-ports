package cn.leolezury.eternalstarlight.common.client.renderer.entity;

import cn.leolezury.eternalstarlight.common.client.compat.ESMobRenderer;
import cn.leolezury.eternalstarlight.common.EternalStarlight;
import cn.leolezury.eternalstarlight.common.client.model.entity.NightfallSpiderModel;
import cn.leolezury.eternalstarlight.common.client.renderer.layer.NightfallSpiderEyesLayer;
import cn.leolezury.eternalstarlight.common.entity.living.monster.NightfallSpider;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;

public class NightfallSpiderRenderer<T extends NightfallSpider> extends ESMobRenderer<T, NightfallSpiderModel<T>> {
	private static final Identifier NIGHTFALL_SPIDER_LOCATION = EternalStarlight.id("textures/entity/nightfall_spider.png");

	public NightfallSpiderRenderer(EntityRendererProvider.Context context) {
		this(context, NightfallSpiderModel.LAYER_LOCATION);
		this.shadowRadius *= 0.7F;
	}

	public NightfallSpiderRenderer(EntityRendererProvider.Context context, ModelLayerLocation location) {
		super(context, new NightfallSpiderModel<>(context.bakeLayer(location)), 0.8F);
		this.addLayer(new NightfallSpiderEyesLayer<>(this));
	}

	@Override
	public Identifier getTextureLocation(T entity) {
		return NIGHTFALL_SPIDER_LOCATION;
	}
}
