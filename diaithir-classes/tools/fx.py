"""Spell Engine JSON helpers for diaithir-classes (MIT). Builders follow the documented pipeline cast -> target -> deliver -> impact."""

# ---------------------------------------------------------------- fx helpers
def P(pid, count=6, shape="CIRCLE", color=None, speed=(0.05, 0.12), motion=None, anchor="ENTITY"):
    app = {}
    if color is not None:
        app["color"] = color
    if motion:
        app["motion"] = motion
    return {"id": pid, "appearance": app,
            "batch": {"count": float(count), "shape": shape, "anchor": anchor,
                      "min_speed": speed[0], "max_speed": speed[1]}}


def S(sid):
    return {"id": sid}


# per Spell Power school: sounds + base colour (RGBA int, alpha 255)
def rgba(r, g, b):
    return (r << 24) | (g << 16) | (b << 8) | 255


KIT = {
    "arcane":    dict(cast="spell_engine:generic_arcane_casting", rel="spell_engine:generic_arcane_release", hit="spell_engine:generic_arcane_release", col=rgba(150, 110, 230), par="spell_engine:magic_arcane"),
    "healing":   dict(cast="spell_engine:generic_healing_casting", rel="spell_engine:generic_healing_release", hit="spell_engine:generic_healing_impact_1", col=rgba(110, 200, 120), par="spell_engine:magic_heal"),
    "frost":     dict(cast="spell_engine:generic_frost_casting", rel="spell_engine:generic_frost_release", hit="spell_engine:generic_frost_impact", col=rgba(170, 220, 235), par="spell_engine:magic_frost"),
    "fire":      dict(cast="spell_engine:generic_fire_casting", rel="spell_engine:generic_fire_release", hit="spell_engine:generic_fire_impact_1", col=rgba(240, 130, 40), par="spell_engine:flame"),
    "lightning": dict(cast="spell_engine:generic_lightning_casting", rel="spell_engine:generic_lightning_release", hit="spell_engine:generic_lightning_release", col=rgba(240, 220, 110), par="spell_engine:magic_spark"),
    "soul":      dict(cast="spell_engine:generic_soul_casting", rel="spell_engine:generic_soul_release", hit="spell_engine:generic_soul_release", col=rgba(60, 190, 190), par="spell_engine:magic_skull"),
    "physical_melee": dict(cast="spell_engine:generic_soul_casting", rel="spell_engine:weapon_swipe_launch", hit="spell_engine:weapon_sickle_impact_small", col=rgba(150, 40, 50), par="spell_engine:magic_rage"),
}


def sp(school):
    return "spell_power:" + school


def dmg(coef, kb=0.0, **kw):
    d = {"spell_power_coefficient": coef}
    if kb:
        d["knockback"] = kb
    d.update(kw)
    return {"action": {"type": "DAMAGE", "damage": d}}


def heal(coef):
    return {"action": {"type": "HEAL", "heal": {"spell_power_coefficient": coef}}}


def fx(eid, dur, amp=0, mode="SET", caster=False, **kw):
    se = {"effect_id": eid, "duration": float(dur), "amplifier": amp, "apply_mode": mode}
    se.update(kw)
    a = {"type": "STATUS_EFFECT", "status_effect": se}
    if caster:
        a["apply_to_caster"] = True
    return {"action": a}


def cleanse():
    return {"action": {"type": "STATUS_EFFECT", "status_effect": {
        "effect_id": "minecraft:slowness", "apply_mode": "REMOVE",
        "remove": {"id": "*", "selector": "ALL", "select_beneficial": False}}}}


def burn(sec):
    return {"action": {"type": "FIRE", "fire": {"duration": float(sec)}}}


def push(frame, x, y, z, power=0.0, intent="HARMFUL", reset=False):
    return {"action": {"type": "VELOCITY", "velocity": {
        "frame": frame, "push": {"x": x, "y": y, "z": z}, "power_coefficient": power,
        "reset_velocity": reset, "intent": intent}}}


def wrap_impact(imp, school=None, particles=None, sound=None, mods=None, blend=None):
    if school:
        imp["school"] = sp(school)
    if blend:
        imp["power_blend"] = blend
    if particles:
        imp["visuals"] = {"particles": particles}
    if sound:
        imp["sound"] = S(sound)
    if mods:
        imp["target_modifiers"] = mods
    return imp


