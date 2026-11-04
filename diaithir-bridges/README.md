# Diaithir Bridges (NeoForge 26.1.2, MIT)

Small blocks that join mods in the Diaithir pack. Ids are English, display names are Diaithiris. Age gates are in `data/diaithirbridges/age_gate.json`.

| Block | Id | Age | What it does |
|---|---|---|---|
| Tobardealain | `tobardealain` | VI | Burns magic reagents into Forge Energy |
| Lochrandraoidheachd | `lochrandraoidheachd` | VII | Spends Forge Energy to make a magic reagent |
| Claochladairdealain | `claochladairdealain` | VI | EU to FE converter, lossy |
| Claochladairanalach | `claochladairanalach` | VI | FE to compressed air and back, lossy |
| Amarlionna | `amarlionna` | VI | Molten metal tank (16 buckets) |
| Carrier Tank | `carrier_tank` | VI | Holds one fuel from the carrier table (diesel, coal, hay, hydrogen, battery bank, mana). Fuel store on every face, fluid port, comparator, feeds engines |
| Magic Carrier | `magic_carrier` | VI | Turns magic reagents into quanta of a magic carrier (mana, aura, ember, life points, vis, source) and pushes them into neighbouring fuel stores |
| Locomotive Charger | `loco_charger` | VII | Takes Forge Energy and pours it (1 FE = 1 J) into the magic joule bank of a fitted locomotive standing on the rail above |

## Converters
Front face (the one that faces you when placed) is the output, every other face is an input. Sneak with an empty hand to flip direction, plain empty hand reads the buffer. Ratios are "out per 100 in" in `diaithirbridges-server.toml`: EU to FE 90 (a tenth lost), FE to EU 50 (two in for one out), FE to air 20 (5 FE per mL), air to FE 200 (2 FE per mL, so a round trip loses most). Rate and buffer are configurable. The air defaults are balance guesses, not taken from PneumaticCraft.

Modern Industrialization, Energized Power, Mekanism and others all expose the plain NeoForge energy capability (`Capabilities.Energy.BLOCK`), so the EU block needs no dependency on any of them. Mekanism machines already convert joules at their own ratio (2.5 FE per J by default in Mekanism's own config); the converter does not touch that and just moves FE.

Air side: PneumaticCraft has no 26.1 build and its API is not on any resolvable Maven for 26.1 (modmaven only lists 1.21.1), so the air side uses reflection (`PneumaticAir`) on `PNCCapabilities.AIR_HANDLER_MACHINE` and `IAirHandlerMachine`. Without PneumaticCraft the mod loads and the air converter just holds its FE buffer. The reflection path is UNTESTED against a real PneumaticCraft build.

## Fluids
Datapack tags `c:molten_iron`, `copper`, `gold`, `tin`, `bronze`, `steel`, `zinc`, `silver`, `lead`, `nickel` list the real ids from the pack registry (Productive Metalworks and Tinkers' molten ids, source and flowing), all `required: false`. Create has no molten metals and Modern Industrialization only has molten redstone, so neither appears. Amarlionna accepts fluids in `diaithirbridges:reservoir` (lava plus every `#c:molten_*`) and fills or empties from buckets and any fluid item.

Skipped: Mekanism gas to fluid conversion. Mekanism has no 26.1 build to compile against (the source tree for 10.8.0 exists, it is not a resolvable artifact), so it is pending a Mekanism 26.1 build.

## Cell guard
Recipe type `diaithirbridges:empty_cell_convert` has the same JSON as `minecraft:crafting_shapeless` but never matches while an input is a storage cell or disk holding items. Contents are detected from data components listed in config `guardComponents` (default `ae2:storage_cell_inv`, `refinedstorage:storage_reference`). AE2 keeps cell contents in that component; a Refined Storage disk only carries a reference to world storage, so any used disk counts as full (conservative). The AE2 API is not used. Existing AE2 to Refined Storage conversion recipes need their `type` changed to this one to get the guard.

## Test
`/diaithirbridges selftest` (owner level) checks converter ratios and face rules through the energy capability, the air converter FE side, reservoir tag filtering plus bucket fill and empty, and the guard. `python3 tools/gen_textures.py` regenerates the textures.

## Universal ports (bridges F, L, S)
Needs `diaithir-units` (Fuels, Engine, FuelStore); the build includes it from the sibling folder.
- **Fuel (F):** capability `Ports.FUEL` (`diaithirbridges:fuel_store`, a `FuelStore` per face). The Carrier Tank and Magic Carrier expose it. An engine asks `tank.canFeed(engine)` (uses `Engine.accepts`) and `tank.feed(engine, powerW, seconds)`, which burns `P*t / efficiency / J-per-unit` and returns the shaft joules delivered. One fuel at a time, as in a real tank. Capacity `tankCapacity` is in the fuel's own unit (L, kg or quanta).
- **Fluid (L):** the Carrier Tank's fluid port takes any fluid in the tag `c:<fuel id>` (`c:diesel`, `c:ethanol`, `c:biodiesel`, `c:kerosene`, `c:petrol` which includes `#c:gasoline`), 1 mB = 1 L, transaction safe. Fuels without a fluid tag member (hydrogen, jet_a1, lpg, heavy_fuel_oil, avgas) only load through the fuel capability until a mod fills their tag.
- **Signal (S):** every block here (tiers, converters, reservoir, tank, magic carrier, charger) gives a comparator level from its fill (`Ports.level`: 0 only when empty, 15 only when full). The computer (OC2R) port is not built.
- **Magic to fuel:** reagent joules (same values the Toburdeluin burns) times `magicYieldPct` (90) over J per quantum (mana 100 J, source 1000 J). Sneak with an empty hand to pick the carrier while its store is empty.
- **Locomotive Charger:** soft link by reflection to `net.diaithir.carts.MagicFuel.fitted/charge` (trains lane PR #228). UNTESTED against a real carts build; without it the block idles and keeps its energy.

The selftest also covers the tank (mixing refused, engine feed numbers, signal, water refused at the fluid port), the magic carrier (yield, push into a tank, carrier cycling) and the charger's FE port. The fluid port with a real fuel fluid and the charger with a real locomotive are not tested.
