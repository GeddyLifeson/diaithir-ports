package net.diaithir.bosses;

import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jspecify.annotations.Nullable;

/**
 * Diaithir Bosses: a data-driven boss framework (bar, phases, telegraphed attacks, arena lock-in, Age scaling, Age-gated loot,
 * no-despawn, altar respawn rules), ten bosses of our own, battle kits for the avatars of the dead courts, and champion elites.
 * Clean-room: the behaviour of boss mods was studied, none of their code, names or art is used.
 */
@Mod(DiaithirBosses.ID)
public final class DiaithirBosses {
    public static final String ID = "diaithirbosses";

    private static final DeferredRegister.Entities ENTITIES = DeferredRegister.createEntities(ID);
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ID);
    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(ID);
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, ID);
    private static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ID);

    /** Our own entity types, by profile id. */
    public static final Map<String, DeferredHolder<EntityType<?>, ? extends EntityType<? extends Mob>>> MOBS = new LinkedHashMap<>();
    public static final Map<String, DeferredItem<Item>> EGGS = new LinkedHashMap<>();
    public static final Map<String, DeferredItem<Item>> TROPHIES = new LinkedHashMap<>();
    private static final Map<String, java.util.function.Supplier<AttributeSupplier.Builder>> ATTRS = new LinkedHashMap<>();

    public static final DeferredBlock<Block> ALTAR = BLOCKS.registerBlock("boss_altar", AltarBlock::new,
        () -> BlockBehaviour.Properties.of().strength(-1.0F, 3600000.0F).requiresCorrectToolForDrops().lightLevel(s -> 7));
    public static final DeferredItem<net.minecraft.world.item.BlockItem> ALTAR_ITEM = ITEMS.registerSimpleBlockItem(ALTAR);
    public static final DeferredItem<Item> TOKEN = ITEMS.registerSimpleItem("champion_token");
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AltarBlockEntity>> ALTAR_BE =
        BLOCK_ENTITIES.register("boss_altar", () -> new BlockEntityType<>(AltarBlockEntity::new, ALTAR.get()));
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> TAB = TABS.register("bosses", () -> CreativeModeTab.builder()
        .title(Component.translatable("itemGroup." + ID))
        .icon(() -> new ItemStack(ALTAR_ITEM.get()))
        .displayItems((params, out) -> {
            out.accept(ALTAR_ITEM.get());
            out.accept(TOKEN.get());
            EGGS.values().forEach(i -> out.accept(i.get()));
            TROPHIES.values().forEach(i -> out.accept(i.get()));
        }).build());

    @SuppressWarnings({"unchecked", "rawtypes"})
    public DiaithirBosses(IEventBus bus) {
        Data.loadClasspath();
        for (Data.Profile p : Data.profiles().values()) {
            TROPHIES.put(p.id, ITEMS.registerSimpleItem(p.id + "_trophy"));
            if (!p.ownsEntity()) continue;
            BossMobs.Kind k = BossMobs.KINDS.get(p.own.kind);
            if (k == null) throw new IllegalStateException("unknown body kind " + p.own.kind + " for " + p.id);
            DeferredHolder<EntityType<?>, EntityType<Mob>> t = ENTITIES.registerEntityType(p.id, (EntityType.EntityFactory) k.factory(), MobCategory.MONSTER,
                b -> b.sized(k.w(), k.h()).clientTrackingRange(10).fireImmune());
            MOBS.put(p.id, t);
            ATTRS.put(p.id, () -> {
                AttributeSupplier.Builder b = k.attrs().get();
                b.add(Attributes.MAX_HEALTH, p.own.hp).add(Attributes.ATTACK_DAMAGE, p.own.damage).add(Attributes.ARMOR, p.own.armor)
                    .add(Attributes.KNOCKBACK_RESISTANCE, p.own.knockback_resistance).add(Attributes.FOLLOW_RANGE, p.own.follow_range)
                    .add(Attributes.SCALE, p.own.scale).add(Attributes.STEP_HEIGHT, Math.max(0.6, 0.45 * p.own.scale));
                return b;
            });
            EGGS.put(p.id, ITEMS.registerItem(p.id + "_spawn_egg", props -> new SpawnEggItem(props.spawnEgg(t.get()))));
        }
        BLOCKS.register(bus);
        ITEMS.register(bus);
        ENTITIES.register(bus);
        BLOCK_ENTITIES.register(bus);
        TABS.register(bus);
        bus.addListener(EntityAttributeCreationEvent.class, e ->
            MOBS.forEach((id, t) -> e.put((EntityType) t.get(), ATTRS.get(id).get().build())));
        NeoForge.EVENT_BUS.register(BossEvents.class);
        if (FMLEnvironment.getDist() == Dist.CLIENT) net.diaithir.bosses.client.BossClient.init(bus);
    }

    /** Spawns the boss of a profile (its own entity, or the other mod's creature the profile attaches to). */
    public static @Nullable Mob spawn(ServerLevel level, Data.Profile p, Vec3 at, @Nullable BlockPos altar) {
        var type = BuiltInRegistries.ENTITY_TYPE.getOptional(Identifier.parse(p.entity));
        if (type.isEmpty()) return null;
        var e = type.get().create(level, EntitySpawnReason.MOB_SUMMONED);
        if (!(e instanceof Mob m)) return null;
        m.snapTo(at.x, at.y, at.z, level.getRandom().nextFloat() * 360F, 0);
        if (altar != null) {
            m.addTag(AltarBlockEntity.posTag(altar));
            m.addTag(BossController.T_ARENA + altar.getX() + "," + altar.getY() + "," + altar.getZ());
        }
        try {
            m.finalizeSpawn(level, level.getCurrentDifficultyAt(m.blockPosition()), EntitySpawnReason.MOB_SUMMONED, null);
        } catch (RuntimeException ignored) {
            // some creatures need nothing here
        }
        level.addFreshEntity(m);
        return m;
    }
}
