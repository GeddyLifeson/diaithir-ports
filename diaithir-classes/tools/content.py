"""Content tables for diaithir-classes (MIT): four class kits, their spells, runes, rings."""
import copy
import fx
from fx import (P, S, rgba, KIT, make, dmg, heal, push, wrap_impact, standard, charge, projectile, trail, burst,
                melee_spell, atk, AREA_CH, AREA_RL, SHOUT, GROUND_CH, HEAL_CH, HEAL_RL, CHARGE_ANIM, REL_ANIM)
from fx import fx as eff

NS = "diaithir_classes"
KIT["physical_ranged"] = dict(cast="spell_engine:generic_soul_casting", rel="spell_engine:weapon_throw",
                              hit="spell_engine:weapon_dagger_impact", col=rgba(110, 170, 70), par="spell_engine:magic_rage")
PM, PR = "physical_melee", "physical_ranged"

# tier key (arms material), Age key, tier number
TIERS = [("bronze", "III", 1), ("steel", "V", 2), ("composite", "VII", 3)]
SLOTS = ["helmet", "chestplate", "leggings", "boots"]
# ingredient for the metal/ceramic part of a tier (same inputs as diaithir-arms)
PART = {"bronze": "#c:ingots/bronze", "steel": "#c:ingots/steel", "composite": "diaithirarms:ceramic_plate"}

CLASSES = {
    "fian": dict(word="warrior", rgb=(168, 48, 40), school=PM, emblem=["minecraft:red_dye", "minecraft:flint"]),
    "sleagh": dict(word="archer", rgb=(80, 140, 60), school=PR, emblem=["minecraft:green_dye", "minecraft:feather"]),
    "ceilg": dict(word="hunter", rgb=(96, 84, 130), school=PM, emblem=["minecraft:black_dye", "minecraft:string"]),
    "cleirech": dict(word="healer", rgb=(214, 190, 96), school="healing", emblem=["minecraft:white_dye", "minecraft:gold_nugget"]),
}

# weapons: class -> list of (kind, word, extras per tier, spell ids per tier)
WEAPONS = {
    "fian": [("sword", "sword"), ("spear", "spear")],
    "sleagh": [("bow", "bow"), ("sling", "sling")],
    "ceilg": [("dagger", "dagger")],
    "cleirech": [("crozier", "crook")],
}
EXTRA = {  # class -> per tier list of (attribute, amount, op)
    "fian": [[("minecraft:attack_damage", a, "add_value")] for a in (1.0, 2.0, 3.0)],
    "sleagh": [[("ranged_weapon:damage", a, "add_value")] for a in (1.0, 2.0, 3.0)],
    "ceilg": [[("minecraft:attack_damage", a, "add_value"), ("spell_power:critical_chance", c, "add_multiplied_base")]
              for a, c in ((1.0, 0.03), (2.0, 0.05), (3.0, 0.07))],
    "cleirech": [[("spell_power:healing", a, "add_value")] for a in (2.0, 4.0, 6.0)],
}
# spells carried by the weapons of a class, by tier index (cumulative)
LOADOUT = {
    "fian": [["fian_shout", "fian_charge"], ["fian_spear_cast"], ["fian_ground_blow"]],
    "sleagh": [["sleagh_volley", "sleagh_net_trap"], ["sleagh_hawk_eye"], []],
    "ceilg": [["ceilg_shadow_cloak", "ceilg_ambush"], ["ceilg_knife_fan"], ["ceilg_mark_prey"]],
    "cleirech": [["cleirech_mercy", "cleirech_aegis"], ["cleirech_smite"], ["cleirech_sanctuary"]],
}
SLING_EXTRA = ["sleagh_sling_shot"]  # the sling swaps the volley for the sling shot

# ------------------------------------------------------------------ spells
SPELLS = []  # dict(id, cls, words, desc, spell, age)


def add(sid, cls, words, desc, spell, age):
    SPELLS.append(dict(id=sid, cls=cls, words=words, desc=desc, spell=spell, age=age))


