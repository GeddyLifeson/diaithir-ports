package org.valkyrienskies.eureka

import net.minecraft.core.registries.Registries
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.FireBlock
import net.minecraft.world.level.block.SoundType
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.material.MapColor
import org.valkyrienskies.eureka.block.AnchorBlock
import org.valkyrienskies.eureka.block.BallastBlock
import org.valkyrienskies.eureka.block.BalloonBlock
import org.valkyrienskies.eureka.block.EngineBlock
import org.valkyrienskies.eureka.block.FloaterBlock
import org.valkyrienskies.eureka.block.ShipHelmBlock
import org.valkyrienskies.eureka.block.WoodType
import org.valkyrienskies.eureka.registry.DeferredRegister
import org.valkyrienskies.mod.common.hooks.VSGameEvents

@Suppress("unused")
object EurekaBlocks {
    internal val BLOCKS = DeferredRegister.create(EurekaMod.MOD_ID, Registries.BLOCK)

    // 26.2: every Block/Item must have its registry id stamped on its Properties BEFORE the
    // constructor runs ("Block id not set" NPE at RegisterEvent otherwise).
    private fun BlockBehaviour.Properties.withId(name: String): BlockBehaviour.Properties =
        setId(
            net.minecraft.resources.ResourceKey.create(
                Registries.BLOCK,
                net.minecraft.resources.Identifier.fromNamespaceAndPath(EurekaMod.MOD_ID, name)
            )
        )

    val ANCHOR = BLOCKS.register("anchor", ::AnchorBlock)
    val ENGINE = BLOCKS.register("engine", ::EngineBlock)
    val FLOATER = BLOCKS.register("floater", ::FloaterBlock)
    val BALLAST = BLOCKS.register("ballast", ::BallastBlock)

