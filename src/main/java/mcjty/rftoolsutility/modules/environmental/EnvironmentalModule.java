package mcjty.rftoolsutility.modules.environmental;

import net.minecraft.world.item.crafting.RecipeSerializer;
import mcjty.rftoolsutility.modules.environmental.recipes.SyringeBasedRecipe;
import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.blocks.RBlock;
import mcjty.lib.container.GenericContainer;
import mcjty.lib.modules.IModule;
import mcjty.rftoolsutility.RFToolsUtility;
import mcjty.rftoolsutility.modules.environmental.blocks.EnvironmentalControllerTileEntity;
import mcjty.rftoolsutility.modules.environmental.client.ClientSetup;
import mcjty.rftoolsutility.modules.environmental.client.EnvironmentalRenderer;
import mcjty.rftoolsutility.modules.environmental.client.GuiEnvironmentalController;
import mcjty.rftoolsutility.modules.environmental.data.EnvironmentalData;
import mcjty.rftoolsutility.modules.environmental.items.EnvironmentalControllerItem;
import mcjty.rftoolsutility.modules.environmental.recipes.SyringeRecipeType;
import mcjty.rftoolsutility.setup.Config;
import mcjty.rftoolsutility.setup.Registration;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;

import java.util.function.Supplier;

import static mcjty.rftoolsutility.RFToolsUtility.tab;
import static mcjty.rftoolsutility.setup.Registration.*;

public class EnvironmentalModule implements IModule {

    public static final RBlock<BaseBlock, BlockItem, EnvironmentalControllerTileEntity> ENVIRONENTAL_CONTROLLER = RBLOCKS.registerBlock("environmental_controller",
            EnvironmentalControllerTileEntity.class,
            EnvironmentalControllerTileEntity::createBlock,
            block -> new BlockItem(block.get(), Registration.createStandardProperties()),
            EnvironmentalControllerTileEntity::new
    );
    public static final Supplier<MenuType<GenericContainer>> CONTAINER_ENVIRONENTAL_CONTROLLER = CONTAINERS.register("environmental_controller", GenericContainer::createContainerType);

    public static final DeferredItem<Item> MODULE_TEMPLATE = ITEMS.register("module_template", tab(() -> new Item(createStandardProperties())));
    public static final DeferredItem<Item> MODULEPLUS_TEMPLATE = ITEMS.register("moduleplus_template", tab(() -> new Item(createStandardProperties())));

