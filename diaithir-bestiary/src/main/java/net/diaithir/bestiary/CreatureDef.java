package net.diaithir.bestiary;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.BossEvent;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.storage.loot.LootTable;

import java.util.Optional;

/** One creature, read from data/<namespace>/diaithirbestiary/creature/<name>.json. README.md documents every field. */
public record CreatureDef(Preset preset, Optional<String> name, Optional<Identifier> model, Optional<Identifier> texture,
                          Optional<Identifier> animation, float width, float height, Stats stats, Optional<Boolean> hostile,
                          Optional<Boolean> rideable, Optional<Boolean> flying, Optional<Tame> tame, Optional<Fuel> fuel,
                          Optional<Boss> boss, Optional<Spawn> spawn, Optional<ResourceKey<LootTable>> lootTable) {

    public record Stats(float health, float armor, float damage, float speed, float flyingSpeed, float followRange, float knockbackResistance) {
        static final Stats DEFAULT = new Stats(20, 0, 3, 0.25F, 0.4F, 32, 0);
        static final Codec<Stats> CODEC = RecordCodecBuilder.create(i -> i.group(
                ExtraCodecs.POSITIVE_FLOAT.optionalFieldOf("health", DEFAULT.health).forGetter(Stats::health),
                ExtraCodecs.NON_NEGATIVE_FLOAT.optionalFieldOf("armor", DEFAULT.armor).forGetter(Stats::armor),
                ExtraCodecs.NON_NEGATIVE_FLOAT.optionalFieldOf("damage", DEFAULT.damage).forGetter(Stats::damage),
                ExtraCodecs.NON_NEGATIVE_FLOAT.optionalFieldOf("speed", DEFAULT.speed).forGetter(Stats::speed),
                ExtraCodecs.NON_NEGATIVE_FLOAT.optionalFieldOf("flying_speed", DEFAULT.flyingSpeed).forGetter(Stats::flyingSpeed),
                ExtraCodecs.POSITIVE_FLOAT.optionalFieldOf("follow_range", DEFAULT.followRange).forGetter(Stats::followRange),
                Codec.floatRange(0, 1).optionalFieldOf("knockback_resistance", DEFAULT.knockbackResistance).forGetter(Stats::knockbackResistance)
        ).apply(i, Stats::new));
    }

    public record Tame(Item item, float chance) {
        static final Codec<Tame> CODEC = RecordCodecBuilder.create(i -> i.group(
                BuiltInRegistries.ITEM.byNameCodec().fieldOf("item").forGetter(Tame::item),
                Codec.floatRange(0, 1).optionalFieldOf("chance", 0.33F).forGetter(Tame::chance)
        ).apply(i, Tame::new));
    }

    public record Fuel(Item item, int ticks) {
        static final Codec<Fuel> CODEC = RecordCodecBuilder.create(i -> i.group(
                BuiltInRegistries.ITEM.byNameCodec().fieldOf("item").forGetter(Fuel::item),
                ExtraCodecs.POSITIVE_INT.optionalFieldOf("ticks", 1200).forGetter(Fuel::ticks)
        ).apply(i, Fuel::new));
    }

    public record Boss(float phaseTwoAt, BossEvent.BossBarColor color) {
        static final Boss DEFAULT = new Boss(0.5F, BossEvent.BossBarColor.PURPLE);
        static final Codec<Boss> CODEC = RecordCodecBuilder.create(i -> i.group(
                Codec.floatRange(0, 1).optionalFieldOf("phase_two_at", DEFAULT.phaseTwoAt).forGetter(Boss::phaseTwoAt),
                BossEvent.BossBarColor.CODEC.optionalFieldOf("bar_color", DEFAULT.color).forGetter(Boss::color)
        ).apply(i, Boss::new));
    }

    public record Spawn(TagKey<Biome> biomes, int weight, int minGroup, int maxGroup, int maxLight) {
        static final Codec<Spawn> CODEC = RecordCodecBuilder.create(i -> i.group(
                TagKey.codec(Registries.BIOME).fieldOf("biomes").forGetter(Spawn::biomes),
                Codec.intRange(1, 100).optionalFieldOf("weight", 10).forGetter(Spawn::weight),
                Codec.intRange(1, 16).optionalFieldOf("min_group", 1).forGetter(Spawn::minGroup),
                Codec.intRange(1, 16).optionalFieldOf("max_group", 1).forGetter(Spawn::maxGroup),
                Codec.intRange(0, 15).optionalFieldOf("max_light", 15).forGetter(Spawn::maxLight)
        ).apply(i, Spawn::new));
    }

    public static final Codec<CreatureDef> CODEC = RecordCodecBuilder.create(i -> i.group(
            Preset.CODEC.fieldOf("preset").forGetter(CreatureDef::preset),
            Codec.STRING.optionalFieldOf("name").forGetter(CreatureDef::name),
            Identifier.CODEC.optionalFieldOf("model").forGetter(CreatureDef::model),
            Identifier.CODEC.optionalFieldOf("texture").forGetter(CreatureDef::texture),
            Identifier.CODEC.optionalFieldOf("animation").forGetter(CreatureDef::animation),
            Codec.floatRange(0.1F, 16F).optionalFieldOf("width", 0.9F).forGetter(CreatureDef::width),
            Codec.floatRange(0.1F, 16F).optionalFieldOf("height", 1.4F).forGetter(CreatureDef::height),
            Stats.CODEC.optionalFieldOf("stats", Stats.DEFAULT).forGetter(CreatureDef::stats),
            Codec.BOOL.optionalFieldOf("hostile").forGetter(CreatureDef::hostile),
            Codec.BOOL.optionalFieldOf("rideable").forGetter(CreatureDef::rideable),
            Codec.BOOL.optionalFieldOf("flying").forGetter(CreatureDef::flying),
            Tame.CODEC.optionalFieldOf("tame").forGetter(CreatureDef::tame),
            Fuel.CODEC.optionalFieldOf("fuel").forGetter(CreatureDef::fuel),
            Boss.CODEC.optionalFieldOf("boss").forGetter(CreatureDef::boss),
            Spawn.CODEC.optionalFieldOf("spawn").forGetter(CreatureDef::spawn),
            LootTable.KEY_CODEC.optionalFieldOf("loot_table").forGetter(CreatureDef::lootTable)
    ).apply(i, CreatureDef::new));

    public boolean isHostile() {
        return hostile.orElse(preset != Preset.MECH);
    }

    public boolean isRideable() {
        return preset == Preset.MECH || rideable.orElse(preset == Preset.FLYER) && preset != Preset.AVATAR && preset != Preset.STALKER;
    }

    public boolean isFlying() {
        return flying.orElse(preset == Preset.FLYER);
    }

    public Boss bossOrDefault() {
        return boss.orElse(Boss.DEFAULT);
    }

    public String nameKey(Identifier id) {
        return name.orElse("entity." + id.getNamespace() + "." + id.getPath());
    }

    public Identifier modelId(Identifier id) {
        return model.orElse(id.withPrefix("entity/"));
    }

    public Identifier textureId(Identifier id) {
        return texture.orElse(id.withPath(p -> "textures/entity/" + p + ".png"));
    }

    public Identifier animationId(Identifier id) {
        return animation.orElse(id.withPrefix("entity/"));
    }
}
