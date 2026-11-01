import json,os,sys,glob,shutil,zipfile,subprocess,re
sys.path.insert(0,'/home/user/work-N2')
ADD={'create-additions-electrum-block':['electrumblock'],'create-thermobaric':['create_thermobaric'],'create-coins':['createcoins'],
 'create-rags-to-riches':['crtr'],'create-choco-paste':['create_chocopaste'],'create-deepslate-processing':['create_deepslate_processing'],
 'create-extra-structures':['create_extra_structures']}
OUT='/home/user/work-N2/ports/create-addons-n2/src/main/resources'
shutil.rmtree('/home/user/work-N2/ports/create-addons-n2',ignore_errors=True)
shutil.copytree('/home/user/work-N2/ports/create-addons-b5','/home/user/work-N2/ports/create-addons-n2',ignore=shutil.ignore_patterns('dist','src','build','.gradle'))
os.makedirs(OUT+'/META-INF');T='/home/user/work-N2/tmpl'
D='/home/user/work-N2/ports/create-addons-n2'
shutil.copy(T+'/build.gradle',D+'/build.gradle');os.remove(D+'/results_B5.json')
os.makedirs(D+'/src/main/java/diaithir/n2');shutil.copy(T+'/java/N2Content.java',D+'/src/main/java/diaithir/n2/')
shutil.copy(T+'/meta/neoforge.mods.toml',OUT+'/META-INF/')
import conv2
manifest={}
for slug,nss in ADD.items():
    os.environ['OWN']=','.join(nss)
    outd=f'/home/user/work-N2/conv/{slug}';shutil.rmtree(outd,ignore_errors=True)
    jar,nrec=conv2.convert_jar(f'jars/{slug}.jar',outd,slug)
    z=zipfile.ZipFile(jar)
    names=z.namelist()
    for n in names:
        if not (n.startswith('assets/') or n.startswith('data/')) or n.endswith('/'):continue
        dst=f'{OUT}/{n}'
        os.makedirs(os.path.dirname(dst),exist_ok=True)
        b=z.read(n)
        if os.path.exists(dst) and n.endswith('.json') and '/tags/' in n:
            a=json.load(open(dst));c=json.loads(b);a['values']=a.get('values',[])+[v for v in c.get('values',[]) if v not in a.get('values',[])];b=json.dumps(a,indent=1).encode()
        open(dst,'wb').write(b)
    for ns in nss:
        models={os.path.basename(p)[:-5] for p in glob.glob(f'{OUT}/assets/{ns}/models/item/*.json')}
        blocks={os.path.basename(p)[:-5] for p in glob.glob(f'{OUT}/assets/{ns}/blockstates/*.json')}
        if not models and not blocks:continue
        manifest[ns]={'blocks':sorted(blocks),'items':sorted(models-blocks)}
        os.makedirs(f'{OUT}/assets/{ns}/items',exist_ok=True)
        for i in sorted(models):
            json.dump({'model':{'type':'minecraft:model','model':f'{ns}:item/{i}'}},open(f'{OUT}/assets/{ns}/items/{i}.json','w'))
json.dump(manifest,open(OUT+'/n2_content.json','w'),indent=1)
print({k:(len(v['blocks']),len(v['items'])) for k,v in manifest.items()})
for w in conv2.warn:print('WARN',w)
