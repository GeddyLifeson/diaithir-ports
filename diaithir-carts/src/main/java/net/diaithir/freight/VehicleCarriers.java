package net.diaithir.freight;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.math.Transformation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.joml.Matrix3f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.jspecify.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Makes Immersive Vehicles cars, trucks, trailers and tanks {@link CargoCarrier}s. The bay of each vehicle type is listed in {@code diaithir_cargo_bays.json}
 * (every jar may ship one); the loads are saved on the vehicle entity as a data attachment, and are drawn as block displays that follow the vehicle, so
 * every player sees them. IV is reached by reflection only, like the fuel code, so nothing here links to it.
 */
public final class VehicleCarriers {
    /** One vehicle type's bay: size in cells, payload rating, and the vehicle-local metre position of cell (0,0,0). */
    record Spec(int w, int h, int l, double payloadKg, double ox, double oy, double oz) {}

    private static final String BUILDER = "mcinterface261.BuilderEntityExisting";
    private static final Map<String, Spec> SPECS = new HashMap<>();
    private static final Map<Entity, VehicleCarrier> CARRIERS = Collections.synchronizedMap(new WeakHashMap<>());
    private static final DeferredRegister<AttachmentType<?>> ATTACHMENTS = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, Freight.ID);
    static final DeferredHolder<AttachmentType<?>, AttachmentType<BayData>> BAY_DATA = ATTACHMENTS.register("vehicle_bay",
            () -> AttachmentType.builder(() -> BayData.EMPTY).serialize(BayData.CODEC.fieldOf("loads")).build());
    /** Tag on every display this class makes, so strays left in a save can be cleared. */
    static final String TAG = "diaithir_cargo";

    private static Field entityField;
    private static Method setBlockState, setTransformation, setPosRot;
    private static int tick;

    private VehicleCarriers() {}

    public static void init(IEventBus bus) {
        ATTACHMENTS.register(bus);
        bus.addListener(RegisterCapabilitiesEvent.class, e -> {
            loadSpecs();
            int types = 0;
            if (!SPECS.isEmpty()) {
                // IV registers more than one entity type for its builders; offer the capability on each of its types and let of() pick out the vehicles
                for (var en : net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.entrySet()) {
                    if (!en.getKey().identifier().getNamespace().equals("mts")) continue;
                    e.registerEntity(Freight.CARRIER, en.getValue(), (entity, ctx) -> of(entity));
                    types++;
                }
            }
            org.slf4j.LoggerFactory.getLogger("diaithir-freight").info("Vehicle cargo bays: {} vehicle types, offered on {} Immersive Vehicles entity types", SPECS.size(), types);
        });
        NeoForge.EVENT_BUS.addListener(ServerTickEvent.Post.class, e -> serverTick(e));
        NeoForge.EVENT_BUS.addListener(net.neoforged.neoforge.event.RegisterCommandsEvent.class, e -> e.getDispatcher().register(
                net.minecraft.commands.Commands.literal("diaithircargo")
                        .requires(net.minecraft.commands.Commands.hasPermission(net.minecraft.commands.Commands.LEVEL_GAMEMASTERS))
                        .then(net.minecraft.commands.Commands.literal("status").executes(c -> status(c.getSource())))
                        .then(net.minecraft.commands.Commands.literal("clear").executes(c -> clear(c.getSource())))
                        .then(net.minecraft.commands.Commands.literal("load").then(net.minecraft.commands.Commands.argument("type", com.mojang.brigadier.arguments.StringArgumentType.word())
                                .executes(c -> load(c.getSource(), com.mojang.brigadier.arguments.StringArgumentType.getString(c, "type")))))));
    }

    /** The nearest cargo-capable vehicle within 8 blocks of the command source. */
    private static @Nullable VehicleCarrier nearest(net.minecraft.commands.CommandSourceStack src) {
        Vec3 at = src.getPosition();
        VehicleCarrier best = null;
        double bestD = Double.MAX_VALUE;
        int seen = 0, vehicles = 0;
        lastProbe = "";
        for (Entity en : src.getLevel().getEntities((Entity) null, new net.minecraft.world.phys.AABB(at, at).inflate(8), VehicleCarriers::isIv)) {
            seen++;
            Object v = vehicleOf(en);
            if (v != null) { vehicles++; lastProbe = " (last vehicle key " + keyOf(v) + ")"; }
            if (!(of(en) instanceof VehicleCarrier c)) continue;
            double d = en.distanceToSqr(at);
            if (d < bestD) { best = c; bestD = d; }
        }
        if (best == null) lastProbe = " [IV entities " + seen + ", vehicles " + vehicles + ", bay specs loaded " + SPECS.size() + "]" + lastProbe;
        return best;
    }

    private static String lastProbe = "";

    private static String keyOf(Object vehicle) {
        try {
            Object def = vehicle.getClass().getField("definition").get(vehicle);
            return def.getClass().getField("packID").get(def) + ":" + def.getClass().getField("systemName").get(def);
        } catch (ReflectiveOperationException | RuntimeException ex) {
            return "unreadable " + ex.getClass().getSimpleName();
        }
    }

    private static int status(net.minecraft.commands.CommandSourceStack src) {
        VehicleCarrier c = nearest(src);
        if (c == null) { src.sendFailure(net.minecraft.network.chat.Component.literal("No vehicle with a cargo bay within 8 blocks" + lastProbe)); return 0; }
        StringBuilder sb = new StringBuilder(String.format("Bay %dx%dx%d cells, %d free, payload %.0f of %.0f kg, displays %d; loads:", c.spec.w, c.spec.h, c.spec.l, c.bay.freeCells(), c.bay.payloadKg(), c.bay.maxPayloadKg, c.displays.size()));
        for (CargoBay.Placed p : c.bay.loads()) sb.append(' ').append(p.type.id()).append('@').append(p.x).append(',').append(p.y).append(',').append(p.z);
        Vec3 vp = c.position();
        if (vp != null) sb.append(String.format(" | vehicle at %.2f,%.2f,%.2f", vp.x, vp.y, vp.z));
        for (Display.BlockDisplay d : c.displays) sb.append(String.format(" | display at %.2f,%.2f,%.2f", d.getX(), d.getY(), d.getZ()));
        String line = sb.toString();
        src.sendSuccess(() -> net.minecraft.network.chat.Component.literal(line), false);
        return 1;
    }

    private static int clear(net.minecraft.commands.CommandSourceStack src) {
        VehicleCarrier c = nearest(src);
        if (c == null) { src.sendFailure(net.minecraft.network.chat.Component.literal("No vehicle with a cargo bay within 8 blocks" + lastProbe)); return 0; }
        List<CargoBay.Placed> all = new ArrayList<>(c.bay.loads());
        all.sort((a, b) -> Integer.compare(b.y, a.y));  // top loads first: a load with another on it cannot be taken
        for (CargoBay.Placed p : all) c.bay.take(p);
        c.changed();
        src.sendSuccess(() -> net.minecraft.network.chat.Component.literal("Bay emptied"), false);
        return 1;
    }

    /** Test aid: puts one load of the given type on the first column that accepts it. */
    private static int load(net.minecraft.commands.CommandSourceStack src, String type) {
        VehicleCarrier c = nearest(src);
        if (c == null) { src.sendFailure(net.minecraft.network.chat.Component.literal("No vehicle with a cargo bay within 8 blocks" + lastProbe)); return 0; }
        var item = Freight.LOAD_ITEMS.get(type) != null ? Freight.LOAD_ITEMS.get(type) : Freight.CONTAINER_ITEMS.get(type);
        if (item == null) { src.sendFailure(net.minecraft.network.chat.Component.literal("Unknown load type: " + type)); return 0; }
        CargoBay.Result last = CargoBay.Result.OUT_OF_BAY;
        for (int z = 0; z < c.spec.l; z++) for (int i = 0; i < c.spec.w; i++) {
            int x = (c.spec.w - 1) / 2 + ((i % 2 == 0) ? i / 2 : -(i + 1) / 2);  // middle columns first, so a loose load sits on the deck rather than at the edge of a wide bay
            if (x < 0 || x >= c.spec.w) continue;
            last = Carriers.put(c, new net.minecraft.world.item.ItemStack(item.get()), x, z);
            if (last == CargoBay.Result.OK) {
                int cx = x, cz = z;
                src.sendSuccess(() -> net.minecraft.network.chat.Component.literal("Loaded " + type + " at column " + cx + "," + cz), false);
                return 1;
            }
        }
        src.sendFailure(net.minecraft.network.chat.Component.literal("Cannot load " + type + ": " + last.name().toLowerCase(java.util.Locale.ROOT).replace('_', ' ')));
        return 0;
    }

    // ---- data ----

    private static void loadSpecs() {
        SPECS.clear();
        for (var info : ModList.get().getModFiles()) {
            var contents = info.getFile().getContents();
            if (!contents.containsFile("diaithir_cargo_bays.json")) continue;
            try {
                JsonObject o = JsonParser.parseString(new String(contents.readFile("diaithir_cargo_bays.json"), java.nio.charset.StandardCharsets.UTF_8)).getAsJsonObject();
                for (var en : o.entrySet()) {
                    JsonObject s = en.getValue().getAsJsonObject();
                    SPECS.put(en.getKey(), new Spec(s.get("w").getAsInt(), s.get("h").getAsInt(), s.get("l").getAsInt(), s.get("payload_kg").getAsDouble(),
                            s.get("ox").getAsDouble(), s.get("oy").getAsDouble(), s.get("oz").getAsDouble()));
                }
            } catch (Exception ex) {
                // a bad file only means its vehicles carry nothing
            }
        }
    }

    static boolean isIv(Entity e) {
        return e.getClass().getName().equals(BUILDER);
    }

    // ---- the carrier ----

    private static @Nullable Object vehicleOf(Entity e) {
        try {
            if (entityField == null) {
                Class<?> c = e.getClass();
                while (c != null && entityField == null) {
                    try { entityField = c.getDeclaredField("entity"); } catch (NoSuchFieldException ex) { c = c.getSuperclass(); }
                }
                if (entityField == null) return null;
                entityField.setAccessible(true);
            }
            Object v = entityField.get(e);
            return v != null && v.getClass().getSimpleName().equals("EntityVehicleF_Physics") ? v : null;
        } catch (ReflectiveOperationException | RuntimeException ex) {
            return null;
        }
    }

    private static @Nullable Spec specOf(Object vehicle) {
        try {
            Object def = vehicle.getClass().getField("definition").get(vehicle);
            return SPECS.get(def.getClass().getField("packID").get(def) + ":" + def.getClass().getField("systemName").get(def));
        } catch (ReflectiveOperationException | RuntimeException ex) {
            return null;
        }
    }

    static @Nullable CargoCarrier of(Entity e) {
        VehicleCarrier c = CARRIERS.get(e);
        if (c != null) return c;
        Object v = vehicleOf(e);
        Spec s = v == null ? null : specOf(v);
        if (s == null) return null;
        c = new VehicleCarrier(e, v, s);
        CARRIERS.put(e, c);
        return c;
    }

    static final class VehicleCarrier implements CargoCarrier {
        final Entity entity;
        final Object vehicle;
        final Spec spec;
        final CargoBay bay;
        final List<Display.BlockDisplay> displays = new ArrayList<>();
        boolean dirty = true;

        VehicleCarrier(Entity entity, Object vehicle, Spec spec) {
            this.entity = entity;
            this.vehicle = vehicle;
            this.spec = spec;
            this.bay = entity.getData(BAY_DATA).toBay(spec.w, spec.h, spec.l, spec.payloadKg);
        }

        @Override
        public CargoBay bay() { return bay; }

        @Override
        public @Nullable int[] columnAt(Vec3 world) {
            double[] m = matrix();
            Vec3 pos = position();
            if (m == null || pos == null) return null;
            double dx = world.x - pos.x, dy = world.y - pos.y, dz = world.z - pos.z;
            double lx = m[0] * dx + m[3] * dy + m[6] * dz - spec.ox, lz = m[2] * dx + m[5] * dy + m[8] * dz - spec.oz;
            int x = (int) Math.floor(lx), z = (int) Math.floor(lz);
            return x < 0 || z < 0 || x >= spec.w || z >= spec.l ? null : new int[]{x, z};
        }

        @Override
        public void changed() {
            entity.setData(BAY_DATA, BayData.of(bay));
            dirty = true;
        }

        /** Row-major IV rotation matrix m00..m22, or null when it cannot be read. */
        double[] matrix() {
            try {
                Object r = vehicle.getClass().getField("orientation").get(vehicle);
                double[] m = new double[9];
                String[] names = {"m00", "m01", "m02", "m10", "m11", "m12", "m20", "m21", "m22"};
                for (int i = 0; i < 9; i++) m[i] = r.getClass().getField(names[i]).getDouble(r);
                return m;
            } catch (ReflectiveOperationException | RuntimeException ex) {
                return null;
            }
        }

        @Nullable Vec3 position() {
            try {
                Object p = vehicle.getClass().getField("position").get(vehicle);
                return new Vec3(p.getClass().getField("x").getDouble(p), p.getClass().getField("y").getDouble(p), p.getClass().getField("z").getDouble(p));
            } catch (ReflectiveOperationException | RuntimeException ex) {
                return null;
            }
        }

        void discardDisplays() {
            for (Display.BlockDisplay d : displays) d.discard();
            displays.clear();
        }

        /** Brings the block displays in line with the loads and the vehicle's pose. */
        void syncDisplays() {
            if (!(entity.level() instanceof ServerLevel level)) return;
            List<CargoBay.Placed> loads = bay.loads();
            double[] m = matrix();
            Vec3 pos = position();
            if (m == null || pos == null) return;
            if (loads.isEmpty() && displays.isEmpty()) { dirty = false; return; }
            while (displays.size() > loads.size()) displays.remove(displays.size() - 1).discard();
            while (displays.size() < loads.size()) {
                Display.BlockDisplay d = new Display.BlockDisplay(EntityType.BLOCK_DISPLAY, level);
                d.addTag(TAG);
                d.setNoGravity(true);
                d.setInvulnerable(true);
                call(setPosRot(), d, 2);
                call(setBrightness(), d, new net.minecraft.util.Brightness(15, 15));
                level.addFreshEntity(d);
                displays.add(d);
            }
            Quaternionf q = new Quaternionf().setFromNormalized(new Matrix3f((float) m[0], (float) m[3], (float) m[6], (float) m[1], (float) m[4], (float) m[7], (float) m[2], (float) m[5], (float) m[8]));
            for (int i = 0; i < loads.size(); i++) {
                CargoBay.Placed p = loads.get(i);
                Display.BlockDisplay d = displays.get(i);
                double lx = spec.ox + p.x, ly = spec.oy + p.y, lz = spec.oz + p.z;
                Vector3f t = new Vector3f((float) (m[0] * lx + m[1] * ly + m[2] * lz), (float) (m[3] * lx + m[4] * ly + m[5] * lz), (float) (m[6] * lx + m[7] * ly + m[8] * lz));
                call(setBlockState(), d, blockFor(p.type));
                call(setTransformation(), d, new Transformation(t, q, new Vector3f(p.w(), p.h(), p.l()), new Quaternionf()));
                if (d.distanceToSqr(pos) > 1.0E-6) d.setPos(pos.x, pos.y, pos.z);
            }
            dirty = false;
        }
    }

    private static BlockState blockFor(LoadType t) {
        if (t.container()) return Blocks.IRON_BLOCK.defaultBlockState();
        var h = Freight.LOADS.get(t.id());
        Block b = h == null ? Blocks.BARREL : h.get();
        return b.defaultBlockState();
    }

    // ---- display plumbing (the setters are private in vanilla) ----

    private static Method setBlockState() {
        if (setBlockState == null) setBlockState = method(Display.BlockDisplay.class, "setBlockState", BlockState.class);
        return setBlockState;
    }

    private static Method setTransformation() {
        if (setTransformation == null) setTransformation = method(Display.class, "setTransformation", Transformation.class);
        return setTransformation;
    }

    private static Method setBrightness;

    private static Method setBrightness() {
        if (setBrightness == null) setBrightness = method(Display.class, "setBrightnessOverride", net.minecraft.util.Brightness.class);
        return setBrightness;
    }

    private static Method setPosRot() {
        if (setPosRot == null) setPosRot = method(Display.class, "setPosRotInterpolationDuration", int.class);
        return setPosRot;
    }

    private static Method method(Class<?> c, String name, Class<?>... args) {
        try {
            Method m = c.getDeclaredMethod(name, args);
            m.setAccessible(true);
            return m;
        } catch (ReflectiveOperationException ex) {
            throw new IllegalStateException("Display." + name + " not found", ex);
        }
    }

    private static void call(Method m, Object target, Object arg) {
        try {
            m.invoke(target, arg);
        } catch (ReflectiveOperationException ex) {
            throw new IllegalStateException(ex);
        }
    }

    // ---- server tick ----

    private static void serverTick(ServerTickEvent.Post e) {
        List<VehicleCarrier> all;
        synchronized (CARRIERS) { all = new ArrayList<>(CARRIERS.values()); }
        for (VehicleCarrier c : all) {
            if (c.entity.isRemoved()) { c.discardDisplays(); CARRIERS.remove(c.entity); continue; }
            c.syncDisplays();
        }
        if (++tick % 20 != 0) return;
        for (ServerLevel level : e.getServer().getAllLevels()) {
            for (Entity en : level.getAllEntities()) {
                if (isIv(en) && !CARRIERS.containsKey(en) && en.hasData(BAY_DATA) && !en.getData(BAY_DATA).loads().isEmpty()) of(en);
                else if (en instanceof Display.BlockDisplay bd && bd.entityTags().contains(TAG) && !isTracked(bd)) bd.discard();
            }
        }
    }

    private static boolean isTracked(Display.BlockDisplay bd) {
        synchronized (CARRIERS) {
            for (VehicleCarrier c : CARRIERS.values()) if (c.displays.contains(bd)) return true;
        }
        return false;
    }
}
