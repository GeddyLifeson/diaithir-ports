"""Site people data for Diaithir Compat's /sitepeople command, from DiaithirRPG world/earth/age1 and tools/culture_dress.csv.

python diaithir-compat/tools/gen_people.py <DiaithirRPG checkout>    (needs numpy and Pillow)

Writes, under src/main/resources/:
- data/diaithircompat/site_people.json: per culture (CULTURE_KITS.csv) its kit, main-hand item, carried items and vanilla
  villager type; per kit its MCA clothing ids, everyday ones by gender and one each for the ruler, warrior and priest roles;
  per site type how many people stand there (the table in PEOPLE_SPEC.md). Every culture is its own kit, dressed in its own
  materials and colours from culture_dress.csv (garment hide or fibre, ornament, pigment, each with its source);
- data/diaithircompat/skins/clothing/<kit>.json: MCA Reborn clothing list entries, one per texture. Their profession is
  diaithircompat:<kit>, which no villager has, so MCA never puts them on its own villagers; /sitepeople sets them directly;
- assets/diaithircompat/skins/clothing/normal/<kit>/<gender>/<n>.png and <role>.png: 64x clothing (512x512 on the player
  skin layout) painted fresh by outfits/clothing.py from the culture's garments, ornament and colours: three everyday
  looks (0 plain, 1 another pattern or cloak, 2 work wear) and the ruler, warrior and priest. The roles are design
  choices; Age I has elders and hunters, not kings. Occlusion comes from outfits/ao.png (outfits/ao_bake.py).

Kit items must be things an Age I person could carry: no metal, no later weapons (LATER_AGE fails the run).
"""
import csv, json, re, shutil, sys
from pathlib import Path

HERE = Path(__file__).resolve().parent
sys.path.insert(0, str(HERE / "outfits"))
import clothing

RES = HERE.parent / "src" / "main" / "resources"
NS = "diaithircompat"
LATER_AGE = re.compile(r"iron|gold|copper|diamond|netherite|emerald|chain|crossbow|shield|trident|mace|lantern|book|paper|glass|brick|bread|map|compass|clock|bucket|shears|flint_and_steel|minecart|boat")
STACK = {"minecraft:arrow": 8, "minecraft:flint": 4, "minecraft:bone": 2, "minecraft:feather": 4, "minecraft:string": 2}


ROLES = ("ruler", "warrior", "priest")


def slug(culture, taken):
    s = re.sub(r"[^a-z0-9]+", "_", culture.lower()).strip("_")[:40].strip("_")
    while s in taken:
        s += "_"
    taken.add(s)
    return s


def people_counts(spec):
    counts = {}
    for line in spec.read_text(encoding="utf-8").splitlines():
        m = re.match(r"\s*\|\s*([a-z_]+)\s*\|\s*(\d+)-(\d+)\s*\|", line)
        if m:
            counts[m.group(1)] = [int(m.group(2)), int(m.group(3))]
    assert len(counts) >= 11, counts
    return counts


def main():
    repo = Path(sys.argv[1])
    for old in (RES / "assets" / NS / "skins" / "clothing" / "normal", RES / "data" / NS / "skins" / "clothing"):
        shutil.rmtree(old, ignore_errors=True)         # kits are rebuilt whole; a renamed kit leaves nothing behind
    age1 = repo / "world" / "earth" / "age1"
    rows = list(csv.DictReader(open(age1 / "CULTURE_KITS.csv", newline="", encoding="utf-8")))
    dress = {r["culture"]: r for r in csv.DictReader(open(HERE / "culture_dress.csv", newline="", encoding="utf-8"))}
    assert set(dress) == {r["culture"] for r in rows}, set(dress) ^ {r["culture"] for r in rows}
    taken, kits = set(), {}
    for r in rows:
        r["kit"] = slug(r["culture"], taken)
        kits[r["kit"]] = dress[r["culture"]]
    cultures = {}
    for r in rows:
        items = [r["mainhand"]] + r["carried"].split()
        bad = [i for i in items if LATER_AGE.search(i)]
        assert not bad, (r["culture"], bad)
        cultures[r["culture"]] = {"kit": r["kit"], "mainhand": r["mainhand"],
                                  "carried": [{"item": i, "count": STACK.get(i, 1)} for i in r["carried"].split()],
                                  "villager_type": "minecraft:" + r["vanilla_villager_type"]}
    out_kits = {}
    ao = clothing.load_ao(HERE / "outfits" / "ao.png")
    for kit, r in sorted(kits.items()):
        entries, clothing_ids, roles = [], {}, {}

        def save(img, gender, name):
            path = f"skins/clothing/normal/{kit}/{gender}/{name}.png"
            dest = RES / "assets" / NS / path
            dest.parent.mkdir(parents=True, exist_ok=True)
            clothing.indexed(img).save(dest, optimize=True, transparency=255)
            cid = f"{NS}:{path}"
            entries.append({"id": cid, "profession": f"{NS}:{kit}", "chance": 1.0, "gender": gender})
            return cid
        for gender in ("male", "female"):
            for n in range(3):
                clothing_ids.setdefault(gender, []).append(save(clothing.render(r, gender, n, ao), gender, str(n)))
            for role in ROLES:
                roles.setdefault(role, {})[gender] = save(clothing.render(r, gender, role, ao), gender, role)
        lst = RES / "data" / NS / "skins" / "clothing" / f"{kit}.json"
        lst.parent.mkdir(parents=True, exist_ok=True)
        lst.write_text(json.dumps(entries, indent=2) + "\n", encoding="utf-8")
        out_kits[kit] = {"clothing": clothing_ids, "roles": roles}
    data = {"_comment": "Written by diaithir-compat/tools/gen_people.py from DiaithirRPG world/earth/age1 and tools/culture_dress.csv; edit those.",
            "counts": people_counts(age1 / "PEOPLE_SPEC.md"), "kits": out_kits,
            "cultures": dict(sorted(cultures.items()))}
    (RES / "data" / NS / "site_people.json").write_text(json.dumps(data, indent=2) + "\n", encoding="utf-8")
    for old in ("LICENSE.txt", "GPL-3.0.txt"):          # the old MCA-derived textures are gone, and their licence with them
        (RES / "assets" / NS / "skins" / old).unlink(missing_ok=True)
    print(f"{len(cultures)} cultures, {len(out_kits)} kits, {sum(len(v) for k in out_kits.values() for v in k['clothing'].values()) + sum(len(v) for k in out_kits.values() for v in k['roles'].values())} textures")


if __name__ == "__main__":
    main()
