"""Spell archetypes for the foci that are ported. kind + params are interpreted by SpellEngine.java.
Costs, cast times and cooldowns come from the original SpellConfig defaults (looked up by the config name given first)."""

def fx(name, dur, amp=0):
    return [name, dur, amp]

# focus id: (spell type, config prefix, kind, params, hold, interval)
# hold=True: the spell repeats every `interval` ticks while the wand is held (breath and beam spells).
S = {
 # ---- dark wand (no element)
 "soul_bolt_focus":  ("none", "SoulBolt", "projectile", dict(proj="soul_bolt", speed=1.6, damage=5.0), False, 0),
 "magic_bolt_focus": ("none", "MagicBolt", "projectile", dict(proj="magic_bolt", speed=1.6, damage=4.0), False, 0),
 "ignite_focus":     ("none", "Ignite", "ray", dict(range=12, fire=8, particle="flame"), False, 0),
 "soul_heal_focus":  ("none", "SoulHeal", "heal", dict(amount=8.0, range=12), False, 0),
 "iron_hide_focus":  ("none", "IronHide", "buff", dict(effects=[fx("resistance", 1200, 1)]), False, 0),
 "bulwark_focus":    ("none", "Bulwark", "buff", dict(effects=[fx("absorption", 1200, 2), fx("resistance", 600, 0)]), False, 0),
 "weakening_focus":  ("none", "Weakening", "aoe", dict(radius=6, damage=0, effects=[fx("weakness", 400, 1), fx("slowness", 200, 0)], particle="smoke"), False, 0),
 "sonic_boom_focus": ("none", "SonicBoom", "ray", dict(range=16, damage=10.0, knock=1.2, particle="sonic_boom"), False, 0),
 "shockwave_focus":  ("none", "Shockwave", "aoe", dict(radius=5, damage=4.0, knock=1.4, particle="poof"), False, 0),
 "arrow_rain_focus": ("none", "ArrowRain", "rain", dict(proj="arrow", count=14, radius=4, height=12, range=24), False, 0),
 "corruption_focus": ("none", "Corruption", "ray", dict(range=14, damage=1.5, effects=[fx("wither", 60, 0)], particle="smoke"), True, 5),
 # ---- illager (ominous staff)
 "biting_focus":     ("ill", "Fang", "fangs", dict(mode="line", count=12), False, 0),
 "teeth_focus":      ("ill", "Teeth", "fangs", dict(mode="ring", count=10), False, 0),
 "shredding_focus":  ("ill", "Spike", "fangs", dict(mode="ring", count=16), False, 0),
 # ---- necromancy (necro staff)
 "rotting_focus":    ("necromancy", "Zombie", "summon", dict(mobs=["zombie_servant"], count=2, spread=3), False, 0),
 "osseous_focus":    ("necromancy", "Skeleton", "summon", dict(mobs=["skeleton_servant"], count=2, spread=3), False, 0),
 "killing_focus":    ("necromancy", "Killing", "ray", dict(range=14, damage=12.0, effects=[fx("wither", 100, 1)], particle="soul"), False, 0),
 "leeching_focus":   ("necromancy", "Leeching", "ray", dict(range=12, damage=2.5, heal_self=2.5, particle="soul"), True, 8),
 # ---- geomancy
 "temblor_clap_focus": ("geomancy", "TemblorClap", "aoe", dict(radius=6, damage=6.0, knock=1.6, up=0.5, particle="block"), False, 0),
 "earth_punch_focus":  ("geomancy", "EarthFist", "ray", dict(range=6, damage=8.0, knock=2.0, up=0.6, particle="block"), False, 0),
 "smack_stone_focus":  ("geomancy", "SmackStone", "ray", dict(range=10, damage=7.0, knock=1.0, particle="block"), False, 0),
 "eruption_focus":     ("geomancy", "Eruption", "aoe", dict(radius=5, damage=5.0, knock=0.5, up=1.0, fire=3, particle="lava"), False, 0),
 "quaking_focus":      ("geomancy", "Quaking", "aoe", dict(radius=8, damage=3.0, knock=0.8, effects=[fx("slowness", 100, 1)], particle="block"), False, 0),
 # ---- frost
 "frost_breath_focus": ("frost", "FrostBreath", "cone", dict(range=7, damage=1.5, effects=[fx("slowness", 80, 1)], freeze=140, particle="snowflake"), True, 4),
 "ice_spike_focus":    ("frost", "IceSpike", "ray", dict(range=16, damage=6.0, effects=[fx("slowness", 120, 2)], freeze=200, particle="snowflake"), False, 0),
 "hail_focus":         ("frost", "Hail", "rain", dict(proj="snowball", count=18, radius=5, height=10, range=20, damage=3.0), False, 0),
 "ice_storm_focus":    ("frost", "IceStorm", "aoe", dict(radius=7, damage=5.0, effects=[fx("slowness", 160, 2)], freeze=240, particle="snowflake"), False, 0),
 "blizzard_focus":     ("frost", "Blizzard", "aoe", dict(radius=6, damage=1.0, effects=[fx("slowness", 80, 1)], freeze=120, particle="snowflake"), True, 10),
 "chilling_focus":     ("frost", "ChillHide", "buff", dict(effects=[fx("resistance", 900, 1), fx("fire_resistance", 900, 0)]), False, 0),
 "frost_nova_focus":   ("frost", "FrostNova", "aoe", dict(radius=6, damage=4.0, knock=0.6, effects=[fx("slowness", 140, 3)], freeze=300, particle="snowflake"), False, 0),
 # ---- wind
 "launch_focus":       ("wind", "Launch", "launch", dict(up=1.4, forward=0.0), False, 0),
 "cushion_focus":      ("wind", "Cushion", "buff", dict(effects=[fx("slow_falling", 600, 0)]), False, 0),
 "updraft_focus":      ("wind", "Updraft", "buff", dict(effects=[fx("levitation", 60, 1), fx("slow_falling", 400, 0)]), False, 0),
 "wind_blast_focus":   ("wind", "WindBlast", "cone", dict(range=9, damage=0.5, knock=1.8, particle="cloud"), False, 0),
 "whirlwind_focus":    ("wind", "Whirlwind", "cone", dict(range=7, damage=1.0, knock=1.0, up=0.3, particle="cloud"), True, 4),
 "cyclone_focus":      ("wind", "Cyclone", "aoe", dict(radius=7, damage=3.0, knock=1.5, up=0.8, particle="cloud"), False, 0),
 "razor_wind_focus":   ("wind", "RazorWind", "ray", dict(range=18, damage=6.0, particle="sweep_attack"), False, 0),
 # ---- storm
 "thunderbolt_focus":  ("storm", "Thunderbolt", "lightning", dict(mode="target", range=24), False, 0),
 "lightning_focus":    ("storm", "Lightning", "lightning", dict(mode="target", range=30), False, 0),
 "thunderstorm_focus": ("storm", "Thunderstorm", "lightning", dict(mode="storm", count=6, radius=8, range=24), False, 0),
 "discharge_focus":    ("storm", "Discharge", "aoe", dict(radius=6, damage=7.0, knock=0.4, particle="electric_spark"), False, 0),
 "sprightly_focus":    ("storm", "Sprite", "buff", dict(effects=[fx("speed", 600, 2), fx("jump_boost", 600, 1)]), False, 0),
 "shocking_focus":     ("storm", "Shocking", "ray", dict(range=12, damage=3.0, effects=[fx("slowness", 40, 4), fx("weakness", 60, 1)], particle="electric_spark"), True, 6),
 # ---- nether
 "fireball_focus":     ("nether", "Fireball", "projectile", dict(proj="small_fireball", count=1, speed=1.2), False, 0),
 "blazing_focus":      ("nether", "Blaze", "projectile", dict(proj="small_fireball", count=3, spread=0.12, speed=1.2), False, 0),
 "ghastly_focus":      ("nether", "Ghast", "projectile", dict(proj="large_fireball", power=1, speed=1.0), False, 0),
 "lavaball_focus":     ("nether", "Lavaball", "projectile", dict(proj="large_fireball", power=2, speed=0.9), False, 0),
 "wither_skull_focus": ("nether", "WitherSkull", "projectile", dict(proj="wither_skull", speed=1.2), False, 0),
 "fire_breath_focus":  ("nether", "FireBreath", "cone", dict(range=7, damage=1.5, fire=4, particle="flame"), True, 4),
 "flame_strike_focus": ("nether", "FlameStrike", "ray", dict(range=14, damage=8.0, fire=6, particle="flame"), False, 0),
 "fire_blast_focus":   ("nether", "FireBlast", "aoe", dict(radius=6, damage=6.0, fire=6, knock=0.8, particle="flame"), False, 0),
 "bombardment_focus":  ("nether", "Bombardment", "rain", dict(proj="small_fireball", count=12, radius=5, height=14, range=24), False, 0),
 "meteor_shower_focus":("nether", "MeteorShower", "rain", dict(proj="large_fireball", count=5, radius=6, height=18, range=26), False, 0),
 # ---- void
 "blink_focus":        ("void", "Blink", "teleport", dict(range=24), False, 0),
 "void_flash_focus":   ("void", "VoidBomb", "aoe", dict(radius=5, damage=8.0, knock=-1.2, particle="portal"), False, 0),
 # ---- abyss
 "water_jet_focus":    ("abyss", "WaterJet", "ray", dict(range=14, damage=2.0, knock=1.0, particle="bubble"), True, 4),
 "steaming_focus":     ("abyss", "Steaming", "cone", dict(range=6, damage=1.5, effects=[fx("slowness", 40, 0)], particle="cloud"), True, 4),
 "prisma_beam_focus":  ("abyss", "PrismaBeam", "ray", dict(range=16, damage=4.0, effects=[fx("mining_fatigue", 100, 1)], particle="end_rod"), True, 6),
 "tidal_focus":        ("abyss", "Tidal", "aoe", dict(radius=6, damage=3.0, knock=1.8, particle="splash"), False, 0),
 # ---- wild
 "poison_dart_focus":  ("wild", "PoisonDart", "ray", dict(range=18, damage=2.0, effects=[fx("poison", 160, 1)], particle="item_slime"), False, 0),
 "entangling_focus":   ("wild", "Entangling", "ray", dict(range=14, damage=0.0, effects=[fx("slowness", 100, 5)], particle="happy_villager"), False, 0),
 "leaping_focus":      ("wild", "Leaping", "launch", dict(up=0.6, forward=1.5), False, 0),
}