def ground_cloud(r, ttl, interval=10):
    return {"type": "CLOUD", "clouds": [{"volume": {"radius": r, "area": {"vertical_range_multiplier": 0.4}},
                                          "time_to_live_seconds": ttl, "impact_tick_interval": interval,
                                          "placement": {"force_onto_ground": True}, "spawn": {}}]}


def held_projectile(vel, spin=0.0):
    return {"type": "PROJECTILE", "projectile": {
        "launch_properties": {"velocity": vel},
        "projectile": {"homing_angle": 0.0, "travel_sound_interval": 8,
                       "client_data": {"composite_model": {"models": [
                           {"orientation": "ALONG_MOTION", "rotate_degrees_per_tick": spin, "rotate_degrees_offset": -135.0,
                            "use_held_item": True, "fx": {"light_emission": "NONE"}}]}}}}}


# ---- Fian: shouts, charges and the thrown spear
add("fian_shout", "fian", ["cry", "war"],
    "A war shout: you and allies within 8 blocks gain Strength and Resistance for 10 seconds.",
    make(PM, 0, "fian_battle", rng=8, cast=standard(0.8, AREA_CH), rel_anim=SHOUT,
         target={"type": "AREA", "area": {"include_caster": True, "vertical_range_multiplier": 0.6}},
         impacts=[eff("minecraft:strength", 10, 0), eff("minecraft:resistance", 10, 0)], cd=40.0, kit=PM), "III")
add("fian_charge", "fian", ["charge", "war"],
    "You rush forward with Speed II and a short Resistance.",
    make(PM, 1, "fian_battle", rng=0, cast=None, target={"type": "CASTER"},
         impacts=[push("LOOK", 0.0, 0.25, 1.9, reset=True, intent="HELPFUL"), eff("minecraft:speed", 3, 1),
                  eff("minecraft:resistance", 3, 0)], cd=14.0, kit=PM), "III")
_cast = {"duration": 0.8, "type": "CHARGE", "charge": {"output_scaling": 0.5, "curve": "EASE_IN_QUART", "bonus": {"range_add": 12.0}},
         "animation": {"id": "spell_engine:weapon_spearthrow_ready"}}
_sp = make(PM, 1, "fian_battle", rng=20, cast=_cast, rel_anim="spell_engine:weapon_spearthrow_toss",
           rel_sound="spell_engine:weapon_spear_throw", target={"type": "AIM", "aim": {}}, deliver=held_projectile(1.6),
           impacts=[wrap_impact(dmg(1.1, 1.5), sound="spell_engine:weapon_spear_stab")], cd=9.0, kit=PM)
add("fian_spear_cast", "fian", ["spear", "throw"],
    "Hold to draw, release to throw the spear you hold: {damage} damage with heavy knockback. A longer draw flies further.",
    _sp, "V")
add("fian_ground_blow", "fian", ["ground", "blow"],
    "You drive your weapon into the ground: foes within 6 blocks take {damage}, are thrown up and slowed.",
    make(PM, 2, "fian_battle", rng=6, cast=standard(0.9, GROUND_CH), rel_anim=AREA_RL,
         target={"type": "AREA", "area": {"vertical_range_multiplier": 0.6}},
         impacts=[dmg(0.9, 1.0), push("ORIGIN", 0.0, 0.35, 0.8), eff("minecraft:slowness", 2, 1)], cd=16.0, kit=PM), "VII")

# ---- Sleagh: volleys, traps and the sling
_arch = {"duration": 0.6, "animation": {"id": "spell_engine:archery_pull"}, "animates_ranged_weapon": True}
_vol = make(PR, 1, "sleagh_bow", rng=48, cast=_arch, rel_anim="spell_engine:archery_release", rel_sound="spell_engine:weapon_throw",
            target={"type": "AIM", "aim": {}},
            deliver={"type": "SHOOT_ARROW", "shoot_arrow": {"consume_arrow": True, "divergence": 5.0, "arrow_critical_strike": False,
                                                              "launch_properties": {"velocity": 3.0, "extra_launch_count": 3, "extra_launch_delay": 2}}},
            impacts=[dmg(0.55)], cd=12.0, kit=PR)
