import os,sys,math
from PIL import Image,ImageDraw
OUT=sys.argv[1]+'/assets/antique_atlas/textures/atlas/marker/'; os.makedirs(OUT+'custom',exist_ok=True); os.makedirs(OUT+'structure',exist_ok=True)
S=8; INK=(48,34,28,255); WH=(255,255,255,255)
def canvas(w=32,h=32): return Image.new('RGBA',(w*S,h*S),(0,0,0,0))
def P(pts): return [(x*S,y*S) for x,y in pts]
def fin(im,w=32,h=32):
    a=im.resize((w,h),Image.BOX); a.putalpha(a.getchannel('A').point(lambda v:255 if v>96 else 0)); return a
def glyph(name):
    m=canvas(); a=canvas(); dm=ImageDraw.Draw(m); da=ImageDraw.Draw(a); dt=canvas(); dd=ImageDraw.Draw(dt)
    def poly(pts,fill_acc=True,ink=True,w=1.2):
        if ink: dm.polygon(P(pts),fill=INK)
        if fill_acc: da.polygon(P(pts),fill=WH)
    if name=='bed':
        dm.rounded_rectangle([3*S,13*S,29*S,25*S],radius=2*S,fill=INK); da.rounded_rectangle([5*S,15*S,27*S,23*S],radius=S,fill=WH); dm.rounded_rectangle([5*S,15*S,12*S,21*S],radius=S,fill=INK); dm.rectangle([3*S,25*S,6*S,28*S],fill=INK); dm.rectangle([26*S,25*S,29*S,28*S],fill=INK); dm.rectangle([3*S,9*S,6*S,14*S],fill=INK)
    elif name=='brush':
        dm.line(P([(8,26),(22,10)]),fill=INK,width=3*S); da.line(P([(8,26),(22,10)]),fill=WH,width=S); dm.polygon(P([(20,12),(28,4),(30,8),(24,16)]),fill=INK); da.polygon(P([(22,12),(27,6),(28,8),(24,14)]),fill=WH)
    elif name=='diamond':
        pts=[(16,3),(28,13),(16,29),(4,13)]; poly(pts); da.polygon(P([(16,3),(28,13),(16,29),(4,13)]),fill=None); dm.polygon(P([(16,9),(22,13),(16,24),(10,13)]),fill=(0,0,0,0)); da.polygon(P([(16,9),(22,13),(16,24),(10,13)]),fill=WH)
    elif name=='pickaxe':
        dm.line(P([(6,28),(22,12)]),fill=INK,width=3*S); da.line(P([(6,28),(22,12)]),fill=WH,width=S); dm.arc([4*S,3*S,28*S,24*S],200,340,fill=INK,width=4*S); da.arc([6*S,6*S,26*S,23*S],205,335,fill=WH,width=S)
    elif name=='point':
        dm.ellipse([10*S,3*S,23*S,16*S],fill=INK); da.ellipse([12*S,5*S,21*S,14*S],fill=WH); dm.polygon(P([(12,13),(21,13),(16.5,29)]),fill=INK); da.polygon(P([(14,13),(19,13),(16.5,23)]),fill=WH)
    elif name in('red_x_large','red_x_small'):
        w=5 if name.endswith('large') else 3; r=11 if name.endswith('large') else 6
        dm.line(P([(16-r,16-r),(16+r,16+r)]),fill=INK,width=(w+2)*S); dm.line(P([(16-r,16+r),(16+r,16-r)]),fill=INK,width=(w+2)*S); da.line(P([(16-r,16-r),(16+r,16+r)]),fill=WH,width=w*S); da.line(P([(16-r,16+r),(16+r,16-r)]),fill=WH,width=w*S)
    elif name=='scroll':
        dm.rounded_rectangle([7*S,6*S,25*S,26*S],radius=2*S,fill=INK); da.rounded_rectangle([9*S,8*S,23*S,24*S],radius=S,fill=WH); dm.ellipse([5*S,3*S,11*S,9*S],fill=INK); dm.ellipse([21*S,23*S,27*S,29*S],fill=INK)
        for y in (12,16,20): dd.line(P([(11,y),(21,y)]),fill=INK,width=S)
    elif name=='skull':
        dm.ellipse([5*S,3*S,27*S,23*S],fill=INK); dm.rectangle([10*S,18*S,22*S,28*S],fill=INK); da.ellipse([7*S,5*S,25*S,21*S],fill=WH); da.rectangle([11*S,18*S,21*S,26*S],fill=WH)
        for x in (10,18): dd.ellipse([x*S,10*S,(x+5)*S,16*S],fill=INK)
        dd.polygon(P([(15,17),(17,17),(16,20)]),fill=INK)
        for x in (13,16,19): dd.line(P([(x,22),(x,26)]),fill=INK,width=S)
    elif name=='stuck_sword':
        dm.polygon(P([(14,3),(18,3),(18,22),(16,26),(14,22)]),fill=INK); da.polygon(P([(15,5),(17,5),(17,21),(16,23),(15,21)]),fill=WH); dm.rectangle([9*S,22*S,23*S,25*S],fill=INK); dm.rectangle([15*S,25*S,17*S,29*S],fill=INK); dm.polygon(P([(4,30),(28,30),(26,27),(6,27)]),fill=INK)
    elif name=='sword':
        dm.polygon(P([(24,3),(29,4),(28,9),(12,22),(9,19)]),fill=INK); da.polygon(P([(24,5),(27,5),(26,8),(12,20),(11,19)]),fill=WH); dm.line(P([(6,16),(16,26)]),fill=INK,width=3*S); dm.line(P([(6,26),(9,23)]),fill=INK,width=3*S)
    elif name=='tower':
        dm.rectangle([10*S,10*S,22*S,29*S],fill=INK); da.rectangle([12*S,12*S,20*S,28*S],fill=WH)
        for x in (8,13,18): dm.rectangle([x*S,5*S,(x+5)*S,11*S],fill=INK); da.rectangle([(x+1)*S,6*S,(x+4)*S,10*S],fill=WH)
        dd.rectangle([14*S,21*S,18*S,29*S],fill=INK)
    mm=fin(m); aa=fin(a); dd_=fin(dt)
    ma=mm.getchannel('A'); aal=aa.getchannel('A')
    ma=Image.composite(Image.new('L',ma.size,0),ma,aal); mm.putalpha(ma); mm.alpha_composite(dd_); aa.putalpha(Image.composite(Image.new('L',aal.size,0),aal,dd_.getchannel('A')))
    return mm,aa
