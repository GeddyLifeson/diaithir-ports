package com.diaithir.overworldbiomes.naturesspirit.blocks;

import com.diaithir.overworldbiomes.naturesspirit.NatureSpirit;
import com.diaithir.overworldbiomes.naturesspirit.blocks.block_entities.PizzaBlockEntity;
import com.diaithir.overworldbiomes.naturesspirit.blocks.block_entities.PizzaToppingVariant;
import com.diaithir.overworldbiomes.naturesspirit.registration.NSMiscBlocks;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class PizzaBlock extends BaseEntityBlock {

  public static final MapCodec<PizzaBlock> CODEC = simpleCodec(PizzaBlock::new);
  public static final int DEFAULT_COMPARATOR_OUTPUT = getComparatorOutput(0);
  protected static final VoxelShape[] BITES_TO_SHAPE;
  public static final IntegerProperty BITES = IntegerProperty.create("pizza_bites", 0, 3);

  static {
    BITES_TO_SHAPE = new VoxelShape[]{
        Block.box(1D, 0D, 1D, 15D, 3D, 15D), Shapes.or(
        Block.box(1D, 0D, 1D, 15D, 3D, 8D),
        Block.box(1D, 0D, 1D, 8D, 3D, 15D)
    ), Block.box(1D, 0D, 1D, 8D, 3D, 15D), Block.box(1D, 0D, 8D, 8D, 3D, 15D)
    };
  }

  public PizzaBlock(Properties settings) {
    super(settings);
    this.registerDefaultState(this.stateDefinition.any().setValue(BITES, 0));
  }

  @Override
  protected MapCodec<? extends BaseEntityBlock> codec() {
    return CODEC;
  }

  @Override
  protected RenderShape getRenderShape(BlockState state) {
    return RenderShape.MODEL;
  }

  protected static InteractionResult tryEat(LevelAccessor world, BlockPos pos, BlockState state, Player player) {
    if (player.canEat(false)) {
      Optional<PizzaBlockEntity> optionalPizzaBlockEntity = world.getBlockEntity(pos, NSMiscBlocks.PIZZA_BLOCK_ENTITY_TYPE.get());
      if (optionalPizzaBlockEntity.isPresent()) {
        PizzaBlockEntity pizzaBlockEntity = optionalPizzaBlockEntity.get();
        player.awardStat(NatureSpirit.EAT_PIZZA_SLICE);
        int foodAmount = 2;
        float saturationModifier = 0.2F;
        for (PizzaToppingVariant pizzaToppingVariant : pizzaBlockEntity.toppings) {
          foodAmount += pizzaToppingVariant.hunger();
          saturationModifier += pizzaToppingVariant.saturation();
        }

        player.getFoodData().eat(foodAmount, saturationModifier);

        int i = state.getValue(BITES);
        world.gameEvent(player, GameEvent.EAT, pos);
        world.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
        if (i < 3) {
          if (!world.isClientSide() && world.getServer() != null) {
            world.getServer().overworld().sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
          }
          world.setBlock(pos, state.setValue(BITES, state.getValue(BITES) + 1), 2);
          pizzaBlockEntity.setChanged();
        } else {
          world.removeBlock(pos, false);
          world.gameEvent(player, GameEvent.BLOCK_DESTROY, pos);
        }
        return InteractionResult.SUCCESS;
      }

    }
    return InteractionResult.PASS;
  }

  public static int getComparatorOutput(int bites) {
    return (7 - bites) * 2;
  }

  @Override
  public ItemStack getCloneItemStack(LevelReader world, BlockPos pos, BlockState state, boolean includeData) {
    Optional<PizzaBlockEntity> optionalPizzaBlockEntity = world.getBlockEntity(pos, NSMiscBlocks.PIZZA_BLOCK_ENTITY_TYPE.get());
    if (optionalPizzaBlockEntity.isPresent()) {
      PizzaBlockEntity pizzaBlockEntity = optionalPizzaBlockEntity.get();
      int BITE_STATE = state.getValue(BITES);
      Item item =
          BITE_STATE == 0 ? NSMiscBlocks.WHOLE_PIZZA : BITE_STATE == 1 ? NSMiscBlocks.THREE_QUARTERS_PIZZA : BITE_STATE == 2 ? NSMiscBlocks.HALF_PIZZA : NSMiscBlocks.QUARTER_PIZZA;
      ItemStack itemStack = new ItemStack(item);
      pizzaBlockEntity.setStackNbt(itemStack, null);
      return itemStack;
    }
    return super.getCloneItemStack(world, pos, state, includeData);
  }

  @Override
  public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
    int BITE_STATE = state.getValue(BITES);
    return BITES_TO_SHAPE[BITE_STATE];
  }

  @Override
  protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
    Optional<PizzaBlockEntity> optionalPizzaBlockEntity = world.getBlockEntity(pos, NSMiscBlocks.PIZZA_BLOCK_ENTITY_TYPE.get());
    if (optionalPizzaBlockEntity.isPresent()) {
      return tryEat(world, pos, state, player);
    }
    return InteractionResult.PASS;
  }

  @Override
  protected InteractionResult useItemOn(ItemStack itemStack, BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
    Optional<PizzaBlockEntity> optionalPizzaBlockEntity = world.getBlockEntity(pos, NSMiscBlocks.PIZZA_BLOCK_ENTITY_TYPE.get());
    if (optionalPizzaBlockEntity.isPresent()) {
      PizzaBlockEntity pizzaBlockEntity = optionalPizzaBlockEntity.get();
      Item item = itemStack.getItem();
      if (pizzaBlockEntity.canPlaceTopping(itemStack, world, pizzaBlockEntity)) {
        if (!player.isCreative()) {
          itemStack.shrink(1);
        }
        if (!world.isClientSide()) {
          world.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
        }

        world.playSound(null, pos, SoundEvents.MOSS_PLACE, SoundSource.BLOCKS, 1.0F, 1.0F);
        pizzaBlockEntity.toppingCount++;
        world.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
        player.awardStat(Stats.ITEM_USED.get(item));

        return InteractionResult.SUCCESS;
      }
      if (world.isClientSide()) {
        if (tryEat(world, pos, state, player).consumesAction()) {
          return InteractionResult.SUCCESS;
        }

        if (itemStack.isEmpty()) {
          return InteractionResult.CONSUME;
        }
      }

      return tryEat(world, pos, state, player);
    }
    return InteractionResult.TRY_WITH_EMPTY_HAND;
  }

  @Override
  protected BlockState updateShape(BlockState state, LevelReader world, ScheduledTickAccess tickAccess, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, net.minecraft.util.RandomSource random) {
    return direction == Direction.DOWN && !state.canSurvive(world, pos) ? Blocks.AIR.defaultBlockState()
        : super.updateShape(state, world, tickAccess, pos, direction, neighborPos, neighborState, random);
  }

  @Override
  protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
    builder.add(BITES);
  }

  @Override
  public boolean canSurvive(BlockState state, LevelReader world, BlockPos pos) {
    BlockPos pos1 = pos.below();
    return world.getBlockState(pos1).isRedstoneConductor(world, pos1);
  }

  @Override
  protected int getAnalogOutputSignal(BlockState state, Level world, BlockPos pos, Direction direction) {
    int BITE_STATE = state.getValue(BITES);
    return getComparatorOutput(BITE_STATE);
  }

  @Override
  protected boolean hasAnalogOutputSignal(BlockState state) {
    return true;
  }

  @Nullable
  @Override
  public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
    return new PizzaBlockEntity(pos, state);
  }
}
