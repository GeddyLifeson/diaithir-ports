package net.diaithir.druidry.ars.api.item;

import net.diaithir.druidry.ars.common.block.tile.SummoningTile;
import net.diaithir.druidry.ars.common.items.ModItem;
import net.diaithir.druidry.ars.setup.registry.ItemsRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

public abstract class AbstractSummonCharm extends ModItem {

    public AbstractSummonCharm(Properties properties) {
        super(properties);
    }

    public AbstractSummonCharm() {
        // PERSISTENT_FAMILIAR_DATA component is not registered in the ported DataComponentRegistry;
        // default item properties are used here.
        this(ItemsRegistry.defaultItemProperties());
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level world = context.getLevel();
        if (world.isClientSide()) return InteractionResult.SUCCESS;
        BlockPos pos = context.getClickedPos();
        InteractionResult result;
        if (world.getBlockEntity(pos) instanceof SummoningTile tile) {
            result = useOnSummonTile(context, world, tile, pos);
        } else {
            result = useOnBlock(context, world, pos);
        }
        if (result == InteractionResult.SUCCESS) context.getItemInHand().shrink(1);

        return result;
    }

    /**
     * @param context useContext
     * @param world   level
     * @param pos     position of the block
     * @return SUCCESS to consume item, PASS to ignore
     */
    public abstract InteractionResult useOnBlock(UseOnContext context, Level world, BlockPos pos);

    /**
     * @param context useContext
     * @param world   level
     * @param tile    summoning tile
     * @param pos     position of the block
     * @return SUCCESS to consume item, PASS to ignore
     */
    public abstract InteractionResult useOnSummonTile(UseOnContext context, Level world, SummoningTile tile, BlockPos pos);

}