_vol["arrow_perks"] = {"damage_multiplier": 0.6}
add("sleagh_volley", "sleagh", ["volley"],
    "You loose four arrows in quick order, each for 60 percent of an arrow's damage. Needs arrows.", _vol, "III")
add("sleagh_net_trap", "sleagh", ["net", "trap"],
    "A hidden net of cord at the target ground for 8 seconds: foes inside are heavily slowed and take light damage.",
    make(PR, 1, "sleagh_trap", rng=24, cast=standard(0.8, GROUND_CH), rel_anim=AREA_RL, target={"type": "AIM", "aim": {}},
         deliver=ground_cloud(2.5, 8.0), impacts=[dmg(0.1), eff("minecraft:slowness", 1.5, 3)], cd=20.0, kit=PR), "III")
add("sleagh_sling_shot", "sleagh", ["stone", "shot"],
    "A sling stone: {damage} damage at range, quick to reload.",
    make(PR, 0, "sleagh_sling", rng=30, cast={"duration": 0.5, "animation": {"id": "spell_engine:one_handed_throw_charge"}},
         rel_anim="spell_engine:one_handed_throw_release", rel_sound="spell_engine:weapon_throw", target={"type": "AIM", "aim": {}},
         deliver=projectile(1.7, trail=trail(PR, 1)), impacts=[wrap_impact(dmg(0.9, 0.4), sound="spell_engine:weapon_dagger_impact")],
         cd=3.0, kit=PR), "III")
add("sleagh_hawk_eye", "sleagh", ["hawk", "eye"],
    "Foes within 16 blocks glow for 10 seconds, and you gain Speed for 8.",
    make(PR, 2, "sleagh_trap", rng=16, cast=standard(0.6, AREA_CH), rel_anim=SHOUT,
         target={"type": "AREA", "area": {"vertical_range_multiplier": 1.0}},
         impacts=[eff("minecraft:glowing", 10, 0), eff("minecraft:speed", 8, 0, caster=True)], cd=24.0, kit=PR), "V")

# ---- Ceilg: stealth and ambush
add("ceilg_shadow_cloak", "ceilg", ["shadow", "cloak"],
    "You fade from sight for 6 seconds and move faster.",
    make(PM, 0, "ceilg_shadow", rng=0, cast=None, target={"type": "CASTER"},
         impacts=[eff("minecraft:invisibility", 6, 0), eff("minecraft:speed", 6, 1)], cd=30.0, kit=PM), "III")
add("ceilg_ambush", "ceilg", ["ambush"],
    "You step behind the foe you look at and stab for {damage}.",
    make(PM, 1, "ceilg_shadow", rng=12, cast=None, target={"type": "AIM", "aim": {"required": True}},
         impacts=[{"action": {"type": "TELEPORT", "teleport": {"mode": "BEHIND_TARGET", "behind_target": {"distance": 1.5}}},
                   "sound": S("spell_engine:generic_arcane_release")},
                  wrap_impact(dmg(1.5), sound="spell_engine:weapon_dagger_impact")], cd=14.0, kit=PM), "III")
_kf = make(PM, 1, "ceilg_blades", rng=15, cast=None, rel_anim="spell_engine:weapon_dual_throw", rel_sound="spell_engine:weapon_dagger_throw",
           target={"type": "AIM", "aim": {}},
           deliver={"type": "PROJECTILE", "projectile": {
               "direction_offsets": [{"yaw": -20.0}, {}, {"yaw": 20.0}],
               "launch_properties": {"velocity": 1.3, "extra_launch_count": 1, "extra_launch_delay": 1},
               "projectile": {"homing_angle": 0.0, "travel_sound_interval": 8,
                              "client_data": {"composite_model": {"models": [
                                  {"orientation": "ALONG_MOTION", "rotate_degrees_per_tick": 0.0, "rotate_degrees_offset": -135.0,
                                   "use_held_item": True, "fx": {"light_emission": "NONE"}}]}}}}},
           impacts=[wrap_impact(dmg(0.7, 0.3), sound="spell_engine:weapon_dagger_impact")], cd=8.0, kit=PM)
add("ceilg_knife_fan", "ceilg", ["knife", "shadow"],
    "You throw six knives in a fan, each for {damage}.", _kf, "V")
