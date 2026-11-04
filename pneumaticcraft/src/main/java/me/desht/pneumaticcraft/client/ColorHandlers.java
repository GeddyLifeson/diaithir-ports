package me.desht.pneumaticcraft.client;

import me.desht.pneumaticcraft.client.util.TintColor;
import me.desht.pneumaticcraft.common.block.entity.IHeatTinted;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

/**
 * 26.1 port: item/block tinting is now declared in model JSON; these interfaces are kept so that the
 * common-side classes which supply tint colours still compile. (Client registration is a TODO.)
 */
public class ColorHandlers {
    public static int desaturate(int c) {
        float[] hsb = TintColor.RGBtoHSB((c & 0xFF0000) >> 16, (c & 0xFF00) >> 8, c & 0xFF, null);
        TintColor color = TintColor.getHSBColor(hsb[0], hsb[1] * 0.4f, hsb[2]);
        if (hsb[2] < 0.7) color = color.brighter();
        return color.getARGB();
    }

    public interface ITintableItem {
        int getTintColor(ItemStack stack, int tintIndex);
    }

    public interface ITintableBlock {
        int getTintColor(BlockState state, @Nullable BlockGetter world, @Nullable BlockPos pos, int tintIndex);
    }

    public interface IHeatTintable extends ITintableBlock {
        @Override
        default int getTintColor(BlockState state, @Nullable BlockGetter world, @Nullable BlockPos pos, int tintIndex) {
            if (world != null && pos != null) {
                BlockEntity te = world.getBlockEntity(pos);
                TintColor tint = te instanceof IHeatTinted ? ((IHeatTinted) te).getColorForTintIndex(tintIndex) : TintColor.WHITE;
                return tint.getARGB();
            }
            return 0xFFFFFFFF;
        }
    }
}
