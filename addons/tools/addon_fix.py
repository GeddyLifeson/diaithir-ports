#!/usr/bin/env python3
"""Second-pass signature rewrites (26.1 Block/Item API)."""
import re, sys, pathlib
def imp(s, i):
    n = i.split('.')[-1]
    if re.search(r'\b%s\b' % n, s) and f'import {i};' not in s:
        s = re.sub(r'(package [\w.]+;\n)', rf'\1\nimport {i};', s, count=1)
    return s
R = [
 (r'public void neighborChanged\s*\(\s*BlockState (\w+),\s*Level (\w+),\s*BlockPos (\w+),\s*Block (\w+),\s*BlockPos (\w+),\s*boolean (\w+)\s*\)',
  r'protected void neighborChanged(BlockState \1, Level \2, BlockPos \3, Block \4, @org.jetbrains.annotations.Nullable net.minecraft.world.level.redstone.Orientation orientation, boolean \6)'),
 (r'public int getAnalogOutputSignal\s*\(\s*BlockState (\w+),\s*Level (\w+),\s*BlockPos (\w+)\s*\)', r'protected int getAnalogOutputSignal(BlockState \1, Level \2, BlockPos \3, net.minecraft.core.Direction analogDir)'),
 (r'public int getBurnTime\s*\(\s*ItemStack (\w+),\s*RecipeType<\?> (\w+)\s*\)', r'public int getBurnTime(ItemStack \1, @org.jetbrains.annotations.Nullable RecipeType<?> \2, net.minecraft.world.level.block.entity.FuelValues fuelValues)'),
 (r'(All(?:Blocks|Items))\.(\w+)\.get\(\)', r'\1.\2'),
 (r'(All(?:Blocks|Items))\.(\w+)\.has\((\w+)\)', r'\3.is(\1.\2)'),
 (r'(All(?:Blocks|Items))\.(\w+)\.asStack\(\)', r'new net.minecraft.world.item.ItemStack(\1.\2)'),
 (r'PacketDistributor\.sendToServer\(', r'net.neoforged.neoforge.client.network.ClientPacketDistributor.sendToServer('),
]
for p in pathlib.Path(sys.argv[1]).rglob('*.java'):
    s = p.read_text(); o = s
    for a, b in R: s = re.sub(a, b, s)
    if s != o: p.write_text(s)
