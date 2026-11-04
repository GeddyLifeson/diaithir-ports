# Colony picker data: one profile per planet from specs (real entries). python3 -I colony_profiles.py -> ports resource planet_colony.json
import json,glob,os,csv
O='/mnt/project-files/planets'; OUT='/home/user/diaithir-ports/diaithir-overworld-biomes/src/main/resources/data/diaithir_planets/planet_colony.json'
P={p['planet_id']:p for p in json.load(open(O+'/planets.json'))}
sett={}
for l in open(O+'/settlements_index.tsv').read().split('\n')[1:]:
    if l.strip(): a,b=l.split('\t'); sett[a]=int(b)
out={}
for pid,p in P.items():
    f=f'{O}/specs/{pid}.json'
    if pid=='diaithir' or not os.path.exists(f): continue
    s=json.load(open(f)); at=s.get('atmo') or {}; k=s.get('kit') or {}; fl=s.get('flora') or {}
    haz=[]
    w=(s.get('weather') or {}).get('kind')
    if w=='ash' or at.get('particles') and any(x['type'] in('ash','white_ash') for x in at['particles']): haz.append('ash_fall')
    if at.get('storms')=='stormy': haz.append('storms')
    if at.get('precip_bias')=='dry': haz.append('drought')
    if at.get('precip_bias')=='snow': haz.append('freezing')
    liq=(s.get('sea') or {}).get('liquid','water')
    if liq=='lava': haz.append('lava_sea')
    if liq=='none': haz.append('no_sea')
    if s.get('terrain')=='amplified': haz.append('extreme_terrain')
    if at.get('night')=='dark': haz.append('dark_nights')
    ores=sorted(((v,o) for o,v in (k.get('ores') or {}).items()),reverse=True)
    out[pid]={"name":p['name'],"soil":p['soil'],"realm":p['realm'],"opens_age":p['opens_age'],"entries":p['entries'],
      "settlements":sett.get(pid,0),"terrain":s.get('terrain','normal'),"sea":liq,"weather":w or 'rain',"hazards":haz,
      "rich_in":[o for v,o in ores if v>=1.0][:3],"poor_in":[o for v,o in ores[::-1] if v<=0.6][:3],
      "wood":(k.get('trees') or [{}])[0].get('log'),"density":k.get('density'),
      "named":{x:fl[x]['name'] for x in ('tree','plant','ore') if x in fl},"notes":s.get('notes','')[:240]}
os.makedirs(os.path.dirname(OUT),exist_ok=True); json.dump(out,open(OUT,'w'),separators=(',',':'))
print(len(out),'profiles',sum(1 for v in out.values() if v['hazards']),'with hazards')
