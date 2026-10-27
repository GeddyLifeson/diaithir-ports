import sys
for l in open(sys.argv[1]):
    p=l.split()
    if len(p)<3 or p[0] not in('accessible','extendable','mutable'): continue
    cls=p[2].replace('/','.')
    mod={'accessible':'public','extendable':'public-f','mutable':'public-f'}[p[0]]
    if p[1]=='class': print(f'{mod} {cls}')
    elif p[1]=='field': print(f'{mod} {cls} {p[3]}')
    else: print(f'{mod} {cls} {p[3]}{p[4]}')
