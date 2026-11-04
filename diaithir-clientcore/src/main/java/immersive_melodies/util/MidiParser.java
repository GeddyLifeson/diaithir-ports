package immersive_melodies.util;

import immersive_melodies.Common;
import immersive_melodies.resources.Melody;
import immersive_melodies.resources.Note;

import javax.sound.midi.*;
import java.io.InputStream;
import java.util.*;

public class MidiParser {
    public static Melody parseMidi(InputStream inputStream, String midiName) {
        Melody melody = new Melody(midiName, new LinkedList<>());
        try {
            Sequence sequence = MidiSystem.getSequence(inputStream);

            // Fetch shared tempo events
            List<MidiEvent> sharedEvents = new ArrayList<>();
            for (Track track : sequence.getTracks()) {
                getEvents(track).stream()
                        .filter(event -> event.getMessage() instanceof MetaMessage message
                                         && message.getType() == 0x51)
                        .forEach(sharedEvents::add);
            }

            List<ParsedTrack> parsedTracks = new ArrayList<>();

            // Iterate through tracks and MIDI events
            Track[] tracks = sequence.getTracks();
            for (int trackIndex = 0; trackIndex < tracks.length; trackIndex++) {
                List<MidiEvent> trackEvents = getEvents(tracks[trackIndex]);
                String trackName = getTrackName(trackEvents, trackIndex + 1);

                List<MidiEvent> events = new ArrayList<>(sharedEvents);
                trackEvents.stream()
                        .filter(event -> event.getMessage() instanceof ShortMessage)
                        .forEach(events::add);
                events.sort(Comparator.comparingLong(MidiEvent::getTick));

                double bpm = 120;
                long lastTick = 0;
                double time = 0;
                Map<Integer, ChannelData> channels = new TreeMap<>();

                for (MidiEvent event : events) {
                    // Convert notes into ms
                    long tick = event.getTick();
                    double deltaMs = ((tick - lastTick) * 60000.0) / (sequence.getResolution() * bpm);
                    time += deltaMs;
                    lastTick = tick;
                    int ms = (int) time;

                    MidiMessage message = event.getMessage();

                    // Parse tempo changes
                    if (message instanceof MetaMessage metaMessage) {
                        byte[] data = metaMessage.getData();
                        int microsecondsPerBeat =
                                ((data[0] & 0xFF) << 16) | ((data[1] & 0xFF) << 8) | (data[2] & 0xFF);
                        bpm = Math.round(60000000.0f / microsecondsPerBeat);
                        continue;
                    }

                    if (!(message instanceof ShortMessage sm)) {
                        continue;
                    }

                    int channel = sm.getChannel();
                    int command = sm.getCommand();
                    ChannelData channelData = channels.computeIfAbsent(channel, ignored -> new ChannelData());

                    // Use the first program change as an instrument hint
                    if (command == ShortMessage.PROGRAM_CHANGE && channelData.program == null) {
                        channelData.program = sm.getData1();
                        continue;
                    }

                    // Another way to decode note offs is note ons with velocity 0
                    if (command == ShortMessage.NOTE_ON && sm.getData2() == 0) {
                        command = ShortMessage.NOTE_OFF;
                    }

                    if (command == ShortMessage.NOTE_ON) {
                        int note = sm.getData1();
                        int velocity = sm.getData2();

                        // We simulate the minimum sustain as the time between repeated notes
                        if (channelData.currentNotes.containsKey(note)) {
                            Note.Builder previousNote = channelData.currentNotes.get(note);
                            previousNote.sustain = ms - previousNote.time;
                        }

                        channelData.currentNotes.put(note, new Note.Builder(note, velocity, ms));
                    } else if (command == ShortMessage.NOTE_OFF) {
                        int note = sm.getData1();
                        Note.Builder noteBuilder = channelData.currentNotes.remove(note);

                        if (noteBuilder != null) {
                            noteBuilder.length = ms - noteBuilder.time;
                            channelData.notes.add(noteBuilder.build());
                        }
                    }
                }

                for (Map.Entry<Integer, ChannelData> entry : channels.entrySet()) {
                    ChannelData channelData = entry.getValue();

                    if (channelData.notes.isEmpty()) {
                        continue;
                    }

                    channelData.notes.sort(Comparator.comparingInt(Note::getTime));

                    parsedTracks.add(new ParsedTrack(
                            trackIndex + 1,
                            entry.getKey() + 1,
                            trackName,
                            channelData.program == null ? null : MidiInstrumentMapping.INSTRUMENTS.get(channelData.program),
                            channelData.notes
                    ));
                }
            }

            // Use short names unless they are ambiguous
            Map<String, Integer> preferredNames = new HashMap<>();
            Map<String, Integer> expandedNames = new HashMap<>();

            for (ParsedTrack track : parsedTracks) {
                preferredNames.merge(track.preferredName(), 1, Integer::sum);
                expandedNames.merge(track.expandedName(), 1, Integer::sum);
            }

            for (ParsedTrack track : parsedTracks) {
                String name = track.preferredName();

                if (preferredNames.get(name) > 1) {
                    name = track.expandedName();

                    if (expandedNames.get(name) > 1) {
                        name = track.qualifiedName();
                    }
                }

                melody.addTrack(new immersive_melodies.resources.Track(name, track.notes()));
            }
        } catch (Exception e) {
            Common.LOGGER.error(e);
        }

        melody.trim();

        return melody;
    }

    private static String getTrackName(List<MidiEvent> events, int trackNr) {
        String name = "Track " + trackNr;

        for (MidiEvent event : events) {
            if (event.getMessage() instanceof MetaMessage message && message.getType() == 0x03) {
                String newName = new String(message.getData()).strip();

                if (!newName.isEmpty()) {
                    name = newName;
                }
            }
        }

        return name;
    }

    private static List<MidiEvent> getEvents(Track track) {
        List<MidiEvent> events = new ArrayList<>(track.size());

        for (int i = 0; i < track.size(); i++) {
            events.add(track.get(i));
        }

        return events;
    }

    private static class ChannelData {
        private final List<Note> notes = new ArrayList<>();
        private final Map<Integer, Note.Builder> currentNotes = new HashMap<>();
        private Integer program;
    }

    private record ParsedTrack(
            int trackId,
            int channelId,
            String trackName,
            String instrumentName,
            List<Note> notes
    ) {
        private String preferredName() {
            return instrumentName != null ? instrumentName : trackName;
        }

        private String expandedName() {
            if (instrumentName == null || instrumentName.equalsIgnoreCase(trackName)) {
                return trackName + " - Channel " + channelId;
            }

            return instrumentName + " - " + trackName;
        }

        private String qualifiedName() {
            if (instrumentName == null) {
                return trackName + " (" + trackId + ") - Channel " + channelId;
            }

            return instrumentName + " (" + channelId + ") - " + trackName + " (" + trackId + ")";
        }
    }
}
