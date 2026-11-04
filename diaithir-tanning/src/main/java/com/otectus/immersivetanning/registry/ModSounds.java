package com.otectus.immersivetanning.registry;

import com.otectus.immersivetanning.ImmersiveTanning;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(Registries.SOUND_EVENT, ImmersiveTanning.MOD_ID);

    public static final DeferredHolder<SoundEvent, SoundEvent> RACK_MOUNT = register("block.tanners_rack.mount");
    public static final DeferredHolder<SoundEvent, SoundEvent> RACK_SCRAPE = register("block.tanners_rack.scrape");
    public static final DeferredHolder<SoundEvent, SoundEvent> RACK_DRIED = register("block.tanners_rack.dried");
    public static final DeferredHolder<SoundEvent, SoundEvent> VAT_FILL = register("block.tanners_vat.fill");
    public static final DeferredHolder<SoundEvent, SoundEvent> VAT_BARK = register("block.tanners_vat.bark");
    public static final DeferredHolder<SoundEvent, SoundEvent> VAT_HIDE = register("block.tanners_vat.hide");
    public static final DeferredHolder<SoundEvent, SoundEvent> VAT_BUBBLE = register("block.tanners_vat.bubble");
    public static final DeferredHolder<SoundEvent, SoundEvent> BENCH_CUT = register("block.leatherworkers_bench.cut");
    public static final DeferredHolder<SoundEvent, SoundEvent> BENCH_STITCH = register("block.leatherworkers_bench.stitch");
    public static final DeferredHolder<SoundEvent, SoundEvent> BENCH_FINISH = register("block.leatherworkers_bench.finish");
    public static final DeferredHolder<SoundEvent, SoundEvent> KNIFE_STRIP = register("item.tanners_knife.strip");
    public static final DeferredHolder<SoundEvent, SoundEvent> AWL_REPAIR = register("item.awl.repair");
    public static final DeferredHolder<SoundEvent, SoundEvent> ACTION_FAIL = register("ui.tanning.fail");
    public static final DeferredHolder<SoundEvent, SoundEvent> TIMING_CUE = register("ui.tanning.timing_cue");

    private static DeferredHolder<SoundEvent, SoundEvent> register(String name) {
        return SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(ImmersiveTanning.id(name)));
    }

    private ModSounds() {}
}
