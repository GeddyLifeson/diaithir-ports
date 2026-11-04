package io.github.tr100000.researcher.api;

import com.mojang.serialization.Codec;
import io.github.tr100000.researcher.Research;
import io.github.tr100000.researcher.ResearcherRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;

public interface ResearchReward {
    Codec<ResearchReward> CODEC = ResearcherRegistries.RESEARCH_REWARD_TYPE.byNameCodec()
            .dispatch(ResearchReward::getType, ResearchRewardType::codec);
    StreamCodec<RegistryFriendlyByteBuf, ResearchReward> STREAM_CODEC = ByteBufCodecs.registry(ResearcherRegistries.RESEARCH_REWARD_TYPE_KEY)
            .dispatch(ResearchReward::getType, ResearchRewardType::streamCodec);

    ResearchRewardType<?> getType();
    void grant(ServerPlayer player, Research research);
}
