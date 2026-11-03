package org.valkyrienskies.tournament.blocks.explosive

import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.Explosion
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.FireBlock
import net.minecraft.world.level.block.state.BlockState
import org.valkyrienskies.tournament.neighborBlocks
import org.valkyrienskies.tournament.util.getHeat

abstract class AbstractReactiveExplosiveBlock: AbstractExplosiveBlock() {

    override fun onExplosionHit(
        state: BlockState,
        level: ServerLevel,
        pos: BlockPos,
        explosion: Explosion,
        onHit: java.util.function.BiConsumer<net.minecraft.world.item.ItemStack, BlockPos>
    ) {
        ignite(level, pos)
        super.onExplosionHit(state, level, pos, explosion, onHit)
    }

    override fun explodeUsingFlint(level: ServerLevel, pos: BlockPos) =
        true

    private fun checkNeighbors(level: ServerLevel, pos: BlockPos,) {
        val exp = pos.neighborBlocks()
            .flatMap { it.neighborBlocks() + it }
            .any {
                val bs = level.getBlockState(it)
                bs.block is FireBlock || bs.getHeat() > 0
            }

        if (exp) {
            ignite(level, pos)
        }
    }

    override fun onPlace(state: BlockState, level: Level, pos: BlockPos, oldState: BlockState, isMoving: Boolean) {
        super.onPlace(state, level, pos, oldState, isMoving)
        if (level is ServerLevel)
            checkNeighbors(level, pos)
    }

    override fun neighborChanged(
        state: BlockState,
        level: Level,
        pos: BlockPos,
        block: Block,
        orientation: net.minecraft.world.level.redstone.Orientation?,
        isMoving: Boolean
    ) {
        if (level is ServerLevel)
            checkNeighbors(level, pos)
        super.neighborChanged(state, level, pos, block, orientation, isMoving)
    }

}