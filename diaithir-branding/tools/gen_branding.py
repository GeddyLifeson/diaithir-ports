#!/usr/bin/env python3
"""Writes Diaithir's client branding into this mod's resources and the early window theme:

  python3 tools/gen_branding.py [--theme-dir DIR]

- assets/minecraft/textures/gui/title/minecraft.png, minceraft.png, edition.png: the title screen wordmark in Uncial Antiqua
- assets/diaithirbranding/icons/icon_*.png: the window and taskbar icon (read by MinecraftMixin)
- assets/diaithirbranding/textures/gui/loading_logo.png: the resource loading wordmark (read by LogoTextureMixin)
- assets/minecraft/font/default.json + assets/diaithirbranding/font/pixelify_sans.ttf: Pixelify Sans as the game font,
  ahead of vanilla's providers, which still cover any glyph it lacks
- assets/minecraft/texts/splashes.txt and assets/minecraft/lang/en_us.json: splash lines and UI strings
- --theme-dir: the FML early window theme (theme-diaithir.json and its images) for the pack's config/fml folder

Both fonts are SIL OFL 1.1 (tools/branding/OFL-*.txt; Pixelify Sans' licence ships at the jar root). The hearth logo is Diaithir's own art.
"""
import argparse, json, os, shutil
from PIL import Image, ImageDraw, ImageFont

HERE = os.path.dirname(os.path.abspath(__file__))
SRC = os.path.join(HERE, "branding")
ROOT = os.path.join(HERE, "..", "src", "main", "resources", "assets")
MC = os.path.join(ROOT, "minecraft")
NS = os.path.join(ROOT, "diaithirbranding")

BONE, GOLD, EMBER, NIGHT, SHADOW = (0xF3, 0xE3, 0xB5), (0xC9, 0xA2, 0x4A), (0xF5, 0x9A, 0x23), (0x1B, 0x1D, 0x2A), (0x3A, 0x22, 0x10)
UNCIAL = os.path.join(SRC, "UncialAntiqua-Regular.ttf")

SPLASHES = """Fire first, then everything else.
Fifteen thousand years before the first city.
The ice is still leaving.
Mind the cave lion.
Every hearth is a town in waiting.
Follow the river down.
Flint is worth the walk.
Wild barley grows in the south.
Aurochs do not forgive.
Somebody lived here before you.
Count the winters.
The reindeer know the way.
Mammoth bone holds up a roof.
The steppe goes on.
Bring the fire home.
Ochre for the dead.
Names last longer than houses.
Your town remembers you.
Raiders come at harvest.
A grudge outlives its owner.
The sea is lower. For now.
Doggerland is dry land.
Walk to Ireland.
One in five, and every step real.
Built on real ground.
Ask the elders.
Cold hands, warm fire.
Salt is wealth.
Trade an axe for a winter.
The Ages turn.""".splitlines()

