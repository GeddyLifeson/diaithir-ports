package mcjty.rftoolsbuilder.modules.shield.blocks;

import mcjty.rftoolsbuilder.modules.shield.ShieldModule;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

// The shield's look (per-projector colour, texture and camouflage) came from a dynamic baked model that 26.1 removed;
// shield blocks now show one static translucent texture. The mimic is still stored for the projector's logic.
public class ShieldingTileEntity extends BlockEntity {

    private BlockPos shieldProjector;
    private BlockState mimic;

    public ShieldingTileEntity(BlockPos pos, BlockState state) {
        super(ShieldModule.TYPE_SHIELDING.get(), pos, state);
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Nonnull
    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider provider) {
        return saveCustomOnly(provider);
    }

    public BlockPos getShieldProjector() {
        return shieldProjector;
    }

    public void setShieldProjector(BlockPos shieldProjector) {
        this.shieldProjector = shieldProjector;
        setChanged();
    }

    public BlockState getMimic() {
        return mimic;
    }

    public void setMimic(BlockState mimic) {
        this.mimic = mimic;
        setChanged();
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        shieldProjector = input.read("projector", BlockPos.CODEC).orElse(BlockPos.ZERO);
        mimic = input.read("mimic", BlockState.CODEC).orElse(null);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.storeNullable("projector", BlockPos.CODEC, shieldProjector);
        output.storeNullable("mimic", BlockState.CODEC, mimic);
    }
}
