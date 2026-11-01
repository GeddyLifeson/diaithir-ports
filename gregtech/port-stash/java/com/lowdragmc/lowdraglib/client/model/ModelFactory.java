package com.lowdragmc.lowdraglib.client.model;

import net.minecraft.client.renderer.block.dispatch.ModelState;
import net.minecraft.client.resources.model.sprite.Material;
import com.google.gson.JsonParseException;
import com.lowdragmc.lowdraglib.core.mixins.accessor.ModelBakeryAccessor;
import com.mojang.datafixers.util.Either;
import com.mojang.math.Transformation;
import lombok.Getter;
import lombok.Setter;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.resources.model.cuboid.ItemModelGenerator;
import net.minecraft.client.resources.model.cuboid.ItemTransform;
import net.minecraft.client.resources.model.cuboid.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.InventoryMenu;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Function;

/**
 * Author: KilaBash
 * Date: 2022/04/24
 * Description:
 */
@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
public class ModelFactory {
    public static final ItemModelGenerator ITEM_MODEL_GENERATOR = new ItemModelGenerator();

    public static ModelBakery getModelBakery() {
        return Minecraft.getInstance().getModelManager().getModelBakery();
    }

    public static UnbakedModel getLDLibModel(UnbakedModel vanilla) {
        return vanilla;
    }

    public static ModelBaker getModelBaker() { return null; /* ported stub */ }

    public static UnbakedModel getUnBakedModel(Identifier modelLocation) {
        return ((ModelBakeryAccessor)getModelBakery()).invokeGetModel(modelLocation);
    }

    public static UnbakedModel getTopLevelModel(ModelResourceLocation modelLocation) { return null; /* ported stub */ }

    public static Quaternionf getQuaternion(Direction facing) {
        return switch (facing) {
            case UP -> new Quaternionf().rotateXYZ(Mth.HALF_PI, 0, 0);
            case DOWN -> new Quaternionf().rotateXYZ(-Mth.HALF_PI, 0, 0);
            case EAST -> new Quaternionf().rotateXYZ(0, -Mth.HALF_PI, 0);
            case WEST -> new Quaternionf().rotateXYZ(0, Mth.HALF_PI, 0);
            case SOUTH -> new Quaternionf().rotateXYZ(0, Mth.PI, 0);
            case NORTH -> new Quaternionf();
        };
    }

    public static ModelState getRotation(Direction facing) { return null; /* ported stub */ }

    public static Direction modelFacing(Direction side, Direction frontFacing) {
        if (side == frontFacing) return Direction.NORTH;
        if (frontFacing == Direction.NORTH) return side;
        if (frontFacing == Direction.SOUTH) {
            if (side.getAxis() == Direction.Axis.Y) return side;
            return side.getOpposite();
        }
        if (frontFacing == Direction.EAST) {
            if (side.getAxis() == Direction.Axis.Y) return side;
            return side.getCounterClockWise();
        }
        if (frontFacing == Direction.WEST) {
            if (side.getAxis() == Direction.Axis.Y) return side;
            return side.getClockWise();
        }
        if (frontFacing == Direction.UP) {
            if (side == Direction.DOWN) return Direction.SOUTH;
            if (side.getAxis() == Direction.Axis.X) return side;
            if (side == Direction.SOUTH) return Direction.UP;
            if (side == Direction.NORTH) return Direction.DOWN;
        }
        if (frontFacing == Direction.DOWN) {
            if (side == Direction.UP) return Direction.SOUTH;
            if (side.getAxis() == Direction.Axis.X) return side;
            if (side == Direction.SOUTH) return Direction.DOWN;
            if (side == Direction.NORTH) return Direction.UP;
        }
        return side;
    }

    private record ModelStateWrapper(ModelState modelState, boolean lockedUV) implements ModelState {
        @Override
        @Nonnull
        public Transformation getRotation() { return null; /* ported stub */ }

        @Override
        public boolean isUvLocked() { return false; /* ported stub */ }
    }

    public static ModelState getRotation(Direction facing, boolean lockedUV) {
        if (lockedUV) {
            return new ModelStateWrapper(getRotation(facing), true);
        } else {
            return getRotation(facing);
        }
    }

    public static Either<Material, String> parseTextureLocationOrReference(Identifier pLocation, String pName) { return null; /* ported stub */ }

    public static Either<Material, String> parseBlockTextureLocationOrReference(String pName) { return null; /* ported stub */ }

    private static boolean isTextureReference(String pStr) {
        return pStr.charAt(0) == '#';
    }

    public static TextureAtlasSprite getBlockSprite(Identifier location) { return null; /* ported stub */ }

    public static TextureAtlasSprite getSprite(Identifier atlas, Identifier location) { return null; /* ported stub */ }

    public static ItemTransform makeTransform(float rotationX, float rotationY, float rotationZ, float translationX, float translationY, float translationZ, float scaleX, float scaleY, float scaleZ) {
        var translation = new Vector3f(translationX, translationY, translationZ);
        translation.mul(0.0625f);
        translation.set(Mth.clamp(translation.x, -5.0F, 5.0F), Mth.clamp(translation.y, -5.0F, 5.0F), Mth.clamp(translation.z, -5.0F, 5.0F));
        return new ItemTransform(new Vector3f(rotationX, rotationY, rotationZ), translation, new Vector3f(scaleX, scaleY, scaleZ));
    }

    public static final ItemTransform TRANSFORM_BLOCK_GUI = makeTransform(30, 225, 0, 0, 0, 0, 0.625f, 0.625f, 0.625f);
    public static final ItemTransform TRANSFORM_BLOCK_GROUND = makeTransform(0, 0, 0, 0, 3, 0, 0.25f, 0.25f, 0.25f);
    public static final ItemTransform TRANSFORM_BLOCK_FIXED = makeTransform(0, 0, 0, 0, 0, 0, 0.5f, 0.5f, 0.5f);
    public static final ItemTransform TRANSFORM_BLOCK_3RD_PERSON_RIGHT = makeTransform(75, 45, 0, 0, 2.5f, 0, 0.375f, 0.375f, 0.375f);
    public static final ItemTransform TRANSFORM_BLOCK_1ST_PERSON_RIGHT = makeTransform(0, 45, 0, 0, 0, 0, 0.4f, 0.4f, 0.4f);
    public static final ItemTransform TRANSFORM_BLOCK_1ST_PERSON_LEFT = makeTransform(0, 225, 0, 0, 0, 0, 0.4f, 0.4f, 0.4f);

    /**
     * Mimics the vanilla model transformation used for most vanilla blocks,
     * and should be suitable for most custom block-like models.
     */
    public static final ItemTransforms MODEL_TRANSFORM_BLOCK = new ItemTransforms(TRANSFORM_BLOCK_3RD_PERSON_RIGHT, TRANSFORM_BLOCK_3RD_PERSON_RIGHT, TRANSFORM_BLOCK_1ST_PERSON_LEFT, TRANSFORM_BLOCK_1ST_PERSON_RIGHT, ItemTransform.NO_TRANSFORM, TRANSFORM_BLOCK_GUI, TRANSFORM_BLOCK_GROUND, TRANSFORM_BLOCK_FIXED);
}
