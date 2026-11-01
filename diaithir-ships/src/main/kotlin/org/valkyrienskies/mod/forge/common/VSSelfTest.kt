package org.valkyrienskies.mod.forge.common

import net.minecraft.core.BlockPos
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Blocks
import net.neoforged.neoforge.event.tick.ServerTickEvent
import org.slf4j.LoggerFactory
import org.valkyrienskies.mod.common.shipObjectWorld
import org.valkyrienskies.eureka.ship.eurekaControl
import org.valkyrienskies.mod.common.getShipManagingPos
import org.valkyrienskies.mod.common.getShipObjectManagingPos
import org.valkyrienskies.mod.common.yRange
import org.valkyrienskies.core.api.ships.ServerShip
import org.valkyrienskies.mod.common.assembly.ShipAssembler

/**
 * Developer smoke test, only active with -Dvs.selftest=true. Builds a small block cluster, assembles it into a ship
 * and logs the ship transform over time so a headless server run can confirm physics is stepping.
 */
private fun shipWorldOf(level: net.minecraft.server.level.ServerLevel) = level.shipObjectWorld

object VSSelfTest {
    private val log = LoggerFactory.getLogger("VS2-selftest")
    private var ship: ServerShip? = null
    private var startY = 0.0
    private var mockPlayer: net.minecraft.server.level.ServerPlayer? = null
    private var eurekaShip: ServerShip? = null

    /** Registry/recipe checks for the merged Eureka content plus a helm-built ship. */
    private fun eurekaTest(server: net.minecraft.server.MinecraftServer, level: net.minecraft.server.level.ServerLevel) {
        val blocks = net.minecraft.core.registries.BuiltInRegistries.BLOCK.keySet().filter { it.namespace == "vs_eureka" }
        val items = net.minecraft.core.registries.BuiltInRegistries.ITEM.keySet().filter { it.namespace == "vs_eureka" }
        val recipes = server.recipeManager.recipes.filter { it.id().identifier().namespace == "vs_eureka" }
        log.info("selftest-eureka: blocks={} items={} recipes={} beTypes={} menus={}", blocks.size, items.size, recipes.size,
            net.minecraft.core.registries.BuiltInRegistries.BLOCK_ENTITY_TYPE.keySet().filter { it.namespace == "vs_eureka" }.size,
            net.minecraft.core.registries.BuiltInRegistries.MENU.keySet().filter { it.namespace == "vs_eureka" }.size)
        val E = { n: String -> net.minecraft.core.registries.BuiltInRegistries.BLOCK.getValue(net.minecraft.resources.Identifier.fromNamespaceAndPath("vs_eureka", n)).defaultBlockState() }
        for (x in 10..12) for (z in 10..12) level.setBlock(BlockPos(x, 121, z), Blocks.OAK_PLANKS.defaultBlockState(), 3)
        val helm = BlockPos(11, 122, 11)
        level.setBlock(helm, E("oak_ship_helm"), 3)
        level.setBlock(BlockPos(11, 123, 11), E("balloon"), 3)
        level.setBlock(BlockPos(12, 122, 11), E("floater"), 3)
        level.setBlock(BlockPos(10, 122, 11), E("engine"), 3)
        val be = level.getBlockEntity(helm) as? org.valkyrienskies.eureka.blockentity.ShipHelmBlockEntity
        log.info("selftest-eureka: helm blockentity={}", be)
        be?.assemble(mockPlayer!!)
        val s = shipWorldOf(level).allShips.filter { it.id != ship?.id }.maxByOrNull { it.id }
        eurekaShip = s
        log.info("selftest-eureka: helm ship={} control={}", s?.id, s?.eurekaControl()?.let { c -> "helms=" + c.helms + " balloons=" + c.balloons + " floaters=" + c.floaters })
    }

