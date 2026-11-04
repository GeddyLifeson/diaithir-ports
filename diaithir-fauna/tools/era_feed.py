#!/usr/bin/env python3
"""Feed for the era-index: animals and fossils by the Age that unlocks them (see docs/AGES.md). Writes /mnt/project-files/era-index/feeds/animals_fossils.json."""
import collections, json, os, sys
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import catalogue as C

OUT = '/mnt/project-files/era-index/feeds/animals_fossils.json'
# Fossil dig and study start in Age III (Cenozoic finds), Age IV adds Mesozoic, Age V Paleozoic; revival by DNA tier: Age VII tier 1, Future tiers 2-3, Rodos tiers 4-5.
FOSSIL_AGE = {'Paleozoic': 'V', 'Mesozoic': 'IV', 'Cenozoic': 'III'}
ERA_OF = {p: 'Paleozoic' for p in 'Cambrian Ordovician Silurian Devonian Carboniferous Permian'.split()}
ERA_OF.update({p: 'Mesozoic' for p in 'Triassic Jurassic Cretaceous'.split()})
ERA_OF.update({p: 'Cenozoic' for p in 'Paleocene Eocene Oligocene Miocene Pliocene Pleistocene'.split()})
REVIVAL = {1: 'VII', 2: 'Future', 3: 'Future', 4: 'Rodos', 5: 'Rodos'}

by = collections.Counter(); kinds = collections.defaultdict(collections.Counter)
for r in C.rows('mob'):
    if 'domesticable' in r['notes']: by['II'] += 1; kinds['II']['domesticable species'] += 1
    by['I'] += 1; kinds['I']['wild species'] += 1
    if 'protected' in r['notes'] or 'reintroduced' in r['notes']: by['VI'] += 1; kinds['VI']['protected or reintroduced species'] += 1
for r in C.rows('fossil'):
    a = FOSSIL_AGE[ERA_OF.get(r['period'], 'Cenozoic')]; by[a] += 1; kinds[a]['fossil taxa (dig, clean, study)'] += 1
    t = REVIVAL[int(r['dna_tier'])]; by[t] += 1; kinds[t]['revivable taxa'] += 1
order = ['I', 'II', 'III', 'IV', 'V', 'VI', 'VII', 'Future', 'Rodos']
feed = dict(system='animals_fossils', owner='ANIMALS + FOSSILS thread (diaithir-ports #36)',
            note='Counts are catalogue entries by the Age that unlocks them. Wild species spawn from Age I; domesticable ones add an Age II entry; protected or reintroduced ones add an Age VI conservation entry. Fossils are dug in Age III (Cenozoic), IV (Mesozoic), V (Paleozoic). Revival by DNA tier: Age VII endgame tier 1, Future tiers 2-3, Rodos tiers 4-5 (Shardfall tech). The dig, clean and study chain is built; revival and breeding are designed and not built yet.',
            by_era={a: by[a] for a in order}, kinds={a: dict(kinds[a]) for a in order})
json.dump(feed, open(OUT, 'w'), indent=1)
print(feed['by_era'])
