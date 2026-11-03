# S6912 rail_reader_check.py OUTDIR WIRETECH_JAR CARTS_JAR [MORE_JARS...]: circuit reader against a real diaithircarts destination rail (named cart) and a real block signal with a cart in its stretch, headless client lane (S6854). Env HEADLESS_BOT.
import sys, time, os
sys.path.insert(0, os.environ.get('HEADLESS_BOT', '/tmp/hb/cloud/headless_bot'))
from lane import Lane
out, *jars = sys.argv[1:]
with Lane('railread', jars, out=out, srv_xmx='2G') as L:
    L.start_client(); d = L.drive
    L.server_cmd('gamemode survival Tester'); L.server_cmd('time set day'); L.wait_render()
    bad = []
    for c in ['setblock 3 -60 -3 diaithircarts:destination_rail', 'setblock 2 -60 -3 diaithir_wiretech:electronic_circuit_reader[facing=west]',
              'summon minecraft:minecart 3.5 -59.9 -2.5 {CustomName:"Alpha"}',
              'setblock 6 -60 -4 rail', 'setblock 6 -60 -5 rail', 'summon minecraft:minecart 6.5 -59.9 -4.5 {CustomName:"Beta",NoGravity:1b}', 'setblock 6 -60 -3 diaithircarts:block_signal[facing=north]', 'setblock 5 -60 -3 diaithir_wiretech:electronic_circuit_reader[facing=west]']:
        r = L.server_cmd(c, 1.0); print(c, '->', r)
        if any('Could not' in x or 'Unknown' in x for x in r): bad.append(c)
    time.sleep(6)
    for name, x in (('rail', 2), ('signal', 5)):
        L.look(x+0.5, -60, 0.5, 180, 20); time.sleep(1); d.key("r"); time.sleep(4); L.shot(f'{name}_reader.png'); d.key('Escape'); time.sleep(1)
    print(L.log_lines('client', r'Exception.*(wiretech|diaithir)')[-6:])
    srv = L.log_lines('server', r'Exception|Could not initialize class net\.diaithir|diaithir.*(rror|ail)')
    srv = [x for x in srv if 'kqueue' not in x and 'AppenderLogging' not in x and 'Only supported on OSX' not in x]
    if bad or srv:
        print('FAIL', bad, srv); sys.exit(1)
    print('PASS setup ok; read the screenshots for cart:alpha, si:vehicles, state:red')
