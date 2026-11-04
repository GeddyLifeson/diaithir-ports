package org.valkyrienskies.mod.mixin.client.renderer;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import java.util.Collection;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.ViewArea;
import net.minecraft.client.renderer.chunk.SectionRenderDispatcher;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.mod.mixinducks.client.render.IVSViewAreaMethods;

/**
 * Vanilla's ViewArea is a camera-centred grid, so shipyard chunks (millions of blocks away) never get render sections.
 * This keeps a separate map of render sections for ship chunks; {@link MixinLevelRendererShips} compiles and draws them
 * with the ship's render transform.
 */
@Mixin(ViewArea.class)
public abstract class MixinViewArea implements IVSViewAreaMethods {
    @Shadow
    protected Level level;

    @Unique
    private final Long2ObjectOpenHashMap<SectionRenderDispatcher.RenderSection> vs$shipSections = new Long2ObjectOpenHashMap<>();
    @Unique
    private SectionRenderDispatcher vs$dispatcher;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void vs$captureDispatcher(final SectionRenderDispatcher dispatcher, final Level level, final int renderDistance,
        final LevelRenderer levelRenderer, final CallbackInfo ci) {
        this.vs$dispatcher = dispatcher;
    }

    @Override
    public SectionRenderDispatcher.RenderSection vs$getShipRenderSection(final int chunkX, final int sectionY, final int chunkZ) {
        return vs$shipSections.get(SectionPos.asLong(chunkX, sectionY, chunkZ));
    }

    @Override
    public SectionRenderDispatcher.RenderSection vs$getOrCreateShipRenderSection(final int chunkX, final int sectionY, final int chunkZ) {
        final long node = SectionPos.asLong(chunkX, sectionY, chunkZ);
        SectionRenderDispatcher.RenderSection section = vs$shipSections.get(node);
        if (section == null && vs$dispatcher != null) {
            section = vs$dispatcher.new RenderSection(0, node);
            section.setFadeDuration(0L);
            vs$shipSections.put(node, section);
        }
        return section;
    }

    @Override
    public void valkyrienskies$unloadChunk(final int chunkX, final int chunkZ) {
        for (int sy = level.getMinSectionY(); sy <= level.getMaxSectionY(); sy++) {
            final SectionRenderDispatcher.RenderSection s = vs$shipSections.remove(SectionPos.asLong(chunkX, sy, chunkZ));
            if (s != null) {
                s.reset();
            }
        }
    }

    @Override
    public Collection<SectionRenderDispatcher.RenderSection> vs$shipRenderSections() {
        return vs$shipSections.values();
    }

    @Inject(method = "releaseAllBuffers", at = @At("HEAD"))
    private void vs$releaseShipSections(final CallbackInfo ci) {
        for (final SectionRenderDispatcher.RenderSection s : vs$shipSections.values()) {
            s.reset();
        }
        vs$shipSections.clear();
    }

    @Inject(method = "setDirty", at = @At("HEAD"), cancellable = true)
    private void vs$setShipDirty(final int x, final int y, final int z, final boolean playerChanged, final CallbackInfo ci) {
        if (VSGameUtilsKt.isChunkInShipyard(level, x, z)) {
            final SectionRenderDispatcher.RenderSection s = vs$shipSections.get(SectionPos.asLong(x, y, z));
            if (s != null) {
                s.setDirty(playerChanged);
            }
            ci.cancel();
        }
    }

    @Inject(method = "getRenderSection(J)Lnet/minecraft/client/renderer/chunk/SectionRenderDispatcher$RenderSection;", at = @At("HEAD"), cancellable = true)
    private void vs$getShipSection(final long node, final CallbackInfoReturnable<SectionRenderDispatcher.RenderSection> cir) {
        if (VSGameUtilsKt.isChunkInShipyard(level, SectionPos.x(node), SectionPos.z(node))) {
            cir.setReturnValue(vs$shipSections.get(node));
        }
    }
}
