#!/usr/bin/env python3
"""Apply script-generated Diaithiris names (tools/diaithiris_names/build_names.py output) to assets/alexscaves/lang/en_us.json.
usage: apply_names.py names.json   (ids never change; only display strings)"""
import json,sys,re
names=json.load(open(sys.argv[1]))
p='src/main/resources/assets/alexscaves/lang/en_us.json'
lang=json.load(open(p)); n=0
for k in list(lang):
    m=re.fullmatch(r'(item|block|entity|biome)\.alexscaves\.([a-z_0-9]+)',k)
    if not m: continue
    key='alexscaves:'+('' if m[1] in('item','block') else m[1]+'/')+m[2]
    if key in names: lang[k]=names[key]; n+=1
json.dump(lang,open(p,'w'),ensure_ascii=False,indent=2)
json.dump(names,open('diaithiris_names.json','w'),ensure_ascii=False,indent=1)
print(n,'names applied')
