package com.simibubi.create;

import com.simibubi.create.foundation.data.CreateRegistrate;
import com.tterrag.registrate.util.entry.RegistryEntry;
import com.zurrtum.create.api.contraption.storage.item.MountedItemStorageType;
import com.zurrtum.create.api.registry.CreateRegistries;
import com.zurrtum.create.api.registry.CreateRegistryKeys;
import net.neoforged.neoforge.registries.DeferredHolder;

/** Registrate-entry view of the port's mounted storage types (fields the addons reference). */
public class AllMountedStorageTypes {
    private static final CreateRegistrate OWNER = CreateRegistrate.create("create");

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static final RegistryEntry VAULT = new RegistryEntry(OWNER,
        DeferredHolder.create(CreateRegistryKeys.MOUNTED_ITEM_STORAGE_TYPE,
            CreateRegistries.MOUNTED_ITEM_STORAGE_TYPE.getKey((MountedItemStorageType) com.zurrtum.create.AllMountedStorageTypes.VAULT)));
}
