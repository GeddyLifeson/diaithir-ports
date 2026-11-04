#!/usr/bin/env python3
"""Block/entity method signature conversions (entityInside, fallOn, hurt)."""
import re, pathlib, sys
RCV=r'((?:this\.)?\w+(?:\(\))?(?:\.\w+\(\))*)'
R=[
 (r'void entityInside\(BlockState (\w+), Level (\w+), BlockPos (\w+), Entity (\w+)\)',
  r'void entityInside(BlockState \1, Level \2, BlockPos \3, Entity \4, net.minecraft.world.entity.InsideBlockEffectApplier effectApplier, boolean isPrecise)'),
 (r'super\.entityInside\((\w+), (\w+), (\w+), (\w+)\)', r'super.entityInside(\1, \2, \3, \4, effectApplier, isPrecise)'),
 (r'void fallOn\(Level (\w+), BlockState (\w+), BlockPos (\w+), Entity (\w+), float (\w+)\)', r'void fallOn(Level \1, BlockState \2, BlockPos \3, Entity \4, double \5)'),
 (r'(?<![\w.])'+RCV+r'\.hurt\((?!\s*\))', r'\1.hurtServer((net.minecraft.server.level.ServerLevel) \1.level(), '),
 (r'(?<![\w.])hurt\((?=[A-Za-z])', 'hurtServer((net.minecraft.server.level.ServerLevel) this.level(), '),
 (r'isOwnContainer\(Player', 'isOwnContainer(Player'),
 (r'protected boolean isOwnContainer', 'public boolean isOwnContainer'),
]
n=0
for p in pathlib.Path(sys.argv[1]).rglob('*.java'):
    s=p.read_text(); o=s
    for a,b in R: s=re.sub(a,b,s)
    if s!=o: p.write_text(s); n+=1
print(n,'files')
