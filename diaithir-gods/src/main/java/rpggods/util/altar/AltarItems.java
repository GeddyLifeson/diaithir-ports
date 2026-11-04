package rpggods.util.altar;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import java.util.Optional;
import net.minecraft.core.registries.BuiltInRegistries;
import rpggods.deity.Offering;

public class AltarItems {

    public static final AltarItems EMPTY = new AltarItems(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(),
            Optional.empty(), Optional.empty(), Blocks.AIR, false, false, false);

    private static final Codec<ItemStackTemplate> ITEM_OR_STACK_CODEC = Offering.ITEM_OR_STACK_CODEC;

    public static final Codec<AltarItems> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ITEM_OR_STACK_CODEC.optionalFieldOf("head").forGetter(o -> o.templates.get(EquipmentSlot.HEAD)),
            ITEM_OR_STACK_CODEC.optionalFieldOf("chest").forGetter(o -> o.templates.get(EquipmentSlot.CHEST)),
            ITEM_OR_STACK_CODEC.optionalFieldOf("legs").forGetter(o -> o.templates.get(EquipmentSlot.LEGS)),
            ITEM_OR_STACK_CODEC.optionalFieldOf("feet").forGetter(o -> o.templates.get(EquipmentSlot.FEET)),
            ITEM_OR_STACK_CODEC.optionalFieldOf("mainhand").forGetter(o -> o.templates.get(EquipmentSlot.MAINHAND)),
            ITEM_OR_STACK_CODEC.optionalFieldOf("offhand").forGetter(o -> o.templates.get(EquipmentSlot.OFFHAND)),
            BuiltInRegistries.BLOCK.byNameCodec().optionalFieldOf("block",Blocks.AIR).forGetter(AltarItems::getBlock),
            Codec.BOOL.optionalFieldOf("armor_locked", false).forGetter(AltarItems::isArmorLocked),
            Codec.BOOL.optionalFieldOf("hands_locked", false).forGetter(AltarItems::isHandsLocked),
            Codec.BOOL.optionalFieldOf("block_locked", false).forGetter(AltarItems::isBlockLocked)
    ).apply(instance, AltarItems::new));

    private final java.util.Map<EquipmentSlot, Optional<ItemStackTemplate>> templates = new java.util.EnumMap<>(EquipmentSlot.class);
    private ImmutableList<ItemStack> handItems;
    private ImmutableList<ItemStack> armorItems;
    private final Block block;
    private final boolean armorLocked;
    private final boolean handsLocked;
    private final boolean blockLocked;

    public AltarItems(Optional<ItemStackTemplate> head, Optional<ItemStackTemplate> chest, Optional<ItemStackTemplate> legs, Optional<ItemStackTemplate> feet,
                      Optional<ItemStackTemplate> mainhand, Optional<ItemStackTemplate> offhand, Block block,
                      boolean armorLocked, boolean handsLocked, boolean blockLocked) {
        templates.put(EquipmentSlot.HEAD, head);
        templates.put(EquipmentSlot.CHEST, chest);
        templates.put(EquipmentSlot.LEGS, legs);
        templates.put(EquipmentSlot.FEET, feet);
        templates.put(EquipmentSlot.MAINHAND, mainhand);
        templates.put(EquipmentSlot.OFFHAND, offhand);
        this.block = block;
        this.armorLocked = armorLocked;
        this.handsLocked = handsLocked;
        this.blockLocked = blockLocked;
    }

    private void build() {
        if (handItems == null) {
            handItems = ImmutableList.of(Offering.toStack(templates.get(EquipmentSlot.MAINHAND)), Offering.toStack(templates.get(EquipmentSlot.OFFHAND)));
            armorItems = ImmutableList.of(Offering.toStack(templates.get(EquipmentSlot.HEAD)), Offering.toStack(templates.get(EquipmentSlot.CHEST)),
                    Offering.toStack(templates.get(EquipmentSlot.LEGS)), Offering.toStack(templates.get(EquipmentSlot.FEET)));
        }
    }

    public ItemStack getItemStackFromSlot(EquipmentSlot slotIn) {
        build();
        switch(slotIn) {
            case MAINHAND: return handItems.get(0);
            case OFFHAND: return handItems.get(1);
            case HEAD: return armorItems.get(0);
            case CHEST: return armorItems.get(1);
            case LEGS: return armorItems.get(2);
            case FEET: return armorItems.get(3);
            default: return ItemStack.EMPTY;
        }
    }

    public Block getBlock() {
        return block;
    }

    public boolean isBlockLocked() {
        return blockLocked;
    }

    public boolean isArmorLocked() {
        return armorLocked;
    }

    public boolean isHandsLocked() {
        return handsLocked;
    }

    @Override
    public String toString() {
        final StringBuilder b = new StringBuilder("AltarItems:");
        build();
        b.append(" hands[").append(handItems.toString()).append("]");
        b.append(" armor[").append(armorItems.toString()).append("]");
        b.append(" block[").append(block.toString()).append("]");
        b.append(" armor_locked[").append(armorLocked).append("]");
        b.append(" hands_locked[").append(handsLocked).append("]");
        b.append(" block_locked[").append(blockLocked).append("]");
        return b.toString();
    }
}
