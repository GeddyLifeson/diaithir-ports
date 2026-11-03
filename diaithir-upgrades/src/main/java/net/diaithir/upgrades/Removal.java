package net.diaithir.upgrades;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/** Sneak with an empty hand and right-click a machine to take its last module out. */
final class Removal {
    private Removal() {}

    static void onRightClick(PlayerInteractEvent.RightClickBlock e) {
        var player = e.getEntity();
        if (e.getHand() != InteractionHand.MAIN_HAND || !player.isShiftKeyDown() || !player.getMainHandItem().isEmpty()) return;
        BlockEntity be = e.getLevel().getBlockEntity(e.getPos());
        if (be == null) return;
        var host = Upgrades.host(be);
        if (host.isEmpty() || host.get().slots().modules().isEmpty()) return;
        e.setCanceled(true);
        if (e.getLevel().isClientSide()) return;
        var mods = host.get().slots().modules();
        int slot = mods.size() - 1;
        String id = mods.get(slot).spec().id();
        if (!Upgrades.remove(be, slot)) return;
        Modules.ALL.stream().filter(i -> i.getId().getPath().equals(id)).findFirst().ifPresent(item -> {
            ItemStack out = new ItemStack(item.get());
            if (!player.getInventory().add(out)) {
                e.getLevel().addFreshEntity(new ItemEntity(e.getLevel(), player.getX(), player.getY(), player.getZ(), out));
            }
        });
    }
}
