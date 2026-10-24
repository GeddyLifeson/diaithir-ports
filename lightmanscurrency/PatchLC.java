import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import java.nio.file.*;
import java.util.*;
import java.util.zip.*;
import java.io.*;

/**
 * Lightman's Currency 26.1-1.0.0.1 -> diaithir.1.
 * Three classes in the client package carry a bare @EventBusSubscriber (no dist), so NeoForge
 * registers them on the dedicated server too, reflects over their client-typed event handlers and
 * dies with NoClassDefFoundError (net/minecraft/client/renderer/entity/RenderLayerParent, ...).
 * This sets value = Dist.CLIENT on them. Nothing else in the jar changes.
 * usage: java -cp asm.jar:asm-tree.jar:. PatchLC in.jar out.jar
 */
public class PatchLC {
  static final String BASE = "io/github/lightman314/lightmanscurrency/";
  static final Set<String> TARGETS = Set.of(
      BASE + "client/features/wallet/WalletClientEvents.class",
      BASE + "client/LCClientRecipeCache.class",
      BASE + "client/features/admin/ClientAdminMode.class");

  public static void main(String[] a) throws Exception {
    int patched = 0;
    try (ZipFile in = new ZipFile(a[0]);
         ZipOutputStream out = new ZipOutputStream(new FileOutputStream(a[1]))) {
      for (Enumeration<? extends ZipEntry> e = in.entries(); e.hasMoreElements();) {
        ZipEntry ze = e.nextElement();
        byte[] data = in.getInputStream(ze).readAllBytes();
        if (TARGETS.contains(ze.getName())) { data = patch(data); patched++; }
        ZipEntry ne = new ZipEntry(ze.getName());
        ne.setTime(ze.getTime());
        out.putNextEntry(ne);
        out.write(data);
        out.closeEntry();
      }
    }
    if (patched != TARGETS.size()) throw new RuntimeException("patched " + patched);
    System.out.println("patched ok: " + patched + " classes");
  }

  static byte[] patch(byte[] b) {
    ClassNode cn = new ClassNode();
    new ClassReader(b).accept(cn, 0);
    int n = 0;
    for (AnnotationNode an : cn.visibleAnnotations) {
      if (!an.desc.equals("Lnet/neoforged/fml/common/EventBusSubscriber;")) continue;
      if (an.values != null && an.values.contains("value")) throw new RuntimeException("already has value: " + cn.name);
      if (an.values == null) an.values = new ArrayList<>();
      an.values.add("value");
      List<Object> dists = new ArrayList<>();
      dists.add(new String[] {"Lnet/neoforged/api/distmarker/Dist;", "CLIENT"});
      an.values.add(dists);
      n++;
    }
    if (n != 1) throw new RuntimeException("no annotation on " + cn.name);
    ClassWriter cw = new ClassWriter(0);
    cn.accept(cw);
    return cw.toByteArray();
  }
}
