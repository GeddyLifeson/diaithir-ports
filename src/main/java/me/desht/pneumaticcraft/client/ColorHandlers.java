/*
 * This file is part of pnc-repressurized.
 *
 *     pnc-repressurized is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     pnc-repressurized is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with pnc-repressurized.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.desht.pneumaticcraft.client;

import me.desht.pneumaticcraft.api.lib.Names;
import me.desht.pneumaticcraft.client.util.TintColor;
import me.desht.pneumaticcraft.common.block.entity.IHeatTinted;
import me.desht.pneumaticcraft.common.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.minecraft.client.color.block.BlockTintSource;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber(modid = Names.MOD_ID, value = Dist.CLIENT)
public class ColorHandlers {
    // dropped: camouflage block tinting and item tinting (ITintableItem, tinted block items, bucket fluid colours). 26.1 tints items from the item
    // definition JSON via ItemTintSource types, and these items have no such definitions yet.

    private static final int MAX_TINT_INDEX = 4;

    @SubscribeEvent
    public static void registerBlockColorHandlers(RegisterColorHandlersEvent.BlockTintSources event) {
        for (var block : ModBlocks.BLOCKS.getEntries()) {
            if (block.get() instanceof ITintableBlock tintable) {
                event.register(sources(tintable), block.get());
            }
        }
    }

    private static List<BlockTintSource> sources(ITintableBlock tint) {
        List<BlockTintSource> list = new ArrayList<>();
        for (int i = 0; i < MAX_TINT_INDEX; i++) {
            final int index = i;
            list.add(new BlockTintSource() {
                @Override
                public int color(BlockState state) {
                    return tint.getTintColor(state, null, null, index);
                }

                @Override
                public int colorInWorld(BlockState state, BlockAndTintGetter level, BlockPos pos) {
                    return tint.getTintColor(state, level, pos, index);
                }
            });
        }
        return list;
    }

    public static int desaturate(int c) {
        float[] hsb = TintColor.RGBtoHSB((c & 0xFF0000) >> 16, (c & 0xFF00) >> 8, c & 0xFF, null);
        TintColor color = TintColor.getHSBColor(hsb[0], hsb[1] * 0.4f, hsb[2]);
        if (hsb[2] < 0.7) color = color.brighter();
        return color.getARGB();
    }

    /**
     * Items implementing this will be automatically registered in the ColorHandler.Item event
     */
    public interface ITintableItem {
        int getTintColor(ItemStack stack, int tintIndex);
    }

    /**
     * Items implementing this will be automatically registered in the ColorHandler.Block event
     */
    public interface ITintableBlock {
        int getTintColor(BlockState state, @Nullable BlockAndTintGetter world, @Nullable BlockPos pos, int tintIndex);
    }

    public interface IHeatTintable extends ITintableBlock {
        @Override
        default int getTintColor(BlockState state, @Nullable BlockAndTintGetter world, @Nullable BlockPos pos, int tintIndex) {
            if (world != null && pos != null) {
                BlockEntity te = world.getBlockEntity(pos);
                TintColor tint = te instanceof IHeatTinted ? ((IHeatTinted) te).getColorForTintIndex(tintIndex) : TintColor.WHITE;
                return tint.getARGB();
            }
            return 0xFFFFFFFF;
        }
    }
}
