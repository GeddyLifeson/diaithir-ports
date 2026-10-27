#!/usr/bin/env python3
"""BlockEntity serialisation idioms: 1.21.1 CompoundTag -> 26.1 ValueInput/ValueOutput (variable name `tag` kept)."""
import re, sys, pathlib
R = [
 (r'read\(CompoundTag (\w+), HolderLookup\.Provider \w+, boolean clientPacket\)', r'read(ValueInput \1, boolean clientPacket)'),
 (r'write\(CompoundTag (\w+), HolderLookup\.Provider \w+, boolean clientPacket\)', r'write(ValueOutput \1, boolean clientPacket)'),
 (r'writeSafe\(CompoundTag (\w+), HolderLookup\.Provider \w+\)', r'writeSafe(ValueOutput \1)'),
 (r'super\.read\((\w+), \w+, clientPacket\)', r'super.read(\1, clientPacket)'),
 (r'super\.write\((\w+), \w+, clientPacket\)', r'super.write(\1, clientPacket)'),
 (r'super\.writeSafe\((\w+), \w+\)', r'super.writeSafe(\1)'),
 (r'\b(tag|nbt|compound)\.getInt\(("[^"]*"|[\w+."]+)\)', r'\1.getIntOr(\2, 0)'),
 (r'\b(tag|nbt|compound)\.getBoolean\(("[^"]*"|[\w+."]+)\)', r'\1.getBooleanOr(\2, false)'),
 (r'\b(tag|nbt|compound)\.getFloat\(("[^"]*"|[\w+."]+)\)', r'\1.getFloatOr(\2, 0f)'),
 (r'\b(tag|nbt|compound)\.getLong\(("[^"]*"|[\w+."]+)\)', r'\1.getLongOr(\2, 0L)'),
 (r'\b(tag|nbt|compound)\.getDouble\(("[^"]*"|[\w+."]+)\)', r'\1.getDoubleOr(\2, 0d)'),
 (r'\b(tag|nbt|compound)\.getString\(("[^"]*"|[\w+."]+)\)', r'\1.getStringOr(\2, "")'),
]
IMPS = ['net.minecraft.world.level.storage.ValueInput', 'net.minecraft.world.level.storage.ValueOutput']
for p in pathlib.Path(sys.argv[1]).rglob('*.java'):
    s = p.read_text(); o = s
    for a, b in R: s = re.sub(a, b, s)
    if s != o:
        for i in IMPS:
            if i.split('.')[-1] in s and f'import {i};' not in s:
                s = re.sub(r'(package [\w.]+;\n)', rf'\1\nimport {i};', s, count=1)
        p.write_text(s)
