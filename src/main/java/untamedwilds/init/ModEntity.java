package untamedwilds.init;

import net.neoforged.fml.common.EventBusSubscriber;
import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import untamedwilds.UntamedWilds;
import untamedwilds.client.render.RendererAardvark;
import untamedwilds.client.render.RendererAnaconda;
import untamedwilds.client.render.RendererArowana;
import untamedwilds.client.render.RendererBaleenWhale;
import untamedwilds.client.render.RendererBear;
import untamedwilds.client.render.RendererBigCat;
import untamedwilds.client.render.RendererBison;
import untamedwilds.client.render.RendererBoar;
import untamedwilds.client.render.RendererCamel;
import untamedwilds.client.render.RendererCatfish;
import untamedwilds.client.render.RendererFootballFish;
import untamedwilds.client.render.RendererGiantClam;
import untamedwilds.client.render.RendererGiantSalamander;
import untamedwilds.client.render.RendererHippo;
import untamedwilds.client.render.RendererHyena;
import untamedwilds.client.render.RendererKingCrab;
import untamedwilds.client.render.RendererManatee;
import untamedwilds.client.render.RendererMonitor;
import untamedwilds.client.render.RendererNewt;
import untamedwilds.client.render.RendererOpossum;
import untamedwilds.client.render.RendererProjectileSpit;
import untamedwilds.client.render.RendererRhino;
import untamedwilds.client.render.RendererShark;
import untamedwilds.client.render.RendererSnake;
import untamedwilds.client.render.RendererSoftshellTurtle;
import untamedwilds.client.render.RendererSpadefish;
import untamedwilds.client.render.RendererSpitter;
import untamedwilds.client.render.RendererSunfish;
import untamedwilds.client.render.RendererTarantula;
import untamedwilds.client.render.RendererTortoise;
import untamedwilds.client.render.RendererTrevally;
import untamedwilds.client.render.RendererTriggerfish;
import untamedwilds.client.render.RendererWhaleShark;
import untamedwilds.entity.ProjectileSpit;
import untamedwilds.entity.amphibian.EntityGiantSalamander;
import untamedwilds.entity.amphibian.EntityNewt;
import untamedwilds.entity.arthropod.EntityKingCrab;
import untamedwilds.entity.arthropod.EntityTarantula;
import untamedwilds.entity.fish.EntityArowana;
import untamedwilds.entity.fish.EntityCatfish;
import untamedwilds.entity.fish.EntityFootballFish;
import untamedwilds.entity.fish.EntityShark;
import untamedwilds.entity.fish.EntitySpadefish;
import untamedwilds.entity.fish.EntitySunfish;
import untamedwilds.entity.fish.EntityTrevally;
import untamedwilds.entity.fish.EntityTriggerfish;
import untamedwilds.entity.fish.EntityWhaleShark;
import untamedwilds.entity.mammal.EntityAardvark;
import untamedwilds.entity.mammal.EntityBaleenWhale;
import untamedwilds.entity.mammal.EntityBear;
import untamedwilds.entity.mammal.EntityBigCat;
import untamedwilds.entity.mammal.EntityElephantid;
import untamedwilds.entity.mammal.EntityCervid;
import untamedwilds.entity.mammal.EntityEquid;
import untamedwilds.entity.mammal.EntityBovid;
import untamedwilds.entity.mammal.EntityCanid;
import untamedwilds.entity.mammal.EntityGroundSloth;
import untamedwilds.entity.mammal.EntityGlyptodont;
import untamedwilds.entity.mammal.EntityBison;
import untamedwilds.entity.mammal.EntityBoar;
import untamedwilds.entity.mammal.EntityCamel;
import untamedwilds.entity.mammal.EntityHippo;
import untamedwilds.entity.mammal.EntityHyena;
import untamedwilds.entity.mammal.EntityManatee;
import untamedwilds.entity.mammal.EntityOpossum;
import untamedwilds.entity.mammal.EntityRhino;
import untamedwilds.entity.mollusk.EntityGiantClam;
import untamedwilds.entity.relict.EntitySpitter;
import untamedwilds.entity.reptile.EntityAnaconda;
import untamedwilds.entity.reptile.EntityMonitor;
import untamedwilds.entity.reptile.EntitySnake;
import untamedwilds.entity.reptile.EntitySoftshellTurtle;
import untamedwilds.entity.reptile.EntityTortoise;
import untamedwilds.item.UntamedSpawnEggItem;
import untamedwilds.world.FaunaHandler;

