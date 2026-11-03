import json, os
J='/mnt/project-files/compute/jobs/'
MEL='''You design the pixel-art silhouette of ONE real historical melee weapon for a 64 px item icon. Output ONE JSON object only. Draw the real weapon accurately from its name (shape, proportions, curvature, head type).
Coordinates: the weapon lies along an axis. L = total length in units 0..78 (pommel at 0, tip at L; short knives ~30, swords 55-70, spears/polearms 78). All other numbers are in the same units (1 unit ~ 1 px).
Schema:
{"L":int,
 "head":{"type":"blade|axe|mace|spear|hook|ring|star|boomerang|club|katar|none",
  blade: "len","w"(max width 2-9),"taper"(tip width / base width 0..1),"curve"(-1 curves toward back edge .. 1 curves toward cutting edge, 0 straight),"tip":"point|round|clip|chisel","fuller":bool,"back":"flat|swell"(swell = wider near tip like a kopis/kukri),
  axe: "len"(along shaft),"w"(blade depth),"h"(edge height),"beard":0..1,"double":bool,"spike":bool,
  mace: "shape":"ball|flange|spike|block|disc|star|teardrop","r"(radius 3-8),
  spear: "len","w","shape":"leaf|triangle|barbed|broad|needle|diamond","extra":"none|hook|axe|crescent|sidebar|blade_back",
  hook: "len","w","curve"(scythe/sickle/pick shape),
  ring/star/boomerang: "r","points"(star),
  club: "len","w","studs":bool, katar: "len","w"},
 "guard":{"type":"none|bar|disc|basket|swept|cross|knuckle|hilt_ring","w":int},
 "handle":{"len":int,"w":int(1-3),"wrap":bool,"pommel":"none|ball|disc|spike|ring|tassel"},
 "shaft":bool (true when a long wooden haft runs from pommel to head: spears, polearms, axes, maces)}
Always fill every key that applies. JSON only.'''
GUN='''You design the pixel-art silhouette of ONE real firearm for a 64 px item icon, side view, barrel pointing right, drawn in a 64x64 canvas. Output ONE JSON object only. Draw the real gun accurately from its name and era.
Schema:
{"class":"handcannon|matchlock|wheellock|flintlock_long|flintlock_pistol|percussion_long|revolver|pistol|rifle|smg|mg|shotgun|launcher|bullpup|energy|minigun",
 "barrel_len":int(10-50 px),"barrel_w":int(1-5),"barrel_style":"round|octagon|ribbed|shrouded|short|heavy",
 "stock":"none|straight|pistol_grip|skeleton|folding|bullpup|thumbhole|full_wood|club",
 "lock":"none|match|wheel|flint|percussion|bolt|lever|semi|revolver_cyl|pump|break|gas",
 "mag":"none|box|drum|tube|belt|banana|top_pan","mag_len":int,
 "fore":"none|wood|rail|pump_slide|ramrod",
 "scope":"none|short|long|iron","bayonet":bool,"muzzle":"none|brake|suppressor|flare","sling":bool,"two_shot":bool}
Full-length weapons use barrel_len 28-50, pistols 10-20. JSON only.'''
TOOL='''You design the pixel-art silhouette of ONE everyday tool, utensil or household item for a 64 px item icon. Output ONE JSON object only. Pick the archetype that matches the real object and set sensible numbers.
Handheld archetypes lie on a diagonal axis (handle bottom-left, working end top-right), L = length 0..78 (small knives ~34, long tools 70).
Vessel archetypes are drawn upright in the canvas.
Schema:
{"arch":"axe|hammer|pick|blade|saw|scoop|fork|tongs|brush|needle|rod|chisel|plane|sickle|rake|hoe|spade|pot|pan|bowl|jug|cup|kettle|jar|basket|bucket|box|lamp|scale|wheel|book|sack|spool|generic,
 "L":int, "head_w":int, "head_len":int, "handle_w":int(1-3), "shape":"short word describing the working end, e.g. flat|curved|round|pointed|toothed|notched|serrated|crescent",
 "teeth":int(0..12), "handle":"wood|none|loop|crossbar|knob|ring",
 for vessels: "w","h"(width,height 14..52), "lid":bool,"spout":bool,"handles":0|1|2,"foot":bool,"neck":0..1}
The object has one or two colourable parts (head, handle) or for vessels (pot, handle). JSON only.'''
def w(name,system,items,mt):
    os.makedirs(J+name,exist_ok=True)
    with open(J+name+'/in.jsonl','w') as f:
        for i,p in items: f.write(json.dumps({'id':i,'system':system,'prompt':p})+'\n')
    open(J+name+'/README','w').write('Silhouette specs for weapon/tool icon layers. model: haiku. max_tokens %d. Check rule: one JSON object. Owner: texture lead thread.\n'%mt)
    print(name,len(items),os.path.getsize(J+name+'/in.jsonl'))
h=json.load(open('hist_in.json'))
w('gear_spec_melee',MEL,[(x['id'],'Weapon id: %s (type %s, material %s). Real name: %s.'%(x['id'],x['type'],x['material'],x['id'].replace('_',' '))) for x in h],700)
g=json.load(open('bp_guns.json'))
w('gear_spec_guns',GUN,[(b['id'],'Gun id: %s. Real name: %s.'%(b['id'],b['id'].replace('_',' '))) for b in g],500)
e=[x for x in json.load(open('everyday_in.json'))['items']]
eb={b['id']:b for b in json.load(open('bp_everyday.json'))}
ee=[x for x in e if 'ev_'+x['id'] in eb]
w('gear_spec_tools',TOOL,[('ev_'+x['id'],'Item id: %s, category %s. Parts: %s. Name: %s.'%(x['id'],x['category'],','.join(s['name'] for s in eb['ev_'+x['id']]['slots']),x['id'].replace('_',' '))) for x in ee],500)
