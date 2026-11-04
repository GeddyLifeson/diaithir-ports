package net.diaithir.druidry.ars.common.mob_jar;

import net.diaithir.druidry.ars.api.item.inv.FilterableItemHandler;
import net.diaithir.druidry.ars.api.item.inv.InventoryManager;
import net.diaithir.druidry.ars.api.item.inv.MultiInsertReference;
import net.diaithir.druidry.ars.api.mob_jar.JarBehavior;
import net.diaithir.druidry.ars.api.util.InvUtil;
import net.diaithir.druidry.ars.common.block.tile.MobJarTile;
import net.diaithir.druidry.ars.common.items.ItemScroll;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.animal.allay.Allay;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AllayBehavior extends JarBehavior<Allay> {

    private static final Map<String, Field> FIELD_CACHE = new HashMap<>();

    private static Field allayField(String name) {
        return FIELD_CACHE.computeIfAbsent(name, n -> {
            try {
                Field f = Allay.class.getDeclaredField(n);
                f.setAccessible(true);
                return f;
            } catch (Exception e) {
                return null;
            }
        });
    }

    private static float getF(Allay allay, String name) {
        Field f = allayField(name);
        if (f == null) {
            return 0.0F;
        }
        try {
            return f.getFloat(allay);
        } catch (Exception e) {
            return 0.0F;
        }
    }

    private static void setF(Allay allay, String name, float value) {
        Field f = allayField(name);
        if (f == null) {
            return;
        }
        try {
            f.setFloat(allay, value);
        } catch (Exception ignored) {
        }
    }

    private static void addFilterScroll(FilterableItemHandler handler, ItemStack scroll) {
        for (Method m : handler.getClass().getMethods()) {
            if (m.getParameterCount() == 1 && m.getParameterTypes()[0] == ItemStack.class) {
                String n = m.getName().toLowerCase();
                if (n.contains("filter") || n.contains("scroll")) {
                    try {
                        m.invoke(handler, scroll);
                    } catch (Exception ignored) {
                    }
                    return;
                }
            }
        }
    }

    @Override
    public void use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand handIn, BlockHitResult hit, MobJarTile tile) {
        super.use(state, world, pos, player, handIn, hit, tile);
    }

    @Override
    public void tick(MobJarTile tile) {
        super.tick(tile);
        if(tile.getLevel().isClientSide()){
            Allay allay = entityFromJar(tile);
            allay.tickCount++;
            setF(allay, "holdingItemAnimationTicks0", getF(allay, "holdingItemAnimationTicks"));
            if (allay.hasItemInHand()) {
                setF(allay, "holdingItemAnimationTicks", Mth.clamp(getF(allay, "holdingItemAnimationTicks") + 1.0F, 0.0F, 5.0F));
            } else {
                setF(allay, "holdingItemAnimationTicks", Mth.clamp(getF(allay, "holdingItemAnimationTicks") - 1.0F, 0.0F, 5.0F));
            }

            if (allay.isDancing()) {
                setF(allay, "dancingAnimationTicks", getF(allay, "dancingAnimationTicks") + 1.0F);
                setF(allay, "spinningAnimationTicks0", getF(allay, "spinningAnimationTicks"));
                if (allay.isSpinning()) {
                    setF(allay, "spinningAnimationTicks", getF(allay, "spinningAnimationTicks") + 1.0F);
                } else {
                    setF(allay, "spinningAnimationTicks", getF(allay, "spinningAnimationTicks") - 1.0F);
                }

                setF(allay, "spinningAnimationTicks", Mth.clamp(getF(allay, "spinningAnimationTicks"), 0.0F, 15.0F));
            } else {
                setF(allay, "dancingAnimationTicks", 0.0F);
                setF(allay, "spinningAnimationTicks", 0.0F);
                setF(allay, "spinningAnimationTicks0", 0.0F);
            }
        }else{
            Level level = tile.getLevel();
            if(level.getGameTime() % 40 == 0){
                Allay allay = entityFromJar(tile);
                ItemStack heldStack = allay.getItemInHand(InteractionHand.MAIN_HAND);
                List<FilterableItemHandler> inventories = InvUtil.adjacentInventories(level, tile.getBlockPos());
                if(inventories.isEmpty()){
                    return;
                }
                if(heldStack.getItem() instanceof ItemScroll){
                    for(FilterableItemHandler filterableItemHandler : inventories){
                        addFilterScroll(filterableItemHandler, heldStack);
                    }
                }
                InventoryManager manager = new InventoryManager(inventories);
                for(ItemEntity entity : level.getEntitiesOfClass(ItemEntity.class, new AABB(tile.getBlockPos()).inflate(5.0D))){
                    if(entity.isAlive() && !entity.getItem().isEmpty()){
                        if (heldStack.isEmpty() || heldStack.getItem() instanceof ItemScroll || ItemStack.isSameItem(entity.getItem(), heldStack)) {
                            int originalCount = entity.getItem().getCount();
                            MultiInsertReference reference = manager.insertStackWithReference(entity.getItem());
                            ItemStack remainder = reference.getRemainder();
                            if(remainder == null || remainder.getCount() != originalCount || remainder.isEmpty()) {
                                entity.setItem(remainder == null ? ItemStack.EMPTY : remainder);
                                level.playSound(null, tile.getBlockPos(), SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 0.8F, 1.0F);
                                return;
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public boolean shouldUsePartialTicks(MobJarTile pBlockEntity) {
        return true;
    }

    @Override
    public Vec3 translate(MobJarTile pBlockEntity) {
        return new Vec3(0, 0.2, 0);
    }

    @Override
    public void getTooltip(MobJarTile tile, List<Component> tooltips) {
        super.getTooltip(tile, tooltips);
        Allay allay = entityFromJar(tile);
        if(allay.getMainHandItem().getItem() instanceof ItemScroll scroll){
            scroll.appendHoverText(allay.getMainHandItem(), Item.TooltipContext.of(tile.getLevel()), TooltipDisplay.DEFAULT, tooltips::add, TooltipFlag.Default.NORMAL);
        }
    }
}
