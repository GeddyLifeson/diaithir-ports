"""64x replacements for the old 16x texture helpers, so gen.py keeps its call sites."""
import art
from art import STEEL, STEEL_D, HAZ_Y, BRASS, mixc, sh, rng as _rng
from tex import PAL, DYES, STONES

_DYE_VALUES = set(DYES.values())

def plate(color, name, rivet=True): return art.plate_tex(name, color, rivet)
def casing(color, name, trim=None): return art.casing_tex(name, color)
EMB = {"encased_fan": "fan", "mechanical_saw": "saw", "mechanical_drill": "drill", "speedometer": "gauge", "stressometer": "gauge_stress", "millstone": "millstone",
       "mechanical_pump": "pump", "spout": "spout", "item_drain": "drain", "fluid_tank": "tank", "hand_crank": "crank", "crushing_wheel": "cog", "crushing_wheel_controller": "cog"}

def machine(color, name, accent=None, trim=None):
    if name in EMB:
        return art.machine_emblem_face(name, EMB[name])
    if tuple(color) in _DYE_VALUES:
        return art.machine_face(name, mixc(STEEL, color, 0.6), mixc(color, (255, 255, 255), 0.2))
    acc = accent or (color if tuple(color) != tuple(PAL["steel"]) else HAZ_Y)
    return art.machine_face(name, STEEL, acc)
def glass(frame, name, pattern="frame"): return art.glass_tex(name, frame, pattern)
def weave(color, name): return art.cloth_tex(name, color)
def shingles(color, name): return art.shingle_tex(name, color)
def tiles(color, name): return art.tile_tex(name, color)
def ore(base, name, fleck=(200, 200, 210)): return art.ore_tex(name, base, fleck)
def pipe_tex(color, name): return art.pipe_tex(name, color)
def gear_tex(color, name): return art.metal_flat(name, color, 0.35, 0.0, "gear")
def speckle(color, name, var=0.1, size=16): return art.metal_flat(name, color, 0.3)
