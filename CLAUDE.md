# diaithir-ports: Claude instructions

**Read `/mnt/project-files/CLAUDE.md` first.** It is the master file for every repo and session (API practices, ModelRouter and free lanes, scripts and tools, master module list, plan, Bible, standing rules with sources). If that path is not mounted, the essentials below are enough to start; do not duplicate or fork them here.

## Essentials (from the master)
1. Order of work: ModelRouter and free lanes first, then the Claude API (job folder, Haiku Batch), then Claude sessions. Session tokens are the scarce resource; spend them on judgment and integration only.
2. Claude API key: read it from `/mnt/project-files/compute/secrets/anthropic_key` into `ANTHROPIC_API_KEY` (Marlon allowed this on 2026-11-01 so every session can use the API; expires about 11-26). Never print it, commit it, paste it into CLAUDE.md, a PR, a job file or memory, or copy it anywhere else. Never put any other token or Marlon's email in a file, commit or request.
3. Anything needing Marlon is one desktop-shortcut double-click. Do not ask him to refresh tokens or paste things.
4. Find the latest 26.1 mod build; else port an older one (licence allowing); else build our own (clean-room for ARR). Duplicates merge into one thing with id migration.
5. No invented entries. Panscriptum assays come from real page text only. Everything in the Panscriptum must be in the modpack and vice versa.
6. Tests run in the real world at spawn, not backrooms or void, and test only the thing asked.
7. Commit and push after every working change. No GitHub Actions. No CurseForge or Modrinth publishing. No local models on the PC or laptop.

## This repo
NeoForge 26.1.2 mod ports and our own `diaithir-*` mods, plus the packwiz pack (public; third-party jars are never rehosted, `.pw.toml` points at the original CDN file). Each mod folder is its own project; check the licence before porting and record it in the PR. Build the pack with `build_pack.py` (DiaithirRPG tools/distribution) and run a clean-install and client world-join test before any publish. `claude/pack-latest` is the newest full test pack. No CurseForge or Modrinth publishing.