add("ceilg_mark_prey", "ceilg", ["mark", "prey"],
    "Mark the foe you look at: it glows for 10 seconds, is weakened for 6 and slowed for 3.",
    make(PM, 2, "ceilg_shadow", rng=20, cast=standard(0.6, CHARGE_ANIM), rel_anim=REL_ANIM, target={"type": "AIM", "aim": {"required": True}},
         impacts=[eff("minecraft:glowing", 10, 0), eff("minecraft:weakness", 6, 0), eff("minecraft:slowness", 3, 0)], cd=24.0, kit=PM), "VII")

# ---- Cleirech: mercy and aegis
add("cleirech_mercy", "cleirech", ["mercy"],
    "A quiet word heals the one you look at (yourself if none) for {heal}.",
    make("healing", 0, "cleirech_mercy", rng=20, cast=standard(1.0, HEAL_CH), rel_anim=HEAL_RL,
         target={"type": "AIM", "aim": {"use_caster_as_fallback": True}},
         impacts=[wrap_impact(heal(0.8), particles=burst("healing", 10, shape="PIPE"), sound="spell_engine:generic_healing_impact_1")], cd=5.0), "III")
add("cleirech_aegis", "cleirech", ["aegis"],
    "A ward over the one you look at (yourself if none): Absorption II for 14 seconds and Resistance for 8.",
    make("healing", 1, "cleirech_mercy", rng=16, cast=standard(0.7, HEAL_CH), rel_anim=HEAL_RL,
         target={"type": "AIM", "aim": {"use_caster_as_fallback": True}},
         impacts=[eff("minecraft:absorption", 14, 1), eff("minecraft:resistance", 8, 0)], cd=22.0), "III")
add("cleirech_smite", "cleirech", ["smite", "light"],
    "A bolt of sunfire: {damage} fire damage and the target burns for 3 seconds.",
    make("fire", 1, "cleirech_light", rng=28, cast=standard(0.9, CHARGE_ANIM), rel_anim=REL_ANIM, target={"type": "AIM", "aim": {}},
         deliver=projectile(1.4, trail=trail("fire", 2)),
         impacts=[wrap_impact(dmg(1.0, 0.2), particles=burst("fire", 8)), fx.burn(3)], cd=8.0), "V")
add("cleirech_sanctuary", "cleirech", ["sanctuary"],
    "A ring of safe ground for 8 seconds: everyone inside is healed {heal} each second.",
    make("healing", 2, "cleirech_light", rng=20, cast=standard(1.2, GROUND_CH), rel_anim=AREA_RL,
         target={"type": "AIM", "aim": {"use_caster_as_fallback": True}}, deliver=ground_cloud(3.5, 8.0, 20),
         impacts=[heal(0.35)], cd=26.0), "VII")

# ---- passives, one per class and tier (set bonus, 4 pieces)
PASSIVE = {
    "fian": dict(words=["rage"], school=PM, trigger="DAMAGE_TAKEN", chance=(0.15, 0.20, 0.25),
                 fx=lambda t: [eff("minecraft:strength", 4 + t, 1)], target="CASTER",
                 desc="When struck there is a {c} percent chance to rage: Strength II for {d} seconds."),
    "sleagh": dict(words=["keen", "eye"], school=PR, trigger="ARROW_IMPACT", chance=(0.15, 0.20, 0.25),
                   fx=lambda t: [eff("minecraft:slowness", 1 + t, 1)], target="TARGET",
                   desc="An arrow that hits has a {c} percent chance to slow the target for {d} seconds."),
    "ceilg": dict(words=["bleed"], school=PM, trigger="MELEE_IMPACT", chance=(0.20, 0.25, 0.30),
                  fx=lambda t: [eff("minecraft:poison", 2 + t, 0)], target="TARGET",
                  desc="A melee hit has a {c} percent chance to poison the target for {d} seconds."),
    "cleirech": dict(words=["ward"], school="healing", trigger="DAMAGE_TAKEN", chance=(0.15, 0.20, 0.25),
                     fx=lambda t: [eff("minecraft:absorption", 3 + t, 1)], target="CASTER",
                     desc="When struck there is a {c} percent chance to gain Absorption II for {d} seconds."),
}
for _cls, _p in PASSIVE.items():
    for _tk, _age, _n in TIERS:
        j = make(_p["school"], 0, f"{_cls}_set", rng=0, cast=None, target={"type": "CASTER"}, impacts=_p["fx"](_n), cd=6.0,
                 kit=_p["school"] if _p["school"] in KIT else None)
        j["type"] = "PASSIVE"
        trig = {"type": _p["trigger"], "stage": "POST", "chance": _p["chance"][_n - 1]}
        if _p["target"] == "TARGET":
            trig["target_override"] = "TARGET"
        j["passive"] = {"triggers": [trig]}
        j.pop("active", None)
        _dur = int(_p["fx"](_n)[0]["action"]["status_effect"]["duration"])
        add(f"{_cls}_{_p['words'][0]}_{_tk}", _cls, _p["words"] + [_tk],
            _p["desc"].format(c=int(round(_p["chance"][_n - 1] * 100)), d=_dur), j, _age)

