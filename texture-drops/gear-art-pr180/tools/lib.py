import math
from PIL import Image, ImageDraw, ImageFilter
SS=4; N=64
class Layer:
    def __init__(s): s.im=Image.new('LA',(N*SS,N*SS),(0,0)); s.d=ImageDraw.Draw(s.im)
    def poly(s,pts,g=200): s.d.polygon([(x*SS,y*SS) for x,y in pts],fill=(g,255))
    def line(s,a,b,w=1,g=200): s.d.line([(a[0]*SS,a[1]*SS),(b[0]*SS,b[1]*SS)],fill=(g,255),width=max(1,int(w*SS)))
    def ell(s,cx,cy,rx,ry,g=200): s.d.ellipse([(cx-rx)*SS,(cy-ry)*SS,(cx+rx)*SS,(cy+ry)*SS],fill=(g,255))
    def rect(s,x0,y0,x1,y1,g=200): s.d.rectangle([x0*SS,y0*SS,x1*SS,y1*SS],fill=(g,255))
    def out(s,path,outline=70):
        a=s.im.resize((N,N),Image.BOX); L,A=a.split()
        A=A.point(lambda v:255 if v>110 else 0); L=L.point(lambda v:v)
        # edge darkening: pixels with a transparent 4-neighbour get outline grey
        px=A.load(); lp=L.load(); o=Image.new('LA',(N,N),(0,0)); op=o.load()
        for y in range(N):
            for x in range(N):
                if px[x,y]:
                    edge=any(not(0<=x+dx<N and 0<=y+dy<N) or not px[x+dx,y+dy] for dx,dy in((1,0),(-1,0),(0,1),(0,-1)))
                    op[x,y]=(outline if edge else max(60,min(255,lp[x,y])),255)
        o.convert('RGBA').save(path)
        return sum(1 for y in range(N) for x in range(N) if px[x,y])
def diag(u,v):  # weapon axis: u along (pommel 0..tip), v perpendicular (+ = upper/back side)
    k=0.7071; return (5+(u-v)*k, 59-(u+v)*k)
def dpoly(l,pts,g=200): l.poly([diag(u,v) for u,v in pts],g)
def dline(l,a,b,w=1,g=200): l.line(diag(*a),diag(*b),w,g)
def ddisc(l,u,v,r,g=200):
    x,y=diag(u,v); l.ell(x,y,r,r,g)
