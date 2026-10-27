#!/bin/bash
# one iteration: refresh adapter jar, remap, stubs, boot addon $1 (substring of jar name); print first failure.
cd /home/user/work-shim; cp shim-adapter/build/libs/create-shim-adapter-0.1.0.jar out/lib-shim-adapter.jar
tools/shim/run_all.sh >/dev/null; python3 tools/shim/make_stubs.py tools/shim/map.json out/lib-stubs.jar >/dev/null
J=$(ls out/create*$1*.jar | head -1); cd srvtest
timeout 200 bash ../tools/shim/boot_one.sh $PWD /home/user/srv/jdk-25.0.5+8 /mnt/project-files/create-neoforge-26.1.2-6.0.9-diaithir.8.jar ../$J 2>&1 | head -1
L=logs/boot_$(basename $J .jar).log
grep -m1 -A6 -E '^java.lang.[A-Za-z]*(Error|Exception)|Exception message|requires' $L | cut -c1-240
