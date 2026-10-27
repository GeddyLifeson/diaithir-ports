package cn.leolezury.eternalstarlight.common.client.renderer.entity;

import cn.leolezury.eternalstarlight.common.EternalStarlight;
import cn.leolezury.eternalstarlight.common.entity.projectile.ThioquartzArrow;
import cn.leolezury.eternalstarlight.common.client.compat.ESArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;

public class ThioquartzArrowRenderer extends ESArrowRenderer<ThioquartzArrow> {
	public static final Identifier ARROW_LOCATION = EternalStarlight.id("textures/entity/thioquartz_arrow.png");

	public ThioquartzArrowRenderer(EntityRendererProvider.Context context) {
		super(context);
	}

	@Override
	public Identifier getTextureLocation(ThioquartzArrow arrow) {
		return ARROW_LOCATION;
	}
}
