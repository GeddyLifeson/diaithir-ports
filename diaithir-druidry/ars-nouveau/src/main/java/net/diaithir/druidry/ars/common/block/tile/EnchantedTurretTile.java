package net.diaithir.druidry.ars.common.block.tile;

import net.diaithir.druidry.ars.setup.registry.BlockRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;


public class EnchantedTurretTile extends BasicSpellTurretTile {

    public EnchantedTurretTile(BlockPos pos, BlockState state) {
        super(BlockRegistry.ENCHANTED_SPELL_TURRET_TYPE.get(), pos, state);
    }

    @Override
    public int getManaCost() {
        return this.spellCaster.getSpell().getCost() / 2;
    }




}
