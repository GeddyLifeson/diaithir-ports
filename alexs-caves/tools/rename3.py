#!/usr/bin/env python3
"""Entity / block entity / item signature conversions for 26.1."""
import re, sys, pathlib
R=[
 (r'(void\s+(?:addAdditionalSaveData|saveAdditional))\(CompoundTag (\w+)(?:,\s*HolderLookup\.Provider \w+)?\)', r'\1(net.minecraft.world.level.storage.ValueOutput \2)'),
 (r'(void\s+(?:readAdditionalSaveData|loadAdditional))\(CompoundTag (\w+)(?:,\s*HolderLookup\.Provider \w+)?\)', r'\1(net.minecraft.world.level.storage.ValueInput \2)'),
 (r'super\.(saveAdditional|loadAdditional)\((\w+),\s*\w+\)', r'super.\1(\2)'),
 # hurt -> hurtServer
 (r'boolean hurt\(DamageSource (\w+), float (\w+)\)', r'boolean hurtServer(net.minecraft.server.level.ServerLevel serverLevel, DamageSource \1, float \2)'),
 (r'super\.hurt\((\w+),\s*(\w+)\)', r'super.hurtServer(serverLevel, \1, \2)'),
 (r'BlockState (\w+),\s*Direction (\w+),\s*BlockState (\w+),\s*LevelAccessor (\w+),\s*BlockPos (\w+),\s*BlockPos (\w+)\) \{', None),
]
n=0
for p in pathlib.Path(sys.argv[1]).rglob('*.java'):
    s=p.read_text(); o=s
    for a,b in R:
        if b is None: continue
        s=re.sub(a,b,s)
    if s!=o: p.write_text(s); n+=1
print(n,'files changed')

# updateShape
SIG=re.compile(r'(BlockState updateShape\()BlockState (\w+),\s*Direction (\w+),\s*BlockState (\w+),\s*LevelAccessor (\w+),\s*BlockPos (\w+),\s*BlockPos (\w+)\)')
CALL=re.compile(r'super\.updateShape\((\w+),\s*(\w+),\s*(\w+),\s*(\w+),\s*(\w+),\s*(\w+)\)')
n=0
for p in pathlib.Path(sys.argv[1]).rglob('*.java'):
    s=p.read_text(); o=s
    s=SIG.sub(lambda m:'%sBlockState %s, net.minecraft.world.level.LevelReader %s, net.minecraft.world.level.ScheduledTickAccess ticks, BlockPos %s, Direction %s, BlockPos %s, BlockState %s, net.minecraft.util.RandomSource randomSource)'%(m.group(1),m.group(2),m.group(5),m.group(6),m.group(3),m.group(7),m.group(4)),s)
    s=CALL.sub(lambda m:'super.updateShape(%s, %s, ticks, %s, %s, %s, %s, randomSource)'%(m.group(1),m.group(4),m.group(5),m.group(2),m.group(6),m.group(3)),s)
    if s!=o: p.write_text(s); n+=1
print(n,'files changed (updateShape)')
