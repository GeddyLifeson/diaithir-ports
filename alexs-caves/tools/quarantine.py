#!/usr/bin/env python3
"""Fixed-point quarantine: compile, move files that fail into quarantine/ (same relative path), delete failing one-line
registry declarations, repeat until clean.  Records everything in quarantine/REPORT.txt.
usage: quarantine.py [max_iters]"""
import re, subprocess, pathlib, shutil, sys, collections
ROOT=pathlib.Path(__file__).resolve().parent.parent
SRC=ROOT/'src/main/java'; Q=ROOT/'quarantine'
REGISTRIES={'ACBlockRegistry','ACItemRegistry','ACEntityRegistry','ACBlockEntityRegistry','ACEffectRegistry','ACFeatureRegistry','ACStructureRegistry','ACStructurePieceRegistry','ACLootTableRegistry','ACRecipeRegistry','ACMenuRegistry','ACSoundRegistry','ACParticleRegistry','ACCarverRegistry','ACSurfaceRuleConditionRegistry','ACAdvancementTriggerRegistry','ACFluidRegistry','ACPOIRegistry','ACEntityDataRegistry','ACFrogRegistry','ACStructureProcessorRegistry'}
def compile_():
    r=subprocess.run(['bash','/tmp/bld.sh'],capture_output=True,text=True)
    return open('/tmp/ac_build.log').read()
def main():
    iters=int(sys.argv[1]) if len(sys.argv)>1 else 30
    Q.mkdir(exist_ok=True); rep=open(Q/'REPORT.txt','a')
    for it in range(iters):
        log=compile_()
        errs=collections.defaultdict(set)
        for m in re.finditer(r'(/\S+\.java):(\d+): error:',log): errs[m.group(1)].add(int(m.group(2)))
        print('iter',it,'files with errors',len(errs),flush=True)
        if not errs: break
        for f,lines in errs.items():
            p=pathlib.Path(f)
            if not p.exists(): continue
            name=p.stem
            if name in REGISTRIES:
                src=p.read_text().split('\n')
                for ln in sorted(lines,reverse=True):
                    i=ln-1
                    # extend to statement start/end (declaration line may be long single line)
                    line=src[i]
                    if re.match(r'\s*(public|private)?\s*static final',line) and line.rstrip().endswith(';'):
                        rep.write('REGISTRY %s: %s\n'%(name,line.strip()[:200])); del src[i]
                    elif re.match(r'\s*import ',line):
                        del src[i]
                    else:
                        rep.write('REGISTRY-UNHANDLED %s:%d %s\n'%(name,ln,line.strip()[:160]))
                p.write_text('\n'.join(src))
            else:
                rel=p.relative_to(SRC); dest=Q/rel; dest.parent.mkdir(parents=True,exist_ok=True)
                shutil.move(str(p),str(dest)); rep.write('FILE %s\n'%rel)
        rep.flush()
if __name__=='__main__': main()
