#!/usr/bin/env python3
"""Library entries for the later Ages: Age II keeping (domesticable), VI conservation (protected or reintroduced), revival by DNA tier (VII tier 1, Future 2-3, Rodos 4-5).
Writes /mnt/project-files/library-sync/animals_ages.json in the loader format [name, kind, age, locale, summary]."""
import json, os, sys
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import catalogue as C

OUT = '/mnt/project-files/library-sync/animals_ages.json'
REVIVE = {1: (7, 'Age VII', 'Its genome survives well enough for the gene works of Age VII to read most of it.'),
          2: (8, 'the Future', 'Its genome is fragmentary. Only the far-future shard works can rebuild the missing stretches.'),
          3: (8, 'the Future', 'Its genome is fragmentary. Only the far-future shard works can rebuild the missing stretches.'),
          4: (9, 'Rodos', 'No genome remains. Rodos alloys and shard resonance are needed to rebuild it from the shape of its bones.'),
          5: (9, 'Rodos', 'No genome remains. Rodos alloys and shard resonance are needed to rebuild it from the shape of its bones.')}

def kg(x):
    x = float(x)
    return f'{x:g} kg' if x < 1000 else f'{x / 1000:g} tonnes'

es = []
for r in C.rows('mob'):
    n = r['common']
    if 'domesticable' in r['notes']:
        es.append([f'Keeping the {n.lower()}', 'being', 2, 'fauna', f'Herders learn to keep the {n.lower()} ({r["latin"]}) by penning, feeding and breeding it. An adult weighs about {kg(r["kg"])} and eats {r["diet"]} food. A kept animal stays tame once raised from young.'])
    if 'protected' in r['notes'] or 'reintroduced' in r['notes']:
        es.append([f'Conserving the {n.lower()}', 'being', 6, 'fauna', f'Wardens in Age VI count the {n.lower()} ({r["latin"]}) and guard the ground it lives on. An adult weighs about {kg(r["kg"])}. Hunting it is forbidden where it is protected, and herds released into old range are watched for years.'])
for r in C.rows('fossil'):
    age, label, line = REVIVE[int(r['dna_tier'])]
    es.append([f'Reviving {r["common"].lower()}', 'being', age, 'fossils', f'{r["common"]} ({r["latin"]}) lived {r["ma_from"]} to {r["ma_to"]} million years ago. {line} Revival is attempted in {label}, and only from fossils a scholar has studied. An adult weighed about {kg(r["kg"])}.'])
seen = set(); out = []
for e in es:
    if e[0] in seen: e[0] += f' ({e[4].split("(")[1].split(")")[0].lower()})'
    seen.add(e[0]); out.append(e)
json.dump(out, open(OUT, 'w', encoding='utf-8'), ensure_ascii=False, indent=0)
import collections; print(len(out), collections.Counter(e[2] for e in out))
