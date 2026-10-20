#!/usr/bin/env python3
"""Writes data/diaithir/earth_ranges/untamedwilds_*.json.

Region boxes reproduce region() of DiaithirRPG tools/earth/age1_layers.py as unions of lat/lon rectangles
(check: python3 gen_earth_ranges.py --check <path to age1_layers.py>). Species lists come from AGE_I_FAUNA.csv regions
(Age I, 14.8-9.5 ka) plus today's native range for species that also lived in Age I. Sources: UW_SPECIES.csv and the
species pages cited there. Species not listed here (fish, sharks, whales, invertebrates, amphibians, most reptiles and
species extinct before Age I) have no entry and spawn by biome only.
"""
import json, os, sys

REG = {  # lat [min,max], lon [min,max]; a partition of the globe that matches region()
 "N America": [([13, 90], [-180, -30])],
 "C America": [([7, 13], [-180, -30])],
 "S America": [([-90, 7], [-180, -30])],
 "Sahul": [([-90, -10], [110, 180])],
 "SE Asia/Sunda": [([-10, 25], [95, 180]), ([-90, -10], [95, 110])],
 "N Asia": [([50, 90], [60, 180])],
 "E/S Asia": [([25, 50], [60, 180]), ([-10, 25], [60, 95]), ([-90, -10], [60, 95])],
 "Africa": [([-90, 28], [-30, 52]), ([28, 35], [-30, 34])],
 "SW Asia": [([35, 45], [26, 60]), ([28, 35], [34, 60]), ([-90, 28], [52, 60])],
 "Europe": [([45, 90], [-30, 60]), ([35, 45], [-30, 26])],
}
EXTRA = {  # finer boxes where a whole region would be too coarse
 "Arctic": [([55, 90], [-180, 180])],
 "N Pacific": [([50, 65], [160, 180]), ([50, 65], [-180, -120])],
 "Giant panda range": [([22, 36], [98, 112])],
 "Wild Bactrian range": [([36, 48], [85, 110])],
 "W Africa coast": [([-15, 20], [-20, 15])],
 "Amazon basin": [([-20, 8], [-80, -45])],
 "Indo-Pacific coast": [([-30, 30], [30, 160])],
 "W Atlantic tropics": [([5, 40], [-100, -30])],
 "S Mexico": [([13, 22], [-100, -85])],
 "N Africa coast": [([28, 37], [-10, 35])],
 "Africa south of the Sahara": [([-40, 15], [-20, 52])],
}
R = dict(REG, **EXTRA)

