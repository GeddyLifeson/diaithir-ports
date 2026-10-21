#!/bin/bash
for p in $(pgrep -f "devlaunch|runclient|gradlew runClient"); do [ "$p" != "$$" ] && kill $p 2>/dev/null; done
pkill -x Xvfb
exit 0
