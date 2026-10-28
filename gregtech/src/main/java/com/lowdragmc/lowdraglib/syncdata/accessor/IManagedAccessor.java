package com.lowdragmc.lowdraglib.syncdata.accessor;

import com.gregtechceu.gtceu.compat.INBTSerializable;

import com.lowdragmc.lowdraglib.syncdata.AccessorOp;
import com.lowdragmc.lowdraglib.syncdata.IManaged;
import com.lowdragmc.lowdraglib.syncdata.IManagedStorage;
import com.lowdragmc.lowdraglib.syncdata.TypedPayloadRegistries;
import com.lowdragmc.lowdraglib.syncdata.managed.IRef;
import com.lowdragmc.lowdraglib.syncdata.payload.ITypedPayload;
import com.lowdragmc.lowdraglib.syncdata.payload.NbtTagPayload;
import com.lowdragmc.lowdraglib.utils.TagUtils;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;

import java.util.BitSet;

public class IManagedAccessor extends ReadonlyAccessor {

    @Override
    public boolean hasPredicate() {
        return true;
    }

    @Override
    public boolean test(Class<?> type) {
        return IManaged.class.isAssignableFrom(type);
    }

    @Override
    public ITypedPayload<?> readFromReadonlyField(AccessorOp op, Object obj, HolderLookup.Provider provider) {
        if(!(obj instanceof IManaged managed)) {
            throw new IllegalArgumentException("Field %s is not INBTSerializable".formatted(obj));
        }

        CompoundTag tag;
        if (op == AccessorOp.SYNCED || op == AccessorOp.FORCE_SYNCED) {
            tag = readSyncedFields(managed, new CompoundTag(), op == AccessorOp.FORCE_SYNCED, provider);
        } else {
            tag = readManagedFields(managed, new CompoundTag(), provider);
        }
        return new NbtTagPayload().setPayload(tag);
    }

    public static CompoundTag readSyncedFields(IManaged managed, CompoundTag tag, boolean force, HolderLookup.Provider provider) {
        BitSet changed = new BitSet();
        var syncedFields = managed.getSyncStorage().getSyncFields();
        var list = new ListTag();
        for (int i = 0; i < syncedFields.length; i++) {
            var field = syncedFields[i];
            if (force || field.isSyncDirty()) {
                changed.set(i);
                var key = field.getKey();

                var payload = key.readSyncedField(field, force, provider);
                CompoundTag payloadTag = new CompoundTag();
                payloadTag.putByte("t", payload.getType());
                var data = payload.serializeNBT(provider);
                if (data != null) {
                    payloadTag.put("d", data);
                }
                list.add(payloadTag);

                field.clearSyncDirty();
            }
        }
        tag.putByteArray("c", changed.toByteArray());
        tag.put("l", list);
        return tag;
    }

    public static CompoundTag readManagedFields(IManaged managed, CompoundTag tag, HolderLookup.Provider provider) {
        var persistedFields = managed.getSyncStorage().getPersistedFields();
        for (var persistedField : persistedFields) {
            var fieldKey = persistedField.getKey();
            var key = persistedField.getPersistedKey();

            var nbt = fieldKey.readPersistedField(persistedField, provider);

            if (nbt != null) {
                TagUtils.setTagExtended(tag, key, nbt);
            }
        }
        return tag;
    }

    @Override
    public void writeToReadonlyField(AccessorOp op, Object obj, ITypedPayload<?> payload, HolderLookup.Provider provider) {  /* ported stub */ }

    public static void writePersistedFields(CompoundTag tag, IRef[] refs, HolderLookup.Provider provider) {
        for (var ref : refs) {
            var fieldKey = ref.getKey();
            var key = ref.getPersistedKey();

            var nbt = TagUtils.getTagExtended(tag, key);
            if (nbt != null) {
                fieldKey.writePersistedField(ref, nbt, provider);
            }
        }
    }

    public static void writeSyncedFields(IManagedStorage storage, IRef[] syncedFields, BitSet changed, ITypedPayload<?>[] payloads, HolderLookup.Provider provider) {
        int j = 0;
        for (int i = 0; i < changed.length(); i++) {
            if (changed.get(i)) {
                var field = syncedFields[i];
                var key = field.getKey();

                boolean hasListener = storage.hasSyncListener(key);
                Object oldValue = null;
                if (hasListener) {
                    oldValue = field.readRaw();
                }
                key.writeSyncedField(field, payloads[j], provider);
                if(hasListener) {
                    storage.notifyFieldUpdate(key, field.readRaw(), oldValue);
                }

                j++;
            }
        }
    }

}
