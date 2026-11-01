#!/usr/bin/env python3
"""Diaithiris display names for every animal species and fossil taxon (ids never change).

Usage: python3 names_gen.py [--lex LEXICON.json] [--arch ARCHAIC_TOOLS_DIR] [--cat CATALOGUE_DIR]
Output: names/diaithiris_names.json ({id: name}) and lang/diaithiris.json (species.* and entity.* keys).

Method (GRAMMAR_MORPHOLOGY s10): every name is a head-first loose kenning written as ONE word:
  KIND (animal head, same root for related species) + describers (genitive noun or adjective: colour,
  size, body trait, habitat, region, diet, period) + a nature-noun epithet only when still not unique.
Every root is a word of LEXICON.json (checked below). The classical compound is run through
archaic.py (the Diaithiris sound laws), so no part equals a modern or Old Gaelic/Irish/Manx word.
Collisions are resolved by lengthening the name with the next describer tier, never by numbering.
"""
import argparse, collections, glob, hashlib, json, os, re, sys, unicodedata

HERE = os.path.dirname(os.path.abspath(__file__))
ap = argparse.ArgumentParser()
ap.add_argument('--lex', default='/mnt/project-files/LEXICON.json')
ap.add_argument('--arch', default='/mnt/project-files/diaithiris-archaic/tools')
ap.add_argument('--cat', default='/mnt/project-files/animals-fossils/catalogue')
ap.add_argument('--out', default='/mnt/project-files/animals-fossils/names/diaithiris_names.json')
ap.add_argument('--lang', default=os.path.join(HERE, '..', 'src/main/resources/assets/diaithirfauna/lang/diaithiris.json'))
args = ap.parse_args()

sys.path.insert(0, args.arch)
import archaic as A  # noqa: E402

LEX = json.load(open(args.lex, encoding='utf8'))['entries']
LROD = {}
for e in LEX:
    LROD.setdefault(e['rod'].lower(), e)
LTOK = set()
for e in LEX:
    for k in ('rod', 'gen', 'pl', 'root', 'vn', 'comp', 'was'):
        if k in e:
            LTOK.update(t.lower() for t in re.findall(r"[^\W\d_]+", e[k]))
OI = set(open(os.path.join(args.arch, 'oldirish_known.txt'), encoding='utf8').read().split())
OI |= {unicodedata.normalize('NFD', w).encode('ascii', 'ignore').decode() for w in OI}
REAL = LTOK | OI
A.REAL = REAL
A.OVERRIDES.update(json.load(open(os.path.join(args.arch, 'overrides.json'), encoding='utf8')))

# ---------------------------------------------------------------- genitives (where the lexicon has none)
GEN = dict(
    feur='feòir', coille='coille', muir='mara', cuan='cuain', fàsach='fàsaich', beinn='beinne', abhainn='aibhne',
    loch='locha', oirthir='oirthire', uamh='uamha', eilean='eilein', creag='creige', sneachd='sneachda',
    deigh='deighe', talamh='talmhainn', craobh='craoibhe', uisce='uisce', oidhche='oidhche', sùil='sùla',
    cluas='cluaise', sròn='sròine', cas='coise', amhach='amhaich', druim='droma', ìne='ìne', grian='grèine',
    gealach='gealaich', gaoth='gaoithe', stoirm='stoirme', teine='teine', ceò='ceò', tràigh='tràghad',
    cladach='cladaich', bolg='builg', fiacail='fiacla', làmh='làimhe', boglach='boglaich', tonn='tuinne',
    gleann='glinne', cnoc='cnuic', srath='sratha', cuilc='cuilce', fraoch='fraoich', ùir='ùire', cath='catha',
    cogadh='cogaidh', sìth='sìthe', bròn='bròin', guth='gutha', ceòl='cèoil', bàrd='bàird', luaithre='luaithre',
    solas='solais', dorchadas='dorchadais', cuimhne='cuimhne', ainm='ainme', lìon='lìn', cèir='cèire',
    snàth='snàith', àth='àtha', geamhradh='geamhraidh', samhradh='samhraidh', earrach='earraich',
    foghar='fogharaidh', sealgair='sealgair', creutair='creutair', clach='cloiche', tobar='tobair',
    dealbh='deilbh', ceum='cèim', àgh='àigh', rionnag='rionnaige', adharc='adhairc', earball='earbaill',
    ceann='cinn', gob='guib', iteag='iteig', sciath='sgèithe', tosc='tuisc', muing='muinge', cìrean='cìrein',
    dealg='deilge', cnàimh='cnàimh', craiceann='craicinn', bian='bèin', nimh='nimhe', latha='latha',
    machair='machrach', mòinteach='mòintich', bàrr='bàrr', slige='slige', seilcheag='seilcheige',
    iasc='èisg', crogall='crogaill', dreagan='dreagain', famhair='famhair', tìr='tìre', raon='raoin',
    cuilean='cuilein', beathach='beathaich', sliabh='slèibhe', bò='bà', luibh='luibhe', gàire='gàire',
    saoi='saoi', draoidh='draoidh', dùthaich='dùthcha', mullach='mullaich', cruinne='cruinne',
)
GEN_FIX = {'cas': 'coise'}


def gen_of(n):
    if n in GEN:
        return GEN[n]
    e = LROD.get(n)
    if e and e.get('gen'):
        return e['gen']
    return n


def gender(n):
    e = LROD.get(n)
    return (e or {}).get('g', 'm')


def lenite(w):
    if not w or w[0] not in 'bcdfgmpt' or w[1:2] == 'h':
        return w
    return w[0] + 'h' + w[1:]


# ---------------------------------------------------------------- kinds: (head, ('gen'|'adj', qualifier) | None)
def K(h, q=None):
    return (h, q)


