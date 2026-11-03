package net.diaithir.druidry.ars.setup.registry;

import net.diaithir.druidry.ars.api.spell.SpellCaster;
import net.diaithir.druidry.ars.common.items.data.*;
import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import static net.diaithir.druidry.ars.ArsNouveau.MODID;

public class DataComponentRegistry {

    public static final DeferredRegister<DataComponentType<?>> DATA = DeferredRegister.create(BuiltInRegistries.DATA_COMPONENT_TYPE, MODID);



    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ItemScrollData>> ITEM_SCROLL_DATA = DATA.register("item_scroll_data", () -> DataComponentType.<ItemScrollData>builder().persistent(ItemScrollData.CODEC).networkSynchronized(ItemScrollData.STREAM_CODEC).build());


    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ArmorPerkHolder>> ARMOR_PERKS = DATA.register("armor_perks", () -> DataComponentType.<ArmorPerkHolder>builder().persistent(ArmorPerkHolder.CODEC).build());

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<DominionWandData>> DOMINION_WAND = DATA.register("dominion_wand", () -> DataComponentType.<DominionWandData>builder().persistent(DominionWandData.CODEC).networkSynchronized(DominionWandData.STREAM).build());

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<PresentData>> PRESENT = DATA.register("present", () -> DataComponentType.<PresentData>builder().persistent(PresentData.CODEC).networkSynchronized(PresentData.STREAM_CODEC).build());

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ScryPosData>> SCRY_DATA = DATA.register("scry_data", () -> DataComponentType.<ScryPosData>builder().persistent(ScryPosData.CODEC).build());





    public static final DeferredHolder<DataComponentType<?>, DataComponentType<SpellCaster>> SPELL_CASTER = DATA.register("spell_caster", () -> DataComponentType.<SpellCaster>builder().persistent(SpellCaster.CODEC.codec()).networkSynchronized(SpellCaster.STREAM_CODEC).build());

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ScryCasterData>> SCRY_CASTER = DATA.register("scry_caster", () -> DataComponentType.<ScryCasterData>builder().persistent(ScryCasterData.CODEC.codec()).networkSynchronized(ScryCasterData.STREAM_CODEC).build());


    public static final DeferredHolder<DataComponentType<?>, DataComponentType<BlockFillContents>> BLOCK_FILL_CONTENTS = DATA.register("block_fill_contents", () -> DataComponentType.<BlockFillContents>builder().persistent(BlockFillContents.CODEC).networkSynchronized(BlockFillContents.STREAM_CODEC).build());

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<MobJarData>> MOB_JAR = DATA.register("mob_jar", () -> DataComponentType.<MobJarData>builder().persistent(MobJarData.CODEC).networkSynchronized(MobJarData.STREAM_CODEC).build());




    public static final DeferredHolder<DataComponentType<?>, DataComponentType<PotionJarData>> POTION_JAR = DATA.register("potion_jar", () -> DataComponentType.<PotionJarData>builder().persistent(PotionJarData.CODEC).networkSynchronized(PotionJarData.STREAM).build());

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> UNBREAKING = DATA.register("unbreaking", () -> DataComponentType.<Boolean>builder().persistent(Codec.BOOL).build());

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<PrestidigitationData>> PRESTIDIGITATION = DATA.register("prestidigitation", () -> DataComponentType.<PrestidigitationData>builder().persistent(PrestidigitationData.CODEC).networkSynchronized(PrestidigitationData.STREAM).build());
}
