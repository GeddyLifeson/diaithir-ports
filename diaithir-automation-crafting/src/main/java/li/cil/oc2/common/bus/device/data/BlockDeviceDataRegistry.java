/* SPDX-License-Identifier: MIT */

package li.cil.oc2.common.bus.device.data;

import li.cil.oc2.api.API;
import li.cil.oc2.api.bus.device.data.BlockDeviceData;
import li.cil.oc2.api.util.Registries;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import javax.annotation.Nullable;
import java.util.stream.Stream;

@SuppressWarnings("unused")
public final class BlockDeviceDataRegistry {
    private static final DeferredRegister<BlockDeviceData> INITIALIZER = DeferredRegister.create(Registries.BLOCK_DEVICE_DATA, API.MOD_ID);

    ///////////////////////////////////////////////////////////////////

    private static final Registry<BlockDeviceData> REGISTRY = INITIALIZER.makeRegistry(builder -> {});

    ///////////////////////////////////////////////////////////////////

    public static final DeferredHolder<BlockDeviceData, BuildrootBlockDeviceData> BUILDROOT = INITIALIZER.register("buildroot", BuildrootBlockDeviceData::new);

    ///////////////////////////////////////////////////////////////////

    public static void initialize(IEventBus modBus) {
        INITIALIZER.register(modBus);
    }

    @Nullable
    public static Identifier getKey(final BlockDeviceData data) {
        Identifier location = REGISTRY.getKey(data);
        if (location == null) {
            location = FileSystems.getKeyByValue(data);
        }
        return location;
    }

    @Nullable
    public static BlockDeviceData getValue(final Identifier location) {
        final BlockDeviceData value = REGISTRY.getValue(location);
        if (value != null) {
            return value;
        }
        return FileSystems.getBlockData().get(location);
    }

    public static Stream<BlockDeviceData> values() {
        return Stream.concat(
            REGISTRY.stream(),
            FileSystems.getBlockData().values().stream());
    }
}
