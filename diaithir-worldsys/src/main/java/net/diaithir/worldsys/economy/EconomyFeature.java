package net.diaithir.worldsys.economy;

import com.mojang.brigadier.Command;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.npc.villager.Villager;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/** The economy on the server: the villager quest chain, the weekly turn and /company. Port of the KubeJS economy; KubeJS stays until this matches it. */
public final class EconomyFeature {
    private static final long WEEK_TICKS = 24000L * 7;

    public static void register(IEventBus modBus) {
        NeoForge.EVENT_BUS.addListener(ServerStartingEvent.class, e -> EconWorld.server(e.getServer()));
        NeoForge.EVENT_BUS.addListener(RegisterCommandsEvent.class, EconomyFeature::commands);
        NeoForge.EVENT_BUS.addListener(PlayerInteractEvent.EntityInteract.class, EconomyFeature::interact);
        NeoForge.EVENT_BUS.addListener(ServerTickEvent.Post.class, EconomyFeature::tick);
    }

    private static EconQuests.World world() {
        return new EconQuests.World(EconWorld.state(), EconWorld.data(), EconWorld.claims(), c -> EconWorld.ctx(null, c), true);
    }

    private static void interact(PlayerInteractEvent.EntityInteract e) {
        if (!(e.getEntity() instanceof ServerPlayer p) || !(e.getTarget() instanceof Villager v) || EconWorld.server() == null) return;
        EconClaims.Claim c = EconWorld.claimAt(v.getX(), v.getZ());
        if (c == null) return;
        String prof = v.getVillagerData().profession().unwrapKey().map(k -> k.identifier().getPath()).orElse("none");
        EconQuests.talk(world(), new ServerPlayerHolder(p), new EconQuests.Place(c.town(), c.name(), c.culture(), EconWorld.ageOf(c.culture())), prof);
        EconWorld.dirty();
    }

    private static final double WEEKLY_BILL = 0.004;

    private static void tick(ServerTickEvent.Post e) {
        var server = e.getServer();
        if (EconWorld.server() == null) EconWorld.server(server);
        long t = server.overworld().getGameTime();
        if (t == 0 || t % WEEK_TICKS != 0) return;
        weekly();
    }

    /** Once a week, in the order the KubeJS economy runs it (econWeekly). */
    static void weekly() {
        EconState st = EconWorld.state();
        var data = EconWorld.data();
        st.week++;
        EconWorld.claims();
        EconFrictions.spoil(st, data::good, region -> {
            double[] pos = EconWorld.REGIONS.get(region);
            return pos == null ? 1.0 : EconFrictions.warmth(EconHooks.temperatureAt.apply(pos[0], pos[1]));
        }, id -> null);
        EconFrictions.season(st, data::good, st.week, region -> null);
        java.util.Map<String, String> seat = new java.util.LinkedHashMap<>();
        for (EconClaims.Claim c : EconWorld.claims()) seat.putIfAbsent(c.culture(), c.town());
        for (var s : seat.entrySet()) {
            String culture = s.getKey();
            EconMarket.Ctx ctx = EconWorld.ctx(s.getValue(), culture);
            String age = EconWorld.ageOf(culture);
            if (ctx.kind() == EconPolity.Kind.COIN && ctx.did() != null) EconFrictions.wear(st, culture, ctx.did());
            EconPolity.week(st, culture, ctx.did(), ctx.kind(), WEEKLY_BILL * st.cur(culture).ref, ctx.rate());
            int ai = EconPolity.AGES.indexOf(age);
            if (ai >= EconPolity.AGES.indexOf("V")) EconBonds.week(st, culture);
            if (ai >= EconPolity.AGES.indexOf("III")) EconBank.week(st.bank(culture), age, ctx.rate());
        }
        EconCompany.week(st, EconWorld.REGIONS, data::good, culture -> EconWorld.ctx(null, culture).scale(), culture -> data.openIds(EconWorld.ageOf(culture)));
        java.util.List<String> traders = seat.keySet().stream().filter(c -> EconPolity.AGES.indexOf(EconWorld.ageOf(c)) >= EconPolity.AGES.indexOf(EconExchange.AGE)).toList();
        if (!traders.isEmpty()) EconExchange.week(st, traders, EconWorld::cultureName);
        EconWorld.dirty();
    }

    private static void commands(RegisterCommandsEvent e) {
        e.getDispatcher().register(BankCommands.bank());
        e.getDispatcher().register(ExchangeCommands.build());
        e.getDispatcher().register(BankCommands.bond());
        e.getDispatcher().register(CompanyCommands.build()
            .then(Commands.literal("quest").executes(ctx -> {
                var p = ctx.getSource().getPlayerOrException();
                var q = EconWorld.state().quest(p.getUUID().toString());
                String s = q.job != null ? "Safe passage: take " + q.job.n + " goods to " + q.job.name + "." : "company".equals(q.company) ? "Founded." : "offered".equals(q.company) ? "Found a company: build an office, then /company found." : "Talk to working people in towns; one of them needs a load moved.";
                ctx.getSource().sendSuccess(() -> Component.literal(s), false);
                return Command.SINGLE_SUCCESS;
            })));
    }
}