# ------------------------------------------------------------------ set bonuses
SET_ATTR = {  # class -> (2 piece [(attr, amounts per tier, op)], 4 piece [...])
    "fian": ([("minecraft:attack_damage", (1.0, 2.0, 3.0), "add_value")], [("minecraft:knockback_resistance", (0.1, 0.15, 0.2), "add_value")]),
    "sleagh": ([("ranged_weapon:damage", (1.0, 2.0, 3.0), "add_value")], [("minecraft:movement_speed", (0.03, 0.05, 0.07), "add_multiplied_base")]),
    "ceilg": ([("spell_power:critical_chance", (0.03, 0.05, 0.07), "add_multiplied_base")], [("minecraft:movement_speed", (0.04, 0.06, 0.08), "add_multiplied_base")]),
    "cleirech": ([("spell_power:healing", (1.0, 2.0, 3.0), "add_value")], [("minecraft:max_health", (2.0, 4.0, 6.0), "add_value")]),
}

# ------------------------------------------------------------------ runes and rings
# id, tree word, vanilla log for the recipe, attribute, amount, op, short effect text
RUNES = [
    ("beith", "birch", "minecraft:birch_log", "spell_power:haste", 0.05, "add_multiplied_base", "+5% spell haste"),
    ("luis", "rowan", "minecraft:cherry_log", "minecraft:armor", 1.0, "add_value", "+1 armour"),
    ("fearn", "alder", "minecraft:mangrove_log", "minecraft:attack_damage", 1.0, "add_value", "+1 attack damage"),
    ("sail", "willow", "minecraft:pale_oak_log", "spell_power:healing", 1.0, "add_value", "+1 healing power"),
    ("nion", "ash", "minecraft:jungle_log", "ranged_weapon:damage", 1.0, "add_value", "+1 ranged damage"),
    ("uath", "hawthorn", "minecraft:spruce_log", "minecraft:armor_toughness", 1.0, "add_value", "+1 toughness"),
    ("dair", "oak", "minecraft:oak_log", "minecraft:max_health", 2.0, "add_value", "+2 health"),
    ("tinne", "holly", "minecraft:acacia_log", "spell_power:critical_chance", 0.03, "add_multiplied_base", "+3% spell crit chance"),
    ("coll", "hazel", "minecraft:dark_oak_log", "spell_power:critical_damage", 0.08, "add_multiplied_base", "+8% spell crit damage"),
    ("iodhadh", "yew", "minecraft:crimson_stem", "minecraft:knockback_resistance", 0.1, "add_value", "+10% knockback resistance"),
    ("ruis", "elder", "minecraft:warped_stem", "spell_power:resistance.generic", 0.05, "add_value", "+5% spell resistance"),
]
RINGS = []  # id, kind, tier key, sockets, curios slot
for _tk, _age, _n in TIERS:
    RINGS.append((f"ring_{_tk}", "ring", _tk, _n, "ring"))
    RINGS.append((f"charm_{_tk}", "charm", _tk, 1 if _n < 3 else 2, "charm"))
