#!/usr/bin/env python3
"""Rewrite northstar: block ids in the ported structure NBTs to vanilla stand-ins (create: ids are kept; the pack ships Create).
Usage: remap_northstar.py STRUCTURE_DIR   (edits files in place, gzip NBT). Properties not in a target's allowed list are dropped.
Block-entity data and entities of northstar: blocks/types are dropped. Stdlib only."""
import gzip, os, struct, sys

# northstar id -> (target, allowed property keys)
STAIRS = ('facing', 'half', 'shape', 'waterlogged')
SLAB = ('type', 'waterlogged')
M = {
 'computer_rack': ('minecraft:iron_block', ()), 'extinguished_lantern': ('minecraft:lantern', ('hanging', 'waterlogged')),
 'frost': ('minecraft:snow_block', ()), 'glowing_mars_roots': ('minecraft:hanging_roots', ('waterlogged',)),
 'glowstone_lamp': ('minecraft:glowstone', ()), 'ice_box': ('minecraft:packed_ice', ()),
 'icicle': ('minecraft:pointed_dripstone', ()), 'iron_cogwheel': ('create:cogwheel', ('axis',)),
 'mars_gravel': ('minecraft:gravel', ()), 'mars_roots': ('minecraft:hanging_roots', ('waterlogged',)),
 'mars_soil': ('minecraft:coarse_dirt', ()), 'mars_worm_nest': ('minecraft:nether_wart_block', ()),
 'martian_steel_blue_lamp': ('minecraft:sea_lantern', ()), 'martian_steel_grate': ('minecraft:iron_bars', ('east', 'north', 'south', 'west', 'waterlogged')),
 'martian_steel_large_plating': ('minecraft:polished_blackstone', ()), 'martian_steel_pillar': ('minecraft:polished_basalt', ('axis',)),
 'martian_steel_plating': ('minecraft:smooth_basalt', ()), 'martian_steel_plating_slab': ('minecraft:polished_blackstone_slab', SLAB),
 'martian_steel_plating_stairs': ('minecraft:polished_blackstone_stairs', STAIRS), 'martian_steel_plating_vertical_slab': ('minecraft:polished_blackstone_slab', ('waterlogged',)),
 'martian_steel_sheetmetal': ('minecraft:polished_deepslate', ()), 'martian_tall_grass': ('minecraft:dead_bush', ()),
 'moon_stone_brick_wall': ('minecraft:stone_brick_wall', ('east', 'north', 'south', 'up', 'waterlogged', 'west')),
 'oxygen_filler': ('minecraft:iron_block', ()), 'sulfuric_acid': ('minecraft:water', ('level',)),
 'temperature_regulator': ('minecraft:iron_block', ()), 'titanium_grate': ('minecraft:iron_bars', ('east', 'north', 'south', 'west', 'waterlogged')),
 'titanium_pillar': ('minecraft:quartz_pillar', ('axis',)), 'titanium_plating': ('minecraft:smooth_quartz', ()),
 'titanium_plating_slab': ('minecraft:smooth_quartz_slab', SLAB), 'titanium_plating_stairs': ('minecraft:smooth_quartz_stairs', STAIRS),
 'titanium_plating_vertical_slab': ('minecraft:smooth_quartz_slab', ('waterlogged',)), 'titanium_sheetmetal': ('minecraft:quartz_block', ()),
 'titanium_space_door': ('minecraft:iron_door', ('facing', 'half', 'hinge', 'open', 'powered')), 'vent': ('minecraft:iron_bars', ()),
 'venus_coal_ore': ('minecraft:deepslate_coal_ore', ()), 'venus_gravel': ('minecraft:gravel', ()), 'venus_plume': ('minecraft:magma_block', ()),
 'venus_stone': ('minecraft:blackstone', ()), 'volcanic_ash': ('minecraft:gray_concrete_powder', ()),
}

def rd(f):
    def u(fmt): return struct.unpack(fmt, f.read(struct.calcsize(fmt)))[0]
    def pl(t):
        if t in (1, 2, 3, 4, 5, 6): return (t, u({1: '>b', 2: '>h', 3: '>i', 4: '>q', 5: '>f', 6: '>d'}[t]))
        if t == 7: n = u('>i'); return (t, f.read(n))
        if t == 8: n = u('>H'); return (t, f.read(n).decode('utf8'))
        if t == 9:
            it = u('>b'); n = u('>i'); return (t, (it, [pl(it) for _ in range(n)]))
        if t == 10:
            d = {}
            while True:
                tt = u('>b')
                if tt == 0: return (t, d)
                n = u('>H'); k = f.read(n).decode(); d[k] = pl(tt)
        if t == 11: n = u('>i'); return (t, f.read(4 * n))
        if t == 12: n = u('>i'); return (t, f.read(8 * n))
        raise ValueError(t)
    t = u('>b'); n = u('>H'); name = f.read(n).decode()
    return name, pl(t)

def wr(name, node):
    o = bytearray()
    def s(x): b = x.encode('utf8'); o.extend(struct.pack('>H', len(b)) + b)
    def pl(t, v):
        if t in (1, 2, 3, 4, 5, 6): o.extend(struct.pack({1: '>b', 2: '>h', 3: '>i', 4: '>q', 5: '>f', 6: '>d'}[t], v))
        elif t == 7: o.extend(struct.pack('>i', len(v)) + v)
        elif t == 8: s(v)
        elif t == 9: it, items = v; o.extend(struct.pack('>bi', it, len(items))); [pl(it, x[1]) for x in items]
        elif t == 10:
            for k, (tt, vv) in v.items(): o.extend(struct.pack('>b', tt)); s(k); pl(tt, vv)
            o.append(0)
        elif t == 11: o.extend(struct.pack('>i', len(v) // 4) + v)
        elif t == 12: o.extend(struct.pack('>i', len(v) // 8) + v)
    t, v = node; o.extend(struct.pack('>b', t)); s(name); pl(t, v)
    return bytes(o)

def fix(root):
    d = root[1]
    pal = d['palette'][1][1]
    drop = set()
    for i, e in enumerate(pal):
        ent = e[1]
        ns, _, nm = ent['Name'][1].partition(':')
        if ns != 'northstar': continue
        tgt, allowed = M[nm]
        ent['Name'] = (8, tgt)
        if 'Properties' in ent:
            props = {k: v for k, v in ent['Properties'][1].items() if k in allowed}
            if props: ent['Properties'] = (10, props)
            else: del ent['Properties']
        drop.add(i)
    if 'blocks' in d:
        for b in d['blocks'][1][1]:
            if b[1]['state'][1] in drop and 'nbt' in b[1]: del b[1]['nbt']
    if 'entities' in d:
        it, ents = d['entities'][1]
        ents[:] = [e for e in ents if not e[1].get('nbt', (0, {}))[1].get('id', (8, ''))[1].startswith('northstar:')]
    return len(drop)

n = 0
for root, _, files in os.walk(sys.argv[1]):
    for fn in files:
        if not fn.endswith('.nbt'): continue
        p = os.path.join(root, fn)
        with gzip.open(p) as g: name, tree = rd(g)
        n += fix(tree)
        with gzip.GzipFile(p, 'wb', mtime=0) as g: g.write(wr(name, tree))
print('remapped palette entries:', n)
