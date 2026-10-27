package cn.leolezury.eternalstarlight.common.client.renderer.layer;

import net.minecraft.client.renderer.rendertype.RenderTypes;
import cn.leolezury.eternalstarlight.common.client.compat.ESRenderState;
import cn.leolezury.eternalstarlight.common.EternalStarlight;
import cn.leolezury.eternalstarlight.common.client.model.entity.LuminarisModel;
import cn.leolezury.eternalstarlight.common.entity.living.animal.Luminaris;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EyesLayer;

public class LuminarisGlowLayer<T extends Luminaris, M extends LuminarisModel<T>> extends EyesLayer<ESRenderState, M> {
	private static final RenderType GLOW = RenderTypes.eyes(EternalStarlight.id("textures/entity/luminaris_glow.png"));

	public LuminarisGlowLayer(RenderLayerParent<ESRenderState, M> parent) {
		super(parent);
	}

	@Override
	public RenderType renderType() {
		return GLOW;
	}
}
