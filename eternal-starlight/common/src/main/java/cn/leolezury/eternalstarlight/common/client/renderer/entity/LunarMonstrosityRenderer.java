package cn.leolezury.eternalstarlight.common.client.renderer.entity;

import cn.leolezury.eternalstarlight.common.client.compat.ESMobRenderer;
import cn.leolezury.eternalstarlight.common.EternalStarlight;
import cn.leolezury.eternalstarlight.common.client.model.entity.LunarMonstrosityModel;
import cn.leolezury.eternalstarlight.common.entity.living.boss.monstrosity.LunarMonstrosity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;

public class LunarMonstrosityRenderer<T extends LunarMonstrosity> extends ESMobRenderer<T, LunarMonstrosityModel<T>> {
	private static final Identifier ENTITY_TEXTURE = EternalStarlight.id("textures/entity/lunar_monstrosity.png");

	public LunarMonstrosityRenderer(EntityRendererProvider.Context context) {
		super(context, new LunarMonstrosityModel<>(context.bakeLayer(LunarMonstrosityModel.LAYER_LOCATION)), 0.5f);
	}

	@Override
	protected float getFlipDegrees(T livingEntity) {
		return 0;
	}

	@Override
	public Identifier getTextureLocation(T entity) {
		return ENTITY_TEXTURE;
	}
}
