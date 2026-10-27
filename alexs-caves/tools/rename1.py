#!/usr/bin/env python3
"""Mechanical 1.21.1 -> 26.1 renames over src/main/java. Idempotent."""
import re, sys, pathlib
R = [
 (r'net\.minecraft\.resources\.ResourceLocation', 'net.minecraft.resources.Identifier'),
 (r'\bResourceLocation\b', 'Identifier'),
 (r'\bgetMinBuildHeight\(\)', 'getMinY()'),
 (r'\bgetMaxBuildHeight\(\)', 'getMaxY()'),
 (r'net\.minecraft\.world\.entity\.MobSpawnType', 'net.minecraft.world.entity.EntitySpawnReason'),
 (r'\bMobSpawnType\b', 'EntitySpawnReason'),
 (r'net\.minecraft\.world\.item\.UseAnim', 'net.minecraft.world.item.ItemUseAnimation'),
 (r'\bUseAnim\b', 'ItemUseAnimation'),
 (r'\b((?:[a-zA-Z_]*[lL]evel[a-zA-Z_]*|world|level\(\)|this\.level\(\)|this\.level))\.isClientSide\b(?!\()', r'\1.isClientSide()'),
 (r'\b((?:[a-zA-Z_]*[lL]evel[a-zA-Z_]*|world|level\(\)|this\.level\(\)|this\.level))\.random\b(?!\()', r'\1.getRandom()'),
 (r'net\.minecraft\.world\.level\.block\.state\.properties\.DirectionProperty', 'net.minecraft.world.level.block.state.properties.EnumProperty'),
 (r'\bDirectionProperty\b', 'EnumProperty<Direction>'),
]
n=0
for p in pathlib.Path(sys.argv[1]).rglob('*.java'):
    s=p.read_text(); o=s
    for a,b in R: s=re.sub(a,b,s)
    s=s.replace('import net.minecraft.world.level.block.state.properties.EnumProperty<Direction>;','import net.minecraft.world.level.block.state.properties.EnumProperty;')
    if s!=o: p.write_text(s); n+=1
print(n,'files changed')

# ---- pass 2: interaction results
R2 = [
 (r'import net\.minecraft\.world\.(InteractionResultHolder|ItemInteractionResult);\n', 'import net.minecraft.world.InteractionResult;\n'),
 (r'InteractionResultHolder<ItemStack>', 'InteractionResult'),
 (r'ItemInteractionResult\.PASS_TO_DEFAULT_BLOCK_INTERACTION', 'InteractionResult.TRY_WITH_EMPTY_HAND'),
 (r'ItemInteractionResult\.sidedSuccess\([^;]*\)', 'InteractionResult.SUCCESS'),
 (r'\bItemInteractionResult\b', 'InteractionResult'),
 (r'new InteractionResultHolder\(InteractionResult\.PASS, [^;]*\)', 'InteractionResult.PASS'),
 (r'InteractionResultHolder\.sidedSuccess\((?:[^();]|\([^()]*\))*\)', 'InteractionResult.SUCCESS'),
 (r'InteractionResultHolder\.(pass|fail|consume|success)\((?:[^();]|\([^()]*\))*\)', lambda m: 'InteractionResult.'+{'pass':'PASS','fail':'FAIL','consume':'CONSUME','success':'SUCCESS'}[m.group(1)]),
 (r'InteractionResult\.sidedSuccess\((?:[^();]|\([^()]*\))*\)', 'InteractionResult.SUCCESS'),
]
n=0
for p in pathlib.Path(sys.argv[1]).rglob('*.java'):
    s=p.read_text(); o=s
    for a,b in R2: s=re.sub(a,b,s)
    s=re.sub(r'(import net\.minecraft\.world\.InteractionResult;\n)(?=(?:.*\n)*?import net\.minecraft\.world\.InteractionResult;\n)', '', s)
    if s!=o: p.write_text(s); n+=1
print(n,'files changed (pass2)')
