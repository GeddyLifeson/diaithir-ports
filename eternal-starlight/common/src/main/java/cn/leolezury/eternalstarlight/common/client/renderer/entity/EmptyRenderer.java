package cn.leolezury.eternalstarlight.common.client.renderer.entity;

import cn.leolezury.eternalstarlight.common.client.compat.ESEntityRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;

public class EmptyRenderer extends ESEntityRenderer<Entity> {
	public EmptyRenderer(EntityRendererProvider.Context context) {
		super(context);
	}

	@Override
	public Identifier getTextureLocation(Entity entity) {
		return TextureAtlas.LOCATION_BLOCKS;
	}

	@Override
	public void render(Entity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {

	}
}
