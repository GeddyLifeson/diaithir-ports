package net.diaithir.computing;

import org.luaj.vm2.Varargs;

/** A zero-argument Lua function that may return several values. */
abstract class Fn0 extends org.luaj.vm2.lib.VarArgFunction {
    abstract Varargs run();
    @Override public Varargs invoke(Varargs a) { return run(); }
}
