package com.gregtechceu.gtceu.client.renderer.entity;

import com.gregtechceu.gtceu.common.entity.GTExplosiveEntity;

import net.minecraft.client.renderer.MultiBufferSource;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.TntMinecartRenderer;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.InventoryMenu;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import org.jetbrains.annotations.NotNull;

@OnlyIn(Dist.CLIENT)
public class GTExplosiveRenderer<T extends GTExplosiveEntity>  {/* ported: removed */

    public GTExplosiveRenderer(EntityRendererProvider.Context context) {
    }

    
    
    public void render(@NotNull T entity, float entityYaw, float partialTicks, PoseStack poseStack,
                       @NotNull MultiBufferSource buffer, int packedLight) {  /* ported stub */ }

    @NotNull
    
    
    public Identifier getTextureLocation(@NotNull T entity) { return null; /* ported stub */ }
}
