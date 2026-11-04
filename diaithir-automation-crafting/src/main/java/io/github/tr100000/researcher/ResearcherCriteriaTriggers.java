package io.github.tr100000.researcher;

import io.github.tr100000.researcher.criterion.BlockBrokenTrigger;
import io.github.tr100000.researcher.criterion.ItemCraftedTrigger;
import io.github.tr100000.researcher.criterion.ResearchItemsTrigger;
import io.github.tr100000.researcher.criterion.ResearchUnlockedTrigger;
import io.github.tr100000.trutils.api.registry.RegistryHelper;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.core.registries.BuiltInRegistries;
import org.jetbrains.annotations.ApiStatus;

public final class ResearcherCriteriaTriggers {
    private ResearcherCriteriaTriggers() {}

    public static final RegistryHelper<CriterionTrigger<?>> REGISTRY = new RegistryHelper<>(BuiltInRegistries.TRIGGER_TYPES, Researcher.MODID);

    public static final BlockBrokenTrigger BLOCK_BROKEN = REGISTRY.add(new BlockBrokenTrigger(), "block_broken");
    public static final ItemCraftedTrigger ITEM_CRAFTED = REGISTRY.add(new ItemCraftedTrigger(), "item_crafted");
    public static final ResearchItemsTrigger RESEARCH_ITEMS = REGISTRY.add(new ResearchItemsTrigger(), "research_items");
    public static final ResearchUnlockedTrigger HAS_RESEARCH = REGISTRY.add(new ResearchUnlockedTrigger(), "has_research");

    @ApiStatus.Internal
    public static void flush() {
        REGISTRY.flush();
    }
}
