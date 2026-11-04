#!/usr/bin/env python3
"""Live KeyMapping dump (cloud, headless): boots a real NeoForge client under Xvfb with the given jars,
waits for the title screen, closes it (WM_DELETE) so Minecraft saves options.txt, and prints every
registered key_* line. Needs: portablemc, python-xlib, Xvfb, JDK25 (LANE_JAVA).
Usage: dump_live_keys.py WORKDIR OUT.txt JAR... """
import os, sys, time, subprocess, shutil, pathlib
w, out, *jars = sys.argv[1:]
w = pathlib.Path(w); shutil.rmtree(w, ignore_errors=True); (w / 'mods').mkdir(parents=True)
for j in jars:
    d = w / "mods" / os.path.basename(j)
    if not d.exists(): os.symlink(os.path.abspath(j), d)
import os as _o
_extra = open(_o.environ['OPTIONS_IN']).read() if _o.environ.get('OPTIONS_IN') else ''
(w / 'options.txt').write_text(_extra + 'version:4790\nonboardAccessibility:false\ngraphicsMode:0\nmaxFps:20\nrenderDistance:2\nskipMultiplayerWarning:true\n')
java = os.environ.get('LANE_JAVA', '/tmp/claude-0/jdk25')
nf = os.environ.get('NF', '26.1.2.116'); disp = os.environ.get('DISPLAY_NUM', '97')
if subprocess.run(['pgrep', '-f', f'Xvfb :{disp}'], capture_output=True).returncode:
    subprocess.Popen(['Xvfb', f':{disp}', '-screen', '0', '1280x720x24'], stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL); time.sleep(2)
env = {**os.environ, 'LIBGL_ALWAYS_SOFTWARE': '1', 'GALLIUM_DRIVER': 'llvmpipe', 'DISPLAY': f':{disp}'}
p = subprocess.Popen(['portablemc', '--main-dir', os.environ.get('MCDIR', '/tmp/claude-0/mc'), 'start', '--mc-dir', str(w), f'neoforge::{nf}', '-u', 'Dump',
    '--jvm', f'{java}/bin/java', f'--jvm-arg=-Xmx{os.environ.get("XMX", "10G")}', '--resolution', '1280x720'], env=env, stdout=open(w / 'pmc.out', 'w'), stderr=subprocess.STDOUT, start_new_session=True)
log = w / 'logs' / 'latest.log'
t0 = time.time(); ready = False
while time.time() - t0 < int(os.environ.get('WAIT', '900')) and p.poll() is None:
    if log.exists():
        t = log.read_text(errors='replace')
        if 'Backend library: LWJGL' in t or 'Narrator library' in t or 'Created: ' in t and 'Stitching' in t: ready = True
        if ready and 'Reloading ResourceManager' in t and time.time() - t0 > 60:
            time.sleep(40); break
    time.sleep(5)
from Xlib import display, X, protocol
dp = display.Display(f':{disp}')
def close(win):
    for c in win.query_tree().children:
        close(c)
    try:
        ev = protocol.event.ClientMessage(window=win, client_type=dp.intern_atom('WM_PROTOCOLS'), data=(32, [dp.intern_atom('WM_DELETE_WINDOW'), X.CurrentTime, 0, 0, 0]))
        win.send_event(ev)
    except Exception: pass
close(dp.screen().root); dp.sync()
try: p.wait(60)
except subprocess.TimeoutExpired: p.terminate()
import signal
try: os.killpg(os.getpgid(p.pid), signal.SIGTERM)
except Exception: pass
o = w / 'options.txt'
lines = [l for l in o.read_text().splitlines() if l.startswith('key_')] if o.exists() else []
pathlib.Path(out).write_text('\n'.join(lines) + '\n')
print(len(lines), 'key lines; mods', len(jars), 'ready', ready)
