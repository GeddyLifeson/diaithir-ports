package org.valkyrienskies.mod.common

import com.mojang.serialization.Codec
import net.minecraft.resources.Identifier
import net.minecraft.world.level.saveddata.SavedData
import net.minecraft.world.level.saveddata.SavedDataType
import org.valkyrienskies.core.internal.world.VsiPipeline
import java.nio.ByteBuffer

/**
 * This class saves/loads ship data for a world.
 *
 * This is only a temporary solution, and should be replaced eventually because it is very inefficient.
 *
 * 26.2: [SavedData] no longer has an overridable save()/CompoundTag-based load() - serialization is
 * now driven entirely by a [Codec] attached to a [SavedDataType]. The old dual-field
 * ("queryable_ship_data" + "chunk_allocator") legacy-load fallback is dropped here since this is a
 * fresh 26.2 fork with no pre-existing saves to migrate from.
 */
class ShipSavedData : SavedData() {

    companion object {
        private val logger = org.slf4j.LoggerFactory.getLogger("VS2")

        fun createEmpty(): ShipSavedData {
            return ShipSavedData().apply { pipeline = vsCore.newPipeline() }
        }

        val CODEC: Codec<ShipSavedData> = Codec.BYTE_BUFFER.xmap(
            { buf ->
                val bytes = ByteArray(buf.remaining())
                buf.get(bytes)
                val data = ShipSavedData()
                try {
                    data.pipeline = vsCore.newPipeline(bytes)
                    logger.info(" ShipSavedData: loaded pipeline from {} KB of data", bytes.size / 1024)
                } catch (ex: Exception) {
                    logger.error(" ShipSavedData: FAILED to load pipeline: {}", ex.message)
                    data.pipeline = vsCore.newPipeline()
                    data.loadingException = ex
                }
                data
            },
            { data ->
                val bytes = vsCore.serializePipeline(data.pipeline)
                logger.info(" ShipSavedData: saving pipeline = {} KB", bytes.size / 1024)
                ByteBuffer.wrap(bytes)
            }
        )

        // Explicit fully-typed java.util.function.Supplier (rather than ::createEmpty directly)
        // to avoid Kotlin picking the wrong SavedDataType constructor overload - createEmpty
        // could otherwise SAM-convert to either Supplier<T> or the ServerLevel-taking Factory<T>.
        private val supplier: java.util.function.Supplier<ShipSavedData> =
            java.util.function.Supplier<ShipSavedData> { createEmpty() }

        // The 3-arg (id, Supplier, Codec) constructor also exists but Kotlin's overload
        // resolution against this particular Java overload set never settles on it (falls through
        // to reporting the 4-arg constructor's missing param instead) - passing DataFixTypes.LEVEL
        // explicitly (same value the old SavedData.Factory call in MixinMinecraftServer.java used)
        // sidesteps the issue entirely since only one constructor then has 4 params.
        @JvmField
        val TYPE: SavedDataType<ShipSavedData> = SavedDataType(
            Identifier.fromNamespaceAndPath(ValkyrienSkiesMod.MOD_ID, "vs_ship_data"),
            supplier,
            CODEC,
            net.minecraft.util.datafix.DataFixTypes.LEVEL,
        )
    }

    lateinit var pipeline: VsiPipeline

    var loadingException: Throwable? = null
        private set

    /**
     * Always report as dirty since ship physics transforms change every tick.
     */
    override fun isDirty(): Boolean {
        return true
    }
}
