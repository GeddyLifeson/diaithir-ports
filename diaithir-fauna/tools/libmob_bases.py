#!/usr/bin/env python3
# S6879 Writes library_mob_bases.tsv: Library mob entries that exist as a mod creature (exact match) spawn as that creature.
"""Usage: libmob_bases.py [MATCH_TSV]. Source: /mnt/project-files/mobs-names/MATCH_PANSCRIPTUM.tsv (status exact). Row: entry id, entity id."""
import csv, os, sys
M = sys.argv[1] if len(sys.argv) > 1 else '/mnt/project-files/mobs-names/MATCH_PANSCRIPTUM.tsv'
OUT = os.path.join(os.path.dirname(os.path.abspath(__file__)), '..', 'src/main/resources/data/diaithirfauna/library_mob_bases.tsv')
NS = {'alexsmobs': 'alexsmobs', 'friendsandfoes': 'friendsandfoes'}
rows = [r for r in csv.DictReader(open(M), delimiter='\t') if r['status'] == 'exact' and ';' not in r['panscriptum_ids']]
with open(OUT, 'w') as f:
    for r in rows: f.write(f"{r['panscriptum_ids']}\t{NS[r['mod']]}:{r['entity']}\n")
print(len(rows), 'bases')
