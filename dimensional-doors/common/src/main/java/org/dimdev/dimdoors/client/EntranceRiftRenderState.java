package org.dimdev.dimdoors.client;

import net.minecraft.world.level.block.state.BlockState;
import org.dimdev.dimdoors.api.client.DefaultTransformation;
import org.dimdev.dimdoors.api.client.Transformer;

public class EntranceRiftRenderState extends RiftRenderState {
    public BlockState renderBlockState;
    public Transformer transformer = DefaultTransformation.NONE;
    public boolean tall;
}
