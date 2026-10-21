import sys, time
# Drives the Xvfb display with XTest: c<x>,<y> click, k<keysym> key, t<text> type, w<secs> wait.
from Xlib import display, X, XK
from Xlib.ext import xtest
d = display.Display(':99')
def click(x, y):
    xtest.fake_input(d, X.MotionNotify, x=x, y=y); d.sync(); time.sleep(0.2)
    xtest.fake_input(d, X.ButtonPress, 1); d.sync(); time.sleep(0.1)
    xtest.fake_input(d, X.ButtonRelease, 1); d.sync()
def key(name):
    k = d.keysym_to_keycode(XK.string_to_keysym(name))
    xtest.fake_input(d, X.KeyPress, k); d.sync(); time.sleep(0.1)
    xtest.fake_input(d, X.KeyRelease, k); d.sync()
NAMES = {' ': 'space', '/': 'slash', '"': 'quotedbl', ':': 'colon', '_': 'underscore', '-': 'minus', '.': 'period', '~': 'asciitilde', '@': 'at', '=': 'equal', ',': 'comma', '[': 'bracketleft', ']': 'bracketright', '{': 'braceleft', '}': 'braceright'}
def typ(text):
    shift = d.keysym_to_keycode(XK.string_to_keysym('Shift_L'))
    for ch in text:
        sym = XK.string_to_keysym(NAMES.get(ch, ch))
        code = d.keysym_to_keycode(sym)
        up = d.keycode_to_keysym(code, 0) != sym
        if up: xtest.fake_input(d, X.KeyPress, shift)
        xtest.fake_input(d, X.KeyPress, code); xtest.fake_input(d, X.KeyRelease, code)
        if up: xtest.fake_input(d, X.KeyRelease, shift)
        d.sync(); time.sleep(0.02)
for a in sys.argv[1:]:
    if a.startswith('t'): typ(a[1:]); continue
    if a.startswith('c'): x, y = a[1:].split(','); click(int(x), int(y))
    elif a.startswith('k'): key(a[1:])
    elif a.startswith('w'): time.sleep(float(a[1:]))
