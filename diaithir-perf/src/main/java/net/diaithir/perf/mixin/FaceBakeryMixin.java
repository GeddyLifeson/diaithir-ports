package net.diaithir.perf.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.diaithir.perf.QuadInterner;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.cuboid.FaceBakery;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** Many models bake to the same quads (shared parents, block variants); keep one copy per bake. */
@Mixin(FaceBakery.class)
abstract class FaceBakeryMixin {
    @ModifyReturnValue(method = "bakeQuad(Lnet/minecraft/client/resources/model/ModelBaker$Interner;Lorg/joml/Vector3fc;Lorg/joml/Vector3fc;Lnet/minecraft/client/resources/model/cuboid/CuboidFace$UVs;Lcom/mojang/math/Quadrant;Lnet/minecraft/client/resources/model/geometry/BakedQuad$MaterialInfo;Lnet/minecraft/core/Direction;Lnet/minecraft/client/renderer/block/dispatch/ModelState;Lnet/minecraft/client/resources/model/cuboid/CuboidRotation;Lnet/neoforged/neoforge/client/model/ExtraFaceData;)Lnet/minecraft/client/resources/model/geometry/BakedQuad;", at = @At("RETURN"))
    private static BakedQuad diaithirperf$intern(BakedQuad quad, @Local(argsOnly = true) ModelBaker.Interner interner) {
        return interner instanceof QuadInterner quads ? quads.diaithirperf$quad(quad) : quad;
    }
}
