package com.diaithir.addons.newage;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Minimal direct-registration helper that keeps the upstream registration code readable (the port has no Registrate). */
public final class Reg {
    public static final String ID = CreateNewAge.MOD_ID;
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(ID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ID);
    public static final DeferredRegister<BlockEntityType<?>> TILES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, ID);
    /** Items shown in the creative tab, in registration order. */
    public static final List<Supplier<? extends ItemLike>> TAB = new ArrayList<>();
    /** Run after registries are frozen (stress values and similar). */
    public static final List<Runnable> POST = new ArrayList<>();

    private Reg() {}

    public static void register(IEventBus bus) {
        BLOCKS.register(bus);
        ITEMS.register(bus);
        TILES.register(bus);
    }

    public static <B extends Block> BlockB<B> block(String name, Function<BlockBehaviour.Properties, B> f) {
        return new BlockB<>(name, f);
    }

    public static <I extends Item> ItemB<I> item(String name, Function<Item.Properties, I> f) {
        return new ItemB<>(name, f);
    }

    public static <T extends BlockEntity> TileB<T> blockEntity(String name, TileCtor<T> c) {
        return new TileB<>(name, c);
    }

    public interface TileCtor<T extends BlockEntity> {
        T create(BlockEntityType<T> type, BlockPos pos, BlockState state);
    }

    public static final class BlockB<B extends Block> {
        final String name;
        final Function<BlockBehaviour.Properties, B> factory;
        Supplier<? extends Block> initial;
        final List<UnaryOperator<BlockBehaviour.Properties>> props = new ArrayList<>();
        BiFunction<B, Item.Properties, ? extends Item> itemFactory;
        final List<Runnable> afterRegister = new ArrayList<>();
        DeferredBlock<B> entry;

        BlockB(String name, Function<BlockBehaviour.Properties, B> f) {
            this.name = name;
            this.factory = f;
        }

        public BlockB<B> initialProperties(Supplier<? extends Block> s) { initial = s; return this; }
        public BlockB<B> properties(UnaryOperator<BlockBehaviour.Properties> p) { props.add(p); return this; }
        public BlockB<B> transform(UnaryOperator<BlockB<B>> t) { return t.apply(this); }
        public BlockB<B> setImpact(double v) { afterRegister.add(() -> com.zurrtum.create.infrastructure.config.CStress.setImpact(entry.get(), v)); return this; }
        public BlockB<B> setCapacity(double v) { afterRegister.add(() -> com.zurrtum.create.infrastructure.config.CStress.setCapacity(entry.get(), v)); return this; }
        public ItemB2<B, com.diaithir.addons.newage.content.TooltipBlockItem> item() { return new ItemB2<>(this, (b, p) -> new com.diaithir.addons.newage.content.TooltipBlockItem(b, p)); }
        public <I extends Item> ItemB2<B, I> item(BiFunction<B, Item.Properties, I> f) { return new ItemB2<>(this, f); }
        public BlockB<B> build() { return this; }
        public BlockB<B> simpleItem() { itemFactory = (b, p) -> new com.diaithir.addons.newage.content.TooltipBlockItem(b, p); return this; }

        public DeferredBlock<B> register() {
            entry = BLOCKS.registerBlock(name, factory, () -> {
                BlockBehaviour.Properties p = initial != null
                        ? BlockBehaviour.Properties.ofFullCopy(initial.get())
                        : BlockBehaviour.Properties.of().strength(2.0f);
                for (UnaryOperator<BlockBehaviour.Properties> u : props) p = u.apply(p);
                return p;
            });
            if (itemFactory != null) {
                DeferredItem<? extends Item> it = ITEMS.registerItem(name,
                        p -> itemFactory.apply(entry.get(), p.useBlockDescriptionPrefix()));
                TAB.add(it);
            }
            POST.addAll(afterRegister);
            return entry;
        }
    }

    public static final class ItemB2<B extends Block, I extends Item> {
        final BlockB<B> parent;
        final BiFunction<B, Item.Properties, I> f;
        ItemB2(BlockB<B> parent, BiFunction<B, Item.Properties, I> f) { this.parent = parent; this.f = f; }
        public ItemB2<B, I> transform(UnaryOperator<ItemB2<B, I>> t) { return t.apply(this); }
        public BlockB<B> build() { parent.itemFactory = f; return parent; }
        public DeferredBlock<B> register() { return build().register(); }
    }

    public static final class ItemB<I extends Item> {
        final String name;
        final Function<Item.Properties, I> f;
        boolean tab = true;
        ItemB(String name, Function<Item.Properties, I> f) { this.name = name; this.f = f; }
        public ItemB<I> removeTab(Object ignored) { tab = false; return this; }
        public DeferredItem<I> register() {
            DeferredItem<I> e = ITEMS.registerItem(name, f::apply);
            if (tab) TAB.add(e);
            return e;
        }
    }

    public static final class TileB<T extends BlockEntity> {
        final String name;
        final TileCtor<T> c;
        final List<Supplier<? extends Block>> valid = new ArrayList<>();
        TileB(String name, TileCtor<T> c) { this.name = name; this.c = c; }
        @SafeVarargs
        public final TileB<T> validBlocks(Supplier<? extends Block>... bs) { valid.addAll(List.of(bs)); return this; }
        @SuppressWarnings("unchecked")
        public DeferredHolder<BlockEntityType<?>, BlockEntityType<T>> register() {
            BlockEntityType<T>[] self = new BlockEntityType[1];
            return TILES.register(name, () -> {
                java.util.Set<Block> set = new java.util.HashSet<>();
                for (Supplier<? extends Block> s : valid) set.add(s.get());
                return self[0] = new BlockEntityType<T>((p, s) -> c.create(self[0], p, s), set);
            });
        }
    }
}
