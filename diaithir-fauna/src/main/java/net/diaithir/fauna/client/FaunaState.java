package net.diaithir.fauna.client;

import net.diaithir.fauna.Species;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;

public class FaunaState extends LivingEntityRenderState {
    public Species species;
    public boolean flying;
    /** 0 to 1 while a swing is under way. */
    public float swing;
}
