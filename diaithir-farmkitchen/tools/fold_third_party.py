#!/usr/bin/env python3
"""(S6954) Fold the MIT mods Agritech: Evolved and Hydrofarm into this module as extra mods of the same jar: their Java and resources are copied in
unchanged (own packages and namespaces, so nothing collides), their neoforge.mods.toml entries are appended to ours, and their LICENSE files go to THIRD_PARTY/.
Datagen classes and the Fabric loader are left out. Usage: python3 -I tools/fold_third_party.py <AgritechEvolved checkout> <hydrofarm checkout>"""
import os, re, shutil, subprocess, sys
M = os.path.join(os.path.dirname(os.path.abspath(__file__)), '..'); J = f'{M}/src/main/java'; R = f'{M}/src/main/resources'
ate, hyd = sys.argv[1], sys.argv[2]

def sha(d): return subprocess.run(['git', '-C', d, 'rev-parse', 'HEAD'], capture_output=True, text=True).stdout.strip()
def copy(src, dst, skip=lambda p: False):
    for root, _, files in os.walk(src):
        for f in files:
            s = os.path.join(root, f); rel = os.path.relpath(s, src)
            if skip(rel): continue
            d = os.path.join(dst, rel); os.makedirs(os.path.dirname(d), exist_ok=True); shutil.copyfile(s, d)

nodata = lambda rel: rel.split(os.sep)[-2:-1] == ['datagen'] or '/datagen/' in rel.replace(os.sep, '/')
copy(f'{ate}/src/main/java', J, nodata)
copy(f'{ate}/src/main/resources', R)
copy(f'{ate}/src/generated/resources', R, lambda rel: rel.startswith('.cache'))
for sub in ('common', 'neoforge'):
    copy(f'{hyd}/{sub}/src/main/java', J)
copy(f'{hyd}/shared-resources', R, lambda rel: rel in ('pack.mcmeta',))
copy(f'{hyd}/neoforge/src/main/resources', R, lambda rel: rel.endswith('neoforge.mods.toml'))
os.makedirs(f'{M}/THIRD_PARTY', exist_ok=True)
shutil.copyfile(f'{ate}/LICENSE.txt', f'{M}/THIRD_PARTY/LICENSE-AgritechEvolved.txt')
shutil.copyfile(f'{hyd}/LICENSE', f'{M}/THIRD_PARTY/LICENSE-Hydrofarm.txt')
open(f'{M}/THIRD_PARTY/README.md', 'w').write(f'''# Folded third-party mods (MIT)
Their code and resources are copied unchanged into this jar by `tools/fold_third_party.py`; each stays its own mod id and namespace.
- Agritech: Evolved 2.2.0.7 (`agritechevolved`), https://github.com/deonjonker123/AgritechEvolved, commit {sha(ate)}, MIT (LICENSE-AgritechEvolved.txt). Datagen classes left out; the generated data it produced is included.
- Hydrofarm 0.2.10 (`hydrofarm`), https://github.com/kestalkayden/hydrofarm, commit {sha(hyd)}, MIT (LICENSE-Hydrofarm.txt). Common and NeoForge sources and shared resources; Fabric left out.
''')
toml = f'{R}/META-INF/neoforge.mods.toml'; t = open(toml).read()
if 'modId = "agritechevolved"' not in t:
    t += '''
[[mods]]
modId = "agritechevolved"
version = "2.2.0.7"
displayName = "Agritech: Evolved"
authors = "MisterD Modding"
description = "Farming automation: planters, machines, modules and power generation (MIT, folded into Diaithir Farm Kitchen)."

[[dependencies.agritechevolved]]
modId = "neoforge"
type = "required"
versionRange = "[26.1.2.109,)"
ordering = "NONE"
side = "BOTH"

[[mods]]
modId = "hydrofarm"
version = "0.2.10"
displayName = "Hydrofarm"
authors = "Kestalkayden"
description = "Water-tech and growth automation: tanks, pipes, pumps, sprinklers, hydroponic planters (MIT, folded into Diaithir Farm Kitchen)."

[[dependencies.hydrofarm]]
modId = "neoforge"
type = "required"
versionRange = "[26.1.2.109,)"
ordering = "NONE"
side = "BOTH"

[[mixins]]
config = "hydrofarm.mixins.json"
'''
    open(toml, 'w').write(t)
