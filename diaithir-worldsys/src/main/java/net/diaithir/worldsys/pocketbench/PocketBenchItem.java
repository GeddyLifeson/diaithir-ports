package net.diaithir.worldsys.pocketbench;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.InteractionHand;

/** Opens a crafting grid anywhere, backed by the player's own position. */
public final class PocketBenchItem extends Item {
    public PocketBenchItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (player.getCooldowns().isOnCooldown(player.getItemInHand(hand))) return InteractionResult.FAIL;
        if (!level.isClientSide()) open(level, player);
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null || player.isShiftKeyDown()) return InteractionResult.PASS;
        if (player.getCooldowns().isOnCooldown(context.getItemInHand())) return InteractionResult.FAIL;
        if (!context.getLevel().isClientSide()) open(context.getLevel(), player);
        return InteractionResult.SUCCESS;
    }

    private void open(Level level, Player player) {
        player.getCooldowns().addCooldown(player.getMainHandItem().is(this) ? player.getMainHandItem() : player.getOffhandItem(), 30);
        player.openMenu(new SimpleMenuProvider(
                (id, inv, p) -> new CraftingMenu(id, inv, ContainerLevelAccess.create(level, p.blockPosition())) {
                    @Override
                    public boolean stillValid(Player pl) {
                        return true;
                    }
                },
                Component.translatable("container.crafting")));
    }
}
