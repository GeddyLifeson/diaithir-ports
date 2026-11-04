/* SPDX-License-Identifier: MIT */

package li.cil.oc2.data;

import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;

import java.util.concurrent.CompletableFuture;

// TODO(oc2-restore): ItemModelProvider, ItemModelBuilder and ExistingFileHelper no longer exist in 26.1.2.
//  The original provider generated the following item models, which need to be restored with the new
//  model/item-definition data generation API (or as static JSON assets):
//  - "item/generated" with layer0 = "item/<name>" for all simple items (wrench, manual, network cable, memory,
//    CPUs, silicon items, flash memory, cards, modules, transistor, circuit board)
//  - hard drives (small/medium/large/extra large): layer0 = "item/hard_drive_base", layer1 = "item/hard_drive_tint"
//  - flash_memory_custom: layer0 = "item/flash_memory"
//  - floppy and floppy_modern: layer0 = "item/floppy_base", layer1 = "item/floppy_tint"
//  - robot: parent "template_shulker_box"
public final class ModItemModelProvider implements DataProvider {
    public ModItemModelProvider(final PackOutput output) {
    }

    @Override
    public CompletableFuture<?> run(final CachedOutput output) {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public String getName() {
        return "OpenComputers II Item Models";
    }
}