    public static final DeferredItem<EnvironmentalControllerItem> BLINDNESS_MODULE = ITEMS.register("blindness_module", tab(EnvironmentalControllerItem::createBlindnessModule));
    public static final DeferredItem<EnvironmentalControllerItem> FEATHERFALLING_MODULE = ITEMS.register("featherfalling_module", tab(EnvironmentalControllerItem::createFeatherfallingModule));
    public static final DeferredItem<EnvironmentalControllerItem> FEATHERFALLINGPLUS_MODULE = ITEMS.register("featherfallingplus_module", tab(EnvironmentalControllerItem::createFeatherfallingPlusModule));
    public static final DeferredItem<EnvironmentalControllerItem> HASTE_MODULE = ITEMS.register("haste_module", tab(EnvironmentalControllerItem::createHasteModule));
    public static final DeferredItem<EnvironmentalControllerItem> HASTEPLUS_MODULE = ITEMS.register("hasteplus_module", tab(EnvironmentalControllerItem::createHastePlusModule));
    public static final DeferredItem<EnvironmentalControllerItem> FLIGHT_MODULE = ITEMS.register("flight_module", tab(EnvironmentalControllerItem::createFlightModule));
    public static final DeferredItem<EnvironmentalControllerItem> GLOWING_MODULE = ITEMS.register("glowing_module", tab(EnvironmentalControllerItem::createGlowingModule));
    public static final DeferredItem<EnvironmentalControllerItem> LUCK_MODULE = ITEMS.register("luck_module", tab(EnvironmentalControllerItem::createLuckModule));
    public static final DeferredItem<EnvironmentalControllerItem> NIGHTVISION_MODULE = ITEMS.register("nightvision_module", tab(EnvironmentalControllerItem::createNightvisionModule));
    public static final DeferredItem<EnvironmentalControllerItem> NOTELEPORT_MODULE = ITEMS.register("noteleport_module", tab(EnvironmentalControllerItem::createNoteleportModule));
    public static final DeferredItem<EnvironmentalControllerItem> PEACEFUL_MODULE = ITEMS.register("peaceful_module", tab(EnvironmentalControllerItem::createPeacefulModule));
    public static final DeferredItem<EnvironmentalControllerItem> POISON_MODULE = ITEMS.register("poison_module", tab(EnvironmentalControllerItem::createPoisonModule));
    public static final DeferredItem<EnvironmentalControllerItem> REGENERATION_MODULE = ITEMS.register("regeneration_module", tab(EnvironmentalControllerItem::createRegenerationModule));
    public static final DeferredItem<EnvironmentalControllerItem> REGENERATIONPLUS_MODULE = ITEMS.register("regenerationplus_module", tab(EnvironmentalControllerItem::createRegenerationPlusModule));
    public static final DeferredItem<EnvironmentalControllerItem> SATURATION_MODULE = ITEMS.register("saturation_module", tab(EnvironmentalControllerItem::createSaturationModule));
    public static final DeferredItem<EnvironmentalControllerItem> SATURATIONPLUS_MODULE = ITEMS.register("saturationplus_module", tab(EnvironmentalControllerItem::createSaturationPlusModule));
    public static final DeferredItem<EnvironmentalControllerItem> SLOWNESS_MODULE = ITEMS.register("slowness_module", tab(EnvironmentalControllerItem::createSlownessModule));
    public static final DeferredItem<EnvironmentalControllerItem> SPEED_MODULE = ITEMS.register("speed_module", tab(EnvironmentalControllerItem::createSpeedModule));
    public static final DeferredItem<EnvironmentalControllerItem> SPEEDPLUS_MODULE = ITEMS.register("speedplus_module", tab(EnvironmentalControllerItem::createSpeedPlusModule));
    public static final DeferredItem<EnvironmentalControllerItem> WATERBREATHING_MODULE = ITEMS.register("waterbreathing_module", tab(EnvironmentalControllerItem::createWaterbreathingModule));
    public static final DeferredItem<EnvironmentalControllerItem> WEAKNESS_MODULE = ITEMS.register("weakness_module", tab(EnvironmentalControllerItem::createWeaknessModule));

