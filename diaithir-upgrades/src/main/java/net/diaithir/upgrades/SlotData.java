package net.diaithir.upgrades;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;

/** Saved form of the modules in one block: ids and quality only, specs are looked up on load. */
public record SlotData(List<Entry> modules) {
    public record Entry(String module, String quality) {
        public static final Codec<Entry> CODEC = RecordCodecBuilder.create(i -> i.group(
                Codec.STRING.fieldOf("module").forGetter(Entry::module),
                Codec.STRING.fieldOf("quality").forGetter(Entry::quality)).apply(i, Entry::new));
    }

    public static final SlotData EMPTY = new SlotData(List.of());

    public static final MapCodec<SlotData> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Entry.CODEC.listOf().fieldOf("modules").forGetter(SlotData::modules)).apply(i, SlotData::new));

    public static SlotData of(Slotted s) {
        List<Entry> out = new ArrayList<>();
        for (Slotted.Installed m : s.modules()) out.add(new Entry(m.spec().id(), m.quality().name()));
        return new SlotData(out);
    }

    /** Rebuild; modules whose id is no longer registered or no longer fit are dropped. */
    public Slotted toSlotted(Target target) {
        Slotted s = new Slotted(target);
        for (Entry e : modules) {
            ModuleRegistry.get(e.module()).ifPresent(spec -> {
                try {
                    s.install(spec, Quality.valueOf(e.quality()));
                } catch (IllegalArgumentException ignored) {
                    // unknown quality name
                }
            });
        }
        return s;
    }
}
