import random,zlib
from PIL import Image,ImageDraw
def sh(c,d): return tuple(int(max(0,min(255,v+d))) for v in c)
GROUND={'desert':(224,200,140),'plains':(140,170,90),'savanna':(180,170,90),'snowy':(236,240,244),'taiga':(90,130,80),'nether':(120,50,50),'jungle':(70,120,50),'swamp':(90,110,80)}
ROOF={'desert':(200,150,90),'plains':(150,70,50),'savanna':(160,120,60),'snowy':(240,244,248),'taiga':(70,60,50)}
WALL={'desert':(226,196,150),'plains':(170,130,80),'savanna':(190,150,90),'snowy':(150,110,80),'taiga':(120,90,60)}
def biome_of(n):
    for k in ('desert','plains','savanna','snowy','taiga','nether','jungle','swamp'):
        if k in n: return k
    return 'plains'
def house(d,x0,y0,x1,y1,b,gable=True):
    wl=WALL[b]; rf=ROOF[b]; ol=(50,38,28)
    d.rectangle([x0,y0+(y1-y0)//3,x1,y1],fill=wl,outline=ol)
    if gable and b not in('desert','savanna'): d.polygon([(x0-2,y0+(y1-y0)//3),((x0+x1)//2,y0-1),(x1+2,y0+(y1-y0)//3)],fill=rf,outline=ol)
    else: d.rectangle([x0-1,y0+(y1-y0)//3-4,x1+1,y0+(y1-y0)//3],fill=rf,outline=ol)
    dw=(x1-x0)//4; d.rectangle([(x0+x1)//2-dw//2,y1-6,(x0+x1)//2+dw//2,y1],fill=(70,45,30)); d.rectangle([x0+3,y0+(y1-y0)//2,x0+6,y0+(y1-y0)//2+3],fill=(150,200,230))
def struct(name,base_img):
    n=name; b=biome_of(n); rnd=random.Random(zlib.crc32(n.encode())); im=base_img.copy(); d=ImageDraw.Draw(im); ol=(50,38,28)
    d.rectangle([0,0,63,95],fill=GROUND.get(b,(140,170,90)))
    for i in range(30): x=rnd.randrange(64); y=rnd.randrange(96); d.point((x,y),fill=sh(GROUND.get(b,(140,170,90)),-14))
    last=n.split('/')[-1]; path=(196,170,120) if b!='snowy' else (200,190,170)
    if 'path_horizontal' in last: d.rectangle([0,38,63,57],fill=path)
    elif 'path_vertical' in last: d.rectangle([22,0,41,95],fill=path)
    elif 'path_crossroad' in last: d.rectangle([0,38,63,57],fill=path); d.rectangle([22,0,41,95],fill=path)
    elif 'path_corners' in last: d.rectangle([0,38,41,57],fill=path); d.rectangle([22,38,41,95],fill=path)
    elif last in('well','well_covered','fountain'):
        d.ellipse([18,34,46,62],fill=(130,130,130),outline=ol); d.ellipse([23,39,41,57],fill=(80,130,190))
        if 'covered' in last: d.rectangle([20,28,44,34],fill=ROOF[b],outline=ol); d.line([(21,34),(21,50)],fill=ol); d.line([(43,34),(43,50)],fill=ol)
    elif last=='lamp': d.line([(32,30),(32,66)],fill=(80,60,40),width=2); d.ellipse([28,24,36,32],fill=(250,220,120),outline=ol)
    elif 'farm' in last:
        d.rectangle([8,16,55,80],fill=(110,80,50),outline=ol)
        for y in range(20,78,8): d.line([(10,y),(53,y)],fill=(110,170,60),width=3)
    elif 'animal_pen' in last:
        d.rectangle([8,22,55,74],outline=(110,80,50),width=2)
        for x in range(10,55,6): d.line([(x,20),(x,24)],fill=(110,80,50)); d.line([(x,72),(x,76)],fill=(110,80,50))
        d.ellipse([22,40,30,48],fill=(240,240,230),outline=ol); d.ellipse([36,52,44,60],fill=(150,100,70),outline=ol)
    elif 'market' in last:
        for i,x in enumerate((6,24,42)):
            d.rectangle([x,36,x+14,60],fill=(120,90,60),outline=ol); d.rectangle([x-1,28,x+15,38],fill=(200,60,60) if i%2==0 else (240,240,230),outline=ol)
    elif 'house' in last or last in('shed','temple','butcher_shop','weaponsmith','tall_house'):
        s=14 if 'small' in last or last=='shed' else 22 if 'large' in last or 'tall' in last or last=='temple' else 18
        h_=40 if 'tall' in last else 30 if 'large' in last else 24
        house(d,32-s,48-h_//2,32+s,48+h_//2,b,'flat' not in last)
        if 'double' in last: house(d,6,60,26,86,b)
        if 'snowy' in last: d.rectangle([32-s-2,48-h_//2-3,32+s+2,48-h_//2+1],fill=(245,248,252))
        if last=='igloo_house': d.pieslice([10,30,54,74],180,360,fill=(240,246,250),outline=(120,150,170)); d.rectangle([28,52,36,60],fill=(60,70,90))
    elif 'tower' in last or 'watchtower' in last: d.rectangle([24,24,40,72],fill=(120,95,65),outline=ol); d.rectangle([20,18,44,26],fill=(90,70,50),outline=ol)
    elif last=='tent': d.polygon([(10,70),(32,24),(54,70)],fill=(210,90,70),outline=ol); d.polygon([(26,70),(32,50),(38,70)],fill=(60,40,30))
    elif last=='cage': d.rectangle([20,30,44,66],outline=(90,90,95),width=2); [d.line([(x,30),(x,66)],fill=(90,90,95)) for x in range(24,44,4)]
    elif 'pyramid' in last: d.polygon([(4,76),(32,14),(60,76)],fill=(214,184,120),outline=ol); d.polygon([(32,14),(60,76),(44,76)],fill=(180,150,95)); d.rectangle([28,62,36,76],fill=(60,40,30))
    elif 'jungle_temple' in last: d.rectangle([12,30,52,74],fill=(110,125,90),outline=ol); d.rectangle([20,18,44,32],fill=(100,115,80),outline=ol); d.rectangle([28,60,36,74],fill=(40,50,40))
    elif last=='ruined_portal': d.rectangle([20,28,44,66],outline=(60,30,90),width=4); d.rectangle([26,34,38,60],fill=(120,40,170))
    elif 'shipwreck' in last: d.polygon([(8,52),(56,48),(50,70),(14,72)],fill=(130,90,55),outline=ol); d.line([(30,48),(30,22)],fill=(110,80,50),width=2)
    elif 'witch_hut' in n or 'swamp_hut' in last: d.rectangle([14,44,50,64],fill=(90,65,45),outline=ol); d.polygon([(10,44),(32,20),(54,44)],fill=(60,70,50),outline=ol)
    elif 'fossil' in last: d.arc([8,24,56,80],20,160,fill=(235,230,210),width=5); [d.line([(14+i*8,50),(14+i*8,66)],fill=(235,230,210),width=3) for i in range(5)]
    elif 'igloo'==last: d.pieslice([10,30,54,74],180,360,fill=(240,246,250),outline=(120,150,170))
    elif 'nether_fortress' in last:
        d.rectangle([0,0,63,95],fill=(60,25,30)); c=(105,45,55)
        if 'horizontal' in last or 'bridge_end_h' in last: d.rectangle([0,34,63,62],fill=c,outline=(30,12,16))
        elif 'vertical' in last: d.rectangle([18,0,46,95],fill=c,outline=(30,12,16))
        elif 'crossing' in last: d.rectangle([0,34,63,62],fill=c); d.rectangle([18,0,46,95],fill=c)
        else: d.rectangle([10,10,54,86],fill=c,outline=(30,12,16))
    return im
