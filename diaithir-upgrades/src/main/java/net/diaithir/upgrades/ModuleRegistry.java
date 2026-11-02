package net.diaithir.upgrades;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/** Module id to spec. Modules register at load (item registration or data), by id, never renumbered. */
public final class ModuleRegistry {
    private static final Map<String, ModuleSpec> MODULES = new ConcurrentHashMap<>();

    private ModuleRegistry() {}

    public static void register(ModuleSpec spec) { MODULES.put(spec.id(), spec); }

    public static Optional<ModuleSpec> get(String id) { return Optional.ofNullable(MODULES.get(id)); }
}
