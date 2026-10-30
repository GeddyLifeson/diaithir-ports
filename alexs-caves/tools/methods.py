#!/usr/bin/env python3
"""Brace-aware helpers: remove methods by name from java files."""
import re, sys, pathlib

def find_method_spans(src, names):
    spans=[]
    for m in re.finditer(r'(?m)^[ \t]*((?:@[\w.]+(?:\([^)]*\))?[ \t]*\n[ \t]*)*)((?:public|protected|private|static|final|default|synchronized|abstract)\s+)*[\w<>\[\],.? ]+\s+(%s)\s*\(' % '|'.join(names), src):
        start=m.start()
        # find the opening brace or semicolon
        i=m.end(); depth=1
        while i<len(src) and depth>0:
            c=src[i]
            if c=='(': depth+=1
            elif c==')': depth-=1
            i+=1
        j=i
        while j<len(src) and src[j] not in '{;': j+=1
        if src[j]==';':
            spans.append((start,j+1)); continue
        depth=0; k=j
        while k<len(src):
            c=src[k]
            if c=='{': depth+=1
            elif c=='}':
                depth-=1
                if depth==0: break
            k+=1
        spans.append((start,k+1))
    # drop spans nested inside an earlier span
    spans.sort()
    out=[]
    for a,b in spans:
        if out and a<out[-1][1]: continue
        out.append((a,b))
    return out

def remove_methods(path, names):
    s=path.read_text()
    spans=find_method_spans(s,names)
    if not spans: return 0
    for a,b in sorted(spans,reverse=True):
        # drop preceding @Override/@SuppressWarnings lines included by regex; also eat trailing newline
        e=b
        while e<len(s) and s[e] in ' \t': e+=1
        if e<len(s) and s[e]=='\n': e+=1
        s=s[:a]+s[e:]
    path.write_text(s)
    return len(spans)

if __name__=='__main__':
    root=pathlib.Path(sys.argv[1]); names=sys.argv[2].split(',')
    n=0
    for p in root.rglob('*.java'):
        n+=remove_methods(p,names)
    print(n,'methods removed')
