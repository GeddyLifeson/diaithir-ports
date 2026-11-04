package net.diaithir.ships.cargo

import net.neoforged.bus.api.IEventBus
import org.slf4j.LoggerFactory

/** Entry point for ship cargo. Does nothing when the containers API (net.diaithir.freight, shipped in diaithir-carts) is absent. */
object ShipCargoMod {
    var apiPresent = false
        private set

    fun init(bus: IEventBus) {
        apiPresent = try { Class.forName("net.diaithir.freight.CargoCarrier"); true } catch (e: Throwable) { false }
        if (!apiPresent) { LoggerFactory.getLogger("vs_yard").info("ship cargo: containers API absent, ships do not carry cargo"); return }
        CargoImpl.init(bus)
        if (net.neoforged.fml.loading.FMLEnvironment.getDist().isClient) {
            net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent.LoggingOut::class.java) { ClientCargo.clear() }
        }
    }

    /** Called from the payload registration; the packet exists only with the API. */
    fun registerPayload(registrar: net.neoforged.neoforge.network.registration.PayloadRegistrar) {
        if (!apiPresent) return
        registrar.playToClient(ShipCargoPacket.TYPE, ShipCargoPacket.CODEC) { p, ctx -> ctx.enqueueWork { CargoImpl.receive(p) } }
    }
}
