/*
 * This file is part of Industrial Foregoing.
 *
 * Copyright 2021, Buuz135
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in the
 * Software without restriction, including without limitation the rights to use, copy,
 * modify, merge, publish, distribute, sublicense, and/or sell copies of the Software,
 * and to permit persons to whom the Software is furnished to do so, subject to the
 * following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies
 * or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE
 * FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.buuz135.industrial.plugin.curios;

import com.buuz135.industrial.item.infinity.item.ItemInfinityBackpack;
import com.buuz135.industrial.module.ModuleTool;
import com.buuz135.industrial.plugin.CuriosPlugin;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.client.ICurioRenderer;
import top.theillusivec4.curios.api.type.capability.ICurio;

public class InfinityBackpackCurios implements ICurio, ICurioRenderer {

    @Override
    public boolean canEquip(SlotContext identifier) {
        return identifier.identifier().equals("back");
    }

    @Override
    public ItemStack getStack() {
        return new ItemStack(ModuleTool.INFINITY_BACKPACK.get());
    }

    @Override
    public void curioTick(SlotContext slotContext) {
        LivingEntity livingEntity = slotContext.entity();
        ItemStack stack = CuriosPlugin.getStack(livingEntity, "back", 0);
        if (stack.getItem() instanceof ItemInfinityBackpack) {
            if (livingEntity.level() instanceof ServerLevel serverLevel)
                ModuleTool.INFINITY_BACKPACK.get().inventoryTick(stack, serverLevel, livingEntity, EquipmentSlot.BODY);
        }
    }

    @Override
    public <S extends LivingEntityRenderState, M extends EntityModel<? super S>> void render(ItemStack stack, SlotContext slotContext, PoseStack matrixStack, SubmitNodeCollector collector, int light, S renderState, RenderLayerParent<S, M> renderLayerParent, EntityRendererProvider.Context context, float partialTicks, float ageInTicks) {
        LivingEntity entity = slotContext.entity();
        ItemStack itemStack = CuriosPlugin.getStack(entity, "back", 0);
        if (itemStack.getItem() instanceof ItemInfinityBackpack) {
            matrixStack.pushPose();
            if (entity.isCrouching()) {
                matrixStack.translate(0D, 0.2D, 0D);
                matrixStack.mulPose(Axis.XP.rotationDegrees((float) (90F / Math.PI)));
            }
            matrixStack.mulPose(Axis.XP.rotationDegrees(180));
            //matrixStack.translate(0,0.25,0.265);
            matrixStack.translate(0, -0.4, -0.2);
            matrixStack.scale(0.65f, 0.65f, 0.65f);

            ItemStackRenderState renderStack = new ItemStackRenderState();
            Minecraft.getInstance().getItemModelResolver().updateForTopItem(renderStack, itemStack, ItemDisplayContext.FIXED, entity.level(), entity, 0);
            renderStack.submit(matrixStack, collector, light, OverlayTexture.NO_OVERLAY, 0);
            matrixStack.popPose();
        }
    }
}
