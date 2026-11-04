# Named per-planet tree/plant/ore pass. python3 -I flora_jobs.py JOBDIR ; spec_run.py JOBDIR ; merge_flora.py JOBDIR  -> specs/<id>.json["flora"]
import sqlite3,json,sys,pathlib,random,collections
R=pathlib.Path('/mnt/project-files'); job=pathlib.Path(sys.argv[1])
P=json.load(open(R/'planets/planets.json'))
LOGB="oak spruce birch dark_oak acacia cherry jungle mangrove pale_oak".split()
PLB="dandelion poppy blue_orchid allium azure_bluet red_tulip oxeye_daisy cornflower lily_of_the_valley torchflower fern".split()
ORB="coal iron copper gold redstone lapis diamond emerald".split()
SYS=f"""You pick three NAMED natural things for one fictional planet of a Minecraft-style game, each taken from the planet's REAL entries (id | name | text): one tree, one plant, one ore. Pick entries whose name and text really suit a tree / a small plant or flower / a mineral or ore (material, metal, gem, stone, wood). Never invent a name; use only an input entry id. Choose different entries for the three. Pick looks that match the entry text, and make this planet's trio distinctive: use the whole colour range.
Reply with JSON only:
{{"tree":{{"entry":"<id>","log_base":"<{'|'.join(LOGB)}>","leaf_base":"<{'|'.join(LOGB)}>","log_color":"#rrggbb","leaf_color":"#rrggbb","shape":"<oak|fancy_oak|birch|spruce|pine|acacia|dark_oak|jungle_tree|cherry>"}},
"plant":{{"entry":"<id>","base":"<{'|'.join(PLB)}>","color":"#rrggbb"}},
"ore":{{"entry":"<id>","base":"<{'|'.join(ORB)}>","color":"#rrggbb","rarity":1-3 (1 common, 3 rare)}},
"grounded":true|false}} (grounded=false only if no entry suits a slot and you had to stretch)"""
c=sqlite3.connect(f'file:{R}/library-content/content.sqlite?mode=ro',uri=True)
s=sqlite3.connect('file:/mnt/attach/project-files/codex-store/library.sqlite?mode=ro',uri=True)
by=collections.defaultdict(list)
for i,u in c.execute("select id,universe from content where form in ('BIOME','MATERIAL','FOOD','LORE','TOOL','STRUCTURE')"): by[u].append(i)
rows=[]
for p in P:
    if p['planet_id'] in ('diaithir','alaima'): continue
    ids=sorted(by[p['name']]); random.Random('flora'+p['planet_id']).shuffle(ids); ids=ids[:120]
    ents=[s.execute("select id,name,substr(summary,1,160) from entry where id=?",(i,)).fetchone() for i in ids]
    rows.append({"id":p['planet_id'],"system":SYS,"prompt":f"Planet: {p['name']}\nEntries:\n"+"\n".join(f"{i} | {n} | {m}" for i,n,m in ents if i)})
job.mkdir(parents=True,exist_ok=True)
with open(job/'in.jsonl','w') as f:
    for r in rows: f.write(json.dumps(r)+'\n')
open(job/'README.md','w').write("max_tokens 1500\nmodel: haiku\nCheck: valid JSON; entry ids must exist. Owner: planets thread.\n")
print(len(rows),'rows')
