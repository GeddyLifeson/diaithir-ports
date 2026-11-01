package org.diaithir.diesel.content;

import net.minecraft.world.item.Item;
import org.diaithir.diesel.registries.CDGItems;

import java.util.function.Supplier;

/** Bolt-on engine upgrades. The turbocharger raises speed and total stress; the silencer only quietens the engine. */
public enum EngineUpgrade {
    NONE("none", 1f, null),
    SILENCER("silencer", 1f, () -> CDGItems.ENGINE_SILENCER.get()),
    TURBO("turbocharger", 1.5f, () -> CDGItems.ENGINE_TURBO.get());

    public final String id;
    public final float multiplier;
    private final Supplier<Item> item;

    EngineUpgrade(String id, float multiplier, Supplier<Item> item) {
        this.id = id;
        this.multiplier = multiplier;
        this.item = item;
    }

    public Item item() {
        return item == null ? null : item.get();
    }

    public static EngineUpgrade byId(String id) {
        for (EngineUpgrade u : values()) if (u.id.equals(id)) return u;
        return NONE;
    }

    public static EngineUpgrade forItem(Item it) {
        for (EngineUpgrade u : values()) if (u.item != null && u.item.get() == it) return u;
        return NONE;
    }
}
