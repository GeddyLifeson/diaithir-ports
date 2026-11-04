package net.diaithir.druidry.ars.common.block.tile;

import net.diaithir.druidry.ars.ArsNouveau;
import net.diaithir.druidry.ars.api.source.SourcelinkEventQueue;
import net.diaithir.druidry.ars.common.datagen.BlockTagProvider;
import net.diaithir.druidry.ars.setup.registry.BlockRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockGrowFeatureEvent;
import net.neoforged.neoforge.event.level.block.CropGrowEvent;

@EventBusSubscriber(modid = ArsNouveau.HOST)
public class AgronomicSourcelinkTile extends SourcelinkTile {

    public AgronomicSourcelinkTile(BlockPos pos, BlockState state) {
        super(BlockRegistry.AGRONOMIC_SOURCELINK_TILE.get(), pos, state);
    }

    @Override
    public int getMaxSource() {
        return 1000;
    }

    @SubscribeEvent
    public static void cropGrow(CropGrowEvent.Post event) {
        int mana = 20;

        if (event.getLevel().getBlockState(event.getPos()).is(BlockTagProvider.MAGIC_PLANTS)) {
            mana += 25;
        }
        if (event.getLevel() instanceof Level)
            SourcelinkEventQueue.addManaEvent((Level) event.getLevel(), AgronomicSourcelinkTile.class, mana, event, event.getPos());
    }

    @SubscribeEvent
    public static void treeGrow(BlockGrowFeatureEvent event) {
        int mana = 50;
        if (event.getLevel().getBlockState(event.getPos()).is(BlockTagProvider.MAGIC_SAPLINGS)) {
            mana += 50;
        }
        if (event.getLevel() instanceof Level)
            SourcelinkEventQueue.addManaEvent((Level) event.getLevel(), AgronomicSourcelinkTile.class, mana, event, event.getPos());
    }

    @Override
    public boolean usesEventQueue() {
        return true;
    }

    @Override
    public int getTransferRate() {
        return 1000;
    }
}