    /** Forces every mixin target class named by the common/forge mixin configs to load so mixin application errors surface. */
    private fun preloadMixinTargets() {
        val loader = VSSelfTest::class.java.classLoader
        val mapper = com.google.gson.Gson()
        var ok = 0
        var failed = 0
        for (cfg in listOf("valkyrienskies-common.mixins.json", "valkyrienskies-forge.mixins.json")) {
            val json = loader.getResourceAsStream(cfg)?.use { mapper.fromJson(it.reader(), com.google.gson.JsonObject::class.java) } ?: continue
            val pkg = json.get("package").asString
            val names = ArrayList<String>()
            json.getAsJsonArray("mixins")?.forEach { names.add(it.asString) }
            for (m in names) {
                val path = (pkg + "." + m).replace('.', '/') + ".class"
                val targets = ArrayList<String>()
                loader.getResourceAsStream(path)?.use { ins ->
                    org.objectweb.asm.ClassReader(ins).accept(object : org.objectweb.asm.ClassVisitor(org.objectweb.asm.Opcodes.ASM9) {
                        override fun visitAnnotation(desc: String, visible: Boolean): org.objectweb.asm.AnnotationVisitor? {
                            if (desc != "Lorg/spongepowered/asm/mixin/Mixin;") return null
                            return object : org.objectweb.asm.AnnotationVisitor(org.objectweb.asm.Opcodes.ASM9) {
                                override fun visitArray(name: String): org.objectweb.asm.AnnotationVisitor = object : org.objectweb.asm.AnnotationVisitor(org.objectweb.asm.Opcodes.ASM9) {
                                    override fun visit(n: String?, v: Any?) {
                                        if (v is org.objectweb.asm.Type) targets.add(v.className)
                                        else if (v is String && name == "targets") targets.add(v.replace('/', '.'))
                                    }
                                }
                            }
                        }
                    }, org.objectweb.asm.ClassReader.SKIP_CODE)
                }
                for (t in targets) {
                    try { Class.forName(t, false, loader); ok++ } catch (e: Throwable) {
                        failed++
                        log.error("selftest MIXIN FAIL {} -> {}: {}", m, t, e.toString().take(400))
                    }
                }
            }
        }
        log.info("selftest: mixin target preload done, ok={} failed={}", ok, failed)
    }

    @JvmStatic
    fun onTick(event: ServerTickEvent.Post) {
        val server = event.server
        val tick = server.tickCount
        val level = server.getLevel(Level.OVERWORLD) ?: return
        when (tick) {
            2 -> preloadMixinTargets()
            20 -> {
                val cookie = net.minecraft.server.network.CommonListenerCookie.createInitial(
                    com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(), "vs-selftest"), false)
                val player = object : net.minecraft.server.level.ServerPlayer(server, level, cookie.gameProfile(), cookie.clientInformation()) {
                    override fun gameMode() = net.minecraft.world.level.GameType.CREATIVE
                }
                val connection = net.minecraft.network.Connection(net.minecraft.network.protocol.PacketFlow.SERVERBOUND)
                io.netty.channel.embedded.EmbeddedChannel(connection)
                net.neoforged.neoforge.network.registration.ChannelAttributes.getOrCreateAdHocChannels(connection).add(VSPacket.VS_PACKET_RL)
                server.playerList.placeNewPlayer(connection, player, cookie)
                mockPlayer = player
                player.teleportTo(1.5, 130.0, 1.5)
                log.info("selftest: mock player placed")
            }
            110 -> eurekaTest(server, level)
            100 -> {
                val blocks = HashSet<BlockPos>()
                for (x in 0..2) for (z in 0..2) {
                    val p = BlockPos(x, 120, z)
                    level.setBlock(p, Blocks.OAK_PLANKS.defaultBlockState(), 3)
                    blocks.add(p)
                }
                val s = ShipAssembler.assembleToShip(level, blocks, 1.0)
                ship = s
                startY = s.transform.position.y()
                log.info("selftest: assembled ship id={} slug={} pos={}", s.id, s.slug, s.transform.position)
            }
            in 150..400 -> if (tick % 50 == 0) eurekaShip?.let { log.info("selftest-eureka: tick={} ship y={} vel={}", tick, it.transform.position.y(), it.velocity) }
            in 140..400 -> if (tick % 40 == 0) ship?.let {
                log.info("selftest: tick={} ship y={} (start {}) vel={} all={} loaded={} static={} mass={} chunkLoaded={} block={}", tick, it.transform.position.y(), startY, it.velocity, shipWorldOf(level).allShips.size, shipWorldOf(level).loadedShips.size, it.isStatic, it.inertiaData.mass, level.chunkSource.getChunkNow(it.chunkClaim.getCenterBlockCoordinates(level.yRange).x shr 4, it.chunkClaim.getCenterBlockCoordinates(level.yRange).z shr 4) != null, level.getBlockState(it.chunkClaim.getCenterBlockCoordinates(level.yRange).let { c -> net.minecraft.core.BlockPos(c.x, c.y, c.z) }))
            }
        }
    }
}
