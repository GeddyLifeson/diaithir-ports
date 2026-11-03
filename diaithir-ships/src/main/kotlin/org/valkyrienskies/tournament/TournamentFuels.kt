package org.valkyrienskies.tournament

import blitz.collections.RefVec
import com.fasterxml.jackson.annotation.JsonIgnore
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import net.neoforged.neoforge.event.AddServerReloadListenersEvent
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent
import net.neoforged.neoforge.event.entity.player.PlayerEvent
import net.neoforged.neoforge.event.server.ServerStartedEvent
import net.minecraft.core.RegistryAccess
import net.minecraft.core.particles.ParticleOptions
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.network.chat.Style
import net.minecraft.resources.Identifier
import net.minecraft.tags.TagKey
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import org.valkyrienskies.core.util.events.EventEmitterImpl
import org.valkyrienskies.mod.common.playerWrapper
import org.valkyrienskies.mod.common.vsCore
import org.valkyrienskies.tournament.util.TypedResourceLocation
import org.valkyrienskies.tournament.util.extension.applyStyle
import org.valkyrienskies.tournament.util.extension.toComponent
import org.valkyrienskies.tournament.util.extension.toResourceLocation
import org.valkyrienskies.tournament.util.parseParticle
import kotlin.jvm.optionals.getOrNull
import kotlin.math.round

data class FuelParticleOptions(
    @JvmField val unparsedParticles: String?,
    @JvmField val particleVelocity: Float,
    @JvmField val particleSpread: Float,
    @JvmField val particleCount: Int,
) {
    @get:JsonIgnore
    @set:JsonIgnore
    @field:JsonIgnore
    @Transient
    @JvmField var particles: ParticleOptions? = null

    /** Resolves the particle string against the given registries (client side, lazily). */
    fun resolve(access: RegistryAccess): ParticleOptions? {
        if (particles == null && unparsedParticles != null) {
            particles = try { parseParticle(access, unparsedParticles) } catch (e: Exception) { null }
        }
        return particles
    }
}

data class FuelType(
    // when off
    @JvmField val standbyBurnRate: Float,

    // when on
    @JvmField val baseBurnRate: Float,
    @JvmField val maxBurnRate: Float,

    // when on
    @JvmField val basePower: Float,
    @JvmField val maxPower: Float,

    @JvmField val particles: FuelParticleOptions,
) {
    // higher score is "better"
    @JsonIgnore
    @JvmField val score = maxPower - baseBurnRate * 1000 - maxBurnRate * 1000 - standbyBurnRate * 10000

    fun getBurnRate(throttle: Float) =
        if (throttle == 0.0f) standbyBurnRate
        else baseBurnRate + maxBurnRate * throttle

    fun getPower(throttle: Float) =
        if (throttle == 0.0f) 0f
        else basePower + maxPower * throttle

    companion object {
        fun decode(json: JsonObject): FuelType =
            FuelType(
                standbyBurnRate = json.get("standbyBurnRate")?.asFloat ?: 0f,
                baseBurnRate = json.get("baseBurnRate")?.asFloat ?: 0f,
                maxBurnRate = json.get("maxBurnRate")?.asFloat ?: 0f,
                basePower = json.get("basePower")?.asFloat ?: 0f,
                maxPower = json.get("maxPower")?.asFloat ?: 0f,
                particles = FuelParticleOptions(
                    unparsedParticles = json.get("particleType")?.asString,
                    particleVelocity = json.get("particleVelocity")?.asFloat ?: 0.4f,
                    particleSpread = json.get("particleSpread")?.asFloat ?: 0f,
                    particleCount = json.get("particleCount")?.asInt ?: 2,
                )
            )
    }

}

class RegisteredFuelType(
    @JvmField val id: Identifier,
    @JvmField val fuel: FuelType,
    @JvmField val targetItems: RefVec<Identifier>,
    @JvmField val targetTags: RefVec<Identifier>,
    @JvmField var compactId: Int?,
) {
    fun toClient() = ClientFuelType(
        particles = fuel.particles,
    )
}

