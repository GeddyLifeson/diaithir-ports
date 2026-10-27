#!/usr/bin/env python3
"""Writes client item definitions + base models for ISTER-style items (special model alexscaves:legacy_item)."""
import json, pathlib
UP=pathlib.Path('/home/user/src/ac/src/main/resources/assets/alexscaves')
R=pathlib.Path('src/main/resources/assets/alexscaves')
# name -> (inventory model name or None, alt (throwing/charging) variant name or None, condition property)
ITEMS={
 'galena_gauntlet':(None,None),'resistor_shield':(None,None),'primitive_club':('primitive_club_inventory',None),
 'limestone_spear':('limestone_spear_inventory','limestone_spear_throwing'),
 'extinction_spear':('extinction_spear_inventory','extinction_spear_throwing'),
 'frostmint_spear':('frostmint_spear_inventory','frostmint_spear_throwing'),
 'siren_light':(None,None),'raygun':(None,None),'sea_staff':('sea_staff_inventory',None),
 'ortholance':('ortholance_inventory','ortholance_charging'),'copper_valve':(None,None),'beholder':(None,None),
 'dreadbow':('dreadbow_inventory',None),'gobthumper':(None,None),'shot_gum':(None,None),'sugar_staff':('sugar_staff_inventory',None),
}
def base_model(name):
    j=json.load(open(UP/'models/item'/(name+'.json')))
    out={k:v for k,v in j.items() if k in ('display','gui_light')}
    out.setdefault('gui_light','front')
    return out
def special(base):
    return {"type":"minecraft:special","base":"alexscaves:item/"+base,"model":{"type":"alexscaves:legacy_item"}}
for name,(inv,alt) in ITEMS.items():
    (R/'models/item'/(name+'.json')).write_text(json.dumps(base_model(name),indent=2))
    hand=special(name)
    if alt:
        (R/'models/item'/(alt+'.json')).write_text(json.dumps(base_model(alt),indent=2))
        prop='minecraft:using_item'
        hand={"type":"minecraft:condition","property":prop,"on_false":special(name),"on_true":special(alt)}
    model=hand
    if inv and (R/'models/item'/(inv+'.json')).exists():
        model={"type":"minecraft:select","property":"minecraft:display_context","cases":[{"when":["gui","ground","fixed"],"model":{"type":"minecraft:model","model":"alexscaves:item/"+inv}}],"fallback":hand}
    (R/'items'/(name+'.json')).write_text(json.dumps({"model":model},indent=2))
    print(name, 'inv' if inv else '-', 'alt' if alt else '-')
