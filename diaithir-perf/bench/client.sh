#!/bin/bash
# client.sh OUT: boot the client to the title screen under Xvfb (software GL), JFR allocation profile, histogram, then quit
OUT=$1; mkdir -p $OUT; J=$HOME/perf/jdk-25.0.4.1+1/bin
export JDK_JAVA_OPTIONS="-XX:StartFlightRecording=settings=profile,filename=$OUT/client-%p.jfr,path-to-gc-roots=true"
export LIBGL_ALWAYS_SOFTWARE=1 GALLIUM_DRIVER=llvmpipe
xvfb-run -a -s "-screen 0 1280x720x24" timeout 1500 portablemc --main-dir /root/mc start --mc-dir /root/perf/cli neoforge::26.1.2.116 -u Tester ${JOIN:+--join-world world} \
  --jvm $J/java --jvm-arg=-Xmx8G --jvm-arg=-Xms2G \
  > $OUT/client_out.log 2>&1 &
t0=$(date +%s)
WAITFOR='Sound engine started'; [ -n "$JOIN" ] && WAITFOR='joined the game|logged in with entity'
until grep -qE "$WAITFOR" /root/perf/cli/logs/latest.log 2>/dev/null && [ $(( $(date +%s)-t0 )) -gt 60 ]; do sleep 5; [ $(( $(date +%s)-t0 )) -gt 1400 ] && break; done
[ -n "$JOIN" ] && sleep 120; sleep 60; PID=$(pgrep -n -f 'jdk-25.*/java .*Tester')
echo "ready_s $(( $(date +%s)-t0 - 60 ))" > $OUT/result.txt
$J/jcmd $PID GC.run >/dev/null 2>&1; sleep 5
$J/jcmd $PID GC.heap_info 2>/dev/null | grep -oE 'used [0-9]+K' | head -1 | sed 's/^/heap_/' >> $OUT/result.txt
$J/jcmd $PID GC.class_histogram 2>/dev/null > $OUT/histo_title.txt
$J/jcmd $PID JFR.dump name=1 filename=$OUT/client-$PID.jfr path-to-gc-roots=true >/dev/null 2>&1
kill $PID; sleep 10
cat $OUT/result.txt
