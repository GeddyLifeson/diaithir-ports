#!/usr/bin/env python3
"""NBT getter/putter conversion for 26.1 (Optional getters, ValueInput/ValueOutput). Idempotent."""
import re, sys, pathlib
A=r'("[^"]*"|[A-Z][A-Z0-9_]*)'
R=[
 (r'\.getBoolean\('+A+r'\)', r'.getBooleanOr(\1, false)'),
 (r'\.getInt\('+A+r'\)', r'.getIntOr(\1, 0)'),
 (r'\.getLong\('+A+r'\)', r'.getLongOr(\1, 0L)'),
 (r'\.getFloat\('+A+r'\)', r'.getFloatOr(\1, 0F)'),
 (r'\.getDouble\('+A+r'\)', r'.getDoubleOr(\1, 0D)'),
 (r'\.getString\('+A+r'\)', r'.getStringOr(\1, "")'),
 (r'\.getByte\('+A+r'\)', r'.getByteOr(\1, (byte) 0)'),
 (r'\.getShort\('+A+r'\)', r'.getShortOr(\1, (short) 0)'),
 (r'\.getCompound\('+A+r'\)', r'.getCompoundOrEmpty(\1)'),
 (r'\.getList\('+A+r',\s*\d+\)', r'.getListOrEmpty(\1)'),
 (r'\.getIntArray\('+A+r'\)', r'.getIntArray(\1).orElse(new int[0])'),
 (r'\.getUUID\('+A+r'\)', r'.read(\1, net.minecraft.core.UUIDUtil.CODEC).orElse(null)'),
 (r'\.putUUID\('+A+r',', r'.store(\1, net.minecraft.core.UUIDUtil.CODEC,'),
 (r'\.hasUUID\('+A+r'\)', r'.contains(\1)'),
 (r'\.getCompound\((\w+)\)', r'.getCompoundOrEmpty(\1)'),
 (r'\bisInWaterOrBubble\(\)', 'isInWater()'),
 (r'\bnoCollission\(\)', 'noCollision()'),
 (r'\.getGameRules\(\)\.getBoolean\(GameRules\.RULE_DOENTITYDROPS\)', '.getGameRules().get(net.minecraft.world.level.gamerules.GameRules.ENTITY_DROPS)'),
]
n=0
for p in pathlib.Path(sys.argv[1]).rglob('*.java'):
    s=p.read_text(); o=s
    for a,b in R: s=re.sub(a,b,s)
    if s!=o: p.write_text(s); n+=1
print(n,'files changed')
