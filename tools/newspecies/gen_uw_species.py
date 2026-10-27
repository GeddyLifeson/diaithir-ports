#!/usr/bin/env python3
"""Catalogue 'uw:' plans -> new Untamed Wilds species: entity JSON entries, recoloured skins, VariantLook.java,
lang, earth_ranges, extinction rows. Idempotent: regenerates from the pristine entity JSON in git HEAD.

    python3 gen_uw_species.py [--only family,family] [--no-skins]
"""
import glob, json, os, subprocess, sys
import numpy as np
from PIL import Image

HERE = os.path.dirname(os.path.abspath(__file__))
ROOT = os.path.abspath(os.path.join(HERE, '..', '..'))
sys.path.insert(0, '/home/user/diaithir-ports/diaithir-fauna/tools')
import catalogue as C

RES = os.path.join(ROOT, 'src/main/resources')
ENT = os.path.join(RES, 'data/untamedwilds/entities')
TEX = os.path.join(RES, 'assets/untamedwilds/textures/entity')
LANG = os.path.join(RES, 'assets/untamedwilds/lang/en_us.json')
ER = os.path.join(RES, 'data/untamedwilds/earth_ranges')
JAVA = os.path.join(ROOT, 'src/main/java/untamedwilds/util/VariantLook.java')
EXT_OUT = os.path.join(ROOT, 'tools/newspecies/out')
LC_OFFSET = 12923      # AD year -> engine LC year (LC 14,949 = AD 2026)
FADE = 600
GATED = {'cervid', 'equid', 'elephantid', 'canid', 'bovid', 'glyptodont'}
SALAD = 100            # virtual bovid look: saiga horns, no saiga nose

HAB = {
    'forest': ['tag|is_forest'], 'jungle': ['tag|is_jungle'], 'plains': ['tag|is_plains'], 'grassland': ['tag|is_plains'],
    'savanna': ['tag|is_savanna'], 'taiga': ['tag|is_taiga'], 'swamp': ['tag|is_swamp'], 'wetland': ['tag|is_swamp'],
    'desert': ['tag|is_desert'], 'badlands': ['tag|is_badlands'], 'mountain': ['tag|is_mountain'],
    'icy': ['tag|is_icy'], 'tundra': ['tag|is_icy'], 'river': ['tag|is_river'], 'lake': ['tag|is_river'],
    'cave': ['tag|is_underground'],
    'reef': ['resource|minecraft:warm_ocean', 'resource|minecraft:lukewarm_ocean'],
    'ocean_warm': ['resource|minecraft:warm_ocean', 'resource|minecraft:lukewarm_ocean', 'resource|minecraft:deep_lukewarm_ocean'],
    'ocean_cold': ['resource|minecraft:cold_ocean', 'resource|minecraft:deep_cold_ocean'],
    'ocean_deep': ['resource|minecraft:deep_ocean', 'resource|minecraft:deep_cold_ocean'],
    'coast': ['resource|minecraft:ocean', 'resource|minecraft:lukewarm_ocean'],
}


def hexrgb(h):
    return np.array([int(h[i:i + 2], 16) for i in (1, 3, 5)], np.float32)


def read_coats():
    coat = {}
    for fn in glob.glob(os.path.join(C.CAT, 'catalogue', 'coat_*.psv')):
        for l in open(fn, encoding='utf8'):
            if l.strip() and not l.startswith('#'):
                f = l.rstrip('\n').split('|')
                coat[f[0]] = f
    return coat


def git_head_json(path):
    rel = os.path.relpath(path, ROOT)
    return json.loads(subprocess.check_output(['git', '-C', ROOT, 'show', 'f2d7b5c:' + rel]).decode('utf8'))


def donor_png(fam, sp):
    d = os.path.join(TEX, fam)
    for n in (sp, sp + '_1', sp + '_male', sp + '_1_male', sp + '_female', sp + '_1_female'):
        p = os.path.join(d, n + '.png')
        if os.path.exists(p):
            return p
    raise FileNotFoundError(fam + '/' + sp)


_donor_cache = {}


