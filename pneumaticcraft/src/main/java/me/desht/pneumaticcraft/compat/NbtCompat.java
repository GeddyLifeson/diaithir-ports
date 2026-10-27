package me.desht.pneumaticcraft.compat;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.common.util.ValueIOSerializable;

/**
 * Bridges the 1.21-style CompoundTag based save/load code of this mod to the 26.1 ValueInput/ValueOutput API.
 */
public final class NbtCompat {
    private NbtCompat() {}

    public static CompoundTag save(ValueIOSerializable s, HolderLookup.Provider provider) {
        TagValueOutput out = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, provider == null ? RegistryAccess.EMPTY : provider);
        s.serialize(out);
        return out.buildResult();
    }

    public static void load(ValueIOSerializable s, HolderLookup.Provider provider, CompoundTag tag) {
        s.deserialize(input(provider, tag));
    }

    public static ValueInput input(HolderLookup.Provider provider, CompoundTag tag) {
        return TagValueInput.create(ProblemReporter.DISCARDING, provider == null ? RegistryAccess.EMPTY : provider, tag);
    }

    public static TagValueOutput output(HolderLookup.Provider provider) {
        return TagValueOutput.createWithContext(ProblemReporter.DISCARDING, provider == null ? RegistryAccess.EMPTY : provider);
    }

    public static CompoundTag toTag(ValueInput in) {
        return in.read(MapCodec.assumeMapUnsafe(CompoundTag.CODEC)).orElseGet(CompoundTag::new);
    }

    public static HolderLookup.Provider registries(net.minecraft.world.level.Level level) {
        return level == null ? RegistryAccess.EMPTY : level.registryAccess();
    }

    private static HolderLookup.Provider prov(HolderLookup.Provider p) {
        return p == null ? RegistryAccess.EMPTY : p;
    }

    public static net.minecraft.nbt.Tag saveItem(HolderLookup.Provider p, net.minecraft.world.item.ItemStack stack) {
        return net.minecraft.world.item.ItemStack.OPTIONAL_CODEC.encodeStart(prov(p).createSerializationContext(net.minecraft.nbt.NbtOps.INSTANCE), stack).result().orElseGet(CompoundTag::new);
    }

    public static net.minecraft.world.item.ItemStack parseItem(HolderLookup.Provider p, net.minecraft.nbt.Tag tag) {
        return net.minecraft.world.item.ItemStack.OPTIONAL_CODEC.parse(prov(p).createSerializationContext(net.minecraft.nbt.NbtOps.INSTANCE), tag).result().orElse(net.minecraft.world.item.ItemStack.EMPTY);
    }

    /** Equivalent of the old ItemStack#save(provider, tag): adds the stack's fields to the given tag. */
    public static CompoundTag saveItemInto(HolderLookup.Provider p, net.minecraft.world.item.ItemStack stack, CompoundTag into) {
        if (saveItem(p, stack) instanceof CompoundTag ct) into.merge(ct);
        return into;
    }

    public static net.minecraft.nbt.Tag saveFluid(HolderLookup.Provider p, net.neoforged.neoforge.fluids.FluidStack stack) {
        return net.neoforged.neoforge.fluids.FluidStack.OPTIONAL_CODEC.encodeStart(prov(p).createSerializationContext(net.minecraft.nbt.NbtOps.INSTANCE), stack).result().orElseGet(CompoundTag::new);
    }

    public static net.neoforged.neoforge.fluids.FluidStack parseFluid(HolderLookup.Provider p, net.minecraft.nbt.Tag tag) {
        return net.neoforged.neoforge.fluids.FluidStack.OPTIONAL_CODEC.parse(prov(p).createSerializationContext(net.minecraft.nbt.NbtOps.INSTANCE), tag).result().orElse(net.neoforged.neoforge.fluids.FluidStack.EMPTY);
    }
}
