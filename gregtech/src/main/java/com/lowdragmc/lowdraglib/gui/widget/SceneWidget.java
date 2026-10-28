package com.lowdragmc.lowdraglib.gui.widget;

import com.lowdragmc.lowdraglib.LDLib;
import com.lowdragmc.lowdraglib.client.scene.*;
import com.lowdragmc.lowdraglib.client.utils.RenderUtils;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib.gui.util.DrawerHelper;

import com.lowdragmc.lowdraglib.utils.BlockPosFace;
import com.lowdragmc.lowdraglib.utils.TrackedDummyWorld;
import com.lowdragmc.lowdraglib.utils.interpolate.Eases;
import com.lowdragmc.lowdraglib.utils.interpolate.Interpolator;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;

import lombok.Getter;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import com.lowdragmc.lowdraglib.gui.compat.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

@SuppressWarnings({"unused", "UnusedReturnValue", "SameParameterValue"})
public class SceneWidget extends WidgetGroup {
    @OnlyIn(Dist.CLIENT)
    protected WorldSceneRenderer renderer;
    @OnlyIn(Dist.CLIENT)
    protected TrackedDummyWorld dummyWorld;
    @Getter
    protected boolean dragging;
    @Getter
    protected boolean renderFacing = true;
    @Getter
    protected boolean renderSelect = true;
    @Getter
    protected boolean draggable = true;
    @Getter
    protected boolean scalable = true;
    @Getter
    protected boolean intractable = true;
    @Getter
    protected boolean hoverTips;
    @Getter
    protected int currentMouseX;
    @Getter
    protected int currentMouseY;
    @Getter
    protected Vector3f center;
    @Getter
    protected float rotationYaw = 25;
    @Getter
    protected float rotationPitch = -135;
    @Getter
    protected float zoom = 5;
    @Getter
    protected float range;
    @Getter
    protected BlockPosFace clickPosFace;
    @Getter
    protected BlockPosFace hoverPosFace;
    @Getter
    protected BlockPosFace selectedPosFace;
    @Getter
    protected ItemStack hoverItem;
    @Getter
    protected BiConsumer<BlockPos, Direction> onSelected;
    @Getter
    protected Set<BlockPos> core;
    @Getter
    protected boolean useCache;
    @Getter
    protected boolean useOrtho = false;
    @Getter
    protected boolean autoReleased;
    protected BiConsumer<SceneWidget, List<Component>> onAddedTooltips;
    protected Consumer<SceneWidget> beforeWorldRender;
    protected Consumer<SceneWidget> afterWorldRender;

    public SceneWidget(int x, int y, int width, int height, Level world, boolean useFBO) {
        super(x, y, width, height);
        if (isRemote()) {
            createScene(world, useFBO);
        }
    }

    public SceneWidget(int x, int y, int width, int height, Level world) {
        super(x, y, width, height);
        if (isRemote()) {
            createScene(world);
        }
    }

    public SceneWidget setOnAddedTooltips(BiConsumer<SceneWidget, List<Component>> onAddedTooltips) {
        this.onAddedTooltips = onAddedTooltips;
        return this;
    }

    public SceneWidget useCacheBuffer() {
        return useCacheBuffer(true);
    }

    public SceneWidget useCacheBuffer(boolean autoReleased) {
        useCache = true;
        this.autoReleased = autoReleased;
        if (isRemote() && renderer != null) {
            renderer.useCacheBuffer(true);
        }
        return this;
    }

    public SceneWidget useOrtho() {
        return useOrtho(true);
    }

    public SceneWidget useOrtho(boolean useOrtho) {
        this.useOrtho = useOrtho;
        if (isRemote() && renderer != null) {
            renderer.useOrtho(useOrtho);
        }
        return this;
    }

    public SceneWidget setBeforeWorldRender(Consumer<SceneWidget> beforeWorldRender) {
        this.beforeWorldRender = beforeWorldRender;
        if (this.beforeWorldRender != null && isRemote() && renderer != null) {
            renderer.setBeforeWorldRender(s -> beforeWorldRender.accept(this));
        }
        return this;
    }

    public SceneWidget setAfterWorldRender(Consumer<SceneWidget> afterWorldRender) {
        this.afterWorldRender = afterWorldRender;
        return this;
    }

