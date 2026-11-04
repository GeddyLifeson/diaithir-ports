package net.diaithir.claims.capitals;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import java.util.Map;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

/**
 * Taxes for capitals (the kingdom-tax idea of Tale of Kingdoms, LGPL-3.0; clean-room, no code taken).
 * The ruler sets one item and amount per day: /capital tax set ITEM AMOUNT. Subjects pay with /capital pay, once per real day.
 * The ruler empties the treasury with /capital treasury take.
 */
public final class Taxes {
    private Taxes() {}

    public static void init() {
        NeoForge.EVENT_BUS.addListener((RegisterCommandsEvent e) -> register(e.getDispatcher()));
    }

    private static int say(CommandSourceStack s, String t) {
        s.sendSystemMessage(Component.literal(t));
        return 1;
    }

    private static int count(ServerPlayer p, net.minecraft.world.item.Item item) {
        int n = 0;
        for (ItemStack s : p.getInventory().getNonEquipmentItems()) if (s.is(item)) n += s.getCount();
        return n;
    }

    private static void register(CommandDispatcher<CommandSourceStack> d) {
        d.register(Commands.literal("capital")
            .then(Commands.literal("tax").then(Commands.literal("set")
                .then(Commands.argument("item", StringArgumentType.string()).then(Commands.argument("amount", IntegerArgumentType.integer(0, 64)).executes(c -> {
                    ServerPlayer p = c.getSource().getPlayerOrException();
                    CapitalData cd = CapitalData.get(c.getSource().getServer());
                    Capital k = cd.ruledBy(p.getUUID());
                    if (k == null) return say(c.getSource(), "You rule no capital.");
                    Identifier id = Identifier.tryParse(StringArgumentType.getString(c, "item"));
                    if (id == null || BuiltInRegistries.ITEM.get(id).isEmpty()) return say(c.getSource(), "No such item.");
                    TaxData.get(c.getSource().getServer()).setRate(k.name(), new TaxData.Rate(id.toString(), IntegerArgumentType.getInteger(c, "amount")));
                    return say(c.getSource(), "Daily tax of " + k.name() + ": " + IntegerArgumentType.getInteger(c, "amount") + " " + id);
                })))))
            .then(Commands.literal("pay").executes(c -> {
                ServerPlayer p = c.getSource().getPlayerOrException();
                CapitalData cd = CapitalData.get(c.getSource().getServer());
                Capital k = cd.subjectOf(p.getUUID());
                if (k == null) return say(c.getSource(), "You are nobody's subject.");
                TaxData td = TaxData.get(c.getSource().getServer());
                TaxData.Rate r = td.rate(k.name());
                if (r == null || r.amount() == 0) return say(c.getSource(), "No tax is set.");
                long now = System.currentTimeMillis() / 1000;
                long left = td.lastPaid(k.name(), p.getUUID()) + TaxData.PERIOD_SECONDS - now;
                if (left > 0) return say(c.getSource(), "Paid already; next due in " + (left / 60) + " min.");
                var item = BuiltInRegistries.ITEM.get(Identifier.parse(r.item())).get().value();
                if (count(p, item) < r.amount()) return say(c.getSource(), "You need " + r.amount() + " " + r.item() + ".");
                int need = r.amount();
                for (ItemStack s : p.getInventory().getNonEquipmentItems()) {
                    if (need > 0 && s.is(item)) {
                        int t = Math.min(need, s.getCount());
                        s.shrink(t);
                        need -= t;
                    }
                }
                td.paid(k.name(), p.getUUID(), now, r.item(), r.amount());
                return say(c.getSource(), "Tax paid to " + k.name() + ".");
            }))
            .then(Commands.literal("treasury")
                .executes(c -> {
                    ServerPlayer p = c.getSource().getPlayerOrException();
                    Capital k = CapitalData.get(c.getSource().getServer()).ruledBy(p.getUUID());
                    if (k == null) return say(c.getSource(), "You rule no capital.");
                    Map<String, Integer> m = TaxData.get(c.getSource().getServer()).view(k.name());
                    return say(c.getSource(), m.isEmpty() ? "The treasury is empty." : m.toString());
                })
                .then(Commands.literal("take").executes(c -> {
                    ServerPlayer p = c.getSource().getPlayerOrException();
                    Capital k = CapitalData.get(c.getSource().getServer()).ruledBy(p.getUUID());
                    if (k == null) return say(c.getSource(), "You rule no capital.");
                    Map<String, Integer> m = TaxData.get(c.getSource().getServer()).take(k.name());
                    m.forEach((id, n) -> BuiltInRegistries.ITEM.get(Identifier.parse(id)).ifPresent(h -> {
                        int left = n;
                        while (left > 0) {
                            ItemStack s = new ItemStack(h.value(), Math.min(left, h.value().getDefaultMaxStackSize()));
                            left -= s.getCount();
                            if (!p.getInventory().add(s)) p.drop(s, false);
                        }
                    }));
                    return say(c.getSource(), "Treasury emptied into your pack.");
                }))));
    }
}
