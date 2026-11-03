package org.valkyrienskies.tournament.advancements

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.advancements.criterion.ContextAwarePredicate
import net.minecraft.advancements.criterion.EntityPredicate
import net.minecraft.advancements.criterion.SimpleCriterionTrigger
import net.minecraft.server.level.ServerPlayer
import java.util.Optional

class BalloonShotTrigger : SimpleCriterionTrigger<BalloonShotTrigger.TriggerInstance>() {
    override fun codec(): Codec<TriggerInstance> = TriggerInstance.CODEC

    fun trigger(player: ServerPlayer) =
        trigger(player) { true }

    class TriggerInstance(private val player: Optional<ContextAwarePredicate>) : SimpleInstance {
        override fun player(): Optional<ContextAwarePredicate> = player

        companion object {
            @JvmField
            val CODEC: Codec<TriggerInstance> = RecordCodecBuilder.create { i ->
                i.group(
                    EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter { it.player() }
                ).apply(i, ::TriggerInstance)
            }
        }
    }
}
