package immersive_melodies.resources;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;


public class Melody extends MelodyDescriptor {
    public static final Melody DEFAULT = new Melody();

    private final List<Track> tracks = new LinkedList<>();

    public Melody() {
        super("unknown");

        addTrack(new Track("unknown", new LinkedList<>()));
    }

    public Melody(String name, List<Track> tracks) {
        super(name);

        this.tracks.addAll(tracks);
    }

    public List<Track> getTracks() {
        return Collections.unmodifiableList(tracks);
    }

    public int getLength() {
        int length = 0;
        for (Track track : tracks) {
            length = Math.max(length, track.getLength());
        }
        return length;
    }

    public void addTrack(Track track) {
        tracks.add(track);
    }

    public void trim() {
        int offset = getFirstNoteTime();
        for (Track track : tracks) {
            List<Note> newNotes = new LinkedList<>();
            for (Note note : track.getNotes()) {
                newNotes.add(new Note(
                        note.getNote(),
                        note.getVelocity(),
                        note.getTime() - offset,
                        note.getLength(),
                        note.getSustain()
                ));
            }
            track.setNotes(newNotes);
        }
    }

    private int getFirstNoteTime() {
        int earliestNote = Integer.MAX_VALUE;
        for (Track track : tracks) {
            List<Note> notes = track.getNotes();
            if (!notes.isEmpty()) {
                earliestNote = Math.min(earliestNote, notes.getFirst().getTime());
            }
        }
        return earliestNote;
    }

    public static StreamCodec<FriendlyByteBuf, Melody> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, Melody::getName,
            Track.STREAM_CODEC.apply(ByteBufCodecs.list()), Melody::getTracks,
            Melody::new
    );
}
