import re,glob
exec(open('tools/rename11.py').read().split('def main')[0])
SL='net.minecraft.server.level.ServerLevel'
def solid(t):
    if len(t)==2: return 'isSolidRender()'
def main():
    for f in glob.glob(ROOT+'**/*.java',recursive=True):
        s=open(f).read();o=s
        s=re.sub(r'\n[^\n]*getProfiler\(\)\.(push|pop)\([^\n]*','',s)
        s=re.sub(r'\n\s*if \(dummyExplosion == null\) \{\n[^\n]*new Explosion\([^\n]*\n\s*\}','',s)
        s=re.sub(r'dummyExplosion = new Explosion\([^;]*;','',s)
        s=re.sub(r'blockstate\.onBlockExploded\(level(?:\(\))?, blockpos, dummyExplosion\);','level'+('()' if 'Tremorzilla' in f else '')+'.setBlock(blockpos, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(), 3);',s)
        s=rewrite_calls(s,'isSolidRender',solid)
        s=re.sub(r'((?:this\.)?(?:mob|entity|level)(?:\.level)?\(\)|\bthis\.mob\.level\(\)|\blevel\(\)|\bthis\.level)\.(getNearestEntity|getNearbyPlayers)\(',lambda m:'(('+SL+') '+m.group(1)+').'+m.group(2)+'(',s) if re.search(r'getNearestEntity|getNearbyPlayers',s) else s
        s=s.replace('level().getNearestPlayer(this.targetConditions','((net.minecraft.server.level.ServerLevel) level()).getNearestPlayer(this.targetConditions')
        s=s.replace('this.mob.level().getNearestPlayer(this.targetConditions','((net.minecraft.server.level.ServerLevel) this.mob.level()).getNearestPlayer(this.targetConditions')
        s=s.replace('((ServerLevel) ((net','((net')
        s=re.sub(r'(\w+)\.getCraftingRemainder\(\)\.create\(\)',r'\1.getCraftingRemainder().create()',s)
        s=re.sub(r'if \(!(\w+)\.getCraftingRemainder\(\) == null\)',r'if (\1.getCraftingRemainder() != null)',s)
        s=s.replace('PathType.DANGER_POWDER_SNOW','PathType.POWDER_SNOW')
        s=s.replace('canOpenDoors()','canOpenDoors()')
        s=s.replace('.getArmorSlots()','.getArmorSlots()')
        if s!=o: open(f,'w').write(s)
main()
