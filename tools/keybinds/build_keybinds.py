#!/usr/bin/env python3
"""Build the pack's default key map from a raw options.txt dumped by a real client boot.

usage: build_keybinds.py raw_options.txt out_keybinds.txt
Rule: vanilla and held-modifier keys stay; the frequent mod actions in KEEP get unique keys;
every other plain-key mod bind is unbound; leftover duplicates are unbound (first one wins).
"""
import re, sys, collections

U = 'key.keyboard.unknown'
K = lambda k, mod='': 'key.keyboard.' + k + (':' + mod if mod else '')

KEEP = {  # frequent actions, one key each
    'key.antique_atlas.open': K('m'),
    'key.sophisticatedbackpacks.open_backpack': K('b'),
    'key.curios.open.desc': K('g'),
    'key.puffish_skills.open': K('k'),
    'key.diaithir_ages.timeline': K('j'),
    'key.diaithir_ages.standing': K('o', 'ALT'),
    'key.neoorigins.skill_1': K('z'),
    'key.neoorigins.skill_2': K('x'),
    'key.neoorigins.skill_3': K('v'),
    'key.neoorigins.skill_4': K('n'),
    'key.neoorigins.class_skill': K('r', 'ALT'),
    'key.neoorigins.view_info': K('o'),
    'key.neoorigins.toggle_night_vision': K('k', 'ALT'),
    'key.mekanism.mode': K('n', 'ALT'),
    'key.mekanism.key_hud': U,
    'simplemagnets.keys.toggle': K('h'),
    'zoomify.key.zoom': K('c'),
    'key.pointblack.reload': K('r'),
    'key.pointblack.firemode': K('y'),
    'key.pointblack.attachments': K('r', 'CONTROL'),
    'key.pointblack.settings': K('y', 'ALT'),
    'key.combat_roll': U,
    'keybinds.combat_roll.roll': K('backslash'),
    'key.effortlessbuilding.undo.desc': K('z', 'CONTROL'),
    'key.effortlessbuilding.redo.desc': K('y', 'CONTROL'),
    'key.logisticsnetworks.wrench_colors': U,
    'key.logisticsnetworks.toggle_wrench_hud': U,
    'key.l2mods.swap': U,
    'key.l2mods.wheel': U,
    'key.structurize.mirror': U,
    'framedblocks.key.toggle_state_cycle': U,
    'key.quickActions': U,
    'key.push_to_talk': K('caps.lock'),
    'key.mute_microphone': K('m', 'ALT'),
    'key.voice_chat': K('v', 'ALT'),
    'key.diaithirvoice.talk': U,
    'key.toolbelt.open': K('u'),
    'key.solonion.open_food_book': K('f', 'ALT'),
    'key.ftbquests.keys.quests': K('i'),
    'key.ftbteams.open_gui': K('i', 'ALT'),
    'key.ftbultimine.default.ftbultimine': K('grave.accent'),
    'key.railcraft.loco.slower': K('comma'),
    'key.railcraft.loco.faster': K('period'),
    'key.railcraft.loco.whistle': K('slash'),
    'key.railcraft.loco.reverse': K('semicolon'),
    'key.railcraft.loco.mode': K('apostrophe'),
    'key.immersive_aircraft.boost': K('b', 'ALT'),
    'key.immersive_aircraft.dismount': K('r', 'ALT'),
    'keybind.ironjetpacks.engine': K('v', 'ALT'),
    'keybind.ironjetpacks.hover': K('h', 'ALT'),
    'key.vampirism.suck_blood': K('c', 'ALT'),
    'key.vampirism.toggle_vision': K('n', 'CONTROL'),
    'key.dragon_fireAttack': K('g', 'ALT'),
    'key.dragon_strike': K('j', 'ALT'),
    'key.dragon_down': K('c', 'CONTROL'),
    'key.saveToolbarActivator': U,
    'key.loadToolbarActivator': U,
    'key.toggleSpectatorShaderEffects': U,
}
# GUI-only or hold-modifier keys: duplicates across them are separate screens or held modifiers, leave alone
GUI = re.compile(r'(ftbquests\.gui|gui_|inventoryessentials|trashslot|craftingtweaks|integratedterminals|sfm\.|sophisticatedcore|'
                 r'jade\.|logisticsnetworks|clientsort|structurize|l2mods|\.desc$|ae2\.|modularrouters|framedblocks|silentgear\.display|'
                 r'integrateddynamics|apotheosis\.(compare|link)|mekanism\.(details|description)|extendedae|crashutils|actualladditions)')
VANILLA = re.compile(r'^key\.(attack|use|forward|left|right|back|jump|sneak|sprint|drop|inventory|chat|command|playerlist|pickItem|'
                     r'swapOffhand|screenshot|smoothCamera|fullscreen|spectatorOutlines|spectatorHotbar|advancements|togglePerspective|'
                     r'toggleGui|socialInteractions|hotbar\.\d|debug\..*|prevDestination|nextDestination)$')

lines = [l.rstrip('\n') for l in open(sys.argv[1]) if l.startswith('key_')]
binds = {}
for l in lines:
    k, v = l.split(':', 1)
    binds[k[4:]] = v
out = {}
for k, v in binds.items():
    if VANILLA.match(k):
        out[k] = v
    elif k in KEEP:
        out[k] = KEEP[k]
    elif v == U:
        out[k] = U
    elif v.endswith(('left.shift', 'left.control', 'left.alt', 'right.alt', 'right.shift')) or v.startswith('key.mouse.') or GUI.search(k):
        out[k] = v
    else:
        out[k] = U  # plain-key mod action not in KEEP
# remove remaining duplicates (outside held-modifier/GUI), first listed wins
seen = {}
for k, v in out.items():
    if v == U or VANILLA.match(k):
        if v != U: seen.setdefault(v, k)
for k, v in list(out.items()):
    if v == U or VANILLA.match(k) or GUI.search(k) or v.startswith('key.mouse.') or v.endswith(('shift', 'control', 'alt')): continue
    if v in seen: out[k] = U
    else: seen[v] = k
# hold-modifier and mouse mod keys that clash with a vanilla in-game key
for k, v in list(out.items()):
    if not VANILLA.match(k) and v in ('key.mouse.right',) and not GUI.search(k) and 'transfer' not in k: out[k] = U
with open(sys.argv[2], 'w') as f:
    for k, v in out.items(): f.write(f'key_{k}:{v}\n')
print(len(out), 'binds,', sum(1 for v in out.values() if v != U), 'bound')
