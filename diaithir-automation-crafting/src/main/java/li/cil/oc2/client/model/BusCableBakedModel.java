/* SPDX-License-Identifier: MIT */

package li.cil.oc2.client.model;

import li.cil.oc2.common.Constants;
import li.cil.oc2.common.block.BusCableBlock;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.EnumProperty;

// TODO(oc2-restore): The dynamic baked model (IDynamicBakedModel, ModelData, ModelProperty, BakedModel,
//  ItemOverrides, BlockModelShaper) does not exist in 26.1.2. The model implementation (support/straight
//  models, facade rendering) has been reduced to the helper logic and data records only.
//  Restore the facade/support/straight quad composition using the new block model API.
public final class BusCableBakedModel {
    private BusCableBakedModel() {
    }

    ///////////////////////////////////////////////////////////////////

    public static boolean isNeighborInDirectionSolid(final BlockAndTintGetter level, final BlockPos pos, final Direction direction) {
        final BlockPos neighborPos = pos.relative(direction);
        return level.getBlockState(neighborPos).isFaceSturdy(level, neighborPos, direction.getOpposite());
    }

    public static boolean isStraightAlongAxis(final BlockState state, final Direction.Axis axis) {
        for (final Direction direction : Constants.DIRECTIONS) {
            final EnumProperty<BusCableBlock.ConnectionType> property = BusCableBlock.FACING_TO_CONNECTION_MAP.get(direction);
            if (axis.test(direction)) {
                if (state.getValue(property) != BusCableBlock.ConnectionType.CABLE) {
                    return false;
                }
            } else {
                if (state.getValue(property) != BusCableBlock.ConnectionType.NONE) {
                    return false;
                }
            }
        }

        return true;
    }

    ///////////////////////////////////////////////////////////////////

    public record BusCableSupportSide(Direction value) { }

    public record BusCableFacade(BlockState blockState) { }
}
