# Creatures

| id | name | court | preset | hostile | adapted from | health | attack | speed |
|---|---|---|---|---|---|---|---|---|
| goburdoiri | Goburdoiri | hellenic | beast | False | Satyr | 28 | 2.5 | 0.35 |
| famuirkeid | Famuirkeid | hellenic | beast | True | Gigante | 100 | 5.5 | 0.22 |
| maktirimatuir | Maktìrimàtuir | italic | beast | False | Centaur | 34 | 4.0 | 0.25 |
| famuirknok | Famuirknok | italic | beast | True | Talos (bronze automaton) | 80 | 6.0 | 0.22 |
| dourasniechgleidid | Dourasniechgleidid | italic | avatar | False | Cerberus | 190 | 8.0 | 0.3125 |
| tarbiumachuoirtach | Tarbiumachuoirtach | minoan | avatar | True | Cretan Minotaur | 114 | 9.0 | 0.31 |
| griubluchuirt | Grìublùchuirt | minoan | flyer | False | Pegasus | 28 | 0.0 | 0.25 |
| tarbmoir | Tarbmoir | minoan | beast | True | Bronze Bull | 65 | 5.0 | 0.28 |
| natuirkriegach | Natuirkriegach | anatolian | avatar | True | Arachne | 80 | 4.5 | 0.285 |
| liupurdsielg | Liupurdsielg | anatolian | beast | True | Nemean Lion | 60 | 6.0 | 0.28 |
| iuluirbeind | Iuluirbeind | anatolian | flyer | False | Stymphalian bird | 16 | 0.0 | 0.26 |
| natuirmoir | Natuirmoir | canaanite | avatar | True | Scylla | 140 | 8.0 | 0.16 |
| leomundkraeb | Leòmundkraeb | canaanite | beast | False | Dryad | 30 | 3.0 | 0.28 |
| leomundcheistrigchatuir | Leòmundcheistrìgchatuir | canaanite | beast | False | Naiad | 24 | 3.0 | 0.25 |
| madadfasaigratad | Madadfàsaigratad | nilotic | beast | False | Orthus | 24 | 4.5 | 0.29 |
| toimsechunleomund | Tòimsechunleòmund | nilotic | avatar | True | Python | 70 | 4.5 | 0.31 |
| natuirkrun | Natuirkrùn | nilotic | beast | True | Cerastes | 24 | 1.0 | 0.31 |
| daelaggrianu | Daelaggrianu | nilotic | flyer | False | Harpy | 24 | 0.0 | 0.22 |

Stats follow the upstream creature. Where the upstream creature is a boss and the table entry is a plain beast, health was scaled down (see `stat_note` in creatures.json); Scylla's speed is raised from 0.08 to 0.16 so she can be fought on land.

## Spawn regions

Natural spawns need the biome habitat tag, a point inside the court region boxes (Tellus latitude/longitude, 1:5) and the world Age at or past `min_age` (Age III when `diaithir_ages` is absent). Commands, eggs and spawners ignore this.

| id | court | region | habitat tag | min Age | boxes (lat, lon) |
|---|---|---|---|---|---|
| goburdoiri | hellenic | Greece, Aegean, Anatolian coast | habitat/woods | 3 | 34.5..41.8, 19.3..29.5 |
| famuirkeid | hellenic | Greece, Aegean, Anatolian coast | habitat/mountains | 3 | 34.5..41.8, 19.3..29.5 |
| maktirimatuir | italic | Italy, Etruria, Latium | habitat/hills | 3 | 36.4..46.6, 6.6..18.7 |
| famuirknok | italic | Italy, Etruria, Latium | habitat/volcano | 3 | 36.4..46.6, 6.6..18.7 |
| dourasniechgleidid | italic | Italy, Etruria, Latium | habitat/gates | 4 | 36.4..46.6, 6.6..18.7 |
| tarbiumachuoirtach | minoan | Crete and the Aegean | habitat/labyrinths | 3 | 34..36, 23..27; 35.5..40.2, 23.3..28 |
| griubluchuirt | minoan | Crete and the Aegean | habitat/cliffs | 2 | 34..36, 23..27; 35.5..40.2, 23.3..28 |
| tarbmoir | minoan | Crete and the Aegean | habitat/sea | 2 | 34..36, 23..27; 35.5..40.2, 23.3..28 |
| natuirkriegach | anatolian | Anatolia | habitat/cliffs | 3 | 35.8..42.2, 26..44.8 |
| liupurdsielg | anatolian | Anatolia | habitat/mountains | 2 | 35.8..42.2, 26..44.8 |
| iuluirbeind | anatolian | Anatolia | habitat/peaks | 2 | 35.8..42.2, 26..44.8 |
| natuirmoir | canaanite | Levant and Phoenicia | habitat/sea | 3 | 29.5..37.1, 33.5..38.5 |
| leomundkraeb | canaanite | Levant and Phoenicia | habitat/groves | 2 | 29.5..37.1, 33.5..38.5 |
| leomundcheistrigchatuir | canaanite | Levant and Phoenicia | habitat/temples | 2 | 29.5..37.1, 33.5..38.5 |
| madadfasaigratad | nilotic | Nile valley | habitat/desert | 2 | 22..31.8, 25..35; 14..22, 30..35 |
| toimsechunleomund | nilotic | Nile valley | habitat/desert | 3 | 22..31.8, 25..35; 14..22, 30..35 |
| natuirkrun | nilotic | Nile valley | habitat/reeds | 2 | 22..31.8, 25..35; 14..22, 30..35 |
| daelaggrianu | nilotic | Nile valley | habitat/dunes | 2 | 22..31.8, 25..35; 14..22, 30..35 |
