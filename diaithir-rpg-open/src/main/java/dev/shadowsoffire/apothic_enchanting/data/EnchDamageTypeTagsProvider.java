package dev.shadowsoffire.apothic_enchanting.data;

import java.util.concurrent.CompletableFuture;

import dev.shadowsoffire.apothic_enchanting.ApothicEnchanting;
import dev.shadowsoffire.apothic_enchanting.Ench;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.DamageTypeTagsProvider;
import net.minecraft.tags.DamageTypeTags;
import net.neoforged.neoforge.common.Tags;

public class EnchDamageTypeTagsProvider extends DamageTypeTagsProvider {

    public EnchDamageTypeTagsProvider(PackOutput output, CompletableFuture<Provider> lookupProvider) {
        super(output, lookupProvider, ApothicEnchanting.MODID);
    }

    @Override
    protected void addTags(Provider registries) {
        this.tag(DamageTypeTags.BYPASSES_ARMOR).add(Ench.DamageTypes.CORRUPTED);
        this.tag(DamageTypeTags.BYPASSES_EFFECTS).add(Ench.DamageTypes.CORRUPTED);
        this.tag(DamageTypeTags.BYPASSES_COOLDOWN).add(Ench.DamageTypes.CORRUPTED);
        this.tag(DamageTypeTags.NO_IMPACT).add(Ench.DamageTypes.CORRUPTED);
        this.tag(Tags.DamageTypes.NO_FLINCH).add(Ench.DamageTypes.CORRUPTED);
    }
}
