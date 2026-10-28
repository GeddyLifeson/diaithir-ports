package com.lowdragmc.lowdraglib.gui.compass.component.animation;

import com.lowdragmc.lowdraglib.client.scene.ISceneBlockRenderHook;
import com.lowdragmc.lowdraglib.client.scene.ISceneEntityRenderHook;
import com.lowdragmc.lowdraglib.client.scene.WorldSceneRenderer;
import com.lowdragmc.lowdraglib.client.utils.RenderUtils;
import com.lowdragmc.lowdraglib.gui.animation.Transform;
import com.lowdragmc.lowdraglib.gui.compass.CompassManager;
import com.lowdragmc.lowdraglib.gui.compass.component.CompassComponent;
import com.lowdragmc.lowdraglib.gui.editor.ColorPattern;
import com.lowdragmc.lowdraglib.gui.editor.Icons;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.ItemStackTexture;
import com.lowdragmc.lowdraglib.gui.texture.ProgressTexture;
import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;
import com.lowdragmc.lowdraglib.gui.util.ClickData;
import com.lowdragmc.lowdraglib.gui.util.DrawerHelper;
import com.lowdragmc.lowdraglib.gui.widget.*;
import com.lowdragmc.lowdraglib.utils.*;
import com.lowdragmc.lowdraglib.utils.interpolate.Eases;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import lombok.Getter;
import lombok.val;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.minecraft.client.Minecraft;
import com.lowdragmc.lowdraglib.gui.compat.GuiGraphics;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.tuple.MutableTriple;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

import javax.annotation.Nullable;
import java.util.*;

/**
 * @author KilaBash
 * @date 2023/7/28
 * @implNote AnimationScene
 */
@OnlyIn(Dist.CLIENT)
public class CompassScene extends WidgetGroup implements ISceneBlockRenderHook, ISceneEntityRenderHook {
    @Getter
    protected final SceneWidget sceneWidget;
    @Getter
    protected final WidgetGroup headerGroup;
    @Getter
    protected final int minX, minY = 0, minZ, maxX, maxY = 0, maxZ;
    @Getter
    protected final List<AnimationFrame> frames;
    protected final boolean useScene, tickScene;
    protected TrackedDummyWorld world;
    //runtime
    private final Map<BlockPos, Tuple<BlockAnima, Integer>> addedBlocks = new HashMap<>();
    private final Map<BlockPos, Tuple<BlockAnima, Integer>> removedBlocks = new HashMap<>();
    private final Map<BlockPosFace, Integer> highlightBlocks = new HashMap<>();
    private final Map<Vector3f, MutableTriple<Tuple<XmlUtils.SizedIngredient, List<Component>>, Vec2, Integer>> tooltipBlocks = new HashMap<>();
    private final Map<Vector3f, Vec2> tooltipPos = new HashMap<>();
    private int currentFrame = -1;
    private int frameTick = 0;
    private boolean isPause;

