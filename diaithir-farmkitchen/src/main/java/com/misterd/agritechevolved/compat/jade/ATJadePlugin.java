package com.misterd.agritechevolved.compat.jade;

import com.misterd.agritechevolved.block.custom.AdvancedPlanterBlock;
import com.misterd.agritechevolved.block.custom.PlanterBlock;
import com.misterd.agritechevolved.blockentity.custom.AdvancedPlanterBlockEntity;
import com.misterd.agritechevolved.blockentity.custom.PlanterBlockEntity;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

@WailaPlugin
public class ATJadePlugin implements IWailaPlugin {
    public void register(IWailaCommonRegistration registration) {
        registration.registerBlockDataProvider(PlanterProvider.INSTANCE, PlanterBlockEntity.class);
        registration.registerBlockDataProvider(PlanterProvider.INSTANCE, AdvancedPlanterBlockEntity.class);
    }

    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(PlanterClientProvider.INSTANCE, PlanterBlock.class);
        registration.registerBlockComponent(PlanterClientProvider.INSTANCE, AdvancedPlanterBlock.class);
    }
}