KW = {
    # fish and sea life
    'salmon': K('bradan'), 'trout': K('breac'), 'char': K('breac'), 'grayling': K('breac'),
    'shark': K('cearban'), 'ray': K('gath'), 'stingray': K('gath'), 'manta': K('gath'), 'skate': K('gath'),
    'eel': K('eascann'), 'moray': K('eascann'), 'lamprey': K('eascann'), 'crab': K('partan'), 'lobster': K('partan', ('gen', 'cuan')),
    'crayfish': K('partan', ('gen', 'abhainn')), 'shrimp': K('partan', ('adj', 'beag')), 'prawn': K('partan', ('adj', 'beag')),
    'squid': K('gibearnach'), 'cuttlefish': K('gibearnach'), 'octopus': K('ochdchasach'), 'nautilus': K('seilcheag', ('gen', 'cuan')),
    'jellyfish': K('muirtèachd'), 'whale': K('mucmhara'), 'dolphin': K('leumadair'), 'porpoise': K('leumadair'),
    'beluga': K('mucmhara', ('adj', 'geal')), 'narwhal': K('mucmhara', ('gen', 'tosc')), 'orca': K('mucmhara', ('adj', 'borb')),
    'seal': K('ròn'), 'walrus': K('ròn', ('gen', 'tosc')), 'sealion': K('ròn', ('gen', 'muing')),
    'otter': K('dòbhran'), 'snail': K('seilcheag'), 'clam': K('slige'), 'oyster': K('slige'), 'mussel': K('slige'),
    'abalone': K('slige'), 'ormer': K('slige'), 'scallop': K('slige'), 'conch': K('slige'), 'shell': K('slige'),
    'urchin': K('rionnag', ('gen', 'dealg')), 'starfish': K('rionnag'), 'worm': K('cnuimh'),
    'seahorse': K('each', ('gen', 'cuan')), 'pipefish': K('iasc', ('adj', 'caol')), 'sunfish': K('iasc', ('gen', 'grian')),
    'catfish': K('iasc', ('gen', 'cat')), 'carp': K('iasc'), 'tuna': K('iasc', ('adj', 'luath')), 'marlin': K('iasc', ('gen', 'gob')),
    'swordfish': K('iasc', ('gen', 'gob')), 'pike': K('iasc', ('adj', 'borb')), 'perch': K('iasc'), 'cod': K('iasc'),
    'herring': K('iasc'), 'sturgeon': K('iasc', ('gen', 'craiceann')), 'paddlefish': K('iasc', ('gen', 'gob')),
    # amphibians reptiles
    'frog': K('loscann'), 'bullfrog': K('loscann', ('adj', 'mòr')), 'toad': K('muilemhàgag'), 'newt': K('dearcluachrach'),
    'salamander': K('dearcluachrach'), 'hellbender': K('dearcluachrach', ('adj', 'anamhòr')), 'caecilian': K('cnuimh', ('gen', 'ùir')),
    'snake': K('nathair'), 'python': K('nathair', ('adj', 'anamhòr')), 'boa': K('nathair', ('adj', 'anamhòr')),
    'anaconda': K('nathair', ('gen', 'abhainn')), 'viper': K('nathair', ('gen', 'nimh')), 'adder': K('nathair', ('gen', 'nimh')),
    'cobra': K('nathair', ('gen', 'nimh')), 'mamba': K('nathair', ('gen', 'nimh')), 'rattlesnake': K('nathair', ('gen', 'nimh')),
    'krait': K('nathair', ('gen', 'nimh')), 'lizard': K('dearcluachrach'), 'gecko': K('dearcluachrach', ('gen', 'oidhche')),
    'iguana': K('dearcluachrach', ('adj', 'mòr')), 'skink': K('dearcluachrach', ('adj', 'caol')), 'monitor': K('dearcluachrach', ('adj', 'anamhòr')),
    'chameleon': K('dearcluachrach', ('gen', 'craobh')), 'anole': K('dearcluachrach', ('gen', 'craobh')),
    'turtle': K('sligeanach'), 'tortoise': K('sligeanach', ('gen', 'talamh')), 'terrapin': K('sligeanach', ('gen', 'uisce')),
    'crocodile': K('crogall'), 'alligator': K('crogall'), 'caiman': K('crogall'), 'gharial': K('crogall', ('adj', 'caol')),
    # birds
    'eagle': K('iolair'), 'hawk': K('seabhag'), 'falcon': K('seabhag'), 'kite': K('seabhag'), 'buzzard': K('seabhag'),
    'harrier': K('seabhag'), 'osprey': K('seabhag', ('gen', 'iasc')), 'vulture': K('iolair', ('gen', 'cnàimh')), 'condor': K('iolair', ('adj', 'anamhòr')),
    'owl': K('comhachag'), 'crow': K('feannag'), 'raven': K('fitheach'), 'rook': K('feannag'), 'jackdaw': K('feannag'), 'magpie': K('feannag'),
    'jay': K('feannag'), 'duck': K('lach'), 'mallard': K('lach'), 'teal': K('lach'), 'pintail': K('lach'), 'shoveler': K('lach'),
    'goose': K('gèadh'), 'swan': K('eala'), 'heron': K('corraghritheach'), 'egret': K('corraghritheach'), 'crane': K('cranntogail'),
    'stork': K('corraghritheach', ('adj', 'mòr')), 'ibis': K('corraghritheach', ('gen', 'gob')), 'spoonbill': K('corraghritheach', ('gen', 'gob')),
    'flamingo': K('corraghritheach', ('adj', 'dearg')), 'gull': K('faoileag'), 'tern': K('faoileag', ('adj', 'beag')),
    'albatross': K('faoileag', ('adj', 'anamhòr')), 'petrel': K('faoileag', ('gen', 'stoirm')), 'shearwater': K('faoileag', ('gen', 'tonn')),
    'penguin': K('ceannfionn'), 'parrot': K('euncainnte'), 'macaw': K('euncainnte'), 'parakeet': K('euncainnte', ('adj', 'beag')),
    'cockatoo': K('euncainnte', ('gen', 'cìrean')), 'pigeon': K('calman'), 'dove': K('calman'), 'dodo': K('calman', ('adj', 'trom')),
    'solitaire': K('calman', ('adj', 'trom')), 'chicken': K('cearc'), 'fowl': K('cearc'), 'turkey': K('cearc', ('adj', 'mòr')),
    'grouse': K('cearc', ('gen', 'fraoch')), 'quail': K('cearc', ('adj', 'beag')), 'partridge': K('cearc'), 'peacock': K('cearc', ('gen', 'dealbh')),
    'pheasant': K('easag'), 'sparrow': K('gealbhan'), 'finch': K('breacanbeithe'), 'wren': K('dreathandonn'), 'lark': K('uiseag'),
    'thrush': K('smeòrach'), 'blackbird': K('smeòrach'), 'robin': K('brùdhearg'), 'woodpecker': K('eun', ('gen', 'craobh')),
    'hummingbird': K('eun', ('adj', 'bìodach')), 'kingfisher': K('eun', ('gen', 'iasc')), 'hornbill': K('eun', ('gen', 'adharc')),
    'ostrich': K('eun', ('adj', 'anamhòr')), 'emu': K('eun', ('adj', 'anamhòr')), 'kiwi': K('eun', ('adj', 'beag')), 'moa': K('eun', ('adj', 'anamhòr')),
    'rhea': K('eun', ('adj', 'anamhòr')), 'cassowary': K('eun', ('adj', 'borb')), 'bustard': K('eun', ('adj', 'trom')),
    'rail': K('eun', ('gen', 'cuilc')), 'cormorant': K('eun', ('gen', 'iasc')), 'pelican': K('eun', ('gen', 'gob')), 'booby': K('faoileag', ('adj', 'trom')),
    'grebe': K('lach', ('adj', 'beag')), 'coot': K('lach'), 'loon': K('lach'), 'auk': K('faoileag'), 'puffin': K('faoileag'),
    # insects and arthropods
    'bee': K('seillean'), 'bumblebee': K('seillean', ('adj', 'molach')), 'wasp': K('seillean', ('gen', 'nimh')), 'hornet': K('seillean', ('gen', 'nimh')),
    'beetle': K('daolag'), 'ladybug': K('daolag'), 'weevil': K('daolag'), 'ant': K('seangan'), 'termite': K('seangan', ('adj', 'geal')),
    'butterfly': K('dealandè'), 'moth': K('leòmann'), 'swallowtail': K('dealandè'), 'monarch': K('dealandè'), 'viceroy': K('dealandè'),
    'spider': K('damhanallaidh'), 'tarantula': K('damhanallaidh', ('adj', 'molach')), 'scorpion': K('scairp'), 'mantis': K('meanbhbhiastag', ('gen', 'ùrnaigh')),
    'fly': K('meanbhbhiastag', ('gen', 'iteag')), 'mosquito': K('meanbhbhiastag', ('gen', 'fuil')), 'cicada': K('meanbhbhiastag', ('gen', 'ceòl')),
    'locust': K('meanbhbhiastag', ('gen', 'feur')), 'cricket': K('meanbhbhiastag', ('gen', 'ceòl')), 'dragonfly': K('meanbhbhiastag', ('gen', 'dreagan')),
    'bug': K('meanbhbhiastag'), 'trilobite': K('cnuimh', ('gen', 'slige')),
    # mammals
    'bat': K('ialtag'), 'mouse': K('luch'), 'rat': K('radan'), 'vole': K('luch', ('gen', 'feur')), 'squirrel': K('feòrag'), 'chipmunk': K('feòrag'),
    'marmot': K('feòrag', ('gen', 'beinn')), 'beaver': K('dòbhran', ('gen', 'craobh')), 'porcupine': K('gràineag', ('adj', 'mòr')),
    'capybara': K('radan', ('gen', 'abhainn')), 'chinchilla': K('luch', ('gen', 'beinn')), 'hare': K('maigheach'), 'rabbit': K('coineanach'),
    'pika': K('maigheach', ('gen', 'creag')), 'hedgehog': K('gràineag'), 'mole': K('famh'), 'shrew': K('luch', ('adj', 'bìodach')), 'tenrec': K('gràineag'),
    'fox': K('sionnach'), 'wolf': K('mactìre'), 'dog': K('cù'), 'jackal': K('mactìre', ('adj', 'beag')), 'coyote': K('mactìre', ('adj', 'beag')),
    'dingo': K('cù', ('adj', 'fiadhaich')), 'dhole': K('cù', ('adj', 'fiadhaich')), 'cat': K('cat'), 'lion': K('leòmhann'), 'tiger': K('tìgear'),
    'leopard': K('liopard'), 'jaguar': K('liopard', ('adj', 'mòr')), 'cheetah': K('liopard', ('adj', 'luath')), 'lynx': K('cat', ('adj', 'fiadhaich')),
    'bobcat': K('cat', ('adj', 'fiadhaich')), 'puma': K('cat', ('adj', 'mòr')), 'cougar': K('cat', ('adj', 'mòr')), 'panther': K('liopard', ('adj', 'dubh')),
    'smilodon': K('cat', ('gen', 'tosc')), 'hyena': K('madadhgàire'), 'bear': K('mathan'), 'panda': K('mathan', ('adj', 'geal')),
    'badger': K('broc'), 'weasel': K('neas'), 'marten': K('neas', ('gen', 'craobh')), 'stoat': K('neas'), 'ferret': K('neas'), 'mink': K('neas', ('gen', 'uisce')),
    'wolverine': K('neas', ('adj', 'borb')), 'skunk': K('neas', ('gen', 'nimh')), 'raccoon': K('broc', ('gen', 'làmh')), 'mongoose': K('neas', ('adj', 'luath')),
    'civet': K('cat', ('gen', 'oidhche')), 'genet': K('cat', ('gen', 'oidhche')),
    'deer': K('fiadh'), 'elk': K('lon'), 'moose': K('lon'), 'caribou': K('fiadh', ('gen', 'sneachd')), 'reindeer': K('fiadh', ('gen', 'sneachd')),
    'horse': K('each'), 'zebra': K('each', ('adj', 'stiallach')), 'ass': K('asal'), 'donkey': K('asal'), 'onager': K('asal', ('adj', 'fiadhaich')),
    'cow': K('bò'), 'ox': K('damh'), 'bison': K('tarbh', ('adj', 'molach')), 'buffalo': K('tarbh', ('gen', 'uisce')), 'yak': K('tarbh', ('gen', 'beinn')),
    'aurochs': K('tarbh', ('adj', 'fiadhaich')), 'wisent': K('tarbh', ('adj', 'molach')), 'antelope': K('fiadh', ('adj', 'luath')), 'gazelle': K('fiadh', ('adj', 'luath')),
    'sheep': K('caora'), 'goat': K('gobhar'), 'ibex': K('gobhar', ('gen', 'creag')), 'chamois': K('gobhar', ('gen', 'beinn')), 'takin': K('gobhar', ('adj', 'trom')),
    'pig': K('muc'), 'boar': K('torc'), 'hog': K('muc'), 'peccary': K('muc', ('gen', 'coille')), 'warthog': K('torc', ('gen', 'tosc')),
    'camel': K('càmhal'), 'llama': K('càmhal', ('adj', 'beag')), 'alpaca': K('càmhal', ('adj', 'beag')), 'guanaco': K('càmhal', ('adj', 'beag')),
    'vicuna': K('càmhal', ('adj', 'beag')), 'giraffe': K('fiadh', ('gen', 'amhach')), 'okapi': K('fiadh', ('gen', 'amhach')),
    'elephant': K('ailbhean'), 'mammoth': K('ailbhean', ('adj', 'molach')), 'mastodon': K('ailbhean', ('gen', 'coille')),
    'rhinoceros': K('srònadharcach'), 'rhino': K('srònadharcach'), 'hippopotamus': K('eachabhainn'), 'hippo': K('eachabhainn'),
    'tapir': K('muc', ('gen', 'sròn')), 'manatee': K('ròn', ('gen', 'feur')), 'dugong': K('ròn', ('gen', 'feur')), 'aardvark': K('famh', ('gen', 'seangan')),
    'monkey': K('muncaidh'), 'macaque': K('muncaidh'), 'baboon': K('muncaidh', ('adj', 'borb')), 'gibbon': K('muncaidh', ('gen', 'craobh')),
    'lemur': K('muncaidh', ('gen', 'oidhche')), 'gorilla': K('muncaidh', ('adj', 'anamhòr')), 'chimpanzee': K('muncaidh'), 'orangutan': K('muncaidh', ('adj', 'ruadh')),
    'sloth': K('leisceancraoibhe'), 'kangaroo': K('cangarù'), 'wallaby': K('cangarù', ('adj', 'beag')), 'wombat': K('famh', ('adj', 'trom')),
    'koala': K('leisceancraoibhe', ('gen', 'bian')), 'opossum': K('luch', ('gen', 'oidhche')), 'platypus': K('dòbhran', ('gen', 'gob')),
    'echidna': K('gràineag', ('gen', 'gob')), 'armadillo': K('beathach', ('gen', 'slige')), 'pangolin': K('beathach', ('gen', 'slige')),
    'anteater': K('beathach', ('gen', 'seangan')), 'glyptodon': K('beathach', ('gen', 'slige')),
    'dragon': K('dreagan'),
}
# plan fallback kinds (also the kind names for the entity.* lang keys)
PK = {
    'ammonite': K('seilcheag', ('gen', 'cuan')), 'ankylosaur': K('dreagan', ('gen', 'clach')), 'archaic_whale': K('mucmhara', ('adj', 'àrsaidh')),
    'armadillo': K('beathach', ('gen', 'slige')), 'bat': K('ialtag'), 'beetle': K('daolag'), 'cephalopod': K('gibearnach'), 'ceratopsian': K('dreagan', ('gen', 'adharc')),
    'corvid': K('feannag'), 'crab': K('partan'), 'crocodilian': K('crogall'), 'dolphin': K('leumadair'), 'dromaeosaur': K('dreagan', ('gen', 'ìne')),
    'early_tetrapod': K('loscann', ('adj', 'àrsaidh')), 'eel': K('eascann'), 'eurypterid': K('scairp', ('gen', 'cuan')), 'fish_deep': K('iasc', ('gen', 'doimhne')),
    'fish_flat': K('iasc', ('adj', 'cam')), 'fish_fusiform': K('iasc', ('adj', 'luath')), 'fish_primitive': K('iasc', ('adj', 'àrsaidh')),
    'frog': K('loscann'), 'galliform': K('cearc'), 'giant_arthropod': K('cnuimh', ('adj', 'anamhòr')), 'giraffid': K('fiadh', ('gen', 'amhach')),
    'hadrosaur': K('dreagan', ('gen', 'gob')), 'ichthyosaur': K('iasc', ('gen', 'dreagan')), 'insect_flying': K('meanbhbhiastag', ('gen', 'iteag')),
    'insectivore': K('luch', ('adj', 'bìodach')), 'jellyfish': K('muirtèachd'), 'lagomorph': K('maigheach'), 'lizard': K('dearcluachrach'),
    'macropod': K('cangarù'), 'marsupial_stout': K('famh', ('adj', 'trom')), 'monotreme': K('dòbhran', ('gen', 'gob')), 'mosasaur': K('crogall', ('gen', 'cuan')),
    'mustelid': K('neas'), 'ornithopod_small': K('dreagan', ('adj', 'beag')), 'owl': K('comhachag'), 'parrot': K('euncainnte'), 'penguin': K('ceannfionn'),
    'pigeon': K('calman'), 'pinniped': K('ròn'), 'placoderm': K('iasc', ('gen', 'cloich')), 'plesiosaur': K('dreagan', ('gen', 'amhach')),
    'primate_ape': K('muncaidh', ('adj', 'mòr')), 'primate_monkey': K('muncaidh'), 'primate_small': K('muncaidh', ('adj', 'beag')), 'pterosaur': K('dreagan', ('gen', 'sciath')),
    'raptor': K('seabhag'), 'ratite': K('eun', ('adj', 'anamhòr')), 'ray': K('gath'), 'rodent_large': K('radan', ('adj', 'mòr')), 'rodent_small': K('luch'),
    'sauropod': K('dreagan', ('gen', 'druim')), 'scorpion': K('scairp'), 'sea_turtle': K('sligeanach', ('gen', 'cuan')), 'seabird': K('faoileag'),
    'sloth_tree': K('leisceancraoibhe'), 'songbird': K('eun', ('adj', 'beag')), 'starfish': K('rionnag'), 'stegosaur': K('dreagan', ('gen', 'dealg')),
    'synapsid_sail': K('beathach', ('gen', 'sciath')), 'syngnathid': K('iasc', ('adj', 'caol')), 'tapir': K('muc', ('gen', 'sròn')),
    'terror_bird': K('eun', ('adj', 'borb')), 'therapsid': K('beathach', ('adj', 'àrsaidh')), 'theropod': K('dreagan', ('gen', 'tosc')), 'trilobite': K('cnuimh', ('gen', 'slige')),
    'viverrid': K('cat', ('gen', 'oidhche')), 'wader': K('corraghritheach'), 'waterfowl': K('lach'),
    'aardvark': K('famh', ('gen', 'seangan')), 'arowana': K('iasc', ('adj', 'òrach')), 'baleen_whale': K('mucmhara'), 'bear': K('mathan'),
    'big_cat': K('cat', ('adj', 'mòr')), 'bison': K('tarbh', ('adj', 'molach')), 'boar': K('torc'), 'bovid': K('bò', ('adj', 'fiadhaich')), 'camel': K('càmhal'),
    'canid': K('mactìre'), 'cervid': K('fiadh'), 'elephantid': K('ailbhean'), 'equid': K('each'), 'football_fish': K('iasc', ('gen', 'oidhche')),
    'giant_clam': K('slige', ('adj', 'anamhòr')), 'giant_salamander': K('dearcluachrach', ('adj', 'anamhòr')), 'glyptodont': K('beathach', ('gen', 'slige')),
    'ground_sloth': K('leisceancraoibhe', ('gen', 'talamh')), 'hippo': K('eachabhainn'), 'hyena': K('madadhgàire'), 'king_crab': K('partan', ('adj', 'anamhòr')),
    'large_snake': K('nathair', ('adj', 'anamhòr')), 'manatee': K('ròn', ('gen', 'feur')), 'monitor': K('dearcluachrach', ('adj', 'anamhòr')),
    'newt': K('dearcluachrach'), 'opossum': K('luch', ('gen', 'oidhche')), 'rhino': K('srònadharcach'), 'shark': K('cearban'), 'snake': K('nathair'),
    'softshell_turtle': K('sligeanach', ('adj', 'bog')), 'spadefish': K('iasc', ('adj', 'cam')), 'sunfish': K('iasc', ('gen', 'grian')),
    'tarantula': K('damhanallaidh', ('adj', 'molach')), 'tortoise': K('sligeanach', ('gen', 'talamh')), 'trevally': K('iasc', ('adj', 'luath')),
    'triggerfish': K('iasc', ('gen', 'dealg')), 'whale_shark': K('cearban', ('adj', 'anamhòr')),
}
PK['fish_deep'] = K('iasc', ('adj', 'domhain')) if 'domhain' in LROD else K('iasc')
PK['placoderm'] = K('iasc', ('gen', 'clach'))

