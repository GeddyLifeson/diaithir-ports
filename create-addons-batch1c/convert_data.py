"""Convert Fabric load conditions in data JSON to NeoForge conditions; rename 'create' mod ids to diaithir_create."""
import json, sys, pathlib
def conv(c):
    t = c.get('condition') or c.get('type')
    if t == 'fabric:all_mods_loaded':
        vals = [('diaithir_create' if v == 'create' else v) for v in c['values']]
        mods = [{'type': 'neoforge:mod_loaded', 'modid': v} for v in vals]
        return mods[0] if len(mods) == 1 else {'type': 'neoforge:and', 'values': mods}
    if t == 'fabric:not': return {'type': 'neoforge:not', 'value': conv(c['value'])}
    if t == 'fabric:and': return {'type': 'neoforge:and', 'values': [conv(x) for x in c['values']]}
    if t == 'fabric:or': return {'type': 'neoforge:or', 'values': [conv(x) for x in c['values']]}
    if t == 'fabric:tags_populated':
        return {'type': 'neoforge:not', 'value': {'type': 'neoforge:tag_empty', 'tag': c['values'][0]}}
    d = dict(c); d.pop('condition', None); d['type'] = t
    return d
def walk(o):
    if isinstance(o, dict):
        if 'fabric:load_conditions' in o:
            lc = o.pop('fabric:load_conditions'); lc = lc if isinstance(lc, list) else [lc]; conds = [conv(c) for c in lc]
            o = {'neoforge:conditions': conds, **o}
        return {k: walk(v) for k, v in o.items()}
    if isinstance(o, list): return [walk(x) for x in o]
    return o
for root in sys.argv[1:]:
    for p in pathlib.Path(root).rglob('*.json'):
        txt = p.read_text()
        if 'fabric:' not in txt: continue
        p.write_text(json.dumps(walk(json.loads(txt)), indent=2) + '\n')
        print('converted', p)
