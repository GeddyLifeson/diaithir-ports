package cn.leolezury.eternalstarlight.common.client.renderer.entity;

import cn.leolezury.eternalstarlight.common.EternalStarlight;
import cn.leolezury.eternalstarlight.common.entity.projectile.VoraciousArrow;
import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;

public class VoraciousArrowRenderer extends ArrowRenderer<VoraciousArrow> {
	public static final Identifier ARROW_LOCATION = EternalStarlight.id("textures/entity/voracious_arrow.png");

	public VoraciousArrowRenderer(EntityRendererProvider.Context context) {
		super(context);
	}

	@Override
	public Identifier getTextureLocation(VoraciousArrow arrow) {
		return ARROW_LOCATION;
	}
}
