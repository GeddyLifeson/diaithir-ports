#!/usr/bin/env python3
import re, pathlib, sys
BO='net.minecraft.world.level.redstone.Orientation'
R=[
 (r'\.getType\(\)\.is\(', '.getType().builtInRegistryHolder().is('),
 (r'\.getOffset\((\w+),\s*(\w+)\)', r'.getOffset(\2)'),
 (r'MapCodec<\? extends BushBlock> codec\(\)', 'MapCodec<BushBlock> codec()'),
 (r'FastColor\.ARGB32\.', 'net.minecraft.util.ARGB.'),
 (r'\.(hasPostProcess|emissiveRendering)\(\([^)]*\) -> (?:true|false)\)', ''),
 (r'PathType\.DANGER_FIRE', 'PathType.FIRE'),
 (r'\.getNormal\(\)', '.getUnitVec3i()'),
 (r'canPlaceLiquid\(BlockGetter ', r'canPlaceLiquid(@javax.annotation.Nullable net.minecraft.world.entity.LivingEntity user, BlockGetter '),
 (r'pickupBlock\(@Nullable Player (\w+),', r'pickupBlock(@Nullable net.minecraft.world.entity.LivingEntity \1,'),
 (r'pickupBlock\(Player (\w+),', r'pickupBlock(@Nullable net.minecraft.world.entity.LivingEntity \1,'),
 (r'void neighborChanged\(BlockState (\w+), Level (\w+), BlockPos (\w+), Block (\w+), BlockPos (\w+), boolean (\w+)\)',
  r'void neighborChanged(BlockState \1, Level \2, BlockPos \3, Block \4, @javax.annotation.Nullable '+BO+r' \5, boolean \6)'),
 (r'canPlaceLiquid\((?:@[\w.]+ )?Player (\w+),', r'canPlaceLiquid(@javax.annotation.Nullable net.minecraft.world.entity.LivingEntity \1,'),
 (r'pickupBlock\((?:@[\w.]+ )?Player (\w+),', r'pickupBlock(@javax.annotation.Nullable net.minecraft.world.entity.LivingEntity \1,'),
 (r'(MapCodec<BushBlock> codec\(\) \{\s*return) CODEC;', r'\1 (MapCodec) CODEC;'),
 (r'protected MapCodec<BushBlock> codec', 'public MapCodec<BushBlock> codec'),
]
n=0
for p in pathlib.Path(sys.argv[1]).rglob('*.java'):
    s=p.read_text(); o=s
    for a,b in R: s=re.sub(a,b,s)
    if s!=o: p.write_text(s); n+=1
print(n,'files')
