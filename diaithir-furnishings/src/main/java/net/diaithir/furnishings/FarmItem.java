package net.diaithir.furnishings;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.cow.Cow;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.FarmlandBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * A farm implement that works a swath of ground per use: till, plant, harvest, fertilize, water, bale or milk.
 * Hand tools cost one durability per swath. Draft implements need a horse, donkey, mule or cow led by the player (or ridden)
 * within 6 m. Powered machines hold fuel in their durability bar (1 point = 1 kJ) and use rated power times swath time;
 * sneak-use with fuel in the off hand tops up the tank. Upgrades set custom data efficiency_mul on the stack in a later
 * pass (diaithir-upgrades has no target rule for these yet), so the cost below reads 1.0.
 */
final class FarmItem extends Item {
    private final String kind, mode;
    private final int width, powerW;

    FarmItem(Properties p, String kind, int width, int powerW, String mode) {
        super(p);
        this.kind = kind;
        this.width = Math.max(1, width);
        this.powerW = powerW;
        this.mode = mode;
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        Level level = ctx.getLevel();
        Player player = ctx.getPlayer();
        ItemStack stack = ctx.getItemInHand();
        if (player == null) return InteractionResult.PASS;
        if (player.isShiftKeyDown() && powerW > 0) return refuel(level, player, stack);
        if (kind.equals("none") || kind.equals("milk")) return InteractionResult.PASS;
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        if (mode.equals("draft") && !hasDraftAnimal(level, player)) {
            say(player, "Needs a horse, donkey, mule or cow led by you.");
            return InteractionResult.FAIL;
        }
        long cost = FarmMath.swathJoules(powerW, mode, 1.0);
        int points = powerW > 0 ? (int) Math.max(1, cost / 1000) : 1;
        if (stack.getDamageValue() + points > stack.getMaxDamage()) {
            say(player, powerW > 0 ? "Out of fuel." : "Worn out.");
            return InteractionResult.FAIL;
        }
        Direction fwd = player.getDirection();
        Direction side = fwd.getClockWise();
        BlockPos origin = ctx.getClickedPos();
        int worked = 0;
        for (int[] o : FarmMath.swath(width, FarmMath.length(mode))) {
            BlockPos pos = origin.relative(fwd, o[0]).relative(side, o[1]);
            if (work(level, player, pos)) worked++;
        }
        if (worked == 0) return InteractionResult.PASS;
        stack.setDamageValue(stack.getDamageValue() + points);
        if (stack.getDamageValue() >= stack.getMaxDamage() && powerW == 0) stack.shrink(1);
        return InteractionResult.SUCCESS;
    }

