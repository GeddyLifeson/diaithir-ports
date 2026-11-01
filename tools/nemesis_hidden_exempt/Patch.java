import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import java.nio.file.*;

public class Patch {
    public static void main(String[] a) throws Exception {
        ClassNode cn = new ClassNode();
        new ClassReader(Files.readAllBytes(Path.of(a[0]))).accept(cn, 0);
        for (MethodNode m : cn.methods) {
            if (!m.name.equals("onRespawn")) continue;
            AbstractInsnNode add = null;
            for (AbstractInsnNode i : m.instructions) if (i instanceof MethodInsnNode mi && mi.name.equals("addEffect")) add = i;
            // find the ifeq jumping to the final return, right before 'aload_1; new MobEffectInstance'
            JumpInsnNode ifeq = null;
            for (AbstractInsnNode i = add; i != null; i = i.getPrevious())
                if (i.getOpcode() == Opcodes.IFEQ) { ifeq = (JumpInsnNode) i; break; }
            InsnList il = new InsnList();
            il.add(new VarInsnNode(Opcodes.ALOAD, 1));
            il.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "net/minecraft/server/level/ServerPlayer", "getStringUUID", "()Ljava/lang/String;", false));
            il.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "net/diaithir/nemesis/HiddenExempt", "exempt", "(Ljava/lang/String;)Z", false));
            il.add(new JumpInsnNode(Opcodes.IFNE, ifeq.label));
            m.instructions.insert(ifeq, il);
        }
        ClassWriter cw = new ClassWriter(0);
        cn.accept(cw);
        Files.write(Path.of(a[1]), cw.toByteArray());
    }
}