# Player-facing strings that name Minecraft, NeoForge or mods. Legal notices, credits and Realms text stay as shipped.
LANG = {
    "accessibility.onboarding.screen.title": "Welcome to Diaithir!\n\nWould you like to turn on the Narrator or open the Accessibility Settings?",
    "gui.loadingMinecraft": "Loading Diaithir",
    "outOfMemory.message": "Diaithir has run out of memory.\n\nClose other programs, or give the game more memory in Prism: Edit, Settings, Java.\n\nThe game stopped to keep your world safe. Try launching again, or go back to the title screen and join again.",
    "selectServer.defaultName": "Diaithir Server",
    "selectWorld.locked": "Open in another Diaithir window.",
    "pack.dropConfirm": "Add these packs to Diaithir?",
    "advancements.story.root.title": "Diaithir",
    "multiplayer.downloadingTerrain": "Raising the land...",
    "resourcePack.vanilla.description": "The base look of Diaithir",
    "dataPack.vanilla.description": "The base rules of Diaithir",
    "pack.incompatible.new": "(Made for a newer game version)",
    "pack.incompatible.old": "(Made for an older game version)",
    "pack.incompatible.confirm.new": "This pack was made for a newer game version and may not work correctly.",
    "pack.incompatible.confirm.old": "This pack was made for an older game version and may no longer work correctly.",
    "pack.incompatible.confirm.unknown": "This pack is broken or made for an unknown game version and may not work correctly.",
    "fml.menu.mods": "Credits",
    "fml.menu.mods.title": "Credits",
    "fml.menu.mods.openmodsfolder": "Open pack folder",
    "fml.menu.mods.config": "Settings",
    "fml.menu.modoptions": "Pack options...",
    "fml.menu.multiplayer.compatible": "Ready to join\n{0,choice,1#1 part|1<%1$s parts} of the pack",
    "fml.menu.multiplayer.incompatible": "Different pack version",
    "fml.menu.multiplayer.incompatible.extra": "Different pack version\n%1$s",
    "fml.menu.multiplayer.vanilla": "Not a Diaithir server",
    "fml.menu.multiplayer.vanilla.incompatible": "Not a Diaithir server",
    "fml.menu.multiplayer.serveroutdated": "The server runs an older pack. Ask the host to update.",
    "fml.menu.multiplayer.clientoutdated": "Your pack is older than the server's. Launch again to update.",
    "fml.menu.multiplayer.extraservermods": "The server has parts your pack lacks. Launch again to update.",
    "fml.menu.multiplayer.modsincompatible": "Your pack does not match the server's. Launch again to update.",
    "fml.menu.accessdenied.title": "Server closed to Diaithir",
    "fml.menu.accessdenied.message": "Diaithir could not join this server.\nThe server %1$s only allows unchanged games.",
    "fml.modmismatchscreen.missingmods.client": "Your pack lacks parts this server needs. Launch again to update, then join:",
    "fml.modmismatchscreen.missingmods.server": "Your pack has parts this server lacks. Launch again to update, then join:",
    "fml.modmismatchscreen.mismatchedmods": "These parts of your pack differ from the server's. Launch again to update, then join:",
    "fml.modmismatchscreen.homepage": "Open this part's home page",
    "fml.modmismatchscreen.table.visit.mod_page": "Open the home page of the part that adds %s",
    "fml.resources.modresources": "Art and sound from %1$s parts of the pack",
    "fml.resources.moddata": "Rules and recipes from %1$s parts of the pack",
    "pack.neoforge.description": "Diaithir core resources",
    "neoforge.network.negotiation.failure.vanilla.client.not_supported": "This server runs Diaithir. Join it from the Diaithir launcher (version %s).",
    "neoforge.network.negotiation.failure.vanilla.server.not_supported": "This server does not run Diaithir, so Diaithir cannot join it.",
    "diaithir.pack.mod_resources": "Diaithir Resources",
    "diaithir.pack.mod_data": "Diaithir Data",
    "diaithir.pack.programmer_art.description": "The oldest block art, from before the Ages",
    "diaithir.pack.high_contrast.description": "Sharper menus and text, easier to read",
    "diaithir.menu.help": "Get help",
    "diaithir.menu.join_page": "Join page",
    "diaithir.warnings.title": "Diaithir started with warnings",
    "diaithir.warnings.subtitle": "The game still works. If something looks wrong, press Get help.",
    "diaithir.warnings.continue": "Continue",
}


def font(size, path=UNCIAL):
    return ImageFont.truetype(path, size)


