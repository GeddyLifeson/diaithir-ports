package net.diaithir.animation;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.BrewingStandMenu;
import net.minecraft.world.inventory.EnchantmentMenu;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.inventory.CraftingMenu;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.item.ItemTossEvent;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerContainerEvent;
import net.neoforged.neoforge.event.level.BlockEvent;

/** Server events that have no client-side signal: they send the visual-only body clips to everyone who sees the player. */
public final class BodyServer {
    private static void body(net.minecraft.world.entity.player.Player p, String clip) {
        if (p instanceof ServerPlayer sp) Anim.play(sp, "body." + clip);
    }

    @SubscribeEvent public void open(PlayerContainerEvent.Open e) {
        AbstractContainerMenu m = e.getContainer();
        body(e.getEntity(), m instanceof BrewingStandMenu ? "brew" : m instanceof EnchantmentMenu ? "enchant" : m instanceof MerchantMenu ? "trade" : m instanceof CraftingMenu ? "craft" : "open_container");
    }
    @SubscribeEvent public void close(PlayerContainerEvent.Close e) { body(e.getEntity(), "close_container"); }
    @SubscribeEvent public void toss(ItemTossEvent e) { body(e.getPlayer(), "drop_item"); }
    @SubscribeEvent public void pickup(ItemEntityPickupEvent.Post e) { body(e.getPlayer(), "pick_up"); }
    @SubscribeEvent public void crafted(PlayerEvent.ItemCraftedEvent e) { body(e.getEntity(), "craft"); }
    @SubscribeEvent public void respawn(PlayerEvent.PlayerRespawnEvent e) { body(e.getEntity(), "respawn"); }
    @SubscribeEvent public void place(BlockEvent.EntityPlaceEvent e) { if (e.getEntity() instanceof net.minecraft.world.entity.player.Player p) body(p, "place"); }

    BodyServer() {}
}
