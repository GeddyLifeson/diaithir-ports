"""Sounds and particles: a subset of the original sounds (creatures, casting, altar) and every particle whose frames exist."""
import json, os, shutil, sys, glob
UP = sys.argv[1]
ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
RES = os.path.join(ROOT, "src/main/resources")
A = os.path.join(RES, "assets/goety")
SRCA = os.path.join(UP, "src/main/resources/assets/goety")
PREFIX = ("wraith", "necromancer", "cast_spell", "summon_spell", "prepare_spell", "prepare_summon", "skull_lord_ambient", "skull_lord_hurt",
          "skull_lord_death", "skull_lord_fly", "reaper_", "soul_heal", "soul_bolt", "spell_fail", "altar_", "soul_explode")
src = json.load(open(os.path.join(SRCA, "sounds.json")))
out, ids = {}, []
for k, v in src.items():
    if not k.startswith(PREFIX): continue
    keep = []
    for s in v["sounds"]:
        n = s["name"] if isinstance(s, dict) else s
        rel = n.split(":")[-1]
        f = os.path.join(SRCA, "sounds", rel + ".ogg")
        if os.path.exists(f):
            os.makedirs(os.path.dirname(os.path.join(A, "sounds", rel + ".ogg")), exist_ok=True)
            shutil.copy(f, os.path.join(A, "sounds", rel + ".ogg")); keep.append(s)
    if keep:
        out[k] = {"sounds": keep, "subtitle": "subtitles.goety." + k}; ids.append(k)
json.dump(out, open(os.path.join(A, "sounds.json"), "w"), indent=1)
json.dump(ids, open(os.path.join(RES, "goety_spec/sounds.json"), "w"))
lang = json.load(open(os.path.join(A, "lang/en_us.json")))
for k in ids: lang["subtitles.goety." + k] = k.replace("_", " ").capitalize()
json.dump(lang, open(os.path.join(A, "lang/en_us.json"), "w"), indent=1, ensure_ascii=False)
parts = []
for f in sorted(glob.glob(SRCA + "/particles/*.json")):
    n = os.path.basename(f)[:-5]
    j = json.load(open(f))
    tex = [t.split(":")[-1] for t in j.get("textures", [])]
    if not tex or not all(os.path.exists(os.path.join(SRCA, "textures/particle", t + ".png")) for t in tex): continue
    for t in tex:
        os.makedirs(os.path.join(A, "textures/particle"), exist_ok=True)
        shutil.copy(os.path.join(SRCA, "textures/particle", t + ".png"), os.path.join(A, "textures/particle", t + ".png"))
    os.makedirs(os.path.join(A, "particles"), exist_ok=True)
    json.dump({"textures": ["goety:" + t for t in tex]}, open(os.path.join(A, "particles", n + ".json"), "w"), indent=1)
    parts.append(n)
json.dump(parts, open(os.path.join(RES, "goety_spec/particles.json"), "w"))
print("sounds", len(ids), "particles", len(parts))
