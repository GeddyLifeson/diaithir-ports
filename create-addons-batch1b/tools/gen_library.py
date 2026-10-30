import json,sys
sys.path.insert(0,'/mnt/project-files/diaithiris-archaic/tools')
from archaic import word
J='/mnt/project-files/compute/jobs/'
names={}
for j in ('create_batch1b_names','create_batch1b_names2'):
    for l in open(J+j+'/out.jsonl',encoding='utf-8',errors='replace'):
        r=json.loads(l)
        try: names[r['id']]=json.loads(r['text'])['classical']
        except Exception: pass
names['biodiesel']='bheoconnadhola'
def A(n): return word(n.lower()).capitalize()
rows=[
('brake','machine','A brake for Create shafts. Redstone power locks the shaft and burns off its speed as heat; it stops machinery, it makes nothing.'),
('brass_gearbox','machine','A brass gearbox that joins shafts on every side, with extra outputs. Brass casing only, so it is Age IV.'),
('parallel_gearbox','machine','A gearbox that carries two parallel shaft lines through one block, one over the other.'),
('six_way_gearbox','machine','A gearbox that passes rotation between all six faces, for dense rotation hubs.'),
('inverted_clutch','machine','A clutch that drives while unpowered and disengages on a redstone signal. Fail-safe lines stop when a circuit fires.'),
('inverted_gearshift','machine','A gearshift that reverses rotation only while unpowered; the opposite of the ordinary gearshift.'),
('cross_connector','machine','A connector that forwards rotation between small cogwheels placed diagonally across it.'),
('overstress_clutch','machine','A clutch that slips open when the network is overstressed and closes again once the load falls.'),
('freewheel_clutch','machine','A one-way clutch. It turns the output when driven forward and spins free when driven back.'),
('centrifugal_clutch','machine','A clutch that engages once the shaft passes a set speed. Set the threshold with the scroll.'),
('shear_pin','machine','A shaft that gives way first. When the network is overstressed the pin breaks and the load is dropped, instead of wrecking the machines.'),
('modular_diesel_engine','machine','A diesel engine built from segments set in a line. Each segment adds stress; all share one tank, and the whole line runs on the same fuel.'),
('huge_diesel_engine','machine','A very large engine for the heaviest loads. Four buckets of fuel in the tank, high speed, and a great deal of stress.'),
('burner','machine','A fuel burner that heats basins like a blaze burner. Turn its valve or give it a redstone signal; it burns the liquid fuel piped into it.'),
('canister','machine','A portable fuel can, and a tank when set down. It keeps what is in it when picked up again.'),
('distillation_tower','machine','Stack three over a burner and feed crude oil into the bottom. Diesel collects in the second segment and gasoline in the third.'),
('pumpjack','machine','Draws crude oil from the pool under its chunk when turned. About one chunk in three holds oil; each pool runs dry in the end.'),
('engine_silencer','machine','Bolts onto a diesel engine to quieten it. It changes the sound and nothing else.'),
('engine_turbocharger','machine','Bolts onto a diesel engine to raise its speed and its stress by half again.'),
('diesel_engine','machine','Burns liquid fuel and turns a shaft. Fuel quality sets speed and stress; a redstone signal throttles it. It runs hot, so it warms the room around it.'),
('crude_oil','fluid','Raw oil from oil springs, black and thick. Sold in buckets. It is distilled into fuels and not burned as it comes.'),
('diesel','fluid','A middle-weight fuel with steady, high power. The engine burns it by the millibucket.'),
('gasoline','fluid','A light, fast fuel. It gives a high-speed engine at a lower stress capacity than diesel.'),
('biodiesel','fluid','Fuel pressed from plant oil. It burns weaker than diesel but needs no oil spring.'),
('ethanol','fluid','Spirit fuel fermented from crops. Weak in the engine, but any farm can make it.'),
('plant_oil','fluid','Oil pressed from seeds. Feeds the biodiesel chain and burns poorly on its own.'),
]
out=[]
for k,c,t in rows:
    n=names.get(k)
    out.append([A(n) if n else k.replace('_',' ').title(),'item',4,'create',t])
json.dump(out,open('/mnt/project-files/library-sync/create_batch1b.json','w'),ensure_ascii=False,indent=1)
print(len(out),[r[0] for r in out])