# ---------------------------------------------------------------- describers: ('adj', rod) | ('gen', noun rod) | ('raw', form, gender)
ADJ = dict(
    black='dubh', red='dearg', white='geal', blue='gorm', green='uaine', yellow='buidhe', brown='donn', grey='glas', gray='glas',
    golden='òrach', silver='airgeadach', spotted='ballach', leopard='ballach', striped='stiallach', banded='stiallach', ringed='stiallach',
    giant='anamhòr', great='mòr', greater='mòr', large='mòr', big='mòr', lesser='beag', little='beag', small='beag', dwarf='beag', pygmy='bìodach',
    tiny='bìodach', long='fada', short='goirid', wild='fiadhaich', swift='luath', hairy='molach', woolly='molach', shaggy='molach', smooth='lom',
    rough='garbh', dark='dorcha', pale='bàn', tawny='odhar', dusky='odhar', fat='reamhar', slender='caol', thin='tana', ruddy='ruadh', rufous='ruadh',
    orange='ruadh', pink='ruadh', scarlet='dearg', crimson='dearg', cobalt='gorm', olive='uaine', purple='gorm', marbled='ballach', painted='ballach',
    laughing='gàire', old='aosta', cold='fuar', hot='teth', ghost='bàn', albino='geal', brindled='ballach', flat='cam', hooded='dubh', king='mòr', emperor='mòr',
    royal='mòr', imperial='mòr', massive='trom', heavy='trom', robust='trom', stout='trom', woolly_='molach', arctic='fuar', antarctic='fuar', polar='fuar',
    snow='geal', snowy='geal', common='', true='',
)
GENN = dict(
    sea='muir', ocean='cuan', river='abhainn', lake='loch', desert='fàsach', mountain='beinn', rock='creag', rocky='creag', tree='craobh', forest='coille',
    wood='coille', woodland='coille', bush='coille', jungle='coille', island='eilean', cave='uamh', sand='cladach', beach='tràigh', shore='cladach',
    swamp='boglach', marsh='boglach', bog='boglach', snow='sneachd', ice='deigh', fire='teine', water='uisce', ground='talamh', night='oidhche',
    vampire='oidhche', moon='gealach', sun='grian', day='latha', storm='stoirm', wind='gaoth', king='ceann', queen='ceann', prairie='feur', plains='raon',
    steppe='feur', grass='feur', meadow='feur', field='raon', reef='creag', coral='creag', mud='ùir', desert_='fàsach', alpine='beinn', highland='beinn',
    venom='nimh', poison='nimh', poisonous='nimh', stone='clach', horned='adharc', horn='adharc', tailed='earball', tail='earball', headed='ceann',
    head='ceann', billed='gob', bill='gob', beaked='gob', footed='cas', legged='cas', toed='ìne', clawed='ìne', eared='cluas', ear='cluas', nosed='sròn',
    nose='sròn', bellied='bolg', belly='bolg', backed='druim', back='druim', necked='amhach', neck='amhach', crested='cìrean', crest='cìrean',
    tusked='tosc', tusk='tosc', winged='sciath', wing='sciath', faced='sùil', eyed='sùil', eye='sùil', spiny='dealg', spine='dealg', thorn='dealg',
    throated='amhach', breasted='uchd', fronted='ceann', crowned='ceann', capped='ceann', maned='muing', mane='muing', fur='bian', fin='sciath',
    finned='sciath', toothed='fiacail', tooth='fiacail', shell='slige', shelled='slige', armoured='slige', bone='cnàimh', skin='craiceann',
    flying='iteag', feather='iteag', feathered='iteag', fairy='sìth', ghost_='sìth', paradise='sìth', lion='leòmhann', dragon='dreagan',
    sword='tosc', hammerhead='ceann', saw='fiacail', whale='mucmhara', stone_='clach', sail='sciath', sailed='sciath', frilled='muing', whiskered='muing',
    bearded='muing', hooded_='ceann', cave_='uamh', pine='craobh', oak='craobh', cedar='craobh', palm='craobh', mangrove='craobh', swampy='boglach',
    tidal='tonn', wave='tonn', thunder='tàirneanach', fog='ceò', mist='ceò', dew='driùchd', reed='cuilc', heather='fraoch', moor='mòinteach', hill='cnoc',
    valley='gleann', glen='gleann', burrowing='uamh', soil='ùir', earth='talamh', cliff='creag', crag='creag', harlequin='dealbh', tiger='tìgear',
    leopard_='liopard', zebra='each', horse='each', cat='cat', dog='cù', fox='sionnach', wolf='mactìre', bear='mathan', bull='tarbh', ox='damh',
    pig='muc', cow='bò', goat='gobhar', rat='radan', mouse='luch', bird='eun', fish='iasc', snake='nathair', eagle='iolair', hawk='seabhag',
    sheep='caora', deer='fiadh', elephant='ailbhean',
)
# demonyms / ocean names -> place genitive (raw form, gender)
PLACE = dict(
    european=('cnuic', 'm'), eurasian=('cnuic', 'm'), british=('cnuic', 'm'), american=('sratha', 'm'), canadian=('sratha', 'm'), mexican=('sratha', 'm'),
    african=('grèine', 'f'), egyptian=('grèine', 'f'), nile=('grèine', 'f'), asian=('ceò', 'm'), indian=('ceò', 'm'), chinese=('ceò', 'm'), japanese=('ceò', 'm'),
    siberian=('ceò', 'm'), himalayan=('ceò', 'm'), malayan=('ceò', 'm'), javan=('ceò', 'm'), sunda=('ceò', 'm'), philippine=('ceò', 'm'),
    australian=('ùire', 'f'), tasmanian=('ùire', 'f'), atlantic=('tuinne', 'f'), pacific=('cuain', 'm'), indo=('cuain', 'm'), mediterranean=('mara', 'f'),
    caribbean=('tràghad', 'f'), brazilian=('fraoich', 'm'), amazon=('fraoich', 'm'), andean=('fraoich', 'm'), cuban=('tràghad', 'f'), jamaican=('tràghad', 'f'),
    hawaiian=('cuain', 'm'), arabian=('fàsaich', 'm'), cape=('gaoithe', 'f'), southern=('deas', 'f'), northern=('tuath', 'f'), eastern=('ear', 'f'),
    western=('iar', 'f'), south=('deas', 'f'), north=('tuath', 'f'), east=('ear', 'f'), west=('iar', 'f'), central=('cridhe', 'm'), mascarene=('cuain', 'm'),
)
REGION_GRP = [
    (r'Europe|British|Scandinavia|Alps|Mediterranean$|Baltic|Iceland', ('cnuic', 'm')), (r'N America|Great Plains|Canadian|Pacific NW|Alaska|Florida|S US|Nearctic|W N America|Eastern N America', ('sratha', 'm')),
    (r'S America|Amazon|Andes|Pampas|Patagonia|Southern Cone|Neotropics', ('fraoich', 'm')), (r'Mexico|C America|Central America|Caribbean$|Galapagos', ('cuilce', 'f')),
    (r'Africa|Sahel|Congo|Nile|Madagascar|Afrotropics', ('grèine', 'f')), (r'Sahara|Arabia|Iran', ('fàsaich', 'm')),
    (r'Asia|Indian sub|China|Japan|Siberia|Himalaya|Indochina|Sunda|Wallacea|Indomalaya|Palearctic|steppe|Beringia', ('ceò', 'm')),
    (r'Australia|Tasmania|Sahul|New Zealand|Polynesia', ('ùire', 'f')), (r'Atlantic', ('tuinne', 'f')), (r'Pacific|Indian Ocean|Coral|Reef|Tropical seas', ('cuain', 'm')),
    (r'Arctic|Antarctic|Subantarctic', ('deighe', 'f')), (r'Red Sea|Black|Caspian|Caribbean sea|Mediterranean sea', ('mara', 'f')), (r'Hawaii|Mascarenes', ('eilein', 'm')),
]
HAB = dict(forest=('coille', 'f'), jungle=('coille', 'f'), plains=('raon', 'm'), savanna=('feòir', 'm'), taiga=('craoibhe', 'f'), swamp=('boglaich', 'f'),
           desert=('fàsaich', 'm'), badlands=('cloiche', 'f'), mountain=('beinne', 'f'), icy=('deighe', 'f'), river=('aibhne', 'f'), lake=('locha', 'm'),
           coast=('cladaich', 'm'), reef=('creige', 'f'), ocean_warm=('cuain', 'm'), ocean_cold=('sneachda', 'm'), ocean_deep=('doimhne', 'f'),
           cave=('uamha', 'f'), island=('eilein', 'm'), tundra=('sneachda', 'm'), grassland=('feòir', 'm'), wetland=('boglaich', 'f'))
