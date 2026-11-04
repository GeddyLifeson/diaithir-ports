package io.github.tr100000.researcher.reward;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.tr100000.researcher.Research;
import io.github.tr100000.researcher.api.ResearchReward;
import io.github.tr100000.researcher.api.ResearchRewardType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ExtraCodecs;

public record ExperienceReward(int levels, int points) implements ResearchReward {
    public static final MapCodec<ExperienceReward> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("levels", 0).forGetter(ExperienceReward::levels),
            ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("points", 0).forGetter(ExperienceReward::points)
    ).apply(instance, ExperienceReward::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, ExperienceReward> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, ExperienceReward::levels,
            ByteBufCodecs.VAR_INT, ExperienceReward::points,
            ExperienceReward::new
    );
    public static final ResearchRewardType<ExperienceReward> TYPE = new ResearchRewardType<>(CODEC, STREAM_CODEC);

    @Override
    public ResearchRewardType<?> getType() {
        return TYPE;
    }

    @Override
    public void grant(ServerPlayer player, Research research) {
        player.giveExperienceLevels(levels);
        player.giveExperiencePoints(points);
    }
}
