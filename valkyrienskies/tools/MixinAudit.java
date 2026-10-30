import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.regex.*;
import java.util.zip.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

/** Static check of every mixin in a mod jar against the target jars: missing targets, injector return types
 *  (CallbackInfo vs CallbackInfoReturnable), handler args, static-ness, INVOKE/FIELD/NEW references, shadows, accessors.
 *  Usage: java -cp asm*.jar MixinAudit.java <mod jar> <target jar>... */
public class MixinAudit {
    static Map<String, byte[]> classes = new HashMap<>();
    static Map<String, ClassNode> cache = new HashMap<>();
    static int errors = 0, warnings = 0;
    static String current;

    static ClassNode node(String name) {
        if (cache.containsKey(name)) return cache.get(name);
        byte[] b = classes.get(name);
        ClassNode cn = null;
        if (b != null) { cn = new ClassNode(); new ClassReader(b).accept(cn, 0); }
        cache.put(name, cn);
        return cn;
    }

    static void err(String m) { errors++; System.out.println("ERROR " + current + ": " + m); }
    static void warn(String m) { warnings++; System.out.println("WARN  " + current + ": " + m); }

    static Map<String, Object> values(AnnotationNode a) {
        Map<String, Object> m = new HashMap<>();
        if (a.values != null) for (int i = 0; i < a.values.size(); i += 2) m.put((String) a.values.get(i), a.values.get(i + 1));
        return m;
    }

    static List<AnnotationNode> anns(List<AnnotationNode> v, List<AnnotationNode> iv) {
        List<AnnotationNode> l = new ArrayList<>();
        if (v != null) l.addAll(v);
        if (iv != null) l.addAll(iv);
        return l;
    }

    @SuppressWarnings("unchecked")
    static List<Object> list(Object o) { return o == null ? List.of() : o instanceof List ? (List<Object>) o : List.of(o); }

    public static void main(String[] args) throws Exception {
        for (int i = 1; i < args.length; i++) load(args[i], true);
        Map<String, byte[]> mod = new HashMap<>();
        List<String> configs = new ArrayList<>();
        try (ZipFile z = new ZipFile(args[0])) {
            for (var e : Collections.list(z.entries())) {
                if (e.getName().endsWith(".class")) mod.put(e.getName().replace(".class", ""), z.getInputStream(e).readAllBytes());
                else if (e.getName().endsWith(".mixins.json") && !e.getName().contains("/")) configs.add(new String(z.getInputStream(e).readAllBytes()));
            }
        }
        classes.putAll(mod);
        int total = 0;
        for (String cfg : configs) {
            Matcher pm = Pattern.compile("\"package\"\\s*:\\s*\"([^\"]+)\"").matcher(cfg);
            if (!pm.find()) continue;
            String pkg = pm.group(1).replace('.', '/');
            for (String arr : new String[]{"mixins", "client", "server"}) {
                Matcher am = Pattern.compile("\"" + arr + "\"\\s*:\\s*\\[([^\\]]*)\\]").matcher(cfg);
                if (!am.find()) continue;
                Matcher nm = Pattern.compile("\"([^\"]+)\"").matcher(am.group(1));
                while (nm.find()) { total++; audit(pkg + "/" + nm.group(1).replace('.', '/')); }
            }
        }
        System.out.println(total + " mixins, " + errors + " errors, " + warnings + " warnings");
    }

    static void load(String jar, boolean overwrite) throws IOException {
        try (ZipFile z = new ZipFile(jar)) {
            for (var e : Collections.list(z.entries()))
                if (e.getName().endsWith(".class") && !e.getName().startsWith("META-INF"))
                    classes.putIfAbsent(e.getName().replace(".class", ""), z.getInputStream(e).readAllBytes());
        }
    }

    static void audit(String mixinName) {
        current = mixinName.substring(mixinName.lastIndexOf('/') + 1);
        ClassNode mx = node(mixinName);
        if (mx == null) { err("mixin class missing from jar"); return; }
        List<String> targets = new ArrayList<>();
        for (AnnotationNode a : anns(mx.visibleAnnotations, mx.invisibleAnnotations)) {
            if (!a.desc.equals("Lorg/spongepowered/asm/mixin/Mixin;")) continue;
            Map<String, Object> v = values(a);
            for (Object t : list(v.get("value"))) targets.add(((Type) t).getInternalName());
            for (Object t : list(v.get("targets"))) targets.add(((String) t).replace('.', '/'));
        }
        for (String t : targets) {
            ClassNode tn = node(t);
            if (tn == null) {
                boolean pseudo = anns(mx.visibleAnnotations, mx.invisibleAnnotations).stream().anyMatch(a -> a.desc.equals("Lorg/spongepowered/asm/mixin/Pseudo;"));
                if (pseudo) warn("@Pseudo target not on classpath (not checked): " + t); else err("target class not found: " + t);
                continue;
            }
            String saved = current;
            current = saved + " -> " + t.substring(t.lastIndexOf('/') + 1);
            check(mx, tn);
            current = saved;
        }
    }

