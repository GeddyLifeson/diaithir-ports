package com.lowdragmc.lowdraglib.client.bakedpipeline;

import net.minecraft.client.resources.model.cuboid.FaceBakery;
import com.mojang.math.Transformation;
import net.minecraft.client.renderer.FaceInfo;
import net.minecraft.client.resources.model.geometry.BakedQuad;



import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.block.dispatch.ModelState;
import net.minecraft.core.BlockMath;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.neoforged.neoforge.client.ClientHooks;
import net.neoforged.neoforge.client.model.ExtraFaceData;

import org.joml.*;

import java.lang.Math;



public class FaceQuadBakery {
    public static final int VERTEX_INT_SIZE = 8;
    private static final float RESCALE_22_5 = 1.0F / (float)Math.cos((float) (Math.PI / 8)) - 1.0F;
    private static final float RESCALE_45 = 1.0F / (float)Math.cos((float) (Math.PI / 4)) - 1.0F;
    public static final int VERTEX_COUNT = 4;
    private static final int COLOR_INDEX = 3;
    public static final int UV_INDEX = 4;

    public static BakedQuad bakeQuad(
        Vector3f posFrom,
        Vector3f posTo,
        BlockElementFace face,
        TextureAtlasSprite sprite,
        Direction facing,
        ModelState transform,
        @javax.annotation.Nullable BlockElementRotation partRotation,
        boolean shade,
        int emissivity
    ) { return null; /* ported stub */ }

    public static BlockFaceUV recomputeUVs(BlockFaceUV uv, Direction facing, Transformation modelRotation) { return null; /* ported stub */ }

    private static int[] makeVertices(
        BlockFaceUV uvs,
        TextureAtlasSprite sprite,
        Direction orientation,
        float[] posDiv16,
        Transformation rotation,
        @javax.annotation.Nullable BlockElementRotation partRotation,
        boolean shade
    ) { return null; /* ported stub */ }

    private static void bakeVertex(
        int[] vertexData,
        int vertexIndex,
        Direction facing,
        BlockFaceUV blockFaceUV,
        float[] posDiv16,
        TextureAtlasSprite sprite,
        Transformation rotation,
        @javax.annotation.Nullable BlockElementRotation partRotation,
        boolean shade
    ) {  /* ported stub */ }

    private static void fillVertex(int[] vertexData, int vertexIndex, Vector3f vector, TextureAtlasSprite sprite, BlockFaceUV blockFaceUV) {  /* ported stub */ }

    private static float[] setupShape(Vector3f min, Vector3f max) { return null; /* ported stub */ }

    private static void applyElementRotation(Vector3f vec, @javax.annotation.Nullable BlockElementRotation partRotation) {  /* ported stub */ }

    public static void applyModelRotation(Vector3f pos, Transformation transform) {  /* ported stub */ }

    private static void rotateVertexBy(Vector3f pos, Vector3f origin, Matrix4f transform, Vector3f scale) {
        Vector4f vector4f = transform.transform(new Vector4f(pos.x() - origin.x(), pos.y() - origin.y(), pos.z() - origin.z(), 1.0F));
        vector4f.mul(new Vector4f(scale, 1.0F));
        pos.set(vector4f.x() + origin.x(), vector4f.y() + origin.y(), vector4f.z() + origin.z());
    }

    private static void recalculateWinding(int[] vertices, Direction direction) {  /* ported stub */ }
}
