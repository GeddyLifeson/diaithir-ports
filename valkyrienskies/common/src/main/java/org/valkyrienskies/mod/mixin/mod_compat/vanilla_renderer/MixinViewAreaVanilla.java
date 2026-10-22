package org.valkyrienskies.mod.mixin.mod_compat.vanilla_renderer;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.client.renderer.ViewArea;
import net.minecraft.client.renderer.chunk.SectionRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import net.minecraft.client.renderer.LevelRenderer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.mod.mixinducks.client.render.IVSViewAreaMethods;

/**
 * Lets {@link ViewArea} manage render sections for ship chunks.
 *
 * <p>Ship sections are kept out of the view grid, keyed by their shipyard-space section node
 * ({@code SectionPos.asLong}); compilation is driven by {@code MixinLevelRendererVanilla}, not by
 * the ViewArea.
 */
@Mixin(ViewArea.class)
public abstract class MixinViewAreaVanilla implements IVSViewAreaMethods {

    @Shadow
    @Final
    protected Level level;

    // Ship render sections, keyed by their shipyard-space section node (SectionPos.asLong).
    @Unique
    private final Long2ObjectMap<SectionRenderDispatcher.RenderSection> vs$shipRenderSections =
        new Long2ObjectOpenHashMap<>();

    // Captured from the constructor; used to create ship render sections.
    @Unique
    private SectionRenderDispatcher vs$sectionRenderDispatcher;

    // Synthetic index counter for ship sections (they live outside the RotatingSectionStorage, so their
    // index is only cosmetic - the per-draw uniform slot is assigned by list position in prepareChunkRenders).
    @Unique
    private int vs$nextShipSectionIndex = 0x40000000;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void vs$captureDispatcher(final SectionRenderDispatcher sectionRenderDispatcher, final Level level,
        final int viewDistance, final LevelRenderer levelRenderer, final CallbackInfo ci) {
        this.vs$sectionRenderDispatcher = sectionRenderDispatcher;
    }

    /**
     * Return the ship render section when the query lands in a shipyard chunk.
     */
    @Inject(method = "getRenderSectionAt", at = @At("HEAD"), cancellable = true)
    private void vs$preGetRenderSectionAt(final BlockPos pos,
        final CallbackInfoReturnable<SectionRenderDispatcher.RenderSection> cir) {
        final Level level = this.level;
        if (level == null) {
            return;
        }
        final int chunkX = SectionPos.blockToSectionCoord(pos.getX());
        final int sectionY = SectionPos.blockToSectionCoord(pos.getY());
        final int chunkZ = SectionPos.blockToSectionCoord(pos.getZ());
        if (VSGameUtilsKt.isChunkInShipyard(level, chunkX, chunkZ)) {
            cir.setReturnValue(vs$shipRenderSections.get(SectionPos.asLong(chunkX, sectionY, chunkZ)));
        }
    }

    @Override
    public SectionRenderDispatcher.RenderSection vs$getShipRenderSection(final int chunkX, final int sectionY,
        final int chunkZ) {
        return vs$shipRenderSections.get(SectionPos.asLong(chunkX, sectionY, chunkZ));
    }

    @Override
    public SectionRenderDispatcher.RenderSection vs$getOrCreateShipRenderSection(final int chunkX, final int sectionY,
        final int chunkZ) {
        final long node = SectionPos.asLong(chunkX, sectionY, chunkZ);
        SectionRenderDispatcher.RenderSection section = vs$shipRenderSections.get(node);
        if (section == null && vs$sectionRenderDispatcher != null) {
            section = vs$sectionRenderDispatcher.new RenderSection(vs$nextShipSectionIndex++, node);
            vs$shipRenderSections.put(node, section);
        }
        return section;
    }

    @Override
    public void valkyrienskies$unloadChunk(final int chunkX, final int chunkZ) {
        for (int sectionY = level.getMinSectionY(); sectionY <= level.getMaxSectionY(); sectionY++) {
            final SectionRenderDispatcher.RenderSection section =
                vs$shipRenderSections.remove(SectionPos.asLong(chunkX, sectionY, chunkZ));
            if (section != null) {
                section.reset();
            }
        }
    }

    /**
     * Reset VS ship render sections so we don't leak GPU buffers.
     */
    @Inject(method = "releaseAllBuffers", at = @At("HEAD"))
    private void vs$postReleaseAllBuffers(final CallbackInfo ci) {
        for (final SectionRenderDispatcher.RenderSection section : vs$shipRenderSections.values()) {
            if (section != null) {
                section.reset();
            }
        }
        vs$shipRenderSections.clear();
    }
}
