package net.diaithir.bosses.client;

import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;

public final class PlanetBossRenderState extends LivingEntityRenderState {
    public String look = "";
    public String anim = "";
    /** 0 none, 1 wind-up, 2 strike. */
    public int animPhase;
    /** Ticks since the phase began, with partial tick. */
    public float animT;
}
