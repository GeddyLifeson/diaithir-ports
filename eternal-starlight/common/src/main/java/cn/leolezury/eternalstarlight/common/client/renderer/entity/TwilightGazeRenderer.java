package cn.leolezury.eternalstarlight.common.client.renderer.entity;

import cn.leolezury.eternalstarlight.common.client.compat.ESMobRenderer;
import cn.leolezury.eternalstarlight.common.EternalStarlight;
import cn.leolezury.eternalstarlight.common.client.model.entity.TwilightGazeModel;
import cn.leolezury.eternalstarlight.common.client.renderer.layer.TwilightGazeEyesLayer;
import cn.leolezury.eternalstarlight.common.entity.living.animal.TwilightGaze;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;

public class TwilightGazeRenderer<T extends TwilightGaze> extends ESMobRenderer<T, TwilightGazeModel<T>> {
	private static final Identifier ENTITY_TEXTURE = EternalStarlight.id("textures/entity/twilight_gaze.png");

	public TwilightGazeRenderer(EntityRendererProvider.Context context) {
		super(context, new TwilightGazeModel<>(context.bakeLayer(TwilightGazeModel.LAYER_LOCATION)), 0.3f);
		this.addLayer(new TwilightGazeEyesLayer<>(this));
	}

	@Override
	public Identifier getTextureLocation(T entity) {
		return ENTITY_TEXTURE;
	}
}
