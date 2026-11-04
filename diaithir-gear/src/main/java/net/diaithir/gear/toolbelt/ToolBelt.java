package net.diaithir.gear.toolbelt;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.diaithir.gear.toolbelt.belt.BeltIngredient;
import net.diaithir.gear.toolbelt.belt.ToolBeltItem;
import net.diaithir.gear.toolbelt.client.HasCustomColor;
import net.diaithir.gear.toolbelt.common.BeltContainer;
import net.diaithir.gear.toolbelt.common.BeltScreen;
import net.diaithir.gear.toolbelt.network.*;
import net.diaithir.gear.toolbelt.slot.BeltAttachment;
import net.diaithir.gear.toolbelt.slot.BeltSlotMenu;
import net.diaithir.gear.toolbelt.slot.BeltSlotScreen;
import net.minecraft.client.color.item.Dye;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.model.ItemModelUtils;
import net.minecraft.client.data.models.model.ModelLocationUtils;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.packs.VanillaRecipeProvider;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.data.tags.IntrinsicHolderTagsProvider;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Util;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.common.conditions.ModLoadedCondition;
import net.neoforged.neoforge.common.crafting.IngredientType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.event.AnvilUpdateEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ToolBelt
{
    static final ToolBelt INSTANCE = new ToolBelt();
    public static final String MODID = "toolbelt";

    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    private static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(BuiltInRegistries.MENU, MODID);
    private static final DeferredRegister<IngredientType<?>> INGREDIENT_TYPES = DeferredRegister.create(NeoForgeRegistries.INGREDIENT_TYPES, MODID);
    private static final DeferredRegister<DataComponentType<?>> DATA_COMPONENT_TYPES = DeferredRegister.create(BuiltInRegistries.DATA_COMPONENT_TYPE, MODID);

    private static final DeferredRegister<MapCodec<? extends ICondition>>
            CONDITION_SERIALIZERS = DeferredRegister.create(NeoForgeRegistries.CONDITION_SERIALIZERS, ToolBelt.MODID);

    public static final DeferredHolder<MapCodec<? extends ICondition>, MapCodec<Conditions.EnableNormalCrafting>>
            ENABLE_NORMAL = CONDITION_SERIALIZERS.register("enable_normal_crafting", () -> Conditions.EnableNormalCrafting.CODEC);

    public static final DeferredHolder<MapCodec<? extends ICondition>, MapCodec<Conditions.EnableSewingCrafting>>
            ENABLE_SEWING = CONDITION_SERIALIZERS.register("enable_sewing_crafting", () -> Conditions.EnableSewingCrafting.CODEC);

    public static DeferredItem<ToolBeltItem>
            BELT = ITEMS.registerItem("belt", props -> new ToolBeltItem(props.stacksTo(1)));
    public static DeferredItem<Item>
            POUCH = ITEMS.registerSimpleItem("pouch");

    public static DeferredHolder<MenuType<?>, MenuType<BeltSlotMenu>>
            BELT_SLOT_MENU = MENU_TYPES.register("belt_slot_container", () -> new MenuType<>(BeltSlotMenu::new, FeatureFlags.DEFAULT_FLAGS));
    public static DeferredHolder<MenuType<?>, MenuType<BeltContainer>>
            BELT_MENU = MENU_TYPES.register("belt_container", () -> IMenuTypeExtension.create(BeltContainer::new));

    public static DeferredHolder<IngredientType<?>, IngredientType<BeltIngredient>>
            BELT_INGREDIENT = INGREDIENT_TYPES.register("belt_upgrade_level", () -> new IngredientType<BeltIngredient>(BeltIngredient.CODEC));

    public static DeferredHolder<DataComponentType<?>, DataComponentType<Integer>>
            BELT_SIZE = DATA_COMPONENT_TYPES.register("belt_size", () -> DataComponentType.<Integer>builder().persistent(Codec.INT).networkSynchronized(ByteBufCodecs.VAR_INT).build());

    public static void init(ModContainer container, IEventBus modEventBus)
    {
        ITEMS.register(modEventBus);
        MENU_TYPES.register(modEventBus);
        INGREDIENT_TYPES.register(modEventBus);
        CONDITION_SERIALIZERS.register(modEventBus);
        DATA_COMPONENT_TYPES.register(modEventBus);

        modEventBus.addListener(INSTANCE::commonSetup);
        modEventBus.addListener(INSTANCE::modConfigLoad);
        modEventBus.addListener(INSTANCE::modConfigReload);
        
        modEventBus.addListener(INSTANCE::addItemsToTabs);
        modEventBus.addListener(INSTANCE::registerPackets);
        modEventBus.addListener(ToolBeltItem::register);

        NeoForge.EVENT_BUS.addListener(INSTANCE::anvilChange);

        container.registerConfig(ModConfig.Type.SERVER, ConfigData.SERVER_SPEC, "diaithirgear-toolbelt-server.toml");
        container.registerConfig(ModConfig.Type.CLIENT, ConfigData.CLIENT_SPEC, "diaithirgear-toolbelt-client.toml");
        container.registerConfig(ModConfig.Type.COMMON, ConfigData.COMMON_SPEC, "diaithirgear-toolbelt-common.toml");

        BeltAttachment.register(modEventBus);
    }

    private void addItemsToTabs(BuildCreativeModeTabContentsEvent event)
    {
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES)
        {
            event.accept(BELT);
            event.accept(POUCH);
        }
    }


    public void modConfigLoad(ModConfigEvent.Loading event)
    {
        reloadConfigs(event.getConfig());
    }

    public void modConfigReload(ModConfigEvent.Reloading event)
    {
        reloadConfigs(event.getConfig());
    }

    private void reloadConfigs(ModConfig config)
    {
        if (config.getSpec() == ConfigData.CLIENT_SPEC)
            ConfigData.refreshClient();
        else if (config.getSpec() == ConfigData.COMMON_SPEC)
            ConfigData.refreshCommon();
        else if (config.getSpec() == ConfigData.SERVER_SPEC)
            ConfigData.refreshServer();
    }

    private void registerPackets(RegisterPayloadHandlersEvent event)
    {
        final PayloadRegistrar registrar = event.registrar(MODID).versioned("1.0");
        registrar.playToServer(SwapItems.TYPE, SwapItems.STREAM_CODEC, SwapItems::handle);
        registrar.playToServer(OpenBeltSlotInventory.TYPE, OpenBeltSlotInventory.STREAM_CODEC, OpenBeltSlotInventory::handle);
        registrar.playToServer(ContainerSlotsHack.TYPE, ContainerSlotsHack.STREAM_CODEC, ContainerSlotsHack::handle);
        registrar.playToClient(BeltContentsChange.TYPE, BeltContentsChange.STREAM_CODEC, BeltContentsChange::handle);
        registrar.playToClient(SyncBeltSlotContents.TYPE, SyncBeltSlotContents.STREAM_CODEC, SyncBeltSlotContents::handle);
    }

    public void commonSetup(FMLCommonSetupEvent event)
    {
        BeltFinderBeltSlot.initBeltSlot();
    }

    @EventBusSubscriber(value= Dist.CLIENT, modid = net.diaithir.gear.DiaithirGear.ID)
    public static class ClientModBusEvents
    {
        @SubscribeEvent
        public static void menuScreens(RegisterMenuScreensEvent event)
        {
            event.register(ToolBelt.BELT_MENU.get(), BeltScreen::new);
            event.register(ToolBelt.BELT_SLOT_MENU.get(), BeltSlotScreen::new);
        }
    }


    public static final int[] XP_COSTS = {3, 5, 8, 12, 15, 20, 30};

    public static int getUpgradeXP(ItemStack stack)
    {
        int slots = ToolBeltItem.getBeltSize(stack);

        if (slots >= 9)
            return -1;

        if (slots < 2)
            return 1;

        return XP_COSTS[slots - 2];
    }

    public void anvilChange(AnvilUpdateEvent ev)
    {
        if (!ConfigData.enableAnvilUpgrading)
            return;

        ItemStack left = ev.getLeft();
        ItemStack right = ev.getRight();
        if (left.getCount() <= 0 || left.getItem() != BELT.get())
            return;
        if (right.getCount() <= 0 || right.getItem() != POUCH.get())
            return;
        int cost = getUpgradeXP(left);
        if (cost < 0)
        {
            ev.setCanceled(true);
            return;
        }
        ev.setXpCost(cost);
        ev.setMaterialCost(1);

        ev.setOutput(ToolBeltItem.makeUpgradedStack(left));
    }

    public static Identifier location(String path)
    {
        return Identifier.fromNamespaceAndPath(MODID, path);
    }

}
