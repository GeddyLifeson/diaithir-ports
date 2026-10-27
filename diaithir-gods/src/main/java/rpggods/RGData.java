package rpggods;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import rpggods.deity.Altar;
import rpggods.deity.Deity;
import rpggods.deity.DeityHelper;
import rpggods.deity.Offering;
import rpggods.deity.Sacrifice;
import rpggods.entity.AltarEntity;
import rpggods.favor.FavorCommand;
import rpggods.network.SAltarPacket;
import rpggods.network.SDeityPacket;
import rpggods.network.SOfferingPacket;
import rpggods.network.SPerkPacket;
import rpggods.network.SSacrificePacket;
import rpggods.network.SUpdateAltarPacket;
import rpggods.perk.Perk;
import rpggods.tameable.ITameable;

import java.util.Map;

public final class RGData {

    @SubscribeEvent
    public static void onAddCommands(final RegisterCommandsEvent event) {
        FavorCommand.register(event.getDispatcher());
    }

    /**
     * Used to load datapack info when resources are reloaded
     * @param event the reload listener event
     **/
    @SubscribeEvent
    public static void onReloadListeners(final AddServerReloadListenersEvent event) {
        event.addListener(Identifier.fromNamespaceAndPath(RPGGods.MODID, "altar"), RPGGods.ALTAR_JSON_MANAGER);
        event.addListener(Identifier.fromNamespaceAndPath(RPGGods.MODID, "deity"), RPGGods.DEITY_JSON_MANAGER);
        event.addListener(Identifier.fromNamespaceAndPath(RPGGods.MODID, "offering"), RPGGods.OFFERING_JSON_MANAGER);
        event.addListener(Identifier.fromNamespaceAndPath(RPGGods.MODID, "perk"), RPGGods.PERK_JSON_MANAGER);
        event.addListener(Identifier.fromNamespaceAndPath(RPGGods.MODID, "sacrifice"), RPGGods.SACRIFICE_JSON_MANAGER);
    }

    /**
     * Sends the loaded data to players when they join or when data is reloaded
     */
    @SubscribeEvent
    public static void onDatapackSync(final OnDatapackSyncEvent event) {
        event.getRelevantPlayers().forEach(RGData::syncTo);
    }

    public static void syncTo(final ServerPlayer player) {
        if (player.connection.getConnection().isMemoryConnection()) {
            // integrated server: the client shares the static maps
            return;
        }
        PacketDistributor.sendToPlayer(player,
                new SDeityPacket(RPGGods.DEITY_JSON_MANAGER.getData()),
                new SAltarPacket(RPGGods.ALTAR_JSON_MANAGER.getData()),
                new SOfferingPacket(RPGGods.OFFERING_JSON_MANAGER.getData()),
                new SSacrificePacket(RPGGods.SACRIFICE_JSON_MANAGER.getData()),
                new SPerkPacket(RPGGods.PERK_JSON_MANAGER.getData()));
    }

    /**
     * Used to ensure that
     * 1. AltarEntity syncs inventory to client and
     * 2. Tameable entities sync sitting state to client
     * @param event the Player Start Tracking Entity event
     */
    @SubscribeEvent
    public static void onStartTracking(final PlayerEvent.StartTracking event) {
        if (event.getEntity() instanceof ServerPlayer player && event.getTarget().isAlive()) {
            // sync altar entity
            if (event.getTarget() instanceof AltarEntity altar) {
                ItemStack block = altar.getBlockBySlot();
                PacketDistributor.sendToPlayer(player, new SUpdateAltarPacket(altar.getId(), block));
            }
            // sync tameable entity
            RPGGods.getTameable(event.getTarget()).ifPresent(t ->
                    PacketDistributor.sendToPlayer(player, new rpggods.network.SUpdateSittingPacket(event.getTarget().getId(), t.isSitting())));
        }
    }

    //// Data updaters, run on the server after reload and on the client after sync ////

    public static void updateDeities(final Map<Identifier, Deity> data) {
        RPGGods.DEITY_MAP.clear();
        RPGGods.DEITY_MAP.putAll(data);
    }

    public static void updateAltars(final Map<Identifier, Altar> data) {
        RPGGods.ALTAR_MAP.clear();
        RPGGods.ALTAR_MAP.putAll(data);
        for (DeityHelper helper : RPGGods.DEITY_HELPER.values()) {
            helper.altarList.clear();
        }
        for (Map.Entry<Identifier, Altar> entry : RPGGods.ALTAR_MAP.entrySet()) {
            entry.getValue().getDeity().ifPresent(deity ->
                    RPGGods.DEITY_HELPER.computeIfAbsent(deity, DeityHelper::new).add(entry.getKey(), entry.getValue()));
        }
    }

    public static void updateOfferings(final Map<Identifier, Offering> data) {
        RPGGods.OFFERING_MAP.clear();
        RPGGods.OFFERING_MAP.putAll(data);
        for (DeityHelper helper : RPGGods.DEITY_HELPER.values()) {
            helper.offeringMap.clear();
        }
        for (Map.Entry<Identifier, Offering> entry : RPGGods.OFFERING_MAP.entrySet()) {
            RPGGods.DEITY_HELPER.computeIfAbsent(Offering.getDeity(entry.getKey()), DeityHelper::new).add(entry.getKey(), entry.getValue());
        }
    }

    public static void updateSacrifices(final Map<Identifier, Sacrifice> data) {
        RPGGods.SACRIFICE_MAP.clear();
        RPGGods.SACRIFICE_MAP.putAll(data);
        for (DeityHelper helper : RPGGods.DEITY_HELPER.values()) {
            helper.sacrificeMap.clear();
        }
        for (Map.Entry<Identifier, Sacrifice> entry : RPGGods.SACRIFICE_MAP.entrySet()) {
            RPGGods.DEITY_HELPER.computeIfAbsent(Sacrifice.getDeity(entry.getKey()), DeityHelper::new).add(entry.getKey(), entry.getValue());
        }
    }

    public static void updatePerks(final Map<Identifier, Perk> data) {
        RPGGods.PERK_MAP.clear();
        RPGGods.PERK_MAP.putAll(data);
        RPGGods.AFFINITY.clear();
        for (DeityHelper helper : RPGGods.DEITY_HELPER.values()) {
            helper.perkList.clear();
            helper.perkByConditionMap.clear();
            helper.perkByTypeMap.clear();
        }
        for (Map.Entry<Identifier, Perk> entry : RPGGods.PERK_MAP.entrySet()) {
            RPGGods.DEITY_HELPER.computeIfAbsent(entry.getValue().getDeity(), DeityHelper::new).add(entry.getKey(), entry.getValue());
        }
    }
}