HAB['ocean_deep'] = ('cuain', 'm')
DIET = dict(carn=('fala', 'm'), omni=None, herb=('feòir', 'm'), pisc=('èisg', 'm'), insect=('seangain', 'm'), fil=('uisce', 'm'), scav=('cnàimh', 'm'), frug=('craoibhe', 'f'), nect=('blàith', 'm'))
NOTE = dict(nocturnal=('oidhche', 'f'), venomous=('nimhe', 'f'), toxic=('nimhe', 'f'), arboreal=('craoibhe', 'f'), burrow=('uamha', 'f'), flies=('sgèithe', 'f'),
            swims=('uisce', 'm'), herd=('sluaigh', 'm'), pack=('sluaigh', 'm'), migratory=('gaoithe', 'f'), fast=None, tusk=('tuisc', 'm'), horn=('adhairc', 'f'), shell=('slige', 'f'))
PERIOD = dict(Cambrian=('sliogach', 'm'), Ordovician=('cuain', 'm'), Silurian=('creige', 'f'), Devonian=('èisg', 'm'), Carboniferous=('cèir', 'f'),
              Permian=('luaithre', 'f'), Triassic=('talmhainn', 'f'), Jurassic=('gleann', 'm'), Cretaceous=('cloiche', 'f'), Paleocene=('earraich', 'm'),
              Eocene=('samhraidh', 'm'), Oligocene=('fogharaidh', 'm'), Miocene=('geamhraidh', 'm'), Pliocene=('driùchd', 'm'), Pleistocene=('deighe', 'f'))
