"""Parsers for the Goety 1.20 upstream tree: block, item and entity registrations."""
import re, os

def read(up, rel):
    return open(os.path.join(up, "src/main/java/com/Polarice3/Goety", rel), encoding="utf8").read()

def blocks(up):
    t = read(up, "common/blocks/ModBlocks.java")
    rows = []
    for m in re.finditer(r'RegistryObject<([^>]+)>\s+(\w+)\s*=\s*(\w+)\("([^"]+)"(.*?)\);\n', t, re.S):
        ty, field, fn, id_, args = m.groups()
        rows.append(dict(type=ty, field=field, fn=fn, id=id_, args=args.strip()))
    return rows

def items(up):
    t = read(up, "common/items/ModItems.java")
    rows = []
    for m in re.finditer(r'RegistryObject<([^>]+)>\s+(\w+)\s*=\s*ITEMS\.register\("([^"]+)",\s*(.*?)\);\n', t, re.S):
        ty, field, id_, body = m.groups()
        k = re.match(r'(?:\(\)\s*->\s*)?new\s+(\w+)|(\w+)::new', body.strip())
        rows.append(dict(type=ty, field=field, id=id_, cls=(k.group(1) or k.group(2)) if k else "?", body=body.strip()))
    return rows

def lang(up, name="en_us"):
    import json
    return json.load(open(os.path.join(up, f"src/main/resources/assets/goety/lang/{name}.json"), encoding="utf8"))
