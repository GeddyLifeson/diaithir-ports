package net.diaithir.claims;

import java.util.Optional;
import java.util.function.Supplier;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

@Mod(DiaithirClaims.ID)
public final class DiaithirClaims {
    public static final String ID = "diaithirclaims";
    public static final int MAX_CHUNKS = 25;

    private static final DeferredRegister<AttachmentType<?>> ATTACH = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, ID);

    /** On a chunk: its claim, if any. Saved with the chunk. */
    public static final Supplier<AttachmentType<Optional<Claim>>> CLAIM = ATTACH.register("claim",
        () -> AttachmentType.<Optional<Claim>>builder(() -> Optional.empty()).serialize(Claim.CODEC.optionalFieldOf("c")).build());
    /** On a player: how many chunks they hold. Survives death. */
    public static final Supplier<AttachmentType<Integer>> COUNT = ATTACH.register("count",
        () -> AttachmentType.<Integer>builder(() -> 0).serialize(com.mojang.serialization.Codec.INT.fieldOf("n")).copyOnDeath().build());

    public DiaithirClaims(IEventBus bus) {
        ATTACH.register(bus);
        net.diaithir.claims.structures.StructureGuard.init();
        bus.addListener((net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent e) ->
            e.registrar("1").optional()
                .playToClient(MapPayload.TYPE, MapPayload.CODEC, (payload, ctx) -> {
                    if (net.neoforged.fml.loading.FMLEnvironment.getDist().isClient()) {
                        ctx.enqueueWork(() -> ClientHooks.open(payload));
                    }
                })
                .playToClient(ClaimInfo.TYPE, ClaimInfo.CODEC, (payload, ctx) -> {
                    if (net.neoforged.fml.loading.FMLEnvironment.getDist().isClient()) {
                        ctx.enqueueWork(() -> ClientHooks.info(payload));
                    }
                })
                .playToServer(ClaimQuery.TYPE, ClaimQuery.CODEC, (q, ctx) -> {
                    if (ctx.player() instanceof net.minecraft.server.level.ServerPlayer sp) {
                        LevelChunk ch = sp.level().getChunkSource().getChunkNow(q.cx(), q.cz());
                        String owner = ch == null ? "" : at(ch).map(Claim::ownerName).orElse("");
                        net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(sp, new ClaimInfo(q.cx(), q.cz(), owner));
                    }
                }));
        if (net.neoforged.fml.loading.FMLEnvironment.getDist().isClient()) {
            bus.addListener((net.neoforged.fml.event.lifecycle.InterModEnqueueEvent e) -> e.enqueueWork(ClientHooks::register));
        }
        NeoForge.EVENT_BUS.register(new Guard());
        NeoForge.EVENT_BUS.addListener((net.neoforged.neoforge.event.RegisterCommandsEvent e) -> Commands.register(e.getDispatcher()));
    }

    public static Optional<Claim> at(LevelChunk chunk) {
        return chunk.getData(CLAIM);
    }
}
