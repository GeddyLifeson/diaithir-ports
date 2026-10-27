package net.id.paradise_lost.client.rendering.util;

import net.id.paradise_lost.registry.BlockRegistry;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.color.block.BlockTintSources;
import net.minecraft.world.level.block.Block;

import java.util.List;
import java.util.function.BiConsumer;

/**
 * Block tint registrations. Item tints are declared in the item definition JSON ("tints"), since 26.1 moved
 * item colouring out of code.
 */
public final class ParadiseLostColorProviders {
    private ParadiseLostColorProviders() {}

    public static void registerBlocks(BiConsumer<List<BlockTintSource>, Block[]> registrar) {
        Block leaves = BlockRegistry.AUREL_WOODSTUFF.leaves().get();
        registrar.accept(List.of(BlockTintSources.foliage()), new Block[]{
                leaves,
                BlockRegistry.AUREL_LEAF_PILE.get(),
                BlockRegistry.SHAMROCK.get()
        });
        registrar.accept(List.of(BlockTintSources.grass()), new Block[]{
                BlockRegistry.HIGHLANDS_GRASS.get(),
                BlockRegistry.GRASS.get(),
                BlockRegistry.GRASS_FLOWERING.get(),
                BlockRegistry.SHORT_GRASS.get(),
                BlockRegistry.FERN.get(),
                BlockRegistry.BUSH.get(),
                BlockRegistry.POTTED_FERN.get()
        });
        registrar.accept(List.of(BlockTintSources.doubleTallGrass()), new Block[]{
                BlockRegistry.TALL_GRASS.get()
        });
    }
}
