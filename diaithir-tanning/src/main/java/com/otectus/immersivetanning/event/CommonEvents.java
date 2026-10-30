package com.otectus.immersivetanning.event;

import com.otectus.immersivetanning.ImmersiveTanning;
import com.otectus.immersivetanning.block.LeatherworkersBenchBlock;
import com.otectus.immersivetanning.block.TannersRackBlock;
import com.otectus.immersivetanning.block.TannersVatBlock;
import com.otectus.immersivetanning.blockentity.StationBlockEntity;
import com.otectus.immersivetanning.command.TanningCommands;
import com.otectus.immersivetanning.hide.HideSourceLoader;
import com.otectus.immersivetanning.minigame.SeamPatterns;
import com.otectus.immersivetanning.minigame.SessionManager;
import com.otectus.immersivetanning.network.ModNetwork;
import com.otectus.immersivetanning.recipe.HideMountingLoader;
import com.otectus.immersivetanning.recipe.LeatherworkingLoader;
import com.otectus.immersivetanning.recipe.TanningData;
import com.otectus.immersivetanning.trait.HideTraitLoader;
import com.otectus.immersivetanning.util.Feedback;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.TriState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.block.BreakBlockEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/** Gameplay events (game bus). */
@EventBusSubscriber(modid = ImmersiveTanning.MOD_ID)
public final class CommonEvents {

    @SubscribeEvent
    public static void onAddReloadListeners(AddServerReloadListenersEvent event) {
        event.addListener(ImmersiveTanning.id("hide_traits"), new HideTraitLoader());
        event.addListener(ImmersiveTanning.id("hide_sources"), new HideSourceLoader());
        event.addListener(ImmersiveTanning.id("hide_mounting"), new HideMountingLoader());
        event.addListener(ImmersiveTanning.id("seam_patterns"), new SeamPatterns());
        event.addListener(ImmersiveTanning.id("leatherworking"), new LeatherworkingLoader());
    }

    /** The bench recipes reach clients through our own payload; vanilla recipe sync does not carry them. */
    @SubscribeEvent
    public static void onDatapackSync(OnDatapackSyncEvent event) {
        event.getRelevantPlayers().forEach(ModNetwork::sendRecipes);
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        SessionManager.onServerStopping(event.getServer());
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        TanningData.reset();
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        SessionManager.tick(event.getServer());
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) SessionManager.onLogout(player);
    }

    @SubscribeEvent
    public static void onDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) SessionManager.onDeath(player);
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        TanningCommands.register(event.getDispatcher());
    }

    /**
     * Vanilla skips a block's use() when the player sneaks with an item in hand. Sneak-mounting a wet hide with the last
     * finish needs it, so allow it for non-block items on the stations. Sneaking with a block item still places blocks.
     */
    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        if (!player.isSecondaryUseActive()) return;
        Block block = event.getLevel().getBlockState(event.getPos()).getBlock();
        if (!(block instanceof TannersRackBlock || block instanceof TannersVatBlock || block instanceof LeatherworkersBenchBlock)) return;
        ItemStack held = event.getItemStack();
        if (!held.isEmpty() && !(held.getItem() instanceof BlockItem)) event.setUseBlock(TriState.TRUE);
    }

    /** A station someone else is working at cannot be broken out from under them. */
    @SubscribeEvent
    public static void onBreak(BreakBlockEvent event) {
        Player player = event.getPlayer();
        if (player == null || player.getAbilities().instabuild) return;
        if (event.getLevel().getBlockEntity(event.getPos()) instanceof StationBlockEntity station && station.isLockedFor(player.getUUID())) {
            event.setCanceled(true);
            Feedback.fail(player, "station_in_use");
        }
    }

    private CommonEvents() {}
}
