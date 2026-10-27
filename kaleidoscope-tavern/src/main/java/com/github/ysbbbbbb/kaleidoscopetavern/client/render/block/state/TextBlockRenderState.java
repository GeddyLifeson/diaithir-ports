package com.github.ysbbbbbb.kaleidoscopetavern.client.render.block.state;

import com.github.ysbbbbbb.kaleidoscopetavern.util.TextAlignment;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.DyeColor;

public class TextBlockRenderState extends BlockEntityRenderState {
    public BlockPos blockPos = BlockPos.ZERO;
    public Direction facing = Direction.NORTH;
    public String text = "";
    public DyeColor color = DyeColor.BLACK;
    public boolean isGlowing = false;
    public TextAlignment textAlignment = TextAlignment.CENTER;
    public boolean isLarge = false;
}
