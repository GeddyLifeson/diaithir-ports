package net.diaithir.sharedspace;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

public final class ClusterBlockEntity extends BlockEntity {
    private final @Nullable BlockState[] cells = new BlockState[Cells.COUNT];

    public ClusterBlockEntity(BlockPos pos, BlockState state) {
        super(SharedSpace.CLUSTER_ENTITY.get(), pos, state);
    }

    /** The cluster is being replaced or destroyed (explosion, piston, command, another mod): hand back every member still inside. */
    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        if (level instanceof ServerLevel server) {
            for (int i = 0; i < Cells.COUNT; i++) {
                BlockState member = cells[i];
                if (member != null) {
                    cells[i] = null;
                    Block.dropResources(member, server, pos);
                }
            }
        }
        super.preRemoveSideEffects(pos, state);
    }

    public @Nullable BlockState get(int cell) {
        return cells[cell];
    }

    public void set(int cell, @Nullable BlockState state) {
        cells[cell] = state;
        setChanged();
    }

    public int count() {
        int n = 0;
        for (BlockState s : cells) {
            if (s != null) {
                n++;
            }
        }
        return n;
    }

    public boolean[] taken() {
        boolean[] t = new boolean[Cells.COUNT];
        for (int i = 0; i < Cells.COUNT; i++) {
            t[i] = cells[i] != null;
        }
        return t;
    }

    public int light() {
        int best = 0;
        for (BlockState s : cells) {
            if (s != null) {
                best = Math.max(best, s.getLightEmission());
            }
        }
        return best;
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        for (int i = 0; i < Cells.COUNT; i++) {
            output.storeNullable("c" + i, BlockState.CODEC, cells[i]);
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        for (int i = 0; i < Cells.COUNT; i++) {
            cells[i] = input.read("c" + i, BlockState.CODEC).orElse(null);
        }
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }
}
