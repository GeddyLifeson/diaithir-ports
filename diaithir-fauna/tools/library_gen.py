#!/usr/bin/env python3
"""Generate Library (Panscriptum) entries for every catalogue species and fossil taxon.
Usage: python3 library_gen.py [catalogue_dir] [out_dir]
Entry format: [name, kind, age, locale, summary]. Facts come only from catalogue fields."""
import glob, json, os, sys, zlib

CAT = sys.argv[1] if len(sys.argv) > 1 else '/mnt/project-files/animals-fossils/catalogue'
OUT = sys.argv[2] if len(sys.argv) > 2 else '/mnt/project-files/library-sync'

DIET = {'herb': 'plants', 'carn': 'meat', 'omni': 'both plants and meat', 'pisc': 'fish', 'insect': 'insects',
        'fil': 'plankton strained from the water', 'scav': 'carrion', 'frug': 'fruit', 'nect': 'nectar'}
DIETV = {'herb': 'grazes and browses on', 'carn': 'hunts', 'omni': 'eats', 'pisc': 'catches', 'insect': 'feeds on',
         'fil': 'filters', 'scav': 'feeds on', 'frug': 'feeds mostly on', 'nect': 'feeds on'}
HAB = {'forest': 'forest', 'jungle': 'jungle', 'plains': 'open plains', 'savanna': 'savanna', 'taiga': 'taiga',
       'swamp': 'swamps', 'desert': 'desert', 'badlands': 'badlands', 'mountain': 'mountains', 'icy': 'icy ground',
       'river': 'rivers', 'lake': 'lakes', 'coast': 'coasts', 'reef': 'reefs', 'ocean_warm': 'warm seas',
       'ocean_cold': 'cold seas', 'ocean_deep': 'the deep sea', 'cave': 'caves', 'island': 'islands',
       'tundra': 'tundra', 'grassland': 'grassland', 'wetland': 'wetlands'}
TRAIT = {'herd': 'It lives in herds.', 'pack': 'It lives and hunts in packs.', 'nocturnal': 'It is active at night.',
         'diurnal': 'It is active by day.', 'venomous': 'Its bite or sting is venomous.',
         'migratory': 'It migrates with the seasons.', 'burrow': 'It digs burrows.',
         'arboreal': 'It lives in trees.', 'flies': 'It flies.', 'swims': 'It swims well.',
         'tusk': 'It carries tusks.', 'horn': 'It carries horns.', 'shell': 'It is protected by a shell.',
         'domesticable': 'It can be tamed.', 'huntable': 'It can be hunted for food.',
         'anadromous': 'It swims up rivers from the sea to spawn.'}
PERIOD_AGE = {'Paleocene': 3, 'Eocene': 3, 'Oligocene': 3, 'Miocene': 3, 'Pliocene': 3, 'Pleistocene': 3,
              'Triassic': 4, 'Jurassic': 4, 'Cretaceous': 4}  # others: Paleozoic, Age V
ERA = {3: 'Cenozoic', 4: 'Mesozoic', 5: 'Paleozoic'}
ROCK = {'marine_sediment': 'marine sediment', 'volcanic_ash': 'volcanic ash'}


def h(s, n):
    return zlib.crc32(s.encode()) % n


def num(x):
    f = float(x)
    if f >= 100: return f'{round(f):,}'
    if f >= 10: return f'{f:.0f}'
    if f >= 1: return f'{f:.1f}'.rstrip('0').rstrip('.')
    return f'{f:.2g}'


def mass(kg):
    f = float(kg)
    if f >= 1000:
        return f'{f / 1000:.1f}'.rstrip('0').rstrip('.') + ' tonnes'
    if f < 1:
        return f'{round(f * 1000)} g' if f >= 0.01 else 'under 10 g'
    return num(f) + ' kg'


def lst(items, k=3):
    items = [i for i in items if i][:k]
    if len(items) <= 1: return ''.join(items)
    return ', '.join(items[:-1]) + ' and ' + items[-1]


def year(y):
    y = int(y)
    return f'{-y} BC' if y < 0 else f'{y} AD'


def uniq(seen, name, latin):
    if name in seen:
        name = f'{name} ({latin})'
    n, i = name, 2
    while n in seen:
        n = f'{name} {i}'; i += 1
    seen.add(n)
    return n


