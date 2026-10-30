package com.github.ysbbbbbb.kaleidoscopetavern.client.render.block.state;

import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.core.Direction;

public class BarCabinetRenderState extends BlockEntityRenderState {
    public final BlockModelRenderState leftBlockRender = new BlockModelRenderState();
    public final BlockModelRenderState rightBlockRender = new BlockModelRenderState();

    public boolean isSingle = false;
    public Direction direction = Direction.NORTH;
}
