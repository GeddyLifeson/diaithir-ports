#!/bin/bash
# startup.sh OUT: boot the server once with a JFR recording, stop right after Done
OUT=$1; mkdir -p $OUT; cd ~/perf/srv; export PATH=$HOME/perf/jdk-25.0.4.1+1/bin:$PATH
IN=$OUT/in; rm -f $IN; mkfifo $IN; LOG=$OUT/server.log
( tail -f $IN | java -Xms6G -Xmx6G -XX:StartFlightRecording=settings=profile,filename=$OUT/startup.jfr @libraries/net/neoforged/neoforge/26.1.2.116/unix_args.txt nogui > $LOG 2>&1 ) &
until grep -qE 'took [0-9.]+ seconds to load' $LOG; do sleep 2; done
echo stop > $IN; sleep 40; pkill -f "tail -f $IN"
grep -oE 'took [0-9.]+ seconds to load|Initial datapack load took [0-9.]+ min' $LOG
