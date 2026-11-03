import os,sys,glob,numpy as np
from PIL import Image
def scale2x(a):
    h,w=a.shape[:2]; p=np.pad(a,((1,1),(1,1),(0,0)),mode='edge'); P=p[:-2,1:-1];Q=p[1:-1,2:];R=p[2:,1:-1];L=p[1:-1,:-2];C=a
    eq=lambda x,y:(x==y).all(axis=2)
    out=np.zeros((h*2,w*2,4),a.dtype)
    e0=C.copy(); m=eq(L,P)&~eq(L,R)&~eq(P,Q); e0[m]=P[m]
    e1=C.copy(); m=eq(P,Q)&~eq(P,L)&~eq(Q,R); e1[m]=Q[m]
    e2=C.copy(); m=eq(L,R)&~eq(L,P)&~eq(R,Q); e2[m]=L[m]
    e3=C.copy(); m=eq(R,Q)&~eq(R,L)&~eq(Q,P); e3[m]=R[m]
    out[0::2,0::2]=e0; out[0::2,1::2]=e1; out[1::2,0::2]=e2; out[1::2,1::2]=e3; return out
src,dst=sys.argv[1],sys.argv[2]; n=up=keep=0
for f in glob.glob(src+'/**/*.png',recursive=True):
    rel=os.path.relpath(f,src); im=Image.open(f).convert('RGBA'); a=np.array(im); w=im.width
    skip=rel.startswith('textures/entity') or '/painting/' in rel or w>=64 or w not in(16,32,18,19) and im.height%16!=0
    if rel.startswith('textures/gui/') and w>=64: skip=True
    if not skip:
        k={16:2,32:1,18:1,19:1}.get(w,0)
        if k:
            for _ in range(k): a=scale2x(a)
            im=Image.fromarray(a); up+=1
        else: keep+=1
    else: keep+=1
    o=os.path.join(dst,rel); os.makedirs(os.path.dirname(o),exist_ok=True); im.save(o)
    m=f+'.mcmeta'
    if os.path.exists(m): import shutil; shutil.copy(m,o+'.mcmeta')
print('upscaled',up,'kept',keep)
