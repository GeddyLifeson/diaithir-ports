#!/usr/bin/env python3
"""Writes the compute-lane job for Diaithiris names + Library entries of the 1a addons (createaddition, createdeco)."""
import json, os
OUT = '/mnt/project-files/compute/jobs/create_addons_1a_names'
SYSTEM = ("You name items in Diaithiris, a Gaelic-family island language. Give a classical-spelling Gaelic compound noun "
          "(one word, letters only; head noun first then modifier, compounds written as one word like Dobhranfamhair) for the item, "
          "using real Gaelic roots (e.g. wire=sreang/uèir, rod=slat/maide, plate=pláta/leacán, spool=iomramh/cuachán, motor=inneal/mótar, "
          "electric=dealain/dealanach, lightning=tintreach/dealán, coil=corcán/lúb, generator=gineadóir/inneal, tower=túr, bar=bara, "
          "ladder=dréimire, lamp=lampa/solas, door=doras, window=fuinneog, brick=bríce/breigeach, catwalk=cabhsa/ardchosán, coin=bonn/pighinn, "
          "zinc=since, copper=copar, brass=práis, iron=iarann, andesite=carraigghlas, cake=cáca, honey=mil). "
          "Then write a Library entry of 2 sentences in plain English: what it is, that it needs Create (Age IV), what it does, with the numbers given. "
          "Output strict JSON: {\"classical\":\"...\",\"entry\":\"...\"}")