PERIOD = {k: v for k, v in PERIOD.items() if k not in ('Cambrian', 'Ordovician', 'Silurian', 'Devonian', 'Carboniferous')}
PERIOD.update(Cambrian=('tòiseachaidh', 'm'), Ordovician=('uisge', 'm'), Silurian=('mara', 'f'), Devonian=('èisg', 'm'), Carboniferous=('cèir', 'f'))
PERIOD['Cambrian'] = ('dorchadais', 'm')
PERIOD['Ordovician'] = ('tuinne', 'f')
PERIOD['Silurian'] = ('mara', 'f')
# epithet pool (nature nouns, genitive); used only to break remaining ties
POOL = [('grèine', 'f'), ('gealaich', 'f'), ('gaoithe', 'f'), ('stoirme', 'f'), ('tàirneanaich', 'm'), ('teine', 'm'), ('ceò', 'm'), ('driùchd', 'm'),
        ('tuinne', 'f'), ('cnuic', 'm'), ('glinne', 'm'), ('sratha', 'm'), ('cuilce', 'f'), ('fraoich', 'm'), ('luaithre', 'f'), ('ùire', 'f'), ('uaighe', 'f'),
        ('catha', 'm'), ('cogaidh', 'm'), ('sìthe', 'f'), ('bròin', 'm'), ('gutha', 'm'), ('cèoil', 'm'), ('bàird', 'm'), ('draoidh', 'm'), ('saoi', 'm'),
        ('solais', 'm'), ('dorchadais', 'm'), ('cuimhne', 'f'), ('ainme', 'm'), ('lìn', 'm'), ('cèire', 'f'), ('snàith', 'm'), ('àtha', 'm'), ('geamhraidh', 'm'),
        ('samhraidh', 'm'), ('earraich', 'm'), ('fogharaidh', 'm'), ('sealgair', 'm'), ('cloiche', 'f'), ('craoibhe', 'f'), ('tobair', 'm'), ('deilbh', 'm'),
        ('cèim', 'm'), ('cuain', 'm'), ('àigh', 'm'), ('rionnaige', 'f'), ('creutair', 'm')]
