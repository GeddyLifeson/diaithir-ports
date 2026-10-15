package mcjty.rftoolsutility.setup;

import mcjty.lib.setup.DefaultModSetup;
import mcjty.lib.varia.SpawnCanceler;
import mcjty.rftoolsutility.RFToolsUtility;
import mcjty.rftoolsutility.compat.RFToolsDimCompat;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;

public class ModSetup extends DefaultModSetup {

    @Override
    public void init(FMLCommonSetupEvent e) {
        super.init(e);

        NeoForge.EVENT_BUS.register(new ForgeEventHandlers());
        SpawnCanceler.registerSpawnCanceler(ForgeEventHandlers::onEntitySpawnEvent);

        e.enqueueWork(() -> {
            CommandHandler.registerCommands();
        });
        RFToolsUtility.screenModuleRegistry.registerBuiltins();
    }

    @Override
    protected void setupModCompat() {
        RFToolsDimCompat.register();
    }
}
