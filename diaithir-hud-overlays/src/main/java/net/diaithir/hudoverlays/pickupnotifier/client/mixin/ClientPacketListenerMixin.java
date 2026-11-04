// Ported from Pick Up Notifier (MPL-2.0) https://github.com/Fuzss/pickupnotifier
// Modifications: Puzzles Lib removed, NeoForge-only, compile-fixed for 26.1.2.
package net.diaithir.hudoverlays.pickupnotifier.client.mixin;

import net.minecraft.client.multiplayer.ClientPacketListener;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ClientPacketListener.class)
abstract class ClientPacketListenerMixin {
}
