package com.github.ysbbbbbb.kaleidoscopetavern.client.render.block.state;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.core.Direction;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

public class PressingTubRenderState extends BlockEntityRenderState {
    public final ItemStackRenderState stackRender = new ItemStackRenderState();
    public Fluid fluid = Fluids.EMPTY;
    public Direction direction = Direction.NORTH;
    public int fluidAmount = 0;
    public int itemAmount = 0;
    public boolean tilt = false;
}
