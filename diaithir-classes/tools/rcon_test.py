#!/usr/bin/env python3
"""rcon smoke test (MIT): summon every item, equip an armor stand with armour and weapons, read back sockets.
Usage: python3 -I tools/rcon_test.py PORT PASSWORD   (server running with diaithir_classes, no player needed)"""
import json
import os
import socket
import struct
import sys

root = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
data = json.load(open(os.path.join(root, "src/main/resources/data/diaithir_classes/classes.json")))
s = socket.create_connection(("127.0.0.1", int(sys.argv[1])))


def pkt(i, t, body):
    d = struct.pack("<ii", i, t) + body.encode() + b"\0\0"
    s.sendall(struct.pack("<i", len(d)) + d)


def rd():
    n = struct.unpack("<i", s.recv(4))[0]
    d = b""
    while len(d) < n:
        d += s.recv(n - len(d))
    return d[8:-2].decode(errors="replace")


pkt(1, 3, sys.argv[2]); rd()


def cmd(c):
    pkt(2, 2, c)
    return rd()


ids = [i["id"] for i in data["items"]] + data["parts"] + ["rune_" + r["id"] for r in data["runes"]] + [r["id"] for r in data["rings"]]
ok = bad = 0
for iid in ids:
    r = cmd(f'summon item 0 100 0 {{Item:{{id:"diaithir_classes:{iid}",count:1}}}}')
    if r.startswith("Summoned"):
        ok += 1
    else:
        bad += 1
        print("BAD summon", iid, r)
print("summoned", ok, "bad", bad, "of", len(ids))
cmd("forceload add 0 0")
import time
time.sleep(3)
cmd("kill @e[type=item]")
cmd('summon armor_stand 0 100 0 {Tags:["ct"],NoGravity:1b}')
slots = {"helmet": "armor.head", "chestplate": "armor.chest", "leggings": "armor.legs", "boots": "armor.feet"}
rep = bad2 = 0
for iid in ids:
    spec = next((i for i in data["items"] if i["id"] == iid), None)
    if spec and spec["kind"] == "armor":
        slot = slots[spec["slot"]]
    else:
        slot = "weapon.mainhand"
    r = cmd(f'item replace entity @e[tag=ct,limit=1] {slot} with diaithir_classes:{iid}')
    if "Replaced" in r:
        rep += 1
    else:
        bad2 += 1
        print("BAD replace", iid, r)
print("replaced", rep, "bad", bad2)
r = cmd('item replace entity @e[tag=ct,limit=1] weapon.mainhand with diaithir_classes:ring_composite[diaithir_classes:sockets=["beith","dair","coll"]]')
print(r)
print(cmd('data get entity @e[tag=ct,limit=1] equipment.mainhand')[:300])
cmd("kill @e[tag=ct]")
