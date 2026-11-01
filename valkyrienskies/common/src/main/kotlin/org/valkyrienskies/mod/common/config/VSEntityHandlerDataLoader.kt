package org.valkyrienskies.mod.common.config

import com.google.gson.Gson
import com.google.gson.JsonElement
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.FileToIdConverter
import net.minecraft.resources.Identifier
import net.minecraft.server.packs.resources.ResourceManager
import net.minecraft.server.packs.resources.SimplePreparableReloadListener
import net.minecraft.util.profiling.ProfilerFiller
import org.valkyrienskies.mod.common.entity.handling.VSEntityManager
import org.valkyrienskies.mod.util.logger

// 26.2: SimpleJsonResourceReloadListener<T> is now Codec-driven (parses each file straight to a T
// via a Codec<T>), so it no longer fits VS2's "read raw JsonElement, parse loosely" pattern.
// Extending SimplePreparableReloadListener directly and re-implementing the raw JSON scan
// preserves the old Gson-based behaviour.
object VSEntityHandlerDataLoader : SimplePreparableReloadListener<Map<Identifier, JsonElement>>() {
    private val gson = Gson()
    private val converter = FileToIdConverter.json("vs_entities")

    override fun prepare(resourceManager: ResourceManager, profiler: ProfilerFiller): Map<Identifier, JsonElement> {
        val result = HashMap<Identifier, JsonElement>()
        for ((id, resource) in converter.listMatchingResources(resourceManager)) {
            try {
                resource.openAsReader().use { reader ->
                    result[converter.fileToId(id)] = gson.fromJson(reader, JsonElement::class.java)
                }
            } catch (e: Exception) {
                logger.error("Error reading entity handler data file $id", e)
            }
        }
        return result
    }

    override fun apply(
        list: Map<Identifier, JsonElement>,
        resourceManager: ResourceManager,
        profiler: ProfilerFiller
    ) {
        list.forEach { (l, v) ->
            try {
                val type = BuiltInRegistries.ENTITY_TYPE.getOptional(l).orElse(null) ?: return@forEach
                val handler = VSEntityManager.getHandler(Identifier.parse(v.asJsonObject.get("handler").asString))
                    ?: throw Exception("Handler not found")

                VSEntityManager.pair(type, handler)
            } catch (e: Exception) {
                logger.error("Error loading entity handler data for entity type $l", e)
            }
        }
    }

    private val logger by logger()
}