for n in ('bed','brush','diamond','pickaxe','point','red_x_large','red_x_small','scroll','skull','stuck_sword','sword','tower'):
    m,a=glyph(n); m.save(OUT+f'custom/{n}.png'); a.save(OUT+f'custom/{n}_accent.png')
# structure markers (coloured, no accent)
def roof_house(d,x,y,w,h,roof,wall):
    d.rectangle([x*S,(y+h*0.4)*S,(x+w)*S,(y+h)*S],fill=wall,outline=INK,width=S//2); d.polygon(P([(x-1,y+h*0.4),(x+w/2,y),(x+w+1,y+h*0.4)]),fill=roof,outline=INK); d.rectangle([(x+w/2-1.5)*S,(y+h-4)*S,(x+w/2+1.5)*S,(y+h)*S],fill=(70,45,30,255))
def village(name,roof,wall,ground):
    im=canvas(); d=ImageDraw.Draw(im); d.ellipse([2*S,18*S,30*S,30*S],fill=ground,outline=INK,width=S//2)
    roof_house(d,4,12,12,14,roof,wall); roof_house(d,17,8,11,16,roof,wall); return fin(im)
for n,r,w,g in (('plains_village',(150,70,50,255),(176,136,84,255),(140,175,90,255)),('desert_village',(214,160,96,255),(228,200,150,255),(228,204,146,255)),('savanna_village',(170,128,64,255),(196,156,96,255),(190,176,96,255)),('snowy_village',(244,247,250,255),(150,110,80,255),(238,242,246,255)),('taiga_village',(70,60,50,255),(120,90,60,255),(96,134,84,255))): village(n,r,w,g).save(OUT+f'structure/{n}.png')
def simple(name,fn): im=canvas(); fn(ImageDraw.Draw(im)); fin(im).save(OUT+f'structure/{name}.png')
def monument(d): 
    d.polygon(P([(4,28),(10,12),(16,4),(22,12),(28,28)]),fill=(90,170,160,255),outline=INK); d.rectangle([13*S,16*S,19*S,28*S],fill=(60,130,125,255),outline=INK); d.ellipse([14*S,9*S,18*S,13*S],fill=(240,200,80,255),outline=INK)
def bastion(d): d.rectangle([4*S,10*S,28*S,28*S],fill=(60,52,56,255),outline=INK,width=S//2); [d.rectangle([x*S,6*S,(x+4)*S,11*S],fill=(60,52,56,255),outline=INK) for x in (4,12,20,24)]; d.rectangle([13*S,18*S,19*S,28*S],fill=(30,20,24,255))
def mansion(d): d.rectangle([3*S,14*S,29*S,29*S],fill=(100,70,50,255),outline=INK,width=S//2); d.polygon(P([(2,14),(16,3),(30,14)]),fill=(50,60,45,255),outline=INK); d.rectangle([14*S,20*S,18*S,29*S],fill=(40,28,20,255)); [d.rectangle([x*S,18*S,(x+3)*S,22*S],fill=(220,200,120,255)) for x in (6,23)]
simple('ocean_monument',monument); simple('bastion_remnant',bastion); simple('woodland_mansion',mansion)
# end city 112x64: 3.5 tiles of 32x64 frames? draw a tower sprite sheet (frames 32x64 x3 + 16)
im=canvas(112,64); d=ImageDraw.Draw(im)
for k,x in enumerate((0,32,64)):
    h=30+k*6; d.rectangle([(x+10)*S,(64-h)*S,(x+22)*S,60*S],fill=(214,205,150,255),outline=INK,width=S//2); d.polygon(P([(x+8,64-h),(x+16,64-h-10),(x+24,64-h)]),fill=(170,90,170,255),outline=INK); d.rectangle([(x+14)*S,(64-h+6)*S,(x+18)*S,(64-h+12)*S],fill=(60,40,70,255))
fin(im,112,64).save(OUT+'structure/end_city.png')
print(len(os.listdir(OUT+'custom'))+len(os.listdir(OUT+'structure')))
