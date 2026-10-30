package com.github.alexmodguy.alexscaves.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import com.github.alexthe666.citadel.client.render.legacy.LegacyEntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;

public class EmptyRenderer<T extends Entity> extends LegacyEntityRenderer<T> {

    public EmptyRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0F;
    }

    public void render(T entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource source, int i) {
        super.render(entity, entityYaw, partialTicks, poseStack, source, i);
    }

    public Identifier getTextureLocation(T entity) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}

