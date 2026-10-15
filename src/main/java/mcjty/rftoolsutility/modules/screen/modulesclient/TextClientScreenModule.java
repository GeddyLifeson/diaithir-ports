package mcjty.rftoolsutility.modules.screen.modulesclient;

import com.mojang.blaze3d.vertex.PoseStack;

import mcjty.rftoolsbase.api.screens.IClientScreenModule;
import mcjty.rftoolsbase.api.screens.IModuleRenderHelper;
import mcjty.rftoolsbase.api.screens.ITextRenderHelper;
import mcjty.rftoolsbase.api.screens.ModuleRenderInfo;
import mcjty.rftoolsbase.api.screens.data.IModuleData;
import mcjty.rftoolsbase.tools.ScreenTextHelper;
import mcjty.rftoolsutility.modules.screen.items.modules.TextModuleItem;
import mcjty.rftoolsutility.modules.screen.modules.TextScreenModule;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class TextClientScreenModule implements IClientScreenModule<IModuleData> {

    private final ITextRenderHelper cache = new ScreenTextHelper();


    public TextClientScreenModule() {
    }

    @Override
    public TransformMode getTransformMode(ItemStack moduleItem) {
        return cache.isLarge() ? TransformMode.TEXTLARGE : TransformMode.TEXT;
    }

    @Override
    public int getHeight(ItemStack moduleItem) {
        return cache.isLarge() ? 20 : 10;
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, IModuleRenderHelper renderHelper, Font fontRenderer, int currenty, IModuleData screenData, ModuleRenderInfo renderInfo) {
        TextScreenModule data = TextModuleItem.data(renderInfo.moduleStack);
        cache.setup(data.getLine(), 512, renderInfo);
        cache.align(data.getAlign());
        cache.large(data.isLarge());
        int y = cache.isLarge() ? (currenty / 2 + 1) : currenty;
        cache.renderText(poseStack, buffer, 0, y, data.getColor(), renderInfo);
    }

    @Override
    public void mouseClick(ItemStack moduleStack, Level world, int x, int y, boolean clicked) {

    }

    // @todo 1.21 how to handle cache.setDirty()???
//    public void setLine(String line) {
//        this.line = line;
//        cache.setDirty();
//    }
//
//    public void setColor(int color) {
//        this.color = color;
//        cache.setDirty();
//    }
//
//    public void setLarge(boolean large) {
//        cache.large(large);
//        cache.setDirty();
//    }

    @Override
    public boolean needsServerData() {
        return false;
    }
}
