package untamedwilds.init;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.RegisterEvent;
import untamedwilds.UntamedWilds;

@SuppressWarnings("unused")
public class ModSounds {

    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(BuiltInRegistries.SOUND_EVENT, UntamedWilds.MOD_ID);

    public static final DeferredHolder<SoundEvent, SoundEvent> ENTITY_ATTACK_BITE = SOUNDS.register("entity.generic.bite", () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(UntamedWilds.MOD_ID, "entity.generic.bite")));

    public static final DeferredHolder<SoundEvent, SoundEvent> ENTITY_HIPPO_AMBIENT = SOUNDS.register("entity.hippo.ambient", () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(UntamedWilds.MOD_ID, "entity.hippo.ambient")));

    public static final DeferredHolder<SoundEvent, SoundEvent> ENTITY_BISON_AMBIENT = SOUNDS.register("entity.bison.ambient", () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(UntamedWilds.MOD_ID, "entity.bison.ambient")));

    public static final DeferredHolder<SoundEvent, SoundEvent> ENTITY_BEAR_AMBIENT = SOUNDS.register("entity.bear.ambient", () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(UntamedWilds.MOD_ID, "entity.bear.ambient")));
    public static final DeferredHolder<SoundEvent, SoundEvent> ENTITY_BEAR_WARNING = SOUNDS.register("entity.bear.warning", () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(UntamedWilds.MOD_ID, "entity.bear.warning")));
    public static final DeferredHolder<SoundEvent, SoundEvent> ENTITY_BEAR_HURT = SOUNDS.register("entity.bear.hurt", () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(UntamedWilds.MOD_ID, "entity.bear.hurt")));
    public static final DeferredHolder<SoundEvent, SoundEvent> ENTITY_BEAR_DEATH = SOUNDS.register("entity.bear.death", () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(UntamedWilds.MOD_ID, "entity.bear.death")));
    public static final DeferredHolder<SoundEvent, SoundEvent> ENTITY_BEAR_BABY_AMBIENT = SOUNDS.register("entity.bear.baby.ambient", () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(UntamedWilds.MOD_ID, "entity.bear.baby.ambient")));
    public static final DeferredHolder<SoundEvent, SoundEvent> ENTITY_BEAR_BABY_CRY = SOUNDS.register("entity.bear.baby.cry", () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(UntamedWilds.MOD_ID, "entity.bear.baby.cry")));

    public static final DeferredHolder<SoundEvent, SoundEvent> ENTITY_BIG_CAT_AMBIENT = SOUNDS.register("entity.big_cat.ambient", () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(UntamedWilds.MOD_ID, "entity.big_cat.ambient")));
    public static final DeferredHolder<SoundEvent, SoundEvent> ENTITY_BIG_CAT_HURT = SOUNDS.register("entity.big_cat.hurt", () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(UntamedWilds.MOD_ID, "entity.big_cat.hurt")));
    public static final DeferredHolder<SoundEvent, SoundEvent> ENTITY_BIG_CAT_DEATH = SOUNDS.register("entity.big_cat.death", () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(UntamedWilds.MOD_ID, "entity.big_cat.death")));

    public static final DeferredHolder<SoundEvent, SoundEvent> ENTITY_AARDVARK_AMBIENT = SOUNDS.register("entity.aardvark.ambient", () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(UntamedWilds.MOD_ID, "entity.aardvark.ambient")));
    public static final DeferredHolder<SoundEvent, SoundEvent> ENTITY_AARDVARK_HURT = SOUNDS.register("entity.aardvark.hurt", () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(UntamedWilds.MOD_ID, "entity.aardvark.hurt")));
    public static final DeferredHolder<SoundEvent, SoundEvent> ENTITY_AARDVARK_DEATH = SOUNDS.register("entity.aardvark.death", () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(UntamedWilds.MOD_ID, "entity.aardvark.death")));

    public static final DeferredHolder<SoundEvent, SoundEvent> ENTITY_HYENA_LAUGHING = SOUNDS.register("entity.hyena.laugh", () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(UntamedWilds.MOD_ID, "entity.hyena.laugh")));
    public static final DeferredHolder<SoundEvent, SoundEvent> ENTITY_HYENA_GROWL = SOUNDS.register("entity.hyena.growl", () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(UntamedWilds.MOD_ID, "entity.hyena.growl")));
    public static final DeferredHolder<SoundEvent, SoundEvent> ENTITY_HYENA_AMBIENT = SOUNDS.register("entity.hyena.ambient", () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(UntamedWilds.MOD_ID, "entity.hyena.ambient")));
    public static final DeferredHolder<SoundEvent, SoundEvent> ENTITY_HYENA_HURT = SOUNDS.register("entity.hyena.hurt", () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(UntamedWilds.MOD_ID, "entity.hyena.hurt")));
    public static final DeferredHolder<SoundEvent, SoundEvent> ENTITY_HYENA_DEATH = SOUNDS.register("entity.hyena.death", () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(UntamedWilds.MOD_ID, "entity.hyena.death")));

    public static final DeferredHolder<SoundEvent, SoundEvent> ENTITY_BOAR_SQUEAL = SOUNDS.register("entity.boar.squeal", () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(UntamedWilds.MOD_ID, "entity.boar.squeal")));
    public static final DeferredHolder<SoundEvent, SoundEvent> ENTITY_BOAR_AMBIENT = SOUNDS.register("entity.boar.ambient", () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(UntamedWilds.MOD_ID, "entity.boar.ambient")));
    public static final DeferredHolder<SoundEvent, SoundEvent> ENTITY_BOAR_HURT = SOUNDS.register("entity.boar.hurt", () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(UntamedWilds.MOD_ID, "entity.boar.hurt")));
    public static final DeferredHolder<SoundEvent, SoundEvent> ENTITY_BOAR_DEATH = SOUNDS.register("entity.boar.death", () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(UntamedWilds.MOD_ID, "entity.boar.death")));

    public static final DeferredHolder<SoundEvent, SoundEvent> ENTITY_CAMEL_SPIT = SOUNDS.register("entity.camel.spit", () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(UntamedWilds.MOD_ID, "entity.camel.spit")));
    public static final DeferredHolder<SoundEvent, SoundEvent> ENTITY_CAMEL_AMBIENT = SOUNDS.register("entity.camel.ambient", () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(UntamedWilds.MOD_ID, "entity.camel.ambient")));
    public static final DeferredHolder<SoundEvent, SoundEvent> ENTITY_CAMEL_HURT = SOUNDS.register("entity.camel.hurt", () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(UntamedWilds.MOD_ID, "entity.camel.hurt")));
    public static final DeferredHolder<SoundEvent, SoundEvent> ENTITY_CAMEL_DEATH = SOUNDS.register("entity.camel.death", () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(UntamedWilds.MOD_ID, "entity.camel.death")));

    public static final DeferredHolder<SoundEvent, SoundEvent> ENTITY_SPITTER_SPIT = SOUNDS.register("entity.spitter.spit", () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(UntamedWilds.MOD_ID, "entity.spitter.spit")));
    public static final DeferredHolder<SoundEvent, SoundEvent> ENTITY_SPITTER_AMBIENT = SOUNDS.register("entity.spitter.ambient", () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(UntamedWilds.MOD_ID, "entity.spitter.ambient")));
    public static final DeferredHolder<SoundEvent, SoundEvent> ENTITY_SPITTER_HURT = SOUNDS.register("entity.spitter.hurt", () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(UntamedWilds.MOD_ID, "entity.spitter.hurt")));
    public static final DeferredHolder<SoundEvent, SoundEvent> ENTITY_SPITTER_DEATH = SOUNDS.register("entity.spitter.death", () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(UntamedWilds.MOD_ID, "entity.spitter.death")));

    public static final DeferredHolder<SoundEvent, SoundEvent> ENTITY_OPOSSUM_HISS = SOUNDS.register("entity.opossum.hiss", () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(UntamedWilds.MOD_ID, "entity.opossum.hiss")));
    public static final DeferredHolder<SoundEvent, SoundEvent> ENTITY_OPOSSUM_AMBIENT = SOUNDS.register("entity.opossum.ambient", () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(UntamedWilds.MOD_ID, "entity.opossum.ambient")));
    public static final DeferredHolder<SoundEvent, SoundEvent> ENTITY_OPOSSUM_HURT = SOUNDS.register("entity.opossum.hurt", () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(UntamedWilds.MOD_ID, "entity.opossum.hurt")));
    public static final DeferredHolder<SoundEvent, SoundEvent> ENTITY_OPOSSUM_DEATH = SOUNDS.register("entity.opossum.death", () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(UntamedWilds.MOD_ID, "entity.opossum.death")));

    public static final DeferredHolder<SoundEvent, SoundEvent> ENTITY_TARANTULA_AMBIENT = SOUNDS.register("entity.tarantula.ambient", () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(UntamedWilds.MOD_ID, "entity.tarantula.ambient")));

    public static final DeferredHolder<SoundEvent, SoundEvent> ENTITY_SNAKE_HISS = SOUNDS.register("entity.snake.warning", () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(UntamedWilds.MOD_ID, "entity.snake.warning")));
    public static final DeferredHolder<SoundEvent, SoundEvent> ENTITY_SNAKE_RATTLE = SOUNDS.register("entity.snake.rattle", () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(UntamedWilds.MOD_ID, "entity.snake.rattle")));
}
