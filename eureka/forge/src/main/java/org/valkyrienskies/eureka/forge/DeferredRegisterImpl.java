package org.valkyrienskies.eureka.forge;

import java.util.Iterator;
import kotlin.jvm.functions.Function0;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.jetbrains.annotations.NotNull;
import org.valkyrienskies.eureka.registry.DeferredRegister;
import org.valkyrienskies.eureka.registry.RegistrySupplier;

// 26.2/NeoForge: net.minecraftforge.registries.DeferredRegister/RegistryObject became
// net.neoforged.neoforge.registries.DeferredRegister/DeferredHolder (create() accepts the
// ResourceKey directly, no .location() unwrap needed).
public class DeferredRegisterImpl<T> implements DeferredRegister<T> {
    private final net.neoforged.neoforge.registries.DeferredRegister<T> forge;

    public DeferredRegisterImpl(final String modId, final ResourceKey<Registry<T>> registry) {
        forge = net.neoforged.neoforge.registries.DeferredRegister.create(registry, modId);
    }

    @NotNull
    @Override
    public <I extends T> RegistrySupplier<I> register(
            @NotNull final String name,
            @NotNull final Function0<? extends I> builder
    ) {
        final DeferredHolder<T, I> result = forge.register(name, builder::invoke);

        return new RegistrySupplier<>() {
            @NotNull
            @Override
            public String getName() {
                return name;
            }

            @Override
            public I get() {
                return result.get();
            }
        };
    }

    @Override
    public void applyAll() {
        forge.register(EurekaModForge.Companion.getModBus());
    }

    @NotNull
    @Override
    public Iterator<RegistrySupplier<T>> iterator() {
        final Iterator<DeferredHolder<T, ? extends T>> iterator = forge.getEntries().iterator();

        return new Iterator<>() {
            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public RegistrySupplier<T> next() {
                final DeferredHolder<T, ? extends T> result = iterator.next();

                return new RegistrySupplier<>() {
                    @NotNull
                    @Override
                    public String getName() {
                        return result.getId().getPath();
                    }

                    @Override
                    public T get() {
                        return result.get();
                    }
                };
            }
        };
    }
}
