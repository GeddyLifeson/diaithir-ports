#!/usr/bin/env python3
"""Checks lane-drafted Library text (compute/jobs/animals_library/out.jsonl) and writes tools/library_accepted.json {id: text}; library_gen.py then uses it."""
import json, os, re
J = '/mnt/project-files/compute/jobs/animals_library'
BAN = re.compile(r"fascinating|remarkable|testament|tapestry|realm|—|–|;|\?", re.I)
facts = {json.loads(l)['id']: json.loads(l)['prompt'] for l in open(f'{J}/in.jsonl')}
ok = {}; bad = 0
for l in open(f'{J}/out.jsonl'):
    o = json.loads(l); t = ' '.join(o['text'].split())
    n = len(t.split())
    nums = set(re.findall(r'\d[\d,.]*', t)) - {'1', '2'}
    pf = facts.get(o['id'], '')
    if 35 <= n <= 85 and not BAN.search(t) and all(x.strip(',.') in pf.replace(',', '') for x in nums):
        ok[o['id']] = t
    else:
        bad += 1
json.dump(ok, open(os.path.join(os.path.dirname(os.path.abspath(__file__)), 'library_accepted.json'), 'w'), ensure_ascii=False)
print(len(ok), 'accepted', bad, 'rejected')
