package org.valkyrienskies.mod.mixin.feature.shipyard_entities;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.world.level.entity.PersistentEntitySectionManager;
import net.minecraft.world.level.entity.Visibility;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Same unload logic as vanilla processUnloads, wrapped in a try/catch because removeIf() was seen to crash.
 *
 * Not applied when C2ME is installed: C2ME overwrites processUnloads (at priority 1100) with its own
 * implementation, so this injection could not apply and would crash mixin application. C2ME's version
 * drains chunksToUnload one key at a time and has no removeIf(), so the guard is not needed there.
 * See ValkyrienCommonMixinConfigPlugin.
 */
@Mixin(PersistentEntitySectionManager.class)
public abstract class MixinPersistentEntitySectionManagerUnloads {
    @Shadow
    @Final
    private LongSet chunksToUnload;

    @Shadow
    @Final
    private Long2ObjectMap<Visibility> chunkVisibility;

    @Shadow
    private boolean processChunkUnload(final long l) {
        throw new IllegalStateException("This should not be invoked");
    }

    @Inject(method = "processUnloads", at = @At("HEAD"), cancellable = true)
    private void replaceProcessUnloads(final CallbackInfo ci) {
        try {
            final LongSet toRemove = new LongOpenHashSet();
            for (final long key : this.chunksToUnload) {
                if (this.chunkVisibility.get(key) != Visibility.HIDDEN) {
                    toRemove.add(key);
                } else if (this.processChunkUnload(key)) {
                    toRemove.add(key);
                }
            }
            chunksToUnload.removeAll(toRemove);
        } catch (final Exception e) {
            e.printStackTrace();
        }
        ci.cancel();
    }
}
