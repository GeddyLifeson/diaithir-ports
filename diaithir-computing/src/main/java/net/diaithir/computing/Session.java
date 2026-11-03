package net.diaithir.computing;

import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaThread;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.Bit32Lib;
import org.luaj.vm2.lib.DebugLib;
import org.luaj.vm2.lib.PackageLib;
import org.luaj.vm2.lib.StringLib;
import org.luaj.vm2.lib.TableLib;
import org.luaj.vm2.lib.VarArgFunction;
import org.luaj.vm2.lib.ZeroArgFunction;
import org.luaj.vm2.lib.jse.JseBaseLib;
import org.luaj.vm2.lib.jse.JseMathLib;

import java.util.function.Consumer;

/**
 * One running program. The program sees base, string, table, math and bit32 only: no io, os, package or debug. It runs on a LuaJ coroutine
 * that is resumed in slices: each slice may use SLICE instructions, and anything that needs the world is a yield the host answers on the
 * server thread (the coroutine's own thread must never touch the world, it would deadlock).
 */
final class Session {
    static final int SLICE = 200_000, TICK = 10_000;

    final Globals user = new Globals();
    private LuaThread thread;
    private int used;
    String error = "";
    boolean finished;

    /** The program's api is installed by the caller; print goes to out. */
    static Session start(String program, Consumer<Globals> api, Consumer<String> out) {
        Session s = new Session();
        Globals g = s.user;
        g.load(new JseBaseLib());
        g.load(new PackageLib());
        g.load(new Bit32Lib());
        g.load(new TableLib());
        g.load(new StringLib());
        g.load(new JseMathLib());
        g.load(new DebugLib());   // closures count instructions only in globals that have it; the name is removed below
        LuaValue setHook = g.get("debug").get("sethook");
        g.set("debug", LuaValue.NIL);
        org.luaj.vm2.LoadState.install(g);
        org.luaj.vm2.compiler.LuaC.install(g);
        for (String banned : new String[]{"dofile", "loadfile", "require", "collectgarbage", "package"}) g.set(banned, LuaValue.NIL);
        g.set("print", new VarArgFunction() {
            @Override public Varargs invoke(Varargs args) {
                StringBuilder b = new StringBuilder();
                for (int i = 1; i <= args.narg(); i++) b.append(i > 1 ? "\t" : "").append(args.arg(i).tojstring());
                out.accept(b.toString());
                return NONE;
            }
        });
        api.accept(g);
        try {
            LuaValue chunk = g.load(program, "program", g);
            s.thread = new LuaThread(g, chunk);
            LuaValue hook = new ZeroArgFunction() {
                @Override public LuaValue call() {
                    s.used += TICK;
                    if (s.used >= SLICE) throw new LuaError("program ran past its " + SLICE + " instruction budget");
                    return NIL;
                }
            };
            setHook.invoke(LuaValue.varargsOf(new LuaValue[]{s.thread, hook, LuaValue.EMPTYSTRING, LuaValue.valueOf(TICK)}));
        } catch (LuaError e) {
            s.finished = true;
            s.error = String.valueOf(e.getMessage());
        }
        return s;
    }

    /** Runs the next slice. Returns what the program yielded, or null when it has finished (see finished and error). */
    Varargs resume(Varargs in) {
        if (finished) return null;
        used = 0;
        try {
            Varargs r = thread.resume(in);
            if (!r.arg1().toboolean()) {
                finished = true;
                error = r.arg(2).tojstring();
                return null;
            }
            if (thread.getStatus().equals("dead")) {
                finished = true;
                return null;
            }
            return r.subargs(2);
        } catch (RuntimeException e) {
            finished = true;
            error = e.toString();
            return null;
        }
    }

    /** Yield from inside an api function. */
    static Varargs yield(Globals g, Varargs args) { return g.yield(args); }
}
