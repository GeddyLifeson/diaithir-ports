package org.valkyrienskies.tournament

import net.minecraft.core.registries.Registries
import org.valkyrienskies.tournament.advancements.BalloonShotTrigger
import org.valkyrienskies.tournament.advancements.ShipAssemblyTrigger
import org.valkyrienskies.tournament.registry.DeferredRegister
import org.valkyrienskies.tournament.util.extension.once

object TournamentTriggers {
    private val TRIGGERS = DeferredRegister.create(TournamentMod.MOD_ID, Registries.TRIGGER_TYPE)

    val SHIP_ASSEMBLY_TRIGGER = ShipAssemblyTrigger()
    val BALLOON_SHOT_TRIGGER = BalloonShotTrigger()

    val register by once {
        TRIGGERS.register("ship_assembled") { SHIP_ASSEMBLY_TRIGGER }
        TRIGGERS.register("balloon_shot") { BALLOON_SHOT_TRIGGER }
        TRIGGERS.applyAll()
    }
}
