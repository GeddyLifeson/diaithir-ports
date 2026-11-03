package org.valkyrienskies.tournament

import org.valkyrienskies.core.api.VsBeta
import org.valkyrienskies.core.api.attachment.getAttachment
import org.valkyrienskies.core.api.attachment.removeAttachment
import org.valkyrienskies.core.api.ships.LoadedServerShip
import org.valkyrienskies.core.api.util.GameTickOnly
import org.valkyrienskies.mod.api.vsApi
import org.valkyrienskies.tournament.ship.*
import org.valkyrienskies.tournament.util.extension.with

// TODO: remove chunkloader if can't fix
// TODO: replace all "vs_tournament" with Tournament.MOD_ID

@OptIn(GameTickOnly::class, VsBeta::class)
private fun migrateShipController(ship: LoadedServerShip) {
    val thrusterShipCtrl = ship.getAttachment<ThrusterShipControl>()
    if (thrusterShipCtrl != null) {
        TournamentShips.getOrCreate(ship).addThrustersV1(thrusterShipCtrl.Thrusters.with(thrusterShipCtrl.thrusters))
        ship.removeAttachment<ThrusterShipControl>()
    }

    val balloonShipCtrl = ship.getAttachment<BalloonShipControl>()
    if (balloonShipCtrl != null) {
        TournamentShips.getOrCreate(ship).addBalloons(balloonShipCtrl.balloons)
        ship.removeAttachment<BalloonShipControl>()
    }

    val spinnerShipCtrl = ship.getAttachment<SpinnerShipControl>()
    if (spinnerShipCtrl != null) {
        TournamentShips.getOrCreate(ship).addSpinners(spinnerShipCtrl.spinners.with(spinnerShipCtrl.Spinners))
        ship.removeAttachment<SpinnerShipControl>()
    }

    val pulsesShipCtrl = ship.getAttachment<PulseShipControl>()
    if (pulsesShipCtrl != null) {
        pulsesShipCtrl.addToNew(TournamentShips.getOrCreate(ship))
        ship.removeAttachment<PulseShipControl>()
    }
}

object TournamentMod {
    const val MOD_ID = "vs_tournament"

    lateinit var modBus: net.neoforged.bus.api.IEventBus

    @OptIn(VsBeta::class, GameTickOnly::class)
    @JvmStatic
    fun init(bus: net.neoforged.bus.api.IEventBus, container: net.neoforged.fml.ModContainer) {
        modBus = bus
        TournamentFuels.postFuelsReloaded // class init: registers the reload broadcast
        TournamentNetworking.register
        TournamentBlocks.register
        TournamentBlockEntities.register
        TournamentItems.register
        TournamentWeights.register
        TournamentTriggers.register

        // creative tab
        val tabs = org.valkyrienskies.tournament.registry.DeferredRegister.create(
            MOD_ID, net.minecraft.core.registries.Registries.CREATIVE_MODE_TAB)
        tabs.register(TournamentItems.TAB.identifier().path) { org.valkyrienskies.tournament.registry.CreativeTabs.create() }
        tabs.applyAll()

        // config
        TournamentConfigUpdater.ALL_CONFIGS.forEach {
            container.registerConfig(it.modConfigType(), it.spec, it.path)
        }
        bus.addListener<net.neoforged.fml.event.config.ModConfigEvent.Loading> { TournamentConfigUpdater.update(it.config) }
        bus.addListener<net.neoforged.fml.event.config.ModConfigEvent.Reloading> { TournamentConfigUpdater.update(it.config) }

        // game events (these replace the original mixin-based hooks)
        val game = net.neoforged.neoforge.common.NeoForge.EVENT_BUS
        game.addListener<net.neoforged.neoforge.event.AddServerReloadListenersEvent>(TournamentFuels::addReloadListener)
        game.addListener<net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent>(TournamentFuels::onPlayerJoin)
        game.addListener<net.neoforged.neoforge.event.server.ServerStartedEvent>(TournamentFuels::onServerStarted)
        game.addListener<net.neoforged.neoforge.event.server.ServerStoppedEvent>(TournamentFuels::onServerStopped)
        game.addListener<net.neoforged.neoforge.event.tick.ServerTickEvent.Post> { TickScheduler.tickServer(it.server) }

        vsApi.registerAttachment(BalloonShipControl::class.java)
        vsApi.registerAttachment(PulseShipControl::class.java)
        vsApi.registerAttachment(SpinnerShipControl::class.java)
        vsApi.registerAttachment(ThrusterShipControl::class.java)
        vsApi.registerAttachment(tournamentShipControl::class.java)
        vsApi.registerAttachment(TournamentShips::class.java)

        vsApi.shipLoadEvent.on { e ->
            TournamentShips.pending.remove(e.ship.id)?.let { if (e.ship.getAttachment<TournamentShips>() == null) e.ship.setAttachment(it) }
            migrateShipController(e.ship)
        }
    }

    @JvmStatic
    fun initClient(bus: net.neoforged.bus.api.IEventBus) {
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener<net.neoforged.neoforge.event.entity.player.ItemTooltipEvent>(TournamentFuels::onTooltip)
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener<net.neoforged.neoforge.client.event.ClientTickEvent.Post>(ClientShaftMan::clientTick)
        bus.addListener<net.neoforged.neoforge.client.event.EntityRenderersEvent.RegisterRenderers>(TournamentBlockEntities::initClientRenderers)
        bus.addListener<net.neoforged.neoforge.client.event.ModelEvent.RegisterStandalone>(TournamentModels::register)
    }
}
