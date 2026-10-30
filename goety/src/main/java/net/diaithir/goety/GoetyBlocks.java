package net.diaithir.goety;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DoubleHighBlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Building blocks of Goety, built from goety_spec/blocks.json. */
public final class GoetyBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Goety.MOD_ID);
    public static final DeferredRegister.Items BLOCK_ITEMS = DeferredRegister.createItems(Goety.MOD_ID);
    public static final Map<String, DeferredBlock<Block>> ALL = new LinkedHashMap<>();
    public static final List<DeferredItem<? extends Item>> ITEMS = new ArrayList<>();
    private static final Map<String, BlockSetType> SETS = new LinkedHashMap<>();
    private static final Map<String, WoodType> WOODS = new LinkedHashMap<>();

    private static BlockSetType set(String n) {
        return SETS.computeIfAbsent(n, k -> BlockSetType.register(new BlockSetType("goety_" + k)));
    }

    private static WoodType wood(String n) {
        return WOODS.computeIfAbsent(n, k -> WoodType.register(new WoodType("goety_" + k, set(k))));
    }

    private static Block vanilla(String id) {
        return BuiltInRegistries.BLOCK.getValue(Identifier.withDefaultNamespace(id));
    }

    private static String likeFor(Spec.BlockSpec s) {
        if (!s.like().startsWith("oak_")) return s.like();
        return switch (s.kind()) {
            case "door" -> "oak_door";
            case "trapdoor" -> "oak_trapdoor";
            case "gate" -> "oak_fence_gate";
            case "plate" -> "oak_pressure_plate";
            case "button" -> "oak_button";
            case "fence" -> "oak_fence";
            case "leaves" -> "oak_leaves";
            default -> s.like();
        };
    }

    private static Block make(Spec.BlockSpec s, BlockBehaviour.Properties p) {
        return switch (s.kind()) {
            case "stairs" -> new StairBlock(ALL.get(s.base()).get().defaultBlockState(), p);
            case "slab" -> new SlabBlock(p);
            case "wall" -> new WallBlock(p);
            case "fence" -> new FenceBlock(p);
            case "gate" -> new FenceGateBlock(wood(s.set()), p);
            case "door" -> new DoorBlock(set(s.set()), p);
            case "trapdoor" -> new TrapDoorBlock(set(s.set()), p);
            case "button" -> new ButtonBlock(set(s.set()), 30, p);
            case "plate" -> new PressurePlateBlock(set(s.set()), p);
            case "pillar" -> new RotatedPillarBlock(p);
            case "altar" -> new net.diaithir.goety.rite.AltarBlock(p.noOcclusion());
            case "pedestal" -> new net.diaithir.goety.rite.PedestalBlock(p.noOcclusion());
            case "leaves" -> new TintedParticleLeavesBlock(0.01F, p);
            default -> new Block(p);
        };
    }

    public static void register(IEventBus bus) {
        List<Spec.BlockSpec> pending = new ArrayList<>(Spec.blocks());
        // derived blocks (stairs, slabs, walls) need their base registered first
        while (!pending.isEmpty()) {
            List<Spec.BlockSpec> next = new ArrayList<>();
            for (Spec.BlockSpec s : pending) {
                if (s.base() != null && !ALL.containsKey(s.base())) { next.add(s); continue; }
                DeferredBlock<Block> b = BLOCKS.registerBlock(s.id(), p -> make(s, p), () -> {
                    BlockBehaviour.Properties p = BlockBehaviour.Properties.ofFullCopy(vanilla(likeFor(s)));
                    return switch (s.kind()) {
                        case "leaves" -> p.noOcclusion().isValidSpawn((st, l, pos, e) -> false);
                        default -> p;
                    };
                });
                ALL.put(s.id(), b);
                if (s.kind().equals("door")) {
                    ITEMS.add(BLOCK_ITEMS.registerItem(s.id(), p -> new DoubleHighBlockItem(b.get(), p.useBlockDescriptionPrefix())));
                } else {
                    ITEMS.add(BLOCK_ITEMS.registerSimpleBlockItem(b));
                }
            }
            if (next.size() == pending.size()) throw new IllegalStateException("unresolved block bases: " + next);
            pending = next;
        }
        BLOCKS.register(bus);
        BLOCK_ITEMS.register(bus);
    }

    private GoetyBlocks() {}
}
