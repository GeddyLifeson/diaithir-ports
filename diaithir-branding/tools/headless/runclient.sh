#!/bin/bash
# Headless NeoForge dev client: Xvfb :99 (1280x720), runClient from the project dir, a screenshot every 3 s into ~/shots.
#   JAVA_HOME=<jdk25> RUNSECS=500 tools/headless/runclient.sh [project dir]    (game log: ~/client.log)
# Drive it with: DISPLAY=:99 python3 tools/headless/x.py c<x>,<y> k<keysym> t<text> w<secs>   (window sits at 213,120, 854x480)
# Stop it with tools/headless/killclient.sh, never `pkill -f` a pattern in your own command line.
export PATH=$JAVA_HOME/bin:$PATH LIBGL_ALWAYS_SOFTWARE=1
Xvfb :99 -screen 0 1280x720x24 >/dev/null 2>&1 &
XV=$!
export DISPLAY=:99
cd "${1:-.}"
mkdir -p ~/shots
( for i in $(seq 1 200); do sleep 3; import -window root ~/shots/s$(printf %03d $i).png 2>/dev/null; done ) &
SH=$!
timeout ${RUNSECS:-500} bash ./gradlew runClient -Dorg.gradle.java.installations.paths=$JAVA_HOME > ~/client.log 2>&1
kill $SH $XV 2>/dev/null
