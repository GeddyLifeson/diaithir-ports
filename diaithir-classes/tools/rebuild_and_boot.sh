#!/bin/bash
# Rebuild the jar, copy it to the test server (/home/user/srv-classes), restart it, wait for Done.
cd "$(dirname "$0")/.."
export JAVA_HOME=/home/user/srv/jdk
./gradlew --no-daemon -q -Porg.gradle.java.installations.paths=/home/user/srv/jdk build 2>&1 | grep -i "error\|FAIL"
cp build/libs/*.jar /home/user/srv-classes/mods/
python3 -I /home/user/wt/arms/diaithir-arms/tools/rcon.py 25632 x stop >/dev/null 2>&1
sleep 8
cd /home/user/srv-classes && rm -rf world && (setsid nohup ./start.sh >/dev/null 2>&1 &)
for i in $(seq 1 60); do sleep 3; grep -q 'Done (' /home/user/srv-classes/server.log 2>/dev/null && break; done
grep -c 'Done (' /home/user/srv-classes/server.log
