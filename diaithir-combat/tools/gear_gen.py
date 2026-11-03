#!/usr/bin/env python3
"""S6830 Library weapon/armor -> compact gear table for diaithir-combat. Reads content.sqlite + codex library.sqlite (summary text, read only), keyword-classifies class/slot from the entry's own text, writes gear.tsv.gz (id, tier, gate Age, class, name). No text is invented or stored."""
import sqlite3, re, gzip, sys, collections
CONTENT = '/mnt/project-files/library-content/content.sqlite'
STORE = '/mnt/project-files/codex-store/library.sqlite'
W = [  # first match wins; class ids are the diaithircombat category ids
 ('crossbow|arbalest|rifle|pistol|firearm|musket|shotgun|blaster|launcher|cannon|gun\\b|sidearm|railgun|carbine|repeater', 'longbow'),
 ('longbow|warbow|bow\\b|bows\\b|arrow|archer|quiver|sling', 'bow'),
 ('dagger|knife|dirk|stiletto|kunai|shiv|poniard|dagger-like|blade-?let|claw', 'dagger'),
 ('rapier|foil|epee|estoc|fencing', 'rapier'),
 ('greatsword|claymore|zweih|two-handed sword|nodachi|colossal blade|buster', 'greatsword'),
 ('battle-?axe|great-?axe|halberd-axe|poleaxe|scythe|double-bladed axe', 'battleaxe'),
 ('\\baxe|hatchet|tomahawk|cleaver', 'axe'),
 ('warhammer|war hammer|maul|sledge|great hammer|hammer\\b|mallet', 'warhammer'),
 ('mace|club|cudgel|flail|morning star|bludgeon|truncheon|baton|rod\\b|cane', 'mace'),
 ('halberd|glaive|polearm|naginata|pike|trident|scythe|bardiche|guandao|lance', 'polearm'),
 ('spear|javelin|harpoon|lance|partisan', 'spear'),
 ('staff|wand|scepter|sceptre|orb|tome|grimoire|catalyst|focus|crook', 'staff'),
 ('sword|blade|sabre|saber|katana|scimitar|cutlass|falchion|saber|edge\\b|longsword|shortsword|saber', 'sword'),
]
A = [  # armor: slot
 ('shield|buckler|aegis|pavise|targe|ward-plate|guard-plate', 'shield'),
 ('boot|shoe|sandal|footwear|greave|sabaton|feet|foot\\b|slipper|sock', 'feet'),
 ('leg |legs\\b|legging|trouser|pants|leg coverings|skirt|kilt|cuisse|tasset|breeches|hose', 'legs'),
 ('helm|helmet|hood\\b|hat\\b|cap\\b|crown|headpiece|circlet|mask|visor|coif|headband|headgear|tiara|diadem|bonnet', 'head'),
 ('ring\\b|amulet|necklace|pendant|talisman|charm|bracelet|bangle|brooch|belt\\b|gloves?\\b|gauntlet|cloak|cape\\b|mantle|scarf|trinket|accessory|earring|badge|medallion', 'trinket'),
 ('armor|armour|plate|mail\\b|cuirass|chest|torso|garment|robe|tunic|coat|vest|jerkin|suit|breastplate|body|outfit|costume|gown|dress|shirt|hauberk|harness|jacket|cloth|worn', 'chest'),
]
cw=[(re.compile(p),c) for p,c in W]; ca=[(re.compile(p),c) for p,c in A]
def cls(text, rules, default):
    t=text.lower()
    for r,c in rules:
        if r.search(t): return c
    return default
import csv
PLANETS='/mnt/project-files/planets/planets.tsv'
def opens():
    return {r['planet_id']:int(r['opens_age']) for r in csv.DictReader(open(PLANETS),delimiter='\t')}
def main(out):
    op=opens()
    d=sqlite3.connect(f'file:{CONTENT}?mode=ro',uri=True); d.execute(f"attach 'file:{STORE}?mode=ro' as l")
    n=collections.Counter(); rows=[]
    for id_,form,tier,name,s,uni in d.execute("select c.id,c.form,c.tier,c.name,e.summary,lower(c.universe) from content c join l.entry e on e.id=c.id where c.form in ('WEAPON','ARMOR') order by c.id"):
        k = cls(s or '', cw if form=='WEAPON' else ca, 'sword' if form=='WEAPON' else 'chest')
        t=max(1,min(8,tier or 1))
        gate=min(9,op.get(uni,1)+t-1)  # planet opens_age + tier - 1 (PLANETS.md)
        rows.append((id_,t,gate,k,name.replace('\t',' ')))
        n[(form,k)]+=1
    with gzip.open(out,'wt',encoding='utf-8',compresslevel=9) as f:
        for r in rows: f.write('\t'.join(map(str,r))+'\n')
    for k,v in sorted(n.items()): print(k,v)
    print(len(rows))
main(sys.argv[1])
