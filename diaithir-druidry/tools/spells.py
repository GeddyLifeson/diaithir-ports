"""Spell definitions for diaithir-druidry (MIT). Original designs from Celtic tradition.

Each school is a thematic line mapped onto the Spell Power attributes that already exist
(Spell Power schools are Java-registered, so a data-only module cannot add new attributes).
Every spell is built from the documented Spell Engine pipeline: cast -> target -> deliver -> impact.
"""

NS = "diaithir_druidry"

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


# ---------------------------------------------------------------- schools
# key, display (Diaithiris), power school(s), staff focus item, spells
SCHOOLS = []


def school(key, rod, en, power, focus, blurb):
    s = dict(key=key, rod=rod, en=en, power=power, focus=focus, blurb=blurb, spells=[])
    SCHOOLS.append(s)
    return s


def add(s, sid, rod, en, desc, spell, age):
    s["spells"].append(dict(id=sid, rod=rod, en=en, desc=desc, spell=spell, age=age))


# ===== Druid: growth, healing, binding of plants =====
d = school("druid", "Draeid", "Druid", "healing", "minecraft:moss_block",
           "The grove-keepers. They mend the hurt and bind the loud with root and thorn. Heals and plant control.")
g = "druid_grove"
w = "druid_wild"
add(d, "thornlash", "Boildidielg", "Thornlash",
    "Flings a barbed vine at a foe: {damage} nature damage and a short slow.",
    make("arcane", 0, w, rng=28, cast=standard(0.9, CHARGE_ANIM), rel_anim=REL_ANIM,
         deliver=projectile(1.4, trail=trail("healing", 2, rgba(80, 160, 70))),
         impacts=[wrap_impact(dmg(0.6, 0.2), particles=burst("healing", 8, rgba(80, 160, 70)), sound="spell_engine:generic_poison_impact"),
                  fx("minecraft:slowness", 2.5, 0)], cd=3.0, kit="healing"), "III")
add(d, "dewdrop", "Driùchdu", "Dewdrop",
    "A bead of cool dew heals the one you look at (yourself if none): {heal} health.",
    make("healing", 0, g, rng=20, cast=standard(1.0, HEAL_CH), rel_anim=HEAL_RL,
         target={"type": "AIM", "aim": {"use_caster_as_fallback": True}},
         impacts=[wrap_impact(heal(0.7), particles=burst("healing", 10, shape="PIPE"), sound="spell_engine:generic_healing_impact_1")], cd=4.0), "III")
add(d, "briar_snare", "Ribidries", "Briar Snare",
    "Briars burst from the ground at the target spot and hold anything in them: slow and light damage each pulse.",
    make("arcane", 1, w, rng=24, cast=standard(1.0, GROUND_CH), rel_anim=AREA_RL,
         target={"type": "AIM", "aim": {}},
         deliver={"type": "CLOUD", "clouds": [{"volume": {"radius": 3.0, "area": {"vertical_range_multiplier": 0.4}},
                                               "time_to_live_seconds": 6.0, "impact_tick_interval": 10,
                                               "placement": {"force_onto_ground": True}, "spawn": {}}]},
         impacts=[dmg(0.18), fx("minecraft:slowness", 1.5, 2)], cd=14.0, kit="healing"), "III")
add(d, "oakskin", "Kraikenddaurach", "Oakskin",
    "Your skin takes on oak bark for 12 seconds: resistance, at the cost of slowness.",
    make("healing", 1, g, rng=0, cast=standard(0.6, HEAL_CH), rel_anim=HEAL_RL, target={"type": "CASTER"},
         impacts=[fx("minecraft:resistance", 12, 0), fx("minecraft:slowness", 12, 0)], cd=28.0), "III")
