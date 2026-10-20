"""Site people data for Diaithir Compat's /sitepeople command, from DiaithirRPG world/earth/age1.

python diaithir-compat/tools/gen_people.py <DiaithirRPG checkout> <MCA Reborn jar>

Writes, under src/main/resources/:
- data/diaithircompat/site_people.json: per culture (CULTURE_KITS.csv) its kit, main-hand item, carried items and vanilla
  villager type; per kit its MCA clothing ids; per site type how many people stand there (the table in PEOPLE_SPEC.md);
- data/diaithircompat/skins/clothing/<kit>.json: MCA Reborn clothing list entries, one per texture. Their profession is
  diaithircompat:<kit>, which no villager has, so MCA never puts them on its own villagers; /sitepeople sets them directly;
- assets/diaithircompat/skins/clothing/normal/<kit>/<gender>/<n>.png: MCA Reborn's own clothing textures (BASES) recoloured
  in the kit's three colours. The most common colour of a garment takes the kit's main colour, the next its accent and the
  rest its trim, each pixel keeping its shading. These textures are derived from MCA Reborn and stay GPL-3.0
  (assets/diaithircompat/skins/LICENSE.txt and GPL-3.0.txt).

Kit items must be things an Age I person could carry: no metal, no later weapons (LATER_AGE fails the run).
"""
import csv, io, json, re, sys, zipfile
from pathlib import Path

from PIL import Image

HERE = Path(__file__).resolve().parent
RES = HERE.parent / "src" / "main" / "resources"
NS = "diaithircompat"
# MCA Reborn clothing used as the cut for every kit: plain tunics, wraps and dresses without buttons, ties or print.
BASES = {
    "male": ["leatherworker/1", "leatherworker/4", "shepherd/0"],
    "female": ["fletcher/0", "leatherworker/2", "none/6"],
}
LATER_AGE = re.compile(r"iron|gold|copper|diamond|netherite|emerald|chain|crossbow|shield|trident|mace|lantern|book|paper|glass|brick|bread|map|compass|clock|bucket|shears|flint_and_steel|minecart|boat")
STACK = {"minecraft:arrow": 8, "minecraft:flint": 4, "minecraft:bone": 2, "minecraft:feather": 4, "minecraft:string": 2}


def hexrgb(h):
    h = h.lstrip("#")
    return tuple(int(h[i:i + 2], 16) for i in (0, 2, 4))


def lum(c):
    return 0.2126 * c[0] + 0.7152 * c[1] + 0.0722 * c[2] + 1.0


def clusters(colours, k=3, rounds=12):
    """k-means over the garment's colours, weighted by pixel count; returns centres sorted by pixel count, largest first."""
    items = sorted(colours.items(), key=lambda kv: -kv[1])
    centres = []
    for c, _ in items:                                   # seeds: most common colours that differ visibly
        if all(sum((a - b) ** 2 for a, b in zip(c, d)) > 40 ** 2 for d in centres):
            centres.append(c)
        if len(centres) == k:
            break
    while len(centres) < k:
        centres.append(items[0][0])
    for _ in range(rounds):
        acc = [[0, 0, 0, 0] for _ in centres]
        for c, n in items:
            i = min(range(len(centres)), key=lambda j: sum((a - b) ** 2 for a, b in zip(c, centres[j])))
            for t in range(3):
                acc[i][t] += c[t] * n
            acc[i][3] += n
        centres = [tuple(a[t] / a[3] for t in range(3)) if a[3] else centres[i] for i, a in enumerate(acc)]
    weight = [0] * len(centres)
    for c, n in items:
        weight[min(range(len(centres)), key=lambda j: sum((a - b) ** 2 for a, b in zip(c, centres[j])))] += n
    order = sorted(range(len(centres)), key=lambda i: -weight[i])
    return [centres[i] for i in order]


def recolour(img, palette):
    img = img.convert("RGBA")
    px = img.load()
    colours = {}
    for y in range(img.height):
        for x in range(img.width):
            r, g, b, a = px[x, y]
            if a:
                colours[(r, g, b)] = colours.get((r, g, b), 0) + 1
    centres = clusters(colours)
    out = Image.new("RGBA", img.size)
    po = out.load()
    for y in range(img.height):
        for x in range(img.width):
            r, g, b, a = px[x, y]
            if not a:
                continue
            i = min(range(3), key=lambda j: sum((p - q) ** 2 for p, q in zip((r, g, b), centres[j])))
            shade = max(0.55, min(1.35, lum((r, g, b)) / lum(centres[i])))
            po[x, y] = tuple(max(0, min(255, round(v * shade))) for v in palette[i]) + (a,)
    return out


