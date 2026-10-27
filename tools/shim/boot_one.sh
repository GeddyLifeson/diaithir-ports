#!/bin/bash
# Usage: boot_one.sh <srv dir> <jdk dir> <port jar> <remapped addon jar...>  -> prints RESULT line; log in $SRV/logs/boot_<name>.log
# Only kills the process group it started. Uses its own server port so parallel agents do not collide.
SRV=$1; JDK=$2; PORT=$3; shift 3
NAME=$(basename "$1" .jar)
rm -rf "$SRV/mods" "$SRV/world"; mkdir -p "$SRV/mods" "$SRV/logs"
cp "$PORT" "$SRV/mods/"; cp /home/user/work-shim/out/lib-*.jar "$SRV/mods/" 2>/dev/null; true
cp "$PORT" "$SRV/mods/"; for j in "$@"; do cp "$j" "$SRV/mods/"; done
cd "$SRV"; echo "eula=true" > eula.txt
sed -i '/^server-port=/d;/^online-mode=/d' server.properties; printf 'server-port=25791\nonline-mode=false\nlevel-type=flat\nview-distance=4\n' >> server.properties
LOG=$SRV/logs/boot_$NAME.log
setsid bash -c "(sleep 170; echo stop) | timeout 200 '$JDK/bin/java' @user_jvm_args.txt @libraries/net/neoforged/neoforge/26.1.2.109/unix_args.txt nogui > '$LOG' 2>&1" &
PG=$!
for i in $(seq 1 200); do
  sleep 1
  if grep -q -E 'Done \(' "$LOG"; then echo "RESULT $NAME LOADS"; kill -- -$PG 2>/dev/null; pkill -g $PG 2>/dev/null; exit 0; fi
  if ! kill -0 $PG 2>/dev/null; then break; fi
  if grep -q -E 'Failed to load|Exception in thread|Crash report|Encountered an unexpected exception|ModLoadingException|FML.*ERROR' "$LOG" && [ $i -gt 20 ]; then sleep 3; break; fi
done
kill -- -$PG 2>/dev/null; pkill -g $PG 2>/dev/null
echo "RESULT $NAME FAILS"
grep -m4 -E 'ERROR|Exception|Caused by|Missing|missing|requires' "$LOG" | cut -c1-260