add(d, "grove_mending", "Slànachaddoiri", "Grove Mending",
    "A ring of green light heals you and every ally close by, and leaves a slow regrowth.",
    make("healing", 2, g, rng=7, cast=standard(1.4, AREA_CH), rel_anim=AREA_RL,
         target={"type": "AREA", "area": {"include_caster": True, "vertical_range_multiplier": 0.6}},
         impacts=[heal(0.6), fx("minecraft:regeneration", 5, 0)], cd=16.0), "IV")
add(d, "stag_leap", "Leimdaum", "Stag Leap",
    "You bound forward and up like a stag and take no damage from the fall that follows.",
    make("healing", 2, w, rng=0, cast=None, target={"type": "CASTER"},
         impacts=[push("LOOK", 0.0, 0.75, 1.3, reset=True, intent="HELPFUL"),
                  fx("minecraft:slow_falling", 4, 0)], cd=10.0, kit="healing"), "IV")
add(d, "mistletoe_spear", "Sliegoilìuk", "Mistletoe Spear",
    "Hold to draw a white-berried spear of pith and thorn. A full draw pierces two foes and poisons them.",
    make("arcane", 3, w, rng=34, cast=charge(1.6), rel_anim=REL_ANIM,
         deliver=projectile(1.8, perks={"pierce": 2}, trail=trail("healing", 3, rgba(220, 235, 200))),
         impacts=[wrap_impact(dmg(1.3, 0.3), particles=burst("healing", 12, rgba(220, 235, 200))),
                  fx("minecraft:poison", 5, 0)], cd=12.0, kit="healing"), "V")

# ===== Ovate: seership, sight, foreknowledge =====
o = school("ovate", "Fiusaichi", "Ovate", "arcane", "minecraft:amethyst_shard",
           "The seers. They see what is hidden and strike where a blow is already foretold. Sight, marks and blinks.")
g = "ovate_sight"
w = "ovate_omen"
add(o, "sight_spark", "Sraudagsieldad", "Spark of Sight",
    "A bright flicker that stings for {damage} and leaves the target outlined for 6 seconds.",
    make("arcane", 0, w, rng=32, cast=standard(0.9, CHARGE_ANIM), rel_anim=REL_ANIM,
         deliver=projectile(1.5, homing=1.0, trail=trail("arcane", 2)),
         impacts=[wrap_impact(dmg(0.6, 0.1), particles=burst("arcane", 8)), fx("minecraft:glowing", 6, 0)], cd=3.0), "III")
add(o, "far_sight", "Fradurk", "Far Sight",
    "Your eyes open to the dark for 40 seconds: night vision.",
    make("arcane", 0, g, rng=0, cast=standard(0.5, HEAL_CH), rel_anim=HEAL_RL, target={"type": "CASTER"},
         impacts=[fx("minecraft:night_vision", 40, 0)], cd=45.0), "III")
add(o, "fate_thread", "Snàddànu", "Thread of Fate",
    "You pull on the target's thread: it is weakened and slowed for 8 seconds and takes {damage}.",
    make("arcane", 1, w, rng=26, cast=standard(0.8, CHARGE_ANIM), rel_anim=REL_ANIM,
         target={"type": "AIM", "aim": {"required": True}},
         impacts=[wrap_impact(dmg(0.35), particles=burst("arcane", 10, rgba(180, 160, 240))),
                  fx("minecraft:weakness", 8, 0), fx("minecraft:slowness", 8, 0)], cd=12.0), "III")
add(o, "veil_step", "Keimskàili", "Veil Step",
    "You step through the veil and appear 8 blocks ahead.",
    make("arcane", 1, g, rng=0, cast=None, target={"type": "CASTER"},
         impacts=[{"action": {"type": "TELEPORT", "teleport": {"mode": "FORWARD", "forward": {"distance": 8.0}}},
                   "visuals": {"particles": burst("arcane", 14, shape="PIPE")}, "sound": S("spell_engine:generic_arcane_release")}],
         cd=14.0), "III")