class ClientFuelType(
    @JvmField val particles: FuelParticleOptions,
)

object TournamentClientFuels {
    @JvmField var types = emptyArray<ClientFuelType>()
}

// TODO: use real registry (also for compact fuel IDs)
object TournamentFuels {
    @JvmField val preFuelsReloaded = EventWrapper(EventEmitterImpl<Unit>())
    @JvmField val postFuelsReloaded = EventWrapper(EventEmitterImpl<Unit>())

    private val registry = mutableMapOf<TypedResourceLocation<RegisteredFuelType>, RegisteredFuelType>()
    private val byItem = mutableMapOf<TypedResourceLocation<Item>, RegisteredFuelType>()
    private val byTag = mutableMapOf<Identifier, RegisteredFuelType>()
    private var clientFuels = emptyList<ClientFuelType>()

    @JvmStatic
    @JvmSynthetic @JvmName("byItemId_typed")
    fun get(item: TypedResourceLocation<Item>): RegisteredFuelType? =
        byItem[item]

    @JvmStatic
    fun byItemId(item: Identifier): RegisteredFuelType? =
        byItem[TypedResourceLocation(item)]

    @JvmStatic
    @JvmName("byTagId")
    fun get(tag: TagKey<Item>): RegisteredFuelType? =
        byTag[tag.location()]

    @JvmStatic
    fun byTagId(tag: Identifier): RegisteredFuelType? =
        byTag[tag]

    @JvmStatic
    @JvmName("byFuelId")
    fun get(id: TypedResourceLocation<RegisteredFuelType>): RegisteredFuelType? =
        registry[id]

    @JvmStatic
    fun get(item: ItemStack): RegisteredFuelType? {
        var best: RegisteredFuelType? = null
        fun RegisteredFuelType.found() {
            if (best == null || fuel.score > best!!.fuel.score)
                best = this
        }
        item.typeHolder().unwrapKey().getOrNull()?.identifier()?.let(::byItemId)?.found()
        item.typeHolder().tags().forEach {
            get(it)?.found()
        }
        return best
    }

    private class FuelListener(private val access: RegistryAccess) :
        net.minecraft.server.packs.resources.SimplePreparableReloadListener<Map<Identifier, JsonElement>>() {
        override fun prepare(
            resourceManager: net.minecraft.server.packs.resources.ResourceManager,
            profiler: net.minecraft.util.profiling.ProfilerFiller
        ): Map<Identifier, JsonElement> {
            val out = LinkedHashMap<Identifier, JsonElement>()
            val lister = net.minecraft.resources.FileToIdConverter.json("vs_tournament_fuel")
            for ((file, res) in lister.listMatchingResources(resourceManager)) {
                try {
                    res.openAsReader().use { out[lister.fileToId(file)] = com.google.gson.JsonParser.parseReader(it) }
                } catch (e: Exception) {
                    println("[Tournament] ERROR while reading fuel '$file': $e")
                }
            }
            return out
        }

        override fun apply(
            objects: Map<Identifier, JsonElement>,
            resourceManager: net.minecraft.server.packs.resources.ResourceManager,
            profiler: net.minecraft.util.profiling.ProfilerFiller
        ) {
            loadFuels(objects)
        }
    }

