package cn.leolezury.eternalstarlight.common.client.renderer.layer;

import net.minecraft.client.renderer.rendertype.RenderTypes;
import cn.leolezury.eternalstarlight.common.client.compat.ESRenderState;
import cn.leolezury.eternalstarlight.common.EternalStarlight;
import cn.leolezury.eternalstarlight.common.client.model.entity.NightfallSpiderModel;
import cn.leolezury.eternalstarlight.common.entity.living.monster.NightfallSpider;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EyesLayer;

public class NightfallSpiderEyesLayer<T extends NightfallSpider, M extends NightfallSpiderModel<T>> extends EyesLayer<ESRenderState, M> {
	private static final RenderType SPIDER_EYES = RenderTypes.eyes(EternalStarlight.id("textures/entity/nightfall_spider_eyes.png"));

	public NightfallSpiderEyesLayer(RenderLayerParent<ESRenderState, M> parent) {
		super(parent);
	}

	public RenderType renderType() {
		return SPIDER_EYES;
	}
}
