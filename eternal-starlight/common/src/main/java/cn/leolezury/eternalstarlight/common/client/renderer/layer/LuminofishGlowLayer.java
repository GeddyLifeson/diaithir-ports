package cn.leolezury.eternalstarlight.common.client.renderer.layer;

import net.minecraft.client.renderer.rendertype.RenderTypes;
import cn.leolezury.eternalstarlight.common.client.compat.ESRenderState;
import cn.leolezury.eternalstarlight.common.EternalStarlight;
import cn.leolezury.eternalstarlight.common.client.model.entity.LuminofishModel;
import cn.leolezury.eternalstarlight.common.entity.living.animal.Luminofish;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EyesLayer;

public class LuminofishGlowLayer<T extends Luminofish, M extends LuminofishModel<T>> extends EyesLayer<ESRenderState, M> {
	private static final RenderType GLOW = RenderTypes.eyes(EternalStarlight.id("textures/entity/luminofish_glow.png"));

	public LuminofishGlowLayer(RenderLayerParent<ESRenderState, M> parent) {
		super(parent);
	}

	@Override
	public RenderType renderType() {
		return GLOW;
	}
}
