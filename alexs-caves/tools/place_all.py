import sys,os,glob; sys.path.insert(0,'tools')
import rcon
bl=sorted(os.path.basename(f)[:-5] for f in glob.glob('src/main/resources/data/alexscaves/loot_table/blocks/*.json'))
it=open('/tmp/items.txt').read().split()
cmds=['forceload add 0 0 15 15']
for i,b in enumerate(bl): cmds.append('setblock %d 100 %d alexscaves:%s'%(i%16,(i//16)*2%32,b))
bad=[]
out=rcon.run(25892,'x',cmds)
for b,o in zip(bl,out[1:]):
    if 'Changed' not in o and 'nothing' not in o.lower(): bad.append((b,o[:60]))
print('blocks',len(bl),'bad',bad)
cmds=['summon item 8 110 8 {Item:{id:"alexscaves:%s",count:1}}'%i for i in it]
out=rcon.run(25892,'x',cmds)
print('items',len(it),[(i,o[:70]) for i,o in zip(it,out) if 'Summoned' not in o])
cmds=['loot spawn 8 120 8 mine %d 100 %d'%(i%16,(i//16)*2%32) for i in range(len(bl))]
out=rcon.run(25892,'x',cmds)
print([ (b,o[:60]) for b,o in zip(bl,out) if 'Dropped' not in o and 'No loot' not in o][:10])
print(rcon.run(25892,'x',['tick sprint 100']))
