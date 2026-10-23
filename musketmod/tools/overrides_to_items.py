"""Convert 1.21.1 item model 'overrides' (musketmod:loaded/loading/aiming predicates) into 26.1 item model definitions.
Usage: python -I overrides_to_items.py <assets/musketmod dir>"""
import itertools, json, pathlib, sys

root = pathlib.Path(sys.argv[1])
PROPS = {
    "musketmod:aiming": {"property": "musketmod:aiming"},
    "musketmod:loading": {"property": "minecraft:using_item"},
    "musketmod:loaded": {"property": "minecraft:has_component", "component": "musketmod:loaded"},
}
ORDER = list(PROPS)


def leaf(model):
    return {"type": "minecraft:model", "model": model}


def tree(pick, fixed, depth):
    if depth == len(ORDER):
        return leaf(pick(fixed))
    on_true = tree(pick, {**fixed, ORDER[depth]: 1}, depth + 1)
    on_false = tree(pick, {**fixed, ORDER[depth]: 0}, depth + 1)
    if on_true == on_false:
        return on_true
    return {"type": "minecraft:condition", **PROPS[ORDER[depth]], "on_true": on_true, "on_false": on_false}


items = root / "items"
for path in sorted((root / "models/item").glob("*.json")):
    model = json.loads(path.read_text())
    name = f"musketmod:item/{path.stem}"
    overrides = model.pop("overrides", None)
    if overrides is None:
        continue
    path.write_text(json.dumps(model, indent=2) + "\n")

    def pick(state, overrides=overrides, name=name):
        chosen = name
        for o in overrides:
            if all(state.get(k, 0) >= v for k, v in o["predicate"].items()):
                chosen = o["model"]
        return chosen

    (items / path.name).write_text(json.dumps({"model": tree(pick, {}, 0)}, indent=2) + "\n")
    for variant in root.glob(f"items/{path.stem}_*.json"):
        if variant.stem.split(path.stem + "_")[1] in {"aiming", "aiming_loaded", "loaded", "loading", "loading_loaded"}:
            variant.unlink()
    print("converted", path.stem)
