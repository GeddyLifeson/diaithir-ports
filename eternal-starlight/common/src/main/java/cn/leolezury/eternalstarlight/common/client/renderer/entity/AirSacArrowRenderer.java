package cn.leolezury.eternalstarlight.common.client.renderer.entity;

import cn.leolezury.eternalstarlight.common.EternalStarlight;
import cn.leolezury.eternalstarlight.common.entity.projectile.AirSacArrow;
import cn.leolezury.eternalstarlight.common.client.compat.ESArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;

public class AirSacArrowRenderer extends ESArrowRenderer<AirSacArrow> {
	public static final Identifier ARROW_LOCATION = EternalStarlight.id("textures/entity/air_sac_arrow.png");

	public AirSacArrowRenderer(EntityRendererProvider.Context context) {
		super(context);
	}

	@Override
	public Identifier getTextureLocation(AirSacArrow arrow) {
		return ARROW_LOCATION;
	}
}
