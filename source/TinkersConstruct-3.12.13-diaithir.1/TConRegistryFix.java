package dev.diaithir.fix;

import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import slimeknights.mantle.util.RegistryHelper;

/** Used by the patched TinkerLoadables$1.decode: play-phase buffers carry the connection's registry access (encode already uses it). */
public final class TConRegistryFix {
  private TConRegistryFix() {}

  public static <T> Registry<T> registry(FriendlyByteBuf buffer, ResourceKey<? extends Registry<T>> key) {
    if (buffer instanceof RegistryFriendlyByteBuf friendly) {
      return friendly.registryAccess().lookupOrThrow(key);
    }
    return RegistryHelper.getRegistry(key);
  }
}
