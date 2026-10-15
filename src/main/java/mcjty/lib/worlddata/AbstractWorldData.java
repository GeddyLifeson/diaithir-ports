package mcjty.lib.worlddata;

import mcjty.lib.McJtyLib;
import mcjty.lib.varia.LevelTools;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import javax.annotation.Nonnull;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Global world data (always attached to the overworld)
 */
public abstract class AbstractWorldData<T extends AbstractWorldData<T>> extends SavedData {

    // 26.1 saved data is codec based; one type per data name, built around the CompoundTag save/load the subclasses keep
    private static final Map<String, SavedDataType<?>> TYPES = new HashMap<>();

    protected AbstractWorldData() {
        super();
    }

    public void save() {
        setDirty();
    }

    public abstract CompoundTag save(CompoundTag tag, HolderLookup.Provider provider);

    @Nonnull
    @SuppressWarnings("unchecked")
    public static <T extends AbstractWorldData<T>> T getData(Level world, Function<CompoundTag, T> loader, Supplier<T> supplier, String name) {
        if (world.isClientSide()) {
            throw new RuntimeException("Don't access this client-side!");
        }
        ServerLevel overworld = LevelTools.getOverworld(world);
        HolderLookup.Provider provider = overworld.registryAccess();
        SavedDataType<T> type = (SavedDataType<T>) TYPES.computeIfAbsent(name, n -> new SavedDataType<>(
                Identifier.fromNamespaceAndPath(McJtyLib.MODID, n.toLowerCase(Locale.ROOT)), supplier,
                CompoundTag.CODEC.xmap(loader, data -> data.save(new CompoundTag(), provider))));
        return overworld.getDataStorage().computeIfAbsent(type);
    }

}