@EventBusSubscriber(modid = UntamedWilds.MOD_ID)
public class ModEntity {

    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(
        BuiltInRegistries.ENTITY_TYPE,
        UntamedWilds.MOD_ID
    );
    //public static Map<RegistryObject<EntityType<? extends Mob>>, EntityRendererProvider<?>> map = Collections.emptyMap();

    // Arthropods
    public static DeferredHolder<EntityType<?>, EntityType<EntityTarantula>> TARANTULA = createEntity(
        EntityTarantula::new,
        "tarantula",
        0.4f,
        0.3f,
        0xB5B095,
        0x26292B
    );
    public static DeferredHolder<EntityType<?>, EntityType<EntityKingCrab>> KING_CRAB = createEntity(
        EntityKingCrab::new,
        "king_crab",
        0.6f,
        0.5f,
        0x715236,
        0xAD9050
    );

    // Reptiles
    public static DeferredHolder<EntityType<?>, EntityType<EntitySnake>> SNAKE = createEntity(
        EntitySnake::new,
        "snake",
        0.95f,
        0.3f,
        0xD8A552,
        0x5C3525
    );
    public static DeferredHolder<EntityType<?>, EntityType<EntitySoftshellTurtle>> SOFTSHELL_TURTLE = createEntity(
        EntitySoftshellTurtle::new,
        "softshell_turtle",
        0.6f,
        0.3f,
        0x828444,
        0x26292B
    );
    public static DeferredHolder<EntityType<?>, EntityType<EntityTortoise>> TORTOISE = createEntity(
        EntityTortoise::new,
        "tortoise",
        0.6f,
        0.6f,
        0xAF9F74,
        0x775232
    );
    public static DeferredHolder<EntityType<?>, EntityType<EntityAnaconda>> ANACONDA = createEntity(
        EntityAnaconda::new,
        "large_snake",
        1.5f,
        0.6f,
        0x65704C,
        0x42291A
    );
    public static DeferredHolder<EntityType<?>, EntityType<EntityMonitor>> MONITOR = createEntity(
        EntityMonitor::new,
        "monitor",
        1.3f,
        0.6f,
        0x423C2C,
        0x958C66
    );

    // Mollusks
    public static DeferredHolder<EntityType<?>, EntityType<EntityGiantClam>> GIANT_CLAM = createEntity(
        EntityGiantClam::new,
        "giant_clam",
        1.0F,
        1.0F,
        0x346B70,
        0xAD713C
    );
    //public static RegistryObject<EntityType<EntityGiantClam>> GIANT_CLAM = createEntity(EntityGiantClam::new, MobCategory.WATER_CREATURE, "giant_clam", 32, 10, true, 1.0F, 1.0F, 0x346B70, 0xAD713C, animalType.SESSILE, 1);

