#!/usr/bin/env python3
"""Generates cave chamber configured/placed features and the biome modifier that adds them to the overworld."""
import json,os
D='src/main/resources/data/alexscaves/'
def bs(n,props=None):
    d={'Name':n}
    if props: d['Properties']=props
    return d
B={
 'primordial_caves':dict(floor=bs('minecraft:moss_block'),wall=bs('alexscaves:limestone'),ceiling=bs('alexscaves:limestone'),
   decor=['cycad','flytrap','primordial_caves_grass','peering_coprolith','ancient_tree','archaic_vine'],rarity=30),
 'forlorn_hollows':dict(floor=bs('minecraft:coarse_dirt'),wall=bs('minecraft:deepslate'),ceiling=bs('minecraft:deepslate'),
   decor=['thornwood_roots','forlorn_posts','forlorn_hollows_brown_mushroom','guano_pile'],rarity=36),
 'magnetic_caves':dict(floor=bs('alexscaves:galena'),wall=bs('alexscaves:galena'),ceiling=bs('alexscaves:galena'),
   decor=['scarlet_magnetic_node','azure_magnetic_node','tesla_bulb','metal_swarf'],rarity=36),
 'toxic_caves':dict(floor=bs('alexscaves:radrock'),wall=bs('alexscaves:radrock'),ceiling=bs('alexscaves:radrock'),
   fluid=bs('alexscaves:acid',{'level':'0'}),decor=['acid_vent','nuclear_siren','sulfur_stack','radrock_uranium_ore'],rarity=40),
 'candy_cavity':dict(floor=bs('alexscaves:block_of_vanilla_frosting'),wall=bs('alexscaves:block_of_chocolate'),ceiling=bs('alexscaves:block_of_chocolate'),
   decor=['candy_cane','lollipop','sweet_puff','peppermint_patch','vanilla_ice_cream_scoop','sprinkles_pile'],rarity=40),
 'abyssal_chasm':dict(floor=bs('alexscaves:abyssmarine'),wall=bs('alexscaves:abyssmarine'),ceiling=bs('alexscaves:abyssmarine'),
   fluid=bs('minecraft:water',{'level':'0'}),fluid_depth=40,decor=['tube_worm','mussel','black_vent','abyssal_boulder'],rarity=40),
}
for k in ('configured_feature','placed_feature'): os.makedirs(D+f'worldgen/{k}',exist_ok=True)
names=[]
for b,c in B.items():
    cfg={'biome':'alexscaves:'+b,'floor':c['floor'],'wall':c['wall'],'ceiling':c['ceiling'],'min_y':-48,'max_y':24,
         'decor':['alexscaves:'+x for x in c['decor']],'radius':13,'height':9}
    if 'fluid' in c: cfg['fluid']=c['fluid']; cfg['fluid_depth']=c.get('fluid_depth',3)
    if b=='abyssal_chasm': cfg.update(radius=12,height=14)
    json.dump({'type':'alexscaves:cave_chamber','config':cfg},open(D+f'worldgen/configured_feature/chamber_{b}.json','w'),indent=2)
    json.dump({'feature':f'alexscaves:chamber_{b}','placement':[{'type':'minecraft:rarity_filter','chance':c['rarity']},{'type':'minecraft:in_square'}]},
              open(D+f'worldgen/placed_feature/chamber_{b}.json','w'),indent=2)
    names.append(f'alexscaves:chamber_{b}')
os.makedirs(D+'neoforge/biome_modifier',exist_ok=True)
json.dump({'type':'neoforge:add_features','biomes':'#minecraft:is_overworld','features':names,'step':'underground_structures'},
          open(D+'neoforge/biome_modifier/add_cave_chambers.json','w'),indent=2)
