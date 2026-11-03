package org.valkyrienskies.tournament

import blitz.Provider
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider
import net.minecraft.core.BlockPos
import net.minecraft.core.registries.Registries
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockState
import org.valkyrienskies.tournament.blockentity.*
import org.valkyrienskies.tournament.blockentity.explosive.ExplosiveBlockEntity
import org.valkyrienskies.tournament.blockentity.render.PropellerBlockEntityRender
import org.valkyrienskies.tournament.blockentity.render.RotatorBlockEntityRender
import org.valkyrienskies.tournament.blockentity.render.TournamentRenderState
import org.valkyrienskies.tournament.blockentity.render.TransparentFuelTankBlockEntityRender
import org.valkyrienskies.tournament.registry.DeferredRegister
import org.valkyrienskies.tournament.registry.RegistrySupplier
import org.valkyrienskies.tournament.util.extension.once
import kotlin.reflect.KProperty

object TournamentBlockEntities {
    private val BLOCKENTITIES = DeferredRegister.create(TournamentMod.MOD_ID, Registries.BLOCK_ENTITY_TYPE)

    private val renderers = mutableListOf<RendererEntry<*>>()

    val CONNECTOR = TournamentBlocks.CONNECTOR
        .withBE(::ConnectorBlockEntity)
        .byName("connector")

    val THRUSTER = TournamentBlocks.THRUSTER
        .withBE(::ThrusterBlockEntity)
        .byName("thruster")

    val SENSOR = TournamentBlocks.SENSOR
        .withBE(::SensorBlockEntity)
        .byName("sensor")

    val ROPE_HOOK = TournamentBlocks.ROPE_HOOK
        .withBE(::RopeHookBlockEntity)
        .byName("rope_hook")

    val PROP_BIG = TournamentBlocks.PROP_BIG
        .withBE(::BigPropellerBlockEntity)
        .byName("prop_big")
        .withRenderer(object : RenderProviderProvider<BigPropellerBlockEntity> {
            override fun get() = BlockEntityRendererProvider<BigPropellerBlockEntity, TournamentRenderState> {
                PropellerBlockEntityRender<BigPropellerBlockEntity>(
                    TournamentModels.PROP_BIG
                )
            }
        })

    val PROP_SMALL = TournamentBlocks.PROP_SMALL
        .withBE(::SmallPropellerBlockEntity)
        .byName("prop_small")
        .withRenderer(object : RenderProviderProvider<SmallPropellerBlockEntity> {
            override fun get() = BlockEntityRendererProvider<SmallPropellerBlockEntity, TournamentRenderState> {
                PropellerBlockEntityRender<SmallPropellerBlockEntity>(
                    TournamentModels.PROP_SMALL
                )
            }
        })

    val CHUNK_LOADER = TournamentBlocks.CHUNK_LOADER
        .withBE(::ChunkLoaderBlockEntity)
        .byName("chunk_loader")

    val EXPLOSIVE = TournamentBlocks.EXPLOSIVE_INSTANT_SMALL
        .withBE(::ExplosiveBlockEntity)
        .byName("explosive_instant_small")

    val FUEL_TANK_FULL_SOLID by rec { self ->
        TournamentBlocks.FUEL_TANK_FULL_SOLID
            .withBE { p, s -> FuelTankBlockEntity(p, s, capf = 1.0f, self) }
            .byName("fuel_tank_full_solid")
    }

    val FUEL_TANK_FULL_TRANSPARENT by rec { self ->
        TournamentBlocks.FUEL_TANK_FULL_TRANSPARENT
            .withBE { p, s -> FuelTankBlockEntity(p, s, capf = 1.0f, self) }
            .byName("fuel_tank_full_transparent")
            .withRenderer(object : RenderProviderProvider<FuelTankBlockEntity> {
                override fun get() = BlockEntityRendererProvider<FuelTankBlockEntity, TournamentRenderState> { TransparentFuelTankBlockEntityRender() }
            })
    }

    val FUEL_TANK_HALF_SOLID by rec { self ->
        TournamentBlocks.FUEL_TANK_HALF_SOLID
            .withBE { p, s -> FuelTankBlockEntity(p, s, capf = 0.5f, self) }
            .byName("fuel_tank_half_solid")
    }

    val ROTATOR = TournamentBlocks.ROTATOR
        .withBE(::RotatorBlockEntity)
        .byName("rotator")
        .withRenderer(object : RenderProviderProvider<RotatorBlockEntity> {
            override fun get() = BlockEntityRendererProvider<RotatorBlockEntity, TournamentRenderState> { RotatorBlockEntityRender() }
        })

    val register by once {
        BLOCKENTITIES.applyAll()
    }

    private infix fun <T : BlockEntity> Set<RegistrySupplier<out Block>>.withBE(blockEntity: (BlockPos, BlockState) -> T) =
        Pair(this, blockEntity)

    private infix fun <T : BlockEntity> RegistrySupplier<out Block>.withBE(blockEntity: (BlockPos, BlockState) -> T) =
        setOf(this).withBE(blockEntity)

    private data class RendererEntry<T: BlockEntity>(
        val type: RegistrySupplier<BlockEntityType<T>>,
        val renderer: RenderProviderProvider<T>
    ) {
        @Suppress("UNCHECKED_CAST")
        fun register(event: net.neoforged.neoforge.client.event.EntityRenderersEvent.RegisterRenderers) {
            event.registerBlockEntityRenderer(
                type.get(),
                renderer.get() as BlockEntityRendererProvider<T, net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState>
            )
        }
    }

    fun initClientRenderers(event: net.neoforged.neoforge.client.event.EntityRenderersEvent.RegisterRenderers) {
        renderers.forEach {
            it.register(event)
        }
    }

    private infix fun <T : BlockEntity> Pair<Set<RegistrySupplier<out Block>>, (BlockPos, BlockState) -> T>.byName(name: String): RegistrySupplier<BlockEntityType<T>> =
        BLOCKENTITIES.register(name) {
            BlockEntityType(this.second, this.first.map { it.get() }.toSet())
        }

    private infix fun <T : BlockEntity> RegistrySupplier<BlockEntityType<T>>.withRenderer(renderer: RenderProviderProvider<T>) =
        this.also {
            renderers += RendererEntry(it, renderer)
        }

    class rec<T>(fn: (Provider<T>) -> RegistrySupplier<T>) {
        val value: RegistrySupplier<T> = fn { value.get() }

        operator fun getValue(thisRef: Any?, property: KProperty<*>): RegistrySupplier<T> {
            return value
        }
    }

    // because class loading!!!
    interface RenderProviderProvider<T: BlockEntity> {
        val a: Int get() = 1 // keep!!!
        fun get(): BlockEntityRendererProvider<T, *>
    }
}
