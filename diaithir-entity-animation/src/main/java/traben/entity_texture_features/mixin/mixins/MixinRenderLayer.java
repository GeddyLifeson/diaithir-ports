package traben.entity_texture_features.mixin.mixins;

import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import traben.entity_texture_features.utils.ETFUtils2;


//#if MC >= 12111
import net.minecraft.client.renderer.rendertype.RenderTypes;
@Mixin(RenderTypes.class)
//#else
//$$ @Mixin(RenderType.class)
//#endif
public abstract class MixinRenderLayer {


    @SuppressWarnings("UnresolvedMixinReference")
    @ModifyVariable(
            method = {
                    "entitySolid",
                    "eyes",
                    "energySwirl",
                    "entityCutout",
                    "entityCutoutCull",
                    "entityTranslucent",
                    "entityTranslucentEmissive",
                    "armorCutoutNoCull",
                    "entityShadow",
                    "entityTranslucentCullItemTarget",
                    "entityCutoutZOffset",
                    "entityCutoutDissolve",
                    "entitySolidZOffsetForward",
            },
            at = @At(value = "HEAD"),
            index = 0, argsOnly = true)
    private static Identifier etf$mixinAllEntityLayers(Identifier value) {
        return ETFUtils2.getETFVariantNotNullForInjector(value);
    }

}
