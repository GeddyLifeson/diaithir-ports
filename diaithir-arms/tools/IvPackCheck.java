import java.io.*; import java.nio.file.*; import java.util.*;
import minecrafttransportsimulator.packloading.JSONParser;
import minecrafttransportsimulator.jsondefs.*;
public class IvPackCheck {
  public static void main(String[] a) throws Exception {
    Path root = Paths.get(a[0]); String pk = a[1]; int ok = 0, bad = 0;
    for (String dir : new String[]{"vehicles","parts"}) {
      try (DirectoryStream<Path> ds = Files.newDirectoryStream(root.resolve("jsondefs").resolve(dir), "*.json")) {
        for (Path p : ds) {
          String n = p.getFileName().toString().replace(".json","");
          try (InputStream in = Files.newInputStream(p)) {
            Object o = dir.equals("vehicles") ? (Object) JSONParser.parseStream(in, JSONVehicle.class, pk, n) : (Object) JSONParser.parseStream(in, JSONPart.class, pk, n);
            if (o == null) throw new Exception("null"); ok++;
            // model + texture existence
            AJSONMultiModelProvider m = (AJSONMultiModelProvider) o;
            for (JSONSubDefinition d : m.definitions) {
              String mdl = d.modelName != null ? d.modelName : n;
              Path obj = root.resolve("objmodels").resolve(dir).resolve(mdl + ".obj");
              if (!Files.exists(obj) && !(dir.equals("parts") && !((JSONPart)o).general.type.startsWith("ground") && !((JSONPart)o).general.type.startsWith("seat") && !((JSONPart)o).general.type.startsWith("engine") && !((JSONPart)o).general.type.startsWith("interactable"))) { System.out.println("NO MODEL " + n + " " + mdl); bad++; }
              String tx = d.textureName != null ? d.textureName : n;
              Path png = root.resolve("textures").resolve(dir).resolve(tx + ".png");
              if (!Files.exists(png)) { System.out.println("NO TEX " + n + " " + tx); bad++; }
            }
          } catch (Throwable t) { System.out.println("FAIL " + n + " " + t); bad++; }
        }
      }
    }
    System.out.println("ok=" + ok + " bad=" + bad);
  }
}
