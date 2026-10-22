"""Renders villagers wearing clothing textures the way the game draws them (nearest filtering, fixed face shades:
up 1.0, front and back 0.8, sides 0.6), from the front and from behind, for approval sheets.

    /root/bpyenv/bin/python preview.py OUT_DIR TEXTURE.png [TEXTURE.png ...]

Writes <name>_front.png and <name>_back.png per texture. Under the clothes is a plain villager skin (skin tone, dark
hair, eyes), standing in for MCA's own skins.
"""
import math, sys
from pathlib import Path

HERE = Path(__file__).resolve().parent
sys.path.insert(0, str(HERE))
import bpy, bmesh
import numpy as np
from mathutils import Vector
import skin

SHADE = {"top": 1.0, "bottom": 0.5, "front": 0.8, "back": 0.8, "left": 0.6, "right": 0.6}


def body_skin(path):
    """A plain villager skin: warm brown skin, dark hair over the top and back of the head, eyes."""
    from PIL import Image
    a = np.zeros((skin.SIZE, skin.SIZE, 4), np.uint8)
    for f in skin.faces():
        if f.layer != "base":
            continue
        c = np.array([150, 104, 76, 255], np.uint8)
        a[f.sl] = c
        if f.part == "head":
            hair = (f.Y > 30.2) | (f.name in ("back", "top")) | ((f.name in ("left", "right")) & ((f.Z < 1.5) | (f.Y > 29)))
            a[f.sl][hair] = (46, 34, 26, 255)
            if f.name == "front":
                eye = (np.abs(np.abs(f.X) - 2.0) < 0.75) & (f.Y > 27.2) & (f.Y < 28.2)
                a[f.sl][eye] = (236, 232, 224, 255)
                pupil = (np.abs(np.abs(f.X) - 1.75) < 0.4) & (f.Y > 27.2) & (f.Y < 28.2)
                a[f.sl][pupil] = (40, 30, 24, 255)
    Image.fromarray(a).save(path)


def material(name, tex, under=None):
    m = bpy.data.materials.new(name)
    m.use_nodes = True
    nt = m.node_tree
    for n in list(nt.nodes):
        nt.nodes.remove(n)
    img = nt.nodes.new("ShaderNodeTexImage"); img.image = tex; img.interpolation = "Closest"
    attr = nt.nodes.new("ShaderNodeAttribute"); attr.attribute_name = "shade"; attr.attribute_type = "GEOMETRY"
    em = nt.nodes.new("ShaderNodeEmission")
    nt.links.new(attr.outputs["Fac"], em.inputs["Strength"])
    out = nt.nodes.new("ShaderNodeOutputMaterial")
    if under is not None:                       # base layer: clothes over the villager's own skin
        u = nt.nodes.new("ShaderNodeTexImage"); u.image = under; u.interpolation = "Closest"
        mx = nt.nodes.new("ShaderNodeMix"); mx.data_type = "RGBA"
        nt.links.new(img.outputs["Alpha"], mx.inputs["Factor"])
        nt.links.new(u.outputs["Color"], mx.inputs["A"]); nt.links.new(img.outputs["Color"], mx.inputs["B"])
        nt.links.new(mx.outputs["Result"], em.inputs["Color"]); nt.links.new(em.outputs[0], out.inputs[0])
    else:
        tr = nt.nodes.new("ShaderNodeBsdfTransparent"); mx = nt.nodes.new("ShaderNodeMixShader")
        nt.links.new(img.outputs["Color"], em.inputs["Color"]); nt.links.new(img.outputs["Alpha"], mx.inputs[0])
        nt.links.new(tr.outputs[0], mx.inputs[1]); nt.links.new(em.outputs[0], mx.inputs[2]); nt.links.new(mx.outputs[0], out.inputs[0])
    return m


def build(layer, mat):
    bm = bmesh.new()
    uvl = bm.loops.layers.uv.new("UV")
    sh = bm.faces.layers.float.new("shade")
    for f in skin.faces():
        if f.layer != layer:
            continue
        x, y, w, h = f.rect
        corners = [(0, 1), (1, 1), (1, 0), (0, 0)]
        pts = [Vector((float(p[0]) / 16, -float(p[2]) / 16, float(p[1]) / 16)) for p in (f.point(np.float32(a), np.float32(b)) for a, b in corners)]
        q = bm.faces.new([bm.verts.new(p) for p in pts])
        for loop, (a, b) in zip(q.loops, corners):
            loop[uvl].uv = ((x + a * w) / 64, 1 - (y + b * h) / 64)
        q[sh] = SHADE[f.name]
    me = bpy.data.meshes.new(layer); bm.to_mesh(me)
    me.materials.append(mat)
    ob = bpy.data.objects.new(layer, me); bpy.context.scene.collection.objects.link(ob)
    return ob


def main():
    out, texs = Path(sys.argv[1]), sys.argv[2:]
    out.mkdir(parents=True, exist_ok=True)
    bpy.ops.wm.read_factory_settings(use_empty=True)
    sc = bpy.context.scene
    sc.render.engine = "CYCLES"; sc.cycles.samples = 4; sc.cycles.pixel_filter_type = "BOX"; sc.cycles.filter_width = 0.5
    sc.cycles.transparent_max_bounces = 8
    sc.render.resolution_x, sc.render.resolution_y = 320, 520; sc.render.film_transparent = True
    sc.view_settings.view_transform = "Standard"
    sc.world = bpy.data.worlds.new("w")
    under_path = out / "_villager_skin.png"
    body_skin(under_path)
    under = bpy.data.images.load(str(under_path))
    cloth = bpy.data.images.load(str(Path(texs[0]).resolve()))
    build("base", material("base", cloth, under)); build("outer", material("outer", cloth))
    bpy.ops.object.camera_add(); cam = bpy.context.object; cam.data.type = "ORTHO"; cam.data.ortho_scale = 2.35; sc.camera = cam
    centre = Vector((0, 0, 16.5 / 16))
    for t in texs:
        cloth.filepath = str(Path(t).resolve()); cloth.reload()
        for side, d in (("front", Vector((0.55, -1, 0.3))), ("back", Vector((-0.55, 1, 0.3)))):
            d = d.normalized()
            cam.location = centre + d * 6
            cam.rotation_euler = (-d).to_track_quat("-Z", "Y").to_euler()
            sc.render.filepath = str(out / f"{Path(t).stem}_{side}.png")
            bpy.ops.render.render(write_still=True)


if __name__ == "__main__":
    main()
