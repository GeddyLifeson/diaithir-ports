# Assembly Yard API (inside diaithir-carts)

One shared system for building vehicles the way they are actually built: a frame is laid on a yard site, parts are installed stage by stage, the vehicle visibly grows, and it rolls out with stats summed from its parts. Rail yards, farm and truck yards, ship yards (slipways) and space yards all use it. Package `net.diaithir.yards`, inside the diaithir-carts jar (no new jar). Core is plain Java (no Minecraft types) so it self-tests headless; the block/entity glue sits beside it.

## Concepts
- **PartKind**: string id from a catalog (`engine`, `boiler`, `bogie`, `cab`, `coupler`, `hull`, `rudder`, `thruster`, ...). Each catalog part (`PartDef`) has `item` (registry id), `kind`, and numeric stats in SI: `mass_kg`, `hp` (structural, dimensionless), `power_w`, `thrust_n`, `buoyancy_n`, `capacity_l`, `crew_slots`, plus a free `extra` map<string,double>.
- **YardDef** (data-driven JSON, `data/<ns>/yards/<id>.json`):
  `id`, `site` (`ground` | `water` | `orbital`), `dimensions` (allowed dimension ids, empty = any), `footprint` (w,l,h in blocks), `result` (id of the vehicle type to roll out), `stages[]`, `crane_speedup` (default 1.0).
- **Stage**: `id`, `order` (stages are installed in list order), `slots[]` (each: `kind`, `count`, optional `min_age`), `base_ticks` (work time per slot at crane_speed 1), `ghost` (model/structure-template id shown for this stage, partial build visible).
- **Site**: multiblock of a controller block + frame blocks. `SiteType` decides placement checks (solid ground, water for slipways, vacuum/orbital dimension for space yards). Optional **crane/jig** adjacent blocks multiply speed (`CraneSpeed` provider, product of installed jig multipliers, upgradeable via diaithir-upgrades modules).
- **Build** (`BuildState`, saved in the controller block entity): stage index, per-slot installed `PartDef`, progress ticks.

## Java surface
```java
YardRegistry.register(YardDef def);            // or load JSON
PartCatalog.register(PartDef part);            // or load JSON
BuildState b = BuildState.start(YardDef def);
InstallResult b.install(PartDef part);          // OK | WRONG_STAGE | NO_SLOT | AGE_LOCKED
boolean b.advance(int ticks, double craneSpeed); // work progress, true when stage finished
Stats b.stats();                                // sums mass/power/thrust/buoyancy/crew, min hp, plus extra
boolean b.complete();
interface RolloutHook { Entity/Object rollOut(ServerLevel, BlockPos, YardDef, Stats, BuildState); }
YardRegistry.rollout(id, hook);                // owner of a yard type supplies the hook
```
`Stats` is immutable: `mass_kg`, `power_w`, `thrust_n`, `buoyancy_n`, `hp`, `crew_slots`, `extra`. Owners map it to their vehicle:
- rail: `power_w`, mass, adhesion (drives tractive effort in diaithir-carts/steamrails)
- ships: `ShipAssembler` call in `RolloutHook`, mass + thrust + buoyancy onto the VS2 ship
- space: orbital site, `thrust_n/mass_kg`, `power_w` on the Keelwright ship

## Cranes and heavy parts
- `PartDef.heavy` (true when `mass_kg` >= 500): heavy parts cannot be carried. The item is a crate/ingot-style handle that stays on the bench; moving it needs a crane (`CraneLift`): crane picks the part at a source (bench, depot, flatcar, ship hold) and sets it at a yard slot or target container.
- Crane set by Age: treadwheel crane, steam crane, diesel mobile crane, electric gantry/overhead, port/container crane. Each has `lift_kg`, `reach_m`, `speed` (m/s); `CraneSpeed` for stage work = product of crane `speed` multipliers in reach of the site.
- Yard stage rule: a slot with a heavy part needs a crane whose `lift_kg` >= part mass in reach, otherwise `NEEDS_CRANE`.
- Loading API: `Crane.move(source, target, stack)` also used for containers between ships, trains and trucks.
- Mod-first note: Create gantries/contraptions are the model for movement; no crane mod has a 26.1 build, so cranes are clean-room in diaithir-yards.

## Operators and blueprints
- A crane has an `OperatorSeat` (cab). Operator = player riding the seat (controls: slew, trolley, hoist) or an NPC worker assigned to the seat (workers from the settlements/NPC lanes; NPC runs a `CraneJob` queue at its skill speed).
- A **Blueprint** (`BlueprintDef`: yard id + ordered part list + target footprint) is an item the player writes at a drafting table. A manned crane with a blueprint and a stocked depot (`PartDepot`, a chest/container block in reach) runs the build: pick part, move, install, next stage, until `complete()`. Missing parts pause the job and list them in the controller.
- Blueprints also cover block structures (bridges, hulls) via the ghost templates; same job queue.

## Visual growth
Each stage names a `ghost` structure template. The controller renders the ghost for the current stage plus every finished stage's real blocks; installing a part places its block(s) from the part's `block` field at the stage anchor. Progress ticks show as a scaffold fade.

## Rules
- Parts come from the crafting-station benches and Foundry machines; the yard never crafts.
- SI units only. `min_age` gates by the shared server calendar.
- Upgrades: cranes, jigs and tooling are upgradable blocks under diaithir-upgrades.
- Data files only for new yards/parts; no new Java needed to add a vehicle type unless it needs a new rollout.

Status: spec v1 (this PR). Core code follows in the same branch `claude/project-thread-slgjcj`.
