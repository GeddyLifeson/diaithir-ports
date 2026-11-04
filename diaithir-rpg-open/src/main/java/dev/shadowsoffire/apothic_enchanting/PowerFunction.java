package dev.shadowsoffire.apothic_enchanting;

import java.math.BigDecimal;
import java.util.function.IntFunction;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.shadowsoffire.apothic_attributes.repack.evalex.Expression;
import net.minecraft.core.Holder;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.enchantment.Enchantment;

/**
 * Simple int to int function, used for converting a level into a required enchanting power.
 * <p>
 * After the datamap migration, {@code PowerFunction} instances are pure (no enchantment holder is captured at
 * construction). The enchantment context is passed to {@link #getPower(int, Holder)} at evaluation time.
 */
public sealed interface PowerFunction {

    MapCodec<PowerFunction> CODEC = Type.CODEC.dispatchMap("type", PowerFunction::getType, t -> switch (t) {
        case DEFAULT_MIN -> DefaultMinPowerFunction.CODEC;
        case DEFAULT_MAX -> DefaultMaxPowerFunction.CODEC;
        case EXPRESSION -> ExpressionPowerFunction.CODEC;
    });

    int getPower(int level, Holder<Enchantment> ench);

    Type getType();

    enum Type implements StringRepresentable {
        DEFAULT_MIN("default_min"),
        DEFAULT_MAX("default_max"),
        EXPRESSION("expression");

        public static final IntFunction<Type> BY_ID = ByIdMap.continuous(Enum::ordinal, values(), ByIdMap.OutOfBoundsStrategy.ZERO);
        public static final Codec<Type> CODEC = StringRepresentable.fromValues(Type::values);

        private final String name;

        Type(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }
    }

    /**
     * Default minimum power function. For levels at or below the enchantment's vanilla max, returns
     * {@link Enchantment#getMinCost(int)} directly. For higher levels, extrapolates by the per-level slope of
     * {@code getMinCost}, scaled by {@code (level - vanillaMax) ^ 1.6}, so the cost grows exponentially above vanilla.
     */
    final class DefaultMinPowerFunction implements PowerFunction {

        public static final DefaultMinPowerFunction INSTANCE = new DefaultMinPowerFunction();
        public static final MapCodec<DefaultMinPowerFunction> CODEC = MapCodec.unit(INSTANCE);

        private DefaultMinPowerFunction() {}

        @Override
        public int getPower(int level, Holder<Enchantment> enchHolder) {
            Enchantment ench = enchHolder.value();
            if (level > ench.definition().maxLevel() && level > 1) {
                int diff = ench.getMinCost(ench.getMaxLevel()) - ench.getMinCost(ench.getMaxLevel() - 1);
                if (diff == 0) diff = 15;
                return ench.getMinCost(level) + diff * (int) Math.pow(level - ench.getMaxLevel(), 1.6);
            }
            return ench.getMinCost(level);
        }

        @Override
        public Type getType() {
            return Type.DEFAULT_MIN;
        }

    }

    /**
     * Default maximum power function — always returns 200 (the maximum eterna value).
     */
    final class DefaultMaxPowerFunction implements PowerFunction {

        public static final DefaultMaxPowerFunction INSTANCE = new DefaultMaxPowerFunction();
        public static final MapCodec<DefaultMaxPowerFunction> CODEC = MapCodec.unit(INSTANCE);

        private DefaultMaxPowerFunction() {}

        @Override
        public int getPower(int level, Holder<Enchantment> ench) {
            return 200;
        }

        @Override
        public Type getType() {
            return Type.DEFAULT_MAX;
        }

    }

    /**
     * Power function parameterized by an EvalEx expression string. The expression has a single variable
     * {@code x} bound to the enchantment level.
     */
    final class ExpressionPowerFunction implements PowerFunction {

        public static final MapCodec<ExpressionPowerFunction> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            Codec.STRING.fieldOf("expression").forGetter(ExpressionPowerFunction::exprString))
            .apply(inst, ExpressionPowerFunction::new));

        private final String exprString;
        private transient final Expression ex;

        public ExpressionPowerFunction(String func) {
            this.exprString = func;
            this.ex = new Expression(func);
        }

        @Override
        public int getPower(int level, Holder<Enchantment> ench) {
            return this.ex.setVariable("x", new BigDecimal(level)).eval().intValue();
        }

        @Override
        public Type getType() {
            return Type.EXPRESSION;
        }

        public String exprString() {
            return this.exprString;
        }

    }
}
