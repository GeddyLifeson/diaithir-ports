#!/usr/bin/env python3
"""Third client pass: hand-item layers, dropped batch renderers. usage: client_port3.py DIR. Idempotent."""
import re, sys, pathlib
LG='com.github.alexthe666.citadel.client.render.legacy.'
def brace_end(s,i):
    d=0
    while True:
        c=s[i]
        if c=='{': d+=1
        elif c=='}':
            d-=1
            if d==0: return i
        i+=1
for p in pathlib.Path(sys.argv[1]).rglob('*.java'):
    s=p.read_text(); o=s
    if 'extends ItemInHandLayer<' in s:
        s=s.replace('extends ItemInHandLayer<','extends '+LG+'LegacyItemInHandLayer<')
        s=re.sub(r'new ItemLayer\(\w+\.getItemInHandRenderer\(\)\)','new ItemLayer()',s)
        s=re.sub(r'private (\w+)\(ItemInHandRenderer itemInHandRenderer\) \{\s*super\((\w+)\.this, itemInHandRenderer\);\s*this\.\w+ = itemInHandRenderer;\s*\}', r'private \1() {\n            super(\2.this);\n        }', s)
        s=re.sub(r'\n\s*private final ItemInHandRenderer \w+;','',s)
        s=re.sub(r'protected void renderArmWithItem\(LivingEntity (\w+), ItemStack (\w+), ItemDisplayContext (\w+), HumanoidArm (\w+), PoseStack (\w+), MultiBufferSource \w+, int (\w+)\)', r'protected void renderArmWithItem(ENT \1, ItemStack \2, ItemDisplayContext \3, HumanoidArm \4, PoseStack \5, int \6)', s)
        m=re.search(r'extends '+re.escape(LG)+r'LegacyItemInHandLayer<(\w+),',s)
        if m: s=s.replace('(ENT ','(%s ' % m[1])
        s=re.sub(r'this\.\w+\.renderItem\((\w+), (\w+), (\w+), \w+, (\w+), \w+, (\w+)\)', LG+r'LegacyRenderContext.renderItem(\1, \2, \3, \4, \5)', s)
    # drop static batch renderers that need LevelRenderer hooks
    m=re.search(r'public static void renderEntireBatch\([^)]*\) \{', s)
    if m:
        e=brace_end(s,m.end()-1); s=s[:m.start()]+s[e+1:]
    if s!=o: p.write_text(s)
