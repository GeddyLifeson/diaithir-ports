package org.valkyrienskies.mod.util

import net.minecraft.core.HolderLookup
import net.minecraft.nbt.CompoundTag
import net.minecraft.util.ProblemReporter
import net.minecraft.world.Clearable
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.storage.TagValueInput

/** 26.1 replacement for `BlockEntity.loadWithComponents(CompoundTag, HolderLookup.Provider)`. */
fun BlockEntity.loadWithComponents(tag: CompoundTag, registries: HolderLookup.Provider) {
    loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING, registries, tag))
}

/** Empties this block entity so removing it does not drop its contents (replacement for `Clearable.tryClear`). */
fun BlockEntity.clearContentsForRelocation(registries: HolderLookup.Provider) {
    if (this is Clearable) {
        clearContent()
    } else {
        loadWithComponents(CompoundTag(), registries)
    }
}
