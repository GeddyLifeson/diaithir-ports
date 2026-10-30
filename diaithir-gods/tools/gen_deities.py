#!/usr/bin/env python3
"""
Builds the "diaithir" court test datapack for Diaithir Gods.

Reads the god list at run time (default /mnt/project-files/mythology/out/gods.json, override with
the first argument or $GODS_JSON) and writes, for every god:

  data/diaithir_gods/deity/deity/<id>.json         the deity
  data/diaithir_gods/deity/altar/<id>.json         its shrine statue
  data/diaithir_gods/deity/offering/<id>/<n>.json  gifts that earn favour (offering word -> vanilla items)
  data/diaithir_gods/deity/sacrifice/<id>/<n>.json creatures whose death costs favour (courts that are not living)
  data/diaithir_gods/deity/perk/<id>/<name>.json   blessings (every god) and curses (courts that are not living)
  assets/diaithir_gods/lang/en_us.json             tools/lang_base.json plus a name and a title per deity

Mapping rules (see docs/DEITY_FORMAT.md):
  effect -> a potion-effect blessing (two tiers); the matching curse is the opposite potion effect.
  living=true courts get no curses, a favour floor of 0 and no kill-based favour loss.
  offering word -> vanilla items; emblem -> icon item and the creature that costs favour when killed.

Deity ids are the god ids folded to ASCII (resource paths cannot hold accents).
"""
import json
import os
import shutil
import sys
import unicodedata
import zlib

HERE = os.path.dirname(os.path.abspath(__file__))
ROOT = os.path.normpath(os.path.join(HERE, ".."))
NS = "diaithir_gods"
DATA = os.path.join(ROOT, "src", "main", "resources", "data", NS)
ASSETS = os.path.join(ROOT, "src", "main", "resources", "assets", NS)
GODS_JSON = (sys.argv[1] if len(sys.argv) > 1 else os.environ.get("GODS_JSON", "/mnt/project-files/mythology/out/gods.json"))

# ------------------------------------------------------------------ mappings
# effect word -> (blessing effect, curse effect, blessing duration in ticks)
EFFECTS = {
    "luck": ("minecraft:luck", "minecraft:unluck", 3600),
    "night_vision": ("minecraft:night_vision", "minecraft:darkness", 1800),
    "speed": ("minecraft:speed", "minecraft:slowness", 1200),
    "dolphins_grace": ("minecraft:dolphins_grace", "minecraft:slowness", 1200),
    "haste": ("minecraft:haste", "minecraft:mining_fatigue", 1200),
    "strength": ("minecraft:strength", "minecraft:weakness", 1200),
    "resistance": ("minecraft:resistance", "minecraft:weakness", 1200),
    "regeneration": ("minecraft:regeneration", "minecraft:poison", 400),
    "water_breathing": ("minecraft:water_breathing", "minecraft:mining_fatigue", 1800),
    "saturation": ("minecraft:saturation", "minecraft:hunger", 40),
    "fire_resistance": ("minecraft:fire_resistance", "minecraft:glowing", 1800),
    # xp_gain has no potion: it is an experience multiplier (blessing) or reducer (curse)
}

# offering word -> list of vanilla items. A dict is an item stack (id + components).
WATER_BOTTLE = {"id": "minecraft:potion", "count": 1, "components": {"minecraft:potion_contents": {"potion": "minecraft:water"}}}
OFFERINGS = {
    "bone": ["minecraft:bone"],
    "hide": ["minecraft:leather"],
    "fruit": ["minecraft:apple"],
    "incense": ["minecraft:dried_kelp"],
    "grain": ["minecraft:wheat"],
    "fish": ["minecraft:cod", "minecraft:salmon"],
    "milk": ["minecraft:milk_bucket"],
    "honey": ["minecraft:honey_bottle"],
    "metal": ["minecraft:iron_ingot"],
    "flower": ["minecraft:poppy"],
    "water": [WATER_BOTTLE],
    "drink": ["minecraft:sweet_berries"],
    "bread": ["minecraft:bread"],
    "rice": ["minecraft:wheat_seeds"],
    "salt": ["minecraft:sugar"],
    "cloth": ["minecraft:white_wool"],
    "corn": ["minecraft:carrot"],
    "amber": ["minecraft:honeycomb"],
    "palm": ["minecraft:melon_slice"],
    "coca": ["minecraft:fern"],
    "ochre": ["minecraft:orange_dye"],
    "fat": ["minecraft:porkchop"],
    "ink": ["minecraft:ink_sac"],
    "coin": ["minecraft:gold_nugget"],
    "chicha": ["minecraft:sweet_berries"],
}
DEFAULT_OFFERING = ["minecraft:bread"]

