# Gauges: standard and one narrow

Two gauges only. Every US and UK train is mapped onto one of them (other gauges are skipped).

- **Standard** = 1435 mm (4 ft 8.5 in). The main-line gauge of the US and UK.
- **Narrow** = 3 ft (914 mm). Chosen because it is the real US narrow gauge (Denver and Rio Grande Western, Rio Grande Southern, East Broad Top, White Pass and Yukon, Colorado and Southern) and also ran in the UK and Ireland (Isle of Man, Ireland). Metre gauge was rejected: neither the US nor the UK ever built it in any quantity. UK 2 ft and 2 ft 6 in lines (Ffestiniog, Talyllyn) and the US 2 ft Maine lines, and 3 ft 6 in lines, are folded onto the one narrow gauge by the roster harvest (500 to 1067 mm).

## Real figures and what the game does with them

Fetched from Wikipedia on 2026-11-03 (article wikitext). Rows marked **no source** could not be found in the fetched pages and are kept from general knowledge until someone finds a page; they are not claimed as sourced.

| | Standard | Narrow (3 ft) | Game rule |
|---|---|---|---|
| Minimum curve radius | 288 ft (88 m) for interchange equipment, normally 410 ft (125 m), 574 ft (175 m) preferred for long freight (North America) [Minimum railway curve radius] | **no source** for 3 ft; about 55 m kept. Sourced for a 2 ft line: Welsh Highland 40 m. The article says the sharpest curves are on the narrowest gauges | Curved rail: a vehicle keeps `sqrt(R_game / R_min)` of its speed, with the game curve taken as 40 m (8 blocks at 1:5): standard 0.57 (R 125 m), narrow 0.85 (R 55 m) (`Gauge.curveSpeed`) |
| Ruling grade | 2.2 % was the US standard grade set by Congress in the 1850s; Southern Pacific across Nevada and Utah faced 0.43 % [Ruling gradient] | about 4 %: the Cumbres and Toltec climbs on a grade averaging 4 % [Cumbres and Toltec Scenic Railroad] | Ascending rail: speed kept per tick 0.96 standard, 0.98 narrow, the 2.2 to 4 ratio read as half the loss (`Gauge.gradeKeep`) |
| Axle load | UK diesels were built to 19 long tons (19.3 t) a axle before the 1990s; the highest in the world is 40 t (Fortescue) [Axle load]. US 32.5 t: **no source** | **no source**; about 8 to 12 t kept | Narrow freight carries 0.4 of the standard payload; narrow vehicles weigh 0.5 of the standard type; none over 90 t (`Gauge.payload`, `maxMassKg`) |
| Loading gauge | AAR Plate B 10 ft 8 in by 15 ft 1 in (3.25 m by 4.60 m); Plate F 10 ft 8 in by 17 ft (3.25 m by 5.18 m). Great Britain has the most restrictive loading gauge for its track gauge (W6A to W12) [Loading gauge] | **no source**; about 2.4 m by 3.4 m kept | A narrow wagon cannot carry a full standard container: half-size boxes and pallets only |
| Speed | The same curve that allows 145 km/h on standard gauge allows 130 km/h on 3 ft 6 in gauge; narrow gauge trains cannot run as fast [Narrow-gauge railway]. Freight 80 mph and passenger 125 mph: **no source** | about 45 km/h typical kept: **no source** | Rail caps, 1 block per tick = 72 km/h: bronze 0.6, steel 0.9, fast 1.2 (standard); narrow 0.5 = 36 km/h |
| Construction cost | Narrow gauge is usually built with tighter curves, smaller structure gauges and lighter rails, so can cost less to build, equip and operate, mostly in mountains (Spooner, 1879, cited by [Narrow-gauge railway]). The 0.6 ratio: **no source** | | Narrow rail costs 5 copper for 24 (0.21 per rail) against 6 for 16 (0.375) for bronze, 0.56 of it |
| When | bronze rail Age III, steel V, fast VI | Age III | Narrow arrives with the first rail |

Pages: https://en.wikipedia.org/wiki/Minimum_railway_curve_radius , /Ruling_gradient , /Axle_load , /Loading_gauge , /Narrow-gauge_railway , /Cumbres_and_Toltec_Scenic_Railroad , /3_ft_gauge_railways .

## Rules

- A vehicle spawned from rolling stock has a gauge. It only moves on its own gauge, on dual-gauge rail, and on gauge-neutral rails (vanilla, station, routing). On the wrong gauge its speed cap is 0 and the rolling stock item refuses to be placed.
- Dual-gauge rail (three rails) takes both, at the narrow cap and the standard gauge's wide curves. Plain and powered.
- Freight between gauges moves by transshipment: a cart loader beside the track moves cargo between a wagon of each gauge and a chest or tank, or dual-gauge rail is laid through the transfer yard.
- Crew: steam locomotives need 2 (driver, fireman), diesel and electric 1. The wages thread charges through `Autonomy.status`; Age VII removes the crew.
