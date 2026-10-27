#!/usr/bin/env python3
"""Mechanical 1.21.1 -> 26.1.2 rewrites shared by the addon ports. Usage: port_fix.py DIR"""
import re, sys, pathlib

R = [
 (r'com\.simibubi\.create', 'com.zurrtum.create'),
 (r'net\.createmod\.catnip', 'com.zurrtum.create.catnip'),
 (r'net\.createmod\.ponder', 'com.zurrtum.create.ponder'),
 (r'net\.minecraft\.resources\.ResourceLocation', 'net.minecraft.resources.Identifier'),
 (r'\bResourceLocation\b', 'Identifier'),
 (r'\.getNormal\(\)', '.getUnitVec3i()'),
 (r'ItemInteractionResult', 'InteractionResult'),
 (r'InteractionResult\.PASS_TO_DEFAULT_BLOCK_INTERACTION', 'InteractionResult.TRY_WITH_EMPTY_HAND'),
 (r'InteractionResult\.SKIP_DEFAULT_BLOCK_INTERACTION', 'InteractionResult.PASS'),
 (r'\bDirectionProperty\b', 'EnumProperty<Direction>'),
 (r'import net\.minecraft\.world\.level\.block\.state\.properties\.EnumProperty<Direction>;', 'import net.minecraft.world.level.block.state.properties.EnumProperty;'),
 (r'(\w+)\.isClientSide(?!\w|\()', r'\1.isClientSide()'),
 (r'canPlaceLiquid\(@Nullable Player (\w+)', r'canPlaceLiquid(@Nullable net.minecraft.world.entity.LivingEntity \1'),
 (r'Block (\w+), BlockPos (\w+), boolean movedByPiston', r'Block \1, @Nullable net.minecraft.world.level.redstone.Orientation orientation, boolean movedByPiston'),
 (r'(super\.neighborChanged\([^;]*?), \w*[nN]eighborPos, movedByPiston', r'\1, orientation, movedByPiston'),
 (r'\.placeInWorld\(([^;]*?), ray\)\.result\(\)', r'.placeInWorld(\1)'),
 (r'\.placeInWorld\(([^;]*?), ray\)', r'.placeInWorld(\1)'),
 (r'^.*MethodsReturnNonnullByDefault.*\n', ''),
 (r'^.*NonnullDefault.*\n', ''),
]

SIG = re.compile(r'(?:public |protected )?BlockState updateShape\s*\(\s*BlockState (\w+),\s*Direction (\w+),\s*BlockState (\w+),\s*LevelAccessor (\w+),\s*BlockPos (\w+),\s*BlockPos (\w+)\s*\)')

def fix_update_shape(s):
    def rep(m):
        st, d, ns, lv, pos, npos = m.groups()
        return (f'protected BlockState updateShape(BlockState {st}, LevelReader {lv}, ScheduledTickAccess tickAccess, BlockPos {pos}, '
                f'Direction {d}, BlockPos {npos}, BlockState {ns}, RandomSource random)')
    out = []
    pos = 0
    for m in SIG.finditer(s):
        out.append(s[pos:m.start()])
        st, d, ns, lv, p, npos = m.groups()
        # find body end by brace matching
        i = s.index('{', m.end()); depth = 0; j = i
        while True:
            c = s[j]
            if c == '{': depth += 1
            elif c == '}':
                depth -= 1
                if depth == 0: break
            j += 1
        body = s[i:j+1]
        body = re.sub(rf'super\.updateShape\(\s*{st}\s*,\s*{d}\s*,\s*{ns}\s*,\s*{lv}\s*,\s*{p}\s*,\s*{npos}\s*\)',
                      f'super.updateShape({st}, {lv}, tickAccess, {p}, {d}, {npos}, {ns}, random)', body)
        body = re.sub(rf'updateWater\(\s*{lv}\s*,\s*{st}\s*,\s*{p}\s*\)', f'updateWater({lv}, tickAccess, {st}, {p})', body)
        body = re.sub(rf'{lv}\.scheduleTick\(', 'tickAccess.scheduleTick(', body)
        body = re.sub(rf'!{lv}\.isClientSide\(\)', f'!({lv} instanceof Level lvl_ && lvl_.isClientSide())', body)
        out.append(rep(m) + ' ' + body)
        pos = j + 1
    out.append(s[pos:])
    s = ''.join(out)
    if 'ScheduledTickAccess tickAccess' in s:
        for imp in ['net.minecraft.world.level.ScheduledTickAccess', 'net.minecraft.world.level.LevelReader',
                    'net.minecraft.util.RandomSource', 'net.minecraft.world.level.Level']:
            if f'import {imp};' not in s:
                s = re.sub(r'(package [\w.]+;\n)', rf'\1\nimport {imp};', s, count=1)
    return s

def main(root):
    for p in pathlib.Path(root).rglob('*.java'):
        s = p.read_text()
        o = s
        for a, b in R:
            s = re.sub(a, b, s, flags=re.M)
        s = fix_update_shape(s)
        if s != o:
            p.write_text(s)

main(sys.argv[1])
