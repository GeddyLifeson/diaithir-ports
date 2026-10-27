#!/usr/bin/env python3
"""Build the Diaithiris display-name lang file for the Deeper Darker port.

Usage: diaithiris_lang.py <archaic-tools-dir> <upstream en_us.json> <out en_us.json>
  archaic-tools-dir: the Diaithiris archaic layer (archaic.py, archaic_map.json, overrides.json, oldirish_known.txt)
Ids never change; only display names move.  Names are composed the Gaelic way: head noun first, then
its modifiers (genitive nouns and adjectives) fused into one word, then run through the archaic laws.
Descriptions stay English (with Celtic otherworld framing, see DESCRIPTIONS below).
"""
import json, os, re, sys

# token -> modern Goidelic stem (head form) or (head form, modifier form).
T = {
 'stone': ('clach', 'cloiche'), 'slate': ('sglèat', 'sglèata'), 'gloomy': 'doilleir', 'gloomslate': 'sglèatdoilleir',
 'gloomsherd': 'criomagdoilleir', 'echo': 'mactalla', 'echoing': 'mactallach', 'echoer': 'mactallair',
 'sculk': ('sìthfhàs', 'sìthfhàis'), 'otherside': ('sìthtaobh', 'sìthtaoibh'),
 'warden': ('fìrfaire', 'fìrfaire'), 'armor': 'armachd', 'boots': 'bòtainn', 'leggings': 'osan', 'chestplate': 'uchdlann',
 'helmet': 'clogaid', 'sword': ('claidheamh', 'claidhimh'), 'axe': ('tuagh', 'tuaighe'), 'pickaxe': ('piocaid', 'piocaid'),
 'shovel': ('spaid', 'spaid'), 'hoe': ('grafan', 'grafain'), 'staff': ('bata', 'bata'), 'elytra': ('sgiathan', 'sgiathan'),
 'soul': ('anam', 'anama'), 'compass': ('combaist', 'combaiste'), 'ancient': 'àrsaidh', 'wayfinder': ('lorgsligh', 'lorgslighe'),
 'heart': ('cridhe', 'cridhe'), 'shard': ('criomag', 'criomaig'), 'template': ('teamplaid', 'teamplaide'),
 'smithing': 'gobhainneachd', 'upgrade': ('àrdachadh', 'àrdachaidh'), 'resonarium': ('athfhuaim', 'athfhuaim'),
 'sonorous': 'fuaimneach', 'transmitter': ('sgaoilear', 'sgaoilear'), 'amber': ('ombar', 'ombair'),
 'crystallized': 'criostalaichte', 'crystal': ('criostal', 'criostail'), 'leech': ('sùgair', 'sùgair'),
 'centipede': ('ceudchasach', 'ceudchasaich'), 'millipede': ('mìlechasach', 'mìlechasaich'), 'shattered': 'briste',
 'stalker': ('leantair', 'leantair'), 'snapper': ('bìdeadair', 'bìdeadair'), 'shriek': ('sgreuch', 'sgreuchaidh'),
 'worm': ('cnuimh', 'cnuimhe'), 'sludge': ('eabar', 'eabair'), 'angler': ('slat', 'slaite'), 'fish': ('iasg', 'èisg'),
 'anger': ('fearg', 'feirge'), 'fear': ('eagal', 'eagail'), 'sorrow': ('bròn', 'bròin'), 'pot': ('poit', 'poite'),
 'potted': 'poiteach', 'vase': ('ballan', 'ballain'), 'geyser': ('tobarteth', 'tobartheth'), 'portal': ('bealach', 'bealaich'),
 'tendrils': ('dualan', 'dualan'), 'vines': ('fìonan', 'fìonan'), 'roots': ('freumhan', 'freumhan'), 'moss': ('còinneach', 'còinnich'),
 'soil': ('ùir', 'ùire'), 'flower': ('flùr', 'flùir'), 'flowers': ('flùraichean', 'flùraichean'), 'flowerless': 'gun-flùr',
 'lily': ('lilidh', 'lilidh'), 'grass': ('feur', 'feòir'), 'cactus': ('cactas', 'cactais'), 'leaves': ('duilleagan', 'duilleagan'),
 'sapling': ('ògchraobh', 'ògchraoibh'), 'log': ('stoc', 'stoic'), 'wood': ('fiodh', 'fiodha'), 'planks': ('clàran', 'clàran'),
 'stem': ('gas', 'gais'), 'stripped': 'rùisgte', 'hanging': 'crochte', 'sign': ('soidhne', 'soidhne'), 'door': ('doras', 'dorais'),
 'trapdoor': ('ceiltdhoras', 'ceiltdhorais'), 'fence': ('feansa', 'feansa'), 'gate': ('geata', 'geata'), 'button': ('putan', 'putain'),
 'plate': ('clàr', 'clàir'), 'pressure': ('cuideam', 'cuideim'), 'slab': ('leac', 'leac'), 'stairs': ('staidhre', 'staidhre'),
 'wall': ('balla', 'balla'), 'block': ('bloc', 'bloic'), 'tile': ('leacag', 'leacaig'), 'tiles': ('leacagan', 'leacagan'),
 'brick': ('breige', 'breige'), 'bricks': ('breigean', 'breigean'), 'chiseled': 'gearrte', 'cut': 'sgathte', 'polished': 'lìomhte',
 'smooth': 'rèidh', 'cobbled': 'clachaichte', 'reinforced': 'neartaichte', 'ore': ('mèinn', 'mèinne'), 'raw': 'amh', 'cooked': 'bruich',
 'coal': ('gual', 'guail'), 'iron': ('iarann', 'iarainn'), 'copper': ('copar', 'copair'), 'gold': ('òr', 'òir'),
 'redstone': ('cloichdhearg', 'cloichdheirg'), 'lapis': ('cloichghorm', 'cloichghoirm'), 'lazuli': '', 'diamond': ('daoimean', 'daoimein'),
 'emerald': ('smàrag', 'smàraig'), 'netherite': ('ìochdaran', 'ìochdarain'),
 'white': 'geal', 'orange': 'orains', 'magenta': 'magenta', 'yellow': 'buidhe', 'lime': 'limeach', 'pink': 'pinc', 'gray': 'liath',
 'cyan': 'glasghorm', 'purple': 'purpaidh', 'blue': 'gorm', 'brown': 'donn', 'green': 'uaine', 'red': 'dearg', 'black': 'dubh',
 'dark': 'dorcha', 'deep': 'domhainn', 'deeplands': ('tìrdomhainn', 'tìrdomhainn'), 'blooming': 'blàthach', 'bloom': ('blàth', 'blàth'),
 'caverns': ('uamhan', 'uamhan'), 'forest': ('coille', 'coille'), 'columns': ('colbhan', 'colbhan'), 'overcast': 'neulach',
 'egg': ('ugh', 'uighe'), 'spawn': 'ginnte', 'splash': 'steallach', 'potion': ('deoch', 'dheoch'), 'lingering': 'mairsinneach',
 'arrow': ('saighead', 'saighde'), 'glowing': 'boillsgeach', 'infested': 'plàigheach', 'jaw': ('giall', 'gialla'), 'grime': ('salachar', 'salachair'),
 'gleam': ('lainnir', 'lainnire'), 'glass': ('glainne', 'glainne'), 'soundproof': 'fuaimdhìonte', 'ice': ('deigh', 'deighe'),
 'light': 'aotrom', 'carapace': ('slige', 'sligeadh'), 'ball': ('ball', 'buill'), 'dust': ('duslach', 'duslaich'), 'gel': ('glaodh', 'glaodha'),
 'plant': ('lus', 'luis'), 'boat': ('bàta', 'bàta'), 'chest': ('ciste', 'ciste'), 'bone': ('cnàimh', 'cnàimh'), 'wood.': '', 'porous': 'toll-fhosgailte',
 'listener': ('èistear', 'èistear'), 'linked': 'ceangailte', 'unlinked': 'gun-cheangal', 'bordered': 'iomaireach', 'equipment': ('uidheam', 'uidheim'),
 'ward': ('dìon', 'dìona'), 'omen': ('manadh', 'manaidh'), 'affinity': ('dàimh', 'dàimh'), 'catalysis': ('brosnachadh', 'brosnachaidh'),
 'volume': ('àirde', 'àirde'), 'reverberation': ('ath-fhuaim', 'ath-fhuaime'), 'smite': ('buille', 'buille'), 'vibrations': ('crith', 'crithe'),
 'dampens': 'bàthadh', 'temple': ('teampall', 'teampaill'), 'kelp': '', 'transmission': ('sgaoileadh', 'sgaoilidh'),
 'tipped': '', 'wayfinder.': '', 'back': '', 'abstraction': ('eas-tharraingeachd', 'eas-tharraingeachd'),
 'adventure': ('dànachd', 'dànachd'), 'clouds': ('neulan', 'neulan'), 'ooze': ('snigheadh', 'snighidh'), 'tool': ('inneal', 'inneil'),
 'weapon': ('armachd', 'armachd'), 'sonic': 'fuaimneach', 'weapons': ('armachd', 'armachd'), 'scutes': ('lannan', 'lannan'), 'stems': ('gasan', 'gasan'), 'logs': ('stocan', 'stocan'),
 'sherds': ('criomagan', 'criomagan'), 'the': '', 'of': '', 'and': '', 'a': '', 'in': '', 'is': '', 'to': '', 'with': '', 'your': '',
 'add': '', 'boost': ('cuideachadh', 'cuideachaidh'), 'link': ('ceangal', 'ceangail'), 'transmit': ('sgaoileadh', 'sgaoilidh'),
 'berries': ('caoran', 'caorann'), 'brittle': 'brisg', 'enriched': 'saidhbhir', 'lite': ('lasair', 'lasrach'),
 'darker': 'dorcha', 'deeper': 'domhainn', 'armor,': '', 'weapon,': '',
}
PHRASES = {
 ('angler', 'fish'): ('iasgslaite', 'èisgslaite'), ('spawn', 'egg'): ('ughginnte', 'ughginnte'),
 ('pressure', 'plate'): ('clàrcuideim', 'clàrcuideim'), ('light', 'blue'): ('gormaotrom', 'gormaotrom'),
 ('light', 'gray'): ('liathaotrom', 'liathaotrom'), ('lapis', 'lazuli'): ('cloichghorm', 'cloichghoirm'),
 ('shriek', 'worm'): ('cnuimhsgreuch', 'cnuimhsgreuch'), ('fence', 'gate'): ('geata', 'geata'),
 ('smithing', 'template'): ('teamplaidgobhainn', 'teamplaidgobhainn'), ('trap', 'door'): ('ceiltdhoras', 'ceiltdhorais'),
 ('sculk', 'transmitter'): ('sgaoilearsìthfhàis', 'sgaoilearsìthfhàis'),
}
# whole-name overrides (modern Goidelic spelling; archaic laws applied afterwards)
FULL = {
 'Deeper and Darker': 'An Sìthtaobh',
 'Back to Your Roots': 'Air ais dhan fhreumh',
 'Below the Bedrock': 'Fon chlach-bhunait', 'Echolocation': 'Lorg mactalla', 'A Metropolis of Restless Souls': 'Baile mòr nan anam gun fhois',
 'Abyssal Descent': 'Sìoladh dhan dubh-aigeann', 'Sculk Slayer': 'Marbhaiche sìthfhàis', 'Phantom Thief': 'Meirleach nan taibhs',
 'Sculk Engineer': 'Fiodhaiche sìthfhàis', 'Remote Storage': 'Stòr fad às', 'Noise Complaint': 'Gearan fuaime',
 'Sculk Story': 'Sgeul an t-sìthfhàis', 'Cover Me with Sculk': 'Còmhdaich mi le sìthfhàs',
}

