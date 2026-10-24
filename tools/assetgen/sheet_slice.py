import sys, render
M = lambda r: ("model", r)
ents = [("shaft", M("create:block/shaft")), ("powered_shaft", M("create:block/powered_shaft")), ("cogwheel", M("create:block/cogwheel")), ("large_cogwheel", M("create:block/large_cogwheel")),
 ("water_wheel", M("create:block/water_wheel/item")), ("press", M("create:block/mechanical_press/item")), ("basin", M("create:block/basin/block")), ("belt", M("create:block/belt/start")),
 ("mixer", M("create:block/mechanical_mixer/block")), ("creative_motor", M("create:block/creative_motor")), ("depot", M("create:block/depot")), ("chute", M("create:block/chute")),
 ("andesite_funnel", M("create:block/andesite_funnel")), ("brass_tunnel", M("create:block/brass_tunnel")), ("andesite_casing", M("create:block/andesite_casing")), ("brass_casing", M("create:block/brass_casing")),
 ("andesite_encased_shaft", M("create:block/andesite_encased_shaft")), ("gearbox", M("create:block/gearbox"))]
render.sheet(ents, sys.argv[1] if len(sys.argv)>1 else "/tmp/slice.png", cols=6)
