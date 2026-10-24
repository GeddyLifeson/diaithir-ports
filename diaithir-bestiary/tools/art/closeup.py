import sys
sys.path.insert(0, '/home/user/w-bestiary/repo/diaithir-bestiary/tools/art')
import numpy as np
from render import Rig
name, out = sys.argv[1], sys.argv[2]
yaw = float(sys.argv[3]) if len(sys.argv) > 3 else 20
cy = float(sys.argv[4]) if len(sys.argv) > 4 else 40
sc = float(sys.argv[5]) if len(sys.argv) > 5 else 14
rig = Rig('/tmp/claude-0/work/out', name)
rig.render(None, 0, yaw, 8, sc, (900, 900), ss=2, center=np.array([0, cy, 0])).save(out)