# emblem -> icon item (anything not listed falls back to the effect icon)
EMBLEM_ITEMS = {
    "amber": "minecraft:honeycomb", "antler": "minecraft:bone", "anvil": "minecraft:anvil", "apple": "minecraft:apple",
    "axe": "minecraft:iron_axe", "ball": "minecraft:slime_ball", "bear": "minecraft:cod", "bird": "minecraft:feather",
    "bison": "minecraft:leather", "blade": "minecraft:iron_sword", "boar": "minecraft:porkchop", "bog": "minecraft:moss_block",
    "bow": "minecraft:bow", "brass": "minecraft:gold_ingot", "bull": "minecraft:leather", "cap": "minecraft:leather_helmet",
    "cat": "minecraft:string", "cave": "minecraft:stone", "cedar": "minecraft:spruce_log", "circle": "minecraft:clock",
    "cliff": "minecraft:stone", "cloud": "minecraft:white_wool", "coast": "minecraft:sand", "coin": "minecraft:gold_nugget",
    "corn": "minecraft:carrot", "cow": "minecraft:milk_bucket", "coyote": "minecraft:bone", "crescent": "minecraft:quartz",
    "deer": "minecraft:leather", "desert": "minecraft:sand", "disk": "minecraft:gold_ingot", "dog": "minecraft:bone",
    "dolphin": "minecraft:cod", "double axe": "minecraft:iron_axe", "dove": "minecraft:feather", "dust": "minecraft:sand",
    "eagle": "minecraft:feather", "ear": "minecraft:wheat", "elephant": "minecraft:bone", "elk": "minecraft:leather",
    "falcon": "minecraft:feather", "feather": "minecraft:feather", "field": "minecraft:wheat", "fire": "minecraft:flint_and_steel",
    "fish": "minecraft:cod", "flame": "minecraft:torch", "forest": "minecraft:oak_sapling", "fox": "minecraft:sweet_berries",
    "frog": "minecraft:slime_ball", "gate": "minecraft:oak_door", "gazelle": "minecraft:leather", "goat": "minecraft:goat_horn",
    "goat-fish": "minecraft:salmon", "grain": "minecraft:wheat", "hammer": "minecraft:iron_pickaxe", "hand": "minecraft:bone_meal",
    "handprint": "minecraft:orange_dye", "hare": "minecraft:rabbit_foot", "hearth": "minecraft:campfire", "helm": "minecraft:iron_helmet",
    "heron": "minecraft:feather", "hill": "minecraft:grass_block", "hook": "minecraft:fishing_rod", "horn": "minecraft:goat_horn",
    "horned crown": "minecraft:golden_helmet", "horse": "minecraft:saddle", "hound": "minecraft:bone", "house": "minecraft:oak_planks",
    "hummingbird": "minecraft:feather", "ice": "minecraft:ice", "ivy": "minecraft:vine", "jackal": "minecraft:bone",
    "jade": "minecraft:emerald", "jaguar": "minecraft:string", "jar": "minecraft:flower_pot", "kettle": "minecraft:cauldron",
    "key": "minecraft:tripwire_hook", "knife": "minecraft:iron_sword", "lake": "minecraft:water_bucket", "leaf": "minecraft:oak_leaves",
    "lion": "minecraft:string", "lotus": "minecraft:lily_pad", "lyre": "minecraft:note_block", "mace": "minecraft:iron_axe",
    "machete": "minecraft:iron_sword", "mammoth": "minecraft:bone", "mirror": "minecraft:glass_pane", "moon": "minecraft:quartz",
    "mountain": "minecraft:stone", "oak": "minecraft:oak_log", "ochre": "minecraft:orange_dye", "orca": "minecraft:cod",
    "owl": "minecraft:feather", "palm": "minecraft:jungle_log", "peach": "minecraft:apple", "peacock": "minecraft:feather",
    "peak": "minecraft:stone", "pearl": "minecraft:ender_pearl", "plain": "minecraft:grass_block", "point": "minecraft:flint",
    "pot": "minecraft:flower_pot", "rabbit": "minecraft:rabbit_foot", "raven": "minecraft:feather", "reed": "minecraft:sugar_cane",
    "reindeer": "minecraft:leather", "rice": "minecraft:wheat_seeds", "river": "minecraft:water_bucket", "root": "minecraft:beetroot",
    "salmon": "minecraft:salmon", "sea": "minecraft:prismarine_shard", "seal": "minecraft:cod", "serpent": "minecraft:spider_eye",
    "sheaf": "minecraft:hay_block", "shell": "minecraft:nautilus_shell", "ship": "minecraft:oak_boat", "sickle": "minecraft:iron_hoe",
    "skull": "minecraft:bone", "sky": "minecraft:white_wool", "snake": "minecraft:spider_eye", "spear": "minecraft:trident",
    "spider": "minecraft:string", "spindle": "minecraft:string", "spring": "minecraft:water_bucket", "staff": "minecraft:stick",
    "stag": "minecraft:leather", "star": "minecraft:nether_star", "stone": "minecraft:stone", "sun": "minecraft:sunflower",
    "sun disk": "minecraft:gold_ingot", "swan": "minecraft:feather", "sword": "minecraft:iron_sword", "thread": "minecraft:string",
    "thunderbolt": "minecraft:lightning_rod", "torch": "minecraft:torch", "track": "minecraft:rail", "tree": "minecraft:oak_sapling",
    "trident": "minecraft:trident", "turtle": "minecraft:turtle_scute", "valley": "minecraft:grass_block", "veil": "minecraft:white_wool",
    "wave": "minecraft:prismarine_shard", "well": "minecraft:bucket", "wheel": "minecraft:minecart", "wing": "minecraft:feather",
    "winged sandal": "minecraft:leather_boots", "wolf": "minecraft:bone",
}
EFFECT_ICONS = {
    "luck": "minecraft:rabbit_foot", "night_vision": "minecraft:golden_carrot", "speed": "minecraft:sugar",
    "dolphins_grace": "minecraft:heart_of_the_sea", "haste": "minecraft:golden_pickaxe", "strength": "minecraft:iron_sword",
    "resistance": "minecraft:shield", "regeneration": "minecraft:ghast_tear", "water_breathing": "minecraft:pufferfish",
    "saturation": "minecraft:cooked_beef", "fire_resistance": "minecraft:magma_cream", "xp_gain": "minecraft:experience_bottle",
}
# emblem -> vanilla creature that is sacred to the god (killing it costs favour, courts that are not living only)
EMBLEM_ENTITIES = {
    "horse": "minecraft:horse", "bull": "minecraft:cow", "cow": "minecraft:cow", "bison": "minecraft:cow",
    "goat": "minecraft:goat", "goat-fish": "minecraft:goat", "wolf": "minecraft:wolf", "hound": "minecraft:wolf",
    "dog": "minecraft:wolf", "jackal": "minecraft:wolf", "coyote": "minecraft:wolf", "fish": "minecraft:salmon",
    "salmon": "minecraft:salmon", "cat": "minecraft:cat", "lion": "minecraft:cat", "jaguar": "minecraft:ocelot",
    "boar": "minecraft:pig", "bear": "minecraft:polar_bear", "bird": "minecraft:parrot", "eagle": "minecraft:parrot",
    "falcon": "minecraft:parrot", "owl": "minecraft:parrot", "raven": "minecraft:parrot", "heron": "minecraft:parrot",
    "swan": "minecraft:chicken", "dove": "minecraft:chicken", "peacock": "minecraft:chicken", "hummingbird": "minecraft:parrot",
    "spider": "minecraft:spider", "frog": "minecraft:frog", "turtle": "minecraft:turtle", "rabbit": "minecraft:rabbit",
    "hare": "minecraft:rabbit", "fox": "minecraft:fox", "dolphin": "minecraft:dolphin", "orca": "minecraft:dolphin",
    "seal": "minecraft:dolphin", "deer": "minecraft:sheep", "elk": "minecraft:sheep", "stag": "minecraft:sheep",
    "reindeer": "minecraft:sheep", "gazelle": "minecraft:sheep", "snake": "minecraft:silverfish", "serpent": "minecraft:silverfish",
    "mammoth": "minecraft:llama", "elephant": "minecraft:llama",
}
# rank (H high, M middle, L low) -> altar pedestal block, light level, favour of the preferred gift, blessing chances
RANKS = {
    "H": {"block": "minecraft:stone_bricks", "light": 8, "gift": 5, "chance_low": 0.60, "chance_high": 0.75, "loss": 8},
    "M": {"block": "minecraft:cobblestone", "light": 4, "gift": 3, "chance_low": 0.45, "chance_high": 0.60, "loss": 5},
    "L": {"block": "minecraft:stone", "light": 0, "gift": 2, "chance_low": 0.30, "chance_high": 0.45, "loss": 3},
}


