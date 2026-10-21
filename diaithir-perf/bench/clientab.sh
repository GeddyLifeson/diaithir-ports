#!/bin/bash
# In-world client A/B: same world copy (no forced chunks), without and with diaithir-perf
JAR=/home/user/diaithir-ports/diaithir-perf/build/libs/diaithir-perf-neoforge-26.1.2-0.1.0.jar
run(){ rm -rf ~/perf/cli/saves/world; cp -r ~/perf/world_master ~/perf/cli/saves/world; rm -f ~/perf/cli/logs/latest.log; JOIN=1 ~/perf/bin/client.sh $1; }
rm -f ~/perf/cli/mods/diaithir-perf-*.jar; run ~/perf/out/world_base
cp $JAR ~/perf/cli/mods/; run ~/perf/out/world_perf
