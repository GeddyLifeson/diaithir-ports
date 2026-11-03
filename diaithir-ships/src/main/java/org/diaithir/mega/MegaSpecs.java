package org.diaithir.mega;

import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/** Full {@link MegaSpec} rows of data/megamachines/catalog.json, by id. {@link MegaCatalog} keeps only the cab's flags. */
public final class MegaSpecs {
    private static final Map<String, MegaSpec> BY_ID = new HashMap<>();

    static {
        try (var in = MegaSpecs.class.getResourceAsStream("/data/megamachines/catalog.json")) {
            for (MegaSpec s : MegaSpec.load(new InputStreamReader(in))) BY_ID.put(s.id(), s);
        } catch (java.io.IOException | RuntimeException ex) {
            throw new IllegalStateException("megamachines catalog unreadable", ex);
        }
    }

    private MegaSpecs() {}

    public static MegaSpec get(String id) { return BY_ID.get(id); }
}
