# S6901 belt_reader_check.py OUTDIR WIRETECH_JAR BELTS_JAR ARCH_JAR: circuit reader against a real belt (belt fork jar, headless bot lane S6854). Env HEADLESS_BOT.
import sys, time, os
sys.path.insert(0, os.environ.get('HEADLESS_BOT', '/tmp/hb/cloud/headless_bot'))
from lane import Lane
def ok(c, r):
    t = ' '.join(r)
    if c.endswith(' air'): return True
    want = 'Summoned' if c.startswith('summon') else 'Modified' if c.startswith('data merge') else 'Replaced' if c.startswith('item replace') else 'Changed the block' if c.startswith('setblock') else None
    return want is None or want in t

out, *jars = sys.argv[1:]
with Lane('beltread', jars, out=out, srv_xmx='2G') as L:
    L.start_client(); d = L.drive
    L.server_cmd('gamemode survival Tester'); L.server_cmd('time set day')
    L.wait_render()
    bad = []
    for c in ['setblock 1 -60 -9 air', 'setblock 1 -60 -3 air', 'setblock 1 -60 -2 air', 'setblock 0 -60 -3 air',
              'setblock 1 -60 -2 chest', 'setblock 1 -60 -3 belts:chute[facing=north]', 'setblock 1 -60 -9 belts:chute[facing=north]',
              'data merge block 1 -60 -3 {target:[I;1,-60,-9]}',
              'item replace block 1 -60 -2 container.0 with minecraft:coal 5', 'item replace block 1 -60 -2 container.1 with minecraft:iron_ingot 3',
              'setblock 0 -60 -3 diaithir_wiretech:electronic_circuit_reader[facing=west]']:
        r = L.server_cmd(c, 0.8); print(c, '->', r)
        if not ok(c, r): bad.append(c)
    time.sleep(6)
    print(L.server_cmd('data get block 1 -60 -3', 1.5))
    L.look(0.5, -60, 0.5, 180, 20); time.sleep(1); d.key('r'); time.sleep(4); L.shot('belt_reader.png')
    print(L.log_lines('server', r'Exception|belts.*(rror|ail)')[-6:])
    if bad:
        print('FAIL', bad); sys.exit(1)
