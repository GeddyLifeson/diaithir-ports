package org.valkyrienskies.eureka

import net.neoforged.bus.api.IEventBus
import org.valkyrienskies.mod.common.ValkyrienSkiesMod

object EurekaMod {
    const val MOD_ID = "vs_eureka"

    lateinit var modBus: IEventBus

    @JvmStatic
    fun init(bus: IEventBus, container: net.neoforged.fml.ModContainer) {
        modBus = bus
        EurekaBlocks.register()
        EurekaBlockEntities.register()
        EurekaItems.register()
        EurekaScreens.register()
        EurekaEntities.register()
        EurekaWeights.register()
        val spec = EurekaConfig.buildSpec()
        container.registerConfig(net.neoforged.fml.config.ModConfig.Type.SERVER, spec, "vs_eureka-server.toml")
        bus.addListener<net.neoforged.fml.event.config.ModConfigEvent.Loading> { if (it.config.spec === spec) EurekaConfig.apply() }
        bus.addListener<net.neoforged.fml.event.config.ModConfigEvent.Reloading> { if (it.config.spec === spec) EurekaConfig.apply() }
        ValkyrienSkiesMod.vsCore.registerAttachment(org.valkyrienskies.eureka.ship.EurekaShipControl::class.java)
    }

    @JvmStatic
    fun initClient(bus: IEventBus) {
        bus.addListener<net.neoforged.neoforge.client.event.RegisterMenuScreensEvent> { EurekaClientScreens.register(it) }
        bus.addListener<net.neoforged.neoforge.client.event.EntityRenderersEvent.RegisterRenderers> {
            it.registerBlockEntityRenderer(EurekaBlockEntities.SHIP_HELM.get()) { ctx ->
                org.valkyrienskies.eureka.blockentity.renderer.ShipHelmBlockEntityRenderer(ctx)
            }
        }
        bus.addListener<net.neoforged.neoforge.client.event.ModelEvent.RegisterStandalone> {
            org.valkyrienskies.eureka.blockentity.renderer.WheelModels.register(it)
        }
    }
}
