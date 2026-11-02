import sys, os, random, math
sys.path.insert(0, '/home/user/geddylifeson/global-repo/cloud/texture-pack/gaps-src')
from pixel_icons import Cv, ramp, N
from PIL import Image
OUT = sys.argv[1] + '/diaithirbosses/textures/block/'; os.makedirs(OUT, exist_ok=True); rnd = random.Random(11)
def rect(cv, x0, y0, x1, y1, c):
    for y in range(y0, y1):
        for x in range(x0, x1): cv.put(x, y, c)
def stone(R, n=.22):
    cv = Cv()
    for y in range(N):
        for x in range(N): cv.put(x, y, R[rnd.choice((1, 2, 2, 2, 3)) if rnd.random() < n * 3 else 2])
    for i in range(N): cv.put(i, 0, R[3]); cv.put(0, i, R[3]); cv.put(i, N - 1, R[1]); cv.put(N - 1, i, R[1])
    return cv
def save(name, cv): cv.img().resize((64, 64), Image.NEAREST).save(OUT + name + '.png')
S = ramp((78, 84, 92)); cv = stone(S); G = ramp((120, 230, 190))   # ogham stone: slate with a glowing stem and notches
rect(cv, 15, 3, 17, 29, G[2]); rect(cv, 15, 3, 16, 29, G[3])
for y, side in ((8, -1), (13, 1), (18, -1), (23, 1)):
    for k in range(1, 6): cv.put(16 + side * k, y + (k // 3), G[2])
save('ogham_stone', cv)
K = ramp((40, 30, 56)); cv = stone(K, .3); P = ramp((190, 130, 255))  # ascendant altar: obsidian with violet knotwork ring
for a in range(60):
    t = a * math.pi / 30; r = 9 + 2.2 * math.cos(3 * t); cv.put(round(16 + r * math.cos(t)), round(16 + r * math.sin(t)), P[2 + (a % 2)])
for a in range(24): t = a * math.pi / 12; cv.put(round(16 + 4 * math.cos(t)), round(16 + 4 * math.sin(t)), P[4])
save('ascendant_altar', cv)
W = ramp((120, 116, 108)); cv = stone(W); Gd = ramp((214, 172, 70), True)   # boss altar: pale stone with a gold rune cross
rect(cv, 14, 5, 18, 27, Gd[2]); rect(cv, 6, 14, 26, 18, Gd[2]); rect(cv, 15, 6, 17, 26, Gd[3]); rect(cv, 7, 15, 25, 17, Gd[3])
save('boss_altar', cv)
