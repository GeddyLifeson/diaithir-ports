package net.diaithir.diaithirfarmkitchen;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class OvenBlock extends Block {
    public OvenBlock(BlockBehaviour.Properties props) {
        super(props);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player,
            InteractionHand hand, BlockHitResult hit) {
        if (stack.isEmpty() || hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;
        if (!(level instanceof ServerLevel server)) return InteractionResult.SUCCESS;
        var input = new SingleRecipeInput(stack);
        var recipe = server.recipeAccess().getRecipeFor(RecipeType.SMELTING, input, server);
        if (recipe.isEmpty()) return InteractionResult.PASS;
        ItemStack fuel = player.getOffhandItem();
        if (fuel.isEmpty() || server.fuelValues().burnDuration(fuel) <= 0) return InteractionResult.PASS;
        ItemStack result = recipe.get().value().assemble(input);
        if (result.isEmpty()) return InteractionResult.PASS;
        stack.shrink(1);
        fuel.shrink(1);
        if (!player.getInventory().add(result)) player.drop(result, false);
        return InteractionResult.SUCCESS;
    }
}
