package com.gregtechceu.gtceu.client.model.pipe;

import com.gregtechceu.gtceu.api.block.PipeBlock;
import com.gregtechceu.gtceu.api.registry.registrate.provider.GTBlockstateProvider;

import net.minecraft.resources.Identifier;

import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

/** Port shim: the generated pipe model is not ported (see PORT_NOTES.md); only the builder surface is kept. */
public class PipeModel {

    public static final Set<PipeModel> DYNAMIC_MODELS = new HashSet<>();

    public static void initDynamicModels() {}

    protected final PipeBlock<?, ?, ?> block;
    public Identifier side, end;
    public @Nullable Identifier sideSecondary, endSecondary;
    public @Nullable Identifier sideOverlay, endOverlay;

    public PipeModel(PipeBlock<?, ?, ?> block, GTBlockstateProvider provider, float thickness, Identifier side,
                     Identifier end) {
        this.block = block;
        this.side = side;
        this.end = end;
    }

    public PipeBlock<?, ?, ?> getBlock() { return block; }
    public final void dynamicModel() {}
    public void initModels() {}
    public void setSide(Identifier id) { side = id; }
    public void setEnd(Identifier id) { end = id; }
    public void setSideSecondary(@Nullable Identifier id) { sideSecondary = id; }
    public void setEndSecondary(@Nullable Identifier id) { endSecondary = id; }
    public void setSideOverlay(@Nullable Identifier id) { sideOverlay = id; }
    public void setEndOverlay(@Nullable Identifier id) { endOverlay = id; }
}