ADD = {
 'electric_motor': ('Electric Motor', 'a motor that turns FE into rotation: up to +/-256 RPM, needs at least 8 FE/t, 5,000 FE buffer, adjustable speed; stress capacity 16,384 SU at 256 RPM'),
 'alternator': ('Alternator', 'a generator that turns Create rotation into FE at 75% efficiency, up to 5,000 FE/t; 16,384 SU at 256 RPM'),
 'rolling_mill': ('Rolling Mill', 'a rotation-driven machine that rolls ingots into plates and rods; 120 ticks per item, 8 SU stress'),
 'creative_energy': ('Creative Energy Source', 'a creative-only block that supplies unlimited FE'),
 'connector': ('Small Connector', 'a wall connector that joins wires: 1,000 FE/t in or out, wires up to 16 blocks'),
 'small_light_connector': ('Small Light Connector', 'a small connector that also lights up, drawing 1 FE/t'),
 'large_connector': ('Large Connector', 'a heavy connector: 5,000 FE/t, wires up to 32 blocks, needs high-current wire'),
 'redstone_relay': ('Redstone Relay', 'a wire relay that cuts the line when it gets a redstone signal'),
 'chocolate_cake': ('Chocolate Cake', 'a cake made with chocolate'),
 'honey_cake': ('Honey Cake', 'a cake sweetened with honey'),
 'barbed_wire': ('Barbed Wire', 'a sharp wire placed in the way: hurts anything that moves through it for 2 damage and slows it'),
 'tesla_coil': ('Tesla Coil', 'a coil that stores 40,000 FE, shocks mobs within 3 blocks for 3 damage on a redstone signal (1,000 FE per shot), and charges items on belts at up to 5,000 FE/t'),
 'modular_accumulator': ('Modular Accumulator', 'a multiblock battery up to 3x3x5 holding 2,000,000 FE per block, 5,000 FE/t in and out'),
 'biomass_pellet_block': ('Biomass Pellet Block', 'a block of pressed biomass, a fuel (nine pellets, 57,600 ticks)'),
 'electrum_block': ('Block of Electrum', 'a storage block of electrum ingots'),
 'capacitor': ('Capacitor', 'a small item battery that stores 5,000 FE'),
 'diamond_grit': ('Diamond Grit', 'crushed diamond, the abrasive for fine sandpaper'),
 'diamond_grit_sandpaper': ('Diamond Grit Sandpaper', 'sandpaper that lasts 1,024 uses'),
 'biomass': ('Biomass', 'compressed plant matter, a fuel ingredient'),
 'biomass_pellet': ('Biomass Pellet', 'a pressed pellet of biomass that burns for 6,400 ticks'),
 'electrum_amulet': ('Electrum Amulet', 'an amulet that slowly charges energy items held in the other hand, about 2 FE/t'),
 'electrum_ingot': ('Electrum Ingot', 'an alloy of gold and silver, a good conductor'),
 'electrum_nugget': ('Electrum Nugget', 'a ninth of an electrum ingot'),
 'electrum_sheet': ('Electrum Sheet', 'a pressed sheet of electrum'),
 'zinc_sheet': ('Zinc Sheet', 'a pressed sheet of zinc'),
 'copper_wire': ('Copper Wire', 'a thin copper wire for crafting and connectors'),
 'iron_wire': ('Iron Wire', 'a thin iron wire'),
 'gold_wire': ('Gold Wire', 'a thin gold wire, charged into electrum wire at a Tesla Coil'),
 'electrum_wire': ('Electrum Wire', 'the best conducting wire, made by charging gold wire'),
 'spool': ('Wire Spool', 'an empty spool for stringing wires'),
 'copper_spool': ('Copper Wire Spool', 'a spool of copper wire for stringing connectors'),
 'gold_spool': ('Gold Wire Spool', 'a spool of gold wire for stringing connectors'),
 'electrum_spool': ('Electrum Wire Spool', 'a spool of electrum wire for stringing connectors'),
 'festive_spool': ('Festive Wire Spool', 'a spool of festive wire with coloured lights'),
 'copper_rod': ('Copper Rod', 'a rolled copper rod'),
 'iron_rod': ('Iron Rod', 'a rolled iron rod'),
 'gold_rod': ('Gold Rod', 'a rolled gold rod'),
 'electrum_rod': ('Electrum Rod', 'a rolled electrum rod'),
 'brass_rod': ('Brass Rod', 'a rolled brass rod'),
 'cake_base': ('Cake Base', 'raw cake dough, to be baked'),
 'cake_base_baked': ('Baked Cake Base', 'a baked sponge, filled with milk to make a cake'),
 'straw': ('Straw', 'a drinking straw'),
}
DECO_NOUNS = {
 'bars': 'iron-style window bars', 'bars_overlay': 'bars that overlay a wall', 'mesh_fence': 'a mesh fence', 'catwalk': 'a metal catwalk platform',
 'catwalk_stairs': 'stairs for a catwalk', 'railing': 'a catwalk railing', 'support': 'a support strut', 'support_wedge': 'a wedge-shaped support strut',
 'hull': 'a riveted hull plate block', 'sheet_metal': 'a sheet metal block', 'ladder': 'a metal ladder', 'door': 'a metal door',
 'locked_door': 'a locked metal door that opens only with redstone', 'trapdoor': 'a metal trapdoor', 'window': 'a framed glass window',
 'window_pane': 'a thin glass window pane', 'cage_lamp': 'a lamp in a protective cage', 'coin': 'a decorative coin', 'coinstack': 'a stack of decorative coins',
 'decal': 'a painted warning decal block', 'brick': 'a decorative brick block', 'brick_stairs': 'brick stairs', 'brick_slab': 'a brick slab', 'brick_wall': 'a brick wall',
}
DECO_MATS = {'andesite': 'andesite alloy', 'zinc': 'zinc', 'copper': 'copper', 'brass': 'brass', 'iron': 'iron', 'industrial_iron': 'dark industrial iron',
 'gold': 'gold', 'netherite': 'netherite', 'blue': 'blue', 'dean': 'yellow-gold', 'dusk': 'near-black', 'pearl': 'pearl white', 'scarlet': 'scarlet red',
 'verdant': 'green', 'umber': 'brown', 'red': 'brick red', 'yellow': 'yellow', 'green': 'green', 'cracked': 'cracked', 'mossy': 'mossy', 'tiled': 'tiled', 'long': 'long', 'short': 'short', 'corner': 'corner'}
rows = []
for k, (nm, d) in ADD.items():
    rows.append({'id': 'add_' + k, 'system': SYSTEM, 'prompt': f'Item: {nm} ({d}). From the add-on Create Crafts & Additions.'})
for k, d in DECO_NOUNS.items():
    rows.append({'id': 'decoN_' + k, 'system': SYSTEM + " For this row give only the bare noun, no material.", 'prompt': f'Noun: {k.replace("_", " ")} ({d}). From the add-on Create Deco.'})
for k, d in DECO_MATS.items():
    rows.append({'id': 'decoM_' + k, 'system': SYSTEM.split(' Then write')[0] + " For this row give a single adjective word and put a one-sentence note in entry. Output strict JSON: {\"classical\":\"...\",\"entry\":\"...\"}", 'prompt': f'Adjective for material or colour: {d}.'})
os.makedirs(OUT, exist_ok=True)
with open(OUT + '/in.jsonl', 'w') as f:
    for r in rows: f.write(json.dumps(r) + '\n')
open(OUT + '/README', 'w').write("max_tokens 400\nCheck: valid JSON with classical (one word, letters only) and entry (<=300 chars). Owner (create-addons batch1a thread) runs diaithiris-archaic/tools/archaic.py on classical, composes deco names as noun+material, then writes /mnt/project-files/library-sync/create_batch1a.json.\n")
print(len(rows), 'rows')
