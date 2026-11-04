package io.github.tr100000.researcher.reward;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.tr100000.researcher.Research;
import io.github.tr100000.researcher.api.ResearchReward;
import io.github.tr100000.researcher.api.ResearchRewardType;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Util;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.FireworkExplosion;
import net.minecraft.world.item.component.Fireworks;
import net.minecraft.world.level.Level;

import java.util.List;

public record FireworksReward(int amount, boolean ownedByPlayer) implements ResearchReward {
    public static final MapCodec<FireworksReward> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("amount", 0).forGetter(FireworksReward::amount),
            Codec.BOOL.optionalFieldOf("owned_by_player", true).forGetter(FireworksReward::ownedByPlayer)
    ).apply(instance, FireworksReward::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, FireworksReward> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, FireworksReward::amount,
            ByteBufCodecs.BOOL, FireworksReward::ownedByPlayer,
            FireworksReward::new
    );
    public static final ResearchRewardType<FireworksReward> TYPE = new ResearchRewardType<>(CODEC, STREAM_CODEC);

    private static final List<FireworkExplosion.Shape> FIREWORK_SHAPES = List.of(
            FireworkExplosion.Shape.SMALL_BALL,
            FireworkExplosion.Shape.LARGE_BALL,
            FireworkExplosion.Shape.STAR,
            FireworkExplosion.Shape.CREEPER,
            FireworkExplosion.Shape.BURST
    );

    @Override
    public ResearchRewardType<?> getType() {
        return TYPE;
    }

    @Override
    public void grant(ServerPlayer player, Research research) {
        Level level = player.level();
        if (level instanceof ServerLevel serverLevel) {
            RandomSource random = serverLevel.getRandom();

            for (int i = 0; i < amount; i++) {
                ItemStack fireworkStack = new ItemStack(Items.FIREWORK_ROCKET);

                int colorCount = random.nextIntBetweenInclusive(1, 3);
                int fadeColorCount = random.nextIntBetweenInclusive(1, 3);

                IntList colors = getRandomFireworkColors(colorCount, random);
                IntList fadeColors = getRandomFireworkColors(fadeColorCount, random);

                FireworkExplosion explosion = new FireworkExplosion(
                        Util.getRandom(FIREWORK_SHAPES, random),
                        colors,
                        fadeColors,
                        true,
                        true
                );

                Fireworks fireworks = new Fireworks(random.nextIntBetweenInclusive(1, 2), List.of(explosion));
                fireworkStack.set(DataComponents.FIREWORKS, fireworks);

                Projectile.spawnProjectile(
                        new FireworkRocketEntity(
                                serverLevel,
                                ownedByPlayer ? null : player,
                                getRandomOffset(player.getX(), 6, random),
                                getRandomOffset(player.getY(), 4, random) + 3,
                                getRandomOffset(player.getZ(), 6, random),
                                fireworkStack
                        ),
                        serverLevel,
                        fireworkStack
                );
            }
        }
    }

    private IntList getRandomFireworkColors(int count, RandomSource random) {
        IntList results = new IntArrayList(count);
        for (int i = 0; i < count; i++) {
            results.add(Util.getRandom(DyeColor.VALUES, random).getFireworkColor());
        }
        return results;
    }

    private double getRandomOffset(double original, double distribution, RandomSource random) {
        return original + (random.nextDouble() - 0.5) * distribution;
    }
}
