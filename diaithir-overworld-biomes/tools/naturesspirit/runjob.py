# direct sync runner (watcher was down): python3 -I runjob.py <jobdir> [model]
import json,sys,os,urllib.request,datetime,concurrent.futures as cf,time,pathlib
d=pathlib.Path(sys.argv[1]); model=sys.argv[2] if len(sys.argv)>2 else 'claude-sonnet-5-5'
KEY=open('/mnt/project-files/compute/secrets/anthropic_key').read().strip()
(d/'.claimed').write_text('ns-port direct runner\n')
rows=[json.loads(l) for l in open(d/'in.jsonl')]
done=set()
if (d/'out.jsonl').exists(): done={json.loads(l)['id'] for l in open(d/'out.jsonl')}
def call(r):
    b={"model":model,"max_tokens":32000,"system":[{"type":"text","text":r['system'],"cache_control":{"type":"ephemeral","ttl":"1h"}}] if r.get('system') else None,"messages":[{"role":"user","content":r['prompt']}]}
    if not b['system']: del b['system']
    if 'sonnet' in model: b['thinking']={"type":"between_tools"}
    for a in range(6):
        try:
            q=urllib.request.Request("https://api.anthropic.com/v1/messages",json.dumps(b).encode(),{"x-api-key":KEY,"anthropic-version":"2023-06-01","content-type":"application/json"})
            x=json.load(urllib.request.urlopen(q,timeout=1200)); u=x['usage']
            open('/mnt/project-files/compute/ledger.csv','a').write(f"{datetime.datetime.now(datetime.UTC):%Y-%m-%dT%H:%M:%SZ},{d.name},{model} sync,{u['input_tokens']},{u['output_tokens']},{u.get('cache_creation_input_tokens',0)},{u.get('cache_read_input_tokens',0)}\n")
            return ''.join(c.get('text','') for c in x['content'])
        except Exception as e:
            err=str(e); time.sleep(5*(a+1))
    return None
with open(d/'out.jsonl','a') as f, cf.ThreadPoolExecutor(24) as ex:
    futs={ex.submit(call,r):r for r in rows if r['id'] not in done}
    for fu in cf.as_completed(futs):
        t=fu.result()
        if t is not None: f.write(json.dumps({"id":futs[fu]['id'],"text":t,"lane":"sync-direct"})+"\n"); f.flush()
print('done',d.name)
