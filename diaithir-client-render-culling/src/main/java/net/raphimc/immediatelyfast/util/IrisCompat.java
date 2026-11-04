package net.raphimc.immediatelyfast.util;

import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import java.util.function.BooleanSupplier;

/** Iris is not in the Diaithir pack; compat reduced to the constants ImmediatelyFast reads. */
public class IrisCompat {
    public static boolean IRIS_LOADED = false;
    public static BooleanSupplier isRenderingLevel;
    public static BooleanConsumer renderWithExtendedVertexFormat;
    public static ThreadLocal<Boolean> skipExtension;

    public static void init() {}
}
