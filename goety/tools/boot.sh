#!/bin/bash
# Boots a dedicated server in run/ (gradle runServer), waits for Done, leaves it running. Log: run/gradle.log
cd "$(dirname "$0")/.."
export JAVA_HOME=/home/user/srv/jdk PATH=/home/user/srv/jdk/bin:$PATH
rm -f run/world/session.lock
(setsid ./gradlew runServer -Porg.gradle.java.installations.paths=/home/user/srv/jdk --offline > run/gradle.log 2>&1 &)
for i in $(seq 1 120); do sleep 3; grep -q 'Done (' run/gradle.log && { echo up; exit 0; }; grep -q 'BUILD FAILED' run/gradle.log && { echo failed; exit 1; }; done
echo timeout; exit 2
