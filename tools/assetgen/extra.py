import os
import os, json
from tex import *
import icons

def run(g):
    A = g.A
    # fluids: still + flow textures
    cols = {"honey": (240, 190, 40), "tea": (150, 90, 40), "potion": (90, 130, 220), "chocolate": (100, 60, 40), "milk": (240, 240, 236)}
    import art
    for f, c in cols.items():
        lo = tuple(int(v * 0.82) for v in c)
        hi = tuple(min(255, int(v * 1.12)) for v in c)
        for k in ("still", "flow"):
            g.wtex(f"block/{f}_{k}", art.fluid_tex(f + k, lo, hi, k == "flow"))
    # item sub-models used by custom item renderers
    subs = {"extendo_grip": ("thin_short", "wide_short", "thin_long", "wide_long"),
            "handheld_worldshaper": ("core_glow", "accelerator"),
            "linked_controller": ("powered", "torch_off"),
            "wand_of_symmetry": ("core_glow",)}
    for nm, lst in subs.items():
        for sub in lst:
            g.wtex(f"item/{nm}_{sub}", icons.gear(g.PAL["brass"], nm + sub) if "glow" not in sub else icons.disk((120, 210, 230), nm + sub))
            g.model(f"item/{nm}/{sub}", {"parent": "minecraft:item/generated", "textures": {"layer0": f"create:item/{nm}_{sub}"}})

import re, math

GUI_SRC = os.path.abspath(os.path.dirname(__file__) + "/../../src/main/java/com/zurrtum/create/client/foundation/gui/AllGuiTextures.java")

def panel(im, x, y, w, h, base, name, slot=False):
    from PIL import ImageDraw
    d = ImageDraw.Draw(im)
    if w < 1 or h < 1: return
    if slot:
        d.rectangle([x, y, x + w - 1, y + h - 1], fill=shade(base, 0.35) + (255,), outline=shade(base, 0.2) + (255,))
        if w > 4 and h > 4:
            d.line([x + 1, y + h - 1, x + w - 1, y + h - 1], fill=shade(base, 1.5) + (255,))
        return
    d.rectangle([x, y, x + w - 1, y + h - 1], fill=base + (255,))
    if w > 3 and h > 3:
        d.line([x, y, x + w - 1, y], fill=shade(base, 1.3) + (255,))
        d.line([x, y, x, y + h - 1], fill=shade(base, 1.3) + (255,))
        d.line([x, y + h - 1, x + w - 1, y + h - 1], fill=shade(base, 0.65) + (255,))
        d.line([x + w - 1, y, x + w - 1, y + h - 1], fill=shade(base, 0.65) + (255,))

def gui_sheets(g):
    src = open(GUI_SRC).read()
    sheets = {}
    pat = re.compile(r'^\s*([A-Z][A-Z0-9_]*)\("([^"]+)"(?:,\s*(\d+))(?:,\s*(\d+))(?:,\s*(\d+))?(?:,\s*(\d+))?\)', re.M)
    for m in pat.finditer(src):
        name, sheet = m.group(1), m.group(2)
        nums = [int(v) for v in m.groups()[2:] if v is not None]
        if len(nums) == 2: x, y, w, h = 0, 0, nums[0], nums[1]
        elif len(nums) == 4: x, y, w, h = nums
        else: continue
        sheets.setdefault(sheet, []).append((name, x, y, w, h))
    from PIL import Image
    for sheet, regs in sheets.items():
        size = 256
        im = Image.new("RGBA", (size, size), (0, 0, 0, 0))
        for (name, x, y, w, h) in regs:
            base = PAL["steel"]
            if any(k in name for k in ("GREEN",)): base = (60, 140, 70)
            elif "RED" in name or "BAD" in name: base = (160, 50, 50)
            elif "YELLOW" in name: base = PAL["paint_yellow"]
            elif "WHITE" in name: base = (220, 220, 220)
            elif "BRASS" in name or "SCHEDULE" in name: base = PAL["brass"] if "BRASS" in name else shade(PAL["steel"], 1.1)
            slot = "SLOT" in name or "HOTSLOT" in name and "ARROW" not in name
            panel(im, x, y, w, h, base, name, slot)
        # player inventory slot grid
        if sheet == "player_inventory":
            for r in range(3):
                for c in range(9): panel(im, 7 + c * 18, 7 + r * 18 - 1, 18, 18, PAL["steel"], "s", True)
            for c in range(9): panel(im, 7 + c * 18, 65, 18, 18, PAL["steel"], "s", True)
        g.save(im, f"{g.A}/textures/gui/{sheet}.png")

def icon_sheet(g):
    from PIL import Image
    im = Image.new("RGBA", (256, 256), (0, 0, 0, 0))
    for cy in range(16):
        for cx in range(16):
            cell = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
            r = rng(f"icon{cx},{cy}")
            col = [PAL["brass"], (230, 230, 230), PAL["copper"], (120, 200, 230)][r.randrange(4)]
            for yy in range(2, 14):
                for xx in range(1, 8):
                    if r.random() < 0.42:
                        px(cell, 1 + xx, yy, col); px(cell, 14 - xx, yy, col)
            im.paste(cell, (cx * 16, cy * 16))
    g.save(im, f"{g.A}/textures/gui/icons.png")

