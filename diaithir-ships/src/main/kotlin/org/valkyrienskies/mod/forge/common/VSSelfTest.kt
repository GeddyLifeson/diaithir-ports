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

    private var tournamentShip: ServerShip? = null
    private var yardShip: ServerShip? = null

    /** Ship yard: lays a hull on a slipway, installs every stage, launches, and checks a VS2 ship appears. */
    private fun yardTest(server: net.minecraft.server.MinecraftServer, level: net.minecraft.server.level.ServerLevel) {
        for (id in listOf("sloop_drydock", "motor_launch_drydock")) yardRun(level, id)
    }

    private fun yardRun(level: net.minecraft.server.level.ServerLevel, yardName: String) {
        val ox = if (yardName.startsWith("sloop")) 40 else 70
        val ctrl = BlockPos(ox, 122, 40)
        val def = net.diaithir.yards.YardRegistry.yard("vs_yard:" + yardName).orElse(null)
        log.info("selftest-yard: {} parts={} def={} yards={}", yardName, net.diaithir.ships.yard.YardData.partBlocks.size, def?.id(), net.diaithir.ships.yard.YardData.yardIds)
        if (def == null) return
        val fp = net.diaithir.ships.yard.YardData.footprints["vs_yard:" + yardName]!!
        for (z in 41..41 + fp.l) level.setBlock(BlockPos(ox, 121, z), Blocks.STONE.defaultBlockState(), 3)
        level.setBlock(ctrl, net.diaithir.ships.yard.ShipYardBlocks.SLIPWAY.get().defaultBlockState(), 3)
        val be = level.getBlockEntity(ctrl) as net.diaithir.ships.yard.SlipwayBlockEntity
        be.chooseYard("vs_yard:" + yardName)
        log.info("selftest-yard: siteCheck={}", be.siteCheck())
        val keel = net.diaithir.yards.YardRegistry.part("vs_yard:keel_beam").get()
        log.info("selftest-yard: keel without crane -> {}", be.install(keel))
        level.setBlock(ctrl.offset(3, 0, 0), net.diaithir.ships.yard.ShipYardBlocks.STEAM_CRANE.get().defaultBlockState(), 3)
        val b = be.build!!
        var guard = 0
        while (!b.complete() && guard++ < 50) {
            val st = def.stages()[b.stageIndex()]
            for (slot in st.slots()) repeat(slot.count()) {
                val part = net.diaithir.ships.yard.ShipYardBlocks.PART_IDS.map { net.diaithir.yards.YardRegistry.part("vs_yard:" + it).get() }.first { it.kind() == slot.kind() }
                val r = be.install(part)
                if (r != net.diaithir.yards.BuildState.InstallResult.OK) log.error("selftest-yard: install {} -> {}", part.item(), r)
            }
            be.work(st.baseTicks() / 3)
            log.info("selftest-yard: stage {} progress {} hullBlocks={}", b.stageIndex(), b.stageProgress(), be.hullBlocks().size)
            be.work(100000)
        }
        val stats = b.stats()
        log.info("selftest-yard: complete={} stats={} hullBlocks={}", b.complete(), stats, be.hullBlocks().size)
        val ships0 = shipWorldOf(level).allShips.size
        val id = be.launch()
        val s = shipWorldOf(level).allShips.firstOrNull { it.id == id }
        yardShip = s
        log.info("selftest-yard: launched shipId={} ships {} -> {} mass={}", id, ships0, shipWorldOf(level).allShips.size, s?.inertiaData?.mass)
    }

    /** Registry/recipe checks for the merged Tournament content plus a ship assembled from its blocks. */
    private fun tournamentTest(server: net.minecraft.server.MinecraftServer, level: net.minecraft.server.level.ServerLevel) {
        val reg = net.minecraft.core.registries.BuiltInRegistries.BLOCK
        val blocks = reg.keySet().filter { it.namespace == "vs_tournament" }
        val items = net.minecraft.core.registries.BuiltInRegistries.ITEM.keySet().filter { it.namespace == "vs_tournament" }
        val recipes = server.recipeManager.recipes.filter { it.id().identifier().namespace == "vs_tournament" }
        log.info("selftest-tournament: blocks={} items={} recipes={} beTypes={} triggers={}", blocks.size, items.size, recipes.size,
            net.minecraft.core.registries.BuiltInRegistries.BLOCK_ENTITY_TYPE.keySet().filter { it.namespace == "vs_tournament" }.size,
            net.minecraft.core.registries.BuiltInRegistries.TRIGGER_TYPES.keySet().filter { it.namespace == "vs_tournament" }.size)
        val T = { n: String -> reg.getValue(net.minecraft.resources.Identifier.fromNamespaceAndPath("vs_tournament", n)).defaultBlockState() }
        for (x in 20..24) for (z in 20..22) level.setBlock(BlockPos(x, 121, z), Blocks.OAK_PLANKS.defaultBlockState(), 3)
        val assembler = BlockPos(20, 122, 21)
        level.setBlock(assembler, T("ship_assembler"), 3)
        level.setBlock(BlockPos(21, 122, 21), T("thruster"), 3)
        level.setBlock(BlockPos(22, 122, 21), T("prop_big"), 3)
        level.setBlock(BlockPos(23, 122, 21), T("balloon"), 3)
        level.setBlock(BlockPos(24, 122, 21), T("spinner"), 3)
        level.setBlock(BlockPos(21, 122, 20), T("fuel_tank_full_transparent"), 3)
        level.setBlock(BlockPos(22, 122, 20), T("rotator"), 3)
        level.setBlock(BlockPos(23, 122, 20), T("seat"), 3)
        val structure = org.valkyrienskies.tournament.util.TournamentShipAssembler.findStructure(level, assembler)
        val s = ShipAssembler.assembleToShipFull(level, structure).ship
        tournamentShip = s
        log.info("selftest-tournament: assembled ship={} structureSize={}", s.id, structure.size)
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
            130 -> tournamentTest(server, level)
            160 -> yardTest(server, level)
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
            in 150..400 -> if (tick % 50 == 0) { yardShip?.let { log.info("selftest-yard: tick={} ship y={} vel={}", tick, it.transform.position.y(), it.velocity) } ; tournamentShip?.let { t -> log.info("selftest-tournament: tick={} ship y={} {}", tick, t.transform.position.y(), shipWorldOf(level).loadedShips.getById(t.id)?.let { l -> org.valkyrienskies.tournament.ship.TournamentShips.getOrCreate(l).debugCounts() }) } ; eurekaShip?.let { log.info("selftest-eureka: tick={} ship y={} vel={}", tick, it.transform.position.y(), it.velocity) } }
            in 140..400 -> if (tick % 40 == 0) ship?.let {
                log.info("selftest: tick={} ship y={} (start {}) vel={} all={} loaded={} static={} mass={} chunkLoaded={} block={}", tick, it.transform.position.y(), startY, it.velocity, shipWorldOf(level).allShips.size, shipWorldOf(level).loadedShips.size, it.isStatic, it.inertiaData.mass, level.chunkSource.getChunkNow(it.chunkClaim.getCenterBlockCoordinates(level.yRange).x shr 4, it.chunkClaim.getCenterBlockCoordinates(level.yRange).z shr 4) != null, level.getBlockState(it.chunkClaim.getCenterBlockCoordinates(level.yRange).let { c -> net.minecraft.core.BlockPos(c.x, c.y, c.z) }))
            }
        }
    }
}
