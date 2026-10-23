import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import java.nio.file.*;

public class Patch {
  public static void main(String[] a) throws Exception {
    ClassNode cn = new ClassNode();
    new ClassReader(Files.readAllBytes(Path.of(a[0]))).accept(cn, 0);
    int patched = 0;
    for (MethodNode m : cn.methods) {
      if (!m.name.equals("decode") || !m.desc.equals("(Lnet/minecraft/network/FriendlyByteBuf;Lslimeknights/mantle/util/typed/TypedMap;)Lnet/minecraft/core/Holder;")) continue;
      for (AbstractInsnNode i : m.instructions.toArray()) {
        if (i instanceof MethodInsnNode mi && mi.owner.equals("slimeknights/mantle/util/RegistryHelper") && mi.name.equals("getRegistry")) {
          // stack: ResourceKey -> need (buffer, key): insert aload_1 before the getstatic that pushes the key
          AbstractInsnNode prev = mi.getPrevious();
          m.instructions.insertBefore(prev, new VarInsnNode(Opcodes.ALOAD, 1));
          mi.owner = "dev/diaithir/fix/TConRegistryFix"; mi.name = "registry";
          mi.desc = "(Lnet/minecraft/network/FriendlyByteBuf;Lnet/minecraft/resources/ResourceKey;)Lnet/minecraft/core/Registry;";
          patched++;
        }
      }
    }
    if (patched != 1) throw new RuntimeException("patched " + patched);
    ClassWriter cw = new ClassWriter(0);
    cn.accept(cw);
    Files.write(Path.of(a[1]), cw.toByteArray());
    System.out.println("patched ok");
  }
}
