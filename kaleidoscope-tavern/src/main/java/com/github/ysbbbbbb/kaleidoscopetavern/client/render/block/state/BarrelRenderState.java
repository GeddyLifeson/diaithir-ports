package com.github.ysbbbbbb.kaleidoscopetavern.client.render.block.state;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.core.Direction;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

public class BarrelRenderState extends BlockEntityRenderState {
    /**
     * 最多渲染的物品数量（4槽 × 16最大堆叠 ÷ 2 + 1 = 最多每槽9个）
     */
    public static final int MAX_RENDER_ITEMS = 36;
    public final ItemStackRenderState[] itemRenders = createItemRenderArray();

    public Direction direction = Direction.NORTH;
    public boolean isOpen = false;
    public int fluidAmount = 0;
    public Fluid fluid = Fluids.EMPTY;
    public int itemCount = 0;
    public long blockPosSeed = 0;
    public long gameTime = 0;
    public float partialTicks = 0;

    private static ItemStackRenderState[] createItemRenderArray() {
        ItemStackRenderState[] array = new ItemStackRenderState[MAX_RENDER_ITEMS];
        for (int i = 0; i < MAX_RENDER_ITEMS; i++) {
            array[i] = new ItemStackRenderState();
        }
        return array;
    }
}
