#!/usr/bin/env python3
# S6927
"""Spawn-to-Gate pipeline (headless, cloud only).
Stages: install (packwiz server side) -> neoforge -> boot -> drive (fake-player progression walk, Gate link + rite) -> report.
Usage: spawn_to_gate.py [--branch claude/pack-v1-3-wave6 | --pack-url URL] [--work /tmp/s2g] [--stage install|boot|drive|all] [--xmx 6G] [--keep]
Writes WORK/report/breakpoints.json and breakpoints.md; exit 0 only if every step passed. Re-run after fixes; --keep reuses the install.
Needs: JDK25 (source /mnt/project-files/tools/warm_cache.sh), /tmp/pw/{bootstrap,installer}.jar (packwiz releases),
harness srvup.sh/cmd.sh from /mnt/project-files/tools/harness."""
import argparse, json, os, re, shutil, subprocess, sys, time, urllib.request
from pathlib import Path

H = "/mnt/project-files/tools/harness"
NF = "26.1.2.116"
RAW = "https://raw.githubusercontent.com/GeddyLifeson/diaithir-ports/{}/pack.toml"
# (name, console command, regex that must appear in new log lines, seconds to wait)
STEPS = [
    ("walk", "ages progression walk", r"PASS progression walk", 120),
    ("gate_link", "bossesgatelink", r"gatelink OK", 120),
    ("gate_rite", "bossesgatetest", r"gate OK|gatetest OK|gate rite OK", 500),
]
ERR = re.compile(r"\[(ERROR|FATAL)\]|/ERROR\]|Mod loading has failed")
IGN = re.compile(r"Picked up JAVA_TOOL_OPTIONS|No key layers|ScalableLux/\]: Failed to analyze|MTSERROR: Welcome")


def sh(cmd, **k):
    return subprocess.run(cmd, text=True, capture_output=True, **k)


def install(a, W):
    srv = W / "srv"
    srv.mkdir(parents=True, exist_ok=True)
    if not (srv / "mods").is_dir() or not a.keep:
        shutil.copy2("/tmp/pw/installer.jar", srv / "packwiz-installer.jar")
        r = sh(["java", "-jar", "/tmp/pw/bootstrap.jar", "--bootstrap-no-update", "-g", "-s", "server", (a.pack_url or RAW.format(a.branch))], cwd=srv)
        (W / "install.log").write_text(r.stdout + r.stderr)
        if r.returncode:
            return "packwiz install failed (see install.log)"
    inst = W / f"neoforge-{NF}-installer.jar"
    if not (srv / "libraries/net/neoforged/neoforge" / NF).exists():
        urllib.request.urlretrieve(f"https://maven.neoforged.net/releases/net/neoforged/neoforge/{NF}/neoforge-{NF}-installer.jar", inst)
        r = sh(["java", "-jar", str(inst), "--install-server"], cwd=srv)
        (W / "neoforge.log").write_text(r.stdout + r.stderr)
        if r.returncode:
            return "neoforge install failed"
    for j in (srv / "mods").glob("grieflogger*.jar"):  # known boot crash on headless server; removed for the test boot only (CLAUDE.md 4a)
        j.unlink()
    for j in Path(a.overlay).glob("*.jar") if a.overlay else []:  # jars from unmerged PRs; a jar replaces mods with the same modid prefix
        for old in (srv / "mods").glob(j.name.split("-neoforge")[0].split("-1")[0] + "*.jar"):
            if old.name != j.name: old.unlink()
        shutil.copy2(j, srv / "mods" / j.name)
    link = W.parent / "content"  # ProgressionService.walk reads ../../content/datapack relative to the server dir (dev-checkout path)
    if a.rpg and not link.exists():
        link.symlink_to(Path(a.rpg) / "content")
    (srv / "eula.txt").write_text("eula=true\n")
    (srv / "server.properties").write_text("online-mode=false\nmax-tick-time=-1\nspawn-protection=0\nview-distance=4\nsimulation-distance=4\n")
    return None


def newlines(log, pos):
    d = log.read_bytes() if log.exists() else b""
    return d[pos:].decode("utf8", "replace"), len(d)