    static boolean hasMember(ClassNode c, String name, String desc, boolean field) {
        for (ClassNode k = c; k != null; k = k.superName == null ? null : node(k.superName)) {
            if (field) { for (FieldNode f : k.fields) if (f.name.equals(name) && (desc == null || f.desc.equals(desc))) return true; }
            else for (MethodNode m : k.methods) if (m.name.equals(name) && (desc == null || m.desc.equals(desc))) return true;
            if (!field) for (String i : k.interfaces) { ClassNode in = node(i); if (in != null && hasMember(in, name, desc, false)) return true; }
        }
        return false;
    }

    static final Set<String> INJECTORS = Set.of(
        "Lorg/spongepowered/asm/mixin/injection/Inject;", "Lorg/spongepowered/asm/mixin/injection/Redirect;",
        "Lorg/spongepowered/asm/mixin/injection/ModifyArg;", "Lorg/spongepowered/asm/mixin/injection/ModifyArgs;",
        "Lorg/spongepowered/asm/mixin/injection/ModifyVariable;", "Lorg/spongepowered/asm/mixin/injection/ModifyConstant;",
        "Lcom/llamalad7/mixinextras/injector/wrapoperation/WrapOperation;", "Lcom/llamalad7/mixinextras/injector/v2/WrapWithCondition;",
        "Lcom/llamalad7/mixinextras/injector/WrapWithCondition;", "Lcom/llamalad7/mixinextras/injector/ModifyExpressionValue;",
        "Lcom/llamalad7/mixinextras/injector/ModifyReturnValue;", "Lcom/llamalad7/mixinextras/injector/ModifyReceiver;",
        "Lcom/llamalad7/mixinextras/injector/wrapmethod/WrapMethod;");

    static void check(ClassNode mx, ClassNode tn) {
        for (FieldNode f : mx.fields)
            for (AnnotationNode a : anns(f.visibleAnnotations, f.invisibleAnnotations))
                if (a.desc.equals("Lorg/spongepowered/asm/mixin/Shadow;")) {
                    String name = shadowName(f.name, a);
                    if (!hasMember(tn, name, f.desc, true)) err("@Shadow field missing: " + name + " " + f.desc);
                }
        for (MethodNode m : mx.methods) {
            for (AnnotationNode a : anns(m.visibleAnnotations, m.invisibleAnnotations)) {
                Map<String, Object> v = values(a);
                switch (a.desc) {
                    case "Lorg/spongepowered/asm/mixin/Shadow;" -> {
                        String name = shadowName(m.name, a);
                        if (!hasMember(tn, name, m.desc, false)) err("@Shadow method missing: " + name + m.desc);
                    }
                    case "Lorg/spongepowered/asm/mixin/Overwrite;" -> {
                        if (!hasMember(tn, m.name, m.desc, false)) err("@Overwrite target missing: " + m.name + m.desc);
                    }
                    case "Lorg/spongepowered/asm/mixin/gen/Accessor;" -> {
                        String name = v.containsKey("value") ? (String) v.get("value") : strip(m.name, "get", "set", "is");
                        Type t = Type.getReturnType(m.desc);
                        String fd = t.getSort() == Type.VOID ? Type.getArgumentTypes(m.desc)[0].getDescriptor() : t.getDescriptor();
                        if (!hasMember(tn, name, fd, true)) err("@Accessor field missing: " + name + " " + fd);
                    }
                    case "Lorg/spongepowered/asm/mixin/gen/Invoker;" -> {
                        String name = v.containsKey("value") ? (String) v.get("value") : strip(m.name, "invoke", "call", "new", "create");
                        String desc = name.equals("<init>") ? Type.getMethodDescriptor(Type.VOID_TYPE, Type.getArgumentTypes(m.desc)) : m.desc;
                        if (!hasMember(tn, name, desc, false)) err("@Invoker method missing: " + name + desc);
                    }
                    default -> { if (INJECTORS.contains(a.desc)) injector(mx, tn, m, a, v); }
                }
            }
        }
    }

