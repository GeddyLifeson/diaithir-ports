package folk.sisby.surveyor.mixin;

import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(BlockBehaviour.class)
public interface AccessAbstractBlock {
	@Accessor("hasCollision")
	boolean isCollidable();

	@Invoker("getCloneItemStack")
	ItemStack invokeGetPickStack(final LevelReader world, final BlockPos pos, final BlockState state, boolean includeData);
}
