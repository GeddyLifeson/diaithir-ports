#!/usr/bin/env python3
"""Roster CSV, names, Age gates, library, Codex, assembly-yard JSON and part catalog for the diaithirtrucks pack.
Reads truck_manifest.json (written by gen_trucks.py). Usage: python3 -I tools/gen_trucks_meta.py <module dir>"""
import csv, json, os, sys
sys.path.insert(0, '/mnt/project-files/diaithiris-archaic/tools')
try:
    import archaic
    nat = archaic.text
except Exception:  # archaic layer not mounted: keep the English
    nat = lambda s: s
M = sys.argv[1] if len(sys.argv) > 1 else '.'
PK = 'diaithirtrucks'
man = json.load(open(f'{M}/truck_manifest.json'))
ID = lambda x: f'mts:{PK}.{x}'
WIKI = lambda p: 'https://en.wikipedia.org/wiki/' + p
def w(path, d): os.makedirs(os.path.dirname(path), exist_ok=True); open(path, 'w').write(json.dumps(d, indent=1, ensure_ascii=False))

KEN = {'tractor': 'carbad tarraing', 'trailer': 'carbad luchda', 'construction': 'carbad oibre'}
names, ages, lib, rows = {}, {}, [], []
for kind, items in (('tractor', man['tractors']), ('trailer', man['trailers']), ('construction', man['construction'])):
    for v in items:
        n = nat(KEN[kind] + ' ' + v['name'])
        names[ID(v['id'])] = {'diaithiris': n, 'english': v['name']}
        ages[ID(v['id'])] = v['age']
        pw = v.get('power_w', 0) // 1000
        extra = (f"{pw} kW, {v['mass_kg']} kg, gross {v['gvw_kg']} kg" if kind == 'tractor' else f"payload {v['payload_kg']} kg, {v['length_m']} m" if kind == 'trailer'
                 else f"{pw} kW, {v['mass_kg']} kg, road role {v['road_role']}")
        lib.append([n, 'item', ['I', 'II', 'III', 'IV', 'V', 'VI', 'VII'].index(v['age']) + 1, 'transport', f"{v['name']} ({v['year']}): {extra}."])
        rows.append([v['id'], kind, v['name'], v.get('maker', ''), v['year'], v.get('country', ''), v.get('layout', v.get('kind', '')), pw or '', v['mass_kg'], v.get('gvw_kg', v.get('payload_kg', '')), v['age'], WIKI(v['wiki'])])
for e in man['engines'] + man['cargo_parts']:
    n = nat('inneal ' + e['name']) if 'kw' in e else nat('ceann ' + e['name'])
    names[ID(e['id'])] = {'diaithiris': n, 'english': e['name']}; ages[ID(e['id'])] = 'VI'
for pid, nm in (('truck_wheel', 'Truck Wheel'), ('heavy_wheel', 'Heavy Wheel'), ('giant_wheel', 'Giant Wheel'), ('small_wheel', 'Small Wheel'), ('cv_track', 'Machine Track'), ('seat', 'Cab Seat')):
    names[ID(pid)] = {'diaithiris': nat('roth ' + nm), 'english': nm}; ages[ID(pid)] = 'VI'
os.makedirs(f'{M}/roster', exist_ok=True)
with open(f'{M}/roster/TRUCK_ROSTER.csv', 'w', newline='') as f:
    cw = csv.writer(f); cw.writerow(['id', 'kind', 'name', 'maker', 'year', 'country', 'layout_or_type', 'power_kW', 'mass_kg', 'gross_or_payload_kg', 'age', 'source_wikipedia'])
    cw.writerows(rows)
w(f'{M}/age_gates_trucks.json', {'_doc': 'Same shape as DiaithirRPG content/tech_ages.json; ids are Immersive Vehicles item ids (mts:<packid>.<system name>). Before 1861 V, to 1945 VI, after VII.', 'items': ages})
w(f'{M}/names_map_trucks.json', names)
os.makedirs(f'{M}/library', exist_ok=True); os.makedirs(f'{M}/codex', exist_ok=True)
w(f'{M}/library/trucks_library.json', lib)
w(f'{M}/codex/trucks_handbook.json', {'id': f'{PK}:handbook', 'title': nat('Carbaid luchda'), 'chapters': [
 {'tier': k, 'name': nat(KEN[k]), 'age': 'VI', 'text': t, 'items': [{'id': ID(v['id']), 'name': names[ID(v['id'])]['diaithiris'], 'age': v['age'], 'text': v['name']} for v in man[key]]}
 for k, key, t in (('tractor', 'tractors', 'Semi tractor units, one per design. Hook a trailer to the fifth wheel.'), ('trailer', 'trailers', 'Box, flatbed, tank, reefer, lowboy, car carrier, log and heavy-haul trailers with real cargo space.'),
                   ('construction', 'construction', 'Rollers, mixers, pavers, graders and diggers for building roads.'))]})

