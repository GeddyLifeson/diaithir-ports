"""Synthesised sound effects (numpy -> wav -> ogg). Every sample is generated from scratch."""
import numpy as np, wave, subprocess, os, hashlib, re
SR = 22050

def rs(name): return np.random.RandomState(int(hashlib.md5(name.encode()).hexdigest()[:8], 16))

def env(n, a=0.005, d=0.1):
    t = np.arange(n) / SR
    e = np.minimum(t / max(a, 1e-4), 1) * np.exp(-t / d)
    return e

def lp(x, k):
    k = max(1, int(k)); return np.convolve(x, np.ones(k) / k, mode="same")

def noise(n, r): return r.uniform(-1, 1, n)

def tone(freqs, n, amps=None):
    t = np.arange(n) / SR
    amps = amps or [1] * len(freqs)
    return sum(a * np.sin(2 * np.pi * f * t) for f, a in zip(freqs, amps))

def whistle(name, r):
    low = "low" in name; high = "high" in name
    f = 330 if low else 880 if high else 560
    dur = 0.5 if name.endswith("end") else 1.1
    n = int(SR * dur); t = np.arange(n) / SR
    vib = 1 + 0.01 * np.sin(2 * np.pi * 5 * t)
    x = tone([f, f * 2, f * 3], n, [1, 0.4, 0.2]) * vib * 0.5 + 0.12 * lp(noise(n, r), 3)
    return x * np.minimum(t / 0.05, 1) * np.exp(-np.maximum(t - dur * 0.5, 0) / (0.2 if dur < 1 else 0.35))

def bell(name, r):
    f = 520 if "desk" in name else 330 if "peculiar" in name else 240 if "haunted" in name else 660
    n = int(SR * 1.2)
    x = tone([f, f * 2.76, f * 5.4], n, [1, 0.5, 0.25]) * env(n, 0.002, 0.35)
    return x * 0.6

def click(name, r):
    n = int(SR * 0.12)
    return (lp(noise(n, r), 3) * env(n, 0.001, 0.02) + 0.5 * tone([900 + r.randint(0, 300)], n) * env(n, 0.001, 0.03)) * 0.7

def thump(name, r):
    n = int(SR * 0.35); t = np.arange(n) / SR
    f = 110 * np.exp(-t * 9) + 45
    x = np.sin(2 * np.pi * np.cumsum(f) / SR) * env(n, 0.002, 0.12) + 0.4 * lp(noise(n, r), 6) * env(n, 0.001, 0.05)
    return x * 0.8

def hiss(name, r):
    n = int(SR * 0.7)
    x = noise(n, r); x = x - lp(x, 12)
    return x * env(n, 0.03, 0.3) * 0.5

def grind(name, r):
    n = int(SR * 0.8); t = np.arange(n) / SR
    x = lp(noise(n, r), 4) * (0.6 + 0.4 * np.sin(2 * np.pi * 38 * t)) * np.minimum(t / 0.05, 1) * np.exp(-np.maximum(t - 0.5, 0) / 0.15)
    return x * 0.6

def train(name, r):
    n = int(SR * 0.9); x = np.zeros(n)
    for k in range(4):
        i = int(k * SR * 0.2); m = int(SR * 0.08)
        x[i:i + m] += lp(noise(m, r), 5) * env(m, 0.001, 0.02)
    return x * 0.7

def pick(name):
    if "whistle" in name: return whistle
    if any(k in name for k in ("bell", "confirm", "deny", "stock_ticker", "stock_link", "slime")): return bell if "slime" not in name else thump
    if any(k in name for k in ("steam", "fwoomp", "spout", "blaze", "chiff")): return hiss
    if any(k in name for k in ("saw", "sanding", "crushing", "mixing", "cranking", "cogs")): return grind
    if "train" in name: return train
    if any(k in name for k in ("click", "scroll", "clipboard", "controller", "wrench", "funnel", "frogport", "item_hatch", "package", "packager", "depot", "crafter")): return click if any(k in name for k in ("click", "scroll", "clipboard", "wrench", "controller")) else thump
    return thump

def synth(base, outdir):
    r = rs(base)
    x = pick(base)(base, r)
    x = x / max(1e-6, np.abs(x).max()) * 0.8
    wav = f"{outdir}/{base}.wav"
    os.makedirs(outdir, exist_ok=True)
    with wave.open(wav, "wb") as w:
        w.setnchannels(1); w.setsampwidth(2); w.setframerate(SR)
        w.writeframes((x * 32767).astype(np.int16).tobytes())
    subprocess.run(["ffmpeg", "-y", "-loglevel", "error", "-i", wav, "-c:a", "libvorbis", "-q:a", "3", f"{outdir}/{base}.ogg"], check=True)
    os.remove(wav)