# type: [(species, [region names], note)]
S = {
 "bear": [
  ("black", ["N America"], "CSV Ursus americanus: N America"),
  ("brown", ["N America", "Europe", "N Asia", "SW Asia", "E/S Asia"], "CSV Ursus arctos: N America, Europe, N Asia; modern range adds SW and E/S Asia"),
  ("polar", ["Arctic"], "Ursus maritimus, Arctic"),
  ("panda", ["Giant panda range"], "Ailuropoda melanoleuca, modern range"),
  ("spectacled", ["S America"], "Tremarctos ornatus, Andes"),
  ("sun", ["SE Asia/Sunda", "E/S Asia"], "Helarctos malayanus, modern range"),
  ("short_faced", ["N America"], "CSV Arctodus simus: N America"),
  ("florida_spectacled", ["N America"], "CSV Tremarctos floridanus: N America"),
 ],
 "big_cat": [
  ("cave_lion", ["Europe", "N Asia", "N America"], "CSV Panthera spelaea: Europe, N Asia, N America"),
  ("jaguar", ["N America", "C America", "S America"], "CSV Panthera onca: N America; modern range adds C and S America"),
  ("leopard", ["Africa", "SW Asia", "E/S Asia", "SE Asia/Sunda"], "Panthera pardus, modern range"),
  ("lion", ["Africa", "SW Asia", "E/S Asia"], "Panthera leo, modern and Holocene range"),
  ("puma", ["N America", "C America", "S America"], "CSV Puma concolor: N America; modern range adds C and S America"),
  ("sabertooth", ["S America"], "Smilodon populator, South America"),
  ("snow_leopard", ["E/S Asia", "N Asia"], "Panthera uncia, modern range"),
  ("tiger", ["E/S Asia", "SE Asia/Sunda", "SW Asia"], "Panthera tigris, modern and Holocene range"),
  ("american_lion", ["N America"], "CSV Panthera atrox / P. leo atrox: N America"),
  ("smilodon_fatalis", ["N America"], "CSV Smilodon fatalis: N America"),
  ("scimitar_cat", ["N America"], "CSV Homotherium serum: N America"),
 ],
 "bison": [
  ("plains", ["N America"], "CSV Bison bison: N America"),
  ("wisent", ["Europe", "SW Asia", "N Asia"], "CSV Bison bonasus: Europe, N Asia, SW Asia"),
  ("steppe", ["N America", "N Asia"], "CSV Bison priscus: N America, N Asia"),
  ("ancient", ["N America"], "CSV Bison antiquus: N America"),
  ("long_horned", ["N America"], "CSV Bison latifrons: N America"),
 ],
 "boar": [
  ("eurasian", ["Europe", "SW Asia", "E/S Asia", "SE Asia/Sunda", "N Africa coast"], "CSV Sus scrofa: Europe, SW Asia; modern native range adds E/S Asia, Sunda, N Africa"),
  ("peccary", ["C America", "S America", "S Mexico"], "Tayassu pecari, modern range"),
  ("red_river", ["Africa south of the Sahara"], "Potamochoerus porcus, modern range"),
  ("warthog", ["Africa south of the Sahara"], "Phacochoerus africanus, modern range"),
  ("flat_headed", ["N America"], "CSV Platygonus compressus: N America"),
  ("long_nosed", ["N America"], "CSV Mylohyus fossilis: N America"),
 ],
 "camel": [
  ("wild_bactrian", ["Wild Bactrian range"], "Camelus ferus, modern range"),
  ("western", ["N America"], "CSV Camelops hesternus: N America"),
 ],
 "hyena": [
  ("aardwolf", ["Africa south of the Sahara"], "Proteles cristata, modern range"),
  ("brown", ["Africa south of the Sahara"], "Parahyaena brunnea, modern range"),
  ("spotted", ["Africa south of the Sahara"], "Crocuta crocuta, Africa only in Age I"),
  ("striped", ["Africa", "SW Asia", "E/S Asia"], "Hyaena hyaena, modern range"),
 ],
 "hippo": [("common", ["Africa south of the Sahara"], "Hippopotamus amphibius, modern range")],
 "manatee": [
  ("african", ["W Africa coast"], "Trichechus senegalensis"),
  ("amazon", ["Amazon basin"], "Trichechus inunguis"),
  ("dugong", ["Indo-Pacific coast"], "Dugong dugon, modern range"),
  ("steller", ["N Pacific"], "Hydrodamalis gigas, North Pacific"),
  ("west", ["W Atlantic tropics"], "Trichechus manatus, modern range"),
 ],
 "rhino": [
  ("wooly", ["N Asia", "Europe"], "CSV Coelodonta antiquitatis: N Asia; Eurasian steppe"),
  ("black", ["Africa south of the Sahara"], "Diceros bicornis, modern range"),
  ("white", ["Africa south of the Sahara"], "Ceratotherium simum, modern range"),
  ("indian", ["E/S Asia"], "Rhinoceros unicornis, modern range"),
  ("javan", ["SE Asia/Sunda", "E/S Asia"], "Rhinoceros sondaicus, modern range"),
  ("sumatran", ["SE Asia/Sunda"], "Dicerorhinus sumatrensis, modern range"),
 ],
 "opossum": [
  ("virginia", ["N America"], "CSV Didelphis virginiana: N America"),
  ("common", ["N America", "C America", "S America"], "CSV Didelphis marsupialis: N America; modern range to Bolivia"),
 ],
 "aardvark": [("common", ["Africa south of the Sahara"], "Orycteropus afer, modern range")],
}

def boxes(names):
    out = []
    for n in names:
        for lat, lon in R[n]:
            out.append({"lat": lat, "lon": lon})
    return out

def region(la, lo):  # copy of age1_layers.region() for --check
    if lo < -30: return "N America" if la > 13 else "C America" if la > 7 else "S America"
    if la < -10 and lo > 110: return "Sahul"
    if lo > 95 and la < 25: return "SE Asia/Sunda"
    if lo > 60: return "N Asia" if la > 50 else "E/S Asia"
    if la < 35 and lo < 52 and not (la > 28 and lo > 34): return "Africa"
    return "SW Asia" if lo > 26 and la < 45 else "Europe"

def check():
    bad = 0
    for la in range(-89, 90):
        for lo in range(-179, 180):
            hit = [n for n in REG if any(a[0] <= la + .5 <= a[1] and b[0] <= lo + .5 <= b[1] for a, b in REG[n])]
            if region(la + .5, lo + .5) not in hit:
                bad += 1
    print("cells whose region() box is missing:", bad)
    return bad

if __name__ == "__main__":
    if "--check" in sys.argv:
        sys.exit(1 if check() else 0)
    out = os.path.join(os.path.dirname(__file__), "..", "src", "main", "resources", "data", "diaithir", "earth_ranges")
    os.makedirs(out, exist_ok=True)
    for t, lst in S.items():
        d = {"_comment": "Natural spawns of these untamedwilds:%s species are limited to the boxes (lat, lon degrees). Generated by tools/gen_earth_ranges.py" % t,
             "ranges": [{"entity": "untamedwilds:" + t, "species": sp, "regions": names, "note": note, "boxes": boxes(names)} for sp, names, note in lst]}
        with open(os.path.join(out, "untamedwilds_%s.json" % t), "w") as f:
            json.dump(d, f, indent=1); f.write("\n")
    print("wrote", len(S), "files")