def misc_textures(g):
    from PIL import Image
    # special textures
    def checker(a, b, size=16, cell=8):
        im = Image.new("RGBA", (size, size))
        for y in range(size):
            for x in range(size):
                px(im, x, y, a if ((x // cell) + (y // cell)) % 2 == 0 else b)
        return im
    g.save(checker((200, 200, 200), (150, 150, 150)), f"{g.A}/textures/special/checkerboard.png")
    g.save(checker((200, 200, 200), (150, 150, 150), 16, 4), f"{g.A}/textures/special/thin_checkerboard.png")
    g.save(checker((200, 200, 200), (255, 255, 255), 16, 8), f"{g.A}/textures/special/highlighted_checkerboard.png")
    cut = checker((200, 200, 200), (0, 0, 0), 16, 8)
    for y in range(16):
        for x in range(16):
            if ((x // 8) + (y // 8)) % 2: cut.putpixel((x, y), (0, 0, 0, 0))
    g.save(cut, f"{g.A}/textures/special/cutout_checkerboard.png")
    sel = Image.new("RGBA", (16, 16), (255, 255, 255, 255)); g.save(sel, f"{g.A}/textures/special/selection.png")
    gl = Image.new("RGBA", (16, 16), (255, 255, 255, 255)); g.save(gl, f"{g.A}/textures/special/glue.png")
    # small overlays
    g.save(Image.new("RGBA", (16, 16), (24, 24, 28, 230)), f"{g.A}/textures/gui/overlay.png")
    g.save(Image.new("RGBA", (16, 16), (255, 255, 255, 255)), f"{g.A}/textures/gui/advancements.png")
    g.save(Image.new("RGBA", (1, 1), (60, 62, 66, 255)), f"{g.A}/textures/gui/jei/background.png")
    # entity textures
    for nm, c in (("coupling", PAL["steel"]), ("logistics_hat", PAL["paint_yellow"]), ("train_hat", PAL["rubber"])):
        g.save(plate(c, nm, False), f"{g.A}/textures/entity/{nm}.png")
    g.save(speckle((120, 190, 90), "slime", 0.08), f"{g.A}/textures/entity/super_glue/slime.png")
    # armor sheets (uniform material over the 64x32 humanoid layout)
    for nm, c in (("copper", PAL["copper"]), ("netherite", (62, 54, 64)), ("cardboard", (176, 140, 96))):
        im = Image.new("RGBA", (64, 32))
        base = plate(c, nm + "arm", False)
        for ty in range(0, 32, 16):
            for tx in range(0, 64, 16): im.paste(base, (tx, ty))
        g.save(im, f"{g.A}/textures/entity/equipment/humanoid/{nm}.png")
    g.save(im, f"{g.A}/textures/entity/equipment/humanoid_leggings/cardboard.png")
    for nm in ("copper", "netherite", "cardboard"):
        layers = {"humanoid": [{"texture": f"create:{nm}"}]}
        if nm == "cardboard": layers["humanoid_leggings"] = [{"texture": f"create:{nm}"}]
        g.wj(f"{g.A}/equipment/{nm}.json", {"layers": layers})
    for nm in ("layer", "arm"):
        im2 = Image.new("RGBA", (64, 64))
        b = plate((62, 54, 64), "ndiv" + nm, False)
        for ty in range(0, 64, 16):
            for tx in range(0, 64, 16): im2.paste(b, (tx, ty))
        g.save(im2, f"{g.A}/textures/models/armor/netherite_diving_{nm}.png")
    g.save(Image.new("RGBA", (256, 256), (0, 0, 0, 0)), f"{g.A}/textures/misc/package_blur.png")
    g.save(Image.new("RGBA", (9, 9), (200, 60, 60, 255)), f"{g.A}/textures/map/decorations/station.png")
    sheet = Image.new("RGBA", (512, 256), (0, 0, 0, 0))
    g.save(sheet, f"{g.A}/textures/gui/trainmap_sprite_sheet.png")
    for nm in ("logo", "curseforge", "modrinth"):
        pass
    g.save(plate(PAL["steel"], "logo", True).resize((256, 256), Image.NEAREST), f"{g.A}/textures/gui/logo.png")
    os.makedirs(f"{g.A}/textures/gui/platform_icons", exist_ok=True)
    for nm in ("curseforge", "modrinth"):
        g.save(Image.new("RGBA", (256, 256), (0, 0, 0, 0)), f"{g.A}/textures/gui/platform_icons/{nm}.png")

_old_run = run
def run(g):
    _old_run(g)
    g.save = save
    gui_sheets(g)
    icon_sheet(g)
    misc_textures(g)
