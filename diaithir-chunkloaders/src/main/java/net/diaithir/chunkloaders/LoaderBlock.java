package net.diaithir.chunkloaders;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;

/** A Loader of tier 1 to 3. Feed it fuel (any furnace fuel) by hand; use it empty-handed to read radius, time left and owner. */
public final class LoaderBlock extends BaseEntityBlock {
    public static final BooleanProperty LIT = BlockStateProperties.LIT;
    public static final MapCodec<LoaderBlock> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(propertiesCodec(), Codec.INT.fieldOf("tier").forGetter(b -> b.tier)).apply(i, LoaderBlock::new));
    public final int tier;

    public LoaderBlock(Properties properties, int tier) {
        super(properties);
        this.tier = tier;
        registerDefaultState(stateDefinition.any().setValue(LIT, false));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() { return CODEC; }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) { builder.add(LIT); }

    @Override
    protected RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new LoaderBlockEntity(pos, state); }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide() ? null : createTickerHelper(type, Registry.LOADER_ENTITY.get(), LoaderBlockEntity::tick);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity by, ItemStack stack) {
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof LoaderBlockEntity be) {
            if (by instanceof Player p) be.claim(p.getStringUUID(), p.getGameProfile().name());
            else be.claim("", "");
        }
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof LoaderBlockEntity be)) return InteractionResult.PASS;
        int burn = level.fuelValues().burnDuration(stack);
        if (burn <= 0) return InteractionResult.TRY_WITH_EMPTY_HAND;
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        if (!be.canManage(player)) {
            player.sendOverlayMessage(Component.literal("This loader belongs to " + be.ownerName + "."));
            return InteractionResult.CONSUME;
        }
        int used = 0;
        do {
            if (be.addFuel(burn) <= 0) break;
            used++;
            var rem = stack.getCraftingRemainder();
            if (!player.hasInfiniteMaterials()) {
                stack.shrink(1);
                if (rem != null) player.getInventory().placeItemBackInInventory(rem.create());
            }
        } while (player.isShiftKeyDown() && !stack.isEmpty());
        player.sendOverlayMessage(Component.literal(used == 0 ? "The loader is full of fuel." : status(be)));
        return InteractionResult.CONSUME;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof LoaderBlockEntity be)) return InteractionResult.PASS;
        if (!level.isClientSide()) player.sendOverlayMessage(Component.literal(status(be)));
        return InteractionResult.SUCCESS;
    }

    static String status(LoaderBlockEntity be) {
        int r = be.radius(), side = 2 * r + 1;
        return "Tier " + be.tier() + ": radius " + r + " (" + side + "x" + side + " chunks), "
                + (be.fuel > 0 ? duration(be.secondsLeft()) + " left" : "out of fuel")
                + (be.owner.isEmpty() ? "" : ", owner " + be.ownerName);
    }

    static String duration(long s) {
        return s >= 3600 ? (s / 3600) + "h " + (s / 60 % 60) + "m" : s >= 60 ? (s / 60) + "m " + (s % 60) + "s" : s + "s";
    }
}