    public float camZoom() {
        if (useOrtho) {
            return 0.1f;
        } else {
            return zoom;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public ParticleManager getParticleManager() {
        if (renderer == null) return null;
        return renderer.getParticleManager();
    }

    @Override
    public void setGui(ModularUI gui) {  /* ported stub */ }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void updateScreen() {
        super.updateScreen();
        ParticleManager particleManager = getParticleManager();
        if (particleManager != null) {
            particleManager.tick();
        }
    }

    public void releaseCacheBuffer() {
        if (isRemote() && renderer != null && autoReleased) {
            renderer.deleteCacheBuffer();
        }
    }

    public void needCompileCache() {
        if (isRemote() && renderer != null) {
            renderer.needCompileCache();
        }
    }

    @OnlyIn(Dist.CLIENT)
    public final void createScene(Level world) {
        createScene(world, false);
    }

    @OnlyIn(Dist.CLIENT)
    protected ParticleManager createParticleManager() {
        return new ParticleManager();
    }

    @OnlyIn(Dist.CLIENT)
    public final void createScene(Level world, boolean useFBOSceneRenderer) {  /* ported stub */ }

    @OnlyIn(Dist.CLIENT)
    public WorldSceneRenderer getRenderer() {
        return renderer;
    }

    @OnlyIn(Dist.CLIENT)
    public TrackedDummyWorld getDummyWorld() {
        return dummyWorld;
    }

    public SceneWidget setOnSelected(BiConsumer<BlockPos, Direction> onSelected) {
        this.onSelected = onSelected;
        return this;
    }

    public SceneWidget setClearColor(int color) {
        if (isRemote()) {
            renderer.setClearColor(color);
        }
        return this;
    }


    public SceneWidget setRenderSelect(boolean renderSelect) {
        this.renderSelect = renderSelect;
        return this;
    }

    public SceneWidget setRenderFacing(boolean renderFacing) {
        this.renderFacing = renderFacing;
        return this;
    }

    public SceneWidget setDraggable(boolean draggable) {
        this.draggable = draggable;
        return this;
    }

    public SceneWidget setScalable(boolean scalable) {
        this.scalable = scalable;
        return this;
    }

    public SceneWidget setIntractable(boolean intractable) {
        this.intractable = intractable;
        return this;
    }

    public SceneWidget setHoverTips(boolean hoverTips) {
        this.hoverTips = hoverTips;
        return this;
    }

    public SceneWidget setRenderedCore(Collection<BlockPos> blocks) {
        return setRenderedCore(blocks, null);
    }

    public SceneWidget setRenderedCore(Collection<BlockPos> blocks, ISceneBlockRenderHook renderHook) {
        if (isRemote()) {
            core.clear();
            core.addAll(blocks);
            int minX = Integer.MAX_VALUE;
            int minY = Integer.MAX_VALUE;
            int minZ = Integer.MAX_VALUE;
            int maxX = Integer.MIN_VALUE;
            int maxY = Integer.MIN_VALUE;
            int maxZ = Integer.MIN_VALUE;
            for (BlockPos vPos : blocks) {
                minX = Math.min(minX, vPos.getX());
                minY = Math.min(minY, vPos.getY());
                minZ = Math.min(minZ, vPos.getZ());
                maxX = Math.max(maxX, vPos.getX());
                maxY = Math.max(maxY, vPos.getY());
                maxZ = Math.max(maxZ, vPos.getZ());
            }
            center = new Vector3f((minX + maxX) / 2f + 0.5F, (minY + maxY) / 2f + 0.5F, (minZ + maxZ) / 2f + 0.5F);
            renderer.addRenderedBlocks(core, renderHook);
            this.zoom = (float) (3.5 * Math.sqrt(Math.max(Math.max(Math.max(maxX - minX + 1, maxY - minY + 1), maxZ - minZ + 1), 1)));
            renderer.setCameraOrtho(range * zoom, range * zoom, range * zoom);
            renderer.setCameraLookAt(center, camZoom(), Math.toRadians(rotationPitch), Math.toRadians(rotationYaw));
            needCompileCache();
        }
        return this;
    }

    private List<Component> getToolTips(List<Component> list) {
        if (this.onAddedTooltips != null) {
            this.onAddedTooltips.accept(this, list);
        }
        return list;
    }

    @OnlyIn(Dist.CLIENT)
    protected void renderBeforeBatchEnd(MultiBufferSource bufferSource, float partialTicks) {
    }

    @OnlyIn(Dist.CLIENT)
    public void renderBlockOverLay(WorldSceneRenderer renderer) {  /* ported stub */ }

    @OnlyIn(Dist.CLIENT)
    public void drawFacingBorder(PoseStack poseStack, BlockPosFace posFace, int color) {
        drawFacingBorder(poseStack, posFace, color, 0);
    }

    @OnlyIn(Dist.CLIENT)
    public void drawFacingBorder(PoseStack poseStack, BlockPosFace posFace, int color, int inner) {  /* ported stub */ }

    @OnlyIn(Dist.CLIENT)
    private static void drawBorder(PoseStack poseStack, int x, int y, int width, int height, int color, int border) {
        drawSolidRect(poseStack,x - border, y - border, width + 2 * border, border, color);
        drawSolidRect(poseStack,x - border, y + height, width + 2 * border, border, color);
        drawSolidRect(poseStack,x - border, y, border, height, color);
        drawSolidRect(poseStack,x + width, y, border, height, color);
    }

    @OnlyIn(Dist.CLIENT)
    private static void drawSolidRect(PoseStack poseStack, int x, int y, int width, int height, int color) {  /* ported stub */ }

    @OnlyIn(Dist.CLIENT)
    private static void fill(PoseStack matrices, int x1, int y1, int x2, int y2, int z, int color) {  /* ported stub */ }

    @Override
    public Object getXEIIngredientOverMouse(double mouseX, double mouseY) { return null; /* ported stub */ }

    @Override
    public void handleClientAction(int id, RegistryFriendlyByteBuf buffer) {
        if (id == -1) {
            selectedPosFace = new BlockPosFace(buffer.readBlockPos(), buffer.readEnum(Direction.class));
            if (onSelected != null) {
                onSelected.accept(selectedPosFace.pos(), selectedPosFace.facing());
            }
        } else {
            super.handleClientAction(id, buffer);
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        if (!intractable) return false;
        if (isMouseOverElement(mouseX, mouseY)) {
            if (draggable) {
                dragging = true;
            }
            clickPosFace = hoverPosFace;
            return true;
        }
        dragging = false;
        return false;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean mouseWheelMove(double mouseX, double mouseY, double scrollX, double scrollY) {
        var result = super.mouseWheelMove(mouseX, mouseY, scrollX, scrollY);
        if (!result && isMouseOverElement(mouseX, mouseY) && scalable) {
            zoom = (float) Mth.clamp(zoom + (scrollY < 0 ? 0.5 : -0.5), 0.1, 999);
            if (renderer != null) {
                renderer.setCameraOrtho(range * zoom, range * zoom, range * zoom);
                renderer.setCameraLookAt(center, camZoom(), Math.toRadians(rotationPitch), Math.toRadians(rotationYaw));
            }
            return true;
        }
        return result;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (!intractable) return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
        if (dragging) {
            rotationPitch += dragX + 360;
            rotationPitch = rotationPitch % 360;
            rotationYaw = (float) Mth.clamp(rotationYaw + dragY, -89.9, 89.9);
            if (renderer != null) {
                renderer.setCameraLookAt(center, camZoom(), Math.toRadians(rotationPitch), Math.toRadians(rotationYaw));
            }
            return false;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (!intractable) return super.mouseReleased(mouseX, mouseY, button);
        dragging = false;
        if (hoverPosFace != null && hoverPosFace.equals(clickPosFace)) {
            selectedPosFace = hoverPosFace;
            writeClientAction(-1, buffer -> {
                buffer.writeBlockPos(selectedPosFace.pos());
                buffer.writeEnum(selectedPosFace.facing());
            });
            if (onSelected != null) {
                onSelected.accept(selectedPosFace.pos(), selectedPosFace.facing());
            }
            clickPosFace = null;
            return true;
        }
        clickPosFace = null;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void drawInForeground(@NotNull @Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        if (hoverTips && isMouseOverElement(mouseX, mouseY)) {
            if (hoverItem != null && !hoverItem.isEmpty()) {
                gui.getModularUIGui().setHoverTooltip(getToolTips(DrawerHelper.getItemToolTip(hoverItem)), hoverItem, null, hoverItem.getTooltipImage().orElse(null));
            }
        }
        super.drawInForeground(graphics, mouseX, mouseY, partialTicks);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void drawInBackground(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {  /* ported stub */ }

    public SceneWidget setCenter(Vector3f center) {
        this.center = center;
        if (renderer != null) {
            renderer.setCameraLookAt(this.center, camZoom(), Math.toRadians(rotationPitch), Math.toRadians(rotationYaw));
        }
        return this;
    }

    public SceneWidget setZoom(float zoom) {
        this.zoom = zoom;
        if (renderer != null) {
            renderer.setCameraOrtho(range * zoom, range * zoom, range * zoom);
            renderer.setCameraLookAt(this.center, camZoom(), Math.toRadians(rotationPitch), Math.toRadians(rotationYaw));
        }
        return this;
    }

    public SceneWidget setOrthoRange(float range) {
        this.range = range;
        if (renderer != null) {
            renderer.setCameraOrtho(range * zoom, range * zoom, range * zoom);
        }
        return this;
    }

    public SceneWidget setCameraYawAndPitch(float rotationYaw, float rotationPitch) {
        this.rotationYaw = rotationYaw;
        this.rotationPitch = rotationPitch;
        if (renderer != null) {
            renderer.setCameraLookAt(this.center, camZoom(), Math.toRadians(rotationPitch), Math.toRadians(rotationYaw));
        }
        return this;
    }

    protected Interpolator interpolator;
    protected long startTick;

    public void setCameraYawAndPitchAnima(float rotationYaw, float rotationPitch, int dur) {  /* ported stub */ }

}
