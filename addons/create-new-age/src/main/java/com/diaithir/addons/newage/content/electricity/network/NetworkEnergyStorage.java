package com.diaithir.addons.newage.content.electricity.network;

import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import com.diaithir.addons.newage.content.electricity.connector.AbstractElectricalConnector;
import com.diaithir.addons.newage.esl.EnergyStorage;
import com.diaithir.addons.newage.esl.SnapshotParticipant;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import java.util.Map;

public class NetworkEnergyStorage extends SnapshotParticipant<Object> implements EnergyStorage {
    private final AbstractElectricalConnector connector;
    private ElectricalNetwork network;

    public NetworkEnergyStorage(AbstractElectricalConnector connector, ElectricalNetwork network) {
        this.connector = connector;
        this.network = network;
    }
    
    public ElectricalNetwork getNetwork() {
        return network;
    }
    
    public void setNetwork(ElectricalNetwork network) {
        this.network = network;
    }

    @Override
    public long insert(long maxAmount, TransactionContext txn) {
        if (network == null)
            return 0;

        updateSnapshots(txn);
        
        return network.insert(connector, maxAmount, txn);
    }

    @Override
    public long extract(long maxAmount, TransactionContext txn) {
        return 0;
    }

    @Override
    public long getStoredEnergy() {
        return 0;
    }

    @Override
    public long getCapacity() {
        return Long.MAX_VALUE;
    }

    @Override
    public Object createSnapshot() {
        if (network == null)
            return null;
        
        return new NetworkSnapshot(network);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public void restoreSnapshot(Object object) {
        if (object instanceof NetworkSnapshot snapshot) {
            getNetwork().getPathManager().setConductivityContext(new NetworkPathConductivityContext(snapshot.getContext()));

            for (Map.Entry<AbstractElectricalConnector, Object> e : snapshot.getSnapshots().entrySet()) {
                EnergyStorage storage = EnergyStorage.findForBlock(e.getKey().getLevel(), e.getKey().getSupportingBlockPos(),
                        e.getKey().getBlockState().getValue(BlockStateProperties.FACING));
                
                if (storage instanceof SnapshotParticipant sp) {
                    sp.restoreSnapshot(e.getValue());
                }
            }
        }
    }
}
