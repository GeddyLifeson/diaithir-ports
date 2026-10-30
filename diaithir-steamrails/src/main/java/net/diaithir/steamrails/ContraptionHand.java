package net.diaithir.steamrails;

import com.zurrtum.create.api.behaviour.interaction.MovingInteractionBehaviour;
import com.zurrtum.create.content.contraptions.AbstractContraptionEntity;
import com.zurrtum.create.content.contraptions.behaviour.MovementContext;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import org.apache.commons.lang3.tuple.MutablePair;

/** Lets a player pour water, load fuel or read the gauges on a boiler or tender while its carriage is a moving train. */
public final class ContraptionHand {
    private ContraptionHand() {}

    public static final class Boiler extends MovingInteractionBehaviour {
        @Override
        public boolean handlePlayerInteraction(Player player, InteractionHand hand, BlockPos localPos, AbstractContraptionEntity entity) {
            if (entity.level().isClientSide()) return true;
            for (MutablePair<StructureBlockInfo, MovementContext> pair : entity.getContraption().getActors()) {
                if (!pair.left.pos().equals(localPos)) continue;
                MovementContext ctx = pair.right;
                BoilerMovement.Run run = BoilerMovement.run(ctx);
                if (run == null) return false;
                Layout l = run.layout != null ? run.layout : Layout.scan(BoilerMovement.view(entity.getContraption()), localPos);
                if (l == null) return false;
                if (Hand.boiler(entity.level(), player, hand, run.st, l.waterCapacity())) BoilerMovement.save(ctx, run);
                else player.sendOverlayMessage(Hand.boilerStatus(run.st, l));
                return true;
            }
            return false;
        }
    }

    public static final class Tender extends MovingInteractionBehaviour {
        @Override
        public boolean handlePlayerInteraction(Player player, InteractionHand hand, BlockPos localPos, AbstractContraptionEntity entity) {
            if (entity.level().isClientSide()) return true;
            StructureBlockInfo info = entity.getContraption().getBlocks().get(localPos);
            if (info == null) return false;
            CompoundTag nbt = info.nbt();
            if (nbt == null) return false;
            var ops = BoilerMovement.ops(entity.level());
            TenderState ts = nbt.read(TenderBlockEntity.KEY, TenderState.CODEC, ops).orElseGet(TenderState::new);
            if (Hand.tender(entity.level(), player, hand, ts)) nbt.store(TenderBlockEntity.KEY, TenderState.CODEC, ops, ts);
            else player.sendOverlayMessage(Hand.tenderStatus(ts));
            return true;
        }
    }
}
