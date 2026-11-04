package net.diaithir.druidry.ars.common.items.data;

import net.diaithir.druidry.ars.api.mana.IManaCap;
import net.diaithir.druidry.ars.api.spell.*;
import net.diaithir.druidry.ars.common.network.Networking;
import net.diaithir.druidry.ars.common.network.NotEnoughManaPacket;
import net.diaithir.druidry.ars.common.util.PortUtil;
import net.diaithir.druidry.ars.setup.registry.CapabilityRegistry;
import net.diaithir.druidry.ars.setup.registry.DataComponentRegistry;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class TomeCasterData extends AbstractCaster<TomeCasterData> {
    public static final MapCodec<TomeCasterData> CODEC = SpellCaster.createCodec(TomeCasterData::new);

    public static final StreamCodec<RegistryFriendlyByteBuf, TomeCasterData> STREAM_CODEC = createStream(TomeCasterData::new);


    @Override
    public MapCodec<TomeCasterData> codec() {
        return CODEC;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, TomeCasterData> streamCodec() {
        return STREAM_CODEC;
    }

    public TomeCasterData() {
        super();
    }

    public TomeCasterData(Integer slot, String flavorText, Boolean isHidden, String hiddenText, int maxSlots) {
        super(slot, flavorText, isHidden, hiddenText, maxSlots);
    }

    public TomeCasterData(Integer slot, String flavorText, Boolean isHidden, String hiddenText, int maxSlots, SpellSlotMap spells) {
        super(slot, flavorText, isHidden, hiddenText, maxSlots, spells);
    }

    @Override
    public SpellResolver getSpellResolver(SpellContext context, Level worldIn, LivingEntity playerIn, InteractionHand handIn) {
        return new SpellResolver(context) {
            @Override
            protected boolean enoughMana(LivingEntity entity) {
                int totalCost = getResolveCost();
                IManaCap manaCap = CapabilityRegistry.getMana(entity);
                if (manaCap == null)
                    return false;
                boolean canCast = totalCost <= manaCap.getCurrentMana() || manaCap.getCurrentMana() == manaCap.getMaxMana() || (entity instanceof Player player && player.isCreative());
                if (!canCast && !entity.level().isClientSide() && !silent) {
                    PortUtil.sendMessageNoSpam(entity, Component.translatable("ars_nouveau.spell.no_mana"));
                    if (entity instanceof ServerPlayer serverPlayer)
                        Networking.sendToPlayerClient(new NotEnoughManaPacket(totalCost), serverPlayer);
                }
                return canCast;
            }

        };
    }

    @Override
    public DataComponentType<TomeCasterData> getComponentType() {
        return DataComponentRegistry.TOME_CASTER.get();
    }

    @Override
    protected TomeCasterData build(int slot, String flavorText, Boolean isHidden, String hiddenText, int maxSlots, SpellSlotMap spells) {
        return new TomeCasterData(slot, flavorText, isHidden, hiddenText, maxSlots, spells);
    }
}
