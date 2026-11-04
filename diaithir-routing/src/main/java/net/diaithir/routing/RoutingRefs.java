package net.diaithir.routing;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;

/** Lazy lookups of this mod's registered objects by registry id, so block and item classes never touch registration holders directly. */
public final class RoutingRefs {
    public static final String MOD_ID = "diaithirrouting";

    private static final String FILTER_DATA = "filter_data";
    private static final String ROUTER_BE = "router";
    private static final String LASER_BE = "laser_node";
    private static final String HOPPER_BE = "hopper";
    private static final String ROUTER_LINK = "router_link";
    private static final String RANGE_CARD = "range_card";

    private RoutingRefs() {
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }

    @SuppressWarnings("unchecked")
    public static DataComponentType<FilterData> filterData() {
        return (DataComponentType<FilterData>) BuiltInRegistries.DATA_COMPONENT_TYPE.getValue(id(FILTER_DATA));
    }

    @SuppressWarnings("unchecked")
    public static BlockEntityType<RouterBlockEntity> routerBe() {
        return (BlockEntityType<RouterBlockEntity>) BuiltInRegistries.BLOCK_ENTITY_TYPE.getValue(id(ROUTER_BE));
    }

    @SuppressWarnings("unchecked")
    public static BlockEntityType<LaserNodeBlockEntity> laserBe() {
        return (BlockEntityType<LaserNodeBlockEntity>) BuiltInRegistries.BLOCK_ENTITY_TYPE.getValue(id(LASER_BE));
    }

    @SuppressWarnings("unchecked")
    public static BlockEntityType<ModHopperBlockEntity> hopperBe() {
        return (BlockEntityType<ModHopperBlockEntity>) BuiltInRegistries.BLOCK_ENTITY_TYPE.getValue(id(HOPPER_BE));
    }

    public static boolean isRouterLink(ItemStack stack) {
        return BuiltInRegistries.ITEM.getKey(stack.getItem()).equals(id(ROUTER_LINK));
    }

    public static boolean isRangeCard(ItemStack stack) {
        return BuiltInRegistries.ITEM.getKey(stack.getItem()).equals(id(RANGE_CARD));
    }
}