    // region Ship Helms
    val OAK_SHIP_HELM = BLOCKS.register("oak_ship_helm") {
        ShipHelmBlock(
            BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.5F).sound(SoundType.WOOD).withId("oak_ship_helm"),
            WoodType.OAK
        )
    }
    val SPRUCE_SHIP_HELM = BLOCKS.register("spruce_ship_helm") {
        ShipHelmBlock(
            BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.5F).sound(SoundType.WOOD).withId("spruce_ship_helm"),
            WoodType.SPRUCE
        )
    }
    val BIRCH_SHIP_HELM = BLOCKS.register("birch_ship_helm") {
        ShipHelmBlock(
            BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.5F).sound(SoundType.WOOD).withId("birch_ship_helm"),
            WoodType.BIRCH
        )
    }
    val JUNGLE_SHIP_HELM = BLOCKS.register("jungle_ship_helm") {
        ShipHelmBlock(
            BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.5F).sound(SoundType.WOOD).withId("jungle_ship_helm"),
            WoodType.JUNGLE
        )
    }
    val ACACIA_SHIP_HELM = BLOCKS.register("acacia_ship_helm") {
        ShipHelmBlock(
            BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.5F).sound(SoundType.WOOD).withId("acacia_ship_helm"),
            WoodType.ACACIA
        )
    }
    val DARK_OAK_SHIP_HELM = BLOCKS.register("dark_oak_ship_helm") {
        ShipHelmBlock(
            BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.5F).sound(SoundType.WOOD).withId("dark_oak_ship_helm"),
            WoodType.DARK_OAK
        )
    }
    val CRIMSON_SHIP_HELM = BLOCKS.register("crimson_ship_helm") {
        ShipHelmBlock(
            BlockBehaviour.Properties.of().mapColor(MapColor.NETHER).strength(2.5F).sound(SoundType.STEM).withId("crimson_ship_helm"),
            WoodType.CRIMSON
        )
    }
    val WARPED_SHIP_HELM = BLOCKS.register("warped_ship_helm") {
        ShipHelmBlock(
            BlockBehaviour.Properties.of().mapColor(MapColor.NETHER).strength(2.5F).sound(SoundType.STEM).withId("warped_ship_helm"),
            WoodType.WARPED
        )
    }
    // endregion

    // region Balloons
    val BALLOON = BLOCKS.register("balloon") {
        BalloonBlock(
            BlockBehaviour.Properties.of().mapColor(MapColor.WOOL).strength(0.8F).sound(SoundType.WOOL).withId("balloon")
        )
    }
    val WHITE_BALLOON = BLOCKS.register("white_balloon") {
        BalloonBlock(
            BlockBehaviour.Properties.of().mapColor(MapColor.SNOW).strength(0.8F).sound(SoundType.WOOL).withId("white_balloon")
        )
    }
    val LIGHT_GRAY_BALLOON = BLOCKS.register("light_gray_balloon") {
        BalloonBlock(
            BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(0.8F).sound(SoundType.WOOL).withId("light_gray_balloon")
        )
    }
    val GRAY_BALLOON = BLOCKS.register("gray_balloon") {
        BalloonBlock(
            BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_GRAY).strength(0.8F).sound(SoundType.WOOL).withId("gray_balloon")
        )
    }
    val BLACK_BALLOON = BLOCKS.register("black_balloon") {
        BalloonBlock(
            BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLACK).strength(0.8F).sound(SoundType.WOOL).withId("black_balloon")
        )
    }
    val RED_BALLOON = BLOCKS.register("red_balloon") {
        BalloonBlock(
            BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_RED).strength(0.8F).sound(SoundType.WOOL).withId("red_balloon")
        )
    }
    val ORANGE_BALLOON = BLOCKS.register("orange_balloon") {
        BalloonBlock(
            BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_ORANGE).strength(0.8F).sound(SoundType.WOOL).withId("orange_balloon")
        )
    }
    val YELLOW_BALLOON = BLOCKS.register("yellow_balloon") {
        BalloonBlock(
            BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_YELLOW).strength(0.8F).sound(SoundType.WOOL).withId("yellow_balloon")
        )
    }
    val LIME_BALLOON = BLOCKS.register("lime_balloon") {
        BalloonBlock(
            BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_LIGHT_GREEN).strength(0.8F).sound(SoundType.WOOL).withId("lime_balloon")
        )
    }
    val GREEN_BALLOON = BLOCKS.register("green_balloon") {
        BalloonBlock(
            BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_GREEN).strength(0.8F).sound(SoundType.WOOL).withId("green_balloon")
        )
    }
    val LIGHT_BLUE_BALLOON = BLOCKS.register("light_blue_balloon") {
        BalloonBlock(
            BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_LIGHT_BLUE).strength(0.8F).sound(SoundType.WOOL).withId("light_blue_balloon")
        )
    }
    val CYAN_BALLOON = BLOCKS.register("cyan_balloon") {
        BalloonBlock(
            BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_CYAN).strength(0.8F).sound(SoundType.WOOL).withId("cyan_balloon")
        )
    }
    val BLUE_BALLOON = BLOCKS.register("blue_balloon") {
        BalloonBlock(
            BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLUE).strength(0.8F).sound(SoundType.WOOL).withId("blue_balloon")
        )
    }
    val PURPLE_BALLOON = BLOCKS.register("purple_balloon") {
        BalloonBlock(
            BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PURPLE).strength(0.8F).sound(SoundType.WOOL).withId("purple_balloon")
        )
    }
    val MAGENTA_BALLOON = BLOCKS.register("magenta_balloon") {
        BalloonBlock(
            BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_MAGENTA).strength(0.8F).sound(SoundType.WOOL).withId("magenta_balloon")
        )
    }
    val PINK_BALLOON = BLOCKS.register("pink_balloon") {
        BalloonBlock(
            BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PINK).strength(0.8F).sound(SoundType.WOOL).withId("pink_balloon")
        )
    }
    val BROWN_BALLOON = BLOCKS.register("brown_balloon") {
        BalloonBlock(
            BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BROWN).strength(0.8F).sound(SoundType.WOOL).withId("brown_balloon")
        )
    }
    // endregion

    fun register() {
        BLOCKS.applyAll()

        VSGameEvents.registriesCompleted.on { _, _ ->
            makeFlammables()
        }
    }

    // region Flammables
    // TODO make this part of the registration sequence
    fun flammableBlock(block: Block, flameOdds: Int, burnOdds: Int) {
        val fire = Blocks.FIRE as FireBlock
        fire.setFlammable(block, flameOdds, burnOdds)
    }

    fun makeFlammables() {
        flammableBlock(OAK_SHIP_HELM.get(), 5, 20)
        flammableBlock(SPRUCE_SHIP_HELM.get(), 5, 20)
        flammableBlock(BIRCH_SHIP_HELM.get(), 5, 20)
        flammableBlock(JUNGLE_SHIP_HELM.get(), 5, 20)
        flammableBlock(ACACIA_SHIP_HELM.get(), 5, 20)
        flammableBlock(DARK_OAK_SHIP_HELM.get(), 5, 20)
        flammableBlock(BALLOON.get(), 30, 60)
        flammableBlock(WHITE_BALLOON.get(), 30, 60)
        flammableBlock(LIGHT_GRAY_BALLOON.get(), 30, 60)
        flammableBlock(GRAY_BALLOON.get(), 30, 60)
        flammableBlock(BLACK_BALLOON.get(), 30, 60)
        flammableBlock(RED_BALLOON.get(), 30, 60)
        flammableBlock(ORANGE_BALLOON.get(), 30, 60)
        flammableBlock(YELLOW_BALLOON.get(), 30, 60)
        flammableBlock(LIME_BALLOON.get(), 30, 60)
        flammableBlock(GREEN_BALLOON.get(), 30, 60)
        flammableBlock(LIGHT_BLUE_BALLOON.get(), 30, 60)
        flammableBlock(CYAN_BALLOON.get(), 30, 60)
        flammableBlock(BLUE_BALLOON.get(), 30, 60)
        flammableBlock(PURPLE_BALLOON.get(), 30, 60)
        flammableBlock(MAGENTA_BALLOON.get(), 30, 60)
        flammableBlock(PINK_BALLOON.get(), 30, 60)
        flammableBlock(BROWN_BALLOON.get(), 30, 60)
        flammableBlock(FLOATER.get(), 5, 20)
    }
    // endregion

    // Blocks should also be registered as items, if you want them to be able to be held
    // aka all blocks
    fun registerItems(items: DeferredRegister<Item>) {
        BLOCKS.forEach {
            items.register(it.name) {
                BlockItem(
                    it.get(),
                    Item.Properties()
                        // 26.2: items need their id pre-set too, and BlockItems must opt into the
                        // "block." translation-key prefix (no longer automatic).
                        .setId(
                            net.minecraft.resources.ResourceKey.create(
                                Registries.ITEM,
                                net.minecraft.resources.Identifier.fromNamespaceAndPath(EurekaMod.MOD_ID, it.name)
                            )
                        )
                        .useBlockDescriptionPrefix()
                )
            }
        }
    }
}
