package net.diaithir.ships.yard

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import net.diaithir.yards.PartDef
import net.diaithir.yards.YardDef
import net.diaithir.yards.YardRegistry
import net.minecraft.resources.FileToIdConverter
import net.minecraft.resources.Identifier
import net.minecraft.server.packs.resources.ResourceManager
import net.minecraft.server.packs.resources.SimplePreparableReloadListener
import net.minecraft.util.profiling.ProfilerFiller
import org.slf4j.LoggerFactory

/** Reads the ship yard datapack tables: data/NS/yard_parts/ID.json (part -> stats) and data/NS/yards/ID.json. */
object YardData {
    private val log = LoggerFactory.getLogger("vs_yard")

    fun yardIds(): List<String> = YardRegistry.yards().map { it.id() }.filter { it.startsWith("vs_yard:") }.sorted()

    fun parsePart(o: JsonObject, id: String): PartDef {
        fun d(k: String) = o.get(k)?.asDouble ?: 0.0
        val extra = LinkedHashMap<String, Double>()
        o.getAsJsonObject("extra")?.entrySet()?.forEach { extra[it.key] = it.value.asDouble }
        val item = o.get("item")?.asString ?: id
        return PartDef(item, o.get("kind")?.asString ?: error("part $id has no kind"), d("mass_kg"), d("hp"), d("power_w"),
            d("thrust_n"), d("buoyancy_n"), d("capacity_l"), d("crew_slots").toInt(), o.get("min_age")?.asInt ?: 0, extra, o.get("block")?.asString ?: "")
    }

    fun parseYard(o: JsonObject, id: String): YardDef {
        val fp = o.getAsJsonObject("footprint")
        val stages = o.getAsJsonArray("stages").map { e ->
            val s = e.asJsonObject
            YardDef.Stage(s.get("id").asString,
                s.getAsJsonArray("slots").map { sl -> val so = sl.asJsonObject; YardDef.Slot(so.get("kind").asString, so.get("count")?.asInt ?: 1) },
                s.get("base_ticks")?.asInt ?: 100, s.get("ghost")?.asString ?: "")
        }
        val site = YardDef.Site.valueOf((o.get("site")?.asString ?: "ground").uppercase())
        val tier = YardDef.Tier.valueOf((o.get("tier")?.asString ?: "workshop").uppercase())
        return YardDef(id, tier, site, o.getAsJsonArray("dimensions")?.map { it.asString } ?: emptyList(),
            o.get("result")?.asString ?: id, stages, o.get("crane_speedup")?.asDouble ?: 1.0,
            YardDef.Footprint(fp.get("w").asInt, fp.get("l").asInt, fp.get("h").asInt))
    }

    fun load(parts: Map<Identifier, JsonElement>, yards: Map<Identifier, JsonElement>) {
        parts.forEach { (k, v) -> try { YardRegistry.register(parsePart(v.asJsonObject, k.toString())) } catch (e: Exception) { log.error("bad yard part {}: {}", k, e.toString()) } }
        yards.forEach { (k, v) -> try {
            val def = parseYard(v.asJsonObject, k.toString())
            YardRegistry.register(def)
            YardRegistry.rollout(def.id(), ShipYardMod.SHIP_HOOK)
        } catch (e: Exception) { log.error("bad yard {}: {}", k, e.toString()) } }
        log.info("vs_yard: {} parts, {} yards loaded", parts.size, yardIds().size)
    }

    class Listener : SimplePreparableReloadListener<Pair<Map<Identifier, JsonElement>, Map<Identifier, JsonElement>>>() {
        private fun read(rm: ResourceManager, dir: String): Map<Identifier, JsonElement> {
            val out = LinkedHashMap<Identifier, JsonElement>()
            val lister = FileToIdConverter.json(dir)
            for ((file, res) in lister.listMatchingResources(rm)) {
                try { res.openAsReader().use { out[lister.fileToId(file)] = com.google.gson.JsonParser.parseReader(it) } }
                catch (e: Exception) { log.error("cannot read {}: {}", file, e.toString()) }
            }
            return out
        }
        override fun prepare(rm: ResourceManager, p: ProfilerFiller) = Pair(read(rm, "yard_parts"), read(rm, "yards"))
        override fun apply(o: Pair<Map<Identifier, JsonElement>, Map<Identifier, JsonElement>>, rm: ResourceManager, p: ProfilerFiller) = load(o.first, o.second)
    }
}
