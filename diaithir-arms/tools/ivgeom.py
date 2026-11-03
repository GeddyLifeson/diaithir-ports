"""Geometry helpers for the Immersive Vehicles packs: OBJ writer plus box, cylinder and wedge faces."""
import math, os

class Obj:
    def __init__(s): s.v, s.vt, s.vn, s.o = [], [], [], []
    def add(s, name, faces, rect):
        """faces: list of quads/tris of (x,y,z); rect = (u0,v0,u1,v1) texture patch."""
        s.o.append((name, faces, rect))
    def write(s, path):
        os.makedirs(os.path.dirname(path), exist_ok=True)
        L, vi, ti, ni = [], 0, 0, 0
        for name, faces, rect in s.o:
            L.append(f'o {name}')
            u0, v0, u1, v1 = rect
            for f in faces:
                a, b, c = f[0], f[1], f[2]
                n = (( (b[1]-a[1])*(c[2]-a[2]) - (b[2]-a[2])*(c[1]-a[1]) ), ( (b[2]-a[2])*(c[0]-a[0]) - (b[0]-a[0])*(c[2]-a[2]) ), ( (b[0]-a[0])*(c[1]-a[1]) - (b[1]-a[1])*(c[0]-a[0]) ))
                ln = math.sqrt(sum(k*k for k in n)) or 1; n = tuple(k/ln for k in n)
                for p in f: L.append('v %.4f %.4f %.4f' % p)
                uv = [(u0, v0), (u1, v0), (u1, v1), (u0, v1)][:len(f)]
                for u, v in uv: L.append('vt %.4f %.4f' % (u, 1 - v))
                L.append('vn %.4f %.4f %.4f' % n)
                ni += 1
                L.append('f ' + ' '.join(f'{vi + i + 1}/{ti + i + 1}/{ni}' for i in range(len(f))))
                vi += len(f); ti += len(f)
        open(path, 'w').write('\n'.join(L) + '\n')

def box(x0, y0, z0, x1, y1, z1):
    p = lambda x, y, z: (x, y, z)
    return [[p(x0,y0,z1),p(x1,y0,z1),p(x1,y1,z1),p(x0,y1,z1)], [p(x1,y0,z0),p(x0,y0,z0),p(x0,y1,z0),p(x1,y1,z0)],
            [p(x0,y1,z1),p(x1,y1,z1),p(x1,y1,z0),p(x0,y1,z0)], [p(x0,y0,z0),p(x1,y0,z0),p(x1,y0,z1),p(x0,y0,z1)],
            [p(x1,y0,z1),p(x1,y0,z0),p(x1,y1,z0),p(x1,y1,z1)], [p(x0,y0,z0),p(x0,y0,z1),p(x0,y1,z1),p(x0,y1,z0)]]

def cyl_z(cx, cy, z0, z1, r, n=10):
    out = []
    for i in range(n):
        a, b = 2*math.pi*i/n, 2*math.pi*(i+1)/n
        p = lambda ang, z: (cx + r*math.cos(ang), cy + r*math.sin(ang), z)
        out.append([p(a, z0), p(b, z0), p(b, z1), p(a, z1)])
    out.append([(cx + r*math.cos(2*math.pi*i/n), cy + r*math.sin(2*math.pi*i/n), z1) for i in range(n)][:4])
    return out

def cyl_x(cy, cz, x0, x1, r, n=10):
    out = []
    for i in range(n):
        a, b = 2*math.pi*i/n, 2*math.pi*(i+1)/n
        p = lambda ang, x: (x, cy + r*math.sin(ang), cz + r*math.cos(ang))
        out.append([p(a, x0), p(b, x0), p(b, x1), p(a, x1)])
    return out

def wedge(x0, x1, y0, y1, z0, z1, ytop_front):
    """Box whose top slopes down toward +z (glacis): top at y1 for z0 and ytop_front at z1."""
    p = lambda x, y, z: (x, y, z)
    return [[p(x0,y0,z1),p(x1,y0,z1),p(x1,ytop_front,z1),p(x0,ytop_front,z1)], [p(x1,y0,z0),p(x0,y0,z0),p(x0,y1,z0),p(x1,y1,z0)],
            [p(x0,ytop_front,z1),p(x1,ytop_front,z1),p(x1,y1,z0),p(x0,y1,z0)], [p(x0,y0,z0),p(x1,y0,z0),p(x1,y0,z1),p(x0,y0,z1)],
            [p(x1,y0,z1),p(x1,y0,z0),p(x1,y1,z0),p(x1,ytop_front,z1)], [p(x0,y0,z0),p(x0,y0,z1),p(x0,ytop_front,z1),p(x0,y1,z0)]]
