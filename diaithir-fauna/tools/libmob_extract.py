#!/usr/bin/env python3
# S6832 Sorts every Library MOB entry onto a fauna body plan (writes the Haiku job).
"""Write the Haiku job that sorts every Library MOB entry onto a fauna body plan.
Usage: libmob_extract.py JOB_DIR   (reads codex-store/library.sqlite + library-content/content.sqlite, plans from resources)
Only the entry's own name and summary go in; the model picks from fixed vocabularies, it writes no text."""
import json, os, sqlite3, sys
JOB = sys.argv[1]
ROOT = '/mnt/project-files'
PL = os.path.join(os.path.dirname(os.path.abspath(__file__)), '..', 'src/main/resources/data/diaithirfauna/plans')
plans = json.load(open(f'{PL}/index.json'))
desc = []
for p in plans:
    m = json.load(open(f'{PL}/{p}.json'))['meta']
    desc.append(p if m['cls'] == 'land' else f"{p}[{m['cls']}]")
SYSTEM = ("You sort fictional creatures onto a fixed set of body plans for a game. Use ONLY what the entry says; if it does not say, use the default.\n"
 "Body plans (use the bare name, e.g. frog; [fly] and [water] mark flyers and swimmers, land is the default): " + ', '.join(desc) + ".\n"
 "For each numbered entry return an object: i (the number), plan (exactly one body plan name above, the closest in body shape and movement; no bear, deer, cat, dog or ghost plan exists, so pick the nearest shape; a flying creature gets a [fly] plan, a swimming one a [water] plan), "
 "size (tiny <1 kg, small 1-20 kg, medium 20-200 kg, large 200-2000 kg, huge 2-20 t, colossal >20 t; default medium), "
 "diet (herb, carn, omni, pisc, insect, none; default omni), "
 "temper (passive, wary, hostile; hostile only if the text says it attacks, hunts people, or is monstrous; default wary), "
 "ranged (true only if the text says it throws, spits, shoots or casts at range), "
 "element (none, fire, ice, lightning, poison, shadow, light, earth, water, wind; only if the text names it).\n"
 "Return a JSON array, one object per entry, in order.")
c = sqlite3.connect(f'file:{ROOT}/library-content/content.sqlite?mode=ro', uri=True)
s = sqlite3.connect(f'file:{ROOT}/codex-store/library.sqlite?mode=ro', uri=True)
rows = c.execute("select id,name from content where form='MOB' order by id").fetchall()
os.makedirs(JOB, exist_ok=True)
n = 0
with open(f'{JOB}/in.jsonl', 'w') as f:
    for k in range(0, len(rows), 40):
        chunk = rows[k:k+40]
        lines = []
        for j, (i, name) in enumerate(chunk):
            r = s.execute("select summary from entry where id=?", (i,)).fetchone()
            lines.append(f"{j}. {name}: {(r[0] if r else '')[:400]}")
        f.write(json.dumps({'id': f'b{k//40:05d}', 'system': SYSTEM, 'prompt': '\n'.join(lines),
                            'ids': [i for i, _ in chunk]}, ensure_ascii=False) + '\n')
        n += 1
open(f'{JOB}/README', 'w').write("Owner: Panscriptum mobs lane (thread Build Panscriptum mobs in game). Sort 59,431 Library MOB entries onto fauna body plans, 40 per row. max_tokens 3500. model: haiku. sync. effort: low\n"
 "Check: JSON array of 40 objects with i 0-39, plan in the plan list, size/diet/temper/element in the allowed sets. Failures retry in libmob_classify-retry.\n")
print(n, 'rows')
