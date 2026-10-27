import sys; sys.path.insert(0,'tools')
import rcon,collections
bs=['primordial_caves','forlorn_hollows','magnetic_caves','toxic_caves','candy_cavity','abyssal_chasm']
N=int(sys.argv[1]) if len(sys.argv)>1 else 192
rcon.run(25892,'x',['forceload add 0 0 %d %d'%(N,N),'tick sprint 300'])
cmds=[];keys=[]
for x in range(0,N,8):
  for z in range(0,N,8):
    for y in range(-44,30,8):
      for b in bs: cmds.append('execute if biome %d %d %d alexscaves:%s'%(x,y,z,b)); keys.append(b)
c=collections.Counter()
for k,o in zip(keys,rcon.run(25892,'x',cmds)):
    if 'passed' in o: c[k]+=1
print(len(cmds),c)
