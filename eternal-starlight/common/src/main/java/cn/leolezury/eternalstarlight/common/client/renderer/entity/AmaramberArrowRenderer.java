package cn.leolezury.eternalstarlight.common.client.renderer.entity;

import cn.leolezury.eternalstarlight.common.EternalStarlight;
import cn.leolezury.eternalstarlight.common.entity.projectile.AmaramberArrow;
import cn.leolezury.eternalstarlight.common.client.compat.ESArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;

public class AmaramberArrowRenderer extends ESArrowRenderer<AmaramberArrow> {
	public static final Identifier ARROW_LOCATION = EternalStarlight.id("textures/entity/amaramber_arrow.png");

	public AmaramberArrowRenderer(EntityRendererProvider.Context context) {
		super(context);
	}

	@Override
	public Identifier getTextureLocation(AmaramberArrow arrow) {
		return ARROW_LOCATION;
	}
}