# English descriptions, framed as the Otherworld (the Sidhe-like otherworld under the hills) rather than a mining dimension.
DESCRIPTIONS = {
 'advancements.deeperdarker.root.description': 'A door stands open in the dark beneath the hills, and the Otherworld waits behind it.',
 'advancements.deeperdarker.enter_otherside.description': 'Cross the threshold below the bedrock and walk in the Otherworld of the Sidhe',
 'advancements.deeperdarker.explore_otherside.description': 'Walk every land of the Otherworld',
 'advancements.deeperdarker.find_ancient_city.description': 'Find a city kept by the restless dead',
 'advancements.deeperdarker.find_ancient_temple.description': 'Follow the deep roads to a temple of the old ones',
 'advancements.deeperdarker.kill_all_sculk_mobs.description': 'Slay one of every creature of the sidhe-growth',
 'advancements.deeperdarker.kill_warden.description': 'Slay the Warden and take its heart',
 'advancements.deeperdarker.obtain_reinforced_echo_shard.description': 'Strengthen an echo shard',
 'advancements.deeperdarker.obtain_sculk_transmitter.description': 'Acquire a sidhe-growth transmitter',
 'advancements.deeperdarker.obtain_sonorous_staff.description': 'Acquire a sonorous staff',
 'advancements.deeperdarker.warden_armor.description': 'Wear a full set of the Warden\'s armor',
 'enchantment.deeperdarker.catalysis.desc': 'Where a foe falls, the growth of the Otherworld spreads.',
 'enchantment.deeperdarker.reverberation.desc': 'The echo of a sonic blast carries farther.',
 'enchantment.deeperdarker.sculk_smite.desc': 'More damage against creatures of the Otherworld, the Warden among them.',
 'enchantment.deeperdarker.volume.desc': 'Sonic blasts strike harder.',
 'tooltips.deeperdarker.crystallized_amber.item': 'Holds %s',
 'tooltips.deeperdarker.crystallized_amber.leech': 'Something from the Otherworld stirs inside',
 'message.deeperdarker.age_locked': 'The Otherworld does not know you yet. Return in a later Age.',
}

