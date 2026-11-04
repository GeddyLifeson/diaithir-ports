package immersive_melodies.resources;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.Collections;
import java.util.List;

public class Track {
    private final String name;
    private final List<Note> notes;
    private int cachedLength = -1;

    public Track(String name, List<Note> notes) {
        this.name = name;
        this.notes = notes;
    }

    public List<Note> getNotes() {
        return Collections.unmodifiableList(notes);
    }

    public String getName() {
        return name;
    }

    public int getLength() {
        if (cachedLength >= 0) {
            return cachedLength;
        }
        cachedLength = notes.stream()
                .mapToInt(note -> note.getTime() + note.getLength())
                .max()
                .orElse(0);
        return cachedLength;
    }

    public void setNotes(List<Note> notes) {
        this.notes.clear();
        this.notes.addAll(notes);
        this.cachedLength = -1;
    }

    public static StreamCodec<FriendlyByteBuf, Track> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, Track::getName,
            Note.STREAM_CODEC.apply(ByteBufCodecs.list()), Track::getNotes,
            Track::new
    );
}
