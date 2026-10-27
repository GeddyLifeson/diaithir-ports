package org.diaithir.connected.registries;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.diaithir.connected.CreateConnected;
import org.diaithir.connected.content.brake.BrakeBlock;
import org.diaithir.connected.content.crossconnector.CrossConnectorBlock;
import org.diaithir.connected.content.centrifugalclutch.CentrifugalClutchBlock;
import org.diaithir.connected.content.freewheelclutch.FreewheelClutchBlock;
import org.diaithir.connected.content.overstressclutch.OverstressClutchBlock;
import org.diaithir.connected.content.crossconnector.EncasedCrossConnectorBlock;
import com.zurrtum.create.AllBlocks;
import org.diaithir.connected.content.brassgearbox.BrassGearboxBlock;
import org.diaithir.connected.content.invertedclutch.InvertedClutchBlock;
import org.diaithir.connected.content.invertedgearshift.InvertedGearshiftBlock;
import org.diaithir.connected.content.parallelgearbox.ParallelGearboxBlock;
import org.diaithir.connected.content.sixwaygearbox.SixWayGearboxBlock;

import java.util.function.Function;

public class CCBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(CreateConnected.MODID);

    private static Properties box(MapColor colour) {
        return Properties.ofFullCopy(Blocks.ANDESITE).mapColor(colour).noOcclusion();
    }

    public static final DeferredBlock<ParallelGearboxBlock> PARALLEL_GEARBOX = reg("parallel_gearbox", ParallelGearboxBlock::new, box(MapColor.PODZOL).pushReaction(PushReaction.PUSH_ONLY));
    public static final DeferredBlock<SixWayGearboxBlock> SIX_WAY_GEARBOX = reg("six_way_gearbox", SixWayGearboxBlock::new, box(MapColor.PODZOL).pushReaction(PushReaction.PUSH_ONLY));
    public static final DeferredBlock<BrassGearboxBlock> BRASS_GEARBOX = reg("brass_gearbox", BrassGearboxBlock::new, box(MapColor.TERRACOTTA_BROWN).pushReaction(PushReaction.PUSH_ONLY));
    public static final DeferredBlock<InvertedClutchBlock> INVERTED_CLUTCH = reg("inverted_clutch", InvertedClutchBlock::new, box(MapColor.PODZOL));
    public static final DeferredBlock<InvertedGearshiftBlock> INVERTED_GEARSHIFT = reg("inverted_gearshift", InvertedGearshiftBlock::new, box(MapColor.PODZOL));
    public static final DeferredBlock<BrakeBlock> BRAKE = reg("brake", BrakeBlock::new, box(MapColor.PODZOL));

    public static final DeferredBlock<CrossConnectorBlock> CROSS_CONNECTOR = reg("cross_connector", CrossConnectorBlock::new, box(MapColor.PODZOL));
    public static final DeferredBlock<EncasedCrossConnectorBlock> ANDESITE_ENCASED_CROSS_CONNECTOR = reg("andesite_encased_cross_connector",
        p -> new EncasedCrossConnectorBlock(p, () -> AllBlocks.ANDESITE_CASING), box(MapColor.PODZOL));
    public static final DeferredBlock<EncasedCrossConnectorBlock> BRASS_ENCASED_CROSS_CONNECTOR = reg("brass_encased_cross_connector",
        p -> new EncasedCrossConnectorBlock(p, () -> AllBlocks.BRASS_CASING), box(MapColor.TERRACOTTA_BROWN));

    public static final DeferredBlock<OverstressClutchBlock> OVERSTRESS_CLUTCH = reg("overstress_clutch", OverstressClutchBlock::new, box(MapColor.PODZOL));
    public static final DeferredBlock<FreewheelClutchBlock> FREEWHEEL_CLUTCH = reg("freewheel_clutch", FreewheelClutchBlock::new, box(MapColor.PODZOL));
    public static final DeferredBlock<CentrifugalClutchBlock> CENTRIFUGAL_CLUTCH = reg("centrifugal_clutch", CentrifugalClutchBlock::new, box(MapColor.PODZOL));

    private static <B extends Block> DeferredBlock<B> reg(String id, Function<Properties, B> factory, Properties props) {
        return BLOCKS.registerBlock(id, factory, () -> props);
    }
}
