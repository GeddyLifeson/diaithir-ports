package net.diaithir.ships.yard

import net.minecraft.server.MinecraftServer
import net.neoforged.fml.ModList

/** The server calendar Age as 0 (Telling) to 7 (Age VII), read from diaithir_ages by reflection (soft dependency, same as diaithir-myth SpawnGate). */
object AgeLookup {
    /** Used instead of the calendar when set (selftest). */
    @Volatile var override: Int? = null
    private val ORDER = listOf("TELLING", "I", "II", "III", "IV", "V", "VI", "VII", "RODOS")
    private var broken = false

    /** No diaithir_ages mod means no calendar to gate by, so nothing is locked. */
    const val UNGATED = 99

    fun current(server: MinecraftServer): Int {
        override?.let { return it }
        if (broken || !ModList.get().isLoaded("diaithir_ages")) return UNGATED
        return try {
            val saved = Class.forName("net.diaithir.ages.mc.AgesSavedData")
            val data = saved.getMethod("get", MinecraftServer::class.java).invoke(null, server)
            val world = saved.getField("world").get(data)
            val age = world.javaClass.getField("age").get(world) as String
            ORDER.indexOf(age.uppercase()).let { if (it < 0) UNGATED else it }
        } catch (e: java.lang.reflect.InvocationTargetException) {
            org.slf4j.LoggerFactory.getLogger("vs_yard").warn("diaithir_ages lookup threw, parts ungated for now: {}", e.cause.toString()); UNGATED
        } catch (e: ReflectiveOperationException) {
            broken = true; org.slf4j.LoggerFactory.getLogger("vs_yard").warn("diaithir_ages lookup unavailable, yard parts ungated: {}", e.toString()); UNGATED
        } catch (e: Throwable) {
            org.slf4j.LoggerFactory.getLogger("vs_yard").warn("diaithir_ages lookup failed this call, parts ungated for now: {}", e.toString()); UNGATED
        }
    }
}
