package net.diaithir.computing;

import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaThread;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.Bit32Lib;
import org.luaj.vm2.lib.PackageLib;
import org.luaj.vm2.lib.StringLib;
import org.luaj.vm2.lib.TableLib;
import org.luaj.vm2.lib.VarArgFunction;
import org.luaj.vm2.lib.ZeroArgFunction;
import org.luaj.vm2.lib.jse.JseBaseLib;
import org.luaj.vm2.lib.jse.JseMathLib;

import java.util.function.Consumer;

/** Runs one program under an instruction budget. The program sees base, string, table, math and bit32 only: no io, os, package or debug. */
final class Sandbox {
    static final int BUDGET = 200_000;

    /** @return null when the program finished, else a one-line error. */
    static String run(String program, Consumer<Globals> api, Consumer<String> out) {
        Globals user = new Globals();
        user.load(new JseBaseLib());
        user.load(new PackageLib());
        user.load(new Bit32Lib());
        user.load(new TableLib());
        user.load(new StringLib());
        user.load(new JseMathLib());
        user.load(new org.luaj.vm2.lib.DebugLib());   // closures count instructions only in globals that have it; the name is removed below
        LuaValue setHook = user.get("debug").get("sethook");
        user.set("debug", LuaValue.NIL);
        org.luaj.vm2.LoadState.install(user);
        org.luaj.vm2.compiler.LuaC.install(user);
        for (String banned : new String[]{"dofile", "loadfile", "require", "collectgarbage", "package"}) user.set(banned, LuaValue.NIL);
        user.set("print", new VarArgFunction() {
            @Override public Varargs invoke(Varargs args) {
                StringBuilder b = new StringBuilder();
                for (int i = 1; i <= args.narg(); i++) b.append(i > 1 ? "\t" : "").append(args.arg(i).tojstring());
                out.accept(b.toString());
                return NONE;
            }
        });
        api.accept(user);
        try {
            LuaValue chunk = user.load(program, "program", user);
            LuaThread thread = new LuaThread(user, chunk);
            LuaValue hook = new ZeroArgFunction() {
                @Override public LuaValue call() { throw new LuaError("program ran past its " + BUDGET + " instruction budget"); }
            };
            setHook.invoke(LuaValue.varargsOf(new LuaValue[]{thread, hook, LuaValue.EMPTYSTRING, LuaValue.valueOf(BUDGET)}));
            Varargs r = thread.resume(LuaValue.NIL);
            if (!r.arg1().toboolean()) return r.arg(2).tojstring();
            return null;
        } catch (LuaError e) {
            return e.getMessage();
        } catch (RuntimeException e) {
            return e.toString();
        }
    }

    private Sandbox() {}
}
