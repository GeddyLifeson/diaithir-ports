// Ported from Structure Compass (MIT) https://github.com/Mrbysco/StructureCompass
// Modifications: Renamed from StructureItems, uses the diaithirstructurenavigation namespace.
package net.diaithir.structurenavigation.structurecompass.registry;

import java.util.function.Supplier;
import net.diaithir.structurenavigation.structurecompass.CompassIds;
import net.diaithir.structurenavigation.structurecompass.items.StructureCompassItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class CompassItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(CompassIds.MOD_ID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, CompassIds.MOD_ID);

    public static final DeferredItem<StructureCompassItem> STRUCTURE_COMPASS = ITEMS.registerItem("structure_compass",
            StructureCompassItem::new, () -> new Item.Properties().stacksTo(1));

    public static final Supplier<CreativeModeTab> COMPASS_TAB = CREATIVE_MODE_TABS.register("structure_compass_tab", () -> CreativeModeTab.builder()
            .icon(() -> STRUCTURE_COMPASS.get().getDefaultInstance())
            .title(Component.translatable("itemGroup.diaithirstructurenavigation.structurecompass"))
            .displayItems((parameters, output) -> output.accept(STRUCTURE_COMPASS.get()))
            .build());

    private CompassItems() {}
}
