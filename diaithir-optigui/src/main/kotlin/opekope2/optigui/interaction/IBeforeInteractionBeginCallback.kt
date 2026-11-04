package opekope2.optigui.interaction

import net.minecraft.client.gui.screens.Screen

/**
 * Callback fired before an interaction begins.
 */
fun interface IBeforeInteractionBeginCallback {
    /**
     * Called right before an interaction begins. This is the last place when calling [Interaction.prepare] is valid.
     *
     * @param screen The screen, which triggered the interaction and is about to be opened
     */
    fun onBeforeBegin(screen: Screen)

    companion object {
        /**
         * Event fired before an interaction begins.
         */
        @JvmField
        val EVENT: Event = Event()

        /**
         * Minimal replacement of Fabric's event object: an ordered listener list with a combined invoker.
         */
        class Event {
            private val listeners = java.util.concurrent.CopyOnWriteArrayList<IBeforeInteractionBeginCallback>()

            fun register(listener: IBeforeInteractionBeginCallback) {
                listeners += listener
            }

            fun invoker(): IBeforeInteractionBeginCallback = IBeforeInteractionBeginCallback { screen ->
                listeners.forEach { it.onBeforeBegin(screen) }
            }
        }
    }
}
