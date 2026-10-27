package com.diaithir.addons.newage.content.motor.extension;

import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import com.diaithir.addons.newage.content.motor.extension.variants.IMotorExtensionVariant;

import java.util.List;

public class MotorExtensionBlockEntity extends SmartBlockEntity {
    private MotorExtensionScrollValueBehaviour stressBehavior;
    private float multiplier = 1;
    private final IMotorExtensionVariant variant;

    public MotorExtensionBlockEntity(BlockEntityType<?> arg, BlockPos arg2, BlockState arg3, IMotorExtensionVariant variant) {
        super(arg, arg2, arg3);
        this.variant = variant;
    }

    public static com.diaithir.addons.newage.Reg.TileCtor<MotorExtensionBlockEntity> create(IMotorExtensionVariant variant) {
        return (type, pos, state) -> new MotorExtensionBlockEntity(type, pos, state, variant);
    }

    @Override
    public void tick() {
        super.tick();
        
        if (multiplier > variant.getMultiplier()) {
            multiplier = variant.getMultiplier();
            stressBehavior.setValue((int) (multiplier * 100));
        }
        
        stressBehavior.setStep(variant.getScrollStep());
        stressBehavior.betweenValidated(1, (int)(100 * variant.getMultiplier()));
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {
        stressBehavior = new MotorExtensionScrollValueBehaviour(this, 1);
        stressBehavior.setRaw(100);
        stressBehavior.withCallback(i -> {
            multiplier = i/100f;
            stressBehavior.setRaw(i);
            this.notifyUpdate();
        });
        behaviours.add(stressBehavior);
    }
    
    public float getMultiplier() {
        return multiplier;
    }
    
    public IMotorExtensionVariant getVariant() {
        return variant;
    }



    @Override
    protected void read(ValueInput tag, boolean clientPacket) {
        multiplier = tag.getFloatOr("stressMultiplier", 0f);
        if (stressBehavior != null) stressBehavior.setRaw((int)multiplier*100);
        super.read(tag, clientPacket);
    }

    @Override
    protected void write(ValueOutput tag, boolean clientPacket) {
        tag.putFloat("stressMultiplier", multiplier);
        super.write(tag, clientPacket);
    }
}
