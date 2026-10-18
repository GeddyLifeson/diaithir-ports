package com.ldtteam.blockui.controls;

import com.ldtteam.blockui.BOGuiGraphics;
import com.ldtteam.blockui.PaneParams;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Useful for overriding things like clock/compass textures. In xml defined through {@value #PARAM_PROPERTIES} key using nbt:
 * {<item registry key>:{<property name>:<float value>, ...}, ...}.
 * <p>
 * Special keys: {@value #NBT_CURRENT_ITEM} - refers to xml item (resolved during parsing not dynamic),
 * {@value #NBT_GENERIC_KEY} - generic properties
 * 
 * Dropped in the 26.1.2 port: vanilla item property overrides are gone, the values are stored but not applied when drawing.
 */
@SuppressWarnings("deprecation")
public class ItemIconWithProperties extends ItemIcon
{
    @FunctionalInterface
    public interface ItemPropertyFunction
    {
        float call(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity, int seed);
    }

    private static final String NBT_GENERIC_KEY = "_generic";
    private static final String NBT_CURRENT_ITEM = "_item";

    public static final String PARAM_PROPERTIES = "properties";

    protected final Map<Identifier, ItemPropertyFunction>            genericPropertyOverrides = new HashMap<>();
    protected final Map<Item, Map<Identifier, ItemPropertyFunction>> itemPropertyOverrides    = new HashMap<>();
    private Map<Identifier, ItemPropertyFunction>                    currentItemOverrides     = Collections.emptyMap();

    public ItemIconWithProperties()
    {
        super();
    }

    public ItemIconWithProperties(final PaneParams paneParams)
    {
        super(paneParams);

        final String data = paneParams.getString(PARAM_PROPERTIES, "");
        if (data != null && itemStack != null)
        {
            final CompoundTag tag;
            try
            {
                tag = TagParser.parseCompoundFully(data);
            }
            catch (CommandSyntaxException e)
            {
                throw new RuntimeException(data, null);
            }
            tag.keySet().forEach(itemKey -> {
                if (tag.getCompound(itemKey).isPresent())
                {
                    final CompoundTag child = tag.getCompoundOrEmpty(itemKey);
                    final var itemOverrides = NBT_GENERIC_KEY.equals(itemKey) ? genericPropertyOverrides :
                        itemPropertyOverrides.computeIfAbsent(NBT_CURRENT_ITEM.equals(itemKey) ? itemStack.getItem() :
                            BuiltInRegistries.ITEM.getValue(Identifier.parse(itemKey)), i -> new HashMap<>());

                    child.keySet().forEach(key -> {
                        child.getFloat(key).ifPresent(value -> itemOverrides.put(Identifier.parse(key), (stack, level, entity, seed) -> value));
                    });
                }
            });

            onItemUpdate();
        }
    }

    /**
     * Short call for adding itemProperty to current item
     */
    public void addPropertyForCurrentItem(final Identifier propertyKey, final ItemPropertyFunction property)
    {
        itemPropertyOverrides
            .computeIfAbsent(Objects.requireNonNull(itemStack, "Call #setItem before this method").getItem(), item -> new HashMap<>())
            .put(propertyKey, property);
    }

    /**
     * @return modifiable all item-based overrides
     */
    public Map<Item, Map<Identifier, ItemPropertyFunction>> getItemPropertyOverrides()
    {
        return itemPropertyOverrides;
    }

    /**
     * @return modifiable generic overrides
     */
    public Map<Identifier, ItemPropertyFunction> getGenericPropertyOverrides()
    {
        return genericPropertyOverrides;
    }

    @Override
    protected void onItemUpdate()
    {
        super.onItemUpdate();
        if (itemPropertyOverrides != null) // ctor race condition
        {
            currentItemOverrides = itemPropertyOverrides.getOrDefault(itemStack.getItem(), Collections.emptyMap());
        }
    }
}
