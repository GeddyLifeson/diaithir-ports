package net.diaithir.tooltips.jade;

import net.diaithir.tooltips.CultureNames;
import net.diaithir.tooltips.DiaithirTooltips;
import net.diaithir.tooltips.ClientHook;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Block;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;
import snownee.jade.api.config.IPluginConfig;

/** Jade: the same culture line under the block's or mob's name. */
@WailaPlugin
public final class JadePlugin implements IWailaPlugin {
    private static final Identifier BLOCK = Identifier.fromNamespaceAndPath(DiaithirTooltips.ID, "block");
    private static final Identifier ENTITY = Identifier.fromNamespaceAndPath(DiaithirTooltips.ID, "entity");

    @Override
    public void registerClient(IWailaClientRegistration reg) {
        reg.addConfig(BLOCK, true);
        reg.addConfig(ENTITY, true);
        reg.registerBlockComponent(new IComponentProvider<BlockAccessor>() {
            @Override
            public void appendTooltip(ITooltip tooltip, BlockAccessor a, IPluginConfig config) {
                String id = BuiltInRegistries.BLOCK.getKey(a.getBlock()).toString();
                String name = CultureNames.name("block", id);
                if (name == null) {
                    name = CultureNames.name("item", id);
                }
                line(tooltip, name);
            }

            @Override
            public Identifier getUid() {
                return BLOCK;
            }
        }, Block.class);
        reg.registerEntityComponent(new IComponentProvider<EntityAccessor>() {
            @Override
            public void appendTooltip(ITooltip tooltip, EntityAccessor a, IPluginConfig config) {
                line(tooltip, CultureNames.name("entity", BuiltInRegistries.ENTITY_TYPE.getKey(a.getEntity().getType()).toString()));
            }

            @Override
            public Identifier getUid() {
                return ENTITY;
            }
        }, Entity.class);
    }

    private static void line(ITooltip tooltip, String name) {
        if (name != null) {
            tooltip.add(Math.min(1, tooltip.size()), Component.literal(name).withStyle(ClientHook.STYLE));
        }
    }
}
