#!/usr/bin/env python3
"""Zip src/main/resources plus the compiled classes of ars-nouveau/ into build/diaithir_druidry-<version>.jar.
The data is MIT; the Ars Nouveau code port is LGPL-3.0 (LICENSE-Ars = ars-nouveau/LICENSE + NOTICE).
Usage: build.py [version] [--no-deps] (--no-deps drops spell_engine/spell_power so a bare server can boot the jar for tests).
Needs JDK 25 (source /mnt/project-files/tools/get_jdk25.sh) when the classes are not compiled yet."""
import os, re, subprocess, sys, zipfile
root = os.path.dirname(os.path.abspath(__file__))
args = [a for a in sys.argv[1:] if not a.startswith("--")]
ver = args[0] if args else "0.1.0"
ars = os.path.join(root, "ars-nouveau")
classes = os.path.join(ars, "build", "classes", "java", "main")
subprocess.check_call(["./gradlew", "compileJava", "--no-daemon", "-q", "-Porg.gradle.java.installations.paths=" + os.environ.get("JAVA_HOME", "")], cwd=ars)
res = os.path.join(root, "src", "main", "resources")
out = os.path.join(root, "build"); os.makedirs(out, exist_ok=True)
jar = os.path.join(out, f"diaithir_druidry-{ver}.jar")
with zipfile.ZipFile(jar, "w", zipfile.ZIP_DEFLATED) as z:
    z.writestr("META-INF/MANIFEST.MF", "Manifest-Version: 1.0\n")
    for d, _, fs in os.walk(res):
        for f in sorted(fs):
            p = os.path.join(d, f); rel = os.path.relpath(p, res).replace(os.sep, "/")
            data = open(p, "rb").read()
            if rel == "META-INF/neoforge.mods.toml":
                data = data.replace(b"${version}", ver.encode())
                if "--no-deps" in sys.argv:
                    data = re.sub(rb'\[\[dependencies\.diaithir_druidry\]\]\nmodId = "spell_\w+".*?\n\n?(?=\[\[|\Z)', b"", data, flags=re.S)
            z.writestr(rel, data)
    for d, _, fs in os.walk(classes):
        for f in sorted(fs):
            p = os.path.join(d, f)
            z.write(p, os.path.relpath(p, classes).replace(os.sep, "/"))
    z.writestr("LICENSE-Ars", open(os.path.join(ars, "LICENSE")).read() + "\n\n" + open(os.path.join(ars, "NOTICE")).read())
    z.write(os.path.join(root, "LICENSE"), "LICENSE")
print(jar)
