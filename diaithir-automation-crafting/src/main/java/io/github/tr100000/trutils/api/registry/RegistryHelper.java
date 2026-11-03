package io.github.tr100000.trutils.api.registry;

import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;

/**
 * Collects objects and registers them when the platform's register event fires (call {@link #flush()} from it).
 * Derived from TrUtils (MIT, Tr100000).
 * @param <T> the type of thing to be registered
 */
public class RegistryHelper<T> {
    protected final BiConsumer<Identifier, T> registerAction;
    protected final String modid;
    protected final Map<Identifier, T> pending = new LinkedHashMap<>();

    public RegistryHelper(BiConsumer<Identifier, T> registerAction, String modid) {
        this.registerAction = Objects.requireNonNull(registerAction, "registerAction is null");
        this.modid = Objects.requireNonNull(modid, "modid is null");
    }

    public RegistryHelper(Registry<? super T> registry, String modid) {
        this((id, object) -> Registry.register(registry, id, object), modid);
    }

    public <R extends T> R add(R object, String name) {
        pending.put(Identifier.fromNamespaceAndPath(modid, Objects.requireNonNull(name, "name is null")), Objects.requireNonNull(object, "object is null"));
        return object;
    }

    public void flush() {
        pending.forEach(registerAction);
        pending.clear();
    }
}