POOL_SRC = ['grian', 'gealach', 'gaoth', 'stoirm', 'tàirneanach', 'teine', 'ceò', 'driùchd', 'tonn', 'cnoc', 'gleann', 'srath', 'cuilc', 'fraoch', 'luaithre',
            'ùir', 'uaigh', 'cath', 'cogadh', 'sìth', 'bròn', 'guth', 'ceòl', 'bàrd', 'draoidh', 'saoi', 'solas', 'dorchadas', 'cuimhne', 'ainm', 'lìon', 'cèir',
            'snàth', 'àth', 'geamhradh', 'samhradh', 'earrach', 'foghar', 'sealgair', 'clach', 'craobh', 'tobar', 'dealbh', 'ceum', 'cuan', 'àgh', 'rionnag', 'creutair']
SRC_ROOTS = set(POOL_SRC) | set(GENN.values()) | set(ADJ.values()) | {h for h, _ in map(lambda v: v, KW.values())} | {h for h, _ in PK.values()}
SRC_ROOTS |= {q[1] for h, q in list(KW.values()) + list(PK.values()) if q}
SRC_ROOTS.discard('')
SRC_ROOTS -= {'cridhe'}


def mk(h, q):
    """kind head (+ fixed qualifier) -> (classical string, gender)"""
    s, g = h, gender(h)
    if q:
        t, w = q
        if t == 'gen':
            s += gen_of(w); g = gender(w)
        else:
            s += (lenite(w) if g == 'f' else w)
    return s, g


