import zipfile,glob,json,re,sys
# usage: scan_items.py MC_CLIENT_JAR MODS_DIR OUT_JSON
MC,MODS,OUT=sys.argv[1:4]
items={}; lang={}; itemlang=set()
def scan(z):
    for n in z.namelist():
        m=re.match(r'assets/([a-z0-9_.-]+)/items/(.+)\.json$',n)
        if m: items.setdefault(f"{m[1]}:{m[2]}",None)
        m=re.match(r'assets/([a-z0-9_.-]+)/lang/en_us\.json$',n)
        if m:
            try: d=json.loads(z.read(n).decode('utf8','ignore'))
            except Exception: continue
            for k,v in d.items():
                p=k.split('.',2)
                if len(p)==3 and '.' not in p[2] and p[0] in('item','block') and isinstance(v,str) and len(v)<48 and '%' not in v:
                    kk=f"{p[1]}:{p[2].replace('.','/')}"; lang.setdefault(kk,v)
                    if p[0]=='item': itemlang.add(kk)
for f in [MC]+glob.glob(MODS+'/*.jar'):
    try: scan(zipfile.ZipFile(f))
    except Exception as e: print('bad',f)
for i in items:
    ns,p=i.split(':'); items[i]=lang.get(i)
for k,v in lang.items():
    if k not in items and v and k in itemlang: items[k]=v   # runtime-registered items without an items/ definition
json.dump(items,open(OUT,'w'),ensure_ascii=False,indent=0)
print(len(items),sum(1 for v in items.values() if v))
