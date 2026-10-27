#!/usr/bin/env python3
"""Second client pass: nested entity/block/item rendering through LegacyRenderContext. usage: client_port2.py DIR. Idempotent."""
import re, sys, pathlib
LG='com.github.alexthe666.citadel.client.render.legacy.'
CTX=LG+'LegacyRenderContext'
def brace_end(s,i):
    d=0
    while True:
        c=s[i]
        if c=='{': d+=1
        elif c=='}':
            d-=1
            if d==0: return i
        i+=1
SIG=re.compile(r'(public|private|protected)( static)? <E extends Entity> void (\w+)\(E (\w+), double (\w+), double (\w+), double (\w+), float (\w+), float (\w+), PoseStack (\w+), MultiBufferSource (\w+), int (\w+)\) \{')
for p in pathlib.Path(sys.argv[1]).rglob('*.java'):
    s=p.read_text(); o=s
    while True:
        m=SIG.search(s)
        if not m or CTX in s[m.start():m.start()+400]: break
        e=brace_end(s,m.end()-1)
        g=m.groups()
        body=f'{{\n        {CTX}.renderEntity({g[3]}, {g[4]}, {g[5]}, {g[6]}, {g[7]}, {g[8]}, {g[9]}, {g[11]});\n    }}'
        s=s[:m.end()-1]+body+s[e+1:]
        # avoid infinite loop on same signature: mark by comment-free check above
    s=re.sub(r'(?:Minecraft\.getInstance\(\)|this\.minecraft|minecraft)\.getBlockRenderer\(\)\.renderSingleBlock\(\s*([^,]+?),\s*([^,]+?),\s*[^,]+?,\s*([^,]+?),\s*[^;]*?\);', lambda m: f'{CTX}.renderBlock({m[1]}, {m[2]}, {m[3]});', s)
    s=s.replace('part.getBoundingBoxForCulling()','part.getBoundingBox()')
    s=re.sub(r'new ItemInHandLayer<>\(this, [\w.]+\.getItemInHandRenderer\(\)\)', 'new '+LG+'LegacyItemInHandLayer<>(this)', s)
    s=re.sub(r'EntityRenderer<\? super (\w+)> (\w+) = null;', r'Object \2 = null;', s)
    if s!=o: p.write_text(s)
