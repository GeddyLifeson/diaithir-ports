package org.valkyrienskies.tournament.advancements

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.advancements.criterion.ContextAwarePredicate
import net.minecraft.advancements.criterion.EntityPredicate
import net.minecraft.advancements.criterion.MinMaxBounds
import net.minecraft.advancements.criterion.SimpleCriterionTrigger
import net.minecraft.server.level.ServerPlayer
import java.util.Optional

class ShipAssemblyTrigger : SimpleCriterionTrigger<ShipAssemblyTrigger.TriggerInstance>() {
    override fun codec(): Codec<TriggerInstance> = TriggerInstance.CODEC

    fun trigger(player: ServerPlayer, shipSize: Int) =
        trigger(player) { it.matches(shipSize) }

    class TriggerInstance(
        private val player: Optional<ContextAwarePredicate>,
        private val shipSize: MinMaxBounds.Ints
    ) : SimpleInstance {
        override fun player(): Optional<ContextAwarePredicate> = player

        fun matches(shipSize: Int): Boolean = this.shipSize.matches(shipSize)

        companion object {
            @JvmField
            val CODEC: Codec<TriggerInstance> = RecordCodecBuilder.create { i ->
                i.group(
                    EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter { it.player },
                    MinMaxBounds.Ints.CODEC.optionalFieldOf("ship_size", MinMaxBounds.Ints.ANY).forGetter { it.shipSize }
                ).apply(i, ::TriggerInstance)
            }
        }
    }
}