def run(a):
    W = Path(a.work)
    (W / "report").mkdir(parents=True, exist_ok=True)
    out = {"branch": a.branch, "started": time.strftime("%FT%TZ", time.gmtime()), "breakpoints": [], "steps": []}

    def bp(stage, what, detail=""):
        out["breakpoints"].append({"stage": stage, "what": what, "detail": detail[:1500]})

    env = dict(os.environ, XMX=a.xmx, WAIT="900")
    if a.stage in ("install", "all"):
        e = install(a, W)
        if e:
            bp("install", e)
            return finish(out, W)
    srv, log = W / "srv", W / "server.log"
    if a.stage in ("boot", "all"):
        up = W / "srvup.out"  # a file, not a pipe: srvup leaves the server running and a pipe would block until it exits
        with open(up, "w") as f:
            subprocess.run(["bash", f"{H}/srvup.sh", str(srv), str(log)], env=env, stdout=f, stderr=subprocess.STDOUT)
        res = up.read_text().strip()
        if not res.startswith("Done"):
            bp("boot", "server did not reach Done", res or "timeout")
            return finish(out, W)
        out["boot"] = res
        txt, _ = newlines(log, 0)
        errs = [l for l in txt.splitlines() if ERR.search(l) and not IGN.search(l)]
        out["boot_error_lines"] = len(errs)
        seen_e = set()
        for l in errs:
            k = re.sub(r"^\[[0-9:]+\]", "", l)[:120]
            if k not in seen_e and len(seen_e) < 40:
                seen_e.add(k)
                bp("boot", "error line at boot", l)
    if a.stage in ("drive", "all"):
        pos = newlines(log, 0)[1]
        for name, cmd, ok, wait in [x for x in STEPS if not a.steps or x[0] in a.steps.split(",")]:
            subprocess.run(["bash", f"{H}/cmd.sh", str(log), cmd])
            t0, seen = time.time(), ""
            while time.time() - t0 < wait:
                time.sleep(3)
                seen = newlines(log, pos)[0]
                if re.search(ok, seen):
                    break
            passed = bool(re.search(ok, seen))
            pos = newlines(log, 0)[1]
            fails = [l for l in seen.splitlines() if re.search(r"FAIL|Unknown or incomplete command|Exception", l) and not re.search(r"ETH canopy", l)]  # canopy tiles 404 from the cloud proxy; trees fall back to biomes
            out["steps"].append({"step": name, "cmd": cmd, "passed": passed and not fails, "secs": round(time.time() - t0)})
            if not passed:
                bp(name, f"no success line for '/{cmd}'", seen[-900:])
            for l in fails[:15]:
                bp(name, "failure line", l)
        subprocess.run(["bash", f"{H}/cmd.sh", str(log), "stop"])
        time.sleep(8)
    return finish(out, W)


def finish(out, W):
    out["passed"] = not out["breakpoints"] and bool(out["steps"]) and all(s["passed"] for s in out["steps"])
    R = W / "report"
    (R / "breakpoints.json").write_text(json.dumps(out, indent=1))
    md = [f"# Spawn-to-Gate run {out['started']} on {out['branch']}", f"Result: {'PASS' if out['passed'] else 'BREAKS'}", ""]
    md += [f"- step {s['step']}: {'ok' if s['passed'] else 'FAILED'} ({s['secs']}s)" for s in out["steps"]]
    md += ["", "## Breakpoints"] + [f"{i+1}. [{b['stage']}] {b['what']}\n   `{b['detail'][:300]}`" for i, b in enumerate(out["breakpoints"])]
    (R / "breakpoints.md").write_text("\n".join(md))
    print("\n".join(md))
    return 0 if out["passed"] else 1


if __name__ == "__main__":
    p = argparse.ArgumentParser()
    p.add_argument("--branch", default="claude/pack-v1-3-wave6")
    p.add_argument("--pack-url", default="", help="full pack.toml URL (e.g. the light pack); overrides --branch")
    p.add_argument("--work", default="/tmp/s2g")
    p.add_argument("--stage", default="all")
    p.add_argument("--xmx", default="6G")
    p.add_argument("--steps", default="", help="comma list of step names to run (default all)")
    p.add_argument("--keep", action="store_true")
    p.add_argument("--rpg", default="/home/user/diaithirrpg", help="DiaithirRPG checkout with content/datapack")
    p.add_argument("--overlay", default="", help="dir of jars built from unmerged PRs, copied over the installed mods")
    sys.exit(run(p.parse_args()))
