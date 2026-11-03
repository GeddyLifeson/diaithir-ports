#!/usr/bin/env python3
# S6873 Habitat pass for Library mobs: writes the Haiku job (extract) and adds a habitat bitmask column to library_mobs.tsv.gz (merge).
"""Usage: libmob_hab.py extract JOB_DIR | libmob_hab.py merge JOB_DIR [RETRY_IN_JSONL]
Habitats are the fauna HABS list (species.json); an empty list means it can live anywhere. Only the entry's own text is used.
Column 10 of library_mobs.tsv.gz is the bitmask (bit i = HABS[i]); 0 = anywhere."""
import gzip, json, os, re, sqlite3, sys
HERE = os.path.dirname(os.path.abspath(__file__))
RES = os.path.join(HERE, '..', 'src/main/resources/data/diaithirfauna')
OUTF = f'{RES}/library_mobs.tsv.gz'
HABS = json.load(open(f'{RES}/species.json'))['habitats']
mode, JOB = sys.argv[1], sys.argv[2].rstrip('/')
if mode == 'extract':
    SYSTEM = ("You place fictional creatures in habitats for a game. Use ONLY what the entry says (where it lives, what it eats, how it moves); "
      "if it names no place, return an empty list.\nHabitats: " + ', '.join(HABS) + ".\n"
      "forest also covers woods and groves; plains and grassland cover open fields; mountain covers hills, cliffs and peaks; icy and tundra cover snow and frozen ground; "
      "swamp and wetland cover marsh, bog and mangrove; coast covers shores and beaches; ocean_warm, ocean_cold and ocean_deep are the open sea; reef is shallow sea; cave covers caverns, tunnels and underground.\n"
      "For each numbered entry return an object {\"i\": number, \"hab\": [habitats from the list, at most 4]}. Return a JSON array, one object per entry, in order.")
    c = sqlite3.connect('file:/mnt/project-files/library-content/content.sqlite?mode=ro', uri=True)
    s = sqlite3.connect('file:/mnt/project-files/codex-store/library.sqlite?mode=ro', uri=True)
    ids = [l.split('\t', 1)[0] for l in gzip.open(OUTF, 'rt', encoding='utf-8') if l.strip()]
    os.makedirs(JOB, exist_ok=True)
    with open(f'{JOB}/in.jsonl', 'w') as f:
        for k in range(0, len(ids), 40):
            chunk = ids[k:k+40]; lines = []
            for j, i in enumerate(chunk):
                n = c.execute("select name from content where id=?", (i,)).fetchone()[0]
                r = s.execute("select summary from entry where id=?", (i,)).fetchone()
                lines.append(f"{j}. {n}: {(r[0] if r else '')[:400]}")
            f.write(json.dumps({'id': f'h{k//40:05d}', 'system': SYSTEM, 'prompt': '\n'.join(lines), 'ids': chunk}, ensure_ascii=False) + '\n')
    open(f'{JOB}/README', 'w').write("Owner: Panscriptum mobs lane. Habitat per Library mob, 40 per row. max_tokens 2500. model: haiku. sync. effort: low\n")
    print(len(ids) // 40 + 1, 'rows')
else:
    RETRY = sys.argv[3] if len(sys.argv) > 3 else JOB + '-retry/in.jsonl'
    done = {}
    for fn in ('out.jsonl', 'out2.jsonl'):
        if os.path.exists(f'{JOB}/{fn}'):
            for l in open(f'{JOB}/{fn}'):
                o = json.loads(l); done[o['id']] = o['text']
    mask = {}
    old = {}
    rows = [l.rstrip('\n').split('\t') for l in gzip.open(OUTF, 'rt', encoding='utf-8') if l.strip()]
    for r in rows:
        if len(r) > 9: old[r[0]] = r[9]
    retry = []
    for l in open(f'{JOB}/in.jsonl'):
        o = json.loads(l)
        try:
            arr = json.loads(re.sub(r'^```(?:json)?|```$', '', done.get(o['id'], '').strip(), flags=re.M).strip())
        except Exception:
            arr = []
        miss = False
        for j, eid in enumerate(o['ids']):
            a = next((x for x in arr if x.get('i') == j), None)
            if a is None or not isinstance(a.get('hab'), list): miss = True; continue
            m = 0
            for h in a['hab']:
                if h in HABS: m |= 1 << HABS.index(h)
            mask[eid] = str(m)
        if miss: retry.append(o)
    if retry:
        os.makedirs(os.path.dirname(RETRY), exist_ok=True)
        with open(RETRY, 'w') as f:
            for o in retry: f.write(json.dumps(o, ensure_ascii=False) + '\n')
    with gzip.open(OUTF, 'wt', encoding='utf-8') as f:
        for r in rows:
            r = r[:9] + [mask.get(r[0], old.get(r[0], '0'))]
            f.write('\t'.join(r) + '\n')
    print(len(mask), 'habitats;', len(retry), 'batches to retry')