add(o, "salmon_knowing", "Eòlasubradun", "Salmon's Knowing",
    "Channel for 2.5 seconds: a stream of bright knowing cuts through everything in a line.",
    make("arcane", 2, w, rng=24, cast=channel(2.5, 10),
         target={"type": "BEAM", "beam": {"color_rgba": rgba(190, 150, 250), "width": 0.08, "flow": 1.4}},
         impacts=[dmg(1.0)], cd=11.0), "IV")
add(o, "watch_hour", "Fairi", "Vigil",
    "Everything hostile within 14 blocks glows for 10 seconds, even behind stone.",
    make("arcane", 2, g, rng=14, cast=standard(1.0, AREA_CH), rel_anim=SHOUT,
         target={"type": "AREA", "area": {"vertical_range_multiplier": 1.0}},
         impacts=[fx("minecraft:glowing", 10, 0)], cd=24.0), "IV")
add(o, "foretold_blow", "Boildibruodur", "Foretold Blow",
    "You name a place and the blow falls there a heartbeat later: {damage} to everything in 3 blocks.",
    make("arcane", 3, w, rng=30, cast=standard(1.6, SKY_CH), rel_anim=AREA_RL,
         target={"type": "AIM", "aim": {}},
         deliver={"type": "METEOR", "meteor": {"launch_height": 14.0, "projectile": {"client_data": {
             "travel_particles": trail("arcane", 3)}}}},
         impacts=[wrap_impact(dmg(1.3, 0.5), particles=burst("arcane", 18))],
         area_impact={"radius": 3.0, "area": {"distance_dropoff": "SQUARED"}}, cd=16.0), "V")

# ===== Bard: sound, lightning of the voice, tempo =====
b = school("bard", "Bàrdu", "Bard", "lightning", "minecraft:bell",
           "The singers. Three strains (sleep, lament, laughter) and their thunder. Crowd control and rally.")
g = "bard_strain"
w = "bard_thunder"
add(b, "sharp_note", "Bilegtàirnenach", "Sharp Note",
    "One struck note travels as a crackle and does {damage}.",
    make("lightning", 0, w, rng=30, cast=standard(0.8, CHARGE_ANIM), rel_anim=REL_ANIM,
         deliver=projectile(1.7, trail=trail("lightning", 2)),
         impacts=[wrap_impact(dmg(0.6, 0.3), particles=burst("lightning", 8))], cd=2.5), "III")
add(b, "lullaby", "Tàlad", "Lullaby",
    "The target's limbs go heavy: slowness and weakness for 6 seconds, and it forgets its hurry.",
    make("lightning", 0, g, rng=22, cast=standard(1.0, HEAL_CH), rel_anim=HEAL_RL,
         target={"type": "AIM", "aim": {"required": True}},
         impacts=[wrap_impact(fx("minecraft:slowness", 6, 2), particles=burst("soul", 6, shape="PIPE")),
                  fx("minecraft:weakness", 6, 0)], cd=14.0, kit="lightning"), "III")
add(b, "lament", "Kuma", "Lament",
    "A long falling strain: nearby foes stagger back and are struck by dread: nausea for 4 seconds.",
    make("lightning", 1, g, rng=6, cast=standard(1.2, AREA_CH), rel_anim=SHOUT,
         target={"type": "AREA", "area": {"vertical_range_multiplier": 0.6}},
         impacts=[dmg(0.4, 0.8), fx("minecraft:nausea", 4, 0), push("ORIGIN", 0.0, 0.2, 0.7)], cd=15.0), "III")
add(b, "laughter", "Gàiri", "Laughter",
    "A bright strain mends you and each ally near you for {heal} and quickens them for 4 seconds.",
    make("healing", 1, g, rng=7, cast=standard(1.2, AREA_CH), rel_anim=AREA_RL,
         target={"type": "AREA", "area": {"include_caster": True, "vertical_range_multiplier": 0.6}},
         impacts=[heal(0.45), fx("minecraft:speed", 4, 0)], cd=15.0), "III")
