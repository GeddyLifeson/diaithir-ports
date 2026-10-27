import re,json,os,zipfile,glob
log=open('boot.log',errors='replace').read()
p=set(json.load(open('prune.json'))) if os.path.exists('prune.json') else set()
n0=len(p)
names=set()
for j in glob.glob('out/*.jar'): names|=set(zipfile.ZipFile(j).namelist())
for m in re.finditer(r"Couldn't parse data file '([^']+)'",log):
    ns,path=m.group(1).split(':',1)
    for n in names:
        if n.startswith(f'data/{ns}/') and n.endswith(f'/{path}.json') and n.split('/')[2] in('recipe','advancement','loot_table'): p.add(n)
json.dump(sorted(p),open('prune.json','w'))
print('pruned +',len(p)-n0)
