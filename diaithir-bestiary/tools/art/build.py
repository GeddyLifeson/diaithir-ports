"""Build one or more creatures: python3 tools/art/build.py name [name ...] [--out DIR] [--sheet DIR]"""
import importlib
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
from render import Rig, sheet, atlas_sheet  # noqa: E402

ROOT = Path(__file__).resolve().parent.parent.parent / "src/main/resources/assets/diaithirbestiary"


def build(name, out=ROOT, sheets=None):
    mod = importlib.import_module("creatures." + name)
    m, anims = mod.make()
    m.build(out)
    m.write(out, anims)
    print(name, m.size_px, "cubes", len(m.cubes))
    if sheets:
        Path(sheets).mkdir(parents=True, exist_ok=True)
        rig = Rig(out, name)
        sheet(rig, Path(sheets) / (name + "_sheet.png"), title=name)
        atlas_sheet(rig, Path(sheets) / (name + "_atlas.png"))
    return m


if __name__ == "__main__":
    args = sys.argv[1:]
    out, sheets = ROOT, None
    names = []
    i = 0
    while i < len(args):
        if args[i] == "--out":
            out = Path(args[i + 1]); i += 2
        elif args[i] == "--sheet":
            sheets = args[i + 1]; i += 2
        else:
            names.append(args[i]); i += 1
    for n in names:
        build(n, out, sheets)
