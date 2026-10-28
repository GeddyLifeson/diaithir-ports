package org.valkyrienskies.mod.common

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.resources.Identifier
import net.minecraft.world.level.saveddata.SavedData
import net.minecraft.world.level.saveddata.SavedDataType
import org.valkyrienskies.core.internal.world.VsiPipeline
import java.nio.ByteBuffer
import java.util.Optional

/**
 * This class saves/loads ship data for a world.
 *
 * This is only a temporary solution, and should be replaced eventually because it is very inefficient.
 */
class ShipSavedData : SavedData() {

    companion object {
        const val SAVED_DATA_ID = "vs_ship_data"
        private const val QUERYABLE_SHIP_DATA_NBT_KEY = "queryable_ship_data"
        private const val CHUNK_ALLOCATOR_NBT_KEY = "chunk_allocator"
        private const val PIPELINE_NBT_KEY = "vs_pipeline"

        fun createEmpty(): ShipSavedData {
            return ShipSavedData().apply { pipeline = vsCore.newPipeline() }
        }

        private fun ByteBuffer.toBytes(): ByteArray {
            val dup = duplicate()
            val arr = ByteArray(dup.remaining())
            dup.get(arr)
            return arr
        }

        private fun Optional<ByteBuffer>.bytesOrEmpty(): ByteArray = map { it.toBytes() }.orElse(ByteArray(0))

        @JvmStatic
        fun load(
            queryableShipDataAsBytes: ByteArray, chunkAllocatorAsBytes: ByteArray, pipelineAsBytes: ByteArray
        ): ShipSavedData {
            val logger = org.slf4j.LoggerFactory.getLogger("VS2")
            val data = ShipSavedData()

            logger.info(" ShipSavedData.load(): pipeline bytes = {} KB, legacy queryable = {} KB, legacy allocator = {} KB",
                pipelineAsBytes.size / 1024, queryableShipDataAsBytes.size / 1024, chunkAllocatorAsBytes.size / 1024)

            try {
                if (pipelineAsBytes.isNotEmpty()) {
                    data.pipeline = vsCore.newPipeline(pipelineAsBytes)
                    logger.info(" ShipSavedData.load(): loaded pipeline from {} KB of data", pipelineAsBytes.size / 1024)
                } else if (queryableShipDataAsBytes.isNotEmpty() && chunkAllocatorAsBytes.isNotEmpty()) {
                    data.pipeline = vsCore.newPipelineLegacyData(queryableShipDataAsBytes, chunkAllocatorAsBytes)
                    logger.info(" ShipSavedData.load(): loaded pipeline from legacy data")
                } else {
                    throw IllegalStateException("Couldn't find serialized ship data")
                }
            } catch (ex: Exception) {
                logger.error(" ShipSavedData.load(): FAILED to load pipeline: {}", ex.message)
                data.loadingException = ex
            }
            return data
        }

        @JvmField
        val CODEC: Codec<ShipSavedData> = RecordCodecBuilder.create { instance ->
            instance.group(
                Codec.BYTE_BUFFER.optionalFieldOf(QUERYABLE_SHIP_DATA_NBT_KEY).forGetter { Optional.empty<ByteBuffer>() },
                Codec.BYTE_BUFFER.optionalFieldOf(CHUNK_ALLOCATOR_NBT_KEY).forGetter { Optional.empty<ByteBuffer>() },
                Codec.BYTE_BUFFER.optionalFieldOf(PIPELINE_NBT_KEY).forGetter { data ->
                    val logger = org.slf4j.LoggerFactory.getLogger("VS2")
                    val bytes = vsCore.serializePipeline(data.pipeline)
                    logger.info(" ShipSavedData.save(): pipeline bytes = {} KB", bytes.size / 1024)
                    Optional.of(ByteBuffer.wrap(bytes))
                }
            ).apply(instance) { q, c, p -> load(q.bytesOrEmpty(), c.bytesOrEmpty(), p.bytesOrEmpty()) }
        }

        /** Use with `overworld().dataStorage.computeIfAbsent(ShipSavedData.TYPE)`. */
        @JvmField
        val TYPE: SavedDataType<ShipSavedData> = SavedDataType(
            Identifier.fromNamespaceAndPath(ValkyrienSkiesMod.MOD_ID, SAVED_DATA_ID),
            java.util.function.Supplier { createEmpty() }, CODEC
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
