package com.gregtechceu.gtceu.client.util;

import net.minecraft.world.entity.animal.equine.Variant;

import net.minecraft.client.renderer.block.dispatch.BlockModelRotation;
import net.minecraft.util.Mth;

import com.google.common.base.Preconditions;
import com.mojang.math.Transformation;
import org.joml.Quaternionf;

/**
 * This code is from
 * <a href=
 * "https://github.com/AppliedEnergistics/Applied-Energistics-2/blob/forge/1.20.1/src/main/java/appeng/hooks/BlockstateDefinitionHook.java">Applied
 * Energistics 2</a>,
 * licensed as LGPL 3.0.
 */
public class VariantRotationHelpers {

    private static final Transformation[] TRANSFORMS = createTransformations();

    private static Transformation[] createTransformations() { return null; /* ported stub */ }

    private VariantRotationHelpers() {}

    public static Variant rotateVariant(Variant variant, int xRot, int yRot, int zRot) { return null; /* ported stub */ }

    public static Transformation getRotationTransform(int xRot, int yRot, int zRot) {
        return TRANSFORMS[indexFromAngles(xRot, yRot, zRot)];
    }

    private static int indexFromAngles(int xRot, int yRot, int zRot) {
        Preconditions.checkArgument(xRot >= 0 && xRot < 360 && xRot % 90 == 0);
        Preconditions.checkArgument(yRot >= 0 && yRot < 360 && yRot % 90 == 0);
        Preconditions.checkArgument(zRot >= 0 && zRot < 360 && zRot % 90 == 0);
        return xRot / 90 * 16 + yRot / 90 * 4 + zRot / 90;
    }
}
