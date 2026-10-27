package net.diaithir.perf.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import java.util.concurrent.CompletableFuture;
import net.diaithir.perf.QuadInterner;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.client.resources.model.sprite.MaterialBaker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ModelBakery.class)
abstract class ModelBakeryMixin {
    @Unique private ModelBaker.Interner diaithirperf$interner;

    @WrapOperation(method = "bakeModels", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/resources/model/ModelBakery$MissingModels;bake(Lnet/minecraft/client/resources/model/ResolvedModel;Lnet/minecraft/client/resources/model/sprite/MaterialBaker;Lnet/minecraft/client/resources/model/ModelBaker$Interner;)Lnet/minecraft/client/resources/model/ModelBakery$MissingModels;"))
    private ModelBakery.MissingModels diaithirperf$captureInterner(ResolvedModel model, MaterialBaker materials, ModelBaker.Interner interner, Operation<ModelBakery.MissingModels> original) {
        diaithirperf$interner = interner;
        return original.call(model, materials, interner);
    }

    @ModifyReturnValue(method = "bakeModels", at = @At("RETURN"))
    private CompletableFuture<ModelBakery.BakingResult> diaithirperf$clearQuadsWhenBaked(CompletableFuture<ModelBakery.BakingResult> result) {
        ModelBaker.Interner interner = diaithirperf$interner;
        diaithirperf$interner = null;
        return interner instanceof QuadInterner quads ? result.whenComplete((r, e) -> quads.diaithirperf$clear()) : result;
    }
}
