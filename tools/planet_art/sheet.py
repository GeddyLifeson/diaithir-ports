import sys,glob
from PIL import Image
fs=sys.argv[2:]; S=4
sh=Image.new('RGBA',(len(fs)*66*S//1 if len(fs)<9 else 8*66*S, ((len(fs)+7)//8)*66*S),(70,70,80,255))
for i,f in enumerate(fs):
    im=Image.open(f).convert('RGBA').resize((64*S,64*S),Image.NEAREST); sh.alpha_composite(im,((i%8)*66*S,(i//8)*66*S))
sh.convert('RGB').save(sys.argv[1])
