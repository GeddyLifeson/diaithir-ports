#!/bin/bash
# bench.sh SRV_DIR OUT_DIR : boot server, measure startup, retained heap, forceload worldgen throughput, spark profile
set -u
SRV=$1 OUT=$2; mkdir -p "$OUT"; cd "$SRV"; : > $OUT/result.txt
export PATH=$HOME/perf/jdk-25.0.4.1+1/bin:$PATH
histo(){ jcmd $PID GC.class_histogram 2>/dev/null > $OUT/histo_$1.txt; awk -v t=$1 '$4 ~ /datafixers/ {s+=$3} END{printf "dfu_retained_%s_MB %.0f\n", t, s/1048576}' $OUT/histo_$1.txt >> $OUT/result.txt; }
rm -rf world crash-reports logs/latest.log; IN=$OUT/in; rm -f $IN; mkfifo $IN
LOG=$OUT/server.log
( tail -f $IN | java -Xms6G -Xmx6G -XX:+UseG1GC -Xlog:gc:file=$OUT/gc.log @libraries/net/neoforged/neoforge/26.1.2.116/unix_args.txt nogui > $LOG 2>&1 ) &
t0=$(date +%s)
cmd(){ echo "$*" > $IN; }
until grep -qE 'Done \([0-9.]+s\)!' $LOG; do sleep 1; grep -qE '^---- Minecraft Crash Report|Mod loading has failed' $LOG && { echo CRASH; break; }; [ $(( $(date +%s)-t0 )) -gt 900 ] && break; done
echo "startup_s $(grep -oE 'Done \([0-9.]+s\)' $LOG)" >> $OUT/result.txt
sleep 20; PID=$(pgrep -f '^java -Xms6G' | head -1)
jcmd $PID GC.run >/dev/null; sleep 3; jcmd $PID GC.heap_info > $OUT/heap_idle.txt; histo idle
cmd "spark profiler start --thread *"
t1=$(date +%s)
for a in "-256 -256 -1 -1" "0 -256 255 -1" "-256 0 -1 255" "0 0 255 255"; do cmd "forceload add $a"; done
until [ $(grep -c 'chunks in Overworld from' $LOG) -ge 4 ] || [ $(( $(date +%s)-t1 )) -gt 1500 ]; do sleep 1; done
echo "forceload_1024_chunks_s $(( $(date +%s)-t1 ))" >> $OUT/result.txt
sleep 60; cmd "spark tps"; sleep 5
jcmd $PID GC.run >/dev/null; sleep 3; jcmd $PID GC.heap_info > $OUT/heap_loaded.txt; histo loaded
cmd "spark profiler stop --save-to-file"; sleep 15
cmd stop; sleep 30; pkill -f "tail -f $IN"
for f in idle loaded; do echo "heap_${f}_used $(grep -oE 'used [0-9]+K' $OUT/heap_$f.txt | head -1)" >> $OUT/result.txt; done
grep -A6 -E 'TPS from last|Tick durations' $LOG | tail -12 >> $OUT/result.txt
find config/spark world -name '*.sparkprofile' -newer $IN -exec cp {} $OUT/ \; 2>/dev/null
echo "load_s $(grep -oE "took [0-9.]+ seconds to load" $LOG)" >> $OUT/result.txt
cat $OUT/result.txt
