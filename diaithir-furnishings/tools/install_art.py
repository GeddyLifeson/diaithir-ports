"""Per-object layered art. Copies the part textures from the gear workshop art drop (diaithir-gear PR 180) into this module and
rewrites each item model to use them. Run after gen_everyday.py: python3 -I tools/install_art.py [drop dir]. Items without art keep the generated sprite."""
import json, shutil, sys
from pathlib import Path

M = Path(__file__).resolve().parents[1]
A = M / "src/main/resources/assets/diaithirfurnishings"
DROP = Path(sys.argv[1] if len(sys.argv) > 1 else "/mnt/project-files/texture-drops/gear-art-pr180") / "assets/diaithirgear"
n = skipped = 0
for it in json.load(open(M / "src/main/resources/data/diaithirfurnishings/everyday.json"))["items"]:
    src = DROP / f"models/item/workshop/ev_{it['id']}.json"
    if not src.exists():
        skipped += 1
        continue
    layers = json.load(open(src))["textures"]
    out = {}
    for k, ref in layers.items():
        part = ref.rsplit("/", 1)[1].removeprefix(f"ev_{it['id']}_")
        shutil.copyfile(DROP / f"textures/item/workshop/{ref.rsplit('/', 1)[1]}.png", A / f"textures/item/{it['id']}_{part}.png")
        out[k] = f"diaithirfurnishings:item/{it['id']}_{part}"
    (A / f"textures/item/{it['id']}.png").unlink(missing_ok=True)
    json.dump({"parent": "minecraft:item/handheld" if it["durability"] else "minecraft:item/generated", "textures": out},
              open(A / f"models/item/{it['id']}.json", "w"), indent=1)
    n += 1
print(n, "items with layered art,", skipped, "without")