add(b, "horn_cry", "Adurkglaed", "Horn Cry",
    "A great horn blast hammers the area in front of you: {damage} and a heavy shove.",
    make("lightning", 2, w, rng=9, cast=standard(1.0, AREA_CH), rel_anim=SHOUT,
         target={"type": "AREA", "area": {"vertical_range_multiplier": 0.5}},
         impacts=[dmg(0.85, 0.2), push("ORIGIN", 0.0, 0.3, 1.1)], cd=13.0), "IV")
add(b, "banner_song", "Òrunbrautach", "Banner Song",
    "A marching song for 15 seconds: you and nearby allies gain speed and haste.",
    make("healing", 2, g, rng=9, cast=standard(1.0, AREA_CH), rel_anim=SHOUT,
         target={"type": "AREA", "area": {"include_caster": True, "vertical_range_multiplier": 0.6}},
         impacts=[fx("minecraft:speed", 15, 0), fx("minecraft:haste", 15, 0)], cd=40.0), "IV")
add(b, "storm_chord", "Koseirmstoirmi", "Storm Chord",
    "Hold a chord for 3 seconds. The sky answers along your aim and each moment of the chord strikes.",
    make("lightning", 3, w, rng=28, cast=channel(3.0, 8),
         target={"type": "BEAM", "beam": {"color_rgba": rgba(255, 245, 160), "width": 0.12, "flow": 2.0}},
         impacts=[dmg(1.1, 0.1)], cd=15.0), "V")

# ===== Filid: word-craft, satire, truth =====
f = school("filid", "Filid", "Filid", "soul", "minecraft:ink_sac",
           "The poets of law and satire. Their words wound, bind and shield. Curses, wards and cleansing.")
g = "filid_word"
w = "filid_satire"
add(f, "verse_blow", "Boildirand", "Verse Blow",
    "A hard line of verse flies out: {damage} and a short weakness.",
    make("soul", 0, w, rng=28, cast=standard(0.9, CHARGE_ANIM), rel_anim=REL_ANIM,
         deliver=projectile(1.5, trail=trail("soul", 2)),
         impacts=[wrap_impact(dmg(0.6, 0.1), particles=burst("soul", 8)), fx("minecraft:weakness", 3, 0)], cd=3.0), "III")
add(f, "ward_verse", "Randskiad", "Ward Verse",
    "A spoken ward gives you a shield of 3 extra hearts for 20 seconds.",
    make("soul", 0, g, rng=0, cast=standard(0.8, HEAL_CH), rel_anim=HEAL_RL, target={"type": "CASTER"},
         impacts=[fx("minecraft:absorption", 20, 1)], cd=30.0), "III")
add(f, "satire", "Aeiri", "Satire",
    "A cutting rhyme raises blisters on the proud: slowness, weakness and {damage} to the target.",
    make("soul", 1, w, rng=24, cast=standard(1.0, CHARGE_ANIM), rel_anim=REL_ANIM,
         target={"type": "AIM", "aim": {"required": True}},
         impacts=[wrap_impact(dmg(0.45), particles=burst("soul", 12, shape="PIPE")),
                  fx("minecraft:slowness", 8, 1), fx("minecraft:weakness", 8, 1)], cd=14.0), "III")
add(f, "truth_word", "Fìrind", "Truth-Word",
    "A true word spoken over you and your allies strips slowing and harmful effects and heals {heal}.",
    make("soul", 1, g, rng=7, cast=standard(1.2, AREA_CH), rel_anim=AREA_RL,
         target={"type": "AREA", "area": {"include_caster": True, "vertical_range_multiplier": 0.6}},
         impacts=[cleanse(), heal(0.3)], cd=20.0), "III")
