package cn.leolezury.eternalstarlight.common.block;

import com.mojang.serialization.MapCodec;

public class EquipableCarvedLunarisCactusFruitBlock extends CarvedLunarisCactusFruitBlock {
	public static final MapCodec<EquipableCarvedLunarisCactusFruitBlock> CODEC = simpleCodec(EquipableCarvedLunarisCactusFruitBlock::new);

	@Override
	public MapCodec<EquipableCarvedLunarisCactusFruitBlock> codec() {
		return CODEC;
	}

	public EquipableCarvedLunarisCactusFruitBlock(Properties properties) {
		super(properties);
	}
}
