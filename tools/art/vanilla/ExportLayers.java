import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.*;
import net.minecraft.client.model.geom.builders.*;
import org.joml.Vector3f;
import java.io.*;
import java.lang.reflect.Field;
import java.util.*;

/** Dump every vanilla model layer as posed polygons with pixel UVs: java ExportLayers OUT.json [filter...] */
public class ExportLayers {
    public static void main(String[] a) throws Exception {
        Map<String, LayerDefinition> roots = new TreeMap<>();
        try (var jar = new java.util.zip.ZipFile(System.getProperty("mcjar"))) {
            for (var en : Collections.list(jar.entries())) {
                String n = en.getName();
                if (!n.startsWith("net/minecraft/client/model/") || !n.endsWith(".class")) continue;
                String cn = n.substring(0, n.length() - 6).replace('/', '.');
                try {
                    Class<?> c = Class.forName(cn, false, ExportLayers.class.getClassLoader());
                    for (var md : c.getDeclaredMethods()) {
                        if (!java.lang.reflect.Modifier.isStatic(md.getModifiers()) || (md.getReturnType() != LayerDefinition.class && md.getReturnType() != MeshDefinition.class)) continue;
                        Object[] args = new Object[md.getParameterCount()];
                        var pt = md.getParameterTypes(); boolean ok = true;
                        for (int i = 0; i < pt.length; i++) {
                            if (pt[i] == CubeDeformation.class) args[i] = CubeDeformation.NONE;
                            else if (pt[i] == boolean.class) args[i] = false;
                            else if (pt[i] == float.class) args[i] = 0f;
                            else ok = false;
                        }
                        if (!ok) continue;
                        md.setAccessible(true);
                        if (Arrays.asList(pt).contains(float.class)) {  // float args are offsets or scales: also export with 1
                            Object[] one = args.clone();
                            for (int i = 0; i < pt.length; i++) if (pt[i] == float.class) one[i] = 1f;
                            try {
                                Object r = md.invoke(null, one);
                                if (r instanceof MeshDefinition mesh) r = LayerDefinition.create(mesh, 64, cn.contains("Feline") || cn.contains("Wolf") ? 32 : 64);
                                roots.putIfAbsent(c.getSimpleName() + "#" + md.getName() + "@1", (LayerDefinition) r);
                            } catch (Throwable t) { System.err.println("skip " + cn + "." + md.getName() + "@1: " + t.getCause()); }
                        }
                        try { Object r = md.invoke(null, args);
                              if (r instanceof MeshDefinition mesh) r = LayerDefinition.create(mesh, 64, cn.contains("Feline") || cn.contains("Wolf") ? 32 : 64);
                              roots.putIfAbsent(c.getSimpleName() + "#" + md.getName(), (LayerDefinition) r); }
                        catch (Throwable t) { System.err.println("skip " + cn + "." + md.getName() + ": " + t.getCause()); }
                    }
                } catch (Throwable t) { System.err.println("skip class " + cn + ": " + t); }
            }
        }
        Field mat = LayerDefinition.class.getDeclaredField("material"); mat.setAccessible(true);
        Field xs = MaterialDefinition.class.getDeclaredField("xTexSize"); xs.setAccessible(true);
        Field ys = MaterialDefinition.class.getDeclaredField("yTexSize"); ys.setAccessible(true);
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (var e : roots.entrySet()) {
            String key = e.getKey();
            if (a.length > 1 && Arrays.stream(a, 1, a.length).noneMatch(key::matches)) continue;
            Object m = mat.get(e.getValue());
            int tw = xs.getInt(m), th = ys.getInt(m);
            ModelPart root;
            try { root = e.getValue().bakeRoot(); } catch (Throwable t) { System.err.println("skip " + key + ": " + t); continue; }
            StringBuilder f = new StringBuilder();
            root.visit(new PoseStack(), (pose, path, idx, cube) -> {
                for (ModelPart.Polygon p : cube.polygons) {
                    if (f.length() > 0) f.append(',');
                    f.append("{\"part\":\"").append(path).append("\",\"cube\":").append(idx)
                     .append(",\"box\":[").append(cube.maxX - cube.minX).append(',').append(cube.maxY - cube.minY).append(',').append(cube.maxZ - cube.minZ).append("]")
                     .append(",\"v\":[");
                    for (int i = 0; i < p.vertices().length; i++) {
                        ModelPart.Vertex v = p.vertices()[i];
                        Vector3f w = pose.pose().transformPosition(v.x() / 16f, v.y() / 16f, v.z() / 16f, new Vector3f());
                        f.append(i > 0 ? "," : "").append('[').append(w.x).append(',').append(w.y).append(',').append(w.z).append(',')
                         .append(v.u() * tw).append(',').append(v.v() * th).append(']');
                    }
                    f.append("]}");
                }
            });
            sb.append(first ? "" : ",").append("\n\"").append(key).append("\":{\"tw\":").append(tw).append(",\"th\":").append(th).append(",\"faces\":[").append(f).append("]}");
            first = false;
        }
        try (Writer w = new FileWriter(a[0])) { w.write(sb.append("\n}").toString()); }
    }
}