def mob(r, seen):
    id_, common, latin, plan, uw, rng, ext, scale, diet, hab, kg, notes = r[:12]
    tags = [t.strip() for t in notes.split(',')]
    habs = lst([HAB.get(x, x) for x in hab.split(';')], 3)
    regs = lst(rng.split(';'), 3)
    k = h(id_, 3)
    gone = ext != '-'
    s1 = [f'{common} ({latin}) {"lived" if gone else "lives"} in {regs}.',
          f'The {common.lower() if common[1:2].islower() else common}, {latin}, {"ranged" if gone else "is found"} across {regs}.',
          f'Native to {regs}, the {common} is {"an extinct" if gone else "a"} species known as {latin}.'][k]
    s2 = [f'It {"favoured" if gone else "favours"} {habs} and {DIETV[diet]} {DIET[diet]}.' if diet in DIETV else '',
          f'Its habitat is {habs}, and its diet is {DIET.get(diet, diet)}.',
          f'Look for it in {habs}; it {"ate" if gone else "eats"} {DIET.get(diet, diet)}.'][h(id_ + 'b', 3)]
    if diet == 'herb' and h(id_ + 'b', 3) == 0:
        s2 = f'It favours {habs} and grazes and browses on plants.' if not gone else f'It favoured {habs} and grazed and browsed on plants.'
    s3 = f'An adult weighs about {mass(kg)}.' if h(id_ + 'c', 2) else f'Adults average {mass(kg)}.'
    tr = [TRAIT[t] for t in tags if t in TRAIT]
    parts = [s1, s2, s3]
    if tr: parts.append(tr[h(id_ + 'd', len(tr))] if len(tr) > 1 else tr[0])
    if gone: parts.append(f'The last of them died out around {year(ext)}.')
    return uniq(seen, common, latin), 'being', 1, 'fauna', ' '.join(p for p in parts if p)


def fossil(r, seen):
    id_, common, latin, plan, period, mf, mt, rock, region, form, scale, kg, diet, tier, notes = r[:15]
    age = PERIOD_AGE.get(period, 5)
    rk = ROCK.get(rock, rock)
    reg = lst(region.split(';'), 3)
    k = h(id_, 3)
    s1 = [f'{common} ({latin}) lived in the {period}, about {mf} to {mt} million years ago.',
          f'{common}, or {latin}, is a {period} animal from {mf} to {mt} million years ago.',
          f'The {period} {common} ({latin}) dates from {mf} to {mt} million years ago.'][k]
    s2 = [f'Its bones are dug from {rk} of the {form} formation in {reg}.',
          f'Fossils come from the {form} formation in {reg}, found in {rk}.',
          f'Dig {rk} of the {form} formation in {reg} to find it.'][h(id_ + 'b', 3)]
    d = DIET.get(diet)
    s3 = f'It weighed about {mass(kg)} and its diet was {d}.' if d else f'It weighed about {mass(kg)}.'
    s4 = f'Players dig and study it from Age {["", "", "", "III", "IV", "V"][age]}.' if h(id_ + 'e', 2) else \
        f'Age {["", "", "", "III", "IV", "V"][age]} players dig and study it as a {ERA[age]} fossil.'
    return uniq(seen, common, latin), 'being', age, 'fossils', ' '.join([s1, s2, s3, s4])


def main():
    seen = set(); groups = {}
    for f in sorted(glob.glob(os.path.join(CAT, '*.psv'))):
        g = os.path.basename(f)[:-4]
        if g.startswith('coat_'): continue
        for line in open(f, encoding='utf-8'):
            line = line.rstrip('\n')
            if not line.strip() or line.startswith('#'): continue
            r = line.split('|')
            e = fossil(r, seen) if len(r) >= 15 else mob(r, seen)
            groups.setdefault(g, []).append(list(e))
    os.makedirs(OUT, exist_ok=True)
    tot = 0
    for g, es in groups.items():
        with open(os.path.join(OUT, f'animals_{g}.json'), 'w', encoding='utf-8') as fh:
            json.dump(es, fh, ensure_ascii=False, indent=0)
        tot += len(es)
    print(tot, 'entries in', len(groups), 'files')

main()
