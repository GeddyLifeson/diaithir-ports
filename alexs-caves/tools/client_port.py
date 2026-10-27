#!/usr/bin/env python3
"""1.21.1 client code -> Citadel legacy bridge on 26.1. usage: client_port.py DIR. Idempotent."""
import re, sys, pathlib
LG='com.github.alexthe666.citadel.client.render.legacy.'
R = [
 (r'net\.minecraft\.resources\.ResourceLocation', 'net.minecraft.resources.Identifier'),
 (r'\bResourceLocation\b', 'Identifier'),
 (r'import net\.minecraft\.client\.renderer\.entity\.MobRenderer;', 'import '+LG+'LegacyMobRenderer;'),
 (r'\bextends MobRenderer<', 'extends LegacyMobRenderer<'),
 (r'import net\.minecraft\.client\.renderer\.entity\.layers\.RenderLayer;', 'import '+LG+'LegacyLayer;'),
 (r'import net\.minecraft\.client\.renderer\.entity\.RenderLayerParent;', 'import '+LG+'LegacyLayerParent;'),
 (r'\bRenderLayerParent<', 'LegacyLayerParent<'),
 (r'\bextends RenderLayer<', 'extends LegacyLayer<'),
 (r'import net\.minecraft\.client\.renderer\.RenderType;', 'import net.minecraft.client.renderer.rendertype.RenderType;\nimport net.minecraft.client.renderer.rendertype.RenderTypes;'),
 (r'RenderType\.(entityCutoutNoCull|entityNoOutline|entityCutoutNoCullZOffset)\(', 'RenderTypes.entityCutout('),
 (r'RenderType\.(entityTranslucentCull)\(', 'RenderTypes.entityTranslucent('),
 (r'LivingEntityRenderer\.getOverlayCoords\(', LG+'LegacyMobRenderer.getOverlayCoords('),
 (r'RenderType\.itemEntityTranslucentCull\(', 'RenderTypes.entityTranslucent('),
 (r'getTimer\(\)\.getGameTimeDeltaPartialTick\(([a-z]+)\)', r'getDeltaTracker().getGameTimeDeltaPartialTick(\1)'),
 (r'getTimer\(\)\.getGameTimeDeltaTicks\(\)', 'getDeltaTracker().getGameTimeDeltaTicks()'),
 (r'net\.minecraft\.world\.entity\.vehicle\.Boat\b', 'net.minecraft.world.entity.vehicle.boat.Boat'),
 (r'import net\.minecraft\.client\.model\.ArmedModel;', 'import '+LG+'LegacyArmedModel;'),
 (r'\bimplements ArmedModel\b', 'implements LegacyArmedModel'),
 (r'\bArmedModel\b(?!;)', 'LegacyArmedModel'),
 (r'RenderType\.(waterMask|entityTranslucentEmissive)\(', r'RenderTypes.\1('),
 (r'RenderType\.(entitySolid|outline|entityCutout|entityTranslucent|eyes|energySwirl|lightning|entitySmoothCutout|entityDecal|entityShadow|breezeEyes|text|lines|leash)\(', r'RenderTypes.\1('),
]
for p in pathlib.Path(sys.argv[1]).rglob('*.java'):
    s=p.read_text(); o=s
    for a,b in R: s=re.sub(a,b,s)
    # one-parameter EntityRenderer<X> (1.21.1) -> LegacyEntityRenderer<X>
    if re.search(r'extends EntityRenderer<\w+>', s):
        s=re.sub(r'extends EntityRenderer<(\w+)>', r'extends LegacyEntityRenderer<\1>', s)
        s=s.replace('import net.minecraft.client.renderer.entity.EntityRenderer;','import '+LG+'LegacyEntityRenderer;')
    if re.search(r'\bRenderTypes?\b', s):
        for c in ('RenderType','RenderTypes'):
            if re.search(r'\b'+c+r'\b', s) and ('rendertype.'+c+';') not in s:
                s=re.sub(r'(package [^;]+;\n)', r'\1\nimport net.minecraft.client.renderer.rendertype.'+c+';', s, count=1)
    for cls in ('LegacyMobRenderer','LegacyEntityRenderer','LegacyLayer','LegacyLayerParent'):
        if re.search(r'\b'+cls+r'\b', s) and ('import '+LG+cls+';') not in s:
            s=re.sub(r'(package [^;]+;\n)', r'\1\nimport '+LG+cls+';', s, count=1)
    # LegacyMobRenderer/EntityRenderer already imply a constructor taking the context
    if s!=o: p.write_text(s)

# ---- pass B (idempotent): more API moves
RB = [
 (r'LevelRenderer\.getLightColor\(', 'LevelRenderer.getLightCoords('),
 (r'import net\.minecraft\.client\.renderer\.LightTexture;', 'import net.minecraft.util.LightCoordsUtil;'),
 (r'\bLightTexture\.', 'LightCoordsUtil.'),
 (r'this\.entityRenderDispatcher\.cameraOrientation\(\)', LG+'LegacyRenderContext.camera().orientation'),
 (r'import net\.minecraft\.world\.entity\.projectile\.DragonFireball;\n', ''),
 (r'(?:\w+\.)*renderItem\((\w+), (\w+), (ItemDisplayContext\.\w+), (?:false|true), (\w+), (\w+), (\w+)\)', LG+r'LegacyRenderContext.renderItem(\1, \2, \3, \4, \6)'),
]
for p in pathlib.Path(sys.argv[1]).rglob('*.java'):
    s=p.read_text(); o=s
    for a,b in RB: s=re.sub(a,b,s)
    if s!=o: p.write_text(s)
