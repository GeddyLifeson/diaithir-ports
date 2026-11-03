package com.diaithir.overworldbiomes.naturesspirit.blocks;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.grower.TreeGrower;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class SproutingCoconutBlock extends SaplingBlock {

  public static final MapCodec<SproutingCoconutBlock> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
      TreeGrower.CODEC.fieldOf("tree").forGetter(block -> block.treeGrower),
      propertiesCodec()
  ).apply(instance, SproutingCoconutBlock::new));

  private static final VoxelShape OUTLINE_SHAPE = Block.box(3D, 0D, 3D, 13D, 10D, 13D);

  private final TreeGrower treeGrower;

  public SproutingCoconutBlock(TreeGrower generator, BlockBehaviour.Properties settings) {
    super(generator, settings);
    this.treeGrower = generator;
  }

  @Override
  public MapCodec<? extends SaplingBlock> codec() {
    return CODEC;
  }

  @Override
  protected boolean mayPlaceOn(BlockState floor, BlockGetter world, BlockPos pos) {
    return floor.isFaceSturdy(world, pos, Direction.DOWN);
  }

  @Override
  protected VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
    return OUTLINE_SHAPE;
  }
}
