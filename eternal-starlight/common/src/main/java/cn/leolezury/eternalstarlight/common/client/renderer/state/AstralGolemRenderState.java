package cn.leolezury.eternalstarlight.common.client.renderer.state;

import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.resources.Identifier;

public class AstralGolemRenderState extends HumanoidRenderState {
	public Identifier texture;
	public int tintColor = -1;
	public float armXRot;
	public boolean blocking;
	public boolean mainHandEmpty = true;
}