    public CompassScene(int width, CompassComponent component) {
        super(0, 0, width, component.getHeight());
        this.frames = component.getFrames();
        this.useScene = component.isUseScene();
        this.tickScene = component.isTickScene();
        this.minX = -component.getRange();
        this.minZ = -component.getRange();
        this.maxX = component.getRange();
        this.maxZ = component.getRange();
        var height = component.getHeight();
        var headerHeight = 80;
        var sceneHeight = height - headerHeight;
        if (useScene) {
            int sw = (sceneHeight * 4);
            sceneWidget = new SceneWidget((width - sw) / 2, headerHeight, sw, sceneHeight, world = new TrackedDummyWorld());
            sceneWidget.setHoverTips(true)
                    .useOrtho(component.isOrtho())
                    .setOrthoRange(0.5f)
                    .setScalable(component.isScalable())
                    .setDraggable(false)
                    .setRenderFacing(false)
                    .setRenderSelect(false);

            sceneWidget.getRenderer().setFov(30);
            sceneWidget.setRenderedCore(List.of(BlockPos.ZERO), this);
            sceneWidget.getRenderer().setSceneEntityRenderHook(this);
            if (component.getZoom() > 0) {
                sceneWidget.setZoom(component.getZoom());
            } else {
                sceneWidget.setZoom((9 * Mth.sqrt(maxX - minX)));
            }

            sceneWidget.setBeforeWorldRender(this::renderBeforeWorld);
            sceneWidget.setAfterWorldRender(this::renderAfterWorld);
            sceneWidget.setCameraYawAndPitch(component.getYaw(), sceneWidget.getRotationPitch());
            addWidget(sceneWidget);
        } else {
            sceneWidget = null;
        }
        addWidget(headerGroup = new WidgetGroup(0, 0, width, useScene ? headerHeight : (height - 25)));
        addWidget(new ButtonWidget((width - 12) / 2 + 20, height - 20, 12, 12, Icons.REPLAY, this::replay).setHoverTexture(Icons.REPLAY.copy().setColor(ColorPattern.GREEN.color)));
        addWidget(new ButtonWidget((width - 12) / 2, height - 20, 12, 12, Icons.ROTATION, this::rotation).setHoverTexture(Icons.ROTATION.copy().setColor(ColorPattern.GREEN.color)));
        addWidget(new ButtonWidget((width - 12) / 2 + 40, height - 20, 12, 12, Icons.borderText(0, "+", -1), cd -> zoom(-1)).setHoverTexture(Icons.borderText(0, "+", ColorPattern.GREEN.color)));
        addWidget(new ButtonWidget((width - 12) / 2 - 40, height - 20, 12, 12, Icons.borderText(0, "-", -1), cd -> zoom(1)).setHoverTexture(Icons.borderText(0, "-", ColorPattern.GREEN.color)));
        addWidget(new SwitchWidget((width - 12) / 2 - 20, height - 20, 12, 12, this::playPause)
                .setSupplier(() -> isPause)
                .setTexture(Icons.PLAY_PAUSE, Icons.PLAY_PAUSE.copy().setColor(ColorPattern.GREEN.color))
                .setClientSideWidget());
        if (!frames.isEmpty()) {
            int progressWidth = (width - 2) / frames.size();
            for (int i = 0; i < frames.size(); i++) {
                val frame = frames.get(i);
                val frameIndex = i;
                addWidget(new ProgressWidget(() -> {
                    if (currentFrame < 0) return 0;
                    if (frameIndex < currentFrame) return 1;
                    if (frameTick > 0 && frameIndex == currentFrame) {
                        var duration = frame.getDuration();
                        return (frameTick + (isPause ? 0 : Minecraft.getInstance().getTimer().getGameTimeDeltaTicks())) / Math.max(duration, 1);
                    }
                    return 0;
                }, progressWidth * i + 1 + 1, height - 6 + 1, progressWidth - 4, 4, new ProgressTexture(IGuiTexture.EMPTY, ColorPattern.WHITE.rectTexture())));
                addWidget(new ButtonWidget(progressWidth * i + 1, height - 6, progressWidth - 2, 6, ColorPattern.WHITE.borderTexture(1), cd -> jumpFrame(frame))
                        .setHoverTooltips(frame.tooltips())
                        .setHoverTexture(ColorPattern.GREEN.borderTexture(1)));
            }
        }
    }

    private void zoom(int value) {  /* ported stub */ }

    private void renderBeforeWorld(SceneWidget sceneWidget) {  /* ported stub */ }

    private void renderAfterWorld(SceneWidget sceneWidget) {  /* ported stub */ }

