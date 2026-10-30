#!/usr/bin/env python3
"""Registry builder conversions (entity types, block entity types)."""
import re, sys, pathlib
root=pathlib.Path(sys.argv[1])
p=root/'com/github/alexmodguy/alexscaves/server/entity/ACEntityRegistry.java'
s=p.read_text()
def f(m):
    name=m.group(1); body=m.group(2)
    body=re.sub(r'\.build\("[^"]*"\)', '.build(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ENTITY_TYPE, net.minecraft.resources.Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "%s")))'%name, body)
    body=body.replace('.setUpdateInterval(1)','.updateInterval(1)').replace('.setShouldReceiveVelocityUpdates(true)','').replace('.setShouldReceiveVelocityUpdates(false)','')
    return 'register("%s", () -> %s'%(name,body)
s=re.sub(r'register\("(\w+)", \(\) -> ((?:\(EntityType\) )?EntityType\.Builder.*?\.build\("[^"]*"\)\))', f, s)
p.write_text(s)
p=root/'com/github/alexmodguy/alexscaves/server/block/blockentity/ACBlockEntityRegistry.java'
s=p.read_text()
s=re.sub(r'BlockEntityType\.Builder\.of\(([\w:]+),\s*((?:[^()]|\([^()]*\))*?)\)\.build\(null\)', r'new BlockEntityType<>(\1, \2)', s)
p.write_text(s)
