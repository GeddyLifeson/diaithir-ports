package cn.leolezury.eternalstarlight.common.client.renderer.layer;

import net.minecraft.client.renderer.rendertype.RenderTypes;
import cn.leolezury.eternalstarlight.common.client.compat.ESRenderState;
import cn.leolezury.eternalstarlight.common.EternalStarlight;
import cn.leolezury.eternalstarlight.common.client.handler.ESClientHandler;
import cn.leolezury.eternalstarlight.common.client.model.entity.StarlightGolemModel;
import cn.leolezury.eternalstarlight.common.entity.living.boss.golem.StarlightGolem;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EyesLayer;

public class StarlightGolemEyesLayer<T extends StarlightGolem, M extends StarlightGolemModel<T>> extends EyesLayer<ESRenderState, M> {
	private static final RenderType EYES = RenderTypes.eyes(EternalStarlight.id("textures/entity/starlight_golem/starlight_golem_eyes.png"));
	private static final RenderType EYES_HALLOWEEN = RenderTypes.eyes(EternalStarlight.id("textures/entity/starlight_golem/starlight_golem_eyes_halloween.png"));

	public StarlightGolemEyesLayer(RenderLayerParent<ESRenderState, M> parent) {
		super(parent);
	}

	@Override
	public RenderType renderType() {
		return ESClientHandler.isHalloween ? EYES_HALLOWEEN : EYES;
	}
}
