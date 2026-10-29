package com.gregtechceu.gtceu.client.renderer;

import com.gregtechceu.gtceu.api.blockentity.PipeBlockEntity;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.item.PipeBlockItem;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.api.item.tool.IToolGridHighlight;
import com.gregtechceu.gtceu.api.item.tool.ToolHelper;
import com.gregtechceu.gtceu.api.pattern.util.RelativeDirection;
import com.gregtechceu.gtceu.api.pipenet.IPipeType;
import com.gregtechceu.gtceu.client.util.RenderUtil;
import com.gregtechceu.gtceu.common.data.item.GTItemAbilities;
import com.gregtechceu.gtceu.common.item.behavior.CoverPlaceBehavior;
import com.gregtechceu.gtceu.common.item.tool.rotation.CustomBlockRotations;

import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionfc;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.Set;
import java.util.function.Function;

import static com.gregtechceu.gtceu.utils.GTMatrixUtils.*;

@OnlyIn(Dist.CLIENT)
public class BlockHighlightRenderer {

    public static void renderBlockHighlight(PoseStack poseStack, Camera camera, BlockHitResult target,
                                            MultiBufferSource multiBufferSource, float partialTick) {  /* ported stub */ }

    private static float rColour;
    private static float gColour;
    private static float bColour;

    private static void drawGridOverlays(PoseStack poseStack, MultiBufferSource bufferSource, Vec3 cameraPos,
                                         BlockHitResult blockHitResult, Function<Direction, ResourceTexture> texture) {  /* ported stub */ }

    private static void drawLine(PoseStack.Pose pose, VertexConsumer buffer, Vector3fc from, Vector3fc to) {
        Vector3f normal = from.sub(to, new Vector3f());

        buffer.addVertex(pose.pose(), from.x(), from.y(), from.z())
                .setColor(rColour, gColour, bColour, 1f)
                .setNormal(normal.x(), normal.y(), normal.z());
        buffer.addVertex(pose.pose(), to.x(), to.y(), to.z())
                .setColor(rColour, gColour, bColour, 1f)
                .setNormal(normal.x(), normal.y(), normal.z());
    }

    private static void drawResourceTexture(PoseStack poseStack, MultiBufferSource bufferSource,
                                            ResourceTexture texture, int color,
                                            float x, float y, float w, float h) {  /* ported stub */ }
}