def people_counts(spec):
    counts = {}
    for line in spec.read_text(encoding="utf-8").splitlines():
        m = re.match(r"\s*\|\s*([a-z_]+)\s*\|\s*(\d+)-(\d+)\s*\|", line)
        if m:
            counts[m.group(1)] = [int(m.group(2)), int(m.group(3))]
    assert len(counts) >= 11, counts
    return counts


def main():
    repo, jar = Path(sys.argv[1]), Path(sys.argv[2])
    age1 = repo / "world" / "earth" / "age1"
    rows = list(csv.DictReader(open(age1 / "CULTURE_KITS.csv", newline="", encoding="utf-8")))
    kits = {}
    for r in rows:
        k = kits.setdefault(r["kit"], r)
        assert (k["colour_main"], k["colour_accent"], k["colour_trim"]) == (r["colour_main"], r["colour_accent"], r["colour_trim"]), r["kit"]
    cultures = {}
    for r in rows:
        items = [r["mainhand"]] + r["carried"].split()
        bad = [i for i in items if LATER_AGE.search(i)]
        assert not bad, (r["culture"], bad)
        cultures[r["culture"]] = {"kit": r["kit"], "mainhand": r["mainhand"],
                                  "carried": [{"item": i, "count": STACK.get(i, 1)} for i in r["carried"].split()],
                                  "villager_type": "minecraft:" + r["vanilla_villager_type"]}
    out_kits = {}
    with zipfile.ZipFile(jar) as z:
        for kit, r in sorted(kits.items()):
            palette = [hexrgb(r["colour_main"]), hexrgb(r["colour_accent"]), hexrgb(r["colour_trim"])]
            entries, clothing = [], {}
            for gender, bases in BASES.items():
                for n, base in enumerate(bases):
                    src = Image.open(io.BytesIO(z.read(f"assets/mca/skins/clothing/normal/{gender}/{base}.png")))
                    path = f"skins/clothing/normal/{kit}/{gender}/{n}.png"
                    dest = RES / "assets" / NS / path
                    dest.parent.mkdir(parents=True, exist_ok=True)
                    recolour(src, palette).save(dest, optimize=True)
                    cid = f"{NS}:{path}"
                    clothing.setdefault(gender, []).append(cid)
                    entries.append({"id": cid, "profession": f"{NS}:{kit}", "chance": 1.0, "gender": gender})
            lst = RES / "data" / NS / "skins" / "clothing" / f"{kit}.json"
            lst.parent.mkdir(parents=True, exist_ok=True)
            lst.write_text(json.dumps(entries, indent=2) + "\n", encoding="utf-8")
            out_kits[kit] = {"clothing": clothing}
    data = {"_comment": "Written by diaithir-compat/tools/gen_people.py from DiaithirRPG world/earth/age1; edit those.",
            "counts": people_counts(age1 / "PEOPLE_SPEC.md"), "kits": out_kits,
            "cultures": dict(sorted(cultures.items()))}
    (RES / "data" / NS / "site_people.json").write_text(json.dumps(data, indent=2) + "\n", encoding="utf-8")
    lic = RES / "assets" / NS / "skins" / "LICENSE.txt"
    lic.write_text(
        "The clothing textures under this folder are MCA Reborn's own clothing textures (Minecraft Comes Alive Reborn,\n"
        "https://github.com/Luke100000/minecraft-comes-alive, GPL-3.0), recoloured by diaithir-compat/tools/gen_people.py.\n"
        "They are licensed under the GNU General Public License version 3 (https://www.gnu.org/licenses/gpl-3.0.txt),\n"
        "not under the MIT licence of the rest of Diaithir Compat. The licence text is in GPL-3.0.txt next to this file.\n",
        encoding="utf-8")
    with zipfile.ZipFile(jar) as z:                      # MCA Reborn ships the GPL text in its jar
        (lic.parent / "GPL-3.0.txt").write_bytes(z.read("LICENSE_Minecraft Comes Alive"))
    print(f"{len(cultures)} cultures, {len(out_kits)} kits, {sum(len(v) for k in out_kits.values() for v in k['clothing'].values())} textures")


if __name__ == "__main__":
    main()
