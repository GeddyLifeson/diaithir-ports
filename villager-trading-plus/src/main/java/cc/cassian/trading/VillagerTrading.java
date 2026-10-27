package cc.cassian.trading;

import cc.cassian.trading.config.ModConfig;
import cc.cassian.trading.helpers.ModHelpers;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class VillagerTrading {

    public static final String MOD_ID = "villager_trading_plus";
    public static final String MOD_NAME = "Villager Trading Plus";
    public static final Logger LOGGER = LogManager.getLogger(MOD_NAME);
    public static final ModConfig CONFIG = ModConfig.createToml(ModHelpers.configPath(), "", MOD_ID, ModConfig.class);

    public static void init() {
        LOGGER.info("Successfully initialized Villager Trading Plus. Villagers, just hand over the Emeralds!");
    }
}
