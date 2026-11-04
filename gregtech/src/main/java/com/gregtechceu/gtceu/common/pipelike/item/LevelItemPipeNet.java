package com.gregtechceu.gtceu.common.pipelike.item;

import com.gregtechceu.gtceu.api.data.chemical.material.properties.ItemPipeProperties;
import com.gregtechceu.gtceu.api.pipenet.LevelPipeNet;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

public class LevelItemPipeNet extends LevelPipeNet<ItemPipeProperties, ItemPipeNet> {

    public static LevelItemPipeNet getOrCreate(ServerLevel serverLevel) {
        return com.gregtechceu.gtceu.compat.SavedDataCompat.computeIfAbsent(serverLevel, "gtceu_item_pipe_net", () -> new LevelItemPipeNet(serverLevel), (tag, provider) -> new LevelItemPipeNet(serverLevel, tag, provider));
    }

    public LevelItemPipeNet(ServerLevel serverLevel) {
        super(serverLevel);
    }

    public LevelItemPipeNet(ServerLevel serverLevel, CompoundTag tag, HolderLookup.Provider provider) {
        super(serverLevel, tag, provider);
    }

    @Override
    protected ItemPipeNet createNetInstance() {
        return new ItemPipeNet(this);
    }
}
