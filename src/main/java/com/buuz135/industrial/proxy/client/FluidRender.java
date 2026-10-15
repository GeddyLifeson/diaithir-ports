package com.buuz135.industrial.proxy.client;
import com.buuz135.industrial.fluid.OreTitaniumFluidType;

import com.buuz135.industrial.utils.Reference;
import com.hrznstudio.titanium.fluid.ClientFluidTypeExtensions;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;

/** Kept apart from ModuleCore: Titanium's fluid render properties load client-only classes, which a dedicated server lacks. */
public final class FluidRender {
    private FluidRender() {}

    public static IClientFluidTypeExtensions of(String fluid) {
        return new ClientFluidTypeExtensions(still(fluid), flow(fluid));
    }

    /** Ore fluids are tinted by the ore they carry. */
    public static IClientFluidTypeExtensions ore(String textures) {
        return new OreTitaniumFluidType.Client(still(textures), flow(textures));
    }

    private static Identifier still(String textures) {
        return Identifier.fromNamespaceAndPath(Reference.MOD_ID, "block/fluids/" + textures + "_still");
    }

    private static Identifier flow(String textures) {
        return Identifier.fromNamespaceAndPath(Reference.MOD_ID, "block/fluids/" + textures + "_flow");
    }
}
