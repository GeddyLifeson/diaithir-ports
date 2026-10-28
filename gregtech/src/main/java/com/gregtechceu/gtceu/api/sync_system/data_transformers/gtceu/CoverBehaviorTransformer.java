package com.gregtechceu.gtceu.api.sync_system.data_transformers.gtceu;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.api.cover.CoverBehavior;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.api.sync_system.data_transformers.ValueTransformer;

import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import org.jetbrains.annotations.Nullable;

public class CoverBehaviorTransformer implements ValueTransformer<CoverBehavior> {

    @Override
    public Tag serializeNBT(@Nullable CoverBehavior value,
                            CoverBehaviorTransformer.TransformerContext<CoverBehavior> context) {
        if (value != null) {
            return serialize(value, context.isClientSync(), context.lookup());
        }
        return new CompoundTag();
    }

    @Override
    public @Nullable CoverBehavior deserializeNBT(Tag tag,
                                                  CoverBehaviorTransformer.TransformerContext<CoverBehavior> context) {
        var compoundTag = ValueTransformer.assertTagType(CompoundTag.class, tag, context);
        if (context.holder() instanceof ICoverable coverable) {
            return deserialize(compoundTag, coverable, context.currentValue(), context.isClientSync(),
                    context.lookup());
        }
        GTCEu.LOGGER.error("Sync: Object attempting to sync cover does not implement ICoverable {}", context);
        return null;
    }

    private CompoundTag serialize(CoverBehavior cover, boolean isSync, HolderLookup.Provider lookup) {
        var compound = new CompoundTag();

        compound.putInt("side", cover.attachedSide.ordinal());
        compound.putString("coverType", cover.coverDefinition.getId().toString());
        CompoundTag serializedCover = cover.getSyncDataHolder().serializeNBT(lookup, isSync);
        compound.put("data", serializedCover);

        return compound;
    }

    public @Nullable CoverBehavior deserialize(CompoundTag tag, ICoverable holder, @Nullable CoverBehavior cover,
                                               boolean isSync, HolderLookup.Provider lookup) { return null; /* ported stub */ }
}
