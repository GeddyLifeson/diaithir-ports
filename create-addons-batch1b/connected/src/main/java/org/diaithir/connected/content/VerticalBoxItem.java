package org.diaithir.connected.content;

import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.content.kinetics.base.IRotate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jspecify.annotations.Nullable;

import java.util.Map;

/** The horizontal-placing variant item shared by every gearbox: places the box with its axis chosen from neighbouring shafts. */
public class VerticalBoxItem extends BlockItem {
    public VerticalBoxItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public void registerBlocks(Map<Block, Item> map, Item self) {
    }

    @Override
    protected boolean updateCustomBlockEntityTag(BlockPos pos, Level world, @Nullable Player player, ItemStack stack, BlockState state) {
        Axis preferred = null;
        for (Direction side : Iterate.horizontalDirections) {
            BlockState neighbour = world.getBlockState(pos.relative(side));
            if (neighbour.getBlock() instanceof IRotate rot && rot.hasShaftTowards(world, pos.relative(side), neighbour, side.getOpposite())) {
                if (preferred != null && preferred != side.getAxis()) {
                    preferred = null;
                    break;
                } else {
                    preferred = side.getAxis();
                }
            }
        }
        Axis axis = preferred == null ? (player == null ? Axis.X : player.getDirection().getClockWise().getAxis()) : preferred == Axis.X ? Axis.Z : Axis.X;
        world.setBlockAndUpdate(pos, state.setValue(BlockStateProperties.AXIS, axis));
        return super.updateCustomBlockEntityTag(pos, world, player, stack, state);
    }
}
