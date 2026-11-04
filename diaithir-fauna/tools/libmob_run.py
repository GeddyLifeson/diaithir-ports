#!/usr/bin/env python3
# S6833 Direct parallel Haiku caller for a compute job folder (resumable, cached system prompt).
"""Direct parallel Haiku calls for a compute job folder (in.jsonl -> out2.jsonl), resumable, 24 workers, cached system prompt.
Usage: libmob_run.py JOB_DIR [WORKERS]. Key from /mnt/project-files/compute/secrets/anthropic_key; never printed."""
import json, os, sys, threading, time, urllib.request
from concurrent.futures import ThreadPoolExecutor
JOB = sys.argv[1].rstrip('/'); W = int(sys.argv[2]) if len(sys.argv) > 2 else 24
KEY = open('/mnt/project-files/compute/secrets/anthropic_key').read().strip()
OUT = f'{JOB}/out2.jsonl'
have = set()
if os.path.exists(OUT):
    have = {json.loads(l)['id'] for l in open(OUT)}
rows = [json.loads(l) for l in open(f'{JOB}/in.jsonl')]
rows = [r for r in rows if r['id'] not in have]
lock = threading.Lock()
LED = '/mnt/project-files/compute/ledger.csv'

def call(r):
    body = {'model': 'claude-haiku-5-5', 'max_tokens': 3500, 'thinking': {'type': 'disabled'},
            'system': [{'type': 'text', 'text': r['system'], 'cache_control': {'type': 'ephemeral', 'ttl': '1h'}}],
            'messages': [{'role': 'user', 'content': r['prompt']}]}
    for a in range(5):
        try:
            req = urllib.request.Request('https://api.anthropic.com/v1/messages', json.dumps(body).encode(),
                                         {'x-api-key': KEY, 'anthropic-version': '2023-06-01', 'content-type': 'application/json'})
            d = json.load(urllib.request.urlopen(req, timeout=120))
            t = ''.join(b.get('text', '') for b in d['content'])
            u = d['usage']
            with lock:
                open(OUT, 'a').write(json.dumps({'id': r['id'], 'text': t, 'lane': 'direct'}, ensure_ascii=False) + '\n')
                open(LED, 'a').write(f"{time.strftime('%Y-%m-%dT%H:%M:%SZ', time.gmtime())},{os.path.basename(JOB)},claude-haiku-5-5-sync,{u.get('input_tokens',0)},{u.get('output_tokens',0)},{u.get('cache_creation_input_tokens',0)},{u.get('cache_read_input_tokens',0)}\n")
            return
        except Exception as e:
            time.sleep(2 ** a)
    print('failed', r['id'], flush=True)

with ThreadPoolExecutor(W) as ex:
    list(ex.map(call, rows))
print('done', len(rows))