add(f, "hundred_verses", "Randkeid", "Hundred Verses",
    "A rapid recital: five small bolts fan out in a spread, each doing {damage}.",
    make("soul", 2, w, rng=26, cast=standard(1.3, CHARGE_ANIM), rel_anim=REL_ANIM,
         deliver=projectile(1.4, trail=trail("soul", 1), offsets=[{"yaw": -24.0}, {"yaw": -12.0}, {}, {"yaw": 12.0}, {"yaw": 24.0}],
                            extra_count=1, extra_delay=3),
         impacts=[wrap_impact(dmg(0.28, 0.05), particles=burst("soul", 5))], cd=11.0), "IV")
add(f, "ban_oath", "Toirmesk", "Ban",
    "You lay a ban on the target: withering for 5 seconds and hunger for 15.",
    make("soul", 2, g, rng=22, cast=standard(1.2, CHARGE_ANIM), rel_anim=REL_ANIM,
         target={"type": "AIM", "aim": {"required": True}},
         impacts=[wrap_impact(dmg(0.5), particles=burst("soul", 12)), fx("minecraft:wither", 5, 0),
                  fx("minecraft:hunger", 15, 1)], cd=22.0), "IV")
add(f, "curse_pall", "Maldachd", "Pall of Curses",
    "A dark cloud of ill words settles on the target ground for 7 seconds and keeps cursing what stands in it.",
    make("soul", 3, w, rng=24, cast=standard(1.5, GROUND_CH), rel_anim=AREA_RL,
         target={"type": "AIM", "aim": {}},
         deliver={"type": "CLOUD", "clouds": [{"volume": {"radius": 3.5, "area": {"vertical_range_multiplier": 0.5}},
                                               "time_to_live_seconds": 7.0, "impact_tick_interval": 10,
                                               "placement": {"force_onto_ground": True}, "spawn": {}}]},
         impacts=[dmg(0.3), fx("minecraft:blindness", 2, 0), fx("minecraft:slowness", 2, 0)], cd=20.0), "V")

# ===== Fitech (raven): war-craft, melee-flavoured =====
r = school("fitech", "Fitech", "Raven", "physical_melee", "minecraft:feather",
           "The war-crow's way. Short sharp melee strikes, battle shouts and fear. Scales with melee power.")
g = "fitech_battle"
w = "fitech_terror"
ATT_ANIM = "spell_engine:weapon_slash_uncross_swipe"


def melee_spell(attacks, tier, group, cd, impacts, cast=None):
    j = make("physical_melee", tier, group, rng=0.0, cast=cast, target={"type": "NONE"},
             deliver={"type": "MELEE", "melee": {"allow_airborne": False, "attacks": attacks}},
             impacts=impacts, cd=cd, kit="physical_melee")
    j["range_mechanic"] = "MELEE"
    j["release"].pop("sound", None)
    return j


def atk(length=0.5, height=0.2, arc=140.0, delay=0.15, anim=ATT_ANIM, strikes=0, mom=0.6, **kw):
    a = {"delay": delay, "additional_strikes": strikes, "additional_strike_delay": 0.15,
         "forward_momentum": mom, "movement_slipperiness": 0.3,
         "hitbox": {"length": length, "height": height, "arc": arc},
         "animation": {"id": anim}, "swing_sound": S("spell_engine:weapon_swipe_launch"),
         "impact_sound": S("spell_engine:weapon_sickle_impact_small")}
    a.update(kw)
    return a


add(r, "crow_strike", "Boildifiendag", "Crow Strike",
    "Two quick strikes, like a crow at carrion. Each hits for {damage}.",
    melee_spell([atk(strikes=1)], 0, w, 5.0, [dmg(0.55, 0.1)]), "III")
add(r, "battle_cry", "Glaedkad", "Battle Cry",
    "A raven-throated shout: you and allies near you gain strength for 10 seconds.",
    make("physical_melee", 0, g, rng=8, cast=standard(0.8, AREA_CH), rel_anim=SHOUT,
         target={"type": "AREA", "area": {"include_caster": True, "vertical_range_multiplier": 0.6}},
         impacts=[fx("minecraft:strength", 10, 0)], cd=30.0, kit="physical_melee"), "III")