# assembly yard (PR 186 spec v1): data only; the rollout hook belongs to whoever owns the yard core
parts = {}
for e in man['engines']: parts[e['id']] = {'item': ID(e['id']), 'kind': 'engine', 'mass_kg': e['mass'], 'power_w': e['kw'] * 1000, 'extra': {'fuel_l_per_h': e['lph']}}
for pid, kg in (('truck_wheel', 120), ('heavy_wheel', 250), ('giant_wheel', 3500), ('small_wheel', 30)): parts[pid] = {'item': ID(pid), 'kind': 'wheel', 'mass_kg': kg}
parts['cv_track'] = {'item': ID('cv_track'), 'kind': 'track', 'mass_kg': 900}; parts['seat'] = {'item': ID('seat'), 'kind': 'seat', 'mass_kg': 30, 'crew_slots': 1}
for c in man['cargo_parts']: parts[c['id']] = {'item': ID(c['id']), 'kind': 'hold' if c['kind'] == 'crate' else 'tank', 'mass_kg': 600 * c['units'], 'capacity_l': c['litres'] or c['slots'] * 64}
for k, p in parts.items(): p['mass_kg'] = p.get('mass_kg', 0); p['heavy'] = p['mass_kg'] >= 500; p['min_age'] = 'VI'
D_ = f'{M}/src/main/resources/data/{PK}'
w(f'{D_}/parts_catalog.json', {'_doc': 'PartDef rows for the assembly yard API (plans/ASSEMBLY_API.md, ports PR 186). Items are Immersive Vehicles part items.', 'parts': [dict(id=k, **v) for k, v in parts.items()]})
def yard(v):
    d = json.load(open(f'{M}/src/main/resources/assets/{PK}/jsondefs/vehicles/{v["id"]}.json'))
    cnt = lambda t: sum(1 for p in d['parts'] if t in p['types'])
    has = lambda t: next((p['defaultPart'].split(':')[1] for p in d['parts'] if t in p['types']), None)
    st = []
    def add(i, slots, ticks, ghost): st.append({'id': i, 'order': len(st) + 1, 'slots': slots, 'base_ticks': ticks, 'ghost': f'{PK}:ghost/{v["id"]}_{ghost}'})
    if cnt('engine_car'): add('powertrain', [{'kind': 'engine', 'count': 1}], 600, 'chassis')
    if cnt('ground_wheel'): add('running_gear', [{'kind': 'wheel', 'count': cnt('ground_wheel')}], 400, 'gear')
    if cnt('ground_tread'): add('running_gear', [{'kind': 'track', 'count': cnt('ground_tread')}], 400, 'gear')
    if cnt('seat'): add('crew', [{'kind': 'seat', 'count': 1}], 100, 'cab')
    for t, k in (('interactable_crate', 'hold'), ('interactable_barrel', 'tank')):
        if cnt(t): add('cargo', [{'kind': k, 'count': 1}], 300, 'body')
    L = v.get('length_m', 12)
    return {'id': v['id'], 'site': 'ground', 'dimensions': [], 'footprint': {'w': 5, 'l': int(L) + 4, 'h': 6}, 'result': ID(v['id']), 'stages': st, 'crane_speedup': 1.0}
for key in ('tractors', 'trailers', 'construction'):
    for v in man[key]: w(f'{D_}/yards/{v["id"]}.json', yard(v))
print(len(names), 'names', len(ages), 'age gates', len(lib), 'library entries', len(rows), 'roster rows', len(parts), 'catalog parts')

# data contracts for the depot and road lanes (the loaders and the road builder live in other PRs; these files only say what each vehicle is)
def fuel_id(v):
    n = v['id']
    if n in ('truck_ox_team', 'truck_horse_team'): return 'hay'
    if n in ('truck_steam_traction', 'truck_steam_wagon', 'cv_steam_shovel'): return 'coal'
    if n in ('truck_scammell_scarab', 'truck_diamond_t_980'): return 'petrol'
    return 'diesel' if v.get('power_w', 0) else ''
for key in ('tractors', 'construction'):
    for v in man[key]: v['fuel'] = fuel_id(v)
cargo = {}
for v in man['trailers']:
    cargo[v['id']] = {'class': 'liquid' if v['tank'] else 'vehicle' if v['cargo'] == 'car' else 'bulk' if v['kind'] in ('dump', 'log') else 'general', 'payload_kg': v['payload_kg'],
                      'hold': v['crate'] or v['tank'], 'hitch': 'drawbar' if v['kind'] in ('cart', 'wagon', 'dray') else 'fifthwheel'}
for v in man['construction']:
    if v['kind'] in ('mixer', 'haul', 'dumpcart', 'pump'): cargo[v['id']] = {'class': 'liquid' if v['kind'] == 'pump' else 'bulk', 'payload_kg': v['mass_kg'] // 2, 'hold': None, 'hitch': 'self'}
w(f'{D_}/depot_cargo.json', {'_doc': 'Cargo class, payload and hitch per vehicle for depot loaders and the company loop. Hold ids are IV part ids of this pack.', 'vehicles': cargo})
ROADS = {'grade': ['dirt_track', 'gravel'], 'compact': ['gravel', 'cobbled', 'macadam', 'asphalt'], 'pave': ['asphalt'], 'mix': ['concrete'], 'mill': ['asphalt'], 'haul': ['gravel', 'macadam', 'asphalt', 'concrete'], 'dig': ['dirt_track']}
rr = {}
for v in man['construction']: rr.setdefault(v['road_role'], {'surfaces': ROADS[v['road_role']], 'machines': []})['machines'].append(dict(id=v['id'], age=v['age']))
w(f'{D_}/road_roles.json', {'_doc': 'Which machine does which road job. Surface ids are the six road blocks of diaithir-carts roads.csv (ports #190); the builder that reads this is not written.', 'roles': rr})
json.dump(man, open(f'{M}/truck_manifest.json', 'w'), indent=1)
