package org.valkyrienskies.tournament.storage

import net.minecraft.nbt.CompoundTag

class NBTList<T>(
    val nbt: CompoundTag,
    private val serializer: NBTSerializer<T>,
): AbstractMutableList<T>() {
    private fun sub(i: Int): CompoundTag = nbt.compoundOf(i)

    override val size: Int
        get() = nbt.getIntOr("size", 0)

    override fun get(index: Int): T =
        serializer.read(sub(index))

    override fun removeAt(index: Int): T {
        val t = get(index)
        for (i in index until size - 1) {
            nbt.put(index.toString(), sub(index + 1))
        }
        nbt.remove((size - 1).toString())
        nbt.putInt("size", size - 1)
        return t
    }

    override fun set(index: Int, element: T): T =
        serializer.write(sub(index), element).let {
            element
        }

    override fun add(index: Int, element: T) {
        serializer.write(sub(index), element)
        if (index == size)
            nbt.putInt("size", size + 1)
    }

    override fun clear() {
        for (i in 0 until size) {
            nbt.remove(i.toString())
        }
        nbt.putInt("size", 0)
    }

    private class Serializer<T>(
        private val elemSerializer: NBTSerializer<T>,
    ): NBTSerializer<MutableList<T>> {
        override fun write(nbt: CompoundTag, t: MutableList<T>) {
            nbt.putInt("size", t.size)
            for (i in t.indices) {
                elemSerializer.write(nbt.compoundOf(i), t[i])
            }
        }

        override fun read(nbt: CompoundTag): MutableList<T> =
            NBTList(nbt, elemSerializer)
    }

    companion object {
        fun <T> serializer(elemSerializer: NBTSerializer<T>): NBTSerializer<MutableList<T>> =
            Serializer(elemSerializer)
    }
}

private fun CompoundTag.compoundOf(i: Int): CompoundTag =
    getCompound(i.toString()).orElseGet { CompoundTag().also { put(i.toString(), it) } }
