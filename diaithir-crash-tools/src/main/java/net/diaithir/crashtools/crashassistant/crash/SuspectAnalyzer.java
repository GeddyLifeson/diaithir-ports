package net.diaithir.crashtools.crashassistant.crash;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLLoader;

public final class SuspectAnalyzer {
    public record Suspect(String modId, String modName, int hits) {}

    private SuspectAnalyzer() {}

    public static List<Suspect> analyze(List<String> frames, int depth) {
        Map<String, Integer> hits = new HashMap<>();
        Map<String, String> names = new HashMap<>();
        int limit = Math.min(depth, frames.size());
        for (int i = 0; i < limit; i++) {
            String cls = classOf(frames.get(i));
            String id = attribute(cls);
            if (id == null) continue;
            hits.merge(id, 1, Integer::sum);
            names.computeIfAbsent(id, k -> ModList.get().getModContainerById(k)
                    .map(c -> c.getModInfo().getDisplayName()).orElse(k));
        }
        List<Suspect> out = new ArrayList<>();
        hits.forEach((k, v) -> out.add(new Suspect(k, names.get(k), v)));
        out.sort(Comparator.comparingInt(Suspect::hits).reversed());
        return out.size() > 3 ? new ArrayList<>(out.subList(0, 3)) : out;
    }

    private static String classOf(String frame) {
        int p = frame.indexOf('/');
        String s = p >= 0 ? frame.substring(p + 1) : frame;
        int paren = s.indexOf('(');
        if (paren >= 0) s = s.substring(0, paren);
        int dot = s.lastIndexOf('.');
        return dot >= 0 ? s.substring(0, dot) : s;
    }

    private static String attribute(String cls) {
        if (cls.startsWith("net.minecraft.") || cls.startsWith("java.") || cls.startsWith("jdk.")
                || cls.startsWith("sun.") || cls.startsWith("com.mojang.") || cls.startsWith("net.neoforged.")
                || cls.startsWith("cpw.mods.") || cls.startsWith("org.spongepowered.") || cls.startsWith("it.unimi."))
            return null;
        String best = null;
        int bestLen = -1;
        try {
            for (var mod : ModList.get().getMods()) {
                String id = mod.getModId();
                if (id.equals("minecraft") || id.equals("neoforge")) continue;
                var file = mod.getOwningFile();
                if (file == null) continue;
                String root = guessRoot(cls, id);
                if (root != null && root.length() > bestLen) {
                    best = id;
                    bestLen = root.length();
                }
            }
        } catch (Throwable ignored) {
        }
        return best;
    }

    private static String guessRoot(String cls, String id) {
        String needle = "." + id.replace('-', '_') + ".";
        String lower = cls.toLowerCase();
        int i = (lower + ".").indexOf(needle);
        if (i >= 0) return cls.substring(0, Math.min(cls.length(), i + needle.length() - 1));
        String idn = id.replace('-', '_');
        if (lower.endsWith("." + idn)) return cls;
        return null;
    }
}
