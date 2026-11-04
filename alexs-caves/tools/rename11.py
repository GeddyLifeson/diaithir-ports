import re,glob,sys
ROOT='src/main/java/com/github/alexmodguy/alexscaves/server/'
def split_args(s,i):
    """s[i] is just after '(' ; return (list of arg spans, end index of ')')"""
    d=0;a=i;args=[];j=i;instr=False
    while j<len(s):
        c=s[j]
        if instr:
            if c=='\\': j+=1
            elif c=='"': instr=False
        elif c=='"': instr=True
        elif c in '([{': d+=1
        elif c in ')]}':
            if d==0:
                args.append((a,j)); return args,j
            d-=1
        elif c==',' and d==0:
            args.append((a,j)); a=j+1
        j+=1
    return None,None
def rewrite_calls(s,name,fn):
    out=[];pos=0
    for m in re.finditer(r'\b'+re.escape(name)+r'\(',s):
        if m.start()<pos: continue
        args,end=split_args(s,m.end())
        if args is None: continue
        texts=[s[a:b].strip() for a,b in args]
        r=fn(texts)
        if r is None: continue
        out.append(s[pos:m.start()]);out.append(r);pos=end+1
    out.append(s[pos:]);return ''.join(out)
def addpart(t):
    if len(t)==8 and t[1] in('true','false'):
        return 'addParticle(%s, %s, false, %s)'%(t[0],t[1],', '.join(t[2:]))
def main():
    for f in glob.glob(ROOT+'**/*.java',recursive=True):
        s=open(f).read();o=s
        s=s.replace('EntityDataSerializers.OPTIONAL_UUID','com.github.alexmodguy.alexscaves.server.misc.ACDataSerializers.OPTIONAL_UUID')
        s=re.sub(r'(lerpMotion\((?:net\.minecraft\.world\.phys\.)?Vec3 (\w+)\)\s*\{[^}]*?)\n',lambda m:m.group(0),s)
        # lerpMotion bodies
        def lm(m):
            body=m.group(0);v=m.group(2)
            body=re.sub(r'\blerpX\b',v+'.x',body);body=re.sub(r'\blerpY\b',v+'.y',body);body=re.sub(r'\blerpZ\b',v+'.z',body)
            return body
        s=re.sub(r'void lerpMotion\((?:net\.minecraft\.world\.phys\.)?Vec3 (\w+)\)\s*\{.*?\n    \}',lambda m:re.sub(r'\blerp([XYZ])\b',lambda k:m.group(1)+'.'+k.group(1).lower(),m.group(0)),s,flags=re.S)
        s=re.sub(r'(\b[\w.()]+?)\.getGameRules\(\)\.getBoolean\(GameRules\.RULE_DOMOBLOOT\)',r'com.github.alexmodguy.alexscaves.server.misc.ACProps.rule(\1, GameRules.MOB_DROPS)',s)
        s=re.sub(r'(\b[\w.()]+?)\.getGameRules\(\)\.getBoolean\(GameRules\.RULE_MOBGRIEFING\)',r'com.github.alexmodguy.alexscaves.server.misc.ACProps.rule(\1, GameRules.MOB_GRIEFING)',s)
        s=re.sub(r'(\b[\w.()]+?)\.getGameRules\(\)\.getBoolean\(GameRules\.RULE_MOB_EXPLOSION_DROP_DECAY\)',r'com.github.alexmodguy.alexscaves.server.misc.ACProps.rule(\1, GameRules.MOB_EXPLOSION_DROP_DECAY)',s)
        s=s.replace('net.neoforged.neoforge.event.EventHooks.canEntityGrief(this.level(), this)','com.github.alexmodguy.alexscaves.server.misc.ACProps.canGrief(this.level(), this)')
        s=re.sub(r'net\.neoforged\.neoforge\.event\.EventHooks\.canEntityGrief\((\w+)\.level\(\), (\w+)\)',r'com.github.alexmodguy.alexscaves.server.misc.ACProps.canGrief(\1.level(), \2)',s)
        s=re.sub(r'\.moveTo\((Vec3\.atCenterOf\()',r'.snapTo(\1',s)
        s=re.sub(r'(\w+)\.hasImpulse = true',r'\1.hurtMarked = true',s)
        s=s.replace('this.hasImpulse = true','this.hurtMarked = true')
        s=s.replace('PathType.DAMAGE_FIRE','PathType.DAMAGING_IN_NEIGHBOR').replace('PathType.DAMAGE_OTHER','PathType.DAMAGING_IN_NEIGHBOR').replace('PathType.DANGER_OTHER','PathType.DAMAGING_IN_NEIGHBOR')
        s=s.replace('AdvancedPathNavigate.MovementType','AdvancedPathNavigateNoTeleport.MovementType')
        s=rewrite_calls(s,'addParticle',addpart)
        s=s.replace('.setCanPassDoors(true)','.setCanOpenDoors(true)')
        s=s.replace('ChunkPos.asLong(','ChunkPos.pack(')
        s=re.sub(r'\.isSolidRender\([^;]*?\)\s*&&',lambda m:'.isSolidRender() &&',s)
        s=re.sub(r'import com\.github\.alexthe666\.citadel\.server\.entity\.pathfinding\.raycoms\.(AdvancedPathNavigate|PathingStuckHandler);\n','',s)
        s=re.sub(r'(\bgetScale\(\) \{)',r'getAgeScale() {',s) if re.search(r'/(Atlatitan|Tremorzilla|Relicheirus|Tremorsaurus)Entity\.java$',f) else s
        if s!=o: open(f,'w').write(s)
main()
