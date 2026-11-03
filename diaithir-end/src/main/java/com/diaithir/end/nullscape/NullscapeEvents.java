package com.diaithir.end.nullscape;

import net.neoforged.bus.api.Event;
import net.neoforged.neoforge.common.NeoForge;

/** Global Events hook point for the End overhaul. */
public final class NullscapeEvents {
 public static final String HOOK_ID = "diaithir_end:nullscape";

 public static class GlobalEndEvent extends Event {
 private final String hook;
 private final int ageGate;
 private final int quantumJ;

 public GlobalEndEvent(String hook, int ageGate, int quantumJ) {
 this.hook = hook;
 this.ageGate = ageGate;
 this.quantumJ = quantumJ;
 }

 public String hook() { return hook; }
 public int ageGate() { return ageGate; }
 public int quantumJ() { return quantumJ; }
 }

 public static void fire() {
 NeoForge.EVENT_BUS.post(new GlobalEndEvent(HOOK_ID, NullscapeConstants.AGE, NullscapeConstants.MAGIC_QUANTUM_J));
 }

 private NullscapeEvents() {}
}
