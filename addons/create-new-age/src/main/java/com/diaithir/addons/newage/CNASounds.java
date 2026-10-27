package com.diaithir.addons.newage;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class CNASounds {
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(Registries.SOUND_EVENT, CreateNewAge.MOD_ID);

    public static final Sound GEIGER_COUNTER = new Sound("geiger_counter", SoundSource.PLAYERS);

    public static void load() {}

    public static final class Sound {
        private final DeferredHolder<SoundEvent, SoundEvent> event;
        private final SoundSource category;

        Sound(String name, SoundSource category) {
            this.category = category;
            this.event = SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(CreateNewAge.MOD_ID, name)));
        }

        public void playOnServer(Level level, BlockPos pos, float volume, float pitch) {
            level.playSound(null, pos, event.get(), category, volume, pitch);
        }
    }
}
