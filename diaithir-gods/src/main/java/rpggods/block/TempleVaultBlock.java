package rpggods.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import rpggods.blockentity.TempleVaultBlockEntity;

/**
 * The temple vault: a stone strongroom. It holds nothing itself; the Diaithir economy scripts (data/diaithir/vault.json)
 * detect it by its id (diaithir:temple_vault) or by the block tag diaithir_gods:temple_vault and do the banking.
 */
public class TempleVaultBlock extends BaseEntityBlock {
    public static final MapCodec<TempleVaultBlock> CODEC = simpleCodec(TempleVaultBlock::new);

    public TempleVaultBlock(final Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TempleVaultBlockEntity(pos, state);
    }
}
