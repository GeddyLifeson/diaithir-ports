package com.gregtechceu.gtceu.common.pipelike.laser;

import com.gregtechceu.gtceu.api.pipenet.LevelPipeNet;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

public class LevelLaserPipeNet extends LevelPipeNet<LaserPipeProperties, LaserPipeNet> {

    private static final String DATA_ID = "gtceu_laser_pipe_net";

    public static LevelLaserPipeNet getOrCreate(ServerLevel serverLevel) { return null; /* ported stub */ }

    public LevelLaserPipeNet(ServerLevel serverLevel) {
        super(serverLevel);
    }

    public LevelLaserPipeNet(ServerLevel serverLevel, CompoundTag tag, HolderLookup.Provider provider) {
        super(serverLevel, tag, provider);
    }

    @Override
    protected LaserPipeNet createNetInstance() {
        return new LaserPipeNet(this);
    }
}
