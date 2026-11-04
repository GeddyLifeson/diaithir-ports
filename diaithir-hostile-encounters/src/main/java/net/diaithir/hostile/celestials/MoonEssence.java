package net.diaithir.hostile.celestials;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Dropped by boosted mobs on a blood moon; using it breaks it open for experience, like a bottle o' enchanting. */
public final class MoonEssence extends Item {
    public static final int XP = 18;
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(DiaithirHostile.ID);
    public static final DeferredItem<Item> ITEM = ITEMS.registerItem("moon_essence", MoonEssence::new,
        p -> p.stacksTo(64).setId(ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(DiaithirHostile.ID, "moon_essence"))));

    public MoonEssence(Item.Properties props) { super(props); }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level instanceof ServerLevel sl) {
            ExperienceOrb.award(sl, player.position(), XP);
            stack.shrink(1);
        }
        return InteractionResult.SUCCESS;
    }
}
