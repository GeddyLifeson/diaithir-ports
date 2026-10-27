package cn.leolezury.eternalstarlight.common.client.renderer.layer;

import net.minecraft.client.renderer.rendertype.RenderTypes;
import cn.leolezury.eternalstarlight.common.client.compat.ESRenderState;
import cn.leolezury.eternalstarlight.common.EternalStarlight;
import cn.leolezury.eternalstarlight.common.client.model.entity.TwilightGazeModel;
import cn.leolezury.eternalstarlight.common.entity.living.animal.TwilightGaze;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EyesLayer;

public class TwilightGazeEyesLayer<T extends TwilightGaze, M extends TwilightGazeModel<T>> extends EyesLayer<ESRenderState, M> {
	private static final RenderType EYES = RenderTypes.eyes(EternalStarlight.id("textures/entity/twilight_gaze_eyes.png"));

	public TwilightGazeEyesLayer(RenderLayerParent<ESRenderState, M> parent) {
		super(parent);
	}

	@Override
	public RenderType renderType() {
		return EYES;
	}
}
