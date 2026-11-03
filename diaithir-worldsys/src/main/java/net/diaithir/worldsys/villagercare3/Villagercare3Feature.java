package net.diaithir.worldsys.villagercare3;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.diaithir.worldsys.villagercare2.Villagercare2Feature;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/** Automatic trader, converter, incubator, iron farm and an inventory viewer for stored villagers. */
public final class Villagercare3Feature {
    public static final String ID = "diaithirworldsys";
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(ID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ID);
    public static final DeferredRegister<BlockEntityType<?>> TYPES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, ID);

    private static BlockBehaviour.Properties props() {
        return BlockBehaviour.Properties.of().strength(2.0F).sound(SoundType.WOOD).noOcclusion();
    }

    public static final DeferredBlock<StationBlock> AUTO_TRADER = BLOCKS.registerBlock("easy_villagers_auto_trader", p -> new StationBlock(p, 0), Villagercare3Feature::props);
    public static final DeferredBlock<StationBlock> CONVERTER = BLOCKS.registerBlock("easy_villagers_converter", p -> new StationBlock(p, 1), Villagercare3Feature::props);
    public static final DeferredBlock<StationBlock> INCUBATOR = BLOCKS.registerBlock("easy_villagers_incubator", p -> new StationBlock(p, 2), Villagercare3Feature::props);
    public static final DeferredBlock<StationBlock> IRON_FARM = BLOCKS.registerBlock("easy_villagers_iron_farm", p -> new StationBlock(p, 3), Villagercare3Feature::props);
    public static final DeferredItem<BlockItem> AUTO_TRADER_ITEM = ITEMS.registerSimpleBlockItem(AUTO_TRADER);
    public static final DeferredItem<BlockItem> CONVERTER_ITEM = ITEMS.registerSimpleBlockItem(CONVERTER);
    public static final DeferredItem<BlockItem> INCUBATOR_ITEM = ITEMS.registerSimpleBlockItem(INCUBATOR);
    public static final DeferredItem<BlockItem> IRON_FARM_ITEM = ITEMS.registerSimpleBlockItem(IRON_FARM);
    public static final DeferredItem<ViewerItem> VIEWER = ITEMS.registerItem("easy_villagers_inventory_viewer", ViewerItem::new, p -> p.stacksTo(1));

    public static final Supplier<BlockEntityType<StationBE>> BE = TYPES.register("easy_villagers_station3",
            () -> new BlockEntityType<>(StationBE::new, AUTO_TRADER.get(), CONVERTER.get(), INCUBATOR.get(), IRON_FARM.get()));

    private Villagercare3Feature() {}

    public static void register(IEventBus modBus) {
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        TYPES.register(modBus);
        modBus.addListener(BuildCreativeModeTabContentsEvent.class, e -> {
            if (e.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
                e.accept(AUTO_TRADER_ITEM.get());
                e.accept(CONVERTER_ITEM.get());
                e.accept(INCUBATOR_ITEM.get());
                e.accept(IRON_FARM_ITEM.get());
            }
            if (e.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) e.accept(VIEWER.get());
        });
    }

    private static ItemStack stackOf(CompoundTag t) {
        ItemStack s = new ItemStack(Villagercare2Feature.VILLAGER.get());
        s.set(DataComponents.CUSTOM_DATA, CustomData.of(t));
        return s;
    }

    private static boolean isZombie(CompoundTag t) {
        return t.getBooleanOr("diaithir_zombie", false);
    }

    private static ItemStack insert(ServerLevel level, BlockPos pos, ItemStack stack) {
        for (Direction d : Direction.values()) {
            ResourceHandler<ItemResource> h = level.getCapability(Capabilities.Item.BLOCK, pos.relative(d), d.getOpposite());
            if (h == null) continue;
            ItemResource res = ItemResource.of(stack);
            int inserted;
            try (Transaction tx = Transaction.openRoot()) {
                inserted = h.insert(res, stack.getCount(), tx);
                tx.commit();
            }
            if (inserted > 0) {
                stack = stack.copy();
                stack.shrink(inserted);
            }
            if (stack.isEmpty()) return stack;
        }
        return stack;
    }

    private static void output(ServerLevel level, BlockPos pos, ItemStack stack) {
        ItemStack rest = insert(level, pos, stack);
        if (!rest.isEmpty()) Block.popResource(level, pos.above(), rest);
    }

    /** One block class for the four stations; kind selects the behaviour. */
    public static final class StationBlock extends Block implements EntityBlock {
        final int kind;

        public StationBlock(Properties p, int kind) {
            super(p);
            this.kind = kind;
        }

        @Override
        public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
            return new StationBE(pos, state);
        }

        @Override
        public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
            if (level.isClientSide() || type != BE.get()) return null;
            return (l, p, s, be) -> ((StationBE) be).tick((ServerLevel) l, kind);
        }

        @Override
        protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
            if (!(level.getBlockEntity(pos) instanceof StationBE be)) return InteractionResult.PASS;
            if (level.isClientSide()) return InteractionResult.SUCCESS;
            if (player.isShiftKeyDown() && !be.villagers.isEmpty()) {
                ItemStack out = stackOf(be.villagers.remove(be.villagers.size() - 1));
                be.timer = 0;
                be.setChanged();
                if (!player.getInventory().add(out)) player.drop(out, false);
                return InteractionResult.SUCCESS_SERVER;
            }
            return InteractionResult.PASS;
        }

        @Override
        protected InteractionResult useItemOn(ItemStack held, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
            if (!(level.getBlockEntity(pos) instanceof StationBE be)) return InteractionResult.PASS;
            if (held.is(VIEWER.get())) return InteractionResult.PASS;
            if (level.isClientSide()) return InteractionResult.SUCCESS;
            int cap = kind == 3 ? 3 : 1;
            if (held.is(Villagercare2Feature.VILLAGER.get()) && !Villagercare2Feature.VillagerItem.isEmpty(held)) {
                CompoundTag t = Villagercare2Feature.data(held);
                if (be.villagers.size() >= cap) return InteractionResult.PASS;
                if (kind == 2 && t.getIntOr("Age", 0) >= 0) return InteractionResult.PASS;
                be.villagers.add(t);
                be.timer = 0;
                held.shrink(1);
                be.setChanged();
                return InteractionResult.SUCCESS_SERVER;
            }
            if (kind == 1 && !be.villagers.isEmpty() && be.target == 0) {
                boolean zombie = isZombie(be.villagers.get(0));
                if ((held.is(Items.ROTTEN_FLESH) && !zombie) || (held.is(Items.GOLDEN_APPLE) && zombie)) {
                    be.target = zombie ? 2 : 1;
                    be.timer = 0;
                    held.shrink(1);
                    be.setChanged();
                    return InteractionResult.SUCCESS_SERVER;
                }
            }
            return InteractionResult.PASS;
        }

        @Override
        public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
            if (!level.isClientSide() && level.getBlockEntity(pos) instanceof StationBE be) {
                for (CompoundTag t : be.villagers) Block.popResource(level, pos, stackOf(t));
                be.villagers.clear();
            }
            return super.playerWillDestroy(level, pos, state, player);
        }
    }

    /** Shared block entity holding stored villagers and one process timer. */
    public static final class StationBE extends BlockEntity {
        final List<CompoundTag> villagers = new ArrayList<>();
        int timer;
        int target;

        public StationBE(BlockPos pos, BlockState state) {
            super(BE.get(), pos, state);
        }

        @Override
        protected void saveAdditional(ValueOutput out) {
            super.saveAdditional(out);
            ListTag list = new ListTag();
            list.addAll(villagers);
            CompoundTag root = new CompoundTag();
            root.put("v", list);
            out.store("villagers", CompoundTag.CODEC, root);
            out.putInt("timer", timer);
            out.putInt("target", target);
        }

        @Override
        protected void loadAdditional(ValueInput in) {
            super.loadAdditional(in);
            villagers.clear();
            in.read("villagers", CompoundTag.CODEC).ifPresent(root -> {
                ListTag list = root.getListOrEmpty("v");
                for (int i = 0; i < list.size(); i++) list.getCompound(i).ifPresent(villagers::add);
            });
            timer = in.getIntOr("timer", 0);
            target = in.getIntOr("target", 0);
        }

        String describe() {
            if (villagers.isEmpty()) return "empty";
            StringBuilder sb = new StringBuilder();
            for (CompoundTag t : villagers) {
                if (sb.length() > 0) sb.append("; ");
                String prof = t.getCompound("VillagerData").flatMap(d -> d.getString("profession")).orElse("none");
                sb.append(isZombie(t) ? "zombie villager" : t.getIntOr("Age", 0) < 0 ? "baby villager" : "villager (" + prof + ")");
            }
            return sb + " | timer " + timer / 20 + " s";
        }

        private Villager load(ServerLevel level, CompoundTag t) {
            Villager v = EntityType.VILLAGER.create(level, EntitySpawnReason.LOAD);
            if (v == null) return null;
            v.load(TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), t));
            return v;
        }

        void tick(ServerLevel level, int kind) {
            if (villagers.isEmpty()) return;
            switch (kind) {
                case 0 -> {
                    if (++timer < 40) return;
                    timer = 0;
                    autoTrade(level);
                }
                case 1 -> {
                    if (target == 0) return;
                    if (++timer < (target == 1 ? 1200 : 2400)) return;
                    CompoundTag t = villagers.get(0);
                    if (target == 1) t.putBoolean("diaithir_zombie", true);
                    else t.remove("diaithir_zombie");
                    target = 0;
                    timer = 0;
                    setChanged();
                }
                case 2 -> {
                    CompoundTag t = villagers.get(0);
                    if (t.getIntOr("Age", 0) >= 0) return;
                    if (++timer < 6000) return;
                    t.putInt("Age", 0);
                    timer = 0;
                    setChanged();
                }
                default -> {
                    if (++timer < 2400) return;
                    timer = 0;
                    output(level, worldPosition, new ItemStack(Items.IRON_INGOT));
                    if (level.getRandom().nextInt(4) == 0) output(level, worldPosition, new ItemStack(Items.POPPY));
                    setChanged();
                }
            }
        }

        private void autoTrade(ServerLevel level) {
            CompoundTag stored = villagers.get(0);
            if (isZombie(stored)) return;
            Villager v = load(level, stored);
            if (v == null) return;
            for (var offer : v.getOffers()) {
                if (offer.isOutOfStock()) continue;
                ItemStack a = offer.getCostA();
                ItemStack b = offer.getCostB();
                if (!b.isEmpty() || !take(level, a, true)) continue;
                take(level, a, false);
                offer.increaseUses();
                output(level, worldPosition, offer.getResult().copy());
                TagValueOutput out = TagValueOutput.createWithoutContext(ProblemReporter.DISCARDING);
                v.saveWithoutId(out);
                villagers.set(0, out.buildResult());
                setChanged();
                return;
            }
        }

        /** Extracts (or only simulates) the payment from one adjacent inventory. */
        private boolean take(ServerLevel level, ItemStack cost, boolean simulate) {
            for (Direction d : Direction.values()) {
                ResourceHandler<ItemResource> h = level.getCapability(Capabilities.Item.BLOCK, worldPosition.relative(d), d.getOpposite());
                if (h == null) continue;
                int need = cost.getCount();
                ItemResource res = ItemResource.of(cost);
                try (Transaction tx = Transaction.openRoot()) {
                    int got = h.extract(res, need, tx);
                    need -= got;
                    if (!simulate && need <= 0) tx.commit();
                }
                if (need <= 0) return true;
                if (!simulate && need < cost.getCount()) return false;
            }
            return false;
        }
    }

    /** Prints the contents of any villager block to chat. */
    public static final class ViewerItem extends Item {
        public ViewerItem(Properties p) {
            super(p);
        }

        @Override
        public InteractionResult useOn(UseOnContext ctx) {
            Level level = ctx.getLevel();
            Player player = ctx.getPlayer();
            BlockEntity be = level.getBlockEntity(ctx.getClickedPos());
            String text;
            if (be instanceof StationBE s) text = s.describe();
            else if (be instanceof Villagercare2Feature.VillagerBE v2) {
                TagValueOutput out = TagValueOutput.createWithoutContext(ProblemReporter.DISCARDING);
                v2.saveWithoutMetadata(out);
                text = out.buildResult().toString();
            } else return InteractionResult.PASS;
            if (!level.isClientSide() && player != null) player.sendSystemMessage(Component.literal(text));
            return InteractionResult.SUCCESS;
        }
    }
}