def recolor(donor_path, base, accent, belly):
    """Luminance-preserving three-role recolour of a donor skin (same UV, same alpha)."""
    if donor_path not in _donor_cache:
        im = Image.open(donor_path).convert('RGBA')
        a = np.asarray(im, np.float32)
        rgb, al = a[..., :3], a[..., 3]
        m = al > 0
        px = rgb[m]
        lum = px @ np.array([.299, .587, .114], np.float32)
        # 3-means in RGB seeded at luminance percentiles
        order = np.argsort(lum)
        cen = np.stack([px[order[int(len(px) * q)]] for q in (.12, .5, .88)]) if len(px) else np.zeros((3, 3), np.float32)
        sub = px[::max(1, len(px) // 20000)]
        for _ in range(12):
            d = ((sub[:, None, :] - cen[None]) ** 2).sum(-1)
            lab = d.argmin(1)
            for k in range(3):
                if (lab == k).any():
                    cen[k] = sub[lab == k].mean(0)
        cl = cen @ np.array([.299, .587, .114], np.float32)
        o = np.argsort(cl)
        cen, cl = cen[o], cl[o]
        _donor_cache[donor_path] = (im.size, rgb, al, m, cen, cl)
    size, rgb, al, m, cen, cl = _donor_cache[donor_path]
    lum = rgb @ np.array([.299, .587, .114], np.float32)
    spread = cl[2] - cl[0]
    roles = np.stack([hexrgb(accent), hexrgb(base), hexrgb(belly)])
    d = ((rgb[..., None, :] - cen[None, None]) ** 2).sum(-1)
    if spread < 45:          # near-uniform donor: base only
        w = np.zeros_like(d); w[..., 1] = 1
    else:
        tau = max(spread, 60.0) ** 2 * 0.06
        w = np.exp(-(d - d.min(-1, keepdims=True)) / tau)
        w /= w.sum(-1, keepdims=True)
    target = (w[..., None] * roles[None, None]).sum(2)
    ref = (w * cl[None, None]).sum(-1)
    tl = target @ np.array([.299, .587, .114], np.float32)
    # keep donor's shading: output luminance = role luminance * (donor lum / donor cluster lum)
    k = (lum + 6) / (ref + 6)
    out = target * np.clip(k, 0.15, 2.2)[..., None]
    # keep very dark detail (eyes, nostrils, claws) and bright whites (teeth, highlights) as in the donor
    dark = (lum < 30)[..., None]
    white = ((lum > 225) & (rgb.max(-1) - rgb.min(-1) < 25))[..., None]
    out = np.where(dark, rgb, out)
    out = np.where(white, rgb, out)
    out = np.clip(out, 0, 255)
    res = np.concatenate([out, al[..., None]], -1).astype(np.uint8)
    return Image.fromarray(res, 'RGBA')


def save_png(im, path):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    try:
        im.quantize(256, method=Image.FASTOCTREE, dither=Image.NONE).save(path, optimize=True)
    except Exception:
        im.save(path, optimize=True)


def spawn_biomes(habitat, donor_sp):
    out, seen = [], set()
    for h in habitat.split(';'):
        for t in HAB.get(h.strip(), []):
            if t not in seen:
                seen.add(t); out.append([t])
    return out or [list(g) for g in donor_sp.get('spawnBiomes', [])] or [['tag|is_forest']]


def main():
    only = None
    skins = '--no-skins' not in sys.argv
    if '--only' in sys.argv:
        only = set(sys.argv[sys.argv.index('--only') + 1].split(','))
    coat = read_coats()
    rows = [r for r in C.rows('mob') if r['plan'].startswith('uw:') and r['uwkey'] == '-']
    fams = {}
    for r in rows:
        fams.setdefault(r['plan'][3:], []).append(r)
    lang = json.load(open(LANG, encoding='utf8'))
    os.makedirs(ER, exist_ok=True); os.makedirs(EXT_OUT, exist_ok=True)
    looks = {}      # family -> {variant: look}
    ext_rows, ext_variants, report, over = [], {}, {}, []
    for fam, rs in sorted(fams.items()):
        if only and fam not in only:
            continue
        path = os.path.join(ENT, fam + '.json')
        data = git_head_json(path)
        sp = data['species']
        byname = {s['name']: s for s in sp}
        base_n = len(sp)
        ranges = []
        added = 0
        for r in rs:
            sid = r['id']
            if sid in byname:
                print('skip existing name', fam, sid); continue
            c = coat[sid]
            dfam, dsp = c[1].split('/')
            if dfam != fam:
                print('donor family mismatch', sid, c[1], fam); dfam = fam
            donor = byname.get(dsp)
            if donor is None:
                print('missing donor', sid, c[1]); continue
            v = base_n + added
            if v >= 99:      # variant 99 is the 'no species for this biome' sentinel in FaunaSpawn
                print('over cap', fam, sid); over.append(fam + '/' + sid); continue
            added += 1
            sc = round(max(0.1, min(float(r['scale']), 4.0)), 3)
            e = {'name': sid, 'variant': v, 'scale': sc}
            dsc = donor.get('scale', data.get('scale', 1.0)) or 1.0
            e['rarity'] = int(max(1, min(donor.get('rarity', 3), 4)))
            for k in ('breeding', 'growing_time', 'offspring', 'activityType', 'favourite_food', 'sounds'):
                if k in donor:
                    e[k] = donor[k]
            notes = {n.strip().split(' ')[0].lower() for n in r['notes'].split(',')}
            if 'nocturnal' in notes:
                e['activityType'] = 'nocturnal'
            elif 'diurnal' in notes:
                e['activityType'] = 'diurnal'
            f = {k: x for k, x in donor.get('flags', {}).items() if k != 'dimorphism'}
            if 'herd' in notes or 'pack' in notes:
                f.setdefault('groupCount', 4)
            elif 'groupCount' in f and not ({'herd', 'pack'} & notes):
                f.pop('groupCount')
            e['flags'] = f
            ratio = max(0.4, min((sc / dsc) ** 0.8, 2.0))
            for k in ('attack', 'health'):
                if k in donor:
                    e[k] = round(donor[k] * ratio) if float(donor[k]).is_integer() else round(donor[k] * ratio, 1)
            e['spawnBiomes'] = spawn_biomes(r['habitat'], donor)
            sp.append(e)
            byname[sid] = e
            # look (model gating)
            if fam in GATED:
                lk = donor['variant']
                if fam == 'bovid' and lk == 2 and not r['latin'].lower().startswith('saiga'):
                    lk = SALAD
                looks.setdefault(fam, {})[v] = lk
            # skin
            if skins:
                im = recolor(donor_png(fam, dsp), c[2], c[3], c[4])
                save_png(im, os.path.join(TEX, fam, sid + '.png'))
            # lang
            lang['entity.untamedwilds.%s_%s' % (fam, sid)] = r['common'][0].upper() + r['common'][1:]
            lang['entity.untamedwilds.%s_%s.sciname' % (fam, sid)] = r['latin']
            # earth range
            bx = C.boxes(r['range'])
            ranges.append({'entity': 'untamedwilds:' + fam, 'variant': v, 'species': sid,
                           'regions': [x.strip() for x in r['range'].split(';')],
                           'note': '%s (%s), catalogue range' % (r['common'], r['latin']), 'boxes': bx})
            if r['ext'] != '-':
                lc = int(r['ext']) + LC_OFFSET
                ext_rows.append([lc - FADE, lc, ['untamedwilds:%s_%s' % (fam, sid)]])
                ext_variants['untamedwilds:%s_%s' % (fam, sid)] = ['untamedwilds:' + fam, v]
        json.dump(data, open(path, 'w', encoding='utf8'), indent=2, ensure_ascii=False)
        open(path, 'a').write('\n')
        json.dump({'_comment': 'Natural spawns of these untamedwilds:%s species are limited to the boxes (lat, lon degrees). Generated by tools/newspecies/gen_uw_species.py from the animals-fossils catalogue.' % fam,
                   'ranges': ranges}, open(os.path.join(ER, 'untamedwilds_%s_new.json' % fam), 'w', encoding='utf8'), indent=1)
        report[fam] = added
        print(fam, added, flush=True)
    json.dump(lang, open(LANG, 'w', encoding='utf8'), indent=2, ensure_ascii=False)
    open(LANG, 'a').write('\n')
    write_java(looks)
    ext_rows.sort()
    with open(os.path.join(EXT_OUT, 'extinctions_uw.js'), 'w') as fh:
        fh.write("// Written by tools/newspecies/gen_uw_species.py: new Untamed Wilds species that die out on their real dates.\n"
                 "// Rows are [fadeStartLc, extinctLc, [ids]] like extinctions_data.js; EXT_VARIANTS maps an id to [entity type, VARIANT].\n"
                 "// Loads after extinctions_data.js (alphabetical) and appends to it.\n"
                 "global.etNS.EXTINCTIONS = (global.etNS.EXTINCTIONS || []).concat(%s)\n"
                 "global.etNS.EXT_VARIANTS = Object.assign(global.etNS.EXT_VARIANTS || {}, %s)\n" % (json.dumps(ext_rows), json.dumps(ext_variants)))
    print('over cap:', over)
    print('total', sum(report.values()), 'extinct rows', len(ext_rows))


def write_java(looks):
    lines = ['package untamedwilds.util;', '',
             '/** Generated by tools/newspecies/gen_uw_species.py. Species added after the original Untamed Wilds roster borrow the body parts',
             ' *  (antlers, horns, ears, manes, tusks) of a donor species: {@link #look} maps a variant number to the variant whose SET arrays apply. */',
             'public final class VariantLook {', '    private VariantLook() {}', '',
             '    /** Bovid look for antelope and gazelle that share the saiga horns but not its inflated nose. */',
             '    public static final int SAIGA_HORNS = %d;' % SALAD, '']
    for fam in sorted(GATED):
        m = looks.get(fam, {})
        if m:
            start = min(m)
            arr = ', '.join(str(m[i]) for i in range(start, max(m) + 1))
            lines.append('    private static final int %s_FIRST = %d;' % (fam.upper(), start))
            lines.append('    private static final int[] %s = {%s};' % (fam.upper(), arr))
        else:
            lines.append('    private static final int %s_FIRST = Integer.MAX_VALUE;' % fam.upper())
            lines.append('    private static final int[] %s = {};' % fam.upper())
    lines += ['', '    public static int look(String family, int variant) {', '        switch (family) {']
    for fam in sorted(GATED):
        lines.append('            case "%s": return variant >= %s_FIRST && variant - %s_FIRST < %s.length ? %s[variant - %s_FIRST] : variant;' % (fam, fam.upper(), fam.upper(), fam.upper(), fam.upper(), fam.upper()))
    lines += ['            default: return variant;', '        }', '    }', '}', '']
    open(JAVA, 'w').write('\n'.join(lines))


if __name__ == '__main__':
    main()