def stem(tok, mod):
    v = T.get(tok.lower())
    if v is None:
        raise KeyError(tok)
    if isinstance(v, tuple):
        return v[1] if mod else v[0]
    return v

def compose(name):
    if name in FULL:
        return FULL[name]
    if ' of ' in name:
        a, b = name.split(' of ', 1)
        return compose(a) + ' ' + compose_mod(b)
    toks = re.findall(r"[A-Za-z]+|%s|[^A-Za-z\s]", name)
    out = []; i = 0
    items = []
    while i < len(toks):
        low = tuple(t.lower() for t in toks[i:i + 2])
        if low in PHRASES:
            items.append(PHRASES[low]); i += 2; continue
        items.append(toks[i]); i += 1
    if len(items) == 1 and isinstance(items[0], str):
        return stem(items[0], False).capitalize()
    head = items[-1]
    parts = [head[0] if isinstance(head, tuple) else stem(head, False)]
    for it in reversed(items[:-1]):
        parts.append(it[1] if isinstance(it, tuple) else stem(it, True))
    w = ''.join(p.replace('-', '') for p in parts if p)
    return w[:1].upper() + w[1:]

def compose_mod(name):
    """Genitive/modifier form of a whole phrase (last word in modifier form)."""
    toks = re.findall(r"[A-Za-z]+", name)
    parts = [stem(t, True) for t in toks[:-1][::-1]]
    parts = [stem(toks[-1], True)] + parts
    w = ''.join(p.replace('-', '') for p in parts if p)
    return w[:1].upper() + w[1:]

