"""Bakes ambient occlusion for the villager model onto the 512x512 clothing layout, in headless Blender through the
shared Diaithir parts kit (DiaithirRPG tools/art/blender/kit.py: its scene, colour management and light setup).

    /root/bpyenv/bin/python ao_bake.py KIT_DIR OUT.png [SAMPLES]

The base layer bakes with only the base boxes present; the outer layer bakes with both, so a hood or cloak is dark
where it sits close against the body. Every quad is built from skin.Face.point, the same mapping the painter uses, so
the occlusion lands exactly on the texels it belongs to. Arms hang and legs stand straight, as a villager idles.
"""
import sys
from pathlib import Path

HERE = Path(__file__).resolve().parent
kit_dir, out = sys.argv[1], str(Path(sys.argv[2]).resolve())
samples = int(sys.argv[3]) if len(sys.argv) > 3 else 128
sys.path[:0] = [kit_dir, str(HERE)]
import bpy, bmesh
import numpy as np
import kit
import skin


def build(layer):
    bm = bmesh.new()
    uvl = bm.loops.layers.uv.new("UV")
    for f in skin.faces():
        if f.layer != layer:
            continue
        x, y, w, h = f.rect
        corners = [(0, 1), (1, 1), (1, 0), (0, 0)]               # texture corners, counter-clockwise seen from outside
        pts = [kit.to_bl(*(float(c) for c in f.point(np.float32(a), np.float32(b)))) for a, b in corners]
        q = bm.faces.new([bm.verts.new(p) for p in pts])
        for loop, (a, b) in zip(q.loops, corners):
            loop[uvl].uv = ((x + a * w) / 64, 1 - (y + b * h) / 64)
        lo, hi = f.box[:3], f.box[3:]
        centre = kit.to_bl(*((l + u) / 2 for l, u in zip(lo, hi)))
        q.normal_update()
        if q.normal.dot(q.calc_center_median() - centre) < 0:
            q.normal_flip()
    me = bpy.data.meshes.new(layer)
    bm.to_mesh(me)
    ob = bpy.data.objects.new(layer, me)
    bpy.context.scene.collection.objects.link(ob)
    return ob


sc = kit.setup(128, samples)
sc.world.light_settings.distance = 0.3          # 5 px: folds and contact, not the whole body
img = bpy.data.images.new("ao", skin.SIZE, skin.SIZE, alpha=True, float_buffer=True)
img.generated_color = (1, 1, 1, 0)
mat = bpy.data.materials.new("ao")
mat.use_nodes = True
node = mat.node_tree.nodes.new("ShaderNodeTexImage")
node.image = img
mat.node_tree.nodes.active = node
base, outer = build("base"), build("outer")
for ob in (base, outer):
    ob.data.materials.append(mat)
sc.render.bake.margin = 2
sc.render.bake.use_clear = False
for ob in sc.objects:
    ob.select_set(False)
for target, present in ((base, [base]), (outer, [base, outer])):
    for ob in (base, outer):
        ob.hide_render = ob not in present
        ob.select_set(ob is target)
    bpy.context.view_layer.objects.active = target
    bpy.ops.object.bake(type="AO")
img.filepath_raw = out
img.file_format = "PNG"
img.save()
print("baked", out)
