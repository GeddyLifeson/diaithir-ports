package org.diaithir.diesel.registries;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.diaithir.diesel.CreateDieselGenerators;
import org.diaithir.diesel.content.DieselEngineBlock;

public class CDGBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(CreateDieselGenerators.MODID);

    public static final DeferredBlock<DieselEngineBlock> DIESEL_ENGINE = BLOCKS.registerBlock("diesel_engine", DieselEngineBlock::new,
        () -> Properties.ofFullCopy(Blocks.IRON_BLOCK).mapColor(MapColor.METAL).noOcclusion().forceSolidOn());

    public static final DeferredBlock<org.diaithir.diesel.content.canister.CanisterBlock> CANISTER = BLOCKS.registerBlock("canister",
        org.diaithir.diesel.content.canister.CanisterBlock::new, () -> Properties.ofFullCopy(Blocks.IRON_BLOCK).mapColor(MapColor.METAL).noOcclusion().strength(2f));

    public static final DeferredBlock<org.diaithir.diesel.content.burner.BurnerBlock> BURNER = BLOCKS.registerBlock("burner",
        org.diaithir.diesel.content.burner.BurnerBlock::new, () -> Properties.ofFullCopy(Blocks.IRON_BLOCK).mapColor(MapColor.METAL).noOcclusion().lightLevel(st -> st.getValue(org.diaithir.diesel.content.burner.BurnerBlock.LIT) ? 12 : 0));

    public static final DeferredBlock<org.diaithir.diesel.content.modular.ModularDieselEngineBlock> MODULAR_DIESEL_ENGINE = BLOCKS.registerBlock("modular_diesel_engine",
        org.diaithir.diesel.content.modular.ModularDieselEngineBlock::new, () -> Properties.ofFullCopy(Blocks.IRON_BLOCK).mapColor(MapColor.METAL).noOcclusion().forceSolidOn());
}
