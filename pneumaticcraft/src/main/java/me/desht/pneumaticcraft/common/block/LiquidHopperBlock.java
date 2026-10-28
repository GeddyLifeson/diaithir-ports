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

package me.desht.pneumaticcraft.common.block;

import me.desht.pneumaticcraft.common.item.PNCBlockItem;
import me.desht.pneumaticcraft.client.ColorHandlers;
import me.desht.pneumaticcraft.common.block.entity.hopper.LiquidHopperBlockEntity;
import me.desht.pneumaticcraft.common.item.IFluidCapProvider;
import me.desht.pneumaticcraft.common.registry.ModDataComponents;
import me.desht.pneumaticcraft.common.registry.ModItems;
import me.desht.pneumaticcraft.common.upgrades.ModUpgrades;
import me.desht.pneumaticcraft.common.upgrades.UpgradableItemUtils;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.util.Lazy;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import net.neoforged.neoforge.fluids.capability.templates.FluidHandlerItemStack;

public class LiquidHopperBlock extends OmnidirectionalHopperBlock implements PneumaticCraftEntityBlock {
    public LiquidHopperBlock(Properties props) {
        super(props);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new LiquidHopperBlockEntity(pPos, pState);
    }

    public static class ItemBlockLiquidHopper extends PNCBlockItem implements ColorHandlers.ITintableItem, IFluidCapProvider {
        public ItemBlockLiquidHopper(Block block) {
            super(block, ModItems.defaultProps());
        }

        @Override
        public net.minecraft.world.item.@org.jspecify.annotations.Nullable ItemStackTemplate getCraftingRemainder(net.minecraft.world.item.ItemInstance instance) {
            if (!(instance instanceof ItemStack itemStack)) return null;
            boolean creative = UpgradableItemUtils.hasCreativeUpgrade(itemStack);
            return FluidUtil.getFluidHandler(itemStack.copy()).map(handler -> {
                // TODO can (or indeed should) we support recipes which drain amounts other than 1000mB?
                handler.drain(1000, creative ? IFluidHandler.FluidAction.SIMULATE : IFluidHandler.FluidAction.EXECUTE);
                return net.minecraft.world.item.ItemStackTemplate.fromNonEmptyStack(handler.getContainer().copy());
            }).orElse(null);
        }

        @Override
        public int getTintColor(ItemStack stack, int tintIndex) {
            int n = UpgradableItemUtils.getUpgradeCount(stack, ModUpgrades.CREATIVE.get());
            return n > 0 ? 0xFFDB46CF : 0xFF2b2727;
        }

        @Override
        public net.neoforged.neoforge.transfer.ResourceHandler<net.neoforged.neoforge.transfer.fluid.FluidResource> provideFluidCapability(net.neoforged.neoforge.transfer.access.ItemAccess access) {
            return new net.neoforged.neoforge.transfer.fluid.ItemAccessFluidHandler(access, ModDataComponents.MAIN_TANK.get(), PneumaticValues.NORMAL_TANK_CAPACITY);
        }
    }
}
