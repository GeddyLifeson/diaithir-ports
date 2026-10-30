package rpggods.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import rpggods.RGRegistry;

/** Holds no data; it marks the vault position for the economy code. **/
public class TempleVaultBlockEntity extends BlockEntity {
    public TempleVaultBlockEntity(BlockPos pos, BlockState state) {
        super(RGRegistry.TEMPLE_VAULT_TYPE.get(), pos, state);
    }
}
