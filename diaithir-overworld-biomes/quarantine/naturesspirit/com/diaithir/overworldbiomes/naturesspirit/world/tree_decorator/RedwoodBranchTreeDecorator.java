package com.diaithir.overworldbiomes.naturesspirit.world.tree_decorator;

import com.diaithir.overworldbiomes.naturesspirit.registration.NSWorldGen;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecoratorType;

import java.util.List;

public class RedwoodBranchTreeDecorator extends TreeDecorator {

  public static final MapCodec<RedwoodBranchTreeDecorator> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
    return instance.group(
        Codec.floatRange(0.0F, 1.0F).fieldOf("probability").forGetter((treeDecorator) -> treeDecorator.probability),
        BlockStateProvider.CODEC.fieldOf("leaf_provider").forGetter((treeDecorator) -> treeDecorator.leaf_provider)
    ).apply(instance, RedwoodBranchTreeDecorator::new);
  });
  private final float probability;
  private final BlockStateProvider leaf_provider;

  public RedwoodBranchTreeDecorator(float probability, BlockStateProvider leaf_provider) {
    this.probability = probability;
    this.leaf_provider = leaf_provider;
  }

  @Override
  protected TreeDecoratorType<?> type() {
    return NSWorldGen.REDWOOD_BRANCH_DECORATOR.get();
  }

  @Override
  public void place(Context context) {
    RandomSource random = context.random();
    List<BlockPos> list = context.logs();
    List<BlockPos> list2 = context.leaves();
    list.stream().filter((pos) -> pos.getY() < list2.get(0).getY() - 2 && pos.getY() > list.get(0).getY() + 2).forEach((pos) -> {

      if (random.nextFloat() <= this.probability) {
        Direction direction = Direction.Plane.HORIZONTAL.getRandomDirection(random);
        BlockPos.MutableBlockPos blockPos = pos.offset(direction.getStepX(), 0, direction.getStepZ()).mutable();
        if (context.isAir(blockPos)) {
          context.setBlock(blockPos, leaf_provider.getState(random, blockPos));
        }
        blockPos.move(direction.getClockWise());
        if (context.isAir(blockPos)) {
          context.setBlock(blockPos, leaf_provider.getState(random, blockPos));
        }
        blockPos.move(direction.getClockWise().getOpposite(), 2);
        if (context.isAir(blockPos)) {
          context.setBlock(blockPos, leaf_provider.getState(random, blockPos));
        }
        blockPos.move(direction.getClockWise()).move(direction);
        if (context.isAir(blockPos)) {
          context.setBlock(blockPos, leaf_provider.getState(random, blockPos));
        }
        blockPos.move(direction.getOpposite()).move(Direction.UP);
        if (context.isAir(blockPos)) {
          context.setBlock(blockPos, leaf_provider.getState(random, blockPos));
        }
      }
    });
  }
}
