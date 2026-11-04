package com.diaithir.overworldbiomes.naturesspirit.registration;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

@SuppressWarnings("unused")
public class NSSounds {

  public static final String MOD_ID = "natures_spirit";

  public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(BuiltInRegistries.SOUND_EVENT, MOD_ID);

  public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_OVERWORLD_ASPEN = registerReference("music.overworld.aspen");
  public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_OVERWORLD_MAPLE = registerReference("music.overworld.maple");
  public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_OVERWORLD_WISTERIA = registerReference("music.overworld.wisteria");
  public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_OVERWORLD_REDWOOD = registerReference("music.overworld.redwood");
  public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_OVERWORLD_DESERT = registerReference("music.overworld.desert");
  public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_OVERWORLD_ARID = registerReference("music.overworld.arid");
  public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_OVERWORLD_TROPICAL = registerReference("music.overworld.tropical");
  public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_OVERWORLD_ALPINE = registerReference("music.overworld.alpine");

  private static DeferredHolder<SoundEvent, SoundEvent> registerReference(String id) {
    return registerReference(id, Identifier.fromNamespaceAndPath(MOD_ID, id));
  }

  private static DeferredHolder<SoundEvent, SoundEvent> registerReference(String id, Identifier soundId) {
    return SOUND_EVENTS.register(id, () -> SoundEvent.createVariableRangeEvent(soundId));
  }

  public static void init(IEventBus modBus) {
    SOUND_EVENTS.register(modBus);
  }

  public static void registerSounds() {
  }
}
