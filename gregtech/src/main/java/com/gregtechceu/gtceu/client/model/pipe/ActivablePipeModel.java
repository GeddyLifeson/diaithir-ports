package com.gregtechceu.gtceu.client.model.pipe;

import com.gregtechceu.gtceu.api.block.PipeBlock;
import com.gregtechceu.gtceu.api.registry.registrate.provider.GTBlockstateProvider;

import net.minecraft.resources.Identifier;

import org.jetbrains.annotations.Nullable;

/** Port shim, see {@link PipeModel}. */
public class ActivablePipeModel extends PipeModel {

    public @Nullable Identifier sideActive, endActive;
    public @Nullable Identifier sideSecondaryActive, endSecondaryActive;
    public @Nullable Identifier sideOverlayActive, endOverlayActive;
    public int activeEmissivity = 15;

    public ActivablePipeModel(PipeBlock<?, ?, ?> block, float thickness, Identifier side, Identifier end,
                              GTBlockstateProvider provider) {
        super(block, provider, thickness, side, end);
    }

    public void setSideActive(@Nullable Identifier id) { sideActive = id; }
    public void setEndActive(@Nullable Identifier id) { endActive = id; }
    public void setSideSecondaryActive(@Nullable Identifier id) { sideSecondaryActive = id; }
    public void setEndSecondaryActive(@Nullable Identifier id) { endSecondaryActive = id; }
    public void setSideOverlayActive(@Nullable Identifier id) { sideOverlayActive = id; }
    public void setEndOverlayActive(@Nullable Identifier id) { endOverlayActive = id; }
    public void setActiveEmissivity(int e) { activeEmissivity = e; }
}
