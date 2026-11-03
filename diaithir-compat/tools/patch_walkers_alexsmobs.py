#!/usr/bin/env python3
"""Rewrite five entity ids in Walkers' AlexMobsIntegration so it matches Alex's Mobs Continued.

Walkers 8.2 asks the registry for crimson-mosquito, giant-squid, komodo-dragon (hyphens), crocodile and sunbird_scorch.
Alex's Mobs registers crimson_mosquito, giant_squid, komodo_dragon, caiman (the crocodile was renamed) and sunbird,
so Walkers' trait reload throws NoSuchElementException and the whole datapack load fails when both mods
are present. The class's CONSTANT_Utf8 entries (tag 1, u2 length, bytes) are rewritten with their lengths;
nothing else changes.
usage: patch_walkers_alexsmobs.py in.jar out.jar
"""
import sys, zipfile
CLS = "dev/tocraft/walkers/integrations/impl/AlexMobsIntegration.class"
def utf8(t):
    return b"\x01" + len(t).to_bytes(2, "big") + t
SUBS = [(utf8(a), utf8(b)) for a, b in [(b"crimson-mosquito", b"crimson_mosquito"), (b"giant-squid", b"giant_squid"),
        (b"komodo-dragon", b"komodo_dragon"), (b"crocodile", b"caiman"), (b"sunbird_scorch", b"sunbird")]]
src, dst = sys.argv[1], sys.argv[2]
with zipfile.ZipFile(src) as zi, zipfile.ZipFile(dst, "w", zipfile.ZIP_DEFLATED) as zo:
    for info in zi.infolist():
        data = zi.read(info.filename)
        if info.filename == CLS:
            for a, b in SUBS:
                assert data.count(a) == 1, a
                data = data.replace(a, b)
        zo.writestr(info, data)
print("patched", dst)
