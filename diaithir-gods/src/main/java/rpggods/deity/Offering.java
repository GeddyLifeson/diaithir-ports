package rpggods.deity;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import rpggods.RPGGods;

import java.util.Optional;
import java.util.function.Function;

public class Offering {
    public static final Offering EMPTY = new Offering(Optional.empty(), Optional.empty(), 0, 0, 0, 0,
            Optional.empty(), Optional.empty(), 0, 0, Optional.empty(), Optional.empty());

    // Codec that accepts Item or ItemStack (decoded as a template because components are not bound at datapack load)
    public static final Codec<ItemStackTemplate> ITEM_OR_STACK_CODEC = ItemStackTemplate.CODEC;

    /** Converts a stack to an optional template. */
    public static Optional<ItemStackTemplate> toTemplate(ItemStack stack) {
        return stack.isEmpty() ? Optional.empty() : Optional.of(ItemStackTemplate.fromNonEmptyStack(stack));
    }

    /** Builds a real stack from an optional template, or EMPTY. */
    public static ItemStack toStack(Optional<ItemStackTemplate> template) {
        return template.map(ItemStackTemplate::create).orElse(ItemStack.EMPTY);
    }

    public static final Codec<Offering> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ITEM_OR_STACK_CODEC.optionalFieldOf("item").forGetter(o -> o.acceptTemplate),
            Codec.STRING.optionalFieldOf("item_tag").forGetter(Offering::getAcceptTagString),
            Codec.INT.optionalFieldOf("favor", 0).forGetter(Offering::getFavor),
            Codec.INT.optionalFieldOf("maxuses", 16).forGetter(Offering::getMaxUses),
            Codec.INT.optionalFieldOf("restocks", -1).forGetter(Offering::getRestocks),
            Codec.INT.optionalFieldOf("cooldown", 12000).forGetter(Offering::getCooldown),
            ITEM_OR_STACK_CODEC.optionalFieldOf("trade").forGetter(o -> o.tradeTemplate),
            Codec.STRING.optionalFieldOf("trade_tag").forGetter(Offering::getTradeTagString),
            Codec.INT.optionalFieldOf("minlevel", Integer.MIN_VALUE).forGetter(Offering::getTradeMinLevel),
            Codec.INT.optionalFieldOf("maxlevel", Integer.MAX_VALUE).forGetter(Offering::getTradeMaxLevel),
            Identifier.CODEC.optionalFieldOf("function").forGetter(Offering::getFunction),
            Codec.STRING.optionalFieldOf("function_text").forGetter(Offering::getFunctionText)
    ).apply(instance, Offering::new));

    private final Optional<ItemStackTemplate> acceptTemplate;
    private final Optional<ItemStackTemplate> tradeTemplate;
    private ItemStack accept;
    private final Optional<String> acceptTagString;
    private final Optional<CompoundTag> acceptTag;
    private final int favor;
    private Optional<ItemStack> trade;
    private final Optional<String> tradeTagString;
    private final Optional<CompoundTag> tradeTag;
    private final int minLevel;
    private final int maxLevel;
    private final Optional<Identifier> function;
    private final Optional<String> functionText;
    private final int maxUses;
    private final int restocks;
    private final int cooldown;

    public Offering(Optional<ItemStackTemplate> accept, Optional<String> acceptTagString, int favor, int maxUses,
                    int restocks, int cooldown, Optional<ItemStackTemplate> trade, Optional<String> tradeTagString,
                    int tradeMinLevel, int tradeMaxLevel,
                    Optional<Identifier> function, Optional<String> functionText) {
        this.acceptTemplate = accept;
        this.tradeTemplate = trade;
        this.acceptTagString = acceptTagString;
        this.favor = favor;
        this.restocks = restocks;
        this.maxUses = maxUses;
        this.cooldown = cooldown;
        this.tradeTagString = tradeTagString;
        this.minLevel = Math.min(tradeMinLevel, tradeMaxLevel);
        this.maxLevel = Math.max(tradeMinLevel, tradeMaxLevel);
        this.function = function;
        this.functionText = functionText;
        // parse accept tag
        Optional<CompoundTag> temp = Optional.empty();
        if (acceptTagString.isPresent()) {
            try {
                temp = Optional.of(TagParser.parseCompoundFully(acceptTagString.get()));
            } catch (CommandSyntaxException e) {
                RPGGods.LOGGER.error("Failed to parse item NBT in Offering\n" + e.getMessage());
            }
        }
        this.acceptTag = temp;
        // parse trade tag
        temp = Optional.empty();
        if (this.tradeTemplate.isPresent() && tradeTagString.isPresent()) {
            try {
                temp = Optional.of(TagParser.parseCompoundFully(tradeTagString.get()));
            } catch (CommandSyntaxException e) {
                RPGGods.LOGGER.error("Failed to parse trade NBT in Offering\n" + e.getMessage());
            }
        }
        this.tradeTag = temp;
    }

    /**
     * Attempts to parse the deity from the given offering id
     *
     * @param offeringId the offering id in the form {@code namespace:deity/offering}
     * @return the resource location if found, otherwise {@link DeityHelper#EMPTY}
     */
    public static Identifier getDeity(final Identifier offeringId) {
        String path = offeringId.getPath();
        int index = path.indexOf("/");
        if (index > -1) {
            return Identifier.fromNamespaceAndPath(offeringId.getNamespace(), path.substring(0, index));
        }
        return DeityHelper.EMPTY.id;
    }

    /**
     * Checks the given item stack to see if this offering can accept it.
     * Examines item, count, and enchantments
     *
     * @param offering another item stack
     * @return true if the given ItemStack matches the one in this offering
     */
    public boolean matches(ItemStack offering) {
        // check item and stack size
        if (!sameItemIgnoreDurability(getAccept(), offering) || offering.getCount() < getAccept().getCount()) {
            return false;
        }
        // check tag (stored in the custom_data component)
        if (this.acceptTag.isPresent()) {
            CustomData data = offering.get(DataComponents.CUSTOM_DATA);
            if (data == null || !NbtUtils.compareNbt(this.acceptTag.get(), data.copyTag(), true)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Compares item and components, ignoring damage and custom data (custom data is checked separately).
     */
    private static boolean sameItemIgnoreDurability(ItemStack expected, ItemStack other) {
        if (!ItemStack.isSameItem(expected, other)) {
            return false;
        }
        ItemStack a = expected.copy();
        ItemStack b = other.copy();
        a.remove(DataComponents.DAMAGE);
        b.remove(DataComponents.DAMAGE);
        a.remove(DataComponents.CUSTOM_DATA);
        b.remove(DataComponents.CUSTOM_DATA);
        // only components that the offering specifies must match
        for (var entry : a.getComponentsPatch().entrySet()) {
            if (entry.getValue().isPresent() && !entry.getValue().equals(b.getComponentsPatch().getPatch(entry.getKey()))) {
                return false;
            }
        }
        return true;
    }

    /**
     * ItemStack aware version of {@link #getTrade()} that allows
     * the trade item to copy NBT data and enchantments from the offering item.
     *
     * @param offering the offering item
     * @return the trade item (or empty if there is no trade)
     */
    public Optional<ItemStack> getTrade(final ItemStack offering) {
        // special handling of trade when same item and NBT is present
        if (getTrade().isPresent() && ItemStack.isSameItem(offering, getTrade().get()) && tradeTag.isPresent()) {
            // create copy of offering item with correct count
            ItemStack tradeItem = offering.copy();
            tradeItem.setCount(getTrade().get().getCount());
            // merge tags
            CustomData offeringData = tradeItem.get(DataComponents.CUSTOM_DATA);
            CompoundTag base = offeringData == null ? new CompoundTag() : offeringData.copyTag();
            CompoundTag tradeItemTag = merge(tradeTag.get(), base, 0);
            CustomData.set(DataComponents.CUSTOM_DATA, tradeItem, tradeItemTag);
            // merge damage
            if (offering.isDamageableItem() && offering.isDamaged()) {
                tradeItem.setDamageValue(offering.getDamageValue());
            }
            return Optional.of(tradeItem);
        }
        // copy trade item
        if (getTrade().isPresent()) {
            return Optional.of(getTrade().get().copy());
        }
        // no trade item
        return Optional.empty();
    }

    /**
     * Checks all of the values in main and merges them with the values in rec
     *
     * @param main  the CompoundNBT that serves as a template
     * @param rec   the CompoundNBT that will be modified
     * @param depth start at 0
     * @return the modified CompoundNBT
     */
    public static CompoundTag merge(CompoundTag main, CompoundTag rec, int depth) {
        // prevent tags that are too deep
        if (depth >= 512) {
            return rec;
        }
        // iterate over all values in main tag
        for (String key : main.keySet()) {
            Tag mnbt = main.get(key);
            // merge the tag value with the value in the other list
            if (mnbt instanceof CompoundTag) {
                CompoundTag merged = merge((CompoundTag) mnbt, rec.getCompoundOrEmpty(key), depth + 1);
                rec.put(key, merged);
            } else if (mnbt instanceof ListTag) {
                ListTag mList = (ListTag) mnbt;
                ListTag rList = rec.getListOrEmpty(key);
                ListTag merged = merge(mList, rList);
                rec.put(key, merged);
            } else {
                rec.put(key, mnbt.copy());
            }
        }
        return rec;
    }

    public static ListTag merge(ListTag main, ListTag rec) {
        // iterate through all items in list
        for (int m = 0, ml = main.size(); m < ml; m++) {
            Tag mItem = main.get(m);
            boolean hasItem = false;
            // attempt to locate the same item in the other list
            for (int r = 0, rl = rec.size(); r < rl; r++) {
                Tag rItem = rec.get(r);
                if (NbtUtils.compareNbt(mItem, rItem, true)) {
                    hasItem = true;
                    break;
                }
            }
            // add the item to the other list
            if (!hasItem) {
                rec.add(mItem);
            }
        }
        return rec;
    }

    /** @return the accepted item without building a stack (safe during datapack load) */
    public net.minecraft.world.item.Item getAcceptItem() {
        return acceptTemplate.map(t -> t.item().value()).orElse(net.minecraft.world.item.Items.AIR);
    }

    public ItemStack getAccept() {
        if (accept == null) {
            ItemStack stack = toStack(acceptTemplate);
            if (acceptTag.isPresent() && !stack.isEmpty()) {
                CustomData.set(DataComponents.CUSTOM_DATA, stack, acceptTag.get());
            }
            accept = stack;
        }
        return accept;
    }

    public Optional<String> getAcceptTagString() {
        return acceptTagString;
    }

    public Optional<CompoundTag> getAcceptTag() {
        return acceptTag;
    }

    public int getFavor() {
        return favor;
    }

    public int getMaxUses() {
        return maxUses;
    }

    public int getRestocks() {
        return restocks;
    }

    public int getCooldown() {
        return cooldown;
    }

    public Optional<ItemStack> getTrade() {
        if (trade == null) {
            trade = tradeTemplate.map(t -> {
                ItemStack stack = t.create();
                if (tradeTag.isPresent()) {
                    CustomData.set(DataComponents.CUSTOM_DATA, stack, tradeTag.get());
                }
                return stack;
            });
        }
        return trade;
    }

    public Optional<String> getTradeTagString() {
        return tradeTagString;
    }

    public Optional<CompoundTag> getTradeTag() {
        return tradeTag;
    }

    public int getTradeMinLevel() {
        return minLevel;
    }

    public int getTradeMaxLevel() {
        return maxLevel;
    }

    public Optional<Identifier> getFunction() {
        return function;
    }

    public Optional<String> getFunctionText() {
        return functionText;
    }

    public Cooldown createCooldown() {
        return new Cooldown(this.maxUses, this.cooldown, this.restocks);
    }

    public boolean hasLevelRange() {
        return hasMinLevel() || hasMaxLevel();
    }

    public boolean hasMinLevel() {
        return minLevel > Integer.MIN_VALUE;
    }

    public boolean hasMaxLevel() {
        return maxLevel < Integer.MAX_VALUE;
    }
}