def main():
    here, src, dst = sys.argv[1:4]
    sys.path.insert(0, here)
    import archaic as A
    for fn in ('archaic_map.json',):
        p = os.path.join(here, '..', fn)
        if os.path.exists(p): A.MEMO.update(json.load(open(p, encoding='utf8')))
    po = os.path.join(here, 'overrides.json')
    if os.path.exists(po): A.OVERRIDES.update(json.load(open(po, encoding='utf8')))
    pk = os.path.join(here, 'oldirish_known.txt')
    if os.path.exists(pk): A.REAL.update(open(pk, encoding='utf8').read().split())
    d = json.load(open(src, encoding='utf8'))
    out = dict(d)
    miss = set()
    for k, v in d.items():
        if k in DESCRIPTIONS:
            out[k] = DESCRIPTIONS[k]
        elif k.startswith(('item.minecraft.',)) and '.effect.' in k:
            try: out[k] = A.text(compose(v))
            except KeyError as e: miss.add(str(e))
        elif k.startswith(('block.', 'item.deeperdarker', 'entity.', 'effect.', 'biome.', 'itemGroup', 'key.categories', 'upgrade.', 'enchantment.', 'tag.item.')) and '.desc' not in k:
            if '%s' in v or any(x in k for x in ('not_found', 'not_transmittable', 'no_cooldown', 'description', '.desc', 'tooltip')) or '.effect.' in k: continue
            try: out[k] = A.text(compose(v))
            except KeyError as e: miss.add(str(e))
        elif k.startswith('painting.') and k.endswith('.title'):
            try: out[k] = A.text(compose(v))
            except KeyError as e: miss.add(str(e))
        elif k.startswith('advancements.') and k.endswith('.title'):
            try: out[k] = A.text(compose(v))
            except KeyError as e: miss.add(str(e))
    if miss: print('MISSING', sorted(miss))
    json.dump(out, open(dst, 'w', encoding='utf8'), indent=2, ensure_ascii=False, sort_keys=True)
    open(dst, 'a').write('\n')

if __name__ == '__main__':
    main()
