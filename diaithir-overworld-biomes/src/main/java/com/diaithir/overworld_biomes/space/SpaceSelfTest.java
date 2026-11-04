package com.diaithir.overworld_biomes.space;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.util.FakePlayerFactory;

/** /stellaris selftest (game master or console): air rules and rocket launch with a fake player. */
final class SpaceSelfTest {
    private SpaceSelfTest() {}

    static void register(CommandDispatcher<CommandSourceStack> d) {
        d.register(Commands.literal("stellaris").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
            .then(Commands.literal("selftest").executes(c -> run(c.getSource().getServer()))));
    }

    private static ItemStack stack(String id) {
        return new ItemStack(BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath("stellaris", id)));
    }

    static int run(net.minecraft.server.MinecraftServer s) {
        ServerLevel moon = s.getLevel(SpaceTravel.MOON), ow = s.overworld();
        List<String> out = new ArrayList<>();
        ServerPlayer p = FakePlayerFactory.get(moon, new GameProfile(UUID.nameUUIDFromBytes("space-test".getBytes()), "SpaceTest"));
        p.setPos(5, 150, 5);
        moon.getChunk(0, 0);
        out.add(chk("vacuum: moon", SpaceTravel.isVacuum(moon), true));
        out.add(chk("vacuum: overworld", SpaceTravel.isVacuum(ow), false));
        out.add(chk("bare player cannot breathe", SpaceAir.breathable(p), false));
        String[] suit = {"space_suit_helmet", "space_suit_chestplate", "space_suit_leggings", "space_suit_boots"};
        EquipmentSlot[] slots = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
        for (int i = 0; i < 4; i++) p.setItemSlot(slots[i], stack(suit[i]));
        out.add(chk("suit without module cannot breathe", SpaceAir.breathable(p), false));
        p.getInventory().add(stack("space_suit_oxygen_module_tier_1"));
        out.add(chk("suit with module breathes", SpaceAir.breathable(p), true));
        p.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
        out.add(chk("missing helmet cannot breathe", SpaceAir.breathable(p), false));
        BlockPos at = p.blockPosition().offset(3, 0, 0);
        BlockPos old = at;
        moon.setBlock(at, BuiltInRegistries.BLOCK.getValue(Identifier.fromNamespaceAndPath("stellaris", "oxygen_distributor")).defaultBlockState(), 3);
        out.add(chk("near oxygen distributor breathes", SpaceAir.breathable(p), true));
        moon.removeBlock(old, false);
        out.add(chk("distributor removed cannot breathe", SpaceAir.breathable(p), false));
        out.add(chk("launch to terra", SpaceTravel.launch(p, "terra") && p.level().dimension().equals(net.minecraft.world.level.Level.OVERWORLD), true));
        out.add(chk("launch to moon", SpaceTravel.launch(p, "moon") && p.level().dimension().equals(SpaceTravel.MOON), true));
        out.add(chk("launch to earth_orbit", SpaceTravel.launch(p, "earth_orbit") && p.level().dimension().equals(SpaceTravel.ORBIT), true));
        out.add(chk("launch to unknown planet fails", SpaceTravel.launch(p, "no_such_planet"), false));
        for (String l : out) s.sendSystemMessage(net.minecraft.network.chat.Component.literal("[stellaris selftest] " + l));
        return out.stream().allMatch(l -> l.startsWith("PASS")) ? 1 : 0;
    }

    private static String chk(String what, boolean got, boolean want) {
        return (got == want ? "PASS " : "FAIL ") + what;
    }
}
