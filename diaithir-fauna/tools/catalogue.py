"""Loads /mnt/project-files/animals-fossils/catalogue/*.psv into dicts. Used by every generator."""
import glob, json, os
CAT = os.environ.get('FAUNA_CATALOGUE', '/mnt/project-files/animals-fossils')
MOB = 'id common latin plan uwkey range ext scale diet habitat kg notes'.split()
FOS = 'id common latin plan period ma_from ma_to rock region formation scale kg diet dna_tier notes'.split()
REGIONS = json.load(open(os.path.join(CAT, 'regions.json')))

def rows(kind):
    out = []
    for fn in sorted(glob.glob(os.path.join(CAT, 'catalogue', '*.psv'))):
        if os.path.basename(fn).startswith('coat_'): continue
        fossil = 'fossil' in os.path.basename(fn)
        if fossil != (kind == 'fossil'): continue
        for l in open(fn, encoding='utf8'):
            l = l.rstrip('\n')
            if not l.strip() or l.startswith('#'): continue
            f = l.split('|')
            d = dict(zip(FOS if fossil else MOB, f)); d['file'] = os.path.basename(fn)
            out.append(d)
    return out

def boxes(rng):
    """Region list string -> [{'lat':[a,b],'lon':[c,d]}]"""
    out = []
    for r in rng.split(';'):
        r = r.strip()
        if r.startswith('box('):
            a, b, c, d = (float(x) for x in r[4:-1].split(','))
            out.append({'lat': [a, b], 'lon': [c, d]})
        else:
            out += REGIONS[r]
    return out
