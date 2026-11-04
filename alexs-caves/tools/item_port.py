#!/usr/bin/env python3
"""quarantine ACItemstackRenderer -> src ACLegacyItemRenderer (special model renderer body). Idempotent."""
import re, pathlib, subprocess, sys
src=pathlib.Path('quarantine/client/render/item/ACItemstackRenderer.java').read_text()
D=pathlib.Path('src/main/java/com/github/alexmodguy/alexscaves/client/render/item'); D.mkdir(parents=True,exist_ok=True)
s=src
s=s.replace('package com.github.alexmodguy.alexscaves.client.render.item;','package com.github.alexmodguy.alexscaves.client.render.item;') 
s=s.replace('ACItemstackRenderer','ACLegacyItemRenderer')
s=s.replace('public class ACLegacyItemRenderer extends BlockEntityWithoutLevelRenderer {','public class ACLegacyItemRenderer {')
s=re.sub(r'    public ACLegacyItemRenderer\(\) \{\n        super\(null, null\);\n    \}\n','    public ACLegacyItemRenderer() {\n    }\n',s)
s=s.replace('    @Override\n    public void renderByItem(ItemStack itemStackIn, ItemDisplayContext transformType,','    public void renderByItem(ItemStack itemStackIn, ItemDisplayContext transformType,')
# drop cave map branch
i=s.index('        if (itemStackIn.is(ACItemRegistry.CAVE_MAP.get())) {'); j=s.index('        if (itemStackIn.is(ACItemRegistry.GALENA_GAUNTLET.get())) {')
s=s[:i]+s[j:]
s=s.replace('Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(false)','Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false)')
s=re.sub(r'ItemRenderer\.getArmorFoilBuffer\(bufferIn, ([^;]*?), itemStackIn\.hasFoil\(\)\)',r'bufferIn.getBuffer(\1)',s)
s=re.sub(r'ItemRenderer\.getFoilBuffer\(bufferIn, _default, false, foil\)','bufferIn.getBuffer(_default)',s)
s=s.replace('RenderType.armorCutoutNoCull(','RenderTypes.entityCutout(')
# renderStatic helpers -> remove bodies
s=re.sub(r'    private void renderStaticItemSprite\(.*?\n    \}\n\n    private void renderStaticItemSpriteWithLighting\(.*?\n    \}\n','    private void renderStaticItemSprite(ItemStack spriteItem, ItemDisplayContext t, int l, int o, PoseStack p, MultiBufferSource b, ClientLevel lv) {\n    }\n',s,flags=re.S)
s=s.replace('manager.render(renderedDreadbowArrow, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F, poseStack, bufferIn,','com.github.alexthe666.citadel.client.render.legacy.LegacyRenderContext.renderEntity(renderedDreadbowArrow, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F, poseStack,')
s=s.replace('EntityRenderDispatcher manager = Minecraft.getInstance().getEntityRenderDispatcher();','')
for imp in ['com.github.alexmodguy.alexscaves.client.gui.book.widget.ItemWidget','com.github.alexmodguy.alexscaves.client.render.misc.CaveMapRenderHelper','net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer','net.minecraft.client.renderer.entity.ItemRenderer','net.minecraft.client.resources.model.BakedModel']:
    s=s.replace('import '+imp+';\n','')
s=s.replace('type.create(Minecraft.getInstance().level)','type.create(Minecraft.getInstance().level, net.minecraft.world.entity.EntitySpawnReason.LOAD)')
(D/'ACLegacyItemRenderer.java').write_text(s)
subprocess.run([sys.executable,'tools/client_port.py',str(D)],check=True)
