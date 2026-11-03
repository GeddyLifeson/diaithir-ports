package net.diaithir.arms;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.CraftingTableBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

/** A crafting table for appearances (see TransmogRecipe; the recipe also works in any other crafting grid). */
public final class TransmogTableBlock extends CraftingTableBlock {
    public static final MapCodec<TransmogTableBlock> CODEC = simpleCodec(TransmogTableBlock::new);

    public TransmogTableBlock(BlockBehaviour.Properties p) { super(p); }

    @Override public MapCodec<? extends CraftingTableBlock> codec() { return CODEC; }
}
