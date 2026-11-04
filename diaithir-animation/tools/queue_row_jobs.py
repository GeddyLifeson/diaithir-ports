#!/usr/bin/env python3
"""Queues the per-row animation spec work from ANIMATIONS_COVERAGE.csv into compute/jobs as shards of ~500 rows.
Usage: queue_row_jobs.py [coverage.csv] [jobs_dir] [--shard=500] [--name=anim_rows]
One in.jsonl line per row: id = the row id (AN00001), prompt = the row's facts. The model returns one JSON object (the spec) per row; the owner thread
validates (see README written beside each in.jsonl) and turns the specs into clips with the generators."""
import csv, json, os, sys
args = [a for a in sys.argv[1:] if not a.startswith("--")]
opt = dict(a[2:].split("=", 1) for a in sys.argv[1:] if a.startswith("--") and "=" in a)
CSV = args[0] if args else "/mnt/project-files/plans/ANIMATIONS_COVERAGE.csv"
JOBS = args[1] if len(args) > 1 else "/mnt/project-files/compute/jobs"
SHARD = int(opt.get("shard", 500)); NAME = opt.get("name", "anim_rows")

SYSTEM = """You write animation specs for a Minecraft NeoForge 26.1 modpack (Player Animation Library for players, GeckoLib for mobs, bosses, machines and vehicles). Reply with ONE JSON object and nothing else, no markdown.
Schema: {"thing": str, "engine": "PAL"|"GeckoLib"|"particles"|"blockstate", "bones": [str], "clips": [{"name": str, "ticks": int, "loop": bool, "trigger": str, "motion": str, "events": [{"tick": int, "kind": "sound"|"particle"|"hit"|"item", "what": str}]}]}.
Rules: one clip for every name in animations_needed (split a comma list into separate clips; keep the given names); ticks are game ticks (20 per second), loops 20-60, one-shots 4-40, attacks 8-30; "trigger" says what in game starts it (state, event or input); "motion" is one or two plain sentences naming the real body part or part of the machine that moves, the direction and the amount (degrees or blocks); bones use vanilla player bones for PAL (head, body, right_arm, left_arm, right_leg, left_leg) and sensible part names for GeckoLib. Stay specific to the named thing: use what you know of the real object or creature. No invented game mechanics. Keep each motion under 200 characters and each spec under 700 tokens."""

rows = list(csv.DictReader(open(CSV, encoding="utf-8")))
shards = [rows[i:i + SHARD] for i in range(0, len(rows), SHARD)]
for n, sh in enumerate(shards):
    d = os.path.join(JOBS, f"{NAME}-s{n:02d}")
    os.makedirs(d, exist_ok=True)
    with open(os.path.join(d, "in.jsonl"), "w", encoding="utf-8") as f:
        for r in sh:
            p = (f"thing: {r['thing']}\ncategory: {r['category']}\nkind: {r['kind']}\nmod namespace: {r['namespace']}\n"
                 f"animations_needed: {r['animations_needed']}\nengine route: {r['engine_route']}")
            f.write(json.dumps({"id": r["id"], "system": SYSTEM, "prompt": p}, ensure_ascii=False) + "\n")
    open(os.path.join(d, "README"), "w").write(
        f"model: haiku\nmax_tokens 900\nowner: animation threads (gun/melee thread; lead cmsg_01EpyoqYuwV3X4z3b4zk5pEKM8LRfAyhG5YgLAAHSVLA61)\n"
        f"Source: plans/ANIMATIONS_COVERAGE.csv shard {n} of {len(shards)} ({len(sh)} rows), made by diaithir-animation/tools/queue_row_jobs.py.\n"
        "CHECK: each out.jsonl text parses as one JSON object; clips has one entry per animations_needed name; every ticks is an int 2..200; no clip without motion. "
        "Rows that fail go back as a new folder <name>-retry with the same in.jsonl line.\n")
print(len(rows), "rows,", len(shards), "shards ->", JOBS)