    private boolean work(Level level, Player player, BlockPos pos) {
        BlockState s = level.getBlockState(pos);
        BlockPos above = pos.above();
        switch (kind) {
            case "till":
                if ((s.is(Blocks.DIRT) || s.is(Blocks.GRASS_BLOCK) || s.is(Blocks.DIRT_PATH) || s.is(Blocks.COARSE_DIRT)) && level.getBlockState(above).isAir()) {
                    level.setBlock(pos, Blocks.FARMLAND.defaultBlockState(), Block.UPDATE_ALL);
                    return true;
                }
                return false;
            case "plant": {
                if (!s.is(Blocks.FARMLAND) || !level.getBlockState(above).isAir()) return false;
                ItemStack seed = findSeed(player);
                if (seed == null) return false;
                level.setBlock(above, ((BlockItem) seed.getItem()).getBlock().defaultBlockState(), Block.UPDATE_ALL);
                seed.shrink(1);
                return true;
            }
            case "harvest":
                if (s.getBlock() instanceof CropBlock c && c.isMaxAge(s)) {
                    level.destroyBlock(pos, true, player);
                    BlockState fresh = c.getStateForAge(0);
                    if (level.getBlockState(pos).isAir() && level.getBlockState(pos.below()).is(Blocks.FARMLAND)) level.setBlock(pos, fresh, Block.UPDATE_ALL);
                    return true;
                }
                return false;
            case "fertilize":
                if (s.getBlock() instanceof CropBlock c && !c.isMaxAge(s) && consume(player, Items.BONE_MEAL)) {
                    level.setBlock(pos, c.getStateForAge(Math.min(c.getMaxAge(), c.getAge(s) + 2)), Block.UPDATE_ALL);
                    return true;
                }
                return false;
            case "water":
                if (s.is(Blocks.FARMLAND) && s.getValue(FarmlandBlock.MOISTURE) < 7) {
                    level.setBlock(pos, s.setValue(FarmlandBlock.MOISTURE, 7), Block.UPDATE_ALL);
                    return true;
                }
                return false;
            case "bale":
                return false;
            default:
                return false;
        }
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (kind.equals("bale") && !level.isClientSide()) {  // 9 wheat make one hay bale, costs one swath of energy
            if (consumeN(player, Items.WHEAT, 9) && spend(stack)) {
                player.getInventory().add(new ItemStack(Items.HAY_BLOCK));
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        if (kind.equals("milk") && target instanceof Cow cow && !cow.isBaby() && !player.level().isClientSide() && consume(player, Items.BUCKET)) {
            if (spend(stack)) {
                player.getInventory().add(new ItemStack(Items.MILK_BUCKET));
                return InteractionResult.SUCCESS;
            }
            player.getInventory().add(new ItemStack(Items.BUCKET));
        }
        return InteractionResult.PASS;
    }

    private static void say(Player p, String text) {
        if (p instanceof net.minecraft.server.level.ServerPlayer sp) sp.sendSystemMessage(net.minecraft.network.chat.Component.literal(text), true);
    }

    private boolean spend(ItemStack stack) {
        int points = (int) Math.max(1, FarmMath.swathJoules(Math.max(powerW, 1), mode, 1.0) / 1000);
        if (stack.getDamageValue() + points > stack.getMaxDamage()) return false;
        stack.setDamageValue(stack.getDamageValue() + points);
        return true;
    }

    private InteractionResult refuel(Level level, Player player, ItemStack stack) {
        ItemStack off = player.getOffhandItem();
        Long j = FarmMath.FUEL_J.get(BuiltInRegistries.ITEM.getKey(off.getItem()).toString());
        if (j == null || stack.getDamageValue() == 0) return InteractionResult.PASS;
        if (!level.isClientSide()) {
            stack.setDamageValue(Math.max(0, stack.getDamageValue() - FarmMath.tankPoints(j)));
            off.shrink(1);
        }
        return InteractionResult.SUCCESS;
    }

    private static boolean hasDraftAnimal(Level level, Player player) {
        List<Animal> near = level.getEntitiesOfClass(Animal.class, player.getBoundingBox().inflate(6.0));
        if (player.getVehicle() instanceof AbstractHorse) return true;
        for (Animal a : near)
            if ((a instanceof AbstractHorse || a instanceof Cow) && a.getLeashHolder() == player) return true;
        return false;
    }

    private static ItemStack findSeed(Player p) {
        for (ItemStack s : p.getInventory().getNonEquipmentItems())
            if (!s.isEmpty() && s.getItem() instanceof BlockItem bi && bi.getBlock() instanceof CropBlock) return s;
        return null;
    }

    private static boolean consume(Player p, Item item) {
        return consumeN(p, item, 1);
    }

    private static boolean consumeN(Player p, Item item, int n) {
        int have = 0;
        for (ItemStack s : p.getInventory().getNonEquipmentItems()) if (s.is(item)) have += s.getCount();
        if (have < n) return false;
        for (ItemStack s : p.getInventory().getNonEquipmentItems()) {
            if (!s.is(item)) continue;
            int t = Math.min(n, s.getCount());
            s.shrink(t);
            n -= t;
            if (n == 0) break;
        }
        return true;
    }
}
