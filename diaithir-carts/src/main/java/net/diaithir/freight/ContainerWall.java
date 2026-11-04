package net.diaithir.freight;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;

/** One wall cell of a container: a full corrugated block. The container is a ring of these round a walkable interior. */
public class ContainerWall extends Block {
    public static final EnumProperty<Paint> PAINT = EnumProperty.create("paint", Paint.class);

    public ContainerWall(Properties props) {
        super(props);
        registerDefaultState(stateDefinition.any().setValue(PAINT, Paint.RED));
    }

    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b) { b.add(PAINT); }
}