def make(school, tier, group, *, rng=30.0, cast=None, rel_anim=None, target=None, deliver=None,
         impacts=None, area_impact=None, cd=8.0, exhaust=0.2, kit=None, rel_sound=None,
         delay=None, cast_particles=True, sub_tier=None, extra=None):
    k = KIT[kit or school]
    sp_school = sp(school)
    j = {"school": sp_school, "range": float(rng), "tier": tier, "group": group, "learn": {}}
    if sub_tier is not None:
        j["sub_tier"] = sub_tier
    active = {}
    if cast is not None:
        c = dict(cast)
        if c.get("duration", 0) > 0:
            c.setdefault("sound", S(k["cast"]))
            if cast_particles:
                c.setdefault("particles", [P(k["par"], 2, "PIPE", k["col"], (0.03, 0.08), "ASCEND")])
        active["cast"] = c
    j["active"] = active
    rel = {}
    if rel_anim:
        rel["animation"] = {"id": rel_anim}
    rel["sound"] = S(rel_sound or k["rel"])
    j["release"] = rel
    j["target"] = target or {"type": "AIM", "aim": {}}
    j["deliver"] = deliver or {"type": "DIRECT"}
    if delay and deliver is None:
        j["deliver"]["delay"] = delay
    j["impacts"] = impacts or []
    if area_impact:
        j["area_impact"] = area_impact
    j["cost"] = {"exhaust": exhaust, "cooldown": {"duration": float(cd)}}
    if extra:
        j.update(extra)
    return j


CHARGE_ANIM = "spell_engine:one_handed_projectile_charge"
REL_ANIM = "spell_engine:one_handed_projectile_release"
AREA_CH = "spell_engine:one_handed_area_charge"
AREA_RL = "spell_engine:one_handed_area_release"
HEAL_CH = "spell_engine:one_handed_healing_charge"
HEAL_RL = "spell_engine:one_handed_healing_release"
SKY_CH = "spell_engine:one_handed_sky_charge"
CHANNEL = "spell_engine:two_handed_channeling"
SHOUT = "spell_engine:one_handed_shout_release"
GROUND_CH = "spell_engine:one_handed_ground_charge"


def standard(dur, anim):
    return {"duration": float(dur), "animation": {"id": anim}}


def channel(dur, ticks, anim=CHANNEL):
    return {"duration": float(dur), "type": "CHANNEL", "channel": {"ticks": ticks}, "animation": {"id": anim}}


def charge(dur, anim=CHARGE_ANIM, minr=0.3, scaling=0.5):
    return {"duration": float(dur), "type": "CHARGE", "animation": {"id": anim},
            "charge": {"min_release_ratio": minr, "output_scaling": scaling, "curve": "EASE_IN_QUAD"}}


def projectile(vel=1.2, perks=None, homing=0.0, div=0.0, offsets=None, extra_count=0, extra_delay=0,
               trail=None, light=8, travel_sound=None):
    pr = {"divergence": div, "homing_angle": homing}
    if perks:
        pr["perks"] = perks
    cd = {"light_level": light}
    if trail:
        cd["travel_particles"] = trail
    pr["client_data"] = cd
    sp_ = {"launch_properties": {"velocity": vel}, "projectile": pr}
    if extra_count:
        sp_["launch_properties"]["extra_launch_count"] = extra_count
        sp_["launch_properties"]["extra_launch_delay"] = extra_delay
    if offsets:
        sp_["direction_offsets"] = offsets
    return {"type": "PROJECTILE", "projectile": sp_}


def trail(school, n=2, col=None):
    k = KIT[school]
    return [P(k["par"], n, "CIRCLE", col or k["col"], (0.0, 0.05), anchor="ENTITY")]


def burst(school, n=10, col=None, shape="SPHERE", speed=(0.05, 0.2)):
    k = KIT[school]
    return [P(k["par"], n, shape, col or k["col"], speed, "BURST")]

def melee_spell(attacks, tier, group, cd, impacts, cast=None):
    j = make("physical_melee", tier, group, rng=0.0, cast=cast, target={"type": "NONE"},
             deliver={"type": "MELEE", "melee": {"allow_airborne": False, "attacks": attacks}},
             impacts=impacts, cd=cd, kit="physical_melee")
    j["range_mechanic"] = "MELEE"
    j["release"].pop("sound", None)
    return j
ATT_ANIM = "spell_engine:weapon_slash_uncross_swipe"


def atk(length=0.5, height=0.2, arc=140.0, delay=0.15, anim=ATT_ANIM, strikes=0, mom=0.6, **kw):
    a = {"delay": delay, "additional_strikes": strikes, "additional_strike_delay": 0.15,
         "forward_momentum": mom, "movement_slipperiness": 0.3,
         "hitbox": {"length": length, "height": height, "arc": arc},
         "animation": {"id": anim}, "swing_sound": S("spell_engine:weapon_swipe_launch"),
         "impact_sound": S("spell_engine:weapon_sickle_impact_small")}
    a.update(kw)
    return a
