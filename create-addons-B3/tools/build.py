import json,zipfile
o=json.load(open('pk/recipes.json'))
o={k:v for k,v in o.items() if 'enhanced_diamonds' not in k and 'charcoal_block_to_diamond' not in k}
toml=open('pk/toml').read()
with zipfile.ZipFile('pk/diaithir-create-addons-b3-26.1.2-0.1.0.jar','w',zipfile.ZIP_DEFLATED) as z:
    z.writestr('META-INF/neoforge.mods.toml',toml)
    z.writestr('pack.mcmeta','{"pack":{"description":"b3","min_format":84,"max_format":99}}')
    for k,v in o.items(): z.writestr(k,v)
print(len(o))
