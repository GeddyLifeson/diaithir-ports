package org.valkyrienskies.tournament.util.extension

import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.Identifier
import net.minecraft.world.level.Level
import org.valkyrienskies.core.api.world.properties.DimensionId

fun DimensionId.toDimensionKey(): ResourceKey<Level> =
    this.split(":").let {
        Identifier.fromNamespaceAndPath(it[it.size - 2], it[it.size - 1]).toDimensionKey()
    }

fun String.toResourceLocation() =
    Identifier.parse(this)

fun Identifier.toDimensionKey(): ResourceKey<Level> =
    ResourceKey.create(Registries.DIMENSION, this)