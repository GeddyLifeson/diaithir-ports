#!/usr/bin/env python3
# S6834 Merges the classify output into library_mobs.tsv.gz for diaithir-fauna.
"""Merge the libmob_classify job into src/main/resources/data/diaithirfauna/library_mobs.tsv.gz.
Usage: libmob_build.py JOB_DIR [RETRY_IN_JSONL]   Rows that fail the check go to RETRY_IN_JSONL (default JOB_DIR-retry/in.jsonl).
Row: id name plan tier kg scale diet flags element. Scale follows the plan's median species: scale * (kg/kg_ref)^(1/3)."""
import gzip, json, os, re, sqlite3, statistics, sys
HERE = os.path.dirname(os.path.abspath(__file__))
RES = os.path.join(HERE, '..', 'src/main/resources/data/diaithirfauna')
JOB = sys.argv[1].rstrip('/')
RETRY = sys.argv[2] if len(sys.argv) > 2 else JOB + '-retry/in.jsonl'
KG = {'tiny': 0.3, 'small': 6, 'medium': 70, 'large': 700, 'huge': 7000, 'colossal': 50000}
DIET = {'herb': 'herb', 'carn': 'carn', 'omni': 'omni', 'pisc': 'pisc', 'insect': 'insect', 'none': 'herb'}
EL = {'none', 'fire', 'ice', 'lightning', 'poison', 'shadow', 'light', 'earth', 'water', 'wind'}
# no bear, deer or humanoid plan exists in fauna: nearest body shape
ALIAS = {'ungulate': 'cervid', 'ungulate_placeholder': 'cervid', 'ghost': 'humanoid', 'bear': 'ursine', 'deer': 'cervid', 'cat': 'felid', 'dog': 'canid', 'feline': 'felid', 'person': 'humanoid',
         'turtle': 'sea_turtle', 'tortoise': 'sea_turtle', 'viperid_small': 'lizard', 'ant': 'beetle', 'spider': 'scorpion', 'finch': 'songbird',
         'woodpecker': 'songbird', 'cuckoo': 'songbird', 'pelican': 'seabird', 'wolf': 'canid'}
F_FLIES, F_SWIMS, F_AGGRESSIVE = 1 << 0, 1 << 7, 1 << 8
sp = json.load(open(f'{RES}/species.json'))['species']
plans = set(json.load(open(f'{RES}/plans/index.json')))
cls = {p: json.load(open(f'{RES}/plans/{p}.json'))['meta']['cls'] for p in plans}
ref = {}
for p in plans:
    xs = [(s['scale'], s['kg']) for s in sp if s['plan'] == p and not s.get('fossil')] or [(s['scale'], s['kg']) for s in sp if s['plan'] == p]
    ref[p] = (statistics.median(a for a, _ in xs), statistics.median(b for _, b in xs)) if xs else (1.0, 70.0)
c = sqlite3.connect('file:/mnt/project-files/library-content/content.sqlite?mode=ro', uri=True)
meta = {i: (n, t) for i, n, t in c.execute("select id,name,tier from content where form='MOB'")}
done = {}
for fn in ('out.jsonl', 'out2.jsonl'):
    if os.path.exists(f'{JOB}/{fn}'):
        for l in open(f'{JOB}/{fn}'):
            o = json.loads(l)
            done[o['id']] = o
src = {}
for l in open(f'{JOB}/in.jsonl'):
    o = json.loads(l); src[o['id']] = o
rows, retry = {}, []
OUTF = f'{RES}/library_mobs.tsv.gz'
if os.path.exists(OUTF):  # incremental: keep rows from earlier runs (job + retry jobs)
    for l in gzip.open(OUTF, 'rt', encoding='utf-8'):
        if l.strip(): rows[l.split('\t', 1)[0]] = l.rstrip('\n')
hab = {k: v.split('\t')[9] for k, v in rows.items() if len(v.split('\t')) > 9}  # habitat column survives a re-sort
for rid, o in src.items():
    t = (done.get(rid) or {}).get('text', '')
    try:
        arr = json.loads(re.sub(r'^```(?:json)?|```$', '', t.strip(), flags=re.M).strip())
        assert isinstance(arr, list)
    except Exception:
        arr = []
    for j, eid in enumerate(o['ids']):
        try:
            a = next(x for x in arr if x.get('i') == j)
            a['size'] = {'default': 'medium'}.get(a['size'], a['size']); a['element'] = a.get('element') or 'none'
            a['plan'] = re.sub(r'\s*[\[(].*$', '', str(a['plan'])).strip()
            a['plan'] = ALIAS.get(a['plan'], a['plan'])
            assert a['plan'] in plans and a['size'] in KG and a['element'] in EL
            n, tier = meta[eid]
            kg = KG[a['size']]; sc0, kg0 = ref[a['plan']]
            scale = max(0.2, min(6.0, sc0 * (kg / kg0) ** (1 / 3)))
            fl = (F_FLIES if cls[a['plan']] == 'fly' else 0) | (F_SWIMS if cls[a['plan']] == 'water' else 0) | (F_AGGRESSIVE if a.get('temper') == 'hostile' else 0)
            rows[eid] = '\t'.join([eid, n.replace('\t', ' '), a['plan'], str(tier), str(kg), f'{scale:.2f}', DIET.get(a['diet'], 'omni'), str(fl), a['element']] + ([hab[eid]] if eid in hab else []))
        except Exception:
            pass
    if any(e not in rows for e in o['ids']): retry.append(o)
os.makedirs(os.path.dirname(RETRY), exist_ok=True)
if retry:
    with open(RETRY, 'w') as f:
        for o in retry: f.write(json.dumps({k: o[k] for k in ('id', 'system', 'prompt', 'ids')}, ensure_ascii=False) + '\n')
with gzip.open(OUTF, 'wt', encoding='utf-8') as f:
    f.write('\n'.join(rows[k] for k in sorted(rows)) + '\n')
print(len(rows), 'of', len(meta), 'rows;', len(retry), 'batches to retry')
