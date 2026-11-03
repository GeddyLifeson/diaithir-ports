package io.github.tr100000.researcher.reward;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.tr100000.researcher.Research;
import io.github.tr100000.researcher.api.ResearchReward;
import io.github.tr100000.researcher.api.ResearchRewardType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

import java.util.List;

public record LootReward(List<ResourceKey<LootTable>> loot) implements ResearchReward {
    public static final MapCodec<LootReward> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            LootTable.KEY_CODEC.listOf().fieldOf("loot").forGetter(LootReward::loot)
    ).apply(instance, LootReward::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, LootReward> STREAM_CODEC = StreamCodec.composite(
            ResourceKey.streamCodec(Registries.LOOT_TABLE).apply(ByteBufCodecs.list()), LootReward::loot,
            LootReward::new
    );
    public static final ResearchRewardType<LootReward> TYPE = new ResearchRewardType<>(CODEC, STREAM_CODEC);

    @Override
    public ResearchRewardType<?> getType() {
        return TYPE;
    }

    @Override
    public void grant(ServerPlayer player, Research research) {
        ServerLevel level = player.level();
        MinecraftServer server = level.getServer();
        LootParams params = new LootParams.Builder(level)
                .withParameter(LootContextParams.THIS_ENTITY, player)
                .withParameter(LootContextParams.ORIGIN, player.position())
                .create(LootContextParamSets.ADVANCEMENT_REWARD);
        boolean changes = false;

        for (ResourceKey<LootTable> lootTable : loot) {
            for (ItemStack stack : server.reloadableRegistries().getLootTable(lootTable).getRandomItems(params)) {
                if (player.addItem(stack)) {
                    level.playSound(
                            null,
                            player.getX(), player.getY(), player.getZ(),
                            SoundEvents.ITEM_PICKUP,
                            SoundSource.PLAYERS,
                            0.2F,
                            ((player.getRandom().nextFloat() - player.getRandom().nextFloat()) * 0.7F + 1.0F) * 2.0F
                    );
                    changes = true;
                }
                else {
                    ItemEntity drop = player.drop(stack, false);
                    if (drop != null) {
                        drop.setDefaultPickUpDelay();
                        drop.setTarget(player.getUUID());
                    }
                }
            }
        }

        if (changes) {
            player.containerMenu.broadcastChanges();
        }
    }
}
