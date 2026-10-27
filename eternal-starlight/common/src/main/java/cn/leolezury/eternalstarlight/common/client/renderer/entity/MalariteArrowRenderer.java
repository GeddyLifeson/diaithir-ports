package cn.leolezury.eternalstarlight.common.client.renderer.entity;

import cn.leolezury.eternalstarlight.common.EternalStarlight;
import cn.leolezury.eternalstarlight.common.entity.projectile.MalariteArrow;
import cn.leolezury.eternalstarlight.common.client.compat.ESArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;

public class MalariteArrowRenderer extends ESArrowRenderer<MalariteArrow> {
	public static final Identifier ARROW_LOCATION = EternalStarlight.id("textures/entity/malarite_arrow.png");

	public MalariteArrowRenderer(EntityRendererProvider.Context context) {
		super(context);
	}

	@Override
	public Identifier getTextureLocation(MalariteArrow arrow) {
		return ARROW_LOCATION;
	}
}
