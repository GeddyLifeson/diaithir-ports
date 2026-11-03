package net.diaithir.aurorian;

import com.mojang.serialization.MapCodec;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;

/** A chest of silentwood; uses the vanilla chest block entity (the vanilla chest model and texture). */
public class SilentwoodChestBlock extends ChestBlock {
    public SilentwoodChestBlock(Properties p) {
        super(() -> BlockEntityType.CHEST, SoundEvents.CHEST_OPEN, SoundEvents.CHEST_CLOSE, p);
    }

    @Override
    public MapCodec<? extends ChestBlock> codec() {
        return simpleCodec(SilentwoodChestBlock::new);
    }
}
