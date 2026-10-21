#!/bin/bash
# clientworld.sh OUT: in-world client run with the current diaithir-perf build on a fresh copy of the bench world
cp /home/user/diaithir-ports/diaithir-perf/build/libs/diaithir-perf-neoforge-26.1.2-0.1.0.jar ~/perf/cli/mods/
rm -rf ~/perf/cli/saves/world; cp -r ~/perf/world_master ~/perf/cli/saves/world; rm -f ~/perf/cli/logs/latest.log
JOIN=1 ~/perf/bin/client.sh $1
