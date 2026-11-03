# Diaithir Units

Shared unit API for the pack. Standard and reasons: `plans/UNITS.md` in the project files (master CLAUDE.md section 4b).

Canonical units: energy J (1 FE = 1 J), power W (1 FE/t = 20 W), fluid L (1 mB = 1 L, 1 bucket = 1000 L = 1 m³), temperature K (shown in °C), pressure bar, torque N·m (Create SU), rotation rpm, mass kg, length m, time s (20 ticks), currency g of silver.

- `Units.convert(value, "mekanism:joule", "j")` converts by unit id; `Units.register(...)` adds more (KubeJS, other modules).
- `Fmt.energy / power / volume / temperature / pressure` format for players.
- `shim.ScaledEnergy` wraps a third-party energy store so the rest of the pack sees joules. Rounds down; never creates energy.

Magic folds into energy: mana, aura, ember, LP and vis are 100 J each, Source is 1000 J.
MIT. The pack reads no third-party code; ids are plain strings, so a missing mod costs nothing.
