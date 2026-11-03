package io.github.tr100000.researcher;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.Contract;

public class ResearchProgress {
    public static final Codec<ResearchProgress> CODEC = Codec.either(
            Codec.BOOL.comapFlatMap(ResearchProgress::createFromBoolean, ResearchProgress::isFinished),
            Codec.INT.xmap(ResearchProgress::new, ResearchProgress::getCount)
    ).xmap(
            either -> either.left().isPresent() ? either.left().get() : either.right().orElseThrow(),
            researchProgress -> researchProgress.isFinished() ? Either.left(researchProgress) : Either.right(researchProgress)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, ResearchProgress> STREAM_CODEC = StreamCodec.ofMember(ResearchProgress::toPacket, ResearchProgress::fromPacket);

    private boolean finished;
    private int count;

    public ResearchProgress() {
        this(false, 0);
    }

    public ResearchProgress(boolean finished) {
        this(finished, finished ? -1 : 0);
    }

    public ResearchProgress(int count) {
        this(false, count);
    }

    public ResearchProgress(boolean finished, int count) {
        this.finished = finished;
        this.count = count;
    }

    @Contract(value = "_ -> new", pure = true)
    private static DataResult<ResearchProgress> createFromBoolean(boolean isFinished) {
        if (isFinished) {
            return DataResult.success(new ResearchProgress(true));
        }
        else {
            return DataResult.error(() -> "Illegal progress value in research data");
        }
    }

    @Contract(pure = true)
    public boolean isFinished() {
        return finished;
    }

    @Contract(pure = true)
    public int getCount() {
        return count;
    }

    @Contract(pure = true)
    public boolean hasProgress() {
        return isFinished() || getCount() > 0;
    }

    @Contract(pure = true)
    public float getProgress01(int max) {
        if (isFinished()) return 1;
        return (float)count / max;
    }

    @Contract(pure = true)
    public int getScaledProgress(int max, int scale) {
        return (int)(getProgress01(max) * scale);
    }

    public void increment(int max, int by) {
        count += by;
        if (count < 0) count = 0;
        if (count >= max) finish();
    }

    public void finish() {
        finished = true;
        count = -1;
    }

    public void reset() {
        finished = false;
        count = 0;
    }

    public void toPacket(FriendlyByteBuf buf) {
        buf.writeBoolean(finished);
        if (!finished) {
            buf.writeVarInt(count);
        }
    }

    @Contract("_ -> new")
    public static ResearchProgress fromPacket(FriendlyByteBuf buf) {
        if (buf.readBoolean()) {
            return new ResearchProgress(true);
        }
        else {
            return new ResearchProgress(buf.readVarInt());
        }
    }

    public static ResearchProgress combine(ResearchProgress a, ResearchProgress b) {
        if (a.finished) return a;
        if (b.finished) return b;
        return a.count > b.count ? a : b;
    }
}
