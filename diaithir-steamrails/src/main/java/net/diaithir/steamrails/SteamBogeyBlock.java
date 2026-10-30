package net.diaithir.steamrails;

import com.mojang.serialization.MapCodec;
import com.zurrtum.create.AllBogeyStyles;
import com.zurrtum.create.AllTrackMaterials;
import com.zurrtum.create.content.trains.bogey.AbstractBogeyBlock;
import com.zurrtum.create.content.trains.bogey.BogeySize;
import com.zurrtum.create.content.trains.bogey.BogeyStyle;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.phys.Vec3;

/**
 * A bogey that rides Create's standard track. Create builds a bogey with a railway casing on a station's assembly track and a wrench cycles its
 * style; ours join the standard cycle group, so a wrench turns a plain bogey into a driver or pony bogey. The style (and so the look and sound)
 * is chosen by the block: the wheel spacing and radius below are what the train physics use.
 */
public final class SteamBogeyBlock extends AbstractBogeyBlock<SteamBogeyBlockEntity> {
    public static final MapCodec<SteamBogeyBlock> CODEC = simpleCodec(p -> new SteamBogeyBlock(p, com.zurrtum.create.content.trains.bogey.AllBogeySizes.SMALL, "pony", 2.0, 6.0));

    private final Identifier styleId;
    private final double spacing;
    private final double radiusPx;

    public SteamBogeyBlock(Properties properties, BogeySize size, String style, double wheelSpacing, double radiusPx) {
        super(properties, size);
        this.styleId = Identifier.fromNamespaceAndPath(SteamRails.ID, style);
        this.spacing = wheelSpacing;
        this.radiusPx = radiusPx;
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    public Identifier getTrackType(BogeyStyle style) {
        return AllTrackMaterials.ANDESITE.getId();
    }

    @Override
    public double getWheelPointSpacing() {
        return spacing;
    }

    @Override
    public double getWheelRadius() {
        return radiusPx / 16.0;
    }

    @Override
    protected Vec3 getConnectorAnchorOffset() {
        return new Vec3(0.0, 0.21875, 1.0);
    }

    @Override
    public BogeyStyle getDefaultStyle() {
        return AllBogeyStyles.BOGEY_STYLES.get(styleId);
    }

    @Override
    public Class<SteamBogeyBlockEntity> getBlockEntityClass() {
        return SteamBogeyBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends SteamBogeyBlockEntity> getBlockEntityType() {
        return SteamBlocks.BOGEY_BE.get();
    }
}