    // Mammals
    public static DeferredHolder<EntityType<?>, EntityType<EntityBear>> BEAR = createEntity(
        EntityBear::new,
        "bear",
        1.3F,
        1.3F,
        0x20130B,
        0x564C45
    );
    public static DeferredHolder<EntityType<?>, EntityType<EntityBigCat>> BIG_CAT = createEntity(
        EntityBigCat::new,
        "big_cat",
        1.2F,
        1.0F,
        0xC59F45,
        0x383121
    );
    public static DeferredHolder<EntityType<?>, EntityType<EntityHippo>> HIPPO = createEntity(
        EntityHippo::new,
        "hippo",
        1.8F,
        1.8F,
        0x463A31,
        0x956761
    );
    public static DeferredHolder<EntityType<?>, EntityType<EntityAardvark>> AARDVARK = createEntity(
        EntityAardvark::new,
        "aardvark",
        0.9F,
        0.9F,
        0x463A31,
        0x956761
    );
    public static DeferredHolder<EntityType<?>, EntityType<EntityRhino>> RHINO = createEntity(
        EntityRhino::new,
        "rhino",
        2.0F,
        1.8F,
        0x787676,
        0x665956
    );
    public static DeferredHolder<EntityType<?>, EntityType<EntityHyena>> HYENA = createEntity(
        EntityHyena::new,
        "hyena",
        0.9F,
        1.1F,
        0x6C6857,
        0x978966
    );
    public static DeferredHolder<EntityType<?>, EntityType<EntityBoar>> BOAR = createEntity(
        EntityBoar::new,
        "boar",
        1.2F,
        1.2F,
        0x503C2A,
        0x605449
    );
    public static DeferredHolder<EntityType<?>, EntityType<EntityBison>> BISON = createEntity(
        EntityBison::new,
        "bison",
        1.7F,
        1.6F,
        0x845B2B,
        0x49342A
    );
    public static DeferredHolder<EntityType<?>, EntityType<EntityCamel>> CAMEL = createEntity(
        EntityCamel::new,
        "camel",
        1.8F,
        2F,
        0xE0B989,
        0x976B3D
    );
    public static DeferredHolder<EntityType<?>, EntityType<EntityElephantid>> ELEPHANTID = createEntity(
        EntityElephantid::new,
        "elephantid",
        2.4F,
        3.0F,
        0x5A3B26,
        0xD8CDB0
    );
    public static DeferredHolder<EntityType<?>, EntityType<EntityCervid>> CERVID = createEntity(
        EntityCervid::new,
        "cervid",
        1.2F,
        1.9F,
        0x8A5C3A,
        0xD9C7A2
    );
    public static DeferredHolder<EntityType<?>, EntityType<EntityEquid>> EQUID = createEntity(
        EntityEquid::new,
        "equid",
        1.3F,
        1.6F,
        0xB08B5A,
        0x3C2E26
    );
    public static DeferredHolder<EntityType<?>, EntityType<EntityBovid>> BOVID = createEntity(
        EntityBovid::new,
        "bovid",
        1.4F,
        1.5F,
        0x4A3A2E,
        0xCFC3A0
    );
    public static DeferredHolder<EntityType<?>, EntityType<EntityCanid>> CANID = createEntity(
        EntityCanid::new,
        "canid",
        0.9F,
        1.0F,
        0x6E6A66,
        0xC9C3B8
    );
    public static DeferredHolder<EntityType<?>, EntityType<EntityGroundSloth>> GROUND_SLOTH = createEntity(
        EntityGroundSloth::new,
        "ground_sloth",
        1.6F,
        1.8F,
        0x7A5C40,
        0xD4C8B0
    );
    public static DeferredHolder<EntityType<?>, EntityType<EntityGlyptodont>> GLYPTODONT = createEntity(
        EntityGlyptodont::new,
        "glyptodont",
        1.6F,
        1.3F,
        0x8A7A5C,
        0x4C4236
    );
    public static DeferredHolder<EntityType<?>, EntityType<EntityManatee>> MANATEE = createEntity(
        EntityManatee::new,
        "manatee",
        1.8F,
        2F,
        0x4A4040,
        0x787676
    );
    public static DeferredHolder<EntityType<?>, EntityType<EntityBaleenWhale>> BALEEN_WHALE = createEntity(
        EntityBaleenWhale::new,
        "baleen_whale",
        2.6F,
        1.6F,
        0x12141E,
        0x5B6168
    );
    public static DeferredHolder<EntityType<?>, EntityType<EntityOpossum>> OPOSSUM = createEntity(
        EntityOpossum::new,
        "opossum",
        0.9F,
        0.9F,
        0xABA29B,
        0x383735
    );

