#!/usr/bin/env python3
"""Synthesises every sound of diaithir-firearms (original audio, no samples). Usage: python3 -I tools/gen_sounds.py <module dir>
numpy for the waveforms, ffmpeg (libvorbis) for the .ogg files, mono 44.1 kHz. Writes assets/diaithirfirearms/sounds/*.ogg and sounds.json."""
import json, os, subprocess, sys, tempfile
import numpy as np

M = sys.argv[1] if len(sys.argv) > 1 else '.'
OUT = f'{M}/src/main/resources/assets/diaithirfirearms'
SR = 44100
rng = np.random.default_rng(1151)

def t(d): return np.arange(int(SR * d)) / SR
def env(x, a=0.002, k=30.0): return np.minimum(1, x / a) * np.exp(-k * x)
def lowpass(sig, fc):
    a = np.exp(-2 * np.pi * fc / SR); out = np.empty_like(sig); y = 0.0
    for i, v in enumerate(sig): y = (1 - a) * v + a * y; out[i] = y
    return out
def noise(d): return rng.standard_normal(int(SR * d))
def norm(s, peak=0.85): return s / max(1e-6, np.max(np.abs(s))) * peak

def shot(d, thump_f, crack, k, body):
    x = t(d)
    n = noise(d)
    crackp = env(x, 0.0005, k * 1.8) * n * crack                         # bright crack
    bod = lowpass(env(x, 0.001, k) * n, body) * 2.2                      # muffled body
    sweep = np.sin(2 * np.pi * np.cumsum(thump_f * np.exp(-9 * x) + 38) / SR) * env(x, 0.002, k * 0.55)  # thump
    tail = lowpass(noise(d) * np.exp(-3.2 * x) * 0.12, 1800)             # room tail
    return norm(crackp * 0.6 + bod + sweep * 1.3 + tail)

def energy(d, f0, f1):
    x = t(d); f = f0 + (f1 - f0) * (x / d) ** 0.6
    ph = np.cumsum(f) / SR * 2 * np.pi
    s = np.sin(ph + 2.2 * np.sin(ph * 0.5)) * env(x, 0.004, 7) + 0.25 * np.sin(ph * 2.01) * env(x, 0.004, 11)
    return norm(s + 0.05 * noise(d) * env(x, 0.001, 40))

def click(f, d=0.08, k=55):
    x = t(d); return env(x, 0.0005, k) * (np.sin(2 * np.pi * f * x) * 0.6 + noise(d) * 0.5)

def seq(parts, total):
    out = np.zeros(int(SR * total))
    for at, s in parts:
        i = int(SR * at); out[i:i + len(s)] += s[:max(0, len(out) - i)]
    return norm(out, 0.7)

SOUNDS = {
 'shot_pistol': shot(0.45, 260, 0.9, 30, 3500),
 'shot_smg': shot(0.40, 220, 0.8, 34, 3000),
 'shot_rifle': shot(0.65, 170, 1.0, 22, 2600),
 'shot_shotgun': shot(0.75, 140, 1.2, 17, 2200),
 'shot_sniper': shot(1.10, 110, 1.3, 10, 1800),
 'shot_lmg': shot(0.50, 150, 0.9, 26, 2300),
 'shot_launcher': norm(lowpass(noise(1.2) * env(t(1.2), 0.02, 3.5), 900) * 3 + np.pad(shot(0.6, 90, 0.3, 8, 800), (0, int(SR*0.6))) * 0.8),
 'shot_energy': energy(0.55, 1800, 260),
 'reload_start': seq([(0.0, click(1900)), (0.12, click(900, 0.1)), (0.3, click(2600))], 0.55),
 'reload_end': seq([(0.0, click(700, 0.12)), (0.11, click(2300)), (0.2, click(1500, 0.14, 35))], 0.45),
 'dry_fire': seq([(0.0, click(1200, 0.06, 70))], 0.1),
 'attach': seq([(0.0, click(1400, 0.07)), (0.07, click(2000, 0.05))], 0.2),
}
os.makedirs(f'{OUT}/sounds', exist_ok=True)
for name, s in SOUNDS.items():
    with tempfile.NamedTemporaryFile(suffix='.raw') as f:
        f.write((np.clip(s, -1, 1) * 32767).astype('<i2').tobytes()); f.flush()
        subprocess.run(['ffmpeg', '-v', 'error', '-y', '-f', 's16le', '-ar', str(SR), '-ac', '1', '-i', f.name, '-c:a', 'libvorbis', '-q:a', '4',
                        f'{OUT}/sounds/{name}.ogg'], check=True)
json.dump({n: {'sounds': [{'name': f'diaithirfirearms:{n}', 'stream': False}], 'subtitle': f'subtitles.diaithirfirearms.{n}'} for n in SOUNDS},
          open(f'{OUT}/sounds.json', 'w'), indent=1)
print(len(SOUNDS), 'sounds')
