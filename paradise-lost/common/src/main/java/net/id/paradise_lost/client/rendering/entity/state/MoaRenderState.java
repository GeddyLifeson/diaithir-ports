package net.id.paradise_lost.client.rendering.entity.state;

import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;

public class MoaRenderState extends LivingEntityRenderState {
    public Identifier texture;
    public boolean glowing;
    public boolean saddled;
    public boolean hasChest;
    public boolean inAir;
    public float wingRoll;
    public float wingYaw;
    public float legPitch;
    public float horizontalSpeed;
}
