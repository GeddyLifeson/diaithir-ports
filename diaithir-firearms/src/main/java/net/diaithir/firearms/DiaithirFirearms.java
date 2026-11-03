package net.diaithir.firearms;

import com.mojang.serialization.Codec;
import java.util.List;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Diaithir Firearms: data-driven guns, ammunition and attachments (clean-room replacement for Point Blank). */
@Mod(DiaithirFirearms.ID)
public final class DiaithirFirearms {
    public static final String ID = "diaithirfirearms";
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ID);
    private static final DeferredRegister.DataComponents COMPONENTS = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, ID);
    /** Rounds in the magazine. */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> MAG =
            COMPONENTS.registerComponentType("mag", b -> b.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.VAR_INT));
    /** Installed attachment item ids (without namespace). */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<List<String>>> ATT =
            COMPONENTS.registerComponentType("att", b -> b.persistent(Codec.STRING.listOf()).networkSynchronized(ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list())));

    private static final DeferredRegister<net.minecraft.sounds.SoundEvent> SOUNDS = DeferredRegister.create(Registries.SOUND_EVENT, ID);
    private static final java.util.Map<String, DeferredHolder<net.minecraft.sounds.SoundEvent, net.minecraft.sounds.SoundEvent>> SND = new java.util.HashMap<>();
    static {
        for (String n : List.of("shot_pistol", "shot_smg", "shot_rifle", "shot_shotgun", "shot_sniper", "shot_lmg", "shot_launcher", "shot_energy",
                "reload_start", "reload_end", "dry_fire", "attach"))
            SND.put(n, SOUNDS.register(n, () -> net.minecraft.sounds.SoundEvent.createVariableRangeEvent(net.minecraft.resources.Identifier.fromNamespaceAndPath(ID, n))));
    }
    public static net.minecraft.sounds.SoundEvent sound(String n) { return SND.get(n).get(); }

    private static final List<DeferredItem<? extends Item>> ALL = new java.util.ArrayList<>();

    public DiaithirFirearms(IEventBus bus, ModContainer container) {
        Defs.GUNS.values().forEach(g -> ALL.add(ITEMS.registerItem(g.id(), p -> new GunItem(p, g), () -> new Item.Properties().stacksTo(1))));
        for (String a : Defs.ATTS.keySet()) ALL.add(ITEMS.registerSimpleItem(a));
        for (String id : EXTRA) ALL.add(ITEMS.registerSimpleItem(id));
        for (String id : Defs.AMMO) ALL.add(ITEMS.registerSimpleItem(id));
        ITEMS.register(bus);
        COMPONENTS.register(bus);
        SOUNDS.register(bus);
        bus.addListener(Net::register);
        bus.addListener(BuildCreativeModeTabContentsEvent.class, e -> {
            if (e.getTabKey().equals(CreativeModeTabs.COMBAT)) ALL.forEach(i -> e.accept(i.get()));
        });
        NeoForge.EVENT_BUS.register(this);
        if (FMLEnvironment.getDist().isClient()) FirearmsClient.init(bus);
    }

    /** Ammunition and parts (plain items). */
    static final List<String> EXTRA = List.of("ammo_9mm", "ammo_45", "ammo_magnum", "ammo_556", "ammo_762", "ammo_12g", "ammo_50bmg",
            "grenade_40mm", "rocket", "energy_cell", "gunmetal_nugget", "gunmetal_ingot", "gunmetal_mesh", "gun_internals");

    @SubscribeEvent public void commands(RegisterCommandsEvent e) { SelfTest.register(e.getDispatcher()); }
    @SubscribeEvent public void serverTick(ServerTickEvent.Post e) { SelfTest.tick(); }
    @SubscribeEvent public void tick(PlayerTickEvent.Post e) { if (e.getEntity() instanceof net.minecraft.server.level.ServerPlayer sp) Engine.tick(sp); }
    @SubscribeEvent public void logout(PlayerEvent.PlayerLoggedOutEvent e) { Engine.forget(e.getEntity()); }
}
