import os,sys,random,zlib,math
from PIL import Image,ImageDraw
OUT=sys.argv[1]+'/assets/diaithir_wiretech/textures/'; os.makedirs(OUT+'block',exist_ok=True); os.makedirs(OUT+'item',exist_ok=True)
RED,GREEN=(214,58,48),(74,190,84)
TIERS={'mechanical':((122,92,60),(200,160,80)),'relay':((96,100,108),(214,120,60)),'electronic':((62,70,82),(110,200,235))}
KINDS={'constant_combinator':('dots',0,1,0),'arithmetic_combinator':('plus',1,1,0),'decider_combinator':('gt',1,1,0),'selector_combinator':('bars',1,1,0),'circuit_reader':('eye',0,1,0),'circuit_gate':('slash',1,0,1),'circuit_lamp':('bulb',1,0,1),'circuit_speaker':('rings',1,0,1)}
def sh(c,d): return tuple(max(0,min(255,v+d)) for v in c)
def plate(t,base,seed,top=False):
    r=random.Random(seed); im=Image.new('RGB',(64,64),base); px=im.load()
    for y in range(64):
        for x in range(64):
            n=r.randint(-7,7); px[x,y]=sh(base,n)
    d=ImageDraw.Draw(im)
    if t=='mechanical':  # planks with brass bands
        for y in range(0,64,16): d.line([(0,y),(63,y)],fill=sh(base,-40)); d.line([(0,y+1),(63,y+1)],fill=sh(base,22))
        for i in range(60): x=r.randint(0,63); y=r.randint(0,63); d.line([(x,y),(x+r.randint(3,9),y)],fill=sh(base,-18))
        for y in (6,57): d.rectangle([0,y,63,y+3],fill=(176,138,60)); d.line([(0,y),(63,y)],fill=(220,185,95))
    elif t=='relay':  # steel panel seams, rivet rows
        d.line([(31,2),(31,61)],fill=sh(base,-36)); d.line([(32,2),(32,61)],fill=sh(base,24))
        for y in range(8,60,13):
            for x in (31,32): pass
    else:  # electronic: pcb traces
        for k in range(7):
            x=r.randint(6,56); y=r.randint(6,56); L=r.randint(8,22); d.line([(x,y),(x+L,y)],fill=sh(base,34)); d.line([(x+L,y),(x+L,y+r.randint(-10,10))],fill=sh(base,34)); d.rectangle([x-1,y-1,x+1,y+1],fill=sh(base,56))
    dark,light=sh(base,-46),sh(base,34)
    d.rectangle([0,0,63,63],outline=dark); d.rectangle([1,1,62,62],outline=light if False else sh(base,-20)); d.line([(2,2),(61,2)],fill=light); d.line([(2,2),(2,61)],fill=light); d.line([(2,61),(61,61)],fill=sh(base,-30))
    for p in ((5,5),(58,5),(5,58),(58,58)):
        d.ellipse([p[0]-2,p[1]-2,p[0]+2,p[1]+2],fill=(176,178,184)); d.point((p[0]-1,p[1]-1),fill=(235,235,240)); d.point((p[0]+1,p[1]+1),fill=(90,92,98))
    return im
def glyph(im,g,col,lit):
    d=ImageDraw.Draw(im); c=sh(col,40) if lit else col; cx=cy=32
    d.rectangle([13,13,50,50],fill=sh((20,22,26),0 if not lit else 10),outline=(8,9,11))
    if g=='dots':
        for x in (19,29,39): d.rectangle([x,29,x+4,34],fill=c)
    elif g=='plus': d.rectangle([29,18,34,46],fill=c); d.rectangle([18,29,46,34],fill=c)
    elif g=='gt': d.line([(20,19),(40,31)],fill=c,width=5); d.line([(40,33),(20,45)],fill=c,width=5)
    elif g=='bars':
        for y,w in ((19,28),(29,18),(39,9)): d.rectangle([18,y,18+w,y+4],fill=c)
    elif g=='eye': d.ellipse([17,22,46,42],outline=c,width=3); d.ellipse([28,27,36,37],fill=c)
    elif g=='slash': d.line([(20,45),(43,19)],fill=c,width=5); d.ellipse([16,41,24,49],fill=c); d.ellipse([39,15,47,23],fill=c)
    elif g=='bulb': d.ellipse([20,17,43,40],fill=c); d.rectangle([26,41,37,46],fill=sh(c,-60)); d.line([(28,22),(26,28)],fill=(255,255,255) if lit else sh(c,50),width=2)
    elif g=='rings': d.ellipse([16,16,47,47],outline=c,width=3); d.ellipse([25,25,38,38],outline=c,width=3); d.ellipse([30,30,33,33],fill=c)
def pins(im,hin,hout,side):
    d=ImageDraw.Draw(im)
    if (side=='back' and hin) or (side=='front' and hout):
        for x,c in ((13,RED),(45,GREEN)): d.rectangle([x,52,x+6,58],fill=(30,30,34)); d.rectangle([x+1,53,x+5,57],fill=c)
for t,(base,acc) in TIERS.items():
    plate(t,base,len(t),False).save(OUT+f'block/circuit_{t}_side.png'); plate(t,sh(base,12),len(t)+1,True).save(OUT+f'block/circuit_{t}_top.png')
    for k,(g,hin,hout,litv) in KINDS.items():
        for side in ('front','back'):
            for lit in ((False,True) if litv and side=='front' else (False,)):
                im=plate(t,sh(base,-6 if side=='back' else 0),zlib.crc32(f'{t}{k}{side}'.encode())%997)
                if side=='front': glyph(im,g,acc,lit)
                pins(im,hin,hout,side)
                if lit: ImageDraw.Draw(im).rectangle([8,8,55,10],fill=sh(acc,60))
                im.save(OUT+f'block/circuit_{t}_{k}_{side}{"_lit" if lit else ""}.png')
    for cn,cc in (('red',RED),('green',GREEN)):
        col=sh(cc,{'mechanical':-25,'relay':0,'electronic':30}[t]); im=Image.new('RGBA',(64,64),(0,0,0,0)); p=im.load(); dark=sh(col,-60)
        for y in range(64):
            for x in range(64):
                dd=(x-30)**2+(y-30)**2
                if dd<=26**2: p[x,y]=col+(255,) if int(math.sqrt(dd)/2.2)%2==0 else dark+(255,)
                if dd<=11**2: p[x,y]=(58,58,64,255)
                if dd<=5**2: p[x,y]=(18,19,22,255)
        for q in((14,14),(15,13),(13,16),(16,15)): p[q]=sh(col,50)+(255,)
        d=ImageDraw.Draw(im); d.rectangle([49,40,52,63],fill=col); d.line([(52,40),(52,63)],fill=dark)
        im.save(OUT+f'item/{t}_{cn}_circuit_wire.png')
print(len(os.listdir(OUT+'block')),len(os.listdir(OUT+'item')))
