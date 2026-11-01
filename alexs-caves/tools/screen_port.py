#!/usr/bin/env python3
"""quarantine/client/gui Spelunkery screen+button -> 26.1 GuiGraphicsExtractor screens. Idempotent."""
import re, pathlib, subprocess, sys
Q=pathlib.Path('quarantine/client/gui'); D=pathlib.Path('src/main/java/com/github/alexmodguy/alexscaves/client/gui'); D.mkdir(parents=True,exist_ok=True)
GG='net.minecraft.client.gui.GuiGraphicsExtractor'
def common(s):
    s=s.replace('net.minecraft.client.gui.GuiGraphics;',GG+';')
    s=re.sub(r'\bGuiGraphics\b','GuiGraphicsExtractor',s)
    s=s.replace('import net.minecraft.client.gui.GuiGraphicsExtractor;','import '+GG+';') if False else s
    s=re.sub(r'guiGraphics\.blit\((\w+), ([^;,]+), ([^;,]+), ([^;,]+), ([^;,]+), ([^;,]+), ([^;,]+)\);',lambda m:'guiGraphics.blit(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, %s, %s, %s, (float)(%s), (float)(%s), %s, %s, 256, 256);'%(m.group(1),m.group(2),m.group(3),m.group(4),m.group(5),m.group(6),m.group(7)),s)
    s=s.replace('guiGraphics.drawString(','guiGraphics.text(')
    s=s.replace('.pose().pushPose()','.pose().pushMatrix()').replace('.pose().popPose()','.pose().popMatrix()')
    s=s.replace('Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(false)','Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false)')
    s=s.replace('Minecraft.getInstance().getTimer().getRealtimeDeltaTicks()','Minecraft.getInstance().getDeltaTracker().getRealtimeDeltaTicks()')
    s=s.replace('net.minecraft.resources.ResourceLocation','net.minecraft.resources.Identifier').replace('ResourceLocation','Identifier')
    return s
b=common((Q/'SpelunkeryTableWordButton.java').read_text())
b=b.replace('public void renderWidget(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick)','public void extractWidgetRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick)')
b=b.replace('@Override\n    public void renderWidget','@Override\n    public void extractWidgetRenderState')
b=b.replace('public void onClick(double x, double y) {','@Override\n    public void onClick(net.minecraft.client.input.MouseButtonEvent event, boolean doubleClick) {')
b=b.replace('public void playDownSound(SoundManager soundManager)','@Override\n    public void playDownSound(SoundManager soundManager)')
b=b.replace('import net.minecraft.util.FastColor;\n','')
(D/'SpelunkeryTableWordButton.java').write_text(b)
s=common((Q/'SpelunkeryTableScreen.java').read_text())
# render pipeline
i=s.index('    public void render(GuiGraphicsExtractor guiGraphics, int x, int y, float partialTick) {'); j=s.index('    private void renderDescText')
s=s[:i]+'''    @Override
    public void extractBackground(GuiGraphicsExtractor guiGraphics, int x, int y, float partialTick) {
        super.extractBackground(guiGraphics, x, y, partialTick);
        this.renderBg(guiGraphics, partialTick, x, y);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor guiGraphics, int x, int y, float partialTick) {
        super.extractRenderState(guiGraphics, x, y, partialTick);
        this.renderMagnify(guiGraphics, partialTick);
        this.renderDescText(guiGraphics);
        this.renderTabletText(guiGraphics);
    }

'''+s[j:]
s=s.replace('protected void renderTooltip(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {','protected void extractTooltip(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {')
s=s.replace('guiGraphics.renderTooltip(font, ','guiGraphics.setTooltipForNextFrame(font, ')
s=s.replace('super.renderTooltip(guiGraphics, mouseX, mouseY);','super.extractTooltip(guiGraphics, mouseX, mouseY);')
# magnify
i=s.index('    private void renderMagnify('); j=s.index('    protected void renderBg(')
s=s[:i]+'''    private void renderMagnify(GuiGraphicsExtractor guiGraphics, float partialTick) {
        float actualPartialTick = Minecraft.getInstance().getDeltaTracker().getRealtimeDeltaTicks();
        int lerpX = (int) getMagnifyPosX(actualPartialTick);
        int lerpY = (int) getMagnifyPosY(actualPartialTick);
        guiGraphics.blit(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, WIDGETS_TEXTURE, lerpX, lerpY, 0F, 14F, 38, 38, 256, 256);
    }

'''+s[j:]
s=s.replace('protected void renderBg(GuiGraphicsExtractor guiGraphics, float f, int x, int y) {','protected void renderBg(GuiGraphicsExtractor guiGraphics, float f, int x, int y) {')
s=s.replace('for (Renderable renderable : renderables) {\n                if (renderable instanceof SpelunkeryTableWordButton tableWordButton) {','for (SpelunkeryTableWordButton tableWordButton : wordButtons) {\n                {')
# input
s=s.replace('public boolean mouseClicked(double mouseX, double mouseY, int button) {\n        boolean prev = super.mouseClicked(mouseX, mouseY, button);','public boolean mouseClicked(net.minecraft.client.input.MouseButtonEvent event, boolean doubleClick) {\n        boolean prev = super.mouseClicked(event, doubleClick);')
s=s.replace('public boolean mouseDragged(double width, double height, int button, double x, double y) {\n        boolean prev = super.mouseDragged(width, height, button, x, y);','public boolean mouseDragged(net.minecraft.client.input.MouseButtonEvent event, double dx, double dy) {\n        double width = event.x();\n        double height = event.y();\n        boolean prev = super.mouseDragged(event, dx, dy);')
s=s.replace('public boolean mouseReleased(double mouseX, double mouseY, int button) {\n        draggingMagnify = false;\n        return super.mouseReleased(mouseX, mouseY, button);','public boolean mouseReleased(net.minecraft.client.input.MouseButtonEvent event) {\n        draggingMagnify = false;\n        return super.mouseReleased(event);')
s=s.replace('protected void renderLabels(GuiGraphicsExtractor guiGraphics, int x, int y) {','protected void extractLabels(GuiGraphicsExtractor guiGraphics, int x, int y) {')
s=s.replace('.withFont(Identifier.fromNamespaceAndPath("minecraft", "alt"))','.withFont(new net.minecraft.network.chat.FontDescription.Resource(Identifier.fromNamespaceAndPath("minecraft", "alt")))')
s=s.replace('super(menu, inventory, name);\n        this.imageWidth = 208;\n        this.imageHeight = 256;','super(menu, inventory, name, 208, 256);')
s=s.replace('PacketDistributor.sendToServer(','net.neoforged.neoforge.client.network.ClientPacketDistributor.sendToServer(')
s=s.replace('biomeResourceKey.location()','biomeResourceKey.identifier()')
(D/'SpelunkeryTableScreen.java').write_text(s)
