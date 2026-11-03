/* SPDX-License-Identifier: MIT */

package li.cil.oc2.client.renderer.color;

import li.cil.oc2.common.blockentity.BusCableBlockEntity;
import li.cil.oc2.common.util.ItemStackUtils;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import javax.annotation.Nullable;

// TODO(oc2-restore): BlockColor no longer exists in 26.1.2 (replaced by the tint source system).
//  The interface was removed; the color logic is kept as a plain method until registration is ported.
@OnlyIn(Dist.CLIENT)
public final class BusCableBlockColor {
    public int getColor(final BlockState state, @Nullable final BlockAndTintGetter level, @Nullable final BlockPos pos, final int tintIndex) {
        if (level == null || pos == null) {
            return 0;
        }

        final BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof final BusCableBlockEntity busCable) {
            final BlockState facade = ItemStackUtils.getBlockState(busCable.getFacade());
            if (facade != null) {
                // TODO(oc2-restore): BlockColors.getColor(...) was removed in 26.1.2; the facade's tint
                //  must be resolved through the new tint source system. Until then, no tint is applied.
                return -1;
            }
        }

        return 0;
    }
}
