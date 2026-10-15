/*
 * This file ("RenderWorm.java") is part of the Actually Additions mod for Minecraft.
 * It is created and owned by Ellpeck and distributed
 * under the Actually Additions License to be found at
 * http://ellpeck.de/actaddlicense
 * View the source code at https://github.com/Ellpeck/ActuallyAdditions
 *
 * © 2015-2017 Ellpeck
 */

package de.ellpeck.actuallyadditions.mod.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import de.ellpeck.actuallyadditions.mod.items.ActuallyItems;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Util;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/** The worm is drawn as its item, circling; named "snail mail" it shows the snail variant. */
public class RenderWorm extends EntityRenderer<EntityWorm, RenderWorm.State> {
    private final ItemModelResolver itemModelResolver;
    private final ItemStack stack;
    private final ItemStack snailStack;

    public RenderWorm(EntityRendererProvider.Context context) {
        super(context);
        this.itemModelResolver = context.getItemModelResolver();
        this.stack = new ItemStack(ActuallyItems.WORM.get());
        this.snailStack = new ItemStack(ActuallyItems.WORM.get());
        this.snailStack.set(DataComponents.CUSTOM_NAME, Component.literal("Snail Mail"));
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(EntityWorm worm, State state, float partialTicks) {
        super.extractRenderState(worm, state, partialTicks);
        Component customName = worm.getCustomName();
        boolean isSnail = customName != null && customName.getString().equalsIgnoreCase("snail mail");
        this.itemModelResolver.updateForTopItem(state.item, isSnail ? this.snailStack : this.stack, ItemDisplayContext.FIXED, worm.level(), null, worm.getId());
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        poseStack.pushPose();
        poseStack.translate(0, 0.75F, 0);
        poseStack.mulPose(Axis.YP.rotationDegrees(-(float) (Util.getMillis() / 70D % 360)));
        poseStack.translate(0, 0, 0.4);
        state.item.submit(poseStack, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
        poseStack.popPose();
        super.submit(state, poseStack, collector, camera);
    }

    public static class State extends EntityRenderState {
        final ItemStackRenderState item = new ItemStackRenderState();
    }
}
