package org.valkyrienskies.eureka.block

import com.mojang.serialization.MapCodec
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.SoundType
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.StateDefinition
import net.minecraft.world.level.block.state.properties.BlockStateProperties.POWER
import net.minecraft.world.level.material.MapColor
import net.minecraft.world.level.redstone.Orientation

class BallastBlock : Block(
    Properties.of().mapColor(MapColor.STONE)
        .sound(SoundType.STONE).strength(1.0f, 2.0f)
        // 26.2: the registry id must be stamped on Properties before the Block ctor runs.
        .setId(
            net.minecraft.resources.ResourceKey.create(
                net.minecraft.core.registries.Registries.BLOCK,
                net.minecraft.resources.Identifier.fromNamespaceAndPath(org.valkyrienskies.eureka.EurekaMod.MOD_ID, "ballast")
            )
        )
) {

    init {
        registerDefaultState(defaultBlockState().setValue(POWER, 0))
    }

    override fun codec(): MapCodec<out Block> = simpleCodec { BallastBlock() }

    override fun createBlockStateDefinition(builder: StateDefinition.Builder<Block, BlockState>) {
        super.createBlockStateDefinition(builder)
        builder.add(POWER)
    }

    override fun neighborChanged(
        state: BlockState,
        level: Level,
        pos: BlockPos,
        block: Block,
        orientation: Orientation?,
        isMoving: Boolean
    ) {
        super.neighborChanged(state, level, pos, block, orientation, isMoving)

        if (level as? ServerLevel == null) return

        val signal = level.getBestNeighborSignal(pos)
        level.setBlock(pos, state.setValue(POWER, signal), 2)
    }
}
