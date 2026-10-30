package rpggods.client.entity;

import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.resources.Identifier;
import rpggods.util.altar.AltarPose;

/** Data needed to draw an altar statue, copied from the entity once per frame **/
public class AltarRenderState extends HumanoidRenderState {
    public AltarPose pose = AltarPose.EMPTY;
    public boolean female;
    public boolean slim;
    public boolean hasBlock;
    public final ItemStackRenderState blockItem = new ItemStackRenderState();
    public Identifier texture;
}
