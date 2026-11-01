import json,glob,sys
base='src/main/resources/data/alexscaves/recipe/'
def ing(x):
    if isinstance(x,str): return x
    if isinstance(x,list): return [ing(i) for i in x]
    if 'item' in x: return x['item']
    if 'tag' in x: return '#'+x['tag']
    print('UNK',x); return x
for f in glob.glob(base+'**/*.json',recursive=True):
    d=json.load(open(f)); o=json.dumps(d)
    if 'key' in d: d['key']={k:ing(v) for k,v in d['key'].items()}
    if 'ingredients' in d: d['ingredients']=[ing(v) for v in d['ingredients']]
    for k in ('ingredient','base','template','addition'):
        if k in d: d[k]=ing(d[k])
    if isinstance(d.get('result'),dict) and 'item' in d['result']:
        d['result']['id']=d['result'].pop('item')
    if isinstance(d.get('result'),str) and d['type']!='minecraft:crafting_shaped':
        d['result']={'id':d['result']}
    if json.dumps(d)!=o: json.dump(d,open(f,'w'),indent=2)