def chain(head, descs):
    s, g = head
    for d in descs:
        if d[0] == 'adj':
            s += lenite(d[1]) if g == 'f' else d[1]
        elif d[0] == 'gen':
            s += gen_of(d[1]); g = gender(d[1])
        else:
            s += d[1]; g = d[2]
    return s


def tokens(name):
    return [t for t in re.split(r"[^a-z]+", name.lower().replace("'s", "").replace("’s", "")) if t]


def kind_for(common, plan):
    toks = tokens(common)
    for t in reversed(toks):
        t2 = t[:-1] if t.endswith('s') and t[:-1] in KW else t
        if t2 in KW:
            return KW[t2], t2
    for i in range(len(toks) - 1):  # two-word keys such as sea lion
        if toks[i] + toks[i + 1] in KW:
            return KW[toks[i] + toks[i + 1]], toks[i] + toks[i + 1]
    p = plan.split(':')[1]
    return PK[p], None


def describers(row, fossil, kw):
    """ordered candidate describers (most specific first)."""
    common, plan = row['common'], row['plan']
    out, seen = [], set()

    def add(d):
        k = tuple(d)
        if k not in seen:
            seen.add(k); out.append(d)
    toks = tokens(common)
    hy = common.lower()
    for t in toks:
        if t == kw:
            continue
        if t in PLACE:
            add(('raw',) + PLACE[t])
    mod = []
    for t in toks:
        if t == kw:
            continue
        if ADJ.get(t):
            mod.append(('adj', ADJ[t]))
        elif t in GENN and t not in PLACE:
            mod.append(('gen', GENN[t]))
    for m in mod:
        add(m)
    if fossil:
        p = PERIOD.get(row['period'])
        if p:
            add(('raw',) + p)
    for h in row['hab']:
        if h in HAB:
            add(('raw',) + HAB[h])
    for r in row['range']:
        for pat, v in REGION_GRP:
            if re.search(pat, r):
                add(('raw',) + v)
                break
    for n in row['notes']:
        if NOTE.get(n):
            add(('raw',) + NOTE[n])
    d = DIET.get(row['diet'])
    if d:
        add(('raw',) + d)
    return out


