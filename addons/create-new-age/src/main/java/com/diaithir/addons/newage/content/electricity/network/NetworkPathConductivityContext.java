package com.diaithir.addons.newage.content.electricity.network;

import net.minecraft.util.Tuple;
import com.diaithir.addons.newage.content.electricity.connector.AbstractElectricalConnector;
import com.diaithir.addons.newage.util.HashSortedPair;

import java.util.HashMap;
import java.util.Map;

public class NetworkPathConductivityContext {
    private final Map<HashSortedPair<AbstractElectricalConnector>, Tuple<Long, Long>> connections;
    private final Map<HashSortedPair<AbstractElectricalConnector>, Long> updatedConnections;
    private long tick = 0;

    public NetworkPathConductivityContext() {
        connections = new HashMap<>();
        updatedConnections = new HashMap<>();
    }

    public NetworkPathConductivityContext(NetworkPathConductivityContext context) {
        connections = new HashMap<>();
        updatedConnections = new HashMap<>(context.updatedConnections);
        tick = context.tick;

        for (Map.Entry<HashSortedPair<AbstractElectricalConnector>, Tuple<Long, Long>> e : context.connections.entrySet())
            connections.put(e.getKey(), new Tuple<>(e.getValue().getA(), e.getValue().getB()));
    }

    public void addConnection(AbstractElectricalConnector node, AbstractElectricalConnector node1) {
        HashSortedPair<AbstractElectricalConnector> key = new HashSortedPair<>(node, node1);
        if (!node.equals(node1) && !connections.containsKey(key)) {
            connections.put(key, new Tuple<>(node.getConnectedConnectors().get(node1).getConductivity(), 0L));
            updatedConnections.put(key, tick);
        }
    }

    public void removeConnection(AbstractElectricalConnector node, AbstractElectricalConnector node1) {
        HashSortedPair<AbstractElectricalConnector> key = new HashSortedPair<>(node, node1);
        connections.remove(key);
        updatedConnections.remove(key);
    }

    protected long calculatePathConductivity(NetworkPath path) {
        AbstractElectricalConnector prevNode = null;
        long conductivity = Long.MAX_VALUE;

        for (AbstractElectricalConnector node : path.getNodes()) {
            if (prevNode == null) {
                prevNode = node;
                continue;
            }

            HashSortedPair<AbstractElectricalConnector> key = new HashSortedPair<>(prevNode, node);

            if (!connections.containsKey(key))
                return 0;

            long connectionConductivity = getConnectionConductivity(key);
            conductivity = Math.min(connectionConductivity, conductivity);
            prevNode = node;
        }

        return conductivity;
    }

    protected void decreasePathConductivity(NetworkPath path, long amount) {
        AbstractElectricalConnector prevNode = null;

        for (AbstractElectricalConnector node : path.getNodes()) {
            if (prevNode == null) {
                prevNode = node;
                continue;
            }

            HashSortedPair<AbstractElectricalConnector> key = new HashSortedPair<>(prevNode, node);
            long connectionConductivity = getConnectionConductivity(key);
            connections.get(key).setB(connectionConductivity - amount);
            prevNode = node;
        }
    }

    protected long getConnectionConductivity(HashSortedPair<AbstractElectricalConnector> key) {
        updateConnection(key);
        return connections.get(key).getB();
    }

    protected void updateConductivity() {
        tick++;
    }

    private void updateConnection(HashSortedPair<AbstractElectricalConnector> key) {
        if (updatedConnections.get(key) == tick)
            return;

        connections.get(key).setB(connections.get(key).getA());
        updatedConnections.put(key, tick);
    }
}
