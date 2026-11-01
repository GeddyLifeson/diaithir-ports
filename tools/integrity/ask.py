#!/usr/bin/env python3 -I
"""Tiny sync Claude API helper (stdlib). ask(system, prompt, model, max_tokens, schema) -> parsed text/JSON.
System prompt carries cache_control (1h) so repeated rows reuse the prefix. Key is read from the shared secrets file, never printed."""
import json, urllib.request, os
KEY = open("/mnt/project-files/compute/secrets/anthropic_key").read().strip()
MODELS = {"haiku": "claude-haiku-5-5", "sonnet": "claude-sonnet-5-5", "opus": "claude-opus-5-5"}

def ask(system, prompt, model="haiku", max_tokens=800, schema=None):
    body = {"model": MODELS[model], "max_tokens": max_tokens,
            "system": [{"type": "text", "text": system, "cache_control": {"type": "ephemeral", "ttl": "1h"}}],
            "messages": [{"role": "user", "content": prompt}]}
    if model == "haiku": body["thinking"] = {"type": "disabled"}
    if schema: body["output_config"] = {"format": {"type": "json_schema", "schema": schema}}
    r = urllib.request.Request("https://api.anthropic.com/v1/messages", json.dumps(body).encode(),
        {"x-api-key": KEY, "anthropic-version": "2023-06-01", "content-type": "application/json"})
    d = json.load(urllib.request.urlopen(r, timeout=120))
    t = "".join(b.get("text", "") for b in d["content"])
    u = d.get("usage", {})
    with open("/mnt/project-files/compute/ledger.csv", "a") as f:
        import datetime
        f.write(f"{datetime.datetime.utcnow():%Y-%m-%dT%H:%M:%SZ},pack-integrity,{MODELS[model]},{u.get('input_tokens',0)},{u.get('output_tokens',0)},{u.get('cache_creation_input_tokens',0)},{u.get('cache_read_input_tokens',0)}\n")
    return json.loads(t) if schema else t
