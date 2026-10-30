import sys; sys.path.insert(0,'tools')
import rcon,re,glob
ents=re.findall(r'DEF_REG\.register\("([a-z_0-9]*)"',open(glob.glob('src/main/java/**/ACEntityRegistry.java',recursive=True)[0]).read())
out=rcon.run(25892,'x',['forceload add 0 0','gamerule spawn_mobs false']+['summon alexscaves:%s 0 100 0'%e for e in ents])
print(len(ents),[e for e,o in zip(ents,out[2:]) if 'Summoned' not in o])
print(rcon.run(25892,'x',['tick sprint 400','data get entity @e[limit=1,type=alexscaves:tremorzilla] Health']))