add(r, "raven_leap", "Leimfitech", "Raven Leap",
    "You throw yourself forward in a low leap with a speed burst.",
    make("physical_melee", 1, g, rng=0, cast=None, target={"type": "CASTER"},
         impacts=[push("LOOK", 0.0, 0.45, 1.5, reset=True, intent="HELPFUL"), fx("minecraft:speed", 3, 1)],
         cd=9.0, kit="physical_melee"), "III")
add(r, "battle_fury", "Bàinid", "Fury",
    "You fall into fury for 8 seconds: strong blows, but you take more harm (weaker defence).",
    make("physical_melee", 1, g, rng=0, cast=standard(0.5, HEAL_CH), rel_anim=SHOUT, target={"type": "CASTER"},
         impacts=[fx("minecraft:strength", 8, 1), fx("minecraft:hunger", 8, 1)], cd=26.0, kit="physical_melee"), "III")
add(r, "talon_wheel", "Roudspoiri", "Wheel of Talons",
    "A full spin: three sweeping cuts that hit everything around you for {damage} each.",
    melee_spell([atk(length=1.6, height=0.6, arc=360.0, strikes=2, mom=0.2, anim="spell_engine:weapon_cleave")],
                2, w, 12.0, [dmg(0.5, 0.35)]), "IV")
add(r, "field_terror", "Oildti", "Terror of the Field",
    "Dread shakes nearby foes: they are shoved back and weakened for 6 seconds.",
    make("physical_melee", 2, w, rng=7, cast=standard(0.9, AREA_CH), rel_anim=SHOUT,
         target={"type": "AREA", "area": {"vertical_range_multiplier": 0.6}},
         impacts=[dmg(0.3, 0.5), push("ORIGIN", 0.0, 0.3, 0.9), fx("minecraft:weakness", 6, 1), fx("minecraft:slowness", 3, 0)],
         cd=20.0, kit="physical_melee"), "IV")
add(r, "ford_washer", "Àdbàsu", "Washer at the Ford",
    "A single heavy stroke. Foes under a third of their health take triple damage.",
    melee_spell([atk(length=0.9, height=0.4, arc=100.0, delay=0.3, anim="spell_engine:weapon_one_handed_slam", mom=0.8,
                     impact_sound=S("spell_engine:weapon_claymore_impact"))],
                3, w, 16.0,
                [wrap_impact(dmg(1.2, 0.3), mods=[
                    {"all_required": True, "conditions": [{"health_percent_below": 0.34}], "execute": "PASS",
                     "modifier": {"power_multiplier": 3.0}}])]), "V")

# ===== Sìtichi (Otherworld, hollow-hill folk): frost, glamour, step =====
s_ = school("sidhe", "Sìtichi", "Otherworld", "frost", "minecraft:prismarine_crystals",
            "The hollow-hill folk. Mist, glamour and gifts that come with a price. Control and mobility.")
g = "sidhe_glamour"
w = "sidhe_frost"
add(s_, "fairy_mist", "Kietach", "Fairy Mist",
    "A cold white mist rises where you point: it clings, slows and nips for 6 seconds.",
    make("frost", 0, w, rng=22, cast=standard(0.9, GROUND_CH), rel_anim=AREA_RL,
         target={"type": "AIM", "aim": {}},
         deliver={"type": "CLOUD", "clouds": [{"volume": {"radius": 3.0, "area": {"vertical_range_multiplier": 0.5}},
                                               "time_to_live_seconds": 6.0, "impact_tick_interval": 10,
                                               "placement": {"force_onto_ground": True}, "spawn": {}}]},
         impacts=[dmg(0.12), fx("minecraft:slowness", 1.5, 0)], cd=12.0), "III")
