package net.diaithir.ships.yard

import com.mojang.serialization.MapCodec
import net.minecraft.core.BlockPos
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.network.chat.Component
import net.minecraft.resources.Identifier
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.BaseEntityBlock
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.RenderShape
import net.minecraft.world.level.block.SoundType
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityTicker
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.material.MapColor
import net.minecraft.world.phys.BlockHitResult
import org.valkyrienskies.eureka.registry.DeferredRegister
import net.diaithir.yards.BuildState
import net.diaithir.yards.YardRegistry

/** Crane tiers: speed multiplies stage work (product of cranes in reach), lift_kg gates heavy parts. */
class CraneBlock(props: Properties, val speed: Double, val liftKg: Double) : Block(props) {
    override fun codec(): MapCodec<out Block> = MapCodec.unit(this)
}

class SlipwayControllerBlock(props: Properties) : BaseEntityBlock(props) {
    override fun codec(): MapCodec<out BaseEntityBlock> = MapCodec.unit(this)
    override fun getRenderShape(state: BlockState) = RenderShape.MODEL
    override fun newBlockEntity(pos: BlockPos, state: BlockState): BlockEntity = SlipwayBlockEntity(pos, state)

    override fun <T : BlockEntity> getTicker(level: Level, state: BlockState, type: BlockEntityType<T>): BlockEntityTicker<T>? =
        BlockEntityTicker<T> { lvl, _, _, be -> if (!lvl.isClientSide && be is SlipwayBlockEntity) be.serverTick() }

    override fun useItemOn(stack: ItemStack, state: BlockState, level: Level, pos: BlockPos, player: Player,
                           hand: InteractionHand, hit: BlockHitResult): InteractionResult {
        if (level !is ServerLevel) return InteractionResult.SUCCESS
        val be = level.getBlockEntity(pos) as? SlipwayBlockEntity ?: return InteractionResult.PASS
        val part = YardRegistry.part(BuiltInRegistries.ITEM.getKey(stack.item).toString()).orElse(null) ?: return InteractionResult.PASS
        if (be.build == null) { player.sendSystemMessage(Component.literal("Choose a yard first (use the controller with an empty hand).")); return InteractionResult.CONSUME }
        val r = be.install(part)
        if (r == BuildState.InstallResult.OK) { if (!player.hasInfiniteMaterials()) stack.shrink(1) }
        player.sendSystemMessage(Component.literal("${part.item()}: $r"))
        return InteractionResult.CONSUME
    }

    override fun useWithoutItem(state: BlockState, level: Level, pos: BlockPos, player: Player, hit: BlockHitResult): InteractionResult {
        if (level !is ServerLevel) return InteractionResult.SUCCESS
        val be = level.getBlockEntity(pos) as? SlipwayBlockEntity ?: return InteractionResult.PASS
        player.sendSystemMessage(Component.literal(be.onUse(player.isShiftKeyDown)))
        return InteractionResult.CONSUME
    }
}

object ShipYardBlocks {
    const val ID = "vs_yard"
    val BLOCKS = DeferredRegister.create(ID, Registries.BLOCK)
    val ITEMS = DeferredRegister.create(ID, Registries.ITEM)
    val BE_TYPES = DeferredRegister.create(ID, Registries.BLOCK_ENTITY_TYPE)

    /** Part items, crafted elsewhere (foundry / crafting stations). Stats live in data/vs_yard/yard_parts. */
    val PART_IDS = listOf("keel_beam", "hull_frame", "hull_plank_set", "deck_plating", "steam_engine", "rudder",
        "helm_kit", "anchor_kit", "floater_kit", "balloon_kit", "thruster_kit", "propeller_kit", "fuel_tank_kit", "ballast_plate")

    val SLIPWAY = BLOCKS.register("slipway_controller") {
        SlipwayControllerBlock(DeferredRegister.blockProps().mapColor(MapColor.METAL).strength(3.5F).sound(SoundType.METAL))
    }
    val TREADWHEEL_CRANE = BLOCKS.register("treadwheel_crane") {
        CraneBlock(DeferredRegister.blockProps().mapColor(MapColor.WOOD).strength(2.5F).sound(SoundType.WOOD), 1.5, 2_000.0)
    }
    val STEAM_CRANE = BLOCKS.register("steam_crane") {
        CraneBlock(DeferredRegister.blockProps().mapColor(MapColor.METAL).strength(4F).sound(SoundType.METAL), 2.0, 20_000.0)
    }
    val GANTRY_CRANE = BLOCKS.register("gantry_crane") {
        CraneBlock(DeferredRegister.blockProps().mapColor(MapColor.METAL).strength(5F).sound(SoundType.NETHERITE_BLOCK), 3.0, 100_000.0)
    }

    val SLIPWAY_BE = BE_TYPES.register("slipway_controller") { BlockEntityType(::SlipwayBlockEntity, setOf(SLIPWAY.get())) }

    val TABS = DeferredRegister.create(ID, Registries.CREATIVE_MODE_TAB)
    val TAB = TABS.register("ship_yard") {
        net.minecraft.world.item.CreativeModeTab.builder(net.minecraft.world.item.CreativeModeTab.Row.TOP, 0)
            .title(Component.translatable("itemGroup.vs_yard.ship_yard"))
            .icon { ItemStack(SLIPWAY.get().asItem()) }
            .displayItems { _, out -> ITEMS.forEach { out.accept(it.get()) } }
            .build()
    }

    fun register() {
        BLOCKS.forEach { b -> ITEMS.register(b.name) { BlockItem(b.get(), DeferredRegister.itemProps().useBlockDescriptionPrefix()) } }
        PART_IDS.forEach { n -> ITEMS.register(n) { Item(DeferredRegister.itemProps()) } }
        BLOCKS.applyAll(); ITEMS.applyAll(); BE_TYPES.applyAll(); TABS.applyAll()
    }

    /** Crane in reach of a site: speed multiplier (product) and best lift. */
    fun craneAt(level: Level, pos: BlockPos, reach: Int = 8): Pair<Double, Double> {
        var speed = 1.0; var lift = 0.0
        for (p in BlockPos.betweenClosed(pos.offset(-reach, -3, -reach), pos.offset(reach, 3, reach))) {
            val b = level.getBlockState(p).block
            if (b is CraneBlock) { speed *= b.speed; lift = maxOf(lift, b.liftKg) }
        }
        return Pair(speed, lift)
    }
}
