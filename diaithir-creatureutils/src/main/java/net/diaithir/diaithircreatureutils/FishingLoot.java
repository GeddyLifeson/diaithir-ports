package net.diaithir.diaithircreatureutils;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.biome.Biome;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.ItemFishedEvent;

public final class FishingLoot {
    // two fish per biome group: river, ocean, swamp, cold ocean, jungle, taiga
    public static final String[] FISH = {
        "river_perch", "river_trout",
        "ocean_tuna", "ocean_snapper",
        "swamp_catfish", "swamp_bullhead",
        "cold_arctic_char", "cold_halibut",
        "jungle_piranha", "jungle_arapaima",
        "taiga_pike", "taiga_whitefish"
    };

    private FishingLoot() {}

    private static int group(Holder<Biome> biome) {
        if (biome.is(BiomeTags.IS_RIVER)) return 0;
        if (biome.is(BiomeTags.IS_OCEAN)) {
            String path = biome.unwrapKey().map(k -> k.identifier().getPath()).orElse("");
            return (path.contains("cold") || path.contains("frozen")) ? 3 : 1;
        }
        if (biome.is(BiomeTags.IS_JUNGLE)) return 4;
        if (biome.is(BiomeTags.IS_TAIGA)) return 5;
        String path = biome.unwrapKey().map(k -> k.identifier().getPath()).orElse("");
        if (path.contains("swamp") || path.contains("mangrove")) return 2;
        return -1;
    }

    private static Item modItem(String name) {
        return BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath(CreatureutilsMod.ID, name));
    }

    @SubscribeEvent
    public static void onFished(ItemFishedEvent event) {
        Player player = event.getEntity();
        var hook = event.getHookEntity();
        var level = hook.level();
        if (level.isClientSide()) return;
        int g = group(level.getBiome(hook.blockPosition()));
        if (g < 0) return;
        float chance = 0.35f;
        ItemStack off = player.getItemInHand(InteractionHand.OFF_HAND);
        if (off.is(modItem("worm_bait"))) {
            chance = 0.75f;
            off.shrink(1);
        }
        if (level.getRandom().nextFloat() >= chance) return;
        List<ItemStack> drops = event.getDrops();
        List<ItemStack> replaced = new ArrayList<>();
        for (ItemStack s : drops) {
            if (s.is(net.minecraft.tags.ItemTags.FISHES)) {
                String name = FISH[g * 2 + level.getRandom().nextInt(2)];
                replaced.add(new ItemStack(modItem(name)));
            } else {
                replaced.add(s);
            }
        }
        drops.clear();
        drops.addAll(replaced);
    }
}
