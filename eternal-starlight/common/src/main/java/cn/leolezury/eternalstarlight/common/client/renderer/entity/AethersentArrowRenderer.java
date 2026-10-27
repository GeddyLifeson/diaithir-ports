package cn.leolezury.eternalstarlight.common.client.renderer.entity;

import cn.leolezury.eternalstarlight.common.EternalStarlight;
import cn.leolezury.eternalstarlight.common.entity.projectile.AethersentArrow;
import cn.leolezury.eternalstarlight.common.client.compat.ESArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;

public class AethersentArrowRenderer extends ESArrowRenderer<AethersentArrow> {
	public static final Identifier ARROW_LOCATION = EternalStarlight.id("textures/entity/aethersent_arrow.png");

	public AethersentArrowRenderer(EntityRendererProvider.Context context) {
		super(context);
	}

	@Override
	public Identifier getTextureLocation(AethersentArrow arrow) {
		return ARROW_LOCATION;
	}
}
