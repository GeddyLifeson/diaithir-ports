#!/bin/bash
# boots the server, runs smoke.py (summon every item/entity + /goetyselftest), prints a summary, stops the server
cd "$(dirname "$0")/.."
tools/boot.sh > run/boot.out 2>&1
cat run/boot.out
python3 -I tools/smoke.py 25642 > run/smoke.out; grep -v "^loot.*\(Dropped\|Spawned\)" run/smoke.out | tail -${2:-30}
python3 -I tools/rcon.py 25642 stop > /dev/null 2>&1
sleep 5
echo "log errors: $(grep -c '/ERROR\]\|Exception' run/gradle.log)"; grep '/ERROR\]\|Exception\|goetyselftest' run/gradle.log | head -${1:-12} | cut -c1-250