add(s_, "glamour_veil", "Loindskàili", "Glamour",
    "A veil of glamour: you are unseen for 6 seconds and quick for 3.",
    make("frost", 0, g, rng=0, cast=standard(0.5, HEAL_CH), rel_anim=HEAL_RL, target={"type": "CASTER"},
         impacts=[fx("minecraft:invisibility", 6, 0), fx("minecraft:speed", 3, 0)], cd=35.0), "III")
add(s_, "hill_step", "Keimknok", "Hill Step",
    "You step inside the hill and out again 12 blocks ahead.",
    make("frost", 1, g, rng=0, cast=None, target={"type": "CASTER"},
         impacts=[{"action": {"type": "TELEPORT", "teleport": {"mode": "FORWARD", "forward": {"distance": 12.0}}},
                   "visuals": {"particles": burst("frost", 14, shape="PIPE")}, "sound": S("spell_engine:generic_frost_release")}],
         cd=16.0), "III")
add(s_, "folk_gift", "Tiudlak", "Folk-Gift",
    "A gift from the hill: the target regains {heal} health over 10 seconds, and is lightly shielded.",
    make("healing", 1, g, rng=22, cast=standard(1.0, HEAL_CH), rel_anim=HEAL_RL,
         target={"type": "AIM", "aim": {"use_caster_as_fallback": True}},
         impacts=[heal(0.35), fx("minecraft:regeneration", 10, 1), fx("minecraft:absorption", 10, 0)], cd=18.0, kit="healing"), "III")
add(s_, "silver_branch", "Geigairged", "Silver Branch",
    "A bright branch flies out and passes through two foes. Each is chilled and takes {damage}.",
    make("frost", 2, w, rng=34, cast=standard(1.2, CHARGE_ANIM), rel_anim=REL_ANIM,
         deliver=projectile(1.7, perks={"pierce": 2}, trail=trail("frost", 3)),
         impacts=[wrap_impact(dmg(0.8, 0.2), particles=burst("frost", 10)), fx("minecraft:slowness", 3, 1)], cd=8.0), "IV")
add(s_, "folk_hour", "Uoirsìtichi", "Hour of the Folk",
    "Time thickens around you: foes within 7 blocks crawl for 3 seconds and are nipped by frost.",
    make("frost", 2, w, rng=7, cast=standard(1.0, AREA_CH), rel_anim=AREA_RL,
         target={"type": "AREA", "area": {"vertical_range_multiplier": 0.7}},
         impacts=[dmg(0.3), fx("minecraft:slowness", 3, 4), fx("minecraft:mining_fatigue", 5, 1)], cd=22.0), "IV")
add(s_, "hail_fall", "Klachmelduin", "Hailfall",
    "A hail of stones falls around the target spot: {damage} to each foe struck, with a chill.",
    make("frost", 3, w, rng=28, cast=standard(1.4, SKY_CH), rel_anim=AREA_RL,
         target={"type": "AIM", "aim": {}},
         deliver={"type": "METEOR", "meteor": {"launch_height": 12.0, "launch_radius": 3.0,
                                               "launch_properties": {"extra_launch_count": 4, "extra_launch_delay": 4},
                                               "projectile": {"divergence": 6.0, "client_data": {"travel_particles": trail("frost", 3)}}}},
         impacts=[wrap_impact(dmg(0.5, 0.2), particles=burst("frost", 10)), fx("minecraft:slowness", 2, 1)],
         area_impact={"radius": 2.5, "area": {"distance_dropoff": "SQUARED"}}, cd=18.0), "V")

# ===== Gouba (Smith): fire, forge, temper =====
m = school("smith", "Gouba", "Smith", "fire", "minecraft:blaze_powder",
           "The forge-masters. Fire from the hearth: sparks, hot iron and the long pour. Damage over time and self-tempering.")
