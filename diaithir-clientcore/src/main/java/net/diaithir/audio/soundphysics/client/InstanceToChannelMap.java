// Ported from Sound Physics Perfected (MIT) https://github.com/FalseMSP/SoundPhysicsPerfected
// Modifications: extracted from the anonymous class in ShadowInstanceToChannelMixin into a normal class.
package net.diaithir.audio.soundphysics.client;

import java.util.AbstractMap;
import java.util.Map;
import java.util.Set;
import net.diaithir.audio.soundphysics.RaycastingHelper;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.ChannelAccess;

/** Wraps SoundEngine#instanceToChannel so original sounds resolve to their SPP wrappers' channels. */
public final class InstanceToChannelMap extends AbstractMap<SoundInstance, ChannelAccess.ChannelHandle> {
    private final Map<SoundInstance, ChannelAccess.ChannelHandle> original;

    public InstanceToChannelMap(Map<SoundInstance, ChannelAccess.ChannelHandle> original) {
        this.original = original;
    }

    private static SoundInstance wrapperOf(Object key) {
        if (!(key instanceof SoundInstance si)) return null;
        SoundInstance wrapped = RaycastingHelper.soundInstanceMap.get(si);
        return wrapped;
    }

    @Override
    public ChannelAccess.ChannelHandle get(Object key) {
        SoundInstance wrapped = wrapperOf(key);
        if (wrapped != null) {
            ChannelAccess.ChannelHandle handle = original.get(wrapped);
            if (handle != null) return handle;
        }
        return original.get(key);
    }

    @Override
    public boolean containsKey(Object key) {
        SoundInstance wrapped = wrapperOf(key);
        if (wrapped != null && original.containsKey(wrapped)) return true;
        return original.containsKey(key);
    }

    @Override
    public ChannelAccess.ChannelHandle put(SoundInstance key, ChannelAccess.ChannelHandle value) {
        return original.put(key, value);
    }

    @Override
    public ChannelAccess.ChannelHandle remove(Object key) {
        return original.remove(key);
    }

    @Override
    public void clear() {
        original.clear();
    }

    @Override
    public int size() {
        return original.size();
    }

    @Override
    public Set<Entry<SoundInstance, ChannelAccess.ChannelHandle>> entrySet() {
        return original.entrySet();
    }
}