# ------------------------------------------------------------------ helpers
def fold(text):
    """ASCII-fold a god id into a valid resource path."""
    text = unicodedata.normalize("NFKD", text)
    text = "".join(c for c in text if not unicodedata.combining(c)).lower()
    out = []
    for c in text:
        if c.isalnum() and c.isascii():
            out.append(c)
        elif c in "._-":
            out.append(c)
        else:
            out.append("_")
    return "".join(out)


def write(path, obj):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8") as f:
        json.dump(obj, f, indent=2, ensure_ascii=False)
        f.write("\n")


def item_json(item):
    """An item id or an item stack."""
    return item


def court_color(court):
    """A stable, readable tint for the perk icons of a court."""
    h = zlib.crc32(court.encode("utf-8"))
    r, g, b = 80 + (h & 0x7F), 80 + ((h >> 8) & 0x7F), 80 + ((h >> 16) & 0x7F)
    return (r << 16) | (g << 8) | b


def potion(effect, amplifier, duration):
    return {"Potion": effect, "Amplifier": amplifier, "Duration": duration, "ShowParticles": False}


def perk(deity_id, icon_item, color, fancy, lo, hi, action, chance, cooldown, category, condition=None):
    p = {
        "icon": {"item": icon_item, "color": color, "fancy": fancy},
        "range": {"deity": deity_id, "minlevel": lo, "maxlevel": hi},
        "action": action,
        "chance": chance,
        "cooldown": cooldown,
        "cooldown_category": category,
    }
    if condition:
        p["condition"] = condition
    return p