g = "smith_temper"
w = "smith_fire"
add(m, "forge_spark", "Sraudag", "Forge Spark",
    "A spark spits from the fire: {damage} and burning for 3 seconds.",
    make("fire", 0, w, rng=26, cast=standard(0.8, CHARGE_ANIM), rel_anim=REL_ANIM,
         deliver=projectile(1.5, trail=trail("fire", 2)),
         impacts=[wrap_impact(dmg(0.55, 0.15), particles=burst("fire", 8)), burn(3)], cd=2.5), "III")
add(m, "quench", "Kaisk", "Quench",
    "You plunge your hands in the quench-trough: fire resistance for 40 seconds and any fire on you goes out.",
    make("fire", 0, g, rng=0, cast=standard(0.6, HEAL_CH), rel_anim=HEAL_RL, target={"type": "CASTER"},
         impacts=[fx("minecraft:fire_resistance", 40, 0)], cd=45.0), "III")
add(m, "hot_iron", "Iarnaigitied", "Hot Iron",
    "Hold to heat the iron. A full draw hurls a white-hot bolt that does {damage} and sets the foe alight.",
    make("fire", 1, w, rng=32, cast=charge(1.5), rel_anim=REL_ANIM,
         deliver=projectile(1.6, trail=trail("fire", 3)),
         impacts=[wrap_impact(dmg(1.0, 0.4), particles=burst("fire", 12)), burn(5)], cd=8.0), "III")
add(m, "tempering", "Kaisiiarnaigi", "Tempering",
    "You temper your body for 10 seconds: haste, strength and resistance, then a short tiredness.",
    make("fire", 1, g, rng=0, cast=standard(0.6, HEAL_CH), rel_anim=HEAL_RL, target={"type": "CASTER"},
         impacts=[fx("minecraft:haste", 10, 1), fx("minecraft:strength", 10, 0), fx("minecraft:resistance", 10, 0)], cd=40.0), "III")
add(m, "slag_cloud", "Skòdsaliaruind", "Slag Cloud",
    "A cloud of hot slag settles at the target spot for 6 seconds and keeps burning what stands in it.",
    make("fire", 2, w, rng=24, cast=standard(1.2, GROUND_CH), rel_anim=AREA_RL,
         target={"type": "AIM", "aim": {}},
         deliver={"type": "CLOUD", "clouds": [{"volume": {"radius": 3.0, "area": {"vertical_range_multiplier": 0.5}},
                                               "time_to_live_seconds": 6.0, "impact_tick_interval": 10,
                                               "placement": {"force_onto_ground": True}, "spawn": {}}]},
         impacts=[dmg(0.28), burn(3)], cd=16.0), "IV")
add(m, "forge_breath", "Anuilkum", "Forge Breath",
    "Channel the bellows for 2.5 seconds: a lance of heat along your aim, burning all it crosses.",
    make("fire", 2, w, rng=18, cast=channel(2.5, 10),
         target={"type": "BEAM", "beam": {"color_rgba": rgba(255, 150, 50), "width": 0.1, "flow": 1.6}},
         impacts=[dmg(0.9), burn(2)], cd=12.0), "IV")
add(m, "molten_rain", "Oiskiliegti", "Molten Rain",
    "A rain of molten iron falls around the target spot: {damage} to each drop's blast and burning.",
    make("fire", 3, w, rng=28, cast=standard(1.5, SKY_CH), rel_anim=AREA_RL,
         target={"type": "AIM", "aim": {}},
         deliver={"type": "METEOR", "meteor": {"launch_height": 12.0, "launch_radius": 3.5,
                                               "launch_properties": {"extra_launch_count": 4, "extra_launch_delay": 5},
                                               "projectile": {"divergence": 6.0, "client_data": {"travel_particles": trail("fire", 3)}}}},
         impacts=[wrap_impact(dmg(0.75, 0.25), particles=burst("fire", 12)), burn(3)],
         area_impact={"radius": 2.5, "area": {"distance_dropoff": "SQUARED"}}, cd=22.0), "V")
