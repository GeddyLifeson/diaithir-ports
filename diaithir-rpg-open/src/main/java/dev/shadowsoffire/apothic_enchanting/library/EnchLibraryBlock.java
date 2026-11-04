package dev.shadowsoffire.apothic_enchanting.library;

import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.MapCodec;

import dev.shadowsoffire.apothic_enchanting.util.TooltipUtil;
import dev.shadowsoffire.placebo.menu.MenuUtil;
import dev.shadowsoffire.placebo.menu.SimplerMenuProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType.BlockEntitySupplier;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;

public class EnchLibraryBlock extends HorizontalDirectionalBlock implements EntityBlock {

    public static final Component NAME = TooltipUtil.lang("menu", "library");
    private static final Logger LOGGER = LogUtils.getLogger();

    protected final BlockEntitySupplier<? extends EnchLibraryTile> tileSupplier;
    protected final int maxLevel;

    public EnchLibraryBlock(BlockBehaviour.Properties properties, BlockEntitySupplier<? extends EnchLibraryTile> tileSupplier, int maxLevel) {
        super(properties);
        this.tileSupplier = tileSupplier;
        this.maxLevel = maxLevel;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        return MenuUtil.openGui(player, pos, EnchLibraryContainer::new);
    }

    @Override
    public MenuProvider getMenuProvider(BlockState state, Level world, BlockPos pos) {
        return new SimplerMenuProvider<>(world, pos, EnchLibraryContainer::new);
    }

    @Override
    protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext p_196258_1_) {
        return this.defaultBlockState().setValue(FACING, p_196258_1_.getHorizontalDirection().getOpposite());
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return this.tileSupplier.create(pPos, pState);
    }

    /**
     * Vanilla middle-click requires Ctrl to be held for {@code includeData = true} — a plain middle-click
     * would otherwise hand back an empty library. Since the library's whole point is the stored enchantments,
     * we always embed the block-entity data, matching the pattern used by banners / decorated pots / beehives.
     */
    @Override
    protected ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state, boolean includeData) {
        ItemStack s = new ItemStack(this);
        BlockEntity te = level.getBlockEntity(pos);
        if (te != null) {
            writeBlockEntityData(s, te, level.registryAccess());
        }
        return s;
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder ctx) {
        ItemStack s = new ItemStack(this);
        BlockEntity te = ctx.getParameter(LootContextParams.BLOCK_ENTITY);
        if (te != null) {
            writeBlockEntityData(s, te, ctx.getLevel().registryAccess());
        }
        return Arrays.asList(s);
    }

    /**
     * Serialise the block entity's custom NBT (points + levels, via {@link EnchLibraryTile#saveAdditional})
     * into the {@link net.minecraft.core.component.DataComponents#BLOCK_ENTITY_DATA} component on the stack.
     * Vanilla {@link BlockItem#updateCustomBlockEntityTag} reads this back at placement and calls
     * {@code customData.loadInto(blockEntity, registries)}, which routes through {@code loadCustomOnly →
     * loadAdditional}, restoring the state. The previously-attempted {@code te.collectComponents()} path
     * returns an empty map for our tile because we don't expose state via implicit components.
     */
    private static void writeBlockEntityData(ItemStack stack, BlockEntity te, HolderLookup.Provider registries) {
        try (ProblemReporter.ScopedCollector reporter = new ProblemReporter.ScopedCollector(te.problemPath(), LOGGER)) {
            TagValueOutput output = TagValueOutput.createWithContext(reporter, registries);
            te.saveCustomOnly(output);
            BlockItem.setBlockEntityData(stack, te.getType(), output);
        }
    }

    // NOTE (26.1 port): Block#appendHoverText was removed; library capacity/points tooltip moved to
    // a follow-up. The underlying BLOCK_ENTITY_DATA component still persists via writeBlockEntityData.

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return null;
    }

}
