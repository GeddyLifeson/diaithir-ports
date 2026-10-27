package net.diaithir.perf.mixin.compat.ctm;

import java.util.List;
import java.util.Map;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.Direction;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * CTM builds every overlay pattern of every connected-texture model up front: 6 faces x 625 patterns, each an
 * ArrayList in an unmodifiable wrapper. In this pack that is 14.6 million lists, about 1.2 GB of client heap.
 * Patterns, and each face's pattern row, are now built the first time a block actually needs them, as compact
 * immutable lists.
 */
@Mixin(targets = "io.github.chiselteam.ctm.impl.texture.StandardCTMOverlayTable")
abstract class StandardCTMOverlayTableMixin {
    @Shadow @Final @Mutable private List<BakedQuad>[][] overlays;
    @Shadow @Final private static int PATTERN_COUNT;
    @Unique private Map<Direction, BakedQuad[][]> diaithirperf$source;

    @Shadow
    private static List<BakedQuad> buildPattern(BakedQuad[][] quads, int pattern) {
        throw new AssertionError();
    }

    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "buildPattern([[Lnet/minecraft/client/resources/model/geometry/BakedQuad;I)Ljava/util/List;"))
    private List<BakedQuad> diaithirperf$skipEagerBuild(BakedQuad[][] quads, int pattern) {
        return null;
    }

    @SuppressWarnings("unchecked")
    @Inject(method = "<init>", at = @At("RETURN"))
    private void diaithirperf$keepSource(Map<Direction, BakedQuad[][]> source, CallbackInfo ci) {
        diaithirperf$source = source;
        overlays = new List[overlays.length][];
    }

    @SuppressWarnings("unchecked")
    @Inject(method = "get", at = @At("HEAD"), cancellable = true)
    private void diaithirperf$buildOnDemand(Direction direction, int pattern, CallbackInfoReturnable<List<BakedQuad>> cir) {
        // Chunk meshing runs on several threads; a lost race only rebuilds a pattern, and the caller always gets the local result.
        int side = direction.get3DDataValue();
        List<BakedQuad>[] face = overlays[side];
        if (face == null) overlays[side] = face = new List[PATTERN_COUNT];
        List<BakedQuad> quads = face[pattern];
        if (quads == null) face[pattern] = quads = buildPattern(diaithirperf$source.get(direction), pattern);
        cir.setReturnValue(quads);
    }

    @Redirect(method = "buildPattern", at = @At(value = "INVOKE", target = "Ljava/util/Collections;unmodifiableList(Ljava/util/List;)Ljava/util/List;"))
    private static List<BakedQuad> diaithirperf$compact(List<BakedQuad> list) {
        return List.copyOf(list);
    }
}
