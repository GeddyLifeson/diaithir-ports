package net.diaithir.druidry.ars.api.event;

import net.diaithir.druidry.ars.common.block.tile.ScribesTile;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

/**
 * Fired on both sides before a Scribes Table applies its own right click handling
 */
public class ScribesTableInteractEvent extends Event implements ICancellableEvent {

    public Level level;
    public BlockPos pos;
    public ScribesTile tile;
    public Player player;
    public InteractionHand hand;
    public ItemStack stack;
    public InteractionResult result;

    public ScribesTableInteractEvent(Level level, BlockPos pos, ScribesTile tile, Player player, InteractionHand hand, ItemStack stack) {
        this.level = level;
        this.pos = pos;
        this.tile = tile;
        this.player = player;
        this.hand = hand;
        this.stack = stack;
        this.result = level.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.SUCCESS_SERVER;
    }
}
