package org.valkyrienskies.tournament

import org.valkyrienskies.core.api.event.EventConsumer
import org.valkyrienskies.core.api.event.RegisteredListener
import org.valkyrienskies.core.api.event.SingleEvent
import org.valkyrienskies.core.util.events.EventEmitterImpl

// kotlin <-> jvm interop moment
class EventWrapper<T>(private val impl: SingleEvent<T>) : SingleEvent<T> {
    override fun on(cb: EventConsumer<T>): RegisteredListener =
        impl.on(cb)

    override fun emit(value: T) =
        impl.emit(value)
}
