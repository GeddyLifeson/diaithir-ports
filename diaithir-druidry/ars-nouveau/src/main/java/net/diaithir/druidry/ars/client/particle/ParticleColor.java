package net.diaithir.druidry.ars.client.particle;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.diaithir.druidry.ars.ArsNouveau;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

import java.util.Objects;

/** Port stub: plain RGB colour data (0-255 per channel); no rendering code. */
public class ParticleColor implements Cloneable {
    public static final Identifier ID = ArsNouveau.prefix("default");
    public static final ParticleColor DEFAULT = new ParticleColor(255, 25, 180);
    public static final ParticleColor WHITE = new ParticleColor(255, 255, 255);
    public static final ParticleColor RED = new ParticleColor(255, 50, 50);
    public static final ParticleColor GREEN = new ParticleColor(50, 255, 50);
    public static final ParticleColor BLUE = new ParticleColor(50, 50, 255);
    public static final ParticleColor YELLOW = new ParticleColor(255, 255, 0);
    public static final ParticleColor PURPLE = new ParticleColor(255, 50, 255);
    public static final ParticleColor CYAN = new ParticleColor(50, 255, 255);
    public static final ParticleColor ORANGE = new ParticleColor(255, 128, 0);
    public static final ParticleColor MAGENTA = new ParticleColor(255, 0, 255);
    public static final ParticleColor LIGHT_BLUE = new ParticleColor(173, 216, 230);
    public static final ParticleColor LIME = new ParticleColor(0, 255, 0);
    public static final ParticleColor PINK = new ParticleColor(255, 192, 203);
    public static final ParticleColor GRAY = new ParticleColor(128, 128, 128);
    public static final ParticleColor LIGHT_GRAY = new ParticleColor(211, 211, 211);
    public static final ParticleColor BROWN = new ParticleColor(125, 42, 42);
    public static final ParticleColor BLACK = new ParticleColor(0, 0, 0);
    public static final IntWrapper TO_HIGHLIGHT = new IntWrapper(255, 50, 50);
    public static final IntWrapper FROM_HIGHLIGHT = new IntWrapper(50, 255, 255);

    public static final Codec<ParticleColor> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.INT.fieldOf("r").forGetter(ParticleColor::getRedInt),
            Codec.INT.fieldOf("g").forGetter(ParticleColor::getGreenInt),
            Codec.INT.fieldOf("b").forGetter(ParticleColor::getBlueInt)
    ).apply(i, ParticleColor::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, ParticleColor> STREAM = StreamCodec.of(
            (buf, c) -> {
                buf.writeInt(c.getRedInt());
                buf.writeInt(c.getGreenInt());
                buf.writeInt(c.getBlueInt());
            },
            buf -> new ParticleColor(buf.readInt(), buf.readInt(), buf.readInt()));

    private float red;
    private float green;
    private float blue;

    public ParticleColor(int r, int g, int b) {
        this.red = r / 255.0F;
        this.green = g / 255.0F;
        this.blue = b / 255.0F;
    }

    public ParticleColor(double r, double g, double b) {
        this.red = (float) r;
        this.green = (float) g;
        this.blue = (float) b;
    }

    public ParticleColor(CompoundTag tag) {
        this(tag.getIntOr("r", 255), tag.getIntOr("g", 25), tag.getIntOr("b", 180));
    }

    public static ParticleColor defaultParticleColor() {
        return new ParticleColor(255, 25, 180);
    }

    public static ParticleColor makeRandomColor(int r, int g, int b, net.minecraft.util.RandomSource rand) {
        return new ParticleColor(rand.nextInt(Math.max(1, r + 1)), rand.nextInt(Math.max(1, g + 1)), rand.nextInt(Math.max(1, b + 1)));
    }

    /** Port stub: constant colour, no animation. */
    public ParticleColor transition(int ticks) {
        return this;
    }

    public double euclideanDistance(ParticleColor other) {
        double dr = getRedInt() - other.getRedInt();
        double dg = getGreenInt() - other.getGreenInt();
        double db = getBlueInt() - other.getBlueInt();
        return Math.sqrt(dr * dr + dg * dg + db * db);
    }

    public static ParticleColor fromInt(int color) {
        return new ParticleColor((color >> 16) & 0xFF, (color >> 8) & 0xFF, color & 0xFF);
    }

    public float getRed() {
        return red;
    }

    public float getGreen() {
        return green;
    }

    public float getBlue() {
        return blue;
    }

    public int getRedInt() {
        return Math.round(red * 255);
    }

    public int getGreenInt() {
        return Math.round(green * 255);
    }

    public int getBlueInt() {
        return Math.round(blue * 255);
    }

    public int getColor() {
        return (getRedInt() << 16) | (getGreenInt() << 8) | getBlueInt();
    }

    public Identifier getRegistryName() {
        return ID;
    }

    public CompoundTag serialize() {
        CompoundTag tag = new CompoundTag();
        tag.putString("type", getRegistryName().toString());
        tag.putInt("r", getRedInt());
        tag.putInt("g", getGreenInt());
        tag.putInt("b", getBlueInt());
        return tag;
    }

    public IntWrapper toWrapper() {
        return new IntWrapper(getRedInt(), getGreenInt(), getBlueInt());
    }

    @Override
    public ParticleColor clone() {
        return new ParticleColor(getRedInt(), getGreenInt(), getBlueInt());
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof ParticleColor c && getColor() == c.getColor();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getColor());
    }

    public static class IntWrapper {
        public int r;
        public int g;
        public int b;

        public IntWrapper(int r, int g, int b) {
            this.r = r;
            this.g = g;
            this.b = b;
        }

        public IntWrapper(ParticleColor color) {
            this(color.getRedInt(), color.getGreenInt(), color.getBlueInt());
        }

        public ParticleColor toParticleColor() {
            return new ParticleColor(r, g, b);
        }

        public CompoundTag serialize() {
            CompoundTag tag = new CompoundTag();
            tag.putInt("r", r);
            tag.putInt("g", g);
            tag.putInt("b", b);
            return tag;
        }

        public static IntWrapper deserialize(CompoundTag tag) {
            return new IntWrapper(tag.getIntOr("r", 255), tag.getIntOr("g", 25), tag.getIntOr("b", 180));
        }
    }
}
