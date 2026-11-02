import sys, os, random
sys.path.insert(0, '/home/user/geddylifeson/global-repo/cloud/texture-pack/gaps-src')
from pixel_icons import Cv, ramp, N
from PIL import Image
OUT = sys.argv[1]; rnd = random.Random(7)
def put_rect(cv, x0, y0, x1, y1, c):
    for y in range(y0, y1):
        for x in range(x0, x1): cv.put(x, y, c)
def bricks(cv, R, mortar):
    put_rect(cv, 0, 0, N, N, mortar)
    for r in range(8):
        off = 0 if r % 2 == 0 else 4
        for i in range(-1, 5):
            x0 = off + i * 8; y0 = r * 4
            lvl = rnd.choice((1, 2, 2, 2, 3))
            put_rect(cv, max(0, x0 + 1), y0 + 1, min(N, x0 + 8), y0 + 4, R[lvl])
            for x in range(max(0, x0 + 1), min(N, x0 + 8)): cv.put(x, y0 + 1, R[min(4, lvl + 1)]) if rnd.random() < .5 else 0
def save(name, cv): 
    f = '%s/diaithirfoundry/textures/block/%s.png' % (OUT, name); os.makedirs(os.path.dirname(f), exist_ok=True); cv.img().resize((64, 64), Image.NEAREST).save(f)
FB = ramp((150, 70, 52)); M = (62, 40, 36, 255)
cv = Cv(); bricks(cv, FB, M); save('foundry_bricks', cv)
cv = Cv(); bricks(cv, FB, M); put_rect(cv, 6, 8, 26, 26, (28, 18, 16, 255)); put_rect(cv, 8, 14, 24, 26, (230, 120, 30, 255)); put_rect(cv, 10, 18, 22, 26, (255, 200, 70, 255)); put_rect(cv, 6, 6, 26, 8, ramp((90, 92, 100), True)[2]); save('foundry_controller', cv)
cv = Cv(); bricks(cv, FB, M); put_rect(cv, 8, 10, 24, 24, (24, 20, 20, 255))
for x in range(9, 24, 3): put_rect(cv, x, 10, x + 1, 24, ramp((120, 124, 134), True)[3])
put_rect(cv, 8, 24, 24, 26, ramp((120, 124, 134), True)[2]); save('foundry_drain', cv)
S = ramp((128, 134, 146), True); cv = Cv(); put_rect(cv, 0, 0, N, N, S[2])
for i in range(N): cv.put(i, 0, S[4]); cv.put(0, i, S[3]); cv.put(i, N - 1, S[0]); cv.put(N - 1, i, S[1])
put_rect(cv, 6, 5, 26, 27, S[0]); put_rect(cv, 7, 6, 25, 26, (50, 70, 90, 255)); put_rect(cv, 8, 16, 24, 25, (240, 130, 30, 255)); put_rect(cv, 8, 20, 24, 25, (255, 190, 60, 255)); put_rect(cv, 9, 7, 12, 14, (170, 200, 220, 255))
for p in ((2, 2), (29, 2), (2, 29), (29, 29)): cv.put(*p, S[0])
save('foundry_tank', cv)
cv = Cv(); put_rect(cv, 0, 0, N, N, S[2])
for i in range(N): cv.put(i, 0, S[4]); cv.put(0, i, S[3]); cv.put(i, N - 1, S[0]); cv.put(N - 1, i, S[1])
put_rect(cv, 5, 10, 27, 20, S[0]); put_rect(cv, 6, 11, 26, 19, (200, 206, 216, 255))
for x in range(7, 26, 2): cv.put(x, 12, S[4]); cv.put(x, 17, S[1])
G = ramp((70, 170, 220)); poly = None
for y in range(22, 28):
    for x in range(12 + (y - 22), 20 - (y - 22)): cv.put(x, y, G[3 if y < 25 else 2])
save('gem_cutter', cv)
print('blocks ok')
