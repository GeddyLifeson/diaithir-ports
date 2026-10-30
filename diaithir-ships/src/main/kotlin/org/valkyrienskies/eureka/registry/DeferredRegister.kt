package org.valkyrienskies.eureka.registry

import net.minecraft.core.Registry
import net.minecraft.resources.ResourceKey
import net.neoforged.bus.api.IEventBus
import net.neoforged.neoforge.registries.DeferredHolder
import org.valkyrienskies.eureka.EurekaMod
import net.neoforged.neoforge.registries.DeferredRegister as NeoDeferredRegister

/** Thin wrapper over NeoForge's DeferredRegister that keeps the original Eureka registration call sites. */
class DeferredRegister<T : Any> private constructor(modId: String, registry: ResourceKey<out Registry<T>>) : Iterable<RegistrySupplier<T>> {
    private val inner: NeoDeferredRegister<T> = NeoDeferredRegister.create(registry, modId)

    fun <I : T> register(name: String, builder: () -> I): RegistrySupplier<I> {
        val holder: DeferredHolder<T, I> = inner.register(name, java.util.function.Function { id ->
            currentId.set(id)
            try { builder() } finally { currentId.remove() }
        })
        return object : RegistrySupplier<I> {
            override val name: String get() = name
            override fun get(): I = holder.get()
        }
    }

    fun applyAll() = inner.register(EurekaMod.modBus)

    override fun iterator(): Iterator<RegistrySupplier<T>> =
        inner.entries.map { h ->
            object : RegistrySupplier<T> {
                override val name: String get() = h.id.path
                override fun get(): T = h.get()
            } as RegistrySupplier<T>
        }.iterator()

    companion object {
        /** Id of the object currently being constructed, so block/item properties can carry it (required since 1.21.2). */
        val currentId = ThreadLocal<net.minecraft.resources.Identifier>()

        fun blockProps(): net.minecraft.world.level.block.state.BlockBehaviour.Properties =
            net.minecraft.world.level.block.state.BlockBehaviour.Properties.of()
                .setId(ResourceKey.create(net.minecraft.core.registries.Registries.BLOCK, currentId.get()))

        fun itemProps(): net.minecraft.world.item.Item.Properties =
            net.minecraft.world.item.Item.Properties()
                .setId(ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, currentId.get()))

        fun <T : Any> create(id: String, registry: ResourceKey<out Registry<T>>): DeferredRegister<T> = DeferredRegister(id, registry)
    }
}
