package org.valkyrienskies.tournament.storage

val NBTInt = NBTSerializer.new(
    write = { nbt, i -> nbt.putInt("value", i) },
    read = { nbt -> nbt.getIntOr("value", 0) }
)

val NBTLong = NBTSerializer.new(
    write = { nbt, i -> nbt.putLong("value", i) },
    read = { nbt -> nbt.getLongOr("value", 0L) }
)

val NBTFloat = NBTSerializer.new(
    write = { nbt, i -> nbt.putFloat("value", i) },
    read = { nbt -> nbt.getFloatOr("value", 0f) }
)

val NBTDouble = NBTSerializer.new(
    write = { nbt, i -> nbt.putDouble("value", i) },
    read = { nbt -> nbt.getDoubleOr("value", 0.0) }
)

val NBTBool = NBTSerializer.new(
    write = { nbt, i -> nbt.putBoolean("value", i) },
    read = { nbt -> nbt.getBooleanOr("value", false) }
)

val NBTString = NBTSerializer.new(
    write = { nbt, i -> nbt.putString("value", i) },
    read = { nbt -> nbt.getStringOr("value", "") }
)