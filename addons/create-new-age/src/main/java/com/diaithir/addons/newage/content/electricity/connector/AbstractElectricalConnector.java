package com.diaithir.addons.newage.content.electricity.connector;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import com.diaithir.addons.newage.content.electricity.network.ElectricalNetwork;
import com.diaithir.addons.newage.content.electricity.wire.WireType;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractElectricalConnector extends SmartBlockEntity {
    protected final Map<AbstractElectricalConnector, WireType> connectors = new HashMap<>();
    protected final Map<BlockPos, WireType> connectorPositions = new HashMap<>();

    protected ElectricalNetwork network;

    protected boolean connectionsInitialized = false;
    boolean needsInstanceUpdate = true;

    public AbstractElectricalConnector(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {}

    @Override
    protected void write(ValueOutput tag, boolean clientPacket) {
        ValueOutput.ValueOutputList list = tag.childrenList("connections");

        for (Map.Entry<BlockPos, WireType> e : connectorPositions.entrySet()) {
            ValueOutput compound = list.addChild();
            compound.store("position", BlockPos.CODEC, e.getKey());
            compound.putString("wire", e.getValue().name());
        }

        super.write(tag, clientPacket);
    }

    @Override
    protected void read(ValueInput tag, boolean clientPacket) {
        connectorPositions.clear();

        tag.childrenListOrEmpty("connections").stream().forEach(ct -> {
            var pos = ct.read("position", BlockPos.CODEC);
            var wire = ct.getString("wire");
            if (pos.isPresent() && wire.isPresent())
                connectorPositions.put(pos.get(), WireType.valueOf(wire.get()));
        });

        needsInstanceUpdate = true;
        super.read(tag, clientPacket);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    public Map<BlockPos, WireType> getConnectorPositions() {
        return Collections.unmodifiableMap(connectorPositions);
    }

    public BlockPos getSupportingBlockPos() {
        return this.getBlockPos();
    }

    public abstract Direction getFacing();

    protected void serverTick() {
        if (network == null)
            setNetwork(new ElectricalNetwork(this));

        if (!connectionsInitialized) {
            updateConnections();
            connectionsInitialized = true;
        }
    }

    public void neighborChanged() {
        if (network != null) {
            network.updateConsumersAndSources();
        }
    }

    private void updateConnections() {
        for (Map.Entry<BlockPos, WireType> e : connectorPositions.entrySet()) {
            if (getLevel().isLoaded(e.getKey())) {
                if (getLevel().getBlockEntity(e.getKey()) instanceof AbstractElectricalConnector connector) {
                    connect(connector, e.getValue());
                }
            }
        }

        needsInstanceUpdate = true;
    }

    public void remove(Level level) {
        if (!level.isClientSide())
            network.destroy();

        for (Map.Entry<AbstractElectricalConnector, WireType> e : connectors.entrySet()) {
            e.getKey().disconnect(this);
            e.getKey().updateConnections();

            e.getKey().setChanged();

            if (level instanceof ServerLevel serverLevel) {
                serverLevel.getChunkSource().blockChanged(e.getKey().getBlockPos());

                Containers.dropContents(level, getBlockPos(), NonNullList.of(ItemStack.EMPTY, e.getValue().getDroppedItem()));
            }
        }
    }

    public void connect(AbstractElectricalConnector entity, WireType wireType) {
        entity.connectWithoutNetworking(this, wireType);
        connectWithoutNetworking(entity, wireType);

        entity.setChanged();
        setChanged();

        if (level instanceof ServerLevel serverLevel) {
            network.addNode(entity);

            serverLevel.getChunkSource().blockChanged(entity.getBlockPos());
            serverLevel.getChunkSource().blockChanged(getBlockPos());
        }
    }

    private void connectWithoutNetworking(AbstractElectricalConnector entity, WireType wireType) {
        if (!connectors.containsKey(entity))
            connectors.put(entity, wireType);

        if (!connectorPositions.containsKey(entity.getBlockPos()))
            connectorPositions.put(entity.getBlockPos(), wireType);
    }

    public void disconnect(AbstractElectricalConnector entity) {
        if (network != null)
            network.removeConnection(this, entity);
        if (entity.getNetwork() != null && entity.getNetwork() != network)
            entity.getNetwork().removeConnection(this, entity);

        connectors.remove(entity);
        connectorPositions.remove(entity.getBlockPos());
    }

    public Map<AbstractElectricalConnector, WireType> getConnectedConnectors() {
        return Collections.unmodifiableMap(connectors);
    }

    public boolean isConnected(BlockPos pos) {
        return connectorPositions.containsKey(pos);
    }

    public void setNetwork(ElectricalNetwork network) {
        this.network = network;
    }

    public ElectricalNetwork getNetwork() {
        return network;
    }

    public Vec3 getConnectionPoint() {
        return new Vec3(0.5f, 0.5f, 0.5f);
    }
}
