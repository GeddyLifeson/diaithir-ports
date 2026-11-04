package net.diaithir.aurorian;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

/** Smelts like a furnace; chimneys stacked on top stretch each piece of fuel (see AurorianFurnaceBlockEntity). */
public class AurorianFurnaceBlock extends AbstractFurnaceBlock {
    public static final MapCodec<AurorianFurnaceBlock> CODEC = simpleCodec(AurorianFurnaceBlock::new);

    public AurorianFurnaceBlock(Properties p) {
        super(p);
    }

    @Override
    public MapCodec<AurorianFurnaceBlock> codec() {
        return CODEC;
    }

    @Override
    protected void openContainer(Level level, BlockPos pos, Player player) {
        if (level.getBlockEntity(pos) instanceof AurorianFurnaceBlockEntity be) {
            player.openMenu((MenuProvider) be);
        }
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AurorianFurnaceBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createFurnaceTicker(level, type, DiaithirAurorian.FURNACE_BE.get());
    }
}
