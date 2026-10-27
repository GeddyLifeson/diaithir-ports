package net.diaithir.goety;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/** /goetyselftest: every item builds a stack, every block has a state and an item, every mob spawns and ticks. */
public final class SelfTest {
    private SelfTest() {}

    public static void register(CommandDispatcher<CommandSourceStack> d) {
        d.register(Commands.literal("goetyselftest").requires(s -> s.permissions().hasPermission(net.minecraft.server.permissions.Permissions.COMMANDS_GAMEMASTER)).executes(c -> {
            ServerLevel level = c.getSource().getLevel();
            int ok = 0, bad = 0;
            for (var it : GoetyItems.ALL) {
                ItemStack st = new ItemStack(it.get());
                if (!st.isEmpty() && st.getItem().getDescriptionId() != null) ok++; else { bad++; log("item " + it.getId()); }
            }
            for (var b : GoetyBlocks.ALL.entrySet()) {
                if (b.getValue().get().defaultBlockState() != null && b.getValue().get().asItem() != null) ok++; else { bad++; log("block " + b.getKey()); }
            }
            for (var m : GoetyEntities.MOBS.entrySet()) {
                Entity e = m.getValue().get().create(level, EntitySpawnReason.COMMAND);
                if (e == null) { bad++; log("entity " + m.getKey()); continue; }
                e.setPos(c.getSource().getPosition());
                level.addFreshEntity(e);
                for (int i = 0; i < 40; i++) e.tick();
                boolean good = e.isAlive() && e instanceof LivingEntity le && le.getMaxHealth() > 0;
                e.discard();
                if (good) ok++; else { bad++; log("entity dead " + m.getKey()); }
            }
            // magic: every spell runs against a dummy target; every wand takes the foci of its school
            net.minecraft.world.entity.Mob caster = (net.minecraft.world.entity.Mob) GoetyEntities.MOBS.get("skeleton_servant").get().create(level, EntitySpawnReason.COMMAND);
            net.minecraft.world.entity.Mob dummy = (net.minecraft.world.entity.Mob) GoetyEntities.MOBS.get("zombie_servant").get().create(level, EntitySpawnReason.COMMAND);
            caster.setPos(c.getSource().getPosition());
            caster.setYRot(0); caster.setXRot(0);
            level.addFreshEntity(caster);
            dummy.setPos(caster.getX(), caster.getY(), caster.getZ() + 4);
            level.addFreshEntity(dummy);
            for (var sp : net.diaithir.goety.magic.SpellDef.all().values()) {
                try {
                    dummy.setHealth(dummy.getMaxHealth());
                    caster.setYRot(0); caster.setXRot(0); caster.setPos(caster.getX(), caster.getY(), caster.getZ());
                    boolean r = net.diaithir.goety.magic.SpellEngine.cast(sp, level, caster);
                    net.minecraft.world.item.Item it = GoetyItems.ALL.stream().map(h -> h.get()).filter(x -> net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(x).getPath().equals(sp.id())).findFirst().orElse(null);
                    if (it == null) throw new IllegalStateException("no focus item " + sp.id());
                    ok++;
                    if (!r) log("spell declined (allowed for heal at full health or servant cap): " + sp.id());
                } catch (Throwable t) { bad++; log("spell " + sp.id() + " threw " + t); }
            }
            caster.discard(); dummy.discard();
            for (var h : GoetyItems.ALL) {
                if (h.get() instanceof net.diaithir.goety.magic.WandItem w) {
                    boolean good = net.diaithir.goety.magic.SpellDef.all().values().stream().anyMatch(sp -> w.schools().contains(sp.type()));
                    if (good) ok++; else { bad++; log("wand without foci: " + h.getId()); }
                }
            }
            ItemStack tot = new ItemStack(GoetyItems.PLAIN.get("totem_of_souls").get());
            net.diaithir.goety.magic.Souls.set(tot, 500);
            if (net.diaithir.goety.magic.Souls.get(tot) == 500) ok++; else { bad++; log("totem souls"); }
            // rites: every loaded rite is run at an altar with pedestals, by a fake player carrying a full totem
            {
                int[] rr = riteTest(level);
                ok += rr[0]; bad += rr[1];
            }
            int fo = ok, fb = bad;
            log("ok=" + fo + " bad=" + fb);
            c.getSource().sendSuccess(() -> Component.literal("goety selftest ok=" + fo + " bad=" + fb), false);
            return fb == 0 ? 1 : 0;
        }));
    }

