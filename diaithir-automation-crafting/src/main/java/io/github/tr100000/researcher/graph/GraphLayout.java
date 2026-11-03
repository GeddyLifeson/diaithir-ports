package io.github.tr100000.researcher.graph;

import io.github.tr100000.researcher.Research;
import it.unimi.dsi.fastutil.ints.Int2ObjectFunction;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jspecify.annotations.Nullable;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class GraphLayout {
    private final ResearchGraph graph;
    private int nodeCount = 0;

    public GraphLayout(ResearchGraph graph) {
        this.graph = graph;
    }

    // Create research->depth map
    public RenderedGraph render(Settings settings) {
        Map<Research, Integer> nodeLayerMap = createNodeLayerMap();
        return render(nodeLayerMap, settings);
    }

    public Map<Research, Integer> createNodeLayerMap() {
        Map<Research, Integer> layerMap = new Object2IntArrayMap<>();
        for (Research node : graph.nodes()) {
            layerMap.put(node, 0);
        }

        for (Research u : graph.getTopologicalOrder()) {
            int base = layerMap.get(u);
            for (Research v : graph.successors(u)) {
                layerMap.put(v, Math.max(layerMap.get(v), base + 1));
            }
        }

        return layerMap;
    }

    // Create research->node map
    public RenderedGraph render(Map<Research, Integer> nodeLayerMap, Settings settings) {
        Map<Research, Node> researchNodeMap = new Object2ObjectOpenHashMap<>();
        Node centeredNode = null;
        for (Map.Entry<Research, Integer> entry : nodeLayerMap.entrySet()) {
            Research r = entry.getKey();
            int layer = entry.getValue();
            if (r == settings.centered()) {
                centeredNode = new Node(r, layer, settings.centeredNodeSize(), settings.centeredNodeSize(), nodeCount++);
                researchNodeMap.put(r, centeredNode);
            }
            else {
                researchNodeMap.put(r, new Node(r, layer, settings.nodeSize(), settings.nodeSize(), nodeCount++));
            }
        }

        return render(centeredNode, nodeLayerMap, researchNodeMap, settings);
    }

    // Collect the nodes into layers and edges
    public RenderedGraph render(@Nullable Node centeredNode, Map<Research, Integer> nodeLayerMap, Map<Research, Node> researchNodeMap, Settings settings) {
        AggregateGraph aggregateGraph = aggregateLayers(nodeLayerMap, researchNodeMap, layer -> new Node(null, layer, settings.dummyNodeSize(), settings.dummyNodeSize(), nodeCount++));
        return render(centeredNode, aggregateGraph, settings);
    }

    public AggregateGraph aggregateLayers(Map<Research, Integer> nodeLayerMap, Map<Research, Node> researchNodeMap, Int2ObjectFunction<Node> dummyNodeFactory) {
        Map<Integer, List<Node>> byLayer = nodeLayerMap.entrySet().stream()
                .collect(Collectors.groupingBy(
                        Map.Entry::getValue,
                        Collectors.mapping(entry -> researchNodeMap.get(entry.getKey()), Collectors.toList())
                ));

        List<List<Node>> layers = new ObjectArrayList<>();
        byLayer.keySet().stream()
                .sorted()
                .forEachOrdered(layer -> layers.add(byLayer.get(layer)));

        int minLayer = nodeLayerMap.values().stream()
                .mapToInt(i -> i)
                .min()
                .orElse(0);

        List<Edge> edges = new ObjectArrayList<>();
        List<RealEdge> realEdges = new ObjectArrayList<>();
        for (Map.Entry<Research, Integer> entry : nodeLayerMap.entrySet()) {
            Research from = entry.getKey();
            int fromLayer = entry.getValue();
            Node fromNode = researchNodeMap.get(from);

            for (Research to : graph.successors(from)) {
                if (!nodeLayerMap.containsKey(to)) continue;
                int toLayer = nodeLayerMap.get(to);
                Node toNode = researchNodeMap.get(to);

                List<Edge> segments = new ObjectArrayList<>();
                RealEdge realEdge = new RealEdge(fromNode, toNode, new ObjectArrayList<>());
                if (toLayer - fromLayer > 1) {
                    Node lastDummyNode = fromNode;
                    for (int layer = fromLayer + 1; layer < toLayer; layer++) {
                        Node dummyNode = dummyNodeFactory.apply(layer);
                        layers.get(layer - minLayer).add(dummyNode);
                        segments.add(new Edge(lastDummyNode, dummyNode, realEdge));
                        lastDummyNode = dummyNode;
                    }
                    segments.add(new Edge(lastDummyNode, toNode, realEdge));
                }
                else {
                    segments.add(new Edge(fromNode, toNode, realEdge));
                }
                edges.addAll(segments);
                realEdge.segments().addAll(segments);
                realEdges.add(realEdge);
            }
        }

        return new AggregateGraph(layers, edges, realEdges);
    }

    public void reorderNodes(AggregateGraph aggregateGraph, Settings settings) {
        Map<Node, List<Node>> outgoing = new Object2ObjectOpenHashMap<>();
        Map<Node, List<Node>> incoming = new Object2ObjectOpenHashMap<>();

        for (List<Node> layer : aggregateGraph.layers()) {
            for (Node node : layer) {
                outgoing.put(node, new ObjectArrayList<>());
                incoming.put(node, new ObjectArrayList<>());
            }
        }

        for (Edge e : aggregateGraph.edges()) {
            outgoing.get(e.from()).add(e.to());
            incoming.get(e.to()).add(e.from());
        }

        for (int n = 0; n < settings.reorderIterations(); n++) {
            reorderDown(aggregateGraph.layers(), incoming, settings);
            reorderUp(aggregateGraph.layers(), outgoing, settings);
        }
    }

    private void reorderDown(List<List<Node>> layers, Map<Node, List<Node>> incoming, Settings settings) {
        Map<Node, Integer> previousLayerPositions = new Object2ObjectOpenHashMap<>();
        for (int i = 0; i < layers.size(); i++) {
            List<Node> currentLayer = layers.get(i);
            if (i > 0) {
                Map<Node, Double> scores = new Object2DoubleOpenHashMap<>();
                for (Node node : currentLayer) {
                    List<Node> parents = incoming.get(node);
                    double average = parents.stream()
                            .mapToInt(previousLayerPositions::get)
                            .average().orElse(Double.NaN);
                    scores.put(node, average);
                }

                currentLayer.sort(Comparator.comparingDouble(scores::get));
            }

            previousLayerPositions.clear();
            int x = 0;
            for (Node node : currentLayer) {
                previousLayerPositions.put(node, x);
                x += node.width();
                x += settings.nodeSpacing;
            }
        }
    }

    private void reorderUp(List<List<Node>> layers, Map<Node, List<Node>> outgoing, Settings settings) {
        Map<Node, Integer> previousLayerPositions = new Object2ObjectOpenHashMap<>();
        for (int i = layers.size() - 1; i >= 0; i--) {
            List<Node> currentLayer = layers.get(i);
            if (i < layers.size() - 1) {
                Map<Node, Double> scores = new Object2DoubleOpenHashMap<>();
                for (Node node : currentLayer) {
                    List<Node> parents = outgoing.get(node);
                    double average = parents.stream()
                            .mapToInt(previousLayerPositions::get)
                            .average().orElse(Double.NaN);
                    scores.put(node, average);
                }

                currentLayer.sort(Comparator.comparingDouble(scores::get));
            }

            previousLayerPositions.clear();
            int x = 0;
            for (Node node : currentLayer) {
                previousLayerPositions.put(node, x);
                x += node.width();
                x += settings.nodeSpacing;
            }
        }
    }

    private int getEdgeAttachOffset(Node node, Edge edge, @Nullable List<Edge> edges) {
        if (edges == null) return 0;
        return (int)(-node.width / 2.0F + (node.width() / (edges.size() + 1.0F)) * (edges.indexOf(edge) + 1));
    }

    public RenderedGraph render(@Nullable Node centeredNode, AggregateGraph aggregateGraph, Settings settings) {
        reorderNodes(aggregateGraph, settings);

        List<RenderedNode> renderedNodes = new ObjectArrayList<>();
        List<RenderedNode> allRenderedNodes = new ObjectArrayList<>();
        List<RenderedEdge> renderedEdges = new ObjectArrayList<>();

        List<Integer> layerHeights = new IntArrayList(aggregateGraph.layers().size());
        Map<Node, Integer> nodeHorizontalPositions = new Object2IntOpenHashMap<>();
        for (List<Node> layer : aggregateGraph.layers()) {
            int layerWidth = layer.stream().mapToInt(Node::width).sum() + settings.nodeSpacing() * (layer.size() - 1);
            int x = -layerWidth / 2;
            for (Node node : layer) {
                x += node.width() / 2;
                nodeHorizontalPositions.put(node, x);
                x += node.width() / 2 + settings.nodeSpacing();
            }

            int layerHeight = layer.stream().mapToInt(Node::height).max().orElse(0);
            layerHeights.add(layerHeight);
        }

        Map<Node, List<Edge>> nodeIncomingEdges = new Object2ObjectOpenHashMap<>();
        Map<Node, List<Edge>> nodeOutgoingEdges = new Object2ObjectOpenHashMap<>();
        for (List<Node> layer : aggregateGraph.layers()) {
            for (Node node : layer) {
                if (node.isDummy()) continue;
                nodeIncomingEdges.put(node, new ObjectArrayList<>());
                nodeOutgoingEdges.put(node, new ObjectArrayList<>());
            }
        }
        for (RealEdge edge : aggregateGraph.realEdges()) {
            nodeOutgoingEdges.get(edge.from()).add(edge.segments().getFirst());
            nodeIncomingEdges.get(edge.to()).add(edge.segments().getLast());
        }
        for (List<Node> layer : aggregateGraph.layers()) {
            for (Node node : layer) {
                if (node.isDummy()) continue;
                nodeIncomingEdges.get(node).sort(Comparator.comparingInt(edge -> nodeHorizontalPositions.get(edge.from())));
                nodeOutgoingEdges.get(node).sort(Comparator.comparingInt(edge -> nodeHorizontalPositions.get(edge.to())));
            }
        }

        Map<Node, List<Edge>> outgoingByNode = new Object2ObjectOpenHashMap<>();
        for (Edge e : aggregateGraph.edges()) {
            outgoingByNode.computeIfAbsent(e.from, _ -> new ObjectArrayList<>()).add(e);
        }

        Map<Node, RenderedNode> nodeRenderedMap = new Object2ObjectOpenHashMap<>();
        Map<Edge, RenderedEdgeSegment> edgeRenderedMap = new Object2ObjectOpenHashMap<>();
        int y = 0;
        List<List<Node>> layers = aggregateGraph.layers();
        for (int i = 0; i < layers.size(); i++) {
            int layerHeight = layerHeights.get(i);
            y += layerHeight / 2;

            List<Node> layer = layers.get(i);

            List<Edge> edgesFromLayer = new ObjectArrayList<>();
            for (Node node : layer) {
                List<Edge> outgoing = outgoingByNode.getOrDefault(node, Collections.emptyList());
                edgesFromLayer.addAll(outgoing);
            }
            edgesFromLayer.sort(Comparator.comparingInt(e -> nodeHorizontalPositions.get(e.from())));

            int maxY = y + settings.edgeBusMargin() * 2;
            EdgeBusTracker edgeBus = new EdgeBusTracker();
            List<RenderedEdgeSegment> tentativeEdges = new ObjectArrayList<>();

            for (Edge edge : edgesFromLayer) {
                Node from = edge.from();
                Node to = edge.to();

                int fromX = nodeHorizontalPositions.get(from) + getEdgeAttachOffset(from, edge, nodeOutgoingEdges.get(from));
                int toX = nodeHorizontalPositions.get(to) + getEdgeAttachOffset(to, edge, nodeIncomingEdges.get(to));

                IntRange range = IntRange.of(fromX, toX);
                int midY = y + layerHeight / 2 + settings.edgeBusMargin();
                while (edgeBus.isOccupied(midY, range, settings.dummyNodeSize())) {
                    midY += settings.edgeBusSpacing();
                }
                edgeBus.occupy(midY, range);

                maxY = Math.max(maxY, midY + settings.edgeBusMargin() + settings.dummyNodeSize());
                tentativeEdges.add(new RenderedEdgeSegment(edge, fromX, -1, midY, toX, -1));
            }

            for (Node node : layer) {
                RenderedNode rendered = new RenderedNode(node, nodeHorizontalPositions.get(node), y);
                nodeRenderedMap.put(node, rendered);
                allRenderedNodes.add(rendered);
                if (!node.isDummy()) renderedNodes.add(rendered);
            }

            for (RenderedEdgeSegment edge : tentativeEdges) {
                Node from = edge.original().from();
                Node to = edge.original().to();
                int fromHeight = from.isDummy() ? 0 : from.height();
                int toHeight = to.isDummy() ? 0 : to.height();

                int fromY = y + fromHeight / 2 - 1;
                int toY = maxY + (layerHeights.get(i + 1) - toHeight) / 2;
                RenderedEdgeSegment rendered = new RenderedEdgeSegment(edge.original(), edge.fromX(), fromY, edge.midY(), edge.toX(), toY);
                edgeRenderedMap.put(edge.original(), rendered);
            }

            y = maxY;
        }

        for (RealEdge edge : aggregateGraph.realEdges()) {
            List<RenderedEdgeSegment> renderedSegments = new ObjectArrayList<>();
            for (Edge segment : edge.segments()) {
                renderedSegments.add(edgeRenderedMap.get(segment));
            }
            renderedEdges.add(new RenderedEdge(edge, renderedSegments, nodeRenderedMap.get(edge.from()), nodeRenderedMap.get(edge.to())));
        }

        Optional<RenderedNode> centered = renderedNodes.stream().filter(n -> n.original() == centeredNode).findAny();

        return new RenderedGraph(
                centered.orElse(null),
                renderedNodes,
                allRenderedNodes,
                renderedEdges
        );
    }

    public record Settings(@Nullable Research centered, int nodeSize, int centeredNodeSize, int dummyNodeSize, int nodeSpacing, int edgeBusMargin, int edgeBusSpacing, int reorderIterations) {}

    public record Node(@Nullable Research research, int layer, int width, int height, int id) {
        public boolean isDummy() {
            return research == null;
        }
    }

    public record AggregateGraph(List<List<Node>> layers, List<Edge> edges, List<RealEdge> realEdges) {}

    // Implement equals and hashCode to prevent infinite loops
    public record Edge(Node from, Node to, RealEdge realEdge) {
        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Edge edge)) return false;

            return to().equals(edge.to()) && from().equals(edge.from());
        }

        @Override
        public int hashCode() {
            int result = from().hashCode();
            result = 31 * result + to().hashCode();
            return result;
        }
    }

    public record RealEdge(Node from, Node to, List<Edge> segments) {}

    public record RenderedGraph(@Nullable RenderedNode centeredNode, List<RenderedNode> nodes, List<RenderedNode> allNodes, List<RenderedEdge> edges) {
        public static final RenderedGraph EMPTY = new RenderedGraph(null, Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
    }

    public record RenderedNode(Node original, int x, int y) {
        public @Nullable Research research() {
            return original.research();
        }

        public Research researchOrThrow() {
            return Objects.requireNonNull(research());
        }

        public int layer() {
            return original.layer();
        }

        public int width() {
            return original.width();
        }

        public int height() {
            return original.height();
        }
    }

    public record RenderedEdge(RealEdge original, List<RenderedEdgeSegment> segments, RenderedNode from, RenderedNode to) {
        public boolean matches(Research from, Research to) {
            return this.from.research() == from && this.to.research() == to;
        }
    }

    public record RenderedEdgeSegment(Edge original, int fromX, int fromY, int midY, int toX, int toY) {}

    record EdgeBusTracker(Map<Integer, List<IntRange>> occupied) {
        EdgeBusTracker() {
            this(new Int2ObjectOpenHashMap<>());
        }

        void occupy(int y, IntRange range) {
            if (range.min() == range.max()) return;
            occupied.computeIfAbsent(y, _ -> new ObjectArrayList<>()).add(range);
        }

        boolean isOccupied(int y, IntRange range, int margin) {
            for (IntRange occupiedRange : occupied.getOrDefault(y, Collections.emptyList())) {
                if (range.max() > occupiedRange.min() - margin && range.min() < occupiedRange.max() + margin)
                    return true;
            }

            return false;
        }
    }

    record IntRange(int min, int max) {
        static IntRange of(int a, int b) {
            return new IntRange(Math.min(a, b), Math.max(a, b));
        }
    }
}