    private fun loadFuels(objects: Map<Identifier, JsonElement>) {
        registry.clear()
        preFuelsReloaded.emit(Unit)
        objects.forEach { (key, element) ->
            try {
                val json = element.asJsonObject
                val fuel = RegisteredFuelType(
                    id = key,
                    fuel = FuelType.decode(json),
                    targetItems = ((json.get("items")?.asJsonArray?.asSequence() ?: emptySequence()) + sequenceOf(json.get("item")))
                        .filterNotNull()
                        .map { it.asString.toResourceLocation() }
                        .asIterable()
                        .let(RefVec.Companion::from),
                    targetTags = ((json.get("tags")?.asJsonArray?.asSequence() ?: emptySequence()) + sequenceOf(json.get("tag")))
                        .filterNotNull()
                        .map { it.asString.toResourceLocation() }
                        .asIterable()
                        .let(RefVec.Companion::from),
                    compactId = null,
                )
                registry[TypedResourceLocation(key)] = fuel
            } catch (e: Exception) {
                println("[Tournament] ERROR while trying to load fuel '$key':")
                e.printStackTrace()
            }
        }

        byItem.clear()
        byTag.clear()

        val allFuels = RefVec<ClientFuelType>(registry.size)
        registry.values.forEachIndexed { compactId, fuel ->
            fuel.compactId = compactId
            allFuels.pushBack(fuel.toClient())

            fuel.targetItems.forEach {
                byItem.compute(TypedResourceLocation(it)) { _, old ->
                    if (old == null || fuel.fuel.score > old.fuel.score) fuel else old
                }
            }

            fuel.targetTags.forEach {
                byTag.compute(it) { _, old ->
                    if (old == null || fuel.fuel.score > old.fuel.score) fuel else old
                }
            }
        }
        clientFuels = allFuels.toCollection(ArrayList(allFuels.size))

        println("[Tournament] ${clientFuels.size} fuel types were registered")

        postFuelsReloaded.emit(Unit)
    }

    @JvmStatic
    fun addReloadListener(event: AddServerReloadListenersEvent) {
        event.addListener(Identifier.fromNamespaceAndPath(TournamentMod.MOD_ID, "vs_tournament_fuel"), FuelListener(event.registryAccess))
    }

    @JvmStatic
    fun onPlayerJoin(event: PlayerEvent.PlayerLoggedInEvent) {
        val player = event.entity as? net.minecraft.server.level.ServerPlayer ?: return
        vsCore.simplePacketNetworking.sendToClient(
            TournamentNetworking.FuelsReloaded(clientFuels),
            player.playerWrapper)
    }

    @Volatile private var broadcast = false

    @JvmStatic
    fun onServerStarted(event: ServerStartedEvent) { broadcast = true }

    @JvmStatic
    fun onServerStopped(event: net.neoforged.neoforge.event.server.ServerStoppedEvent) { broadcast = false }

    init {
        // after the server started, when the datapack is reloaded, send the fuels to all online players
        postFuelsReloaded.on { _ ->
            if (broadcast) vsCore.simplePacketNetworking.sendToAllClients(
                TournamentNetworking.FuelsReloaded(clientFuels))
        }
    }

    @JvmStatic
    fun onTooltip(event: ItemTooltipEvent) {
        val stack = event.itemStack
        val tooltipComponents = event.toolTip
        run {
            val fuel = stack.tournamentFuel()?.fuel ?: return@run

            fun num(num: Float): String =
                (round(num * 1_000_000f) / 1_000_000f).toString()

            fun tc(key: String, vararg args: Any, styleMod: (Style) -> Style = { it }): MutableComponent =
                Component.translatable("tooltip.vs_tournament.fuel.$key", *args)
                    .applyStyle(styleMod)

            fun t(key: String, vararg args: Any, styleMod: (Style) -> Style = { it }) {
                tooltipComponents += tc(key, *args, styleMod = styleMod)
            }

            fun separator() {
                tooltipComponents += Component.empty()
            }

            t("title") { it.withUnderlined(true) }
            separator()
            t("standbyBurnRate", num(fuel.standbyBurnRate), num(fuel.standbyBurnRate * 20))
            t("baseBurnRate", num(fuel.baseBurnRate), num(fuel.baseBurnRate * 20))
            t("maxBurnRate", num(fuel.maxBurnRate), num(fuel.maxBurnRate * 20))
            separator()
            t("basePower", num(fuel.basePower))
            t("maxPower", num(fuel.maxPower))
            separator()
            t("infoThrottle")
        }
    }
}

fun ItemStack.tournamentFuel(): RegisteredFuelType? =
    TournamentFuels.get(this)