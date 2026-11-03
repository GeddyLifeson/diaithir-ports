package net.diaithir.diaithirfarmkitchen;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemUtil;
import net.neoforged.neoforge.transfer.transaction.Transaction;

public class KitchenCounterBlock extends Block {
    private record Recipe(String id, List<Predicate<ItemStack>> needs, String product, int count) {}

    private static TagKey<Item> tag(String path) {
        return TagKey.create(net.minecraft.core.registries.Registries.ITEM, Identifier.fromNamespaceAndPath("c", path));
    }

    private static Predicate<ItemStack> t(String path) {
        TagKey<Item> k = tag(path);
        return s -> s.is(k);
    }

    private static Predicate<ItemStack> i(Item item) {
        return s -> s.is(item);
    }

    private static final List<Recipe> RECIPES = List.of(
            new Recipe("ration", List.of(i(Items.WHEAT), i(Items.WHEAT), t("foods")), "ration", 2),
            new Recipe("hearty_stew", List.of(t("crops"), t("crops"), t("foods"), i(Items.BOWL)), "hearty_stew", 1),
            new Recipe("offering_feast", List.of(t("foods"), t("foods"), t("crops"), t("cooked_fishes"), i(Items.HONEY_BOTTLE)), "offering_feast", 1),
            new Recipe("travelers_bread", List.of(i(Items.WHEAT), i(Items.WHEAT), i(Items.WHEAT), i(Items.HONEY_BOTTLE)), "travelers_bread", 2),
            new Recipe("smoked_fish_plate", List.of(t("cooked_fishes"), t("cooked_fishes"), t("crops")), "smoked_fish_plate", 1));

    public KitchenCounterBlock(BlockBehaviour.Properties props) {
        super(props);
    }

    private static List<ResourceHandler<ItemResource>> handlers(Level level, BlockPos origin) {
        List<ResourceHandler<ItemResource>> out = new ArrayList<>();
        Direction[] dirs = new Direction[] {null, Direction.UP, Direction.DOWN, Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST};
        for (BlockPos p : BlockPos.betweenClosed(origin.offset(-4, -4, -4), origin.offset(4, 4, 4))) {
            if (p.equals(origin)) continue;
            BlockPos imm = p.immutable();
            for (Direction d : dirs) {
                ResourceHandler<ItemResource> h = level.getCapability(Capabilities.Item.BLOCK, imm, d);
                if (h != null) {
                    if (!out.contains(h)) out.add(h);
                    break;
                }
            }
        }
        return out;
    }

    // returns list of [handler, slot] picks or null
    private static List<int[]> plan(List<ResourceHandler<ItemResource>> hs, Recipe r) {
        List<int[]> picks = new ArrayList<>();
        int[][] used = new int[hs.size()][];
        for (int k = 0; k < hs.size(); k++) used[k] = new int[hs.get(k).size()];
        for (Predicate<ItemStack> need : r.needs()) {
            boolean found = false;
            outer:
            for (int k = 0; k < hs.size(); k++) {
                ResourceHandler<ItemResource> h = hs.get(k);
                for (int s = 0; s < h.size(); s++) {
                    ItemStack st = ItemUtil.getStack(h, s);
                    if (!st.isEmpty() && st.getCount() - used[k][s] > 0 && need.test(st)) {
                        used[k][s]++;
                        picks.add(new int[] {k, s});
                        found = true;
                        break outer;
                    }
                }
            }
            if (!found) return null;
        }
        return picks;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        List<ResourceHandler<ItemResource>> hs = handlers(level, pos);
        if (player.isShiftKeyDown()) {
            List<String> names = new ArrayList<>();
            for (Recipe r : RECIPES) {
                if (plan(hs, r) != null) names.add(r.id());
            }
            player.sendSystemMessage(Component.literal(names.isEmpty()
                    ? "Nothing craftable nearby."
                    : "Craftable: " + String.join(", ", names)));
            return InteractionResult.SUCCESS;
        }
        for (Recipe r : RECIPES) {
            List<int[]> picks = plan(hs, r);
            if (picks == null) continue;
            try (Transaction tx = Transaction.openRoot()) {
                for (int[] p : picks) {
                    ResourceHandler<ItemResource> h = hs.get(p[0]);
                    ItemResource res = h.getResource(p[1]);
                    h.extract(res, 1, tx);
                }
                tx.commit();
            }
            Item product = BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath(FarmkitchenMod.ID, r.product()));
            ItemStack out = new ItemStack(product, r.count());
            if (!player.getInventory().add(out)) player.drop(out, false);
            player.sendSystemMessage(Component.literal("Cooked " + r.id()));
            return InteractionResult.SUCCESS;
        }
        player.sendSystemMessage(Component.literal("Missing ingredients."));
        return InteractionResult.SUCCESS;
    }
}
