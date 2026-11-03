package net.diaithir.upgrades;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.entity.BlockEntity;

/** A module item. Use on a machine to slot it in; use on it while sneaking to take the last one out. */
public final class ModuleItem extends Item {
    private final ModuleSpec spec;

    public ModuleItem(Properties properties, ModuleSpec spec) {
        super(properties);
        this.spec = spec;
    }

    public ModuleSpec spec() { return spec; }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        BlockEntity be = ctx.getLevel().getBlockEntity(ctx.getClickedPos());
        if (be == null || Upgrades.host(be).isEmpty()) return InteractionResult.PASS;
        if (ctx.getLevel().isClientSide()) return InteractionResult.SUCCESS;
        var player = ctx.getPlayer();
        if (Upgrades.insert(be, spec, Quality.NORMAL)) {
            if (player == null || !player.isCreative()) ctx.getItemInHand().shrink(1);
            return InteractionResult.SUCCESS;
        }
        if (player != null) player.sendOverlayMessage(Component.literal("No free slot, or this machine does not take that module"));
        return InteractionResult.FAIL;
    }
}
