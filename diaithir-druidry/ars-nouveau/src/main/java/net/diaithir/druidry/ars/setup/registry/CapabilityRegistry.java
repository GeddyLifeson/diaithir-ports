package net.diaithir.druidry.ars.setup.registry;

import net.diaithir.druidry.ars.ArsNouveau;
import net.diaithir.druidry.ars.api.item.IWandable;
import net.diaithir.druidry.ars.api.item.inv.IFiltersetProvider;
import net.diaithir.druidry.ars.api.item.inv.IMapInventory;
import net.diaithir.druidry.ars.api.mana.IManaCap;
import net.diaithir.druidry.ars.api.source.ISourceCap;
import net.diaithir.druidry.ars.api.spell.IResolveListener;
import net.diaithir.druidry.ars.common.capability.ANPlayerDataCap;
import net.diaithir.druidry.ars.common.capability.IPlayerCap;
import net.diaithir.druidry.ars.common.capability.ManaCap;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.EntityCapability;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import java.util.List;

public class CapabilityRegistry {

    public static final EntityCapability<ManaCap, Void> MANA_CAPABILITY = EntityCapability.createVoid(ArsNouveau.prefix("mana"), ManaCap.class);
    public static final EntityCapability<ANPlayerDataCap, Void> PLAYER_DATA_CAP = EntityCapability.createVoid(ArsNouveau.prefix("player_data"), ANPlayerDataCap.class);
    public static final BlockCapability<ISourceCap, Direction> SOURCE_CAPABILITY = BlockCapability.createSided(ArsNouveau.prefix("source"), ISourceCap.class);
    public static final BlockCapability<IMapInventory, Direction> MAP_INV_CAP = BlockCapability.createSided(ArsNouveau.prefix("map_inventory"), IMapInventory.class);
    public static final BlockCapability<IFiltersetProvider, Direction> FILTERSET_CAPABILITY = BlockCapability.createSided(ArsNouveau.prefix("filterset"), IFiltersetProvider.class);
    public static final BlockCapability<IResolveListener, Void> BLOCK_SPELL_RESOLVE_CAP = BlockCapability.createVoid(ArsNouveau.prefix("spell_listener"), IResolveListener.class);
    public static final EntityCapability<IResolveListener, Void> ENTITY_SPELL_RESOLVE_CAP = EntityCapability.createVoid(ArsNouveau.prefix("spell_listener"), IResolveListener.class);

    public static final BlockCapability<IWandable, Direction> WANDABLE_BLOCK_CAPABILITY = BlockCapability.createSided(ArsNouveau.prefix("wandable"), IWandable.class);

    /**
     * Get the {@link IManaCap} from the specified entity.
     *
     * @param entity The entity
     * @return A lazy optional containing the IMana, if any
     */
    public static ManaCap getMana(final LivingEntity entity) {
        if (entity == null)
            return null;
        return entity.getCapability(MANA_CAPABILITY);
    }

    /**
     * Get the {@link IPlayerCap} from the specified entity.
     *
     * @param entity The entity
     * @return A lazy optional containing the IMana, if any
     */
    public static ANPlayerDataCap getPlayerDataCap(final LivingEntity entity) {
        if (entity == null)
            return null;
        return entity.getCapability(PLAYER_DATA_CAP);
    }

    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerEntity(MANA_CAPABILITY, EntityType.PLAYER, (player, ctx) -> new ManaCap(player));
        event.registerEntity(PLAYER_DATA_CAP, EntityType.PLAYER, (player, ctx) -> new ANPlayerDataCap(player));
        // TODO(26.1): item handler capabilities (Capabilities.Item.BLOCK, ResourceHandler<ItemResource>) removed; need transfer-API rewrite
        event.registerBlockEntity(MAP_INV_CAP, BlockRegistry.REPOSITORY_CONTROLLER_TILE.get(), (c, side) -> c.getControllerInv());
        event.registerBlockEntity(FILTERSET_CAPABILITY, BlockRegistry.REPOSITORY_CONTROLLER_TILE.get(), (c, side) -> c);
        event.registerBlockEntity(MAP_INV_CAP, BlockRegistry.REPOSITORY_TILE.get(), (c, side) -> c);

        var sourceContainers = List.of(BlockRegistry.SOURCE_JAR_TILE, BlockRegistry.CREATIVE_SOURCE_JAR_TILE,
                BlockRegistry.AGRONOMIC_SOURCELINK_TILE, BlockRegistry.ALCHEMICAL_TILE, BlockRegistry.VITALIC_TILE, BlockRegistry.MYCELIAL_TILE, BlockRegistry.VOLCANIC_TILE,
                BlockRegistry.RELAY_COLLECTOR_TILE, BlockRegistry.RELAY_DEPOSIT_TILE, BlockRegistry.RELAY_WARP_TILE, BlockRegistry.ARCANE_RELAY_TILE,
                BlockRegistry.IMBUEMENT_TILE);

        for (var container : sourceContainers) {
            event.registerBlockEntity(SOURCE_CAPABILITY, container.get(), (sourceJar, side) -> sourceJar.getSourceStorage());
        }

        event.registerBlockEntity(CapabilityRegistry.BLOCK_SPELL_RESOLVE_CAP, BlockRegistry.REPOSITORY_CONTROLLER_TILE.get(), (c, none) -> c.getSpellListener());
        event.registerBlockEntity(CapabilityRegistry.BLOCK_SPELL_RESOLVE_CAP, BlockRegistry.PLANARIUM_TILE.get(), (c, none) -> c.onResolve());
        event.registerBlockEntity(CapabilityRegistry.BLOCK_SPELL_RESOLVE_CAP, BlockRegistry.DECOR_BLOSSOM_TILE.get(), (c, none) -> c);
        event.registerBlock(CapabilityRegistry.WANDABLE_BLOCK_CAPABILITY, (level, pos, state, be, side) -> BlockRegistry.DIM_BOUNDARY.get(), BlockRegistry.DIM_BOUNDARY.get());
    }


    @SuppressWarnings("unused")
    @EventBusSubscriber(modid = ArsNouveau.MODID)
    public static class EventHandler {

        @SubscribeEvent
        public static void onPlayerLoginEvent(PlayerEvent.PlayerLoggedInEvent event) {
            if (event.getEntity() instanceof ServerPlayer) {
                syncPlayerCap(event.getEntity());
            }
        }

        @SubscribeEvent
        public static void respawnEvent(PlayerEvent.PlayerRespawnEvent event) {
            if (event.getEntity() instanceof ServerPlayer) {
                syncPlayerCap(event.getEntity());
            }
        }


        @SubscribeEvent
        public static void onPlayerStartTrackingEvent(PlayerEvent.StartTracking event) {
            if (event.getTarget() instanceof Player && event.getEntity() instanceof ServerPlayer) {
                syncPlayerCap(event.getEntity());
            }
        }

        @SubscribeEvent
        public static void onPlayerDimChangedEvent(PlayerEvent.PlayerChangedDimensionEvent event) {
            if (event.getEntity() instanceof ServerPlayer) {
                syncPlayerCap(event.getEntity());
            }
        }

        public static void syncPlayerCap(Player player) {
            if (player instanceof ServerPlayer serverPlayer) {
                ANPlayerDataCap playerData = getPlayerDataCap(serverPlayer);
                if (playerData != null) {
                    playerData.syncToClient(serverPlayer);
                }
            }
        }
    }
}
