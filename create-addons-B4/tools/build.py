import sys,json,zipfile,os,re,shutil
sys.path.insert(0,'/home/user/work-B4/tools')
import fix
keep,drop=fix.run(None)
out='/home/user/work-B4/pack/src';shutil.rmtree('/home/user/work-B4/pack',ignore_errors=True)
def w(path,data):
    p=os.path.join(out,path);os.makedirs(os.path.dirname(p),exist_ok=True)
    open(p,'wb' if isinstance(data,bytes) else 'w').write(data)
ports=[]
for k,l in keep.items():
    for path,j in l:
        w(path,json.dumps(j,indent=1));
    if l: ports.append((k,len(l)))
z=zipfile.ZipFile('jars/create-expanded-potato-cannon.jar')
for n in z.namelist():
    if n.startswith('data/create/create/potato_projectile/') and n.endswith('.json'):
        w(n.replace('data/create/create/','data/create/',1),z.read(n))
ports.append(('create-expanded-potato-cannon','projectiles'))
z=zipfile.ZipFile('jars/createstructures.jar')
for n in z.namelist():
    if n.startswith('data/') and not n.endswith('/'): w(n,z.read(n))
ports.append(('createstructures','worldgen'))
w('pack.mcmeta',json.dumps({"pack":{"description":"Diaithir Create addons B4","min_format":84,"max_format":84,"pack_format":84}}))
w('META-INF/neoforge.mods.toml','''modLoader="javafml"
loaderVersion="[1,)"
license="MIT"
[[mods]]
modId="diaithir_create_addons_b4"
version="0.1.0-diaithir.1"
displayName="Diaithir Create Addons (slice B4)"
authors="Recipe/data from MIT addons: Catworks, Create Factory Processing, Create Melodies and Mechanisms, Create Sandpaper Overhaul, Create Expanded Potato Cannon, Create Structures"
description=\'\'\'Data from small MIT Create addons, rewritten for the Diaithir Create port.\'\'\'
[[dependencies.diaithir_create_addons_b4]]
modId="diaithir_create"
type="required"
versionRange="[6.0.9,)"
ordering="AFTER"
side="BOTH"
''')
jar='/home/user/work-B4/diaithir-create-addons-b4-26.1.2-0.1.0.jar'
with zipfile.ZipFile(jar,'w',zipfile.ZIP_DEFLATED) as o:
    for d,_,fs in os.walk(out):
        for f in fs:
            p=os.path.join(d,f);o.write(p,os.path.relpath(p,out))
print(ports,jar)
