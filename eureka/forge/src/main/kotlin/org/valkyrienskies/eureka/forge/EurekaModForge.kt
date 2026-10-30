package org.valkyrienskies.eureka.forge

import net.minecraft.core.registries.Registries
import net.neoforged.bus.api.IEventBus
import net.neoforged.fml.ModLoadingContext
import net.neoforged.fml.common.Mod
import net.neoforged.fml.config.ModConfig
import net.neoforged.fml.event.config.ModConfigEvent
import net.neoforged.fml.loading.FMLEnvironment
import net.neoforged.neoforge.registries.DeferredRegister
import org.valkyrienskies.eureka.EurekaConfig
import org.valkyrienskies.eureka.EurekaMod
import org.valkyrienskies.eureka.EurekaMod.init
import org.valkyrienskies.eureka.forge.registry.FuelRegistryImpl
import org.valkyrienskies.eureka.registry.CreativeTabs
import thedarkcolour.kotlinforforge.neoforge.forge.MOD_BUS

// 26.2/NeoForge: kotlinforforge moved to the thedarkcolour.kotlinforforge.neoforge namespace
// (runForDist/LOADING_CONTEXT helpers are gone - use FMLEnvironment.getDist() and
// ModLoadingContext.get().activeContainer directly, matching ValkyrienSkiesModForge).
@Mod(EurekaMod.MOD_ID)
class EurekaModForge {
    init {
        if (FMLEnvironment.getDist().isClient) {
            EurekaModForgeClient.registerClient()
        }
        ModLoadingContext.get().activeContainer.registerConfig(
            ModConfig.Type.SERVER, EurekaConfig.EUREKA_SPEC, "valkyrienskies/vs_eureka.toml"
        )
        MOD_BUS.addListener(::onConfigReload)
        MOD_BUS.addListener(::onConfigLoad)
        FuelRegistryImpl()
        init()

        val deferredRegister = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, EurekaMod.MOD_ID)
        deferredRegister.register("general") { ->
            CreativeTabs.create()
        }
        deferredRegister.register(MOD_BUS)
    }

    companion object {
        // Used by DeferredRegisterImpl.applyAll (Java) to attach registers to the mod bus.
        fun getModBus(): IEventBus = MOD_BUS
    }

    private fun onConfigLoad(event: ModConfigEvent.Loading) {
        applyConfig(event.config)
    }

    private fun onConfigReload(event: ModConfigEvent.Reloading) {
        applyConfig(event.config)
    }

    // 26.2: EurekaConfig.update no longer takes the (forge-only) ModConfig type in common code;
    // it takes a plain key -> value reader backed by the loaded TOML.
    private fun applyConfig(config: ModConfig) {
        if (config.modId == EurekaMod.MOD_ID) {
            val loaded = config.loadedConfig?.config() ?: return
            EurekaConfig.update { key -> loaded.get<Any?>(key) }
        }
    }
}