    // Fish
    public static DeferredHolder<EntityType<?>, EntityType<EntitySunfish>> SUNFISH = createEntity(
        EntitySunfish::new,
        "sunfish",
        1.6F,
        1.6F,
        0x2C545B,
        0xB6D0D3
    );
    public static DeferredHolder<EntityType<?>, EntityType<EntityTrevally>> TREVALLY = createEntity(
        EntityTrevally::new,
        "trevally",
        0.8F,
        0.8F,
        0xA5B4AF,
        0xC89D17
    );
    public static DeferredHolder<EntityType<?>, EntityType<EntityArowana>> AROWANA = createEntity(
        EntityArowana::new,
        "arowana",
        0.6F,
        0.6F,
        0x645C45,
        0xB29F52
    );
    public static DeferredHolder<EntityType<?>, EntityType<EntityShark>> SHARK = createEntity(
        EntityShark::new,
        "shark",
        1.8F,
        1.3F,
        0x6B5142,
        0xB0B0A3
    );
    public static DeferredHolder<EntityType<?>, EntityType<EntityFootballFish>> FOOTBALL_FISH = createEntity(
        EntityFootballFish::new,
        "football_fish",
        0.8F,
        0.8F,
        0x53556C,
        0x2F3037
    );
    public static DeferredHolder<EntityType<?>, EntityType<EntityWhaleShark>> WHALE_SHARK = createEntity(
        EntityWhaleShark::new,
        "whale_shark",
        2.6F,
        1.6F,
        0x222426,
        0x7E7D84
    );
    public static DeferredHolder<EntityType<?>, EntityType<EntityTriggerfish>> TRIGGERFISH = createEntity(
        EntityTriggerfish::new,
        "triggerfish",
        0.8F,
        0.8F,
        0x1F0A19,
        0xFCBD00
    );
    public static DeferredHolder<EntityType<?>, EntityType<EntityCatfish>> CATFISH = createEntity(
        EntityCatfish::new,
        "catfish",
        0.8F,
        0.8F,
        0x545963,
        0x3A2C23
    );
    public static DeferredHolder<EntityType<?>, EntityType<EntitySpadefish>> SPADEFISH = createEntity(
        EntitySpadefish::new,
        "spadefish",
        0.8F,
        0.8F,
        0x545963,
        0x3A2C23
    );

    // Amphibians
    public static DeferredHolder<EntityType<?>, EntityType<EntityGiantSalamander>> GIANT_SALAMANDER = createEntity(
        EntityGiantSalamander::new,
        "giant_salamander",
        1F,
        0.6f,
        0x3A2C23,
        0x6B5142
    );
    public static DeferredHolder<EntityType<?>, EntityType<EntityNewt>> NEWT = createEntity(
        EntityNewt::new,
        "newt",
        0.6F,
        0.3f,
        0x232323,
        0xFF8D00
    );

    // Relicts
    public static DeferredHolder<EntityType<?>, EntityType<EntitySpitter>> SPITTER = createEntity(
        EntitySpitter::new,
        "spitter",
        1.3F,
        1.3f,
        0x3A345E,
        0xB364E0
    );

    // Projectiles
    public static DeferredHolder<EntityType<?>, EntityType<ProjectileSpit>> SPIT = createProjectile(
        ProjectileSpit::new,
        "spit",
        64,
        1,
        true,
        0.6F,
        0.3f
    );

