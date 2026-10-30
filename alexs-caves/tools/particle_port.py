#!/usr/bin/env python3
"""quarantine/client/particle -> src particle package (26.1 SingleQuadParticle / legacy bridge). Idempotent per run (re-copies)."""
import re, pathlib, shutil, subprocess, sys
Q=pathlib.Path('quarantine/client/particle'); D=pathlib.Path('src/main/java/com/github/alexmodguy/alexscaves/client/particle')
KEEP={'ACParticleRegistry.java','ACSpriteParticle.java','LegacyParticle.java'}
LG='com.github.alexthe666.citadel.client.render.legacy.'
for f in Q.glob('*.java'):
    if f.name in KEEP: continue
    shutil.copy(f, D/f.name)
subprocess.run([sys.executable,'tools/client_port.py',str(D)],check=True)
def rm_method(s, sig_re):
    # remove optional @Override + method whose header matches sig_re, balanced braces
    out='';pos=0
    for m in re.finditer(r'(?:[ \t]*@Override\n)?[ \t]*'+sig_re, s):
        if m.start()<pos: continue
        i=s.index('{',m.end()-1); d=0; j=i
        while True:
            if s[j]=='{': d+=1
            elif s[j]=='}':
                d-=1
                if d==0: break
            j+=1
        out+=s[pos:m.start()]; pos=j+1
        if s[pos:pos+1]=='\n': pos+=1
    return out+s[pos:]
for p in D.glob('*.java'):
    if p.name in KEEP: continue
    s=p.read_text()
    s=re.sub(r'import net\.neoforged\.api\.distmarker\.(Dist|OnlyIn);\n','',s)
    s=re.sub(r'[ \t]*@OnlyIn\([^)]*\)\n','',s)
    has_render=bool(re.search(r'void render\(VertexConsumer',s))
    # base classes
    if re.search(r'extends TextureSheetParticle',s):
        s=s.replace('extends TextureSheetParticle','extends LegacyParticle' if has_render else 'extends ACSpriteParticle')
    elif re.search(r'class \w+ extends Particle\b',s):
        s=re.sub(r'(class \w+ extends )Particle\b',r'\1LegacyParticle',s)
    if has_render and 'AbstractTrailParticle' not in s and 'LegacyParticle' not in s:
        print('?? render but no legacy base',p.name)
    s=rm_method(s, r'public ParticleRenderType getRenderType\(\)')
    s=rm_method(s, r'protected ParticleRenderType getRenderType\(\)')
    s=re.sub(r'(createParticle\([^)]*?double \w+)\)', lambda m: m.group(1)+', net.minecraft.util.RandomSource randomSource)', s)
    s=re.sub(r'MultiBufferSource\.BufferSource ([\w$]+) = Minecraft\.getInstance\(\)\.renderBuffers\(\)\.bufferSource\(\);', r'MultiBufferSource \1 = '+LG+'LegacyBuffers.source();', s)
    s=re.sub(r'MultiBufferSource\.BufferSource ', 'MultiBufferSource ', s)
    s=re.sub(r'[ \t]*[\w$]+\.endBatch\([^)]*\);\n','',s)
    s=re.sub(r'Minecraft\.getInstance\(\)\.renderBuffers\(\)\.bufferSource\(\)', LG+'LegacyBuffers.source()', s)
    s=re.sub(r'\.setOverlay\(NO_OVERLAY\)', '.setOverlay(NO_OVERLAY)', s)
    s=s.replace('import net.minecraft.util.FastColor;','import net.minecraft.util.ARGB;').replace('FastColor.ARGB32.','ARGB.')
    s=re.sub(r'\.getPosition\(\)', '.position()', s)
    s=re.sub(r'camera\.getXRot\(\)', 'camera.xRot()', s)
    s=re.sub(r'camera\.getYRot\(\)', 'camera.yRot()', s)
    s=re.sub(r'\b(world|level|worldIn|clientLevel)\.random\b', r'\1.getRandom()', s)
    s=re.sub(r'RenderTypes?\.itemEntityTranslucentCull\(', 'RenderTypes.entityTranslucent(', s)
    s=re.sub(r'ACRenderTypes\.itemEntityTranslucentCull\(', 'ACRenderTypes.itemEntityTranslucentCull(', s)
    p.write_text(s)
