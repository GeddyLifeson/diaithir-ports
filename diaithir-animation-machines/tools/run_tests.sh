#!/bin/sh
# S-ID registered in Global-Repo. Needs only a JDK 17+; no Gradle, no network.
set -e
cd "$(dirname "$0")/.."
out=$(mktemp -d)
D=src/main/resources/data/diaithir_animation; cat $D/rules_bones.tsv $D/rules.tsv > "$out/all.tsv"
javac -d "$out" src/main/java/net/diaithir/animation/machines/*.java src/test/java/net/diaithir/animation/machines/*.java
java -cp "$out" net.diaithir.animation.machines.TestMain "$out/all.tsv"
