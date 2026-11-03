package net.diaithir.aurorian;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * The Aurorian: port of shiroroku/TheAurorian (MIT). Items, tool and armour tiers, building blocks and the dimension are
 * described by data/theaurorian/aurorian.json (written by tools/port.py from the upstream source).
 */
@Mod(DiaithirAurorian.ID)
public final class DiaithirAurorian {
    public static final String ID = "theaurorian";
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ID);
    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(ID);
    private static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ID);
    private static final Map<String, DeferredItem<Item>> ALL = new LinkedHashMap<>();
    private static final Map<String, DeferredBlock<Block>> BLK = new LinkedHashMap<>();
    private static final Map<String, ArmorMaterial> ARMOR = new LinkedHashMap<>();
    private static final Map<String, ToolMaterial> TOOLS = new LinkedHashMap<>();

    public record ToolMat(int durability, float speed, float bonus, int enchant, String repair, String incorrect) {}
    public record ArmorMat(int durability, int boots, int legs, int chest, int helmet, int enchant, String sound,
                           float toughness, float knockback, String repair) {}
    public record ItemSpec(String id, String kind, String mat, String slot, Float damage, Float speed, Integer durability,
                           Integer nutrition, Float saturation, Boolean fast, String rarity, List<Effect> effects) {}
    public record Effect(String id, int ticks, int amp, float chance) {}
    public record BlockSpec(String id, String kind, String copy, String base, Boolean unbreakable, Integer light, String key, Boolean lockpick) {}
    public record Data(Map<String, ToolMat> tools, Map<String, ArmorMat> armor, List<ItemSpec> items, List<BlockSpec> blocks) {}

    public static final Data DATA = load();
    public static DeferredBlock<Block> PORTAL;
    public static DeferredBlock<Block> PORTAL_FRAME;

    private static Data load() {
        try (Reader r = new InputStreamReader(DiaithirAurorian.class.getResourceAsStream("/data/" + ID + "/aurorian.json"))) {
            Type t = new TypeToken<Data>() {}.getType();
            return new Gson().fromJson(r, t);
        } catch (Exception e) {
            throw new IllegalStateException("aurorian.json missing", e);
        }
    }

    private static TagKey<Item> repairTag(String name) {
        return TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(ID, "repair/" + name));
    }

    private static Holder<SoundEvent> sound(String s) {
        return switch (s) {
            case "leather" -> SoundEvents.ARMOR_EQUIP_LEATHER;
            case "diamond" -> SoundEvents.ARMOR_EQUIP_DIAMOND;
            case "netherite" -> SoundEvents.ARMOR_EQUIP_NETHERITE;
            default -> SoundEvents.ARMOR_EQUIP_IRON;
        };
    }

    private static TagKey<Block> incorrect(String s) {
        return switch (s) {
            case "wood" -> BlockTags.INCORRECT_FOR_WOODEN_TOOL;
            case "stone" -> BlockTags.INCORRECT_FOR_STONE_TOOL;
            case "iron" -> BlockTags.INCORRECT_FOR_IRON_TOOL;
            default -> BlockTags.INCORRECT_FOR_DIAMOND_TOOL;
        };
    }

    static ArmorMaterial armorMaterial(String mat) {
        return ARMOR.computeIfAbsent(mat, k -> {
            ArmorMat m = DATA.armor().get(k);
            Map<ArmorType, Integer> d = new EnumMap<>(ArmorType.class);
            d.put(ArmorType.BOOTS, m.boots());
            d.put(ArmorType.LEGGINGS, m.legs());
            d.put(ArmorType.CHESTPLATE, m.chest());
            d.put(ArmorType.HELMET, m.helmet());
            d.put(ArmorType.BODY, m.chest());
            ResourceKey<EquipmentAsset> asset = ResourceKey.create(EquipmentAssets.ROOT_ID, Identifier.fromNamespaceAndPath(ID, k));
            return new ArmorMaterial(m.durability(), d, m.enchant(), sound(m.sound()), m.toughness(), m.knockback(), repairTag(k), asset);
        });
    }

    static ToolMaterial toolMaterial(String mat) {
        return TOOLS.computeIfAbsent(mat, k -> {
            ToolMat m = DATA.tools().get(k);
            return new ToolMaterial(incorrect(m.incorrect()), m.durability(), m.speed(), m.bonus(), m.enchant(), repairTag(k));
        });
    }

    private static Item.Properties props(ItemSpec s) {
        Item.Properties p = switch (s.kind()) {
            case "armor" -> new Item.Properties().humanoidArmor(armorMaterial(s.mat()), ArmorType.valueOf(s.slot().toUpperCase()));
            case "sword" -> new Item.Properties().sword(toolMaterial(s.mat()), s.damage(), s.speed());
            case "pickaxe" -> new Item.Properties().pickaxe(toolMaterial(s.mat()), s.damage(), s.speed());
            case "food", "drink" -> food(s);
            default -> new Item.Properties();
        };
        if (s.durability() != null && s.kind().equals("item")) {
            p = p.durability(s.durability());
        }
        if (s.rarity() != null) {
            p = p.rarity(Rarity.valueOf(s.rarity().toUpperCase()));
        }
        return p;
    }

    private static Item.Properties food(ItemSpec s) {
        boolean drink = s.kind().equals("drink");
        Item.Properties p = new Item.Properties().food(new FoodProperties(s.nutrition(), s.saturation(), drink || s.effects() != null));
        Consumable.Builder c = Consumable.builder().consumeSeconds(Boolean.TRUE.equals(s.fast()) ? 0.8F : 1.6F)
            .animation(drink ? ItemUseAnimation.DRINK : ItemUseAnimation.EAT).sound(drink ? SoundEvents.GENERIC_DRINK : SoundEvents.GENERIC_EAT)
            .hasConsumeParticles(!drink);
        if (s.effects() != null) {
            for (Effect e : s.effects()) {
                Holder<MobEffect> eff = BuiltInRegistries.MOB_EFFECT.get(Identifier.withDefaultNamespace(e.id())).orElseThrow();
                c.onConsume(new ApplyStatusEffectsConsumeEffect(new MobEffectInstance(eff, e.ticks(), e.amp()), e.chance()));
            }
        }
        return p.component(DataComponents.CONSUMABLE, c.build());
    }

    private static Item make(ItemSpec s, Item.Properties p) {
        return switch (s.kind()) {
            case "axe" -> new AxeItem(toolMaterial(s.mat()), s.damage(), s.speed(), p);
            case "shovel" -> new ShovelItem(toolMaterial(s.mat()), s.damage(), s.speed(), p);
            case "hoe" -> new HoeItem(toolMaterial(s.mat()), s.damage(), s.speed(), p);
            default -> new Item(p);
        };
    }

    private static Block block(String id) {
        return id.startsWith("minecraft:") ? BuiltInRegistries.BLOCK.getValue(Identifier.parse(id)) : BLK.get(id.substring(ID.length() + 1)).get();
    }

    private static BlockBehaviour.Properties blockProps(BlockSpec b) {
        BlockBehaviour.Properties p = BlockBehaviour.Properties.ofFullCopy(block(b.copy()));
        if (Boolean.TRUE.equals(b.unbreakable())) {
            p = p.strength(-1.0F, 3600000.0F);
        }
        if (b.light() != null) {
            int l = b.light();
            p = p.lightLevel(s -> l);
        }
        return p;
    }

    private static Block makeBlock(BlockSpec b, BlockBehaviour.Properties p) {
        return switch (b.kind()) {
            case "slab" -> new SlabBlock(p);
            case "stairs" -> new StairBlock(block(b.base()).defaultBlockState(), p);
            case "wall" -> new WallBlock(p);
            case "fence" -> new FenceBlock(p);
            case "keyhole" -> new GateKeyholeBlock(p, Identifier.parse(b.key()), Boolean.TRUE.equals(b.lockpick()));
            case "portal" -> new AurorianPortalBlock(p.noCollision().strength(-1.0F).lightLevel(s -> 11).noLootTable().pushReaction(net.minecraft.world.level.material.PushReaction.BLOCK));
            case "portal_frame" -> new PortalFrameBlock(p);
            case "bars" -> new IronBarsBlock(p);
            case "log" -> new RotatedPillarBlock(p);
            case "leaves" -> new net.minecraft.world.level.block.TintedParticleLeavesBlock(0.01F, p.noOcclusion());
            case "plant" -> new net.minecraft.world.level.block.BushBlock(p.noCollision().instabreak().noOcclusion());
            default -> new Block(p);
        };
    }

    public DiaithirAurorian(IEventBus bus) {
        for (BlockSpec b : DATA.blocks()) {
            DeferredBlock<Block> blk = BLOCKS.registerBlock(b.id(), p -> makeBlock(b, p), () -> blockProps(b));
            BLK.put(b.id(), blk);
            if (b.id().equals("aurorian_portal")) {
                PORTAL = blk;
            } else if (b.id().equals("aurorian_portal_frame")) {
                PORTAL_FRAME = blk;
            }
            ALL.put(b.id(), ITEMS.registerItem(b.id(), p -> new net.minecraft.world.item.BlockItem(blk.get(), p), () -> new Item.Properties().useBlockDescriptionPrefix()));
        }
        for (ItemSpec s : DATA.items()) {
            ALL.put(s.id(), ITEMS.registerItem(s.id(), p -> make(s, p), () -> props(s)));
        }
        DeferredHolder<CreativeModeTab, CreativeModeTab> tab = TABS.register("aurorian", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.theaurorian"))
            .icon(() -> new ItemStack(ALL.get("aurorian_steel_ingot").get()))
            .displayItems((params, out) -> ALL.values().forEach(i -> out.accept(i.get()))).build());
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(net.neoforged.neoforge.event.RegisterCommandsEvent.class, e -> SelfTest.register(e.getDispatcher()));
        BLOCKS.register(bus);
        ITEMS.register(bus);
        TABS.register(bus);
    }
}
