import org.objectweb.asm.*;
import java.nio.file.*;
import java.util.*;

public class PatchClaims {
    static int map(int v) {
        switch (v) { case -7000: return -4800; case 7000: return 4800; case 7001: return 4801; default: return v; }
    }
    public static void main(String[] a) throws Exception {
        for (String f : a) {
            byte[] in = Files.readAllBytes(Path.of(f));
            ClassReader cr = new ClassReader(in);
            ClassWriter cw = new ClassWriter(0);
            int[] n = {0};
            cr.accept(new ClassVisitor(Opcodes.ASM9, cw) {
                public FieldVisitor visitField(int acc, String name, String desc, String sig, Object val) {
                    if (val instanceof Integer i && map(i) != i) { n[0]++; val = map(i); }
                    return super.visitField(acc, name, desc, sig, val);
                }
                public MethodVisitor visitMethod(int acc, String name, String desc, String sig, String[] ex) {
                    return new MethodVisitor(Opcodes.ASM9, super.visitMethod(acc, name, desc, sig, ex)) {
                        public void visitIntInsn(int op, int operand) {
                            if (op == Opcodes.SIPUSH && map(operand) != operand) { n[0]++; operand = map(operand); }
                            super.visitIntInsn(op, operand);
                        }
                        public void visitLdcInsn(Object v) {
                            if (v instanceof Integer i && map(i) != i) { n[0]++; v = map(i); }
                            super.visitLdcInsn(v);
                        }
                    };
                }
            }, 0);
            Files.write(Path.of(f), cw.toByteArray());
            System.out.println(f + " patched " + n[0]);
        }
    }
}