    static String shadowName(String name, AnnotationNode a) {
        Object p = values(a).get("prefix");
        String prefix = p == null ? "shadow$" : (String) p;
        return name.startsWith(prefix) ? name.substring(prefix.length()) : name;
    }

    static String strip(String n, String... pre) {
        for (String p : pre) if (n.startsWith(p) && n.length() > p.length()) {
            String r = n.substring(p.length());
            return Character.toLowerCase(r.charAt(0)) + r.substring(1);
        }
        return n;
    }

    static List<MethodNode> select(ClassNode tn, String sel) {
        String s = sel;
        if (s.startsWith("L") && s.contains(";")) s = s.substring(s.indexOf(';') + 1);
        String name = s, desc = null;
        int p = s.indexOf('(');
        if (p >= 0) { name = s.substring(0, p); desc = s.substring(p); }
        if (name.contains(":")) name = name.substring(0, name.indexOf(':'));
        List<MethodNode> out = new ArrayList<>();
        Pattern re = Pattern.compile(Pattern.quote(name).replace("*", "\\E.*\\Q"));
        for (MethodNode m : tn.methods)
            if ((name.contains("*") ? re.matcher(m.name).matches() : m.name.equals(name)) && (desc == null || m.desc.equals(desc))) out.add(m);
        return out;
    }

    @SuppressWarnings("unchecked")
    static void injector(ClassNode mx, ClassNode tn, MethodNode h, AnnotationNode a, Map<String, Object> v) {
        String kind = a.desc.substring(a.desc.lastIndexOf('/') + 1, a.desc.length() - 1);
        int require = v.containsKey("require") ? (Integer) v.get("require") : 1;
        List<MethodNode> tms = new ArrayList<>();
        for (Object sel : list(v.get("method"))) {
            List<MethodNode> found = select(tn, (String) sel);
            if (found.isEmpty()) report(require, kind + " " + h.name + ": target method not found: " + sel);
            tms.addAll(found);
        }
        Map<String, Integer> hitsByAt = new LinkedHashMap<>();
        for (Object at : list(v.get("at"))) if (checkable((AnnotationNode) at)) hitsByAt.put(atKey((AnnotationNode) at), 0);
        for (MethodNode tm : tms) {
            boolean anyHit = hitsByAt.isEmpty();
            for (Object at : list(v.get("at"))) {
                int n = countAt(tm, (AnnotationNode) at);
                if (n != 0) anyHit = true;
                if (n > 0) {
                    hitsByAt.merge(atKey((AnnotationNode) at), n, Integer::sum);
                    if (kind.equals("WrapOperation") || kind.equals("Redirect")) checkWrapSignature(kind, h, tm, (AnnotationNode) at);
                }
            }
            if (!anyHit) continue;
            boolean tStatic = (tm.access & Opcodes.ACC_STATIC) != 0, hStatic = (h.access & Opcodes.ACC_STATIC) != 0;
            if (tStatic && !hStatic) err(kind + " " + h.name + ": target " + tm.name + " is static, handler is not");
            if (kind.equals("Inject")) {
                Type[] hp = Type.getArgumentTypes(h.desc), tp = Type.getArgumentTypes(tm.desc);
                boolean voidT = tm.name.equals("<init>") || Type.getReturnType(tm.desc).getSort() == Type.VOID;
                int ci = -1;
                for (int i = 0; i < hp.length; i++) if (hp[i].getSort() == Type.OBJECT && hp[i].getInternalName().startsWith("org/spongepowered/asm/mixin/injection/callback/CallbackInfo")) { ci = i; break; }
                if (ci < 0) err("Inject " + h.name + ": no CallbackInfo parameter");
                else {
                    boolean cir = hp[ci].getInternalName().endsWith("CallbackInfoReturnable");
                    if (voidT && cir) err("Inject " + h.name + ": target " + tm.name + tm.desc + " returns void, handler takes CallbackInfoReturnable");
                    if (!voidT && !cir) err("Inject " + h.name + ": target " + tm.name + tm.desc + " returns a value, handler needs CallbackInfoReturnable");
                    if (ci != 0 && ci != tp.length) err("Inject " + h.name + ": handler takes " + ci + " target args, target " + tm.name + tm.desc + " has " + tp.length);
                    else for (int i = 0; i < ci; i++) if (!hp[i].equals(tp[i])) { err("Inject " + h.name + ": arg " + i + " is " + hp[i] + ", target has " + tp[i] + " in " + tm.name + tm.desc); break; }
                }
            }
        }
        if (!tms.isEmpty()) for (var e : hitsByAt.entrySet())
            if (e.getValue() == 0) report(require, kind + " " + h.name + " in " + tms.size() + " target method(s): @At not found: " + e.getKey());
    }

