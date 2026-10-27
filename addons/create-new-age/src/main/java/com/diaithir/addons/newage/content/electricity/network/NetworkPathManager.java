package com.diaithir.addons.newage.content.electricity.network;

import com.diaithir.addons.newage.config.CNAConfig;
import com.diaithir.addons.newage.content.electricity.connector.AbstractElectricalConnector;
import com.diaithir.addons.newage.util.HashSortedPair;

import java.util.*;

public class NetworkPathManager {
    private NetworkPathConductivityContext context = new NetworkPathConductivityContext();
    private final Map<HashSortedPair<AbstractElectricalConnector>, NetworkPath> paths = new HashMap<>();

    protected void addConnection(AbstractElectricalConnector node, AbstractElectricalConnector node1) {
        context.addConnection(node, node1);
        paths.clear();
    }

    protected void removeConnection(AbstractElectricalConnector node, AbstractElectricalConnector node1) {
        context.removeConnection(node, node1);
        paths.clear();
    }

    protected NetworkPath findConductiblePath(AbstractElectricalConnector a, AbstractElectricalConnector b) {
        HashSortedPair<AbstractElectricalConnector> key = new HashSortedPair<>(a, b);
        NetworkPath path = paths.get(key);

        if (!paths.containsKey(key)) {
            path = findPath(a, b);
            paths.put(key, path);
        }

        if (path != null && context.calculatePathConductivity(path) > 0)
            return path;

        // Only the shortest route is eligible. If it is saturated this tick,
        // do not fall back to longer alternate paths.
        return null;
    }

    private NetworkPath findPath(AbstractElectricalConnector a, AbstractElectricalConnector b) {
        List<AbstractElectricalConnector> visited = new ArrayList<>();
        Queue<QueueElement> queue = new LinkedList<>();
        queue.add(new QueueElement(a, null, 0));
        visited.add(a);

        while (!queue.isEmpty()) {
            var element = queue.poll();

            if (element.connector.equals(b)) {
                NetworkPath path = unwrapConductiblePath(element);
                if (path != null)
                    return path;
            }

            for (AbstractElectricalConnector connector : element.connector.getConnectedConnectors().keySet()) {
                if (!visited.contains(connector) && element.depth < CNAConfig.getServer().maxPathfindingDepth.get()) {
                    visited.add(connector);
                    queue.add(new QueueElement(connector, element, element.depth + 1));
                }
            }
        }

        return null;
    }

    private NetworkPath unwrapConductiblePath(QueueElement element) {
        NetworkPath path = new NetworkPath();

        while (element != null) {
            path.addNodeToBeginning(element.connector);
            element = element.parent;
        }

        if (path.getLength() < 2)
            return null;

        return path;
    }

    protected NetworkPathConductivityContext getConductivityContext() {
        return context;
    }
    
    protected void setConductivityContext(NetworkPathConductivityContext context) {
        this.context = context;
    }

    protected void tick() {
        context.updateConductivity();
    }

    private record QueueElement(AbstractElectricalConnector connector, QueueElement parent, int depth) { }
}
