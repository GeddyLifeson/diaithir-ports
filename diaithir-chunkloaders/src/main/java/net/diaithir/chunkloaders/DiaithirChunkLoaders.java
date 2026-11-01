package net.diaithir.chunkloaders;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.world.chunk.RegisterTicketControllersEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

@Mod(DiaithirChunkLoaders.ID)
public final class DiaithirChunkLoaders {
    public static final String ID = "diaithirchunkloaders";

    public DiaithirChunkLoaders(IEventBus bus, ModContainer container) {
        Registry.register(bus);
        container.registerConfig(ModConfig.Type.SERVER, Config.SPEC);
        bus.addListener(RegisterTicketControllersEvent.class, e -> {
            e.register(Tickets.BLOCKS);
            e.register(Tickets.CARTS);
        });
        NeoForge.EVENT_BUS.addListener(RegisterCommandsEvent.class, e -> Commands.register(e.getDispatcher()));
        NeoForge.EVENT_BUS.addListener(BlockEvent.EntityPlaceEvent.class, DiaithirChunkLoaders::onPlace);
        NeoForge.EVENT_BUS.addListener(net.neoforged.neoforge.event.level.block.BreakBlockEvent.class, DiaithirChunkLoaders::onBreak);
        NeoForge.EVENT_BUS.addListener(PlayerInteractEvent.EntityInteract.class, Carts::onInteract);
        NeoForge.EVENT_BUS.addListener(EntityJoinLevelEvent.class, Carts::onJoin);
        NeoForge.EVENT_BUS.addListener(EntityLeaveLevelEvent.class, Carts::onLeave);
        NeoForge.EVENT_BUS.addListener(ServerTickEvent.Post.class, Carts::onTick);
    }

    /** Refuses a placement that would put a player over the cap. */
    private static void onPlace(BlockEvent.EntityPlaceEvent e) {
        if (!(e.getPlacedBlock().getBlock() instanceof LoaderBlock) || !(e.getEntity() instanceof ServerPlayer player)) return;
        if (Config.OPS_BYPASS_CAP.get() && player.permissions().hasPermission(net.minecraft.server.permissions.Permissions.COMMANDS_GAMEMASTER)) return;
        int cap = Config.MAX_PER_PLAYER.get();
        if (LoaderData.get(player.level().getServer()).count(player.getStringUUID()) >= cap) {
            e.setCanceled(true);
            player.sendOverlayMessage(Component.literal("You already own " + cap + " loaders (server limit)."));
        }
    }

    /** Only the owner, a teammate or an operator may break a loader. */
    private static void onBreak(net.neoforged.neoforge.event.level.block.BreakBlockEvent e) {
        if (e.getLevel().getBlockEntity(e.getPos()) instanceof LoaderBlockEntity be && e.getPlayer() instanceof ServerPlayer p && !be.canManage(p)) {
            e.setCanceled(true);
            p.sendOverlayMessage(Component.literal("This loader belongs to " + be.ownerName + "."));
        }
    }
}
