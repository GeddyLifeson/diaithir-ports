package net.diaithir.creatureutilities.guards;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.tags.ItemTags;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Registration for the guard villager fold (source: Guard Villagers Fabric, CC0). */
public final class Guards {
    private static final String ID = net.diaithir.creatureutilities.CreatureUtilities.ID;
    private static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(Registries.ENTITY_TYPE, ID);

    public static final DeferredHolder<EntityType<?>, EntityType<GuardEntity>> GUARD = ENTITIES.register("guard", () ->
        EntityType.Builder.of(GuardEntity::new, MobCategory.MISC).sized(0.6F, 1.95F).clientTrackingRange(10)
            .build(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(ID, "guard"))));

    private Guards() {}

    public static void init(IEventBus bus) {
        ENTITIES.register(bus);
        bus.addListener((EntityAttributeCreationEvent e) -> e.put(GUARD.get(), GuardEntity.attributes().build()));
        NeoForge.EVENT_BUS.addListener(Guards::interact);
    }

    /** Sneak-use a sword on an adult villager to turn it into a guard. */
    private static void interact(PlayerInteractEvent.EntityInteract e) {
        if (e.getLevel().isClientSide() || !e.getEntity().isShiftKeyDown()) return;
        if (!(e.getTarget() instanceof Villager v) || v.isBaby()) return;
        ItemStack held = e.getItemStack();
        if (!held.is(ItemTags.SWORDS)) return;
        GuardEntity g = GuardEntity.convert(v, held.copyWithCount(1));
        if (g != null) {
            if (!e.getEntity().getAbilities().instabuild) held.shrink(1);
            e.setCancellationResult(InteractionResult.SUCCESS);
            e.setCanceled(true);
        }
    }
}
