package mcjty.rftoolspower.modules.generator.client;

import mcjty.lib.container.GenericContainer;
import mcjty.lib.gui.GenericGuiContainer;
import mcjty.lib.gui.Window;
import mcjty.lib.gui.widgets.EnergyBar;
import mcjty.lib.gui.widgets.ImageChoiceLabel;
import mcjty.rftoolspower.RFToolsPower;
import mcjty.rftoolspower.modules.generator.CoalGeneratorModule;
import mcjty.rftoolspower.modules.generator.blocks.CoalGeneratorTileEntity;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;


public class GuiCoalGenerator extends GenericGuiContainer<CoalGeneratorTileEntity, GenericContainer> {

    private EnergyBar energyBar;

    public GuiCoalGenerator(GenericContainer container, Inventory inventory, Component title) {
        super(container, inventory, title, CoalGeneratorModule.COALGENERATOR.block().get().getManualEntry());
    }

    public static void register(RegisterMenuScreensEvent event) {
        event.register(CoalGeneratorModule.CONTAINER_COALGENERATOR.get(), GuiCoalGenerator::new);
    }

    @Override
    public void init() {
        window = new Window(this, getBE(), Identifier.fromNamespaceAndPath(RFToolsPower.MODID, "gui/coalgenerator.gui"));
        super.init();

        initializeFields();
    }

    private void initializeFields() {
        energyBar = window.findChild("energybar");
    }

    private void updateFields() {
        if (window == null) {
            return;
        }
        ((ImageChoiceLabel) window.findChild("redstone")).setCurrentChoice(getBE().getRSMode().ordinal());
        updateEnergyBar(energyBar);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
        super.extractBackground(graphics, mouseX, mouseY, partialTicks);
        updateFields();
        drawWindow(graphics, partialTicks, mouseX, mouseY);
    }
}
