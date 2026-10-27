#!/usr/bin/env python3
"""Apply regex substitutions only on lines javac flagged with a matching error message.
usage: lfix.py LOG  (rules below)"""
import re, sys, collections
RULES = [
 # (error-text regex, line regex, replacement)
 (r'method scheduleTick', r'\b\w+\.scheduleTick\(', 'ticks.scheduleTick('),
 (r'no suitable method found for create\(', r'\.create\(((?:[^(),]|\([^()]*\))+)\)', r'.create(\1, net.minecraft.world.entity.EntitySpawnReason.EVENT)'),
 (r'isClientSide has private', r'\.isClientSide\b(?!\()', '.isClientSide()'),
 (r'random has protected', r'\.random\b(?!\()', '.getRandom()'),
 (r'ChunkPos', r'(\w+)\.x\b(?!\()', r'\1.x()'),
 (r'ChunkPos', r'(\w+)\.z\b(?!\()', r'\1.z()'),
 (r'method contains\(String\)', r'(\w+)\.contains\(("[^"]*")\)', r'com.github.alexmodguy.alexscaves.server.misc.ACProps.has(\1, \2)'),
 (r'spawnAtLocation', r'(\w+(?:\(\))?(?:\.\w+\(\))*)\.spawnAtLocation\((?!\(net)', r'\1.spawnAtLocation((net.minecraft.server.level.ServerLevel) \1.level(), '),
 (r'spawnAtLocation', r'(?<![\w.)])spawnAtLocation\((?!\(net)', 'spawnAtLocation((net.minecraft.server.level.ServerLevel) this.level(), '),
 (r'displayClientMessage', r'\.displayClientMessage\(((?:[^();]|\((?:[^()]|\([^()]*\))*\))+),\s*true\)', r'.sendOverlayMessage(\1)'),
 (r'displayClientMessage', r'\.displayClientMessage\(((?:[^();]|\((?:[^()]|\([^()]*\))*\))+),\s*false\)', r'.sendSystemMessage(\1)'),
 (r'method update in class WalkAnimationState', r'update\(([^,()]+(?:\([^()]*\))?),\s*([^,()]+)\)', r'update(\1, \2, 1.0F)'),
 (r'Reference<SoundEvent> cannot be converted to SoundEvent', r'\.playSound\((SoundEvents\.\w+)', r'.playSound(\1.value()'),
 (r'method location\(\)', r'\.location\(\)', '.identifier()'),
 (r'method getHolder\(ResourceKey', r'\.getHolder\(', '.get('),
 (r'method registryOrThrow', r'\.registryOrThrow\(', '.lookupOrThrow('),
 (r'method registry\(', r'\.registry\((Registries\.\w+)\)\.get\(\)', r'.lookupOrThrow(\1)'),
 (r'Level cannot be converted to ServerLevel', r'(canEntityGrief|mayInteract)\((\w+(?:\(\))?),', r'\1((net.minecraft.server.level.ServerLevel) \2,'),
 (r'no suitable constructor found for ItemParticleOption', r'new ItemParticleOption\(([\w.()]+), (new ItemStack\((?:[^()]|\([^()]*\))*\)|[\w.()]+?)\)(?=[,)])', r'new ItemParticleOption(\1, \2.getItem())'),
 (r'constructor ChunkPos in record', r'new ChunkPos\((\w+)\)', r'ChunkPos.containing(\1)'),
 (r'method getRandom\(\)', r'levelAccessor\.getRandom\(\)', 'levelAccessor.getRandom()'),
]
log=open(sys.argv[1]).read().split('\n')
last=None
edits=collections.defaultdict(list)
for l in log:
    m=re.match(r'(/\S+\.java):(\d+): error: (.*)',l)
    if m:
        edits[m.group(1)].append([int(m.group(2)),m.group(3)]); last=edits[m.group(1)][-1]
    elif l.strip().startswith('symbol:') and last:
        last[1]+=' '+l.strip()
tot=0
for f,lst in edits.items():
    src=open(f).read().split('\n'); ch=False
    for ln,msg in lst:
        for em,lr,rep in RULES:
            if re.search(em,msg):
                new=re.sub(lr,rep,src[ln-1])
                if new!=src[ln-1]: src[ln-1]=new; ch=True; tot+=1
    if ch: open(f,'w').write('\n'.join(src))
print(tot,'lines fixed')