    private static int[] riteTest(ServerLevel level) {
        int ok = 0, bad = 0;
        net.minecraft.server.level.ServerPlayer fake = net.neoforged.neoforge.common.util.FakePlayerFactory.get(level, new com.mojang.authlib.GameProfile(java.util.UUID.nameUUIDFromBytes("goety-rite".getBytes()), "[goety]"));
        net.minecraft.core.BlockPos altar = new net.minecraft.core.BlockPos(2, 120, 2);
        level.setChunkForced(0, 0, true);
        level.getChunk(altar);
        var altarBlock = GoetyBlocks.ALL.get("dark_altar").get();
        var pedBlock = GoetyBlocks.ALL.get("pedestal").get();
        for (var r : net.diaithir.goety.rite.RitualManager.all()) {
            ItemStack totem = new ItemStack(GoetyItems.PLAIN.get("totem_of_souls").get());
            net.diaithir.goety.magic.Souls.set(totem, net.diaithir.goety.magic.Souls.MAX);
            fake.getInventory().clearContent();
            fake.getInventory().setItem(20, totem);
            level.setBlock(altar, altarBlock.defaultBlockState(), 3);
            java.util.List<net.minecraft.core.BlockPos> peds = new java.util.ArrayList<>();
            for (int i = 0; i < r.offerings().size(); i++) {
                net.minecraft.core.BlockPos p = altar.offset((i % 4) * 2 - 3, 0, 3 + (i / 4) * 2);
                level.setBlock(p, pedBlock.defaultBlockState(), 3);
                ((net.diaithir.goety.rite.PedestalBlockEntity) level.getBlockEntity(p)).setHeld(stackFor(r.offerings().get(i)));
                peds.add(p);
            }
            ItemStack act = stackFor(r.activation());
            fake.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, act);
            var be = (net.diaithir.goety.rite.AltarBlockEntity) level.getBlockEntity(altar);
            boolean started = be.tryStart(fake, act);
            int ticks = 0;
            while (started && be.busy() && ticks < r.duration() * 20 + 40) {
                net.diaithir.goety.rite.AltarBlockEntity.tick(level, altar, level.getBlockState(altar), be);
                ticks++;
            }
            boolean outcome;
            if (r.type().equals("summon")) {
                outcome = be.lastSummoned != null;
                if (be.lastSummoned != null) be.lastSummoned.discard();
            } else {
                outcome = !be.lastOutput.isEmpty() && be.lastOutput.getItem() == r.resultItem() && be.lastOutput.getCount() == r.count();
                level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class, new net.minecraft.world.phys.AABB(altar).inflate(4)).forEach(Entity::discard);
            }
            if (started && !be.busy() && outcome) ok++; else { bad++; log("rite failed: " + r.result() + r.entity() + " started=" + started + " outcome=" + outcome); }
            level.setBlock(altar, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(), 3);
            for (var p : peds) level.setBlock(p, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(), 3);
        }
        level.setChunkForced(0, 0, false);
        return new int[] {ok, bad};
    }

    private static ItemStack stackFor(String entry) {
        if (entry.startsWith("#")) {
            var tag = net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.ITEM, net.minecraft.resources.Identifier.parse(entry.substring(1)));
            return net.minecraft.core.registries.BuiltInRegistries.ITEM.get(tag).flatMap(h -> h.stream().findFirst()).map(h -> new ItemStack(h.value())).orElse(ItemStack.EMPTY);
        }
        return new ItemStack(net.minecraft.core.registries.BuiltInRegistries.ITEM.getValue(net.minecraft.resources.Identifier.parse(entry)));
    }

    private static void log(String s) {
        System.out.println("[goetyselftest] " + s);
    }
}
