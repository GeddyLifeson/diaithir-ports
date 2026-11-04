import json,sys,pathlib,re,sqlite3
J=pathlib.Path(sys.argv[1]); O=pathlib.Path('/mnt/project-files/planets/specs')
src=open('/mnt/project-files/planets/flora_jobs.py').read()
def menu(n): return set(re.search(n+r'="([^"]*)"\.split\(\)',src).group(1).split())
LOGB,PLB,ORB=menu('LOGB'),menu('PLB'),menu('ORB')
SH={'oak','fancy_oak','birch','spruce','pine','acacia','dark_oak','jungle_tree','cherry'}
S=sqlite3.connect('file:/mnt/attach/project-files/codex-store/library.sqlite?mode=ro',uri=True)
hx=lambda c:bool(re.fullmatch(r'#[0-9a-fA-F]{6}',str(c)))
ok=bad=0
for l in open(J/'out.jsonl'):
    o=json.loads(l)
    try:
        t=o['text']; k=json.loads(t[t.index('{'):t.rindex('}')+1])
        for slot in ('tree','plant','ore'):
            r=S.execute("select name from entry where id=?",(k[slot]['entry'],)).fetchone(); assert r,(slot,'entry')
            k[slot]['name']=r[0]
        tr,pl,orr=k['tree'],k['plant'],k['ore']
        assert tr['log_base'] in LOGB and tr['leaf_base'] in LOGB and tr['shape'] in SH and hx(tr['log_color']) and hx(tr['leaf_color'])
        assert pl['base'] in PLB and hx(pl['color']) and orr['base'] in ORB and hx(orr['color'])
        orr['rarity']=max(1,min(3,int(orr.get('rarity',2))))
        assert len({tr['entry'],pl['entry'],orr['entry']})==3
        f=O/(o['id']+'.json'); sp=json.loads(f.read_text()); sp['flora']=k; f.write_text(json.dumps(sp,indent=1)); ok+=1
    except Exception as e: bad+=1; print(o['id'],repr(o.get('error',e))[:90])
print(ok,bad)
