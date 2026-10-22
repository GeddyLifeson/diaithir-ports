"""Site people data for Diaithir Compat's /sitepeople command, from DiaithirRPG world/earth/age1 and tools/culture_dress.csv.

python diaithir-compat/tools/gen_people.py <DiaithirRPG checkout> <MCA Reborn jar>

Writes, under src/main/resources/:
- data/diaithircompat/site_people.json: per culture (CULTURE_KITS.csv) its kit, main-hand item, carried items and vanilla
  villager type; per kit its MCA clothing ids, everyday ones by gender and one each for the ruler, warrior and priest roles;
  per site type how many people stand there (the table in PEOPLE_SPEC.md). Every culture is its own kit, dressed in its own
  materials and colours from culture_dress.csv (garment hide or fibre, ornament, pigment, each with its source);
- data/diaithircompat/skins/clothing/<kit>.json: MCA Reborn clothing list entries, one per texture. Their profession is
  diaithircompat:<kit>, which no villager has, so MCA never puts them on its own villagers; /sitepeople sets them directly;
- assets/diaithircompat/skins/clothing/normal/<kit>/<gender>/<n>.png: MCA Reborn's own clothing textures (BASES) recoloured
  in the culture's three colours. The most common colour of a garment takes the main colour, the next its accent and the
  rest its trim, each pixel keeping its shading. <role>.png adds the role's marks on the first cut: a ruler's bead collar and
  belt in the ornament colour, a warrior's pigment stripes, a priest's painted dots and hem. The roles are design choices;
  Age I has elders and hunters, not kings. These textures are derived from MCA Reborn and stay GPL-3.0
  (assets/diaithircompat/skins/LICENSE.txt and GPL-3.0.txt).

Kit items must be things an Age I person could carry: no metal, no later weapons (LATER_AGE fails the run).
"""
import csv, io, json, re, shutil, sys, zipfile
from pathlib import Path

from PIL import Image

HERE = Path(__file__).resolve().parent
RES = HERE.parent / "src" / "main" / "resources"
NS = "diaithircompat"
# MCA Reborn clothing used as the cuts, by what the culture wore (culture_dress.csv garments): plain coats, tunics, robes,
# dresses and loincloths without buttons, ties or print. Rulers wear a long robe and priests a ritual robe in every culture.
CUTS = {
    "parka": {"male": ["leatherworker/1", "leatherworker/2", "fletcher/2"], "female": ["fletcher/0", "leatherworker/0", "leatherworker/3"]},
    "robe": {"male": ["shepherd/0", "leatherworker/4", "shepherd/1"], "female": ["leatherworker/2", "none/6", "cleric/0"]},
    "wrap": {"male": ["fisherman/1", "shepherd/1", "none/4"], "female": ["cleric/2", "none/0", "none/6"]},
}
ROLE_CUTS = {"ruler": {"male": "cultist/0", "female": "cleric/1"}, "priest": {"male": "cleric/2", "female": "cleric/3"}}


def cut(garments):
    g = garments.lower()
    return "parka" if "parka" in g else "wrap" if re.search(r"wrap|kilt|apron|skirt|bark-cloth", g) and "robe" not in g else "robe"


def darker(c, f=0.72):
    return tuple(round(v * f) for v in c)
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


ROLES = ("ruler", "warrior", "priest")
# Player-skin rows and columns of the torso (front, back) and arms on both layers of a 64x64 clothing texture.
TORSO = [(20, 20), (32, 20), (20, 36), (32, 36)]          # (x, y) of each 8x12 torso face
ARMS = [(44, 20), (36, 52), (44, 36), (52, 52)]           # (x, y) of each 4x12 arm front


def mark(img, role, palette):
    """Paints a role's marks in the culture's ornament and pigment colours: beads and dots only on the garment, a warrior's
    stripes on the garment or, where the cut leaves the body bare, on the skin as body paint (on the base layer only)."""
    out = img.copy()
    px = out.load()
    _, accent, trim = palette

    def put(x, y, c, paint=False):
        if px[x, y][3] or paint:
            px[x, y] = c + (255,)
    for tx, ty in TORSO:
        for x in range(8):
            for y in range(12):
                if role == "ruler" and (y in (0, 1) or y == 7) and (x + y) % 2 == 0:
                    put(tx + x, ty + y, accent if y != 1 else trim)
                elif role == "warrior" and (x + y) % 6 in (0, 1):
                    put(tx + x, ty + y, trim, paint=ty == 20)
                elif role == "priest" and ((x + 2 * y) % 4 == 0 and y < 10 or y >= 10):
                    put(tx + x, ty + y, accent if y < 10 else trim)
    for ax, ay in ARMS:
        for x in range(4):
            for y in range(12):
                if role == "warrior" and y in (3, 4) or role == "ruler" and y == 9:
                    put(ax + x, ay + y, trim if role == "warrior" else accent, paint=role == "warrior" and (ay == 20 or (ax, ay) == (36, 52)))
    return out


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
    repo, jar = Path(sys.argv[1]), Path(sys.argv[2])
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
    with zipfile.ZipFile(jar) as z:
        for kit, r in sorted(kits.items()):
            main, accent, trim = hexrgb(r["main"]), hexrgb(r["accent"]), hexrgb(r["trim"])
            palette = [main, darker(main), trim]               # garment, its second hide or fibre, pigment; ornament only in marks
            bases = CUTS[cut(r["garments"])]
            entries, clothing, roles = [], {}, {}

            def save(img, gender, name):
                path = f"skins/clothing/normal/{kit}/{gender}/{name}.png"
                dest = RES / "assets" / NS / path
                dest.parent.mkdir(parents=True, exist_ok=True)
                img.save(dest, optimize=True)
                cid = f"{NS}:{path}"
                entries.append({"id": cid, "profession": f"{NS}:{kit}", "chance": 1.0, "gender": gender})
                return cid
            def worn(gender, base):
                return recolour(Image.open(io.BytesIO(z.read(f"assets/mca/skins/clothing/normal/{gender}/{base}.png"))), palette)
            for gender, names in bases.items():
                for n, base in enumerate(names):
                    clothing.setdefault(gender, []).append(save(worn(gender, base), gender, str(n)))
                for role in ROLES:
                    base = ROLE_CUTS.get(role, {}).get(gender, names[0])
                    roles.setdefault(role, {})[gender] = save(mark(worn(gender, base), role, (main, accent, trim)), gender, role)
            lst = RES / "data" / NS / "skins" / "clothing" / f"{kit}.json"
            lst.parent.mkdir(parents=True, exist_ok=True)
            lst.write_text(json.dumps(entries, indent=2) + "\n", encoding="utf-8")
            out_kits[kit] = {"clothing": clothing, "roles": roles}
    data = {"_comment": "Written by diaithir-compat/tools/gen_people.py from DiaithirRPG world/earth/age1 and tools/culture_dress.csv; edit those.",
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
    print(f"{len(cultures)} cultures, {len(out_kits)} kits, {sum(len(v) for k in out_kits.values() for v in k['clothing'].values()) + sum(len(v) for k in out_kits.values() for v in k['roles'].values())} textures")


if __name__ == "__main__":
    main()
