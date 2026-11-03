# Gauges: standard and one narrow

Two gauges only. Every US and UK train is mapped onto one of them (other gauges are skipped).

- **Standard** = 1435 mm (4 ft 8.5 in). The main-line gauge of the US and UK.
- **Narrow** = 3 ft (914 mm). Chosen because it is the real US narrow gauge (Denver and Rio Grande Western, Rio Grande Southern, East Broad Top, White Pass and Yukon, Colorado and Southern) and also ran in the UK and Ireland (Isle of Man, Ireland). Metre gauge was rejected: neither the US nor the UK ever built it in any quantity. UK 2 ft and 2 ft 6 in lines (Ffestiniog, Talyllyn) and the US 2 ft Maine lines, and 3 ft 6 in lines, are folded onto the one narrow gauge by the roster harvest (500 to 1067 mm).

## Real figures and what the game does with them

Figures are rounded values of the kind the standard references give (AREMA and Network Rail guidance, D&RGW and EBT histories). They were recalled, not fetched, so they stay approximate until checked against the Wikipedia pages for Track gauge, Narrow-gauge railway and Loading gauge.

| | Standard | Narrow (3 ft) | Game rule |
|---|---|---|---|
| Minimum curve radius | about 150 m (yards and branches), 300 m and up on main lines | about 55 m (D&RGW 30 degree curves are 58 m) | Curved rail: a vehicle keeps `sqrt(R_game / R_min)` of its speed, with the game curve about 40 m real (8 blocks at 1:5). Standard 0.52, narrow 0.85 (`Gauge.curveSpeed`) |
| Ruling grade | about 2 % (US main lines 1 to 2.2 %) | about 4 % (Cumbres, Marshall) | Ascending rail: speed kept per tick 0.96 standard, 0.98 narrow, so narrow loses half as much (`Gauge.gradeKeep`) |
| Axle load | 25 t (UK RA8 to RA10) to 32.5 t (US 286k) | about 8 to 12 t | Narrow freight carries 0.4 of the standard payload; narrow vehicles weigh 0.5 of the standard type; no narrow vehicle over 90 t (`Gauge.payload`, `maxMassKg`) |
| Loading gauge | US Plate F about 3.2 m wide, 5.2 m high; UK W6 to W12 smaller | about 2.4 m wide, 3.4 m high | A narrow wagon cannot carry a full standard container (a container thread rule: narrow takes half-size boxes and pallets only) |
| Top speed | freight 80 mph (130 km/h), passenger 125 mph and up | about 45 km/h typical, 90 km/h the best | Rail caps, 1 block per tick = 72 km/h: bronze 0.6, steel 0.9, fast 1.2 (standard); narrow 0.5 = 36 km/h (the ratio narrow to steel, 0.55, matches 45 to 80 km/h) |
| Construction cost per km | 1.0 | about 0.5 to 0.7 | Narrow rail costs 5 copper for 24 (0.21 per rail) against 6 for 16 (0.375) for bronze, 0.56 of it |
| When | bronze rail Age III, steel V, fast VI | Age III | Narrow arrives with the first rail and is the cheap way to open mountains and mines |

## Rules

- A vehicle spawned from rolling stock has a gauge. It only moves on its own gauge, on dual-gauge rail, and on gauge-neutral rails (vanilla, station, routing). On the wrong gauge its speed cap is 0 and the rolling stock item refuses to be placed.
- Dual-gauge rail (three rails) takes both, at the narrow cap and the standard gauge's wide curves. Plain and powered.
- Freight between gauges moves by transshipment: a cart loader beside the track moves cargo between a wagon of each gauge and a chest or tank, or dual-gauge rail is laid through the transfer yard.
- Crew: steam locomotives need 2 (driver, fireman), diesel and electric 1. The wages thread charges through `Autonomy.status`; Age VII removes the crew.