    @Override
    public void drawInForeground(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {  /* ported stub */ }

    private void playPause(ClickData clickData, boolean isPressed) {
        isPause = isPressed;
    }

    private void rotation(ClickData clickData) {  /* ported stub */ }

    private void resetScene() {  /* ported stub */ }

    private void jumpFrame(AnimationFrame frame) {
        resetScene();
        currentFrame = 0;
        for (AnimationFrame animationFrame : frames) {
            if (animationFrame != frame) {
                animationFrame.performFrameResult(this);
                currentFrame++;
            } else {
                frameTick = 0;
                break;
            }
        }
    }

    private void replay(ClickData clickData) {
        resetScene();
        isPause = false;
    }

    @Override
    public void updateScreen() {  /* ported stub */ }

    protected void nextFrame() {  /* ported stub */ }

    @Override
    public void applyEntity(Level world, Entity entity, PoseStack poseStack, float partialTicks) {
        ISceneEntityRenderHook.super.applyEntity(world, entity, poseStack, partialTicks);
    }

    @Override
    public void applyBESR(Level world, BlockPos pos, BlockEntity blockEntity, PoseStack poseStack, float partialTicks) {
        if (isPause) partialTicks = 0;
        if (removedBlocks.containsKey(pos)) {
            var tuple = removedBlocks.get(pos);
            var anima = tuple.getA();
            var tick = 1 - ((tuple.getB() - partialTicks) / anima.duration());
            if (tick > 0) {
                poseStack.translate(tick * anima.offset().x, tick * anima.offset().y, tick * anima.offset().z);
            }
        } else if (addedBlocks.containsKey(pos)) {
            var tuple = addedBlocks.get(pos);
            var anima = tuple.getA();
            var tick = (tuple.getB() - partialTicks) / anima.duration();
            if (tick > 0) {
                tick = Eases.EaseQuadIn.getInterpolation(tick);
                poseStack.translate(tick * anima.offset().x, tick * anima.offset().y, tick * anima.offset().z);
            }
        }
    }

    @Override
    public void applyVertexConsumerWrapper(Level world, BlockPos pos, BlockState state, WorldSceneRenderer.VertexConsumerWrapper wrapperBuffer, RenderType layer, float partialTicks) {
        if (isPause) partialTicks = 0;
        if (removedBlocks.containsKey(pos)) {
            var tuple = removedBlocks.get(pos);
            var anima = tuple.getA();
            float tick = 1 - ((tuple.getB() - partialTicks) / anima.duration());
            if (tick > 0) {
                wrapperBuffer.addOffset(tick * anima.offset().x, tick * anima.offset().y, tick * anima.offset().z);
            }
        } else if (addedBlocks.containsKey(pos)) {
            var tuple = addedBlocks.get(pos);
            var anima = tuple.getA();
            var tick = (tuple.getB() - partialTicks) / anima.duration();
            if (tick > 0) {
                tick = Eases.EaseQuadIn.getInterpolation(tick);
                wrapperBuffer.addOffset(tick * anima.offset().x, tick * anima.offset().y, tick * anima.offset().z);
            }
        }
    }

    public void addBlock(BlockPos pos, BlockInfo blockInfo, @Nullable BlockAnima anima) {  /* ported stub */ }

    public void removeBlock(BlockPos pos, @Nullable BlockAnima anima) {  /* ported stub */ }

    public void highlightBlock(BlockPosFace key, int duration) {
        if (!useScene) return;
        highlightBlocks.put(key, duration);
    }

    public void addEntity(EntityInfo entityInfo, @Nullable Vector3f pos, boolean update) {  /* ported stub */ }

    public void removeEntity(EntityInfo entityInfo, boolean force) {  /* ported stub */ }


    public void addTooltip(Vector3f pos, Tuple<XmlUtils.SizedIngredient, List<Component>> tuple, Vec2 middle, Integer time) {
        tooltipBlocks.put(pos, MutableTriple.of(tuple, middle, time));
    }

    public void rotate(float rotation, boolean anima) {  /* ported stub */ }

    public void addInformation(Widget widget, boolean anima) {  /* ported stub */ }
}
