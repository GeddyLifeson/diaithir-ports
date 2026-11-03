package net.diaithir.druidry.ars.common.world.tree;


import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProviderType;

import java.util.function.Function;

public abstract class AbstractSupplierBlockStateProvider extends BlockStateProvider {
    public static <T extends AbstractSupplierBlockStateProvider> Codec<T> codecBuilder(Function<Identifier, T> builder) {
        return Identifier.CODEC.fieldOf("key").xmap(builder, (provider) -> provider.key).codec();
    }

    protected final Identifier key;
    protected BlockState state = null;

    public AbstractSupplierBlockStateProvider(Identifier key) {
        this.key = key;
    }

    @Override
    protected abstract BlockStateProviderType<?> type();

    @Override
    public BlockState getState(WorldGenLevel level, RandomSource random, BlockPos pos) {
        if (state == null) {
            Block block = BuiltInRegistries.BLOCK.getValue(key);
            state = block.defaultBlockState();
        }

        return state;
    }
}
