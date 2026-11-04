#!/bin/sh
# S-ID registered in Global-Repo. Needs only a JDK 17+; no Gradle, no network.
set -e
cd "$(dirname "$0")/.."
out=$(mktemp -d)
javac -d "$out" src/main/java/net/diaithir/animation/*.java src/test/java/net/diaithir/animation/*.java
java -cp "$out" net.diaithir.animation.TestMain src/main/resources/data/diaithir_animation/rules.tsv
