package net.diaithir.worldsys.villagercare2;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ProblemReporter;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

/** Villager capture item plus trader, breeder and farmer blocks that keep a stored villager. */
public final class Villagercare2Feature {
    public static final String ID = "diaithirworldsys";
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(ID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ID);
    public static final DeferredRegister<BlockEntityType<?>> TYPES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, ID);

    private static BlockBehaviour.Properties props() {
        return BlockBehaviour.Properties.of().strength(2.0F).sound(SoundType.WOOD).noOcclusion();
    }

    public static final DeferredItem<VillagerItem> VILLAGER = ITEMS.registerItem("easy_villagers_villager", VillagerItem::new, p -> p.stacksTo(1));
    public static final DeferredBlock<VillagerBlock> TRADER = BLOCKS.registerBlock("easy_villagers_trader", p -> new VillagerBlock(p, 0), Villagercare2Feature::props);
    public static final DeferredBlock<VillagerBlock> BREEDER = BLOCKS.registerBlock("easy_villagers_breeder", p -> new VillagerBlock(p, 1), Villagercare2Feature::props);
    public static final DeferredBlock<VillagerBlock> FARMER = BLOCKS.registerBlock("easy_villagers_farmer", p -> new VillagerBlock(p, 2), Villagercare2Feature::props);
    public static final DeferredItem<BlockItem> TRADER_ITEM = ITEMS.registerSimpleBlockItem(TRADER);
    public static final DeferredItem<BlockItem> BREEDER_ITEM = ITEMS.registerSimpleBlockItem(BREEDER);
    public static final DeferredItem<BlockItem> FARMER_ITEM = ITEMS.registerSimpleBlockItem(FARMER);

    public static final Supplier<BlockEntityType<VillagerBE>> BE = TYPES.register("easy_villagers_station",
            () -> new BlockEntityType<>(VillagerBE::new, TRADER.get(), BREEDER.get(), FARMER.get()));

    private Villagercare2Feature() {}

    public static void register(IEventBus modBus) {
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        TYPES.register(modBus);
        modBus.addListener(BuildCreativeModeTabContentsEvent.class, e -> {
            if (e.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
                e.accept(TRADER_ITEM.get());
                e.accept(BREEDER_ITEM.get());
                e.accept(FARMER_ITEM.get());
            }
            if (e.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) e.accept(VILLAGER.get());
        });
        NeoForge.EVENT_BUS.addListener(PlayerInteractEvent.EntityInteract.class, Villagercare2Feature::capture);
    }

    /** Saves a villager entity into a fresh stored-villager stack. */
    public static ItemStack pack(Villager v) {
        TagValueOutput out = TagValueOutput.createWithoutContext(ProblemReporter.DISCARDING);
        v.saveWithoutId(out);
        CompoundTag tag = out.buildResult();
        ItemStack s = new ItemStack(VILLAGER.get());
        s.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        return s;
    }

    public static CompoundTag data(ItemStack s) {
        CustomData d = s.get(DataComponents.CUSTOM_DATA);
        return d == null ? new CompoundTag() : d.copyTag();
    }

    public static ItemStack babyStack(ServerLevel level) {
        Villager v = EntityType.VILLAGER.create(level, EntitySpawnReason.BREEDING);
        if (v == null) return ItemStack.EMPTY;
        v.setAge(-24000);
        return pack(v);
    }

    private static void capture(PlayerInteractEvent.EntityInteract e) {
        if (!e.getItemStack().is(VILLAGER.get()) || !(e.getTarget() instanceof Villager v)) return;
        if (!VillagerItem.isEmpty(e.getItemStack())) return;
        e.setCancellationResult(InteractionResult.SUCCESS);
        e.setCanceled(true);
        if (!(e.getLevel() instanceof ServerLevel)) return;
        ItemStack packed = pack(v);
        v.discard();
        e.getItemStack().set(DataComponents.CUSTOM_DATA, packed.get(DataComponents.CUSTOM_DATA));
    }

    /** Item that holds one villager in custom data. */
    public static final class VillagerItem extends Item {
        public VillagerItem(Properties p) {
            super(p);
        }

        public static boolean isEmpty(ItemStack s) {
            return data(s).isEmpty();
        }

        @Override
        public Component getName(ItemStack stack) {
            return isEmpty(stack) ? Component.literal("Empty Villager Carrier") : Component.literal("Stored Villager");
        }

        @Override
        public InteractionResult useOn(UseOnContext ctx) {
            ItemStack s = ctx.getItemInHand();
            if (isEmpty(s)) return InteractionResult.PASS;
            if (!(ctx.getLevel() instanceof ServerLevel level)) return InteractionResult.SUCCESS;
            BlockPos pos = ctx.getClickedPos().relative(ctx.getClickedFace());
            Villager v = EntityType.VILLAGER.create(level, EntitySpawnReason.SPAWN_ITEM_USE);
            if (v == null) return InteractionResult.FAIL;
            v.load(TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), data(s)));
            v.snapTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
            level.addFreshEntity(v);
            s.remove(DataComponents.CUSTOM_DATA);
            return InteractionResult.SUCCESS_SERVER;
        }
    }

    /** One block class for all three stations; kind selects the behaviour. */
    public static final class VillagerBlock extends Block implements net.minecraft.world.level.block.EntityBlock {
        final int kind;

        public VillagerBlock(Properties p, int kind) {
            super(p);
            this.kind = kind;
        }

        @Override
        public net.minecraft.world.level.block.entity.BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
            return new VillagerBE(pos, state);
        }

        @Override
        public <T extends net.minecraft.world.level.block.entity.BlockEntity> net.minecraft.world.level.block.entity.BlockEntityTicker<T> getTicker(
                Level level, BlockState state, BlockEntityType<T> type) {
            if (level.isClientSide() || type != BE.get()) return null;
            return (l, p, s, be) -> ((VillagerBE) be).tick((ServerLevel) l, kind);
        }

        @Override
        protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
            if (!(level.getBlockEntity(pos) instanceof VillagerBE be)) return InteractionResult.PASS;
            if (level.isClientSide()) return InteractionResult.SUCCESS;
            if (player.isShiftKeyDown() && !be.villagers.isEmpty()) {
                ItemStack out = be.removeLast();
                if (!player.getInventory().add(out)) player.drop(out, false);
                return InteractionResult.SUCCESS_SERVER;
            }
            return InteractionResult.PASS;
        }

        @Override
        protected InteractionResult useItemOn(ItemStack held, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
            if (!(level.getBlockEntity(pos) instanceof VillagerBE be)) return InteractionResult.PASS;
            if (level.isClientSide()) return InteractionResult.SUCCESS;
            ServerLevel sl = (ServerLevel) level;
            int cap = kind == 1 ? 2 : 1;
            if (held.is(VILLAGER.get()) && !VillagerItem.isEmpty(held)) {
                if (be.villagers.size() >= cap) return InteractionResult.PASS;
                be.villagers.add(data(held));
                held.shrink(1);
                be.setChanged();
                return InteractionResult.SUCCESS_SERVER;
            }
            if (kind == 1 && VillagerBE.isFood(held) && be.food < 64) {
                int add = Math.min(held.getCount(), 64 - be.food);
                be.food += add;
                held.shrink(add);
                be.setChanged();
                return InteractionResult.SUCCESS_SERVER;
            }
            if (kind == 0 && !held.isEmpty() && !be.villagers.isEmpty()) {
                return be.trade(sl, player, held) ? InteractionResult.SUCCESS_SERVER : InteractionResult.PASS;
            }
            return InteractionResult.PASS;
        }

        @Override
        public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
            if (!level.isClientSide() && level.getBlockEntity(pos) instanceof VillagerBE be) {
                for (CompoundTag t : be.villagers) {
                    ItemStack s = new ItemStack(VILLAGER.get());
                    s.set(DataComponents.CUSTOM_DATA, CustomData.of(t));
                    Block.popResource(level, pos, s);
                }
                be.villagers.clear();
            }
            return super.playerWillDestroy(level, pos, state, player);
        }
    }

    /** Shared block entity holding villagers, breeder fuel and timers. */
    public static final class VillagerBE extends net.minecraft.world.level.block.entity.BlockEntity {
        public final java.util.List<CompoundTag> villagers = new java.util.ArrayList<>();
        public int food;
        int timer;
        long lastRestock;

        public VillagerBE(BlockPos pos, BlockState state) {
            super(BE.get(), pos, state);
        }

        static boolean isFood(ItemStack s) {
            return s.is(net.minecraft.world.item.Items.BREAD) || s.is(net.minecraft.world.item.Items.CARROT)
                    || s.is(net.minecraft.world.item.Items.POTATO) || s.is(net.minecraft.world.item.Items.BEETROOT);
        }

        ItemStack removeLast() {
            CompoundTag t = villagers.remove(villagers.size() - 1);
            ItemStack s = new ItemStack(VILLAGER.get());
            s.set(DataComponents.CUSTOM_DATA, CustomData.of(t));
            setChanged();
            return s;
        }

        private Villager load(ServerLevel level, CompoundTag t) {
            Villager v = EntityType.VILLAGER.create(level, EntitySpawnReason.LOAD);
            if (v == null) return null;
            v.load(TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), t));
            return v;
        }

        boolean trade(ServerLevel level, Player player, ItemStack held) {
            Villager v = load(level, villagers.get(0));
            if (v == null) return false;
            long now = level.getGameTime();
            if (now - lastRestock > 24000L) {
                v.getOffers().forEach(o -> o.resetUses());
                lastRestock = now;
            } else if (now - lastRestock > 6000L) {
                for (var o : v.getOffers()) if (o.getUses() > 0) {
                    o.resetUses();
                    break;
                }
                lastRestock = now;
            }
            for (var offer : v.getOffers()) {
                if (offer.isOutOfStock()) continue;
                ItemStack a = offer.getCostA();
                ItemStack b = offer.getCostB();
                if (!b.isEmpty() || !ItemStack.isSameItemSameComponents(a, held) || held.getCount() < a.getCount()) continue;
                held.shrink(a.getCount());
                offer.increaseUses();
                ItemStack res = offer.getResult().copy();
                if (!player.getInventory().add(res)) player.drop(res, false);
                TagValueOutput out = TagValueOutput.createWithoutContext(ProblemReporter.DISCARDING);
                v.saveWithoutId(out);
                villagers.set(0, out.buildResult());
                setChanged();
                return true;
            }
            return false;
        }

        public void tick(ServerLevel level, int kind) {
            if (kind == 1) {
                if (villagers.size() < 2 || food < 12) return;
                if (++timer < 6000) return;
                timer = 0;
                food -= 12;
                push(level, babyStack(level));
                setChanged();
            } else if (kind == 2) {
                if (villagers.isEmpty()) return;
                if (++timer < 200) return;
                timer = 0;
                harvest(level);
            }
        }

        private void harvest(ServerLevel level) {
            BlockPos base = worldPosition.above();
            for (int dx = -2; dx <= 2; dx++) for (int dz = -2; dz <= 2; dz++) {
                BlockPos p = base.offset(dx, 0, dz);
                BlockState st = level.getBlockState(p);
                if (!(st.getBlock() instanceof net.minecraft.world.level.block.CropBlock crop) || !crop.isMaxAge(st)) continue;
                java.util.List<ItemStack> drops = Block.getDrops(st, level, p, null);
                level.setBlock(p, crop.getStateForAge(0), 3);
                for (ItemStack d : drops) push(level, d.copy());
            }
        }

        private void push(ServerLevel level, ItemStack stack) {
            Vec3 c = Vec3.atCenterOf(worldPosition.above());
            level.addFreshEntity(new net.minecraft.world.entity.item.ItemEntity(level, c.x, c.y, c.z, stack));
        }

        @Override
        protected void saveAdditional(ValueOutput out) {
            super.saveAdditional(out);
            out.putInt("Food", food);
            out.putInt("Timer", timer);
            out.putLong("Restock", lastRestock);
            ValueOutput.ValueOutputList list = out.childrenList("Villagers");
            for (CompoundTag t : villagers) list.addChild().store("V", CompoundTag.CODEC, t);
        }

        @Override
        protected void loadAdditional(ValueInput in) {
            super.loadAdditional(in);
            food = in.getIntOr("Food", 0);
            timer = in.getIntOr("Timer", 0);
            lastRestock = in.getLongOr("Restock", 0L);
            villagers.clear();
            for (ValueInput c : in.childrenListOrEmpty("Villagers")) c.read("V", CompoundTag.CODEC).ifPresent(villagers::add);
        }
    }
}
