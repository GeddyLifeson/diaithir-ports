#!/bin/bash
# compact javac error summary: file:line message
sed -i 's|/home/user/work-1a/addons/[a-z]*/src/main/java/||' /tmp/build_$1.log
grep -E "error:" /tmp/build_$1.log | sed 's/ error: / /' | sort -u | head -${2:-120}
