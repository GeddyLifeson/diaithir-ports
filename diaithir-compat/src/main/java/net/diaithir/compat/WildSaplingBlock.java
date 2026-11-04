package net.diaithir.compat;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.grower.TreeGrower;

public class WildSaplingBlock extends SaplingBlock {
    public static final MapCodec<WildSaplingBlock> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            TreeGrower.CODEC.fieldOf("tree").forGetter(b -> b.grower), propertiesCodec()).apply(i, WildSaplingBlock::new));
    private final TreeGrower grower;

    public WildSaplingBlock(TreeGrower grower, Properties properties) {
        super(grower, properties);
        this.grower = grower;
    }

    @Override
    public MapCodec<? extends SaplingBlock> codec() {
        return CODEC;
    }
}