def load():
    rows = {}
    for f in sorted(glob.glob(os.path.join(args.cat, '*.psv'))):
        b = os.path.basename(f)
        if b.startswith('coat_'):
            continue
        fossil = 'fossil' in b
        for l in open(f, encoding='utf8'):
            if l.startswith('#') or not l.strip():
                continue
            p = l.rstrip('\n').split('|')
            if fossil:  # id common latin plan period ma_from ma_to rock region formation scale kg diet dna notes
                r = dict(id=p[0], common=p[1], latin=p[2], plan=p[3], period=p[4], range=[p[8]] if p[8] else [], hab=[], diet=p[12], notes=p[14].split(',') if len(p) > 14 else [], fossil=True)
            else:  # id common latin plan uwkey range ext scale diet habitat kg notes
                r = dict(id=p[0], common=p[1], latin=p[2], plan=p[3], period='', range=[x.strip() for x in p[5].split(';')], hab=p[9].split(';'), diet=p[8],
                         notes=[n.strip() for n in p[11].split(',')] if len(p) > 11 else [], fossil=False)
            r['notes'] = [n.strip() for n in r['notes']]
            rows.setdefault(r['id'], r)
    return rows


def main():
    rows = load()
    miss = sorted(w for w in SRC_ROOTS if w not in LROD)
    if miss:
        sys.exit('roots not in LEXICON: %s' % miss)
    ids = sorted(rows)
    heads, cand, kinds = {}, {}, {}
    for i in ids:
        r = rows[i]
        (h, q), kw = kind_for(r['common'], r['plan'])
        heads[i] = mk(h, q)
        cand[i] = describers(r, r['fossil'], kw)
    # tiered resolution: names that collide at k describers move to k+1
    final, used = {}, set()
    pending = list(ids)
    maxk = 3
    for k in range(1, maxk + 1):
        groups = collections.defaultdict(list)
        for i in pending:
            if len(cand[i]) >= k:
                groups[chain(heads[i], cand[i][:k])].append(i)
        for s, g in groups.items():
            if len(g) == 1 and s not in used:
                final[g[0]] = (s, k); used.add(s)
        pending = [i for i in pending if i not in final]
    # remaining: use up to 3 describers + epithet from the nature pool, chosen by hash of the id
    for i in pending:
        base = chain(heads[i], cand[i][:2])
        hx = int(hashlib.sha1(i.encode()).hexdigest(), 16)
        n = len(POOL)
        for j in range(n * n):
            a = POOL[(hx + j) % n]
            s = base + a[0] if j < n else base + POOL[(hx // 7 + j) % n][0] + a[0]
            if s not in used:
                used.add(s); final[i] = (s, -1); break
    # archaic layer + validation
    names = {}
    for i in ids:
        cl = final[i][0]
        w = A.word(cl)
        names[i] = w[:1].upper() + w[1:]
    return rows, final, names


def validate(rows, final, names):
    bad, seen = [], collections.Counter(n.lower() for n in names.values())
    for i, n in names.items():
        cl = final[i][0]
        ok = (A.text(cl) == A.word(cl)) and n.lower() not in REAL and re.fullmatch(r"[^\W\d_]+", n) and seen[n.lower()] == 1
        if not ok:
            bad.append((i, n))
    return bad


if __name__ == '__main__':
    rows, final, names = main()
    bad = validate(rows, final, names)
    # plan-kind names for the entity.* lang keys
    plan_names = {}
    for p, (h, q) in PK.items():
        s, _ = mk(h, q)
        w = A.word(s)
        plan_names['entity.diaithirfauna.' + p] = w[:1].upper() + w[1:]
    os.makedirs(os.path.dirname(args.out), exist_ok=True)
    json.dump(dict(sorted(names.items())), open(args.out, 'w', encoding='utf8'), ensure_ascii=False, indent=1)
    lang = dict(plan_names)
    lang.update({'species.diaithirfauna.' + i: n for i, n in sorted(names.items())})
    os.makedirs(os.path.dirname(os.path.abspath(args.lang)), exist_ok=True)
    json.dump(lang, open(args.lang, 'w', encoding='utf8'), ensure_ascii=False, indent=0)
    tiers = collections.Counter(k for _, k in final.values())
    print('names', len(names), 'unique', len(set(n.lower() for n in names.values())), 'validated', len(names) - len(bad), 'rejected', len(bad), 'tiers', dict(sorted(tiers.items())))
    for b in bad[:20]:
        print('BAD', b)
    sys.exit(1 if bad else 0)
