package net.diaithir.yards;

import net.diaithir.carts.Gauge;
import net.diaithir.carts.GaugeData;
import net.diaithir.carts.RailSpeed;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.BaseRailBlock;

/**
 * A finished vehicle. Used on a rail it becomes the matching existing cart entity, so the diaithir-carts coupling, speed tiers and stations apply:
 * locomotives are furnace carts (fuelled), freight is chest or hopper carts (cargo), passenger cars and cranes are plain carts (riders).
 * Used on a yard controller it chooses that design (handled by the controller).
 */
public final class RollingStockItem extends Item {
    private final String vehicleName;
    private final String category;
    private final Gauge gauge;
    private final int crew;

    public RollingStockItem(Properties p, String vehicleName, String category, Gauge gauge, int crew) {
        super(p);
        this.vehicleName = vehicleName;
        this.category = category;
        this.gauge = gauge;
        this.crew = crew;
    }

    public Gauge gauge() {
        return gauge;
    }

    /** Crew the vehicle needs while it runs (driver, fireman). The wages thread charges them through Autonomy.status. */
    public int crew() {
        return crew;
    }

    static EntityType<? extends AbstractMinecart> cartFor(String category) {
        return switch (category) {
            case "loco_steam", "loco_diesel", "loco_electric", "loco_primitive" -> EntityType.FURNACE_MINECART;
            case "freight" -> EntityType.CHEST_MINECART;
            default -> EntityType.MINECART;
        };
    }

    /** Hopper-bottomed freight (hoppers, tanks, ore) is a hopper cart so it loads and unloads like one. */
    static EntityType<? extends AbstractMinecart> cartFor(String id, String category) {
        if (category.equals("freight") && (id.contains("hopper") || id.equals("wag_ore") || id.equals("wag_tank"))) return EntityType.HOPPER_MINECART;
        return cartFor(category);
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        if (!(ctx.getLevel() instanceof ServerLevel level)) return InteractionResult.SUCCESS;
        BlockPos pos = ctx.getClickedPos();
        if (!(level.getBlockState(pos).getBlock() instanceof BaseRailBlock rail)) return InteractionResult.PASS;
        if (rail instanceof RailSpeed r && !Gauge.runs(gauge, r.gauge())) {
            if (ctx.getPlayer() != null) ctx.getPlayer().sendOverlayMessage(Component.literal("Wrong gauge: this is " + gauge.id + " gauge, the track is " + r.gauge().id + "."));
            return InteractionResult.FAIL;
        }
        ItemStack stack = ctx.getItemInHand();
        if (spawn(level, pos, stack) == null) return InteractionResult.FAIL;
        stack.shrink(1);
        return InteractionResult.CONSUME;
    }

    /** Places the cart for this stack on the rail at pos. */
    public static AbstractMinecart spawn(ServerLevel level, BlockPos pos, ItemStack stack) {
        if (!(stack.getItem() instanceof RollingStockItem r)) return null;
        String id = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();
        AbstractMinecart cart = cartFor(id, r.category).create(level, EntitySpawnReason.SPAWN_ITEM_USE);
        if (cart == null) return null;
        cart.setPos(pos.getX() + 0.5, pos.getY() + 0.0625, pos.getZ() + 0.5);
        cart.setCustomName(Component.literal(r.vehicleName));
        GaugeData.set(cart, r.gauge);
        level.addFreshEntity(cart);
        return cart;
    }
}
