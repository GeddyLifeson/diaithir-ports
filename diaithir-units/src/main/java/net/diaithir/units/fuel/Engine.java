package net.diaithir.units.fuel;

/** A prime mover class with its fuels and a typical fuel-to-shaft efficiency (0..1). */
public record Engine(String id, double efficiency, java.util.List<String> fuels) {}
