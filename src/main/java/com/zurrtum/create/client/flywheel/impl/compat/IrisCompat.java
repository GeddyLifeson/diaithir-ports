package com.zurrtum.create.client.flywheel.impl.compat;

/** Shader-pack mods are not supported by this build. */
public final class IrisCompat {
    public static final boolean ACTIVE = false;

    private IrisCompat() {
    }

    public static boolean isShaderPackInUse() {
        return false;
    }

    public static boolean isRenderingShadowPass() {
        return false;
    }
}
