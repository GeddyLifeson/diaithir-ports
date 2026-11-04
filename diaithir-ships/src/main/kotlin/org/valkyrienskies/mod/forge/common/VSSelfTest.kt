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
    private var yardShip2: ServerShip? = null
    private val yardPhase = System.getProperty("vs.yardphase", "")

    private fun yardPool(level: net.minecraft.server.level.ServerLevel, ox: Int) {
        for (cx in ((ox - 8) shr 4)..((ox + 8) shr 4)) for (cz in (32 shr 4)..(64 shr 4)) level.setChunkForced(cx, cz, true)
        for (x in ox - 7..ox + 7) for (z in 37..63) for (y in 115..122) {
            val wall = y == 115 || x == ox - 7 || x == ox + 7 || z == 37 || z == 63
            level.setBlock(BlockPos(x, y, z), when { y == 122 && !(x == ox - 7 || x == ox + 7 || z == 37 || z == 63) -> Blocks.AIR.defaultBlockState(); y == 122 -> Blocks.STONE.defaultBlockState(); wall -> Blocks.STONE.defaultBlockState(); y <= 121 -> Blocks.WATER.defaultBlockState(); else -> Blocks.AIR.defaultBlockState() }, 2)
        }
    }

    private fun installAll(level: net.minecraft.server.level.ServerLevel, be: net.diaithir.ships.yard.SlipwayBlockEntity, upToStage: Int, prefer: String? = null) {
        val def = be.def!!
        val b = be.build!!
        var guard = 0
        while (!b.complete() && b.stageIndex() < upToStage && guard++ < 50) {
            val st = def.stages()[b.stageIndex()]
            for (slot in st.slots()) repeat(slot.count()) {
                val all = net.diaithir.ships.yard.ShipYardBlocks.PART_IDS.map { net.diaithir.yards.YardRegistry.part("vs_yard:" + it).get() }
                val part = all.firstOrNull { it.kind() == slot.kind() && it.item() == "vs_yard:" + prefer } ?: all.first { it.kind() == slot.kind() }
                val r = be.install(part)
                if (r != net.diaithir.yards.BuildState.InstallResult.OK) log.error("selftest-yard: install {} -> {}", part.item(), r)
            }
            be.work(st.baseTicks() / 3)
            log.info("selftest-yard: stage {} progress {} hullBlocks={}", b.stageIndex(), b.stageProgress(), be.hullBlocks().size)
            be.work(100000)
        }
    }

    /** Ship yard: lays hulls on slipways, installs every stage, launches, applies the part stats, checks floating and thrust. */
    private fun yardTest(server: net.minecraft.server.MinecraftServer, level: net.minecraft.server.level.ServerLevel) {
        if (yardPhase == "save") {
            net.diaithir.ships.yard.AgeLookup.override = 7
            val ctrl = BlockPos(100, 122, 40)
            for (z in 41..57) level.setBlock(BlockPos(100, 121, z), Blocks.STONE.defaultBlockState(), 3)
            level.setBlock(ctrl, net.diaithir.ships.yard.ShipYardBlocks.SLIPWAY.get().defaultBlockState(), 3)
            level.setBlock(ctrl.offset(3, 0, 0), net.diaithir.ships.yard.ShipYardBlocks.STEAM_CRANE.get().defaultBlockState(), 3)
            val be = level.getBlockEntity(ctrl) as net.diaithir.ships.yard.SlipwayBlockEntity
            be.chooseYard("vs_yard:sloop_drydock")
            installAll(level, be, 3)
            val b = be.build!!
            val part = net.diaithir.yards.YardRegistry.part("vs_yard:hull_plank_set").get()
            // leave stage 3 half done: one slot filled and some work in
            be.install(net.diaithir.yards.YardRegistry.part("vs_yard:deck_plating").get())
            be.work(10)
            yardSaveBe = be
            server.execute { }
            return
        }
        if (yardPhase == "resume") {
            net.diaithir.ships.yard.AgeLookup.override = 7
            val be = level.getBlockEntity(BlockPos(100, 122, 40)) as? net.diaithir.ships.yard.SlipwayBlockEntity
            val b = be?.build
            val line = if (b == null) "NO BUILD" else "stage=" + b.stageIndex() + " ticks=" + b.progressTicks() + " installed=" + b.installed().size + " stageParts=" + b.stageParts().size + " hull=" + be.hullBlocks().size
            val saved = java.io.File("yardsave.txt").readText()
            log.info("selftest-yard-resume: saved='{}' loaded='{}' match={}", saved, line, saved == line)
            if (be != null && b != null) {
                installAll(level, be, 99)
                log.info("selftest-yard-resume: completed={} launch={}", b.complete(), be.launch())
            }
            return
        }
        // Age gating: Age I world cannot install a steam engine, Age VII can
        net.diaithir.ships.yard.AgeLookup.override = 1
        run {
            val b = net.diaithir.yards.BuildState.start(net.diaithir.yards.YardRegistry.yard("vs_yard:sloop_drydock").get())
            log.info("selftest-yard: age gate at Age I -> {}", b.install(net.diaithir.yards.YardRegistry.part("vs_yard:steam_engine").get(), net.diaithir.ships.yard.AgeLookup.current(server), 1e9))
        }
        net.diaithir.ships.yard.AgeLookup.override = 7
        yardPool(level, 40); yardPool(level, 70)
        yardShips.clear()
        yardRun(level, "sloop_slipway_water", 40)
        yardRun(level, "motor_launch_slipway_water", 70)
        for (z in 41..57) level.setBlock(BlockPos(100, 121, z), Blocks.STONE.defaultBlockState(), 3)
        yardRun(level, "sloop_drydock", 100)
        yardPool(level, 10)
        yardRun(level, "motor_launch_heavy_water", 10)
        yardObstruction(level)
        yardPool(level, -20)
        yardRun(level, "motor_launch_slipway_water", -20, "arcane_motor")
        yardShip = yardShips.getOrNull(0); yardShip2 = yardShips.getOrNull(1)
    }

    private val yardShips = ArrayList<ServerShip>()
    private val restSum = HashMap<Int, Double>()
    private val maxSpeed = HashMap<Int, Double>()
    private var yardSaveBe: net.diaithir.ships.yard.SlipwayBlockEntity? = null

    /** A replaced hull block must not be moved into the ship, and launch is refused while a hull cell is missing. */
    private fun yardObstruction(level: net.minecraft.server.level.ServerLevel) {
        val ctrl = BlockPos(-60, 122, 40)
        for (x in -66..-54) for (z in 38..58) level.setBlock(BlockPos(x, 121, z), Blocks.STONE.defaultBlockState(), 3)
        level.setBlock(ctrl, net.diaithir.ships.yard.ShipYardBlocks.SLIPWAY.get().defaultBlockState(), 3)
        val be = level.getBlockEntity(ctrl) as net.diaithir.ships.yard.SlipwayBlockEntity
        be.chooseYard("vs_yard:motor_launch_slipway_water")
        level.setBlock(ctrl.offset(3, 0, 0), net.diaithir.ships.yard.ShipYardBlocks.STEAM_CRANE.get().defaultBlockState(), 3)
        installAll(level, be, 99)
        val hull = be.hullBlocks()
        val victim = hull.first { level.getBlockState(it).block == Blocks.OAK_PLANKS }
        level.setBlock(victim, Blocks.DIRT.defaultBlockState(), 3)
        log.info("selftest-yard-obstruct: hull {} -> {} after replacing one block, missing={}, launch={}", hull.size, be.hullBlocks().size, be.missingCells().size, be.launch())
        // retained unresolved build: selection refuses, sneak-use discards, then selection works again
        val f = be.javaClass.getDeclaredField("rawSaved"); f.isAccessible = true
        f.set(be, Triple(mapOf("Yard" to "gone:yard", "Placed" to "", "Log" to ""), 2, 5))
        val refused = !be.chooseYard("vs_yard:motor_launch_slipway_water") && be.onUse(false).startsWith("This slipway holds") && be.holdsSavedBuild
        val discarded = be.onUse(true) == "Saved build discarded." && !be.holdsSavedBuild
        log.info("selftest-yard-retained: refused={} discarded={} reselect={}", refused, discarded, be.chooseYard("vs_yard:motor_launch_slipway_water"))
    }

    private fun yardRun(level: net.minecraft.server.level.ServerLevel, yardName: String, ox: Int, prefer: String? = null) {
        val ctrl = BlockPos(ox, 122, 40)
        val def = net.diaithir.yards.YardRegistry.yard("vs_yard:" + yardName).orElse(null)
        log.info("selftest-yard: {} def={} yards={}", yardName, def?.id(), net.diaithir.ships.yard.YardData.yardIds())
        if (def == null) return
        level.setBlock(ctrl, net.diaithir.ships.yard.ShipYardBlocks.SLIPWAY.get().defaultBlockState(), 3)
        val be = level.getBlockEntity(ctrl) as net.diaithir.ships.yard.SlipwayBlockEntity
        be.chooseYard("vs_yard:" + yardName)
        log.info("selftest-yard: siteCheck={}", be.siteCheck())
        val keel = net.diaithir.yards.YardRegistry.part("vs_yard:keel_beam").get()
        log.info("selftest-yard: keel without crane -> {}", be.install(keel))
        level.setBlock(ctrl.offset(3, 0, 0), net.diaithir.ships.yard.ShipYardBlocks.STEAM_CRANE.get().defaultBlockState(), 3)
        val b = be.build!!
        installAll(level, be, 99, prefer)
        val stats = b.stats()
        log.info("selftest-yard: complete={} stats={} hullBlocks={}", b.complete(), stats, be.hullBlocks().size)
        val ships0 = shipWorldOf(level).allShips.size
        val id = be.launch()
        val s = shipWorldOf(level).allShips.firstOrNull { it.id == id }
        if (s != null) yardShips.add(s)
        if (s != null) {
            val counts = java.util.TreeMap<String, Int>(); val bb = s.shipAABB; val p = BlockPos.MutableBlockPos()
            if (bb != null) for (x in bb.minX()..bb.maxX()) for (y in bb.minY()..bb.maxY()) for (z in bb.minZ()..bb.maxZ()) { val st = level.getBlockState(p.set(x, y, z)); if (!st.isAir) counts.merge(net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(st.block).toString(), 1, Int::plus) }
            log.info("selftest-yard-fit: {} blocks {}", yardName, counts)
        }
        val c = shipWorldOf(level).loadedShips.getById(id ?: -1)?.getAttachment(net.diaithir.ships.yard.YardShipControl::class.java)
        log.info("selftest-yard: launched shipId={} ships {} -> {} blockMass={} control={}", id, ships0, shipWorldOf(level).allShips.size, s?.inertiaData?.mass, c?.let { "buoy=" + it.buoyancyN + " thrust=" + it.thrustN + " statMass=" + it.statMassKg })
    }

    /** Registry/recipe checks for the merged Tournament content plus a ship assembled from its blocks. */
    private fun tournamentTest(server: net.minecraft.server.MinecraftServer, level: net.minecraft.server.level.ServerLevel) {
        val reg = net.minecraft.core.registries.BuiltInRegistries.BLOCK
        val blocks = reg.keySet().filter { it.namespace == "vs_tournament" }
        val items = net.minecraft.core.registries.BuiltInRegistries.ITEM.keySet().filter { it.namespace == "vs_tournament" }
        val recipes = server.recipeManager.recipes.filter { it.id().identifier().namespace == "vs_tournament" }
        val cooked = listOf("fuel/fuel_paste", "fuel/fuel_paste_with_campfire", "ingot_phynite")
        log.info("selftest-tournament-cooking: {}", cooked.joinToString { id -> id + "=" + recipes.any { it.id().identifier().path == id } })
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
        // hulls at rest (sloop, motor, heavy motor; thrust and anchors are anchors released at 300, thrust at 700): mean |vy| over ticks 601..700 must be near zero
        if (yardPhase == "" && tick in 601..700) {
            for (n in intArrayOf(0, 1, 3)) yardShips.getOrNull(n)?.let { restSum[n] = (restSum[n] ?: 0.0) + Math.abs(it.velocity.y()) }
            if (tick == 700) for ((n, sum) in restSum.toSortedMap()) log.info("selftest-yard-rest: ship{} mean|vy|={} -> {}", n, sum / 100.0, if (sum / 100.0 < 0.05) "PASS" else "FAIL")
        }
        if (yardPhase == "" && tick in 701..900) {
            for (n in intArrayOf(1, 3, 4)) yardShips.getOrNull(n)?.let { maxSpeed[n] = maxOf(maxSpeed[n] ?: 0.0, it.velocity.length()) }
            if (tick == 900) log.info("selftest-yard-thrust: max speed after thrust release {}", maxSpeed.toSortedMap())
        }
        when (tick) {
            2 -> preloadMixinTargets()
            20 -> {
                val cookie = net.minecraft.server.network.CommonListenerCookie.createInitial(
                    com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(), "vs-selftest"), false)
                val player = object : net.minecraft.server.level.ServerPlayer(server, level, cookie.gameProfile(), cookie.clientInformation()) {
                    override fun gameMode() = net.minecraft.world.level.GameType.CREATIVE
                }
                // EmbeddedChannel is not thread-safe, and VS sends packets from both the server and physics threads: sharing one made
                // the physics thread spin forever in ChannelOutboundBuffer.addFlush. The mock player has no reader anyway, so drop its packets.
                val connection = object : net.minecraft.network.Connection(net.minecraft.network.protocol.PacketFlow.SERVERBOUND) {
                    override fun send(packet: net.minecraft.network.protocol.Packet<*>, listener: io.netty.channel.ChannelFutureListener?, flush: Boolean) {}
                }
                io.netty.channel.embedded.EmbeddedChannel(connection)
                net.neoforged.neoforge.network.registration.ChannelAttributes.getOrCreateAdHocChannels(connection).add(VSPacket.VS_PACKET_RL)
                server.playerList.placeNewPlayer(connection, player, cookie)
                mockPlayer = player
                player.teleportTo(1.5, 130.0, 1.5)
                log.info("selftest: mock player placed")
            }
            110 -> eurekaTest(server, level)
            130 -> tournamentTest(server, level)
            160 -> if (net.diaithir.ships.yard.ShipYardMod.apiPresent) yardTest(server, level)
            200 -> if (yardPhase == "save") {
                // snapshot at the halt: the build keeps advancing after tick 160
                yardSaveBe?.let { be -> val b = be.build!!
                    val line = "stage=" + b.stageIndex() + " ticks=" + b.progressTicks() + " installed=" + b.installed().size + " stageParts=" + b.stageParts().size + " hull=" + be.hullBlocks().size
                    java.io.File("yardsave.txt").writeText(line)
                    log.info("selftest-yard-save: {}", line) }
                server.halt(false)
            }
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
            in 251..299 -> if (tick % 6 == 0) log.info("selftest-yard-ab: tick={} light(3010kg) speed={} z={} | heavy(6010kg) speed={} z={}", tick,
                yardShips.getOrNull(1)?.velocity?.length(), yardShips.getOrNull(1)?.transform?.position?.z(), yardShips.getOrNull(3)?.velocity?.length(), yardShips.getOrNull(3)?.transform?.position?.z())
            in 150..900 -> if (tick % 50 == 0) { if (tick == 300) yardShip?.let { y -> (shipWorldOf(level).loadedShips.getById(y.id))?.eurekaControl()?.anchorsActive = 0 }; if (tick == 700) yardShips.getOrNull(4)?.let { a -> shipWorldOf(level).loadedShips.getById(a.id)?.getAttachment(net.diaithir.ships.yard.YardShipControl::class.java)?.let { it2 -> log.info("selftest-yard: arcane engine={} accepts mana={} diesel={} addMana={} addDiesel={} stored={}", it2.engineId, net.diaithir.ships.yard.ArcaneFuel.accepts(it2.engineId, "mana"), net.diaithir.ships.yard.ArcaneFuel.accepts(it2.engineId, "diesel"), it2.addFuel("mana", 2_000_000.0), it2.addFuel("diesel", 5.0), it2.fuelStored) } }; if (tick == 700) listOf(yardShip2, yardShips.getOrNull(3), yardShips.getOrNull(4)).forEach { it?.let { s -> shipWorldOf(level).loadedShips.getById(s.id)?.getAttachment(net.diaithir.ships.yard.YardShipControl::class.java)?.manualThrottle = 1.0 } }; for ((n, ys) in listOf(yardShip, yardShip2, yardShips.getOrNull(2), yardShips.getOrNull(3), yardShips.getOrNull(4)).withIndex()) ys?.let { log.info("selftest-yard: ship{} tick={} static={} pos={} vel={} ctl={}", n, tick, it.isStatic, it.transform.position, it.velocity, shipWorldOf(level).loadedShips.getById(it.id)?.getAttachment(net.diaithir.ships.yard.YardShipControl::class.java)?.let { c -> "buoyApplied=" + c.lastBuoyancyApplied + " thrustApplied=" + c.lastThrustApplied + " cover=" + c.cover + " aabbY=" + it.worldAABB.minY() + ".." + it.worldAABB.maxY() + " waterAtCentre=" + level.getFluidState(BlockPos(70, 120, 46)).isEmpty + " overlap=" + c.lastOverlap + " physMass=" + c.physMass + " physSpeed=" + c.physSpeed + " physStatic=" + c.physStatic + " engine=" + c.engineId + " fuel=" + c.fuelStored }) } ; tournamentShip?.let { t -> log.info("selftest-tournament: tick={} ship y={} {}", tick, t.transform.position.y(), shipWorldOf(level).loadedShips.getById(t.id)?.let { l -> org.valkyrienskies.tournament.ship.TournamentShips.getOrCreate(l).debugCounts() }) } ; eurekaShip?.let { log.info("selftest-eureka: tick={} ship y={} vel={}", tick, it.transform.position.y(), it.velocity) } }
            in 140..400 -> if (tick % 40 == 0) ship?.let {
                log.info("selftest: tick={} ship y={} (start {}) vel={} all={} loaded={} static={} mass={} chunkLoaded={} block={}", tick, it.transform.position.y(), startY, it.velocity, shipWorldOf(level).allShips.size, shipWorldOf(level).loadedShips.size, it.isStatic, it.inertiaData.mass, level.chunkSource.getChunkNow(it.chunkClaim.getCenterBlockCoordinates(level.yRange).x shr 4, it.chunkClaim.getCenterBlockCoordinates(level.yRange).z shr 4) != null, level.getBlockState(it.chunkClaim.getCenterBlockCoordinates(level.yRange).let { c -> net.minecraft.core.BlockPos(c.x, c.y, c.z) }))
            }
        }
    }
}