    static void checkWrapSignature(String kind, MethodNode h, MethodNode tm, AnnotationNode at) {
        Map<String, Object> v = values(at);
        String target = (String) v.get("target");
        for (AbstractInsnNode in : tm.instructions) {
            if (!(in instanceof MethodInsnNode mi) || !"INVOKE".equals(v.get("value")) || !matchMember(target, mi.owner, mi.name, mi.desc)) continue;
            List<Type> expected = new ArrayList<>();
            if (mi.getOpcode() != Opcodes.INVOKESTATIC) expected.add(Type.getObjectType(mi.owner));
            expected.addAll(List.of(Type.getArgumentTypes(mi.desc)));
            Type[] hp = Type.getArgumentTypes(h.desc);
            if (hp.length < expected.size()) { err(kind + " " + h.name + ": handler has " + hp.length + " params, call " + mi.owner + "." + mi.name + mi.desc + " needs " + expected.size()); return; }
            for (int i = 0; i < expected.size(); i++)
                if (!hp[i].equals(expected.get(i))) { err(kind + " " + h.name + ": param " + i + " is " + hp[i] + ", call " + mi.name + mi.desc + " has " + expected.get(i)); return; }
            if (kind.equals("WrapOperation") && (hp.length == expected.size() || !hp[expected.size()].getInternalName().equals("com/llamalad7/mixinextras/injector/wrapoperation/Operation")))
                err(kind + " " + h.name + ": Operation must follow the call's " + expected.size() + " params");
            if (!Type.getReturnType(h.desc).equals(Type.getReturnType(mi.desc)))
                err(kind + " " + h.name + ": returns " + Type.getReturnType(h.desc) + ", call returns " + Type.getReturnType(mi.desc));
            return;
        }
    }

    static boolean checkable(AnnotationNode at) {
        Map<String, Object> v = values(at);
        return v.get("target") != null && Set.of("INVOKE", "INVOKE_ASSIGN", "INVOKE_STRING", "FIELD", "NEW").contains(v.get("value"));
    }

    static String atKey(AnnotationNode at) {
        Map<String, Object> v = values(at);
        return v.get("value") + (v.containsKey("target") ? " " + v.get("target") : "") + (v.containsKey("ordinal") ? " ordinal " + v.get("ordinal") : "");
    }

    /** Matches of an @At in one method; -1 when the kind of @At is not checked (HEAD, TAIL, RETURN...). */
    static int countAt(MethodNode tm, AnnotationNode at) {
        Map<String, Object> v = values(at);
        String value = (String) v.get("value");
        String target = (String) v.get("target");
        int ordinal = v.containsKey("ordinal") ? (Integer) v.get("ordinal") : -1;
        if (target == null || value == null) return -1;
        int hits = 0;
        for (AbstractInsnNode in : tm.instructions) {
            switch (value) {
                case "INVOKE", "INVOKE_ASSIGN", "INVOKE_STRING" -> { if (in instanceof MethodInsnNode mi && matchMember(target, mi.owner, mi.name, mi.desc)) hits++; }
                case "FIELD" -> { if (in instanceof FieldInsnNode fi && matchMember(target, fi.owner, fi.name, ":" + fi.desc)) hits++; }
                case "NEW" -> { if (in instanceof TypeInsnNode ti && ti.getOpcode() == Opcodes.NEW && matchNew(target, ti.desc)) hits++; }
                default -> { return -1; }
            }
        }
        return ordinal >= hits ? 0 : hits;
    }

    static void report(int require, String m) { if (require == 0) warn(m + " (require = 0)"); else err(m); }

    static boolean matchMember(String sel, String owner, String name, String desc) {
        String s = sel, o = null;
        if (s.startsWith("L") && s.contains(";")) { o = s.substring(1, s.indexOf(';')); s = s.substring(s.indexOf(';') + 1); }
        String n = s, d = null;
        int p = s.indexOf(desc.startsWith(":") ? ':' : '(');
        if (p >= 0) { n = s.substring(0, p); d = s.substring(p); }
        return (o == null || o.equals(owner)) && n.equals(name) && (d == null || d.equals(desc));
    }

    static boolean matchNew(String sel, String type) {
        String s = sel;
        if (s.startsWith("(")) s = s.substring(s.indexOf(')') + 1);
        if (s.startsWith("L") && s.endsWith(";")) s = s.substring(1, s.length() - 1);
        return s.replace('.', '/').equals(type);
    }
}
