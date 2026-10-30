"""Box geometry of the eighteen creatures (new, blocky, authored in absolute model coordinates: 16 units per block,
y down, ground at y=24, front towards -z). Every box has integer size so the unwrap is clean."""


class Part:
    def __init__(self, name, pivot, parent=None, rot=(0, 0, 0)):
        self.name, self.pivot, self.parent, self.rot, self.boxes = name, pivot, parent, rot, []

    def box(self, x, y, z, w, h, d, mat, eyes=()):
        assert w == int(w) and h == int(h) and d == int(d), (self.name, w, h, d)
        self.boxes.append((x, y, z, int(w), int(h), int(d), mat, tuple(eyes)))
        return self


class Model:
    def __init__(self):
        self.parts = []

    def part(self, name, pivot, parent=None, rot=(0, 0, 0)):
        p = Part(name, pivot, parent, rot)
        self.parts.append(p)
        return p

    def get(self, name):
        return [p for p in self.parts if p.name == name][0]


def human(m, leg=(4, 12, 4), legm='skin', foot=None, torso=(8, 12, 4), torsom='skin', arm=(4, 12, 4), armm='skin',
          head=(8, 8, 8), headm='skin', gap=0, shoulder=2, eyes=None, torso2=None):
    """Upright figure. foot = (material, height) is carved off the bottom of each leg. Returns the head part."""
    lw, lh, ld = leg
    tw, th, td = torso
    aw, ah, ad = arm
    hw, hh, hd = head
    legtop = 24 - lh
    ttop = legtop - th
    for nm, x in (('legA', -lw - gap), ('legB', gap)):
        p = m.part(nm, (x + lw / 2, legtop, 0))
        fh = foot[1] if foot else 0
        p.box(x, legtop, -ld / 2, lw, lh - fh, ld, legm)
        if foot:
            p.box(x, 24 - fh, -ld / 2, lw, fh, ld, foot[0])
    b = m.part('body', (0, ttop, 0))
    b.box(-tw / 2, ttop, -td / 2, tw, th, td, torsom)
    if torso2:
        b.box(*torso2)
    for nm, x in (('armB', -tw / 2 - aw), ('armA', tw / 2)):
        m.part(nm, (x + aw / 2, ttop + shoulder, 0)).box(x, ttop, -ad / 2, aw, ah, ad, armm)
    hp = m.part('head', (0, ttop, 0))
    hp.box(-hw / 2, ttop - hh, -hd / 2, hw, hh, hd, headm,
           eyes=eyes if eyes is not None else [('front', 1, hh // 2), ('front', hw - 2, hh // 2)])
    return hp


def quad(m, body, leg, bodym, legm, hoof=None, front_z=-6, hind_z=7, spread=None, bodyy=None, legtop=None):
    bw, bh, bd = body
    lw, lh, ld = leg
    legtop = 24 - lh if legtop is None else legtop
    by = legtop - bh + 2 if bodyy is None else bodyy
    sp = spread if spread is not None else bw / 2 - lw / 2
    m.part('body', (0, by, 0)).box(-bw / 2, by, -bd / 2, bw, bh, bd, bodym)
    for nm, x, z in (('legA1', -sp, front_z), ('legB1', sp, front_z), ('legB2', -sp, hind_z), ('legA2', sp, hind_z)):
        p = m.part(nm, (x, legtop, z))
        fh = hoof[1] if hoof else 0
        p.box(x - lw / 2, legtop, z - ld / 2, lw, lh - fh, ld, legm)
        if hoof:
            p.box(x - lw / 2, 24 - fh, z - ld / 2, lw, fh, ld, hoof[0])
    return by


def wing(m, nm, side, pivot, span, depth, mat, tipmat=None):
    """side +1 (x>0) or -1. A broad root box and a tip box."""
    p = m.part(nm, pivot)
    x, y, z = pivot
    root = int(span * 0.55)
    if side > 0:
        p.box(x, y, z - 1, root, 1, depth, mat)
        p.box(x + root, y, z, span - root, 1, depth - 2, tipmat or mat)
    else:
        p.box(x - root, y, z - 1, root, 1, depth, mat)
        p.box(x - span, y, z, span - root, 1, depth - 2, tipmat or mat)
    return p


def chain(m, n, z, y_bottom, wfun, seglen, mats, parent):
    """Serpent body: segments hanging off `parent`, each a child of the previous one."""
    prev = parent
    for i in range(1, n + 1):
        w = wfun(i)
        p = m.part('seg%d' % i, (0, y_bottom, z), prev)
        p.box(-w / 2, y_bottom - w + 1, z, w, w - 1, seglen, mats[i % len(mats)])
        prev = p.name
        z += seglen


def m_goburdoiri():  # goat-man
    m = Model()
    hp = human(m, leg=(4, 12, 4), legm='fur_brown', foot=('hoof', 3), torso=(8, 12, 4), torsom='skin',
               arm=(3, 11, 3), armm='skin', head=(6, 6, 6), headm='skin',
               torso2=(-4.5, 6, -2.5, 9, 6, 5, 'fur_brown'))
    hp.box(-1.5, -4, -5, 3, 2, 2, 'skin_pale')
    hp.box(-3, -10.5, -1.5, 1, 5, 1, 'horn'); hp.box(2, -10.5, -1.5, 1, 5, 1, 'horn')
    hp.box(-4, -5, -1, 1, 2, 3, 'skin'); hp.box(3, -5, -1, 1, 2, 3, 'skin')
    hp.box(-1, -2, -4, 2, 3, 1, 'hair_brown')
    hp.box(-3.5, -7, -3.5, 7, 2, 7, 'hair_brown')
    return m


def m_famuirkeid():  # hill giant with a club
    m = Model()
    hp = human(m, leg=(6, 18, 6), legm='skin_grey', foot=('skin_pale', 3), torso=(14, 18, 8), torsom='skin_grey',
               arm=(5, 18, 5), armm='skin_grey', head=(8, 8, 8), headm='skin_grey', gap=1, shoulder=2,
               torso2=(-7.5, 3, -4.5, 15, 4, 9, 'cloth_brown'))
    hp.box(-4.5, -7, -4.5, 9, 2, 1, 'stone')
    armA = m.get('armA')
    armA.box(8, 3, -9, 3, 3, 12, 'wood')
    armA.box(7.5, 1.5, -13, 4, 6, 4, 'wood')
    return m


def m_maktirimatuir():  # horse-bodied archer-folk
    m = Model()
    quad(m, body=(10, 10, 20), leg=(3, 11, 3), bodym='fur_brown', legm='fur_brown', hoof=('hoof', 3),
         front_z=-6, hind_z=8, spread=3, bodyy=4, legtop=13)
    m.part('tail', (0, 6, 10)).box(-1, 6, 10, 2, 10, 2, 'mane_dark')
    p = m.part('torso', (0, 4, -8))
    p.box(-4, -6, -10, 8, 10, 4, 'skin')
    for nm, x in (('armB', -7), ('armA', 4)):
        m.part(nm, (x + 1.5, -4, -8)).box(x, -5, -9.5, 3, 11, 3, 'skin')
    hp = m.part('head', (0, -6, -8))
    hp.box(-3, -12, -11, 6, 6, 6, 'skin', eyes=[('front', 1, 3), ('front', 4, 3)])
    hp.box(-3.5, -13, -10.5, 7, 3, 7, 'hair_black')
    hp.box(-2, -8, -12, 4, 2, 1, 'hair_black')
    return m


def m_famuirknok():  # bronze striding colossus
    m = Model()
    hp = human(m, leg=(5, 16, 5), legm='bronze', foot=('bronze_old', 3), torso=(12, 14, 6), torsom='bronze',
               arm=(4, 15, 4), armm='bronze', head=(7, 7, 7), headm='bronze_old', gap=1,
               eyes=[('front', 1, 3), ('front', 5, 3)], torso2=(-7, 4, -3.5, 14, 4, 7, 'stone'))
    hp.box(-0.5, -16, -4, 1, 4, 8, 'bronze')
    return m


def m_dourasniechgleidid():  # three-headed hound of the gate
    m = Model()
    quad(m, body=(12, 12, 22), leg=(4, 12, 4), bodym='fur_black', legm='fur_black', hoof=('claw', 2),
         front_z=-8, hind_z=9, spread=4, bodyy=2, legtop=12)
    m.get('body').box(-7, 0, -14, 14, 8, 6, 'mane_dark')
    for nm, x, z, hy in (('head', 0, -16, -2), ('head2', -6.5, -13, 1), ('head3', 6.5, -13, 1)):
        p = m.part(nm, (x, 5, -11))
        p.box(x - 3.5, hy, z - 1, 7, 7, 7, 'fur_black', eyes=[('front', 1, 2), ('front', 5, 2)])
        p.box(x - 2, hy + 3, z - 5, 4, 4, 4, 'fur_dark')
        p.box(x - 3.5, hy - 2, z + 4, 2, 3, 1, 'fur_black'); p.box(x + 1.5, hy - 2, z + 4, 2, 3, 1, 'fur_black')
    m.part('tail', (0, 4, 11)).box(-1, 4, 11, 2, 2, 12, 'scale_green')
    return m


def m_tarbiumachuoirtach():  # bull-headed labyrinth lord
    m = Model()
    hp = human(m, leg=(6, 16, 6), legm='fur_dark', foot=('hoof', 4), torso=(14, 16, 8), torsom='skin_grey',
               arm=(5, 17, 5), armm='skin_grey', head=(8, 8, 8), headm='fur_dark', gap=0, shoulder=2,
               torso2=(-7.5, 3, -4.5, 15, 4, 9, 'cloth_red'))
    hp.box(-3, -12, -8, 6, 5, 4, 'fur_brown')
    hp.box(-1.5, -8, -8.5, 3, 1, 1, 'bronze')
    hp.box(-8, -13, -1, 4, 2, 2, 'horn'); hp.box(4, -13, -1, 4, 2, 2, 'horn')
    hp.box(-9, -16, -1, 2, 3, 2, 'horn'); hp.box(7, -16, -1, 2, 3, 2, 'horn')
    hp.box(-5, -12, -1, 1, 2, 2, 'fur_dark'); hp.box(4, -12, -1, 1, 2, 2, 'fur_dark')
    hp.box(-3, -17, 3, 6, 9, 3, 'mane_dark')
    armA = m.get('armA')
    armA.box(8, 7, -5, 2, 2, 12, 'wood')
    armA.box(8.5, 3, -9, 1, 8, 5, 'bronze')
    return m


def m_griublochuirt():  # winged horse
    m = Model()
    quad(m, body=(8, 9, 18), leg=(3, 11, 3), bodym='feather_white', legm='fur_white', hoof=('hoof', 3),
         front_z=-6, hind_z=8, spread=2, bodyy=4, legtop=13)
    m.part('neck', (0, 4, -9)).box(-2, 1, -12, 4, 7, 4, 'fur_white').box(-2, -5, -14, 4, 7, 4, 'fur_white')
    m.get('neck').box(-0.5, -6, -10, 1, 9, 3, 'mane_dark')
    hp = m.part('head', (0, -5, -13))
    hp.box(-2.5, -9, -21, 5, 5, 9, 'fur_white', eyes=[('left', 6, 1), ('right', 2, 1)])
    hp.box(-2, -6, -25, 4, 3, 4, 'fur_cream')
    hp.box(-2.5, -11, -14, 1, 2, 1, 'fur_white'); hp.box(1.5, -11, -14, 1, 2, 1, 'fur_white')
    m.part('tail', (0, 6, 9)).box(-1.5, 5, 9, 3, 12, 2, 'mane_dark')
    wing(m, 'wingP', 1, (4, 4, -2), 15, 9, 'feather_white', 'feather_white')
    wing(m, 'wingN', -1, (-4, 4, -2), 15, 9, 'feather_white', 'feather_white')
    return m


def m_tarbmoir():  # great bronze-ringed bull
    m = Model()
    quad(m, body=(13, 13, 22), leg=(4, 10, 4), bodym='fur_dark', legm='fur_dark', hoof=('hoof', 3),
         front_z=-7, hind_z=8, spread=4, bodyy=1, legtop=14)
    m.get('body').box(-5, -2, -11, 10, 7, 8, 'mane_dark')
    hp = m.part('head', (0, 5, -11))
    hp.box(-4, 1, -19, 8, 8, 8, 'fur_dark', eyes=[('left', 1, 3), ('right', 6, 3)])
    hp.box(-3, 4, -23, 6, 4, 4, 'fur_brown')
    hp.box(-1.5, 6.5, -24, 3, 1, 1, 'bronze')
    hp.box(-9, 0, -16, 5, 2, 2, 'horn'); hp.box(4, 0, -16, 5, 2, 2, 'horn')
    hp.box(-9, -3, -16, 2, 3, 2, 'horn'); hp.box(7, -3, -16, 2, 3, 2, 'horn')
    m.part('tail', (0, 2, 11)).box(-1, 2, 11, 2, 12, 2, 'fur_dark')
    return m


def m_natuirkriegach():  # spider-woman weaver
    m = Model()
    b = m.part('body', (0, 12, 0))
    b.box(-4, 8, -5, 8, 6, 8, 'fur_dark')
    b.box(-6, 7, 3, 12, 10, 14, 'feather_dark')
    for i, z in enumerate((-4, -1, 2, 5)):
        for side in (-1, 1):
            nm = 'leg%s%d' % ('AB'[(i + (side > 0)) % 2], i)
            p = m.part(nm, (side * 3, 13, z), rot=(0, side * (i - 1.5) * 0.3, side * 0.85))
            p.box(3 if side > 0 else -16, 12, z - 1, 13, 2, 2, 'fur_dark')
    t = m.part('torso', (0, 8, -3))
    t.box(-3, -2, -5, 6, 10, 4, 'skin_pale')
    t.box(-3.5, 3, -5.5, 7, 5, 5, 'cloth_linen')
    for nm, x in (('armB', -6), ('armA', 3)):
        m.part(nm, (x + 1.5, 0, -3)).box(x, -1, -4.5, 3, 9, 3, 'skin_pale')
    hp = m.part('head', (0, -2, -3))
    hp.box(-3, -8, -6, 6, 6, 6, 'skin_pale', eyes=[('front', 1, 3), ('front', 4, 3)])
    hp.box(-3.5, -8.5, -2, 7, 8, 3, 'hair_black')
    hp.box(-3.5, -9, -6.5, 7, 2, 7, 'hair_black')
    return m


def m_liupurdsielg():  # maned great cat
    m = Model()
    quad(m, body=(9, 9, 20), leg=(3, 10, 3), bodym='fur_tawny', legm='fur_tawny', hoof=('claw', 2),
         front_z=-7, hind_z=8, spread=3, bodyy=5, legtop=14)
    hp = m.part('head', (0, 8, -10))
    hp.box(-3.5, 3, -16, 7, 7, 6, 'fur_tawny', eyes=[('front', 1, 2), ('front', 5, 2)])
    hp.box(-2, 6, -19, 4, 3, 3, 'fur_cream')
    hp.box(-4, 2, -14, 2, 2, 1, 'fur_tawny'); hp.box(2, 2, -14, 2, 2, 1, 'fur_tawny')
    hp.box(-6, 0, -13, 12, 12, 5, 'mane_dark')
    t = m.part('tail', (0, 7, 10), rot=(0.4, 0, 0))
    t.box(-1, 6, 10, 2, 11, 2, 'fur_tawny'); t.box(-1.5, 16, 9.5, 3, 4, 3, 'mane_dark')
    return m


def m_iuluirbeind():  # bronze-feathered great bird
    m = Model()
    m.part('body', (0, 12, 0)).box(-3.5, 8, -5, 7, 7, 11, 'feather_bronze')
    for nm, x in (('legA', -2), ('legB', 1)):
        p = m.part(nm, (x + 0.5, 15, 1))
        p.box(x, 15, 0, 1, 7, 1, 'claw'); p.box(x - 1, 22, -2, 3, 2, 4, 'claw')
    m.part('neck', (0, 10, -5)).box(-1.5, 3, -7, 3, 8, 3, 'feather_bronze')
    hp = m.part('head', (0, 3, -6))
    hp.box(-2, -1, -9, 4, 4, 5, 'feather_dark', eyes=[('left', 3, 1), ('right', 0, 1)])
    hp.box(-1, 1, -14, 2, 2, 5, 'bronze')
    m.part('tail', (0, 12, 6), rot=(-0.3, 0, 0)).box(-2.5, 10, 6, 5, 1, 8, 'feather_dark')
    wing(m, 'wingP', 1, (3.5, 9, -2), 16, 9, 'feather_bronze', 'feather_dark')
    wing(m, 'wingN', -1, (-3.5, 9, -2), 16, 9, 'feather_bronze', 'feather_dark')
    return m


def m_natuirmoir():  # six-tentacled sea hag with hound heads
    m = Model()
    for i, (x, z) in enumerate(((-6, -4), (0, -7), (6, -4), (-6, 4), (0, 7), (6, 4))):
        m.part('leg%s%d' % ('AB'[i % 2], i), (x, 16, z)).box(x - 2, 16, z - 2, 4, 8, 4, 'skin_blue')
    b = m.part('body', (0, 6, 0))
    b.box(-8, 6, -8, 16, 10, 16, 'skin_blue')
    b.box(-5, -6, -3, 10, 12, 6, 'skin_pale')
    b.box(-5.5, 1, -3.5, 11, 6, 7, 'cloth_teal')
    for nm, x in (('armB', -8), ('armA', 5)):
        m.part(nm, (x + 1.5, -4, 0)).box(x, -5, -1.5, 3, 11, 3, 'skin_pale')
    hp = m.part('head', (0, -6, 0))
    hp.box(-3, -12, -3, 6, 6, 6, 'skin_pale', eyes=[('front', 1, 3), ('front', 4, 3)])
    hp.box(-3.5, -12.5, 0, 7, 12, 3, 'hair_teal')
    hp.box(-3.5, -12.5, -3.5, 7, 2, 7, 'hair_teal')
    for nm, x, sx in (('head2', -11, -1), ('head3', 11, 1)):
        p = m.part(nm, (x - sx * 2, 9, 0))
        p.box(x - 1.5 - sx * 2, 8, -2, 3, 3, 6, 'skin_blue')
        p.box(x - 3 + sx, 4, -5, 6, 6, 7, 'fur_grey', eyes=[('front', 1, 2), ('front', 4, 2)])
        p.box(x - 2 + sx, 7, -9, 4, 3, 4, 'fur_dark')
    return m


def m_leomundkraeb():  # grove dryad
    m = Model()
    hp = human(m, leg=(3, 12, 3), legm='skin_bark', torso=(6, 11, 3), torsom='skin_moss',
               arm=(2, 11, 2), armm='skin_bark', head=(6, 6, 6), headm='skin_bark',
               eyes=[('front', 1, 3), ('front', 4, 3)])
    hp.box(-4, -7, -4, 8, 3, 8, 'leaf'); hp.box(-3, -9, -3, 6, 2, 6, 'leaf_dark')
    hp.box(-4, -5, -1, 1, 4, 4, 'leaf'); hp.box(3, -5, -1, 1, 4, 4, 'leaf')
    return m


def m_leomundcheistrigchatuir():  # river-spring nymph
    m = Model()
    hp = human(m, leg=(3, 12, 3), legm='skin_blue', torso=(6, 11, 3), torsom='cloth_teal',
               arm=(2, 11, 2), armm='skin_blue', head=(6, 6, 6), headm='skin_blue',
               eyes=[('front', 1, 3), ('front', 4, 3)], torso2=(-3.5, 7, -2, 7, 5, 4, 'cloth_teal'))
    hp.box(-3.5, -5, 3, 7, 12, 2, 'hair_teal'); hp.box(-3.5, -6, -3.5, 7, 2, 7, 'hair_teal')
    return m


def m_madadfasaigratad():  # two-headed jackal hound
    m = Model()
    quad(m, body=(7, 7, 14), leg=(3, 9, 3), bodym='fur_sand', legm='fur_sand', hoof=('claw', 2),
         front_z=-5, hind_z=5, spread=2, bodyy=7, legtop=15)
    for nm, x in (('head', -3), ('head2', 3)):
        p = m.part(nm, (x, 9, -7))
        p.box(x - 2.5, 5, -13, 5, 5, 6, 'fur_sand', eyes=[('front', 0, 2), ('front', 4, 2)])
        p.box(x - 1.5, 7, -16, 3, 3, 3, 'fur_cream')
        p.box(x - 2.5, 2, -10, 1, 4, 1, 'fur_dark'); p.box(x + 1.5, 2, -10, 1, 4, 1, 'fur_dark')
    t = m.part('tail', (0, 8, 7), rot=(0.7, 0, 0))
    t.box(-1, 8, 7, 2, 10, 2, 'scale_green'); t.box(-1.5, 17, 7, 3, 2, 3, 'scale_green')
    return m


def m_toimsechunleomund():  # colossal desert serpent
    m = Model()
    z0 = -26
    hp = m.part('head', (0, 24, z0 - 2))
    hp.box(-4, 17, z0 - 10, 8, 7, 10, 'scale_sand', eyes=[('left', 2, 2), ('right', 7, 2)])
    hp.box(-1, 21, z0 - 12, 2, 1, 2, 'cloth_red')
    hp.box(-7, 12, z0, 14, 12, 2, 'scale_dark')
    m.part('anchor', (0, 24, z0 + 2))
    chain(m, 7, z0 + 2, 24, lambda i: max(6, 11 - i // 2), 8, ['scale_sand', 'scale_dark', 'scale_sand'], 'anchor')
    return m


def m_natuirkrun():  # horned reed viper
    m = Model()
    hp = m.part('head', (0, 24, -14))
    hp.box(-2, 20, -18, 4, 4, 5, 'scale_green', eyes=[('left', 3, 1), ('right', 0, 1)])
    hp.box(-2, 17, -16, 1, 3, 1, 'horn'); hp.box(1, 17, -16, 1, 3, 1, 'horn')
    m.part('anchor', (0, 24, -13))
    chain(m, 5, -13, 24, lambda i: max(2, 4 - i // 3), 5, ['scale_dark', 'scale_green'], 'anchor')
    return m


def m_daelaggrianu():  # woman-bodied dune harpy
    m = Model()
    for nm, x in (('legA', -2), ('legB', 1)):
        p = m.part(nm, (x + 0.5, 17, 0))
        p.box(x, 17, -1, 1, 5, 2, 'claw'); p.box(x - 1, 22, -3, 3, 2, 5, 'claw')
    b = m.part('body', (0, 6, 0))
    b.box(-3, 6, -2, 6, 11, 4, 'skin')
    b.box(-3.5, 12, -2.5, 7, 6, 5, 'feather_brown')
    for nm, x in (('armB', -5), ('armA', 3)):
        m.part(nm, (x + 1, 7, 0)).box(x, 6, -1, 2, 8, 2, 'skin')
    hp = m.part('head', (0, 6, 0))
    hp.box(-2.5, 0, -2.5, 5, 6, 5, 'skin', eyes=[('front', 1, 3), ('front', 3, 3)])
    hp.box(-3, -1, -3, 6, 2, 6, 'feather_dark'); hp.box(-3, 0, 2, 6, 7, 2, 'feather_dark')
    m.part('tail', (0, 16, 2), rot=(-0.3, 0, 0)).box(-2.5, 16, 2, 5, 1, 8, 'feather_brown')
    wing(m, 'wingP', 1, (3, 7, 1), 16, 8, 'feather_brown', 'feather_dark')
    wing(m, 'wingN', -1, (-3, 7, 1), 16, 8, 'feather_brown', 'feather_dark')
    return m
