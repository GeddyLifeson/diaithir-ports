package cn.leolezury.eternalstarlight.common.client.renderer.entity;

import cn.leolezury.eternalstarlight.common.EternalStarlight;
import cn.leolezury.eternalstarlight.common.entity.projectile.GlaciteArrow;
import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;

public class GlaciteArrowRenderer extends ArrowRenderer<GlaciteArrow> {
	public static final Identifier ARROW_LOCATION = EternalStarlight.id("textures/entity/glacite_arrow.png");

	public GlaciteArrowRenderer(EntityRendererProvider.Context context) {
		super(context);
	}

	@Override
	public Identifier getTextureLocation(GlaciteArrow arrow) {
		return ARROW_LOCATION;
	}
}
