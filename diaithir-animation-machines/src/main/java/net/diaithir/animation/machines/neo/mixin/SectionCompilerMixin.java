package net.diaithir.animation.machines.neo.mixin;

import net.diaithir.animation.machines.neo.client.AnimatedBlocks;
import net.minecraft.client.renderer.chunk.SectionCompiler;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** Hides the static chunk copy of a block that the animated renderer draws. */
@Mixin(SectionCompiler.class)
public abstract class SectionCompilerMixin {
    @Redirect(method = "compile(Lnet/minecraft/core/SectionPos;Lnet/minecraft/client/renderer/chunk/RenderSectionRegion;Lcom/mojang/blaze3d/vertex/VertexSorting;Lnet/minecraft/client/renderer/SectionBufferBuilderPack;Ljava/util/List;)Lnet/minecraft/client/renderer/chunk/SectionCompiler$Results;", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getRenderShape()Lnet/minecraft/world/level/block/RenderShape;"))
    private RenderShape diaithirmachineanim$hide(BlockState state) {
        return AnimatedBlocks.hidden(state.getBlock()) ? RenderShape.INVISIBLE : state.getRenderShape();
    }
}
