package com.otectus.immersivetanning.registry;

import com.otectus.immersivetanning.ImmersiveTanning;
import com.otectus.immersivetanning.hide.HideSize;
import com.otectus.immersivetanning.item.AwlItem;
import com.otectus.immersivetanning.item.FinishedLeatherItem;
import com.otectus.immersivetanning.item.HintItem;
import com.otectus.immersivetanning.item.LeatherScrapsItem;
import com.otectus.immersivetanning.item.ProcessedHideItem;
import com.otectus.immersivetanning.item.RawHideItem;
import com.otectus.immersivetanning.item.StationBlockItem;
import com.otectus.immersivetanning.item.TannersKnifeItem;
import com.otectus.immersivetanning.leather.Finish;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.EnumMap;
import java.util.Map;

public final class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ImmersiveTanning.MOD_ID);

    public static final DeferredItem<Item> TANNERS_RACK = ITEMS.registerItem("tanners_rack",
            p -> new StationBlockItem(ModBlocks.TANNERS_RACK.get(), p), () -> new Item.Properties().useBlockDescriptionPrefix());
    public static final DeferredItem<Item> TANNERS_VAT = ITEMS.registerItem("tanners_vat",
            p -> new StationBlockItem(ModBlocks.TANNERS_VAT.get(), p), () -> new Item.Properties().useBlockDescriptionPrefix());
    public static final DeferredItem<Item> LEATHERWORKERS_BENCH = ITEMS.registerItem("leatherworkers_bench",
            p -> new StationBlockItem(ModBlocks.LEATHERWORKERS_BENCH.get(), p), () -> new Item.Properties().useBlockDescriptionPrefix());

    public static final Map<HideSize, DeferredItem<RawHideItem>> RAW_HIDES = new EnumMap<>(HideSize.class);

    static {
        for (HideSize size : HideSize.values()) {
            RAW_HIDES.put(size, ITEMS.registerItem(size.getSerializedName() + "_hide",
                    p -> new RawHideItem(size, p), () -> new Item.Properties().stacksTo(16)));
        }
    }

    public static final DeferredItem<ProcessedHideItem> SCRAPED_HIDE = ITEMS.registerItem("scraped_hide",
            p -> new ProcessedHideItem(ProcessedHideItem.Stage.SCRAPED, p), () -> new Item.Properties().stacksTo(16));
    public static final DeferredItem<ProcessedHideItem> WET_TANNED_HIDE = ITEMS.registerItem("wet_tanned_hide",
            p -> new ProcessedHideItem(ProcessedHideItem.Stage.WET, p), () -> new Item.Properties().stacksTo(16));

    public static final Map<Finish, DeferredItem<FinishedLeatherItem>> LEATHERS = new EnumMap<>(Finish.class);

    static {
        for (Finish finish : Finish.values()) {
            LEATHERS.put(finish, ITEMS.registerItem(finish.getSerializedName() + "_leather",
                    p -> new FinishedLeatherItem(finish, p), () -> new Item.Properties()));
        }
    }

    /** Replaces vanilla leather in every recipe, loot table and trade. See {@link com.otectus.immersivetanning.leather.BindingLeather}. */
    public static final DeferredItem<Item> BINDING_LEATHER = ITEMS.registerItem("binding_leather", HintItem::new, () -> new Item.Properties());
    public static final DeferredItem<LeatherScrapsItem> LEATHER_SCRAPS = ITEMS.registerItem("leather_scraps",
            LeatherScrapsItem::new, () -> new Item.Properties());
    public static final DeferredItem<Item> BARK = ITEMS.registerItem("bark", Item::new, () -> new Item.Properties());
    public static final DeferredItem<TannersKnifeItem> TANNERS_KNIFE = ITEMS.registerItem("tanners_knife",
            TannersKnifeItem::new, () -> new Item.Properties().durability(250).repairable(net.neoforged.neoforge.common.Tags.Items.INGOTS_IRON));
    public static final DeferredItem<AwlItem> AWL = ITEMS.registerItem("awl",
            AwlItem::new, () -> new Item.Properties().durability(200).repairable(net.neoforged.neoforge.common.Tags.Items.NUGGETS_IRON));

    public static RawHideItem rawHide(HideSize size) {
        return RAW_HIDES.get(size).get();
    }

    public static FinishedLeatherItem leather(Finish finish) {
        return LEATHERS.get(finish).get();
    }

    private ModItems() {}
}