def clean():
    """Removes what an earlier run wrote, keeping hand-written files."""
    for sub in ("deity/deity", "deity/offering", "deity/sacrifice", "deity/perk"):
        shutil.rmtree(os.path.join(DATA, *sub.split("/")), ignore_errors=True)
    altar = os.path.join(DATA, "deity", "altar")
    if os.path.isdir(altar):
        for name in os.listdir(altar):
            if name != "stone_figure.json":
                os.remove(os.path.join(altar, name))


def main():
    with open(GODS_JSON, encoding="utf-8") as f:
        gods = json.load(f)
    clean()
    names = {}
    seen = {}
    counts = {"deities": 0, "perks": 0, "offerings": 0, "sacrifices": 0, "living": 0, "curses": 0}
    for g in gods:
        slug = fold(g["id"])
        if slug in seen:
            raise SystemExit("id collision after folding: %s and %s" % (seen[slug], g["id"]))
        seen[slug] = g["id"]
        deity_id = "%s:%s" % (NS, slug)
        living = bool(g.get("living"))
        rank = RANKS.get(g.get("rank", "M"), RANKS["M"])
        effect = g.get("effect", "luck")
        icon = EMBLEM_ITEMS.get(g.get("emblem", ""), EFFECT_ICONS.get(effect, "minecraft:stone"))
        color = court_color(g.get("court", ""))
        names[slug] = (g["name"], g.get("domain", ""))

        # deity: a favour floor of 0 means living courts can never fall into disfavour
        write(os.path.join(DATA, "deity", "deity", slug + ".json"), {
            "name": deity_id, "icon": icon, "unlocked": True, "enabled": True,
            "minlevel": 0 if living else -10, "maxlevel": 10,
        })
        counts["deities"] += 1
        if living:
            counts["living"] += 1

        # shrine statue
        write(os.path.join(DATA, "deity", "altar", slug + ".json"), {
            "name": deity_id, "female": False, "slim": False, "light": rank["light"],
            "items": {"block": rank["block"], "mainhand": icon, "armor_locked": False, "hands_locked": False, "block_locked": False},
            "pose_locked": False,
        })

        # offerings
        gifts = OFFERINGS.get(g.get("offering", ""), DEFAULT_OFFERING)
        for n, item in enumerate(gifts, 1):
            write(os.path.join(DATA, "deity", "offering", slug, "gift_%d.json" % n), {
                "item": item_json(item), "favor": rank["gift"], "maxuses": 8, "cooldown": 12000,
            })
            counts["offerings"] += 1

        # blessings: one potion tier at level 2 and a stronger tier at level 6 (xp_gain scales experience)
        cat = slug
        if effect == "xp_gain":
            low = [{"type": "xp", "multiplier": 1.25}]
            high = [{"type": "xp", "multiplier": 1.5}]
            curse_low = [{"type": "xp", "multiplier": 0.8}]
            curse_high = [{"type": "xp", "multiplier": 0.5}]
            cond = None
        else:
            blessing, curse, duration = EFFECTS.get(effect, EFFECTS["luck"])
            low = [{"type": "potion", "tag": potion(blessing, 0, duration)}]
            high = [{"type": "potion", "tag": potion(blessing, 1, duration)}]
            curse_low = [{"type": "potion", "tag": potion(curse, 0, 600)}]
            curse_high = [{"type": "potion", "tag": potion(curse, 1, 600)}]
            cond = {"type": "random_tick"}
        write(os.path.join(DATA, "deity", "perk", slug, "blessing_1.json"),
              perk(deity_id, icon, color, False, 2, 10, low, rank["chance_low"], 1200, cat + "_blessing_1", cond))
        write(os.path.join(DATA, "deity", "perk", slug, "blessing_2.json"),
              perk(deity_id, icon, color, True, 6, 10, high, rank["chance_high"], 1200, cat + "_blessing_2", cond))
        counts["perks"] += 2

        # curses and kill-based favour loss only for courts that are not living
        if not living:
            write(os.path.join(DATA, "deity", "perk", slug, "curse_1.json"),
                  perk(deity_id, icon, color, False, -10, -2, curse_low, 0.25, 1200, cat + "_curse_1", cond))
            write(os.path.join(DATA, "deity", "perk", slug, "curse_2.json"),
                  perk(deity_id, icon, color, True, -10, -6, curse_high, 0.35, 1200, cat + "_curse_2", cond))
            counts["perks"] += 2
            counts["curses"] += 2
            entity = EMBLEM_ENTITIES.get(g.get("emblem", ""))
            if entity:
                write(os.path.join(DATA, "deity", "sacrifice", slug, "sacred_creature.json"), {
                    "entity": entity, "favor": -rank["loss"], "maxuses": 8, "cooldown": 6000,
                })
                counts["sacrifices"] += 1

    # lang: hand-written base plus a name and a title for every deity
    with open(os.path.join(HERE, "lang_base.json"), encoding="utf-8") as f:
        lang = json.load(f)
    for slug, (name, domain) in sorted(names.items()):
        key = "deity.%s.%s" % (NS, slug)
        lang[key] = name
        lang[key + ".title"] = domain[:1].upper() + domain[1:]
    write(os.path.join(ASSETS, "lang", "en_us.json"), lang)
    print("wrote", ", ".join("%s=%d" % kv for kv in counts.items()), "from", GODS_JSON)


if __name__ == "__main__":
    main()
