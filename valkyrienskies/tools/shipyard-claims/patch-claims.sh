#!/bin/bash
# patch-claims.sh <vs jar> <asm-9.x.jar> <java-home>
# Post-build step: rewrites the shipyard claim-area constants (-7000/7000/7001 -> -4800/4800/4801)
# in the bundled closed vs-core classes (ChunkAllocator, ChunkAllocator$Companion, VSCoreImpl),
# in place. Zip order is kept (MANIFEST first) by using `zip -X` on a temp tree.
set -e
JAR=$(readlink -f "$1"); ASM=$(readlink -f "$2"); JH=$3
HERE=$(cd "$(dirname "$0")" && pwd)
T=$(mktemp -d); trap 'rm -rf "$T"' EXIT
mkdir "$T/out" "$T/x"
"$JH/bin/javac" -cp "$ASM" -d "$T/out" "$HERE/PatchClaims.java"
(cd "$T/x" && unzip -q "$JAR" && \
  C=$(find . -name 'ChunkAllocator.class' -o -name 'ChunkAllocator$Companion.class' -o -name 'VSCoreImpl.class') && \
  "$JH/bin/java" -cp "$T/out:$ASM" PatchClaims $C && \
  rm "$JAR" && zip -qX "$JAR" META-INF/MANIFEST.MF && zip -qrX "$JAR" . -x META-INF/MANIFEST.MF)
