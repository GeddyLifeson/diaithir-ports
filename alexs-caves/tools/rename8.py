#!/usr/bin/env python3
"""Block entity / item stack storage conversions."""
import re, pathlib, sys
R=[
 (r'ItemStack\.parseOptional\(registries,\s*(\w+)\.getCompoundOrEmpty\(("[^"]*")\)\)', r'\1.read(\2, ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY)'),
 (r'(\w+)\.put\(("[^"]*"),\s*([\w.()]+)\.saveOptional\(registries\)\)', r'\1.store(\2, ItemStack.OPTIONAL_CODEC, \3)'),
 (r'ContainerHelper\.(saveAllItems|loadAllItems)\(([^;]*?),\s*registries\)', r'ContainerHelper.\1(\2)'),
 (r'int\[\] (\w+)', r'int[] \1'),
 (r'protected boolean isOwnContainer\(Player (\w+)\)', r'protected boolean isOwnContainer(Player \1)'),
 (r'incrementOpeners\((\w+), this\.getLevel\(\), this\.getBlockPos\(\), this\.getBlockState\(\)\)', r'incrementOpeners(\1, this.getLevel(), this.getBlockPos(), this.getBlockState(), \1.blockInteractionRange())'),
 (r'decrementOpeners\((\w+), this\.getLevel\(\), this\.getBlockPos\(\), this\.getBlockState\(\)\)', r'decrementOpeners(\1, this.getLevel(), this.getBlockPos(), this.getBlockState())'),
 (r'\.(\w+)\.moveTo\(', r'.\1.snapTo('),
 (r'(?<![\w.])moveTo\(([^;]*?),\s*([^,;]*?),\s*([^,;]*?),\s*([^,;]*?),\s*([^,;]*?)\);', None),
]
n=0
for p in pathlib.Path(sys.argv[1]).rglob('*.java'):
    s=p.read_text(); o=s
    for a,b in R:
        if b is None: continue
        s=re.sub(a,b,s)
    s=re.sub(r'\.moveTo\(((?:[^();]|\((?:[^()]|\([^()]*\))*\))+)\);',lambda m: '.snapTo(%s);'%m.group(1) if m.group(1).count(',')>=2 else m.group(0),s)
    if s!=o: p.write_text(s); n+=1
print(n,'files')
