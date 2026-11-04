package net.diaithir.worldvisuals.lambdynamiclights.client;

import net.minecraft.core.BlockPos;

public record LightSource(int entityId, BlockPos pos, int level) {}