    public static final Supplier<AttachmentType<EnvironmentalData>> ENVIRONMENTAL_DATA = ATTACHMENT_TYPES.register(
            "environmental_data", () -> AttachmentType.builder(EnvironmentalData::createDefault)
                    .serialize(EnvironmentalData.CODEC.fieldOf("value"))
                    .build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<EnvironmentalData>> ITEM_ENVIRONMENTAL_DATA = COMPONENTS.registerComponentType(
            "environmental_data",
            builder -> builder
                    .persistent(EnvironmentalData.CODEC)
                    .networkSynchronized(EnvironmentalData.STREAM_CODEC));

    public static final Supplier<RecipeSerializer<SyringeBasedRecipe>> SYRINGE_SERIALIZER = RECIPE_SERIALIZERS.register("syringe", () -> SyringeBasedRecipe.SERIALIZER);

    public static final Identifier SYRINGE_RECIPE_TYPE_ID = Identifier.fromNamespaceAndPath(RFToolsUtility.MODID, "syringe");
    public static final Supplier<SyringeRecipeType> SYRINGE_RECIPE_TYPE = RECIPE_TYPES.register("syringe", SyringeRecipeType::new);

//    public static void initCrafting() {
//
//        Object inkSac = Item.REGISTRY.getObjectById(351);
//
//        String[] syringeMatcher = new String[]{"level", "mobId"};
//        String[] pickMatcher = new String[]{"ench"};
//
//        ItemStack ironGolemSyringe = SyringeItem.createMobSyringe(EntityIronGolem.class);
//        ItemStack endermanSyringe = SyringeItem.createMobSyringe(EntityEnderman.class);
//        ItemStack ghastSyringe = SyringeItem.createMobSyringe(EntityGhast.class);
//        ItemStack chickenSyringe = SyringeItem.createMobSyringe(EntityChicken.class);
//        ItemStack batSyringe = SyringeItem.createMobSyringe(EntityBat.class);
//        ItemStack horseSyringe = SyringeItem.createMobSyringe(EntityHorse.class);
//        ItemStack zombieSyringe = SyringeItem.createMobSyringe(EntityZombie.class);
//        ItemStack squidSyringe = SyringeItem.createMobSyringe(EntitySquid.class);
//        ItemStack guardianSyringe = SyringeItem.createMobSyringe(EntityGuardian.class);
//        ItemStack caveSpiderSyringe = SyringeItem.createMobSyringe(EntityCaveSpider.class);
//        ItemStack blazeSyringe = SyringeItem.createMobSyringe(EntityBlaze.class);
//        ItemStack shulkerEntity = SyringeItem.createMobSyringe(EntityShulker.class);
//        ItemStack diamondPick = createEnchantedItem(Items.DIAMOND_PICKAXE, Enchantment.REGISTRY.getObject(Identifier.fromNamespaceAndPath("efficiency")), 3);
//        ItemStack reds = new ItemStack(Items.REDSTONE);
//        ItemStack gold = new ItemStack(Items.GOLD_INGOT);
//        ItemStack ink = new ItemStack((Item) inkSac);
//        ItemStack obsidian = new ItemStack(Blocks.OBSIDIAN);
//        ItemStack lapis = new ItemStack(Items.DYE, 1, 4);
//
//        ForgeRegistries.RECIPES.register(
//                new NBTMatchingRecipe(3, 3,
//                        new ItemStack[]{ItemStack.EMPTY, chickenSyringe, ItemStack.EMPTY, reds, gold, reds, ItemStack.EMPTY, ink, ItemStack.EMPTY},
//                        new String[][]{null, syringeMatcher, null, null, null, null, null, null, null},
//                        new ItemStack(featherFallingEModuleItem))
//                        .setRegistryName(Identifier.fromNamespaceAndPath(RFTools.MODID, "featherfalling_module")));
//
//        ForgeRegistries.RECIPES.register(
//                new NBTMatchingRecipe(3, 3,
//                        new ItemStack[]{ItemStack.EMPTY, ironGolemSyringe, ItemStack.EMPTY, reds, gold, reds, ItemStack.EMPTY, ink, ItemStack.EMPTY},
//                        new String[][]{null, syringeMatcher, null, null, null, null, null, null, null},
//                        new ItemStack(regenerationEModuleItem))
//                        .setRegistryName(Identifier.fromNamespaceAndPath(RFTools.MODID, "regeneration_module")));
//
//        ForgeRegistries.RECIPES.register(
//                new NBTMatchingRecipe(3, 3,
//                        new ItemStack[]{ItemStack.EMPTY, horseSyringe, ItemStack.EMPTY, reds, gold, reds, ItemStack.EMPTY, ink, ItemStack.EMPTY},
//                        new String[][]{null, syringeMatcher, null, null, null, null, null, null, null},
//                        new ItemStack(speedEModuleItem))
//                        .setRegistryName(Identifier.fromNamespaceAndPath(RFTools.MODID, "speed_module")));
//
//        ForgeRegistries.RECIPES.register(
//                new NBTMatchingRecipe(3, 3, new ItemStack[]{ItemStack.EMPTY, diamondPick, ItemStack.EMPTY, reds, gold, reds, ItemStack.EMPTY, ink, ItemStack.EMPTY},
//                        new String[][]{null, pickMatcher, null, null, null, null, null, null, null},
//                        new ItemStack(hasteEModuleItem))
//                        .setRegistryName(Identifier.fromNamespaceAndPath(RFTools.MODID, "haste_module")));
//
//        ForgeRegistries.RECIPES.register(
//                new NBTMatchingRecipe(3, 3,
//                        new ItemStack[]{ItemStack.EMPTY, zombieSyringe, ItemStack.EMPTY, reds, gold, reds, ItemStack.EMPTY, ink, ItemStack.EMPTY},
//                        new String[][]{null, syringeMatcher, null, null, null, null, null, null, null},
//                        new ItemStack(saturationEModuleItem))
//                        .setRegistryName(Identifier.fromNamespaceAndPath(RFTools.MODID, "saturation_module")));
//
//        ForgeRegistries.RECIPES.register(
//                new NBTMatchingRecipe(3, 3,
//                        new ItemStack[]{ItemStack.EMPTY, ghastSyringe, ItemStack.EMPTY, reds, gold, reds, ItemStack.EMPTY, ink, ItemStack.EMPTY},
//                        new String[][]{null, syringeMatcher, null, null, null, null, null, null, null},
//                        new ItemStack(flightEModuleItem))
//                        .setRegistryName(Identifier.fromNamespaceAndPath(RFTools.MODID, "flight_module")));
//
//        ForgeRegistries.RECIPES.register(
//                new NBTMatchingRecipe(3, 3,
//                        new ItemStack[]{ItemStack.EMPTY, guardianSyringe, ItemStack.EMPTY, reds, gold, reds, ItemStack.EMPTY, ink, ItemStack.EMPTY},
//                        new String[][]{null, syringeMatcher, null, null, null, null, null, null, null},
//                        new ItemStack(waterBreathingEModuleItem))
//                        .setRegistryName(Identifier.fromNamespaceAndPath(RFTools.MODID, "waterbreathing_module")));
//
//        ForgeRegistries.RECIPES.register(
//                new NBTMatchingRecipe(3, 3,
//                        new ItemStack[]{ItemStack.EMPTY, caveSpiderSyringe, ItemStack.EMPTY, reds, gold, reds, ItemStack.EMPTY, ink, ItemStack.EMPTY},
//                        new String[][]{null, syringeMatcher, null, null, null, null, null, null, null},
//                        new ItemStack(nightVisionEModuleItem))
//                        .setRegistryName(Identifier.fromNamespaceAndPath(RFTools.MODID, "nightvision_module")));
//
//        ForgeRegistries.RECIPES.register(
//                new NBTMatchingRecipe(2, 2,
//                        new ItemStack[]{new ItemStack(regenerationEModuleItem), ironGolemSyringe, ironGolemSyringe, ItemStack.EMPTY},
//                        new String[][]{null, syringeMatcher, syringeMatcher, null},
//                        new ItemStack(regenerationPlusEModuleItem))
//                        .setRegistryName(Identifier.fromNamespaceAndPath(RFTools.MODID, "regenerationplus_module")));
//
//        ForgeRegistries.RECIPES.register(
//                new NBTMatchingRecipe(2, 2,
//                        new ItemStack[]{new ItemStack(speedEModuleItem), horseSyringe, horseSyringe, ItemStack.EMPTY},
//                        new String[][]{null, syringeMatcher, syringeMatcher, null},
//                        new ItemStack(speedPlusEModuleItem))
//                        .setRegistryName(Identifier.fromNamespaceAndPath(RFTools.MODID, "speedplus_module")));
//
//        ForgeRegistries.RECIPES.register(
//                new NBTMatchingRecipe(2, 2,
//                        new ItemStack[]{new ItemStack(hasteEModuleItem), diamondPick, ItemStack.EMPTY, ItemStack.EMPTY},
//                        new String[][]{null, pickMatcher, null, null},
//                        new ItemStack(hastePlusEModuleItem))
//                        .setRegistryName(Identifier.fromNamespaceAndPath(RFTools.MODID, "hasteplus_module")));
//
//        ForgeRegistries.RECIPES.register(
//                new NBTMatchingRecipe(2, 2,
//                        new ItemStack[]{new ItemStack(saturationEModuleItem), zombieSyringe, zombieSyringe, ItemStack.EMPTY},
//                        new String[][]{null, syringeMatcher, syringeMatcher, null},
//                        new ItemStack(saturationPlusEModuleItem))
//                        .setRegistryName(Identifier.fromNamespaceAndPath(RFTools.MODID, "saturationplus_module")));
//
//        ForgeRegistries.RECIPES.register(
//                new NBTMatchingRecipe(2, 2,
//                        new ItemStack[]{new ItemStack(featherFallingEModuleItem), chickenSyringe, batSyringe, ItemStack.EMPTY},
//                        new String[][]{null, syringeMatcher, syringeMatcher, null},
//                        new ItemStack(featherFallingPlusEModuleItem))
//                        .setRegistryName(Identifier.fromNamespaceAndPath(RFTools.MODID, "featherfallingplus_module")));
//
//        ForgeRegistries.RECIPES.register(
//                new NBTMatchingRecipe(3, 3,
//                        new ItemStack[]{ItemStack.EMPTY, blazeSyringe, ItemStack.EMPTY, reds, gold, reds, ItemStack.EMPTY, ink, ItemStack.EMPTY},
//                        new String[][]{null, syringeMatcher, null, null, null, null, null, null, null},
//                        new ItemStack(glowingEModuleItem))
//                        .setRegistryName(Identifier.fromNamespaceAndPath(RFTools.MODID, "glowing_module")));
//
//        ForgeRegistries.RECIPES.register(
//                new NBTMatchingRecipe(3, 3,
//                        new ItemStack[]{ItemStack.EMPTY, shulkerEntity, ItemStack.EMPTY, reds, gold, reds, ItemStack.EMPTY, ink, ItemStack.EMPTY},
//                        new String[][]{null, syringeMatcher, null, null, null, null, null, null, null},
//                        new ItemStack(luckEModuleItem))
//                        .setRegistryName(Identifier.fromNamespaceAndPath(RFTools.MODID, "luck_module")));
//
//
//        ForgeRegistries.RECIPES.register(
//                new NBTMatchingRecipe(3, 3,
//                        new ItemStack[]{ItemStack.EMPTY, squidSyringe, ItemStack.EMPTY, lapis, obsidian, lapis, ItemStack.EMPTY, ink, ItemStack.EMPTY},
//                        new String[][]{null, syringeMatcher, null, null, null, null, null, null, null},
//                        new ItemStack(blindnessEModuleItem))
//                        .setRegistryName(Identifier.fromNamespaceAndPath(RFTools.MODID, "blindness_module")));
//
//        ForgeRegistries.RECIPES.register(
//                new NBTMatchingRecipe(3, 3,
//                        new ItemStack[]{ItemStack.EMPTY, batSyringe, ItemStack.EMPTY, lapis, obsidian, lapis, ItemStack.EMPTY, ink, ItemStack.EMPTY},
//                        new String[][]{null, syringeMatcher, null, null, null, null, null, null, null},
//                        new ItemStack(weaknessEModuleItem))
//                        .setRegistryName(Identifier.fromNamespaceAndPath(RFTools.MODID, "weakness_module")));
//
//        ForgeRegistries.RECIPES.register(
//                new NBTMatchingRecipe(3, 3,
//                        new ItemStack[]{ItemStack.EMPTY, caveSpiderSyringe, ItemStack.EMPTY, lapis, obsidian, lapis, ItemStack.EMPTY, ink, ItemStack.EMPTY},
//                        new String[][]{null, syringeMatcher, null, null, null, null, null, null, null},
//                        new ItemStack(poisonEModuleItem))
//                        .setRegistryName(Identifier.fromNamespaceAndPath(RFTools.MODID, "poison_module")));
//
//        ForgeRegistries.RECIPES.register(
//                new NBTMatchingRecipe(3, 3,
//                        new ItemStack[]{ItemStack.EMPTY, new ItemStack(Items.CLOCK), ItemStack.EMPTY, lapis, obsidian, lapis, ItemStack.EMPTY, ink, ItemStack.EMPTY},
//                        new String[][]{null, null, null, null, null, null, null, null, null},
//                        new ItemStack(slownessEModuleItem))
//                        .setRegistryName(Identifier.fromNamespaceAndPath(RFTools.MODID, "slowness_module")));
//
//        ForgeRegistries.RECIPES.register(
//                new NBTMatchingRecipe(3, 3,
//                        new ItemStack[]{ItemStack.EMPTY, endermanSyringe, ItemStack.EMPTY, lapis, obsidian, lapis, ItemStack.EMPTY, ink, ItemStack.EMPTY},
//                        new String[][]{null, syringeMatcher, null, null, null, null, null, null, null},
//                        new ItemStack(noTeleportEModuleItem))
//                        .setRegistryName(Identifier.fromNamespaceAndPath(RFTools.MODID, "noteleport_module")));
//    }

//    public static ItemStack createEnchantedItem(Item item, Enchantment effectId, int amount) {
//        ItemStack stack = new ItemStack(item);
//        Map<Enchantment, Integer> enchant = new HashMap<>();
//        enchant.put(effectId, amount);
//        EnchantmentHelper.setEnchantments(enchant, stack);
//        return stack;
//    }


    public EnvironmentalModule(IEventBus bus, Dist dist) {
        bus.addListener(this::registerMenuScreens);
    }

    @Override
    public void init(FMLCommonSetupEvent event) {

    }

    public void registerMenuScreens(RegisterMenuScreensEvent event) {
        GuiEnvironmentalController.register(event);
    }

    @Override
    public void initClient(FMLClientSetupEvent event) {
        ClientSetup.initClient();
        EnvironmentalRenderer.register();
    }

    @Override
    public void initConfig(IEventBus bus) {
        EnvironmentalConfiguration.init(Config.SERVER_BUILDER, Config.CLIENT_BUILDER);
    }

}