def wordmark(width, height, text, size, fill, outline=SHADOW, stroke=0, top=None):
    """Centred text with a dark outline and a 1px-per-64 drop shadow, on transparent ground."""
    img = Image.new("RGBA", (width, height), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    f = font(size)
    l, t, r, b = d.textbbox((0, 0), text, font=f, stroke_width=stroke)
    x = (width - (r - l)) // 2 - l
    y = ((height - (b - t)) // 2 - t) if top is None else top - t
    off = max(1, size // 24)
    d.text((x + off, y + off), text, font=f, fill=outline + (255,), stroke_width=stroke, stroke_fill=outline + (255,))
    d.text((x, y), text, font=f, fill=fill + (255,), stroke_width=stroke, stroke_fill=outline + (255,))
    return img


def save(img, *path):
    os.makedirs(os.path.dirname(os.path.join(*path)), exist_ok=True)
    img.save(os.path.join(*path))


def title_textures():
    # LogoRenderer draws 256x44 of a 256x64 texture (1024x256 here), and the edition strip 128x14 of 128x16 (512x64).
    logo = Image.new("RGBA", (1024, 256), (0, 0, 0, 0))
    # The edition strip overlaps the wordmark by 7 of 64 rows, so the letters end 28 rows above the drawn height.
    logo.alpha_composite(wordmark(1024, 148, "Diaithir", 132, BONE, stroke=6))
    for name in ("minecraft.png", "minceraft.png"):
        save(logo, MC, "textures", "gui", "title", name)
    edition = Image.new("RGBA", (512, 64), (0, 0, 0, 0))
    edition.alpha_composite(wordmark(512, 56, "Age I", 44, GOLD, stroke=3))
    save(edition, MC, "textures", "gui", "title", "edition.png")


def loading_logo():
    # LoadingOverlay draws a 4:1 wordmark from a square texture: the top half is its left half, the bottom half its right half.
    # The overlay blends additively over the night blue background, so the art sits on black.
    wide = Image.new("RGBA", (1024, 256), (0, 0, 0, 255))
    hearth = Image.open(os.path.join(SRC, "hearth.png")).convert("RGBA").crop((180, 120, 844, 640))
    hearth.putdata([(0, 0, 0, 0) if sum(abs(a - b) for a, b in zip(px, NIGHT)) < 60 else px for px in hearth.getdata()])
    wide.alpha_composite(hearth.resize((256, 200), Image.LANCZOS), (24, 28))
    wide.alpha_composite(wordmark(720, 256, "Diaithir", 150, BONE, outline=(0, 0, 0)), (290, 0))
    square = Image.new("RGBA", (512, 512))
    square.paste(wide.crop((0, 0, 512, 256)), (0, 0))
    square.paste(wide.crop((512, 0, 1024, 256)), (0, 256))
    save(square, NS, "textures", "gui", "loading_logo.png")


def window_icons():
    hearth = Image.open(os.path.join(SRC, "hearth.png")).convert("RGBA")
    for size in (16, 32, 48, 128, 256):
        save(hearth.resize((size, size), Image.LANCZOS), NS, "icons", f"icon_{size}.png")


def game_font():
    os.makedirs(os.path.join(NS, "font"), exist_ok=True)
    shutil.copy(os.path.join(SRC, "pixelify_regular.ttf"), os.path.join(NS, "font", "pixelify_sans.ttf"))
    shutil.copy(os.path.join(SRC, "OFL-PixelifySans.txt"), os.path.join(ROOT, "..", "OFL-PixelifySans.txt"))
    # Pixelify Sans draws on a grid of about 90 units per pixel and a 700 unit cap height, so size 10 puts its caps at
    # vanilla's 7 pixels; oversample keeps the edges crisp at GUI scales 2 to 4.
    provider = {"type": "ttf", "file": "diaithirbranding:pixelify_sans.ttf", "size": 10.0, "oversample": 4.0, "shift": [0.0, 0.5]}
    save_json({"providers": [provider]}, MC, "font", "default.json")


def save_json(obj, *path):
    os.makedirs(os.path.dirname(os.path.join(*path)), exist_ok=True)
    with open(os.path.join(*path), "w", encoding="utf-8", newline="\n") as f:
        json.dump(obj, f, ensure_ascii=False, indent=2)
        f.write("\n")


def texts():
    os.makedirs(os.path.join(MC, "texts"), exist_ok=True)
    with open(os.path.join(MC, "texts", "splashes.txt"), "w", encoding="utf-8", newline="\n") as f:
        f.write("\n".join(SPLASHES) + "\n")
    # NeoForge's own keys only lose to a file in its namespace from a pack loaded after it (mods.toml orders us AFTER neoforge).
    save_json({k: v for k, v in LANG.items() if not k.startswith(("fml.", "neoforge.", "pack.neoforge"))}, MC, "lang", "en_us.json")
    save_json({k: v for k, v in LANG.items() if k.startswith(("fml.", "neoforge.", "pack.neoforge"))}, ROOT, "neoforge", "lang", "en_us.json")


def theme(out):
    # The FML early window reads these from config/fml; Monocraft is swapped for Pixelify Sans and NeoForge's art is hidden.
    os.makedirs(out, exist_ok=True)
    hearth = Image.open(os.path.join(SRC, "hearth.png")).convert("RGBA")
    hearth.resize((256, 256), Image.LANCZOS).save(os.path.join(out, "diaithir_logo.png"))
    hearth.resize((64, 64), Image.LANCZOS).save(os.path.join(out, "diaithir_icon.png"))
    bg, fg = Image.new("RGBA", (40, 20)), Image.new("RGBA", (40, 20))
    for x in range(40):
        for y in range(20):
            if x < 2 or x > 37 or y < 2 or y > 17:
                bg.putpixel((x, y), GOLD + (255,))
            if 4 <= x < 36 and 4 <= y < 16:
                fg.putpixel((x, y), EMBER + (255,))
    bg.save(os.path.join(out, "diaithir_bar_bg.png"))
    fg.save(os.path.join(out, "diaithir_bar_fg.png"))
    shutil.copy(os.path.join(SRC, "pixelify_regular.ttf"), os.path.join(out, "PixelifySans.ttf"))

    def nine(resource, inset):
        return {"resource": resource, "scaling": {"type": "nine_slice", "width": 40, "height": 20, "left": inset, "top": inset,
                "right": inset, "bottom": inset, "stretchHorizontalFill": True, "stretchVerticalFill": True, "linearScaling": False}}

    def element(**kw):
        return {"visible": True, "maintainAspectRatio": True, "centerHorizontally": False, "centerVertically": False, "font": "default", **kw}

    hidden = {"type": "image", "visible": False, "font": "default"}
    save_json({
        "version": 1,
        "windowIcon": "diaithir_icon.png",
        "fonts": {"default": "PixelifySans.ttf"},
        "shaders": {name: {"vertexShader": "gui.vert", "fragmentShader": frag} for name, frag in
                    (("gui", "gui.frag"), ("font", "gui_font.frag"), ("color", "gui_color.frag"))},
        "colorScheme": {"screenBackground": "#1b1d2a", "text": "#f3e3b5", "memoryLowColor": "#c9a24a", "memoryHighColor": "#d9481c"},
        "sprites": {"progressBarBackground": nine("diaithir_bar_bg.png", 2), "progressBarForeground": nine("diaithir_bar_fg.png", 4),
                    "progressBarIndeterminate": nine("diaithir_bar_fg.png", 4), "progressBarIndeterminateBounces": False},
        "loadingScreen": {
            "background": hidden,
            "performance": element(left=220.0, top=10.0, right=220.0),
            "progressBars": element(labelGap=4, barGap=5, maintainAspectRatio=False, left=220.0, top=250.0, right=220.0),
            "startupLog": element(visible=False, left=10.0, bottom=10.0),
            "mojangLogo": element(visible=False, top=96.0, centerHorizontally=True),
            "decoration": {"logo": element(type="image", top=60.0, centerHorizontally=True, texture={
                "resource": "diaithir_logo.png", "scaling": {"type": "stretch", "width": 160, "height": 160, "linearScaling": True}})},
        },
    }, out, "theme-diaithir.json")


if __name__ == "__main__":
    ap = argparse.ArgumentParser()
    ap.add_argument("--theme-dir")
    args = ap.parse_args()
    title_textures()
    loading_logo()
    window_icons()
    game_font()
    texts()
    if args.theme_dir:
        theme(args.theme_dir)
