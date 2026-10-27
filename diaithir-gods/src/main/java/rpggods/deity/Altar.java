package rpggods.deity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import rpggods.util.altar.AltarItems;
import rpggods.util.altar.AltarPose;

import java.util.Optional;

public class Altar {

    public static final Identifier MATERIAL = Identifier.parse("stone");

    public static final Altar EMPTY = new Altar(true, Optional.empty(), false, false, 0,
            AltarItems.EMPTY, MATERIAL, AltarPose.EMPTY, false);

    public static final Codec<Altar> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.optionalFieldOf("enabled", true).forGetter(Altar::isEnabled),
            Codec.STRING.optionalFieldOf("name").forGetter(Altar::getName),
            Codec.BOOL.optionalFieldOf("female", false).forGetter(Altar::isFemale),
            Codec.BOOL.optionalFieldOf("slim", false).forGetter(Altar::isSlim),
            Codec.INT.optionalFieldOf("light", 0).forGetter(Altar::getLightLevel),
            AltarItems.CODEC.optionalFieldOf("items", AltarItems.EMPTY).forGetter(Altar::getItems),
            Identifier.CODEC.optionalFieldOf("material", MATERIAL).forGetter(Altar::getMaterial),
            AltarPose.CODEC.optionalFieldOf("pose", AltarPose.WALKING).forGetter(Altar::getPose),
            Codec.BOOL.optionalFieldOf("pose_locked", false).forGetter(Altar::isPoseLocked)
    ).apply(instance, Altar::new));

    private final boolean enabled;
    private final Optional<String> name;
    private final boolean female;
    private final boolean slim;
    private final int lightLevel;
    private final AltarItems items;
    private final Identifier material;
    private final AltarPose pose;
    private final boolean poseLocked;

    private final Optional<Identifier> deity;

    public Altar(boolean enabled, Optional<String> name, boolean female, boolean slim,
                 int lightLevel, AltarItems items, Identifier material,
                 AltarPose pose, boolean poseLocked) {
        this.enabled = enabled;
        this.name = name;
        this.female = female;
        this.slim = slim;
        this.lightLevel = Math.max(0, Math.min(15, lightLevel));
        this.items = items;
        this.material = material;
        this.pose = pose;
        this.poseLocked = poseLocked;

        if(name.isPresent() && !name.get().isEmpty()) {
            this.deity = Optional.ofNullable(Identifier.tryParse(name.get()));
        } else {
            this.deity = Optional.empty();
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public Optional<Identifier> getDeity() {
        return deity;
    }

    public Optional<String> getName() { return name; }

    public boolean isFemale() {
        return female;
    }

    public boolean isSlim() {
        return slim;
    }

    public int getLightLevel() {
        return lightLevel;
    }

    public AltarItems getItems() {
        return items;
    }

    public Identifier getMaterial() {
        return material;
    }

    public AltarPose getPose() {
        return pose;
    }

    public boolean isPoseLocked() {
        return poseLocked;
    }

    @Override
    public String toString() {
        final StringBuilder b = new StringBuilder("Deity:");
        b.append(" enabled[").append(enabled).append("]");
        b.append(" items[").append(items.toString()).append("]");
        b.append(" female[").append(female).append("]");
        b.append(" slim[").append(slim).append("]");
        b.append(" pose_locked[").append(poseLocked).append("]");
        return b.toString();
    }

    public static String createTranslationKey(final Identifier deity) {
        return "deity." + deity.getNamespace() + "." + deity.getPath();
    }
}
