package org.valkyrienskies.mod.common.util

import net.minecraft.core.HolderLookup
import net.minecraft.nbt.CompoundTag
import net.minecraft.util.ProblemReporter
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.storage.TagValueInput
import net.minecraft.world.level.storage.TagValueOutput

/**
 * 26.2 changed BlockEntity serialization from raw [CompoundTag] to the [ValueInput]/[ValueOutput]
 * abstraction:
 *  - `loadWithComponents(CompoundTag, Provider)` -> `loadWithComponents(ValueInput)`
 *  - `saveWithFullMetadata(Provider): CompoundTag` -> `saveWithFullMetadata(ValueOutput): Unit`
 *
 * VS2 copies block entities between the world and the shipyard by round-tripping through NBT, so
 * these extensions preserve the old CompoundTag-based call sites by bridging through
 * [TagValueInput]/[TagValueOutput]. Problems during (de)serialization are discarded, matching the
 * previous best-effort behaviour.
 */
fun BlockEntity.loadWithComponents(tag: CompoundTag, registries: HolderLookup.Provider) {
    val input = TagValueInput.create(ProblemReporter.DISCARDING, registries, tag)
    this.loadWithComponents(input)
}

fun BlockEntity.saveWithFullMetadata(registries: HolderLookup.Provider): CompoundTag {
    val output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, registries)
    this.saveWithFullMetadata(output)
    return output.buildResult()
}
