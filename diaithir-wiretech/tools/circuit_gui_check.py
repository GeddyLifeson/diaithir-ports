# S6896 circuit_gui_check.py OUTDIR JAR: opens the wiretech circuit screens on a real client (headless bot lane, Global-Repo cloud/headless_bot, S6854), programs a constant combinator through the box and screenshots them. Env HEADLESS_BOT=path to cloud/headless_bot.
import sys, time, pathlib
import os; sys.path.insert(0, os.environ.get('HEADLESS_BOT', '/tmp/hb/cloud/headless_bot'))
from lane import Lane
out = sys.argv[1]; jar = sys.argv[2]
with Lane('circuit', [jar], out=out, srv_xmx='2G') as L:
    L.start_client(); d = L.drive
    L.server_cmd('gamemode survival Tester'); L.server_cmd('time set day')
    L.wait_render()
    def open_block(blk, name, prog=None):
        L.server_cmd('setblock 0 -60 -3 air')
        print(L.server_cmd(f'setblock 0 -60 -3 {blk}', 0.8))
        L.look(0.5, -60, 0.5, 180, 20); time.sleep(1); d.key('r'); time.sleep(3); L.shot(f'{name}_0.png')
        if prog:
            for _ in range(40): d.key('BackSpace', hold=0.02)
            d.text(prog); time.sleep(0.5); d.key('Return'); time.sleep(3); L.shot(f'{name}_1.png')
        d.key('Escape'); time.sleep(1)
    open_block('diaithir_wiretech:electronic_constant_combinator', 'const', 'const a=7 b=2')
    open_block('diaithir_wiretech:relay_decider_combinator', 'decider', 'decide a > 5 and red:b < 3 -> c input')
    open_block('diaithir_wiretech:mechanical_circuit_lamp', 'lamp', 'when a >')
    open_block('diaithir_wiretech:electronic_circuit_reader', 'reader')
    print(L.log_lines('client', r'Exception|ERROR.*wiretech|circuit')[-8:])
    print(L.log_lines('server', r'Exception|wiretech.*(error|fail)')[-8:])
