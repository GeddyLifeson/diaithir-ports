package cc.cassian.trading.helpers;

//? fabric
/*import net.fabricmc.loader.api.FabricLoader;*/
//? neoforge
import net.neoforged.fml.loading.FMLPaths;

import java.nio.file.Path;

public class ModHelpers {

    public static Path configPath() {
        //? neoforge
        return FMLPaths.CONFIGDIR.get();
        //? fabric
        /*return FabricLoader.getInstance().getConfigDir();*/
    }
}
