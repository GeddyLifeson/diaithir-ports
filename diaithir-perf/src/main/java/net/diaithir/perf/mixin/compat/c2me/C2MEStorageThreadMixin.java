package net.diaithir.perf.mixin.compat.c2me;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * C2ME replaces every vanilla IOWorker with a C2MEStorageThread that starts its own platform thread in the
 * constructor. A ServerLevel opens three storages (chunks, POI, entities), and this pack registers about 90
 * dimensions (72 Backrooms levels and the other mod dimensions), so a dedicated server idles with 267 "C2ME Storage"
 * threads that sit in Object.wait() and never receive a task in dimensions nobody enters. The thread now starts on
 * its first piece of work: every enqueue and close() go through wakeUp(), which is the only place that needs it.
 * flush(boolean) (the autosave right after "Done", for every level) returns at once while the thread was never
 * started, since nothing was ever read or written through it.
 * Both injectors are required: if a C2ME update renames wakeUp or the constructor, boot fails loudly instead of
 * leaving a storage thread that never starts.
 */
@Mixin(targets = "com.ishland.c2me.rewrites.chunkio.common.C2MEStorageThread", remap = false)
abstract class C2MEStorageThreadMixin extends Thread {
    @Unique private final AtomicBoolean diaithirperf$started = new AtomicBoolean();

    @Redirect(method = "<init>", remap = false,
            at = @At(value = "INVOKE", target = "Lcom/ishland/c2me/rewrites/chunkio/common/C2MEStorageThread;start()V"))
    private void diaithirperf$deferStart(@Coerce Object self) {
        // started by wakeUp() on first use
    }

    @Inject(method = "wakeUp", at = @At("HEAD"), remap = false)
    private void diaithirperf$startOnFirstUse(CallbackInfo ci) {
        if (diaithirperf$started.compareAndSet(false, true)) this.start();
    }

    @Inject(method = "flush(Z)Ljava/util/concurrent/CompletableFuture;", at = @At("HEAD"), cancellable = true, remap = false)
    private void diaithirperf$nothingToFlush(boolean sync, CallbackInfoReturnable<CompletableFuture<Void>> cir) {
        if (!diaithirperf$started.get()) cir.setReturnValue(CompletableFuture.completedFuture(null));
    }
}
