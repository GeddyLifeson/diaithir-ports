package com.diaithir.addons.newage.content.electricity.generation.magnet;

import com.zurrtum.create.catnip.placement.IPlacementHelper;
import com.zurrtum.create.catnip.placement.PlacementOffset;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import com.diaithir.addons.newage.CNABlocks;
import com.diaithir.addons.newage.CNATags;
import com.diaithir.addons.newage.config.CNAConfig;
import com.diaithir.addons.newage.content.electricity.generation.coil.GeneratorCoilBlockEntity;

import java.util.function.Predicate;

@SuppressWarnings("NullableProblems")
public class MagnetPlacementHelper implements IPlacementHelper {
    @Override
    public Predicate<ItemStack> getItemPredicate() {
        return is -> is.is(CNATags.Block.MAGNET.itemTag);
    }

    @Override
    public Predicate<BlockState> getStatePredicate() {
        return bs -> bs.is(CNABlocks.GENERATOR_COIL.get());
    }

    @Override
    public PlacementOffset getOffset(Player player, Level world, BlockState state, BlockPos pos, BlockHitResult ray) {
        if (world.getBlockEntity(ray.getBlockPos()) instanceof GeneratorCoilBlockEntity coil) {
            PlacementOffset offset = checkCoil(coil);
            if (offset == null) {
                for (int i = 0; i < CNAConfig.getServer().maxCoils.get(); i++) {
                    if (world.getBlockEntity(ray.getBlockPos().relative(coil.getBlockState().getValue(BlockStateProperties.AXIS), i)) 
                            instanceof GeneratorCoilBlockEntity c) {
                        if ((offset = checkCoil(c)) != null)
                            return offset;
                    }

                    if (world.getBlockEntity(ray.getBlockPos().relative(coil.getBlockState().getValue(BlockStateProperties.AXIS), -i))
                            instanceof GeneratorCoilBlockEntity c) {
                        if ((offset = checkCoil(c)) != null)
                            return offset;
                    }
                }
            } else return offset;
        }
        
        return PlacementOffset.fail();
    }
    
    private PlacementOffset checkCoil(GeneratorCoilBlockEntity coil) {
        for (BlockPos magnetPos : coil.magnetPositions) {
            if (coil.getLevel().getBlockState(magnetPos).canBeReplaced())
                return PlacementOffset.success(magnetPos);
        }
        
        return null;
    }
}