    private static <T extends Projectile> DeferredHolder<EntityType<?>, EntityType<T>> createProjectile(
        EntityType.EntityFactory<T> factory,
        String name,
        int trackingRange,
        int updateFrequency,
        boolean sendsVelocityUpdates,
        float sizeX,
        float sizeY
    ) {
        DeferredHolder<EntityType<?>, EntityType<T>> type = ENTITIES.register(name, id ->
            EntityType.Builder.of(factory, MobCategory.MISC)
                .sized(sizeX, sizeY)
                .setTrackingRange(trackingRange)
                .setShouldReceiveVelocityUpdates(sendsVelocityUpdates)
                .build(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ENTITY_TYPE, id))
        );

        return type;
    }

    private static <T extends Mob> DeferredHolder<EntityType<?>, EntityType<T>> createEntity(
        EntityType.EntityFactory<T> factory,
        String name,
        float sizeX,
        float sizeY,
        int baseColor,
        int overlayColor
    ) {
        return createEntity(factory, MobCategory.CREATURE, name, 64, 1, true, sizeX, sizeY, baseColor, overlayColor);
    }

    private static <T extends Mob> DeferredHolder<EntityType<?>, EntityType<T>> createEntity(
        EntityType.EntityFactory<T> factory,
        MobCategory classification,
        String name,
        int trackingRange,
        int updateFrequency,
        boolean sendsVelocityUpdates,
        float sizeX,
        float sizeY,
        int maincolor,
        int backcolor
    ) {
        DeferredHolder<EntityType<?>, EntityType<T>> type = ENTITIES.register(name, id ->
            EntityType.Builder.of(factory, classification)
                .sized(sizeX, sizeY)
                .setTrackingRange(trackingRange)
                .setShouldReceiveVelocityUpdates(sendsVelocityUpdates)
                .build(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ENTITY_TYPE, id))
        );
        ModItems.ITEMS.registerItem(name + "_spawn_egg", props ->
            new UntamedSpawnEggItem(type::get, maincolor, backcolor, props)
        );
        return type;
    }

    @SubscribeEvent
    public static void bakeAttributes(EntityAttributeCreationEvent event) {
        event.put(TARANTULA.get(), EntityTarantula.registerAttributes().build());
        event.put(KING_CRAB.get(), EntityKingCrab.registerAttributes().build());

        event.put(SNAKE.get(), EntitySnake.registerAttributes().build());
        event.put(SOFTSHELL_TURTLE.get(), EntitySoftshellTurtle.registerAttributes().build());
        event.put(TORTOISE.get(), EntityTortoise.registerAttributes().build());
        event.put(ANACONDA.get(), EntityAnaconda.registerAttributes().build());
        event.put(MONITOR.get(), EntityMonitor.registerAttributes().build());

        event.put(GIANT_CLAM.get(), EntityGiantClam.registerAttributes().build());

        event.put(BEAR.get(), EntityBear.registerAttributes().build());
        event.put(BIG_CAT.get(), EntityBigCat.registerAttributes().build());
        event.put(HIPPO.get(), EntityHippo.registerAttributes().build());
        event.put(AARDVARK.get(), EntityAardvark.registerAttributes().build());
        event.put(RHINO.get(), EntityRhino.registerAttributes().build());
        event.put(HYENA.get(), EntityHyena.registerAttributes().build());
        event.put(BOAR.get(), EntityBoar.registerAttributes().build());
        event.put(BISON.get(), EntityBison.registerAttributes().build());
        event.put(CAMEL.get(), EntityCamel.registerAttributes().build());
        event.put(ELEPHANTID.get(), EntityElephantid.registerAttributes().build());
        event.put(CERVID.get(), EntityCervid.registerAttributes().build());
        event.put(EQUID.get(), EntityEquid.registerAttributes().build());
        event.put(BOVID.get(), EntityBovid.registerAttributes().build());
        event.put(CANID.get(), EntityCanid.registerAttributes().build());
        event.put(GROUND_SLOTH.get(), EntityGroundSloth.registerAttributes().build());
        event.put(GLYPTODONT.get(), EntityGlyptodont.registerAttributes().build());
        event.put(MANATEE.get(), EntityManatee.registerAttributes().build());
        event.put(BALEEN_WHALE.get(), EntityBaleenWhale.registerAttributes().build());
        event.put(OPOSSUM.get(), EntityOpossum.registerAttributes().build());

        event.put(SUNFISH.get(), EntitySunfish.registerAttributes().build());
        event.put(TREVALLY.get(), EntityTrevally.registerAttributes().build());
        event.put(AROWANA.get(), EntityArowana.registerAttributes().build());
        event.put(SHARK.get(), EntityShark.registerAttributes().build());
        event.put(FOOTBALL_FISH.get(), EntityFootballFish.registerAttributes().build());
        event.put(WHALE_SHARK.get(), EntityWhaleShark.registerAttributes().build());
        event.put(TRIGGERFISH.get(), EntityTriggerfish.registerAttributes().build());
        event.put(CATFISH.get(), EntityCatfish.registerAttributes().build());
        event.put(SPADEFISH.get(), EntitySpadefish.registerAttributes().build());

        event.put(GIANT_SALAMANDER.get(), EntityGiantSalamander.registerAttributes().build());
        event.put(NEWT.get(), EntityNewt.registerAttributes().build());

        event.put(SPITTER.get(), EntitySpitter.registerAttributes().build());
    }

    // Renderer registration moved to ClientProxy.clientInit()
    // Old method removed to prevent conflicts

    public static void addWorldSpawn(
        EntityType<?> entityClass,
        int weightedProb,
        FaunaHandler.animalType type,
        int groupCount
    ) {
        List<FaunaHandler.SpawnListEntry> spawns = FaunaHandler.getSpawnableList(type);
        boolean found = false;
        for (FaunaHandler.SpawnListEntry entry : spawns) {
            // Adjusting an existing spawn entry
            if (entry.entityType == entityClass) {
                entry.itemWeight = weightedProb;
                //entry.groupCount = groupCount;
                found = true;
                break;
            }
        }

        //if (!found)
        //spawns.add(new FaunaHandler.SpawnListEntry(entityClass, weightedProb, groupCount));
    }

    @SubscribeEvent
    public static void registerSpawnPlacements(RegisterSpawnPlacementsEvent event) {
        // Register spawn placements for all entities
        // Most terrestrial animals use ON_GROUND placement
        event.register(
            TARANTULA.get(),
            SpawnPlacementTypes.ON_GROUND,
            Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
            Animal::checkAnimalSpawnRules,
            RegisterSpawnPlacementsEvent.Operation.REPLACE
        );
        event.register(
            KING_CRAB.get(),
            SpawnPlacementTypes.ON_GROUND,
            Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
            Animal::checkAnimalSpawnRules,
            RegisterSpawnPlacementsEvent.Operation.REPLACE
        );

        event.register(
            SNAKE.get(),
            SpawnPlacementTypes.ON_GROUND,
            Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
            Animal::checkAnimalSpawnRules,
            RegisterSpawnPlacementsEvent.Operation.REPLACE
        );
        event.register(
            SOFTSHELL_TURTLE.get(),
            SpawnPlacementTypes.ON_GROUND,
            Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
            Animal::checkAnimalSpawnRules,
            RegisterSpawnPlacementsEvent.Operation.REPLACE
        );
        event.register(
            TORTOISE.get(),
            SpawnPlacementTypes.ON_GROUND,
            Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
            Animal::checkAnimalSpawnRules,
            RegisterSpawnPlacementsEvent.Operation.REPLACE
        );
        event.register(
            ANACONDA.get(),
            SpawnPlacementTypes.ON_GROUND,
            Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
            Animal::checkAnimalSpawnRules,
            RegisterSpawnPlacementsEvent.Operation.REPLACE
        );
        event.register(
            MONITOR.get(),
            SpawnPlacementTypes.ON_GROUND,
            Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
            Animal::checkAnimalSpawnRules,
            RegisterSpawnPlacementsEvent.Operation.REPLACE
        );

        event.register(
            GIANT_CLAM.get(),
            SpawnPlacementTypes.IN_WATER,
            Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
            (entityType, level, spawnType, pos, random) -> true,
            RegisterSpawnPlacementsEvent.Operation.REPLACE
        );

        event.register(
            BEAR.get(),
            SpawnPlacementTypes.ON_GROUND,
            Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
            Animal::checkAnimalSpawnRules,
            RegisterSpawnPlacementsEvent.Operation.REPLACE
        );
        event.register(
            BIG_CAT.get(),
            SpawnPlacementTypes.ON_GROUND,
            Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
            Animal::checkAnimalSpawnRules,
            RegisterSpawnPlacementsEvent.Operation.REPLACE
        );
        event.register(
            HIPPO.get(),
            SpawnPlacementTypes.ON_GROUND,
            Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
            Animal::checkAnimalSpawnRules,
            RegisterSpawnPlacementsEvent.Operation.REPLACE
        );
        event.register(
            AARDVARK.get(),
            SpawnPlacementTypes.ON_GROUND,
            Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
            Animal::checkAnimalSpawnRules,
            RegisterSpawnPlacementsEvent.Operation.REPLACE
        );
        event.register(
            RHINO.get(),
            SpawnPlacementTypes.ON_GROUND,
            Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
            Animal::checkAnimalSpawnRules,
            RegisterSpawnPlacementsEvent.Operation.REPLACE
        );
        event.register(
            HYENA.get(),
            SpawnPlacementTypes.ON_GROUND,
            Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
            Animal::checkAnimalSpawnRules,
            RegisterSpawnPlacementsEvent.Operation.REPLACE
        );
        event.register(
            BOAR.get(),
            SpawnPlacementTypes.ON_GROUND,
            Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
            Animal::checkAnimalSpawnRules,
            RegisterSpawnPlacementsEvent.Operation.REPLACE
        );
        event.register(
            BISON.get(),
            SpawnPlacementTypes.ON_GROUND,
            Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
            Animal::checkAnimalSpawnRules,
            RegisterSpawnPlacementsEvent.Operation.REPLACE
        );
        event.register(
            CAMEL.get(),
            SpawnPlacementTypes.ON_GROUND,
            Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
            Animal::checkAnimalSpawnRules,
            RegisterSpawnPlacementsEvent.Operation.REPLACE
        );
        event.register(
            ELEPHANTID.get(),
            SpawnPlacementTypes.ON_GROUND,
            Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
            Animal::checkAnimalSpawnRules,
            RegisterSpawnPlacementsEvent.Operation.REPLACE
        );
        event.register(
            CERVID.get(),
            SpawnPlacementTypes.ON_GROUND,
            Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
            Animal::checkAnimalSpawnRules,
            RegisterSpawnPlacementsEvent.Operation.REPLACE
        );
        event.register(
            EQUID.get(),
            SpawnPlacementTypes.ON_GROUND,
            Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
            Animal::checkAnimalSpawnRules,
            RegisterSpawnPlacementsEvent.Operation.REPLACE
        );
        event.register(
            BOVID.get(),
            SpawnPlacementTypes.ON_GROUND,
            Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
            Animal::checkAnimalSpawnRules,
            RegisterSpawnPlacementsEvent.Operation.REPLACE
        );
        event.register(
            CANID.get(),
            SpawnPlacementTypes.ON_GROUND,
            Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
            Animal::checkAnimalSpawnRules,
            RegisterSpawnPlacementsEvent.Operation.REPLACE
        );
        event.register(
            GROUND_SLOTH.get(),
            SpawnPlacementTypes.ON_GROUND,
            Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
            Animal::checkAnimalSpawnRules,
            RegisterSpawnPlacementsEvent.Operation.REPLACE
        );
        event.register(
            GLYPTODONT.get(),
            SpawnPlacementTypes.ON_GROUND,
            Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
            Animal::checkAnimalSpawnRules,
            RegisterSpawnPlacementsEvent.Operation.REPLACE
        );
        event.register(
            MANATEE.get(),
            SpawnPlacementTypes.IN_WATER,
            Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
            (entityType, level, spawnType, pos, random) -> true,
            RegisterSpawnPlacementsEvent.Operation.REPLACE
        );
        event.register(
            BALEEN_WHALE.get(),
            SpawnPlacementTypes.IN_WATER,
            Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
            (entityType, level, spawnType, pos, random) -> true,
            RegisterSpawnPlacementsEvent.Operation.REPLACE
        );
        event.register(
            OPOSSUM.get(),
            SpawnPlacementTypes.ON_GROUND,
            Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
            Animal::checkAnimalSpawnRules,
            RegisterSpawnPlacementsEvent.Operation.REPLACE
        );

        event.register(
            SUNFISH.get(),
            SpawnPlacementTypes.IN_WATER,
            Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
            (entityType, level, spawnType, pos, random) -> true,
            RegisterSpawnPlacementsEvent.Operation.REPLACE
        );
        event.register(
            TREVALLY.get(),
            SpawnPlacementTypes.IN_WATER,
            Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
            (entityType, level, spawnType, pos, random) -> true,
            RegisterSpawnPlacementsEvent.Operation.REPLACE
        );
        event.register(
            AROWANA.get(),
            SpawnPlacementTypes.IN_WATER,
            Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
            (entityType, level, spawnType, pos, random) -> true,
            RegisterSpawnPlacementsEvent.Operation.REPLACE
        );
        event.register(
            SHARK.get(),
            SpawnPlacementTypes.IN_WATER,
            Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
            (entityType, level, spawnType, pos, random) -> true,
            RegisterSpawnPlacementsEvent.Operation.REPLACE
        );
        event.register(
            FOOTBALL_FISH.get(),
            SpawnPlacementTypes.IN_WATER,
            Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
            (entityType, level, spawnType, pos, random) -> true,
            RegisterSpawnPlacementsEvent.Operation.REPLACE
        );
        event.register(
            WHALE_SHARK.get(),
            SpawnPlacementTypes.IN_WATER,
            Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
            (entityType, level, spawnType, pos, random) -> true,
            RegisterSpawnPlacementsEvent.Operation.REPLACE
        );
        event.register(
            TRIGGERFISH.get(),
            SpawnPlacementTypes.IN_WATER,
            Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
            (entityType, level, spawnType, pos, random) -> true,
            RegisterSpawnPlacementsEvent.Operation.REPLACE
        );
        event.register(
            CATFISH.get(),
            SpawnPlacementTypes.IN_WATER,
            Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
            (entityType, level, spawnType, pos, random) -> true,
            RegisterSpawnPlacementsEvent.Operation.REPLACE
        );
        event.register(
            SPADEFISH.get(),
            SpawnPlacementTypes.IN_WATER,
            Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
            (entityType, level, spawnType, pos, random) -> true,
            RegisterSpawnPlacementsEvent.Operation.REPLACE
        );

        event.register(
            GIANT_SALAMANDER.get(),
            SpawnPlacementTypes.ON_GROUND,
            Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
            Animal::checkAnimalSpawnRules,
            RegisterSpawnPlacementsEvent.Operation.REPLACE
        );
        event.register(
            NEWT.get(),
            SpawnPlacementTypes.ON_GROUND,
            Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
            Animal::checkAnimalSpawnRules,
            RegisterSpawnPlacementsEvent.Operation.REPLACE
        );

        event.register(
            SPITTER.get(),
            SpawnPlacementTypes.ON_GROUND,
            Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
            Animal::checkAnimalSpawnRules,
            RegisterSpawnPlacementsEvent.Operation.REPLACE
        );
    }
}
