package rpggods.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import rpggods.RGRegistry;
import rpggods.RPGGods;
import rpggods.deity.Altar;
import rpggods.entity.AltarEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Places a statue or altar. The altar to place is stored in the {@code diaithir_gods:altar} data component.
 */
public class AltarItem extends Item {

    public AltarItem(Properties properties) {
        super(properties);
    }

    /** @return the altar ID stored in the item, or null **/
    public static Identifier getAltarId(ItemStack stack) {
        return stack.get(RGRegistry.ALTAR_ID.get());
    }

    @Override
    public Component getName(ItemStack stack) {
        Identifier altarId = getAltarId(stack);
        // create translation key using altar information
        if (altarId != null) {
            // determine if altar is a deity
            Optional<Altar> altar = Optional.ofNullable(RPGGods.ALTAR_MAP.get(altarId));
            if (altar.isPresent() && altar.get().getDeity().isPresent()) {
                return Component.translatable("item.diaithir_gods.altar_x",
                        Component.translatable(rpggods.deity.Altar.createTranslationKey(altarId)));
            }
        }
        // fallback when no altar information provided
        return Component.translatable(this.getDescriptionId());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
        Identifier altarId = getAltarId(stack);
        if (altarId != null) {
            // determine if altar is not a deity but has a name
            Optional<Altar> altar = Optional.ofNullable(RPGGods.ALTAR_MAP.get(altarId));
            if (altar.isPresent() && altar.get().getDeity().isEmpty() && altar.get().getName().isPresent()) {
                builder.accept(Component.literal(altar.get().getName().get()));
            }
        }
    }

    public static void addAltarItems(List<ItemStack> items) {
        // add altar item stacks for each registered altar
        List<Identifier> altarList = new ArrayList<>(RPGGods.ALTAR_MAP.keySet());
        altarList.sort(Identifier::compareNamespaced);
        for (Identifier altarId : altarList) {
            ItemStack itemStack = new ItemStack(RGRegistry.ALTAR_ITEM.get());
            itemStack.set(RGRegistry.ALTAR_ID.get(), altarId);
            items.add(itemStack);
        }
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Direction direction = context.getClickedFace();
        if (direction == Direction.DOWN) {
            return InteractionResult.FAIL;
        } else {
            Level world = context.getLevel();
            BlockPlaceContext blockitemusecontext = new BlockPlaceContext(context);
            BlockPos blockpos = blockitemusecontext.getClickedPos();
            ItemStack itemstack = context.getItemInHand();
            Vec3 vector3d = Vec3.atBottomCenterOf(blockpos);
            AABB axisalignedbb = RGRegistry.ALTAR_TYPE.get().getDimensions().makeBoundingBox(vector3d.x(), vector3d.y(), vector3d.z());
            if (world.noCollision(null, axisalignedbb) && world.getEntities(null, axisalignedbb).isEmpty()) {
                if (world instanceof ServerLevel serverworld) {
                    // determine altar properties to apply
                    Identifier altarId = getAltarId(itemstack);
                    if (altarId == null) {
                        return InteractionResult.FAIL;
                    }
                    // create altar entity
                    AltarEntity altarEntity = AltarEntity.createAltar(world, blockpos, context.getHorizontalDirection().getOpposite(), altarId);
                    if (altarEntity == null) {
                        return InteractionResult.FAIL;
                    }
                    serverworld.addFreshEntity(altarEntity);
                    world.playSound(null, altarEntity.getX(), altarEntity.getY(), altarEntity.getZ(), SoundEvents.STONE_PLACE, SoundSource.BLOCKS, 0.75F, 0.8F);
                }

                itemstack.shrink(1);
                return InteractionResult.SUCCESS;
            } else {
                return InteractionResult.FAIL;
            }
        }
    }
}
