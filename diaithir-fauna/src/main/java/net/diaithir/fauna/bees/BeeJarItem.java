package net.diaithir.fauna.bees;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.bee.Bee;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

import java.util.function.Consumer;

/** Use on a bee to catch it; use on the ground to let it out; sneak with it empty to read nothing. A full jar goes into an apiary. */
public class BeeJarItem extends Item {
    public BeeJarItem(Properties props) { super(props); }

    /** The bee's genes string, or "" for an empty jar. */
    public static String species(ItemStack jar) {
        CustomData d = jar.get(DataComponents.CUSTOM_DATA);
        if (d == null) return "";
        String g = d.copyTag().getStringOr("Genes", d.copyTag().getStringOr("Species", ""));
        return Genes.valid(g) ? g : "";
    }

    public static ItemStack filled(String species) {
        ItemStack st = new ItemStack(Bees.JAR.get());
        CompoundTag t = new CompoundTag();
        t.putString("Genes", species);
        st.set(DataComponents.CUSTOM_DATA, CustomData.of(t));
        return st;
    }

    @Override
    public Component getName(ItemStack stack) {
        BeeSpecies s = BeeSpecies.byId(Genes.species(species(stack)));
        return s == null ? super.getName(stack) : Component.translatable("item.diaithirfauna.bee_jar.full", Component.translatable("bee.diaithirfauna." + s.id()));
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext ctx, TooltipDisplay display, Consumer<Component> out, net.minecraft.world.item.TooltipFlag flag) {
        String g = species(stack);
        BeeSpecies s = BeeSpecies.byId(Genes.species(g));
        if (s == null) return;
        out.accept(Component.literal("Productivity " + Genes.prod(g) + ", endurance " + Genes.end(g)).withStyle(net.minecraft.ChatFormatting.GRAY));
        if (s.isCross()) out.accept(Component.literal("Bred from " + s.parentA() + " and " + s.parentB()).withStyle(net.minecraft.ChatFormatting.GRAY));
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        if (!species(stack).isEmpty() || !(target instanceof Bee bee) || bee.isBaby()) return InteractionResult.PASS;
        if (player.level().isClientSide()) return InteractionResult.SUCCESS;
        ItemStack jar = filled(Bees.genesOf(bee).isEmpty() ? Genes.of(BeeSpecies.DEFAULT, 0, 0) : Bees.genesOf(bee));
        bee.discard();
        stack.shrink(1);
        if (!player.getInventory().add(jar)) player.drop(jar, false);
        return InteractionResult.CONSUME;
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        ItemStack stack = ctx.getItemInHand();
        String sp = species(stack);
        if (sp.isEmpty()) return InteractionResult.PASS;
        if (ctx.getLevel() instanceof net.minecraft.server.level.ServerLevel level) {
            release(level, sp, ctx.getClickedPos().relative(ctx.getClickedFace()));
            Player p = ctx.getPlayer();
            stack.shrink(1);
            ItemStack empty = new ItemStack(Bees.JAR.get());
            if (p != null && !p.getInventory().add(empty)) p.drop(empty, false);
        }
        return InteractionResult.SUCCESS;
    }

    static Bee release(net.minecraft.server.level.ServerLevel level, String species, net.minecraft.core.BlockPos at) {
        Bee bee = EntityType.BEE.create(level, EntitySpawnReason.TRIGGERED);
        if (bee == null) return null;
        bee.setData(Bees.SPECIES.get(), species);
        bee.setPos(at.getX() + 0.5, at.getY() + 0.2, at.getZ() + 0.5);
        level.addFreshEntity(bee);
        return bee;
    }
}
