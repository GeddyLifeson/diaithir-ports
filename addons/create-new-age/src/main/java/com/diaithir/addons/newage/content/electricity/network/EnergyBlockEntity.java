package com.diaithir.addons.newage.content.electricity.network;

import net.minecraft.world.level.block.entity.BlockEntity;
import com.diaithir.addons.newage.esl.EnergyStorage;

public record EnergyBlockEntity(BlockEntity entity, EnergyStorage storage) {
}
