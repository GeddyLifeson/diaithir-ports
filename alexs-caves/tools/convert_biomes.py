#!/usr/bin/env python3
"""One-off: convert 1.21.1 biome JSON (effects block, dict carvers) to the 26.1 attribute form. Idempotent."""
import json, glob
def hexc(v):
    return v if isinstance(v, str) else '#%06x' % (v & 0xFFFFFF)
for f in glob.glob('src/main/resources/data/alexscaves/worldgen/biome/*.json'):
    d = json.load(open(f))
    e = d.get('effects', {})
    attrs = d.setdefault('attributes', {})
    if 'sky_color' in e: attrs['minecraft:visual/sky_color'] = hexc(e.pop('sky_color'))
    if 'fog_color' in e: attrs['minecraft:visual/fog_color'] = hexc(e.pop('fog_color'))
    if 'water_fog_color' in e: attrs['minecraft:visual/water_fog_color'] = hexc(e.pop('water_fog_color'))
    snd = {}
    if 'ambient_sound' in e: snd['loop'] = e.pop('ambient_sound')
    if 'additions_sound' in e: snd['additions'] = e.pop('additions_sound')
    if 'mood_sound' in e: snd['mood'] = e.pop('mood_sound')
    if snd: attrs['minecraft:audio/ambient_sounds'] = snd
    if 'music' in e:
        m = e.pop('music')
        if isinstance(m, dict) and 'data' in m: m = m['data']
        attrs['minecraft:audio/background_music'] = {'default': m}
    if 'particle' in e:
        p = e.pop('particle')
        attrs['minecraft:visual/ambient_particles'] = [{'particle': p['options'], 'probability': p['probability']}]
    for k in ('water_color', 'foliage_color', 'grass_color', 'dry_foliage_color'):
        if k in e: e[k] = hexc(e[k])
    d['effects'] = e
    c = d.get('carvers')
    if isinstance(c, dict):
        out = []
        for v in c.values():
            out += v if isinstance(v, list) else [v]
        d['carvers'] = out
    json.dump(d, open(f, 'w'), indent=2); print('converted', f)
