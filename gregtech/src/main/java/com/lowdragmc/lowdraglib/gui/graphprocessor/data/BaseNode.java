package com.lowdragmc.lowdraglib.gui.graphprocessor.data;

import com.lowdragmc.lowdraglib.LDLib;
import com.lowdragmc.lowdraglib.Platform;
import com.lowdragmc.lowdraglib.gui.editor.ColorPattern;
import com.lowdragmc.lowdraglib.gui.editor.ILDLRegister;
import com.lowdragmc.lowdraglib.gui.editor.configurator.IConfigurable;

import com.lowdragmc.lowdraglib.gui.graphprocessor.annotation.CustomPortBehavior;
import com.lowdragmc.lowdraglib.gui.graphprocessor.annotation.InputPort;
import com.lowdragmc.lowdraglib.gui.graphprocessor.annotation.OutputPort;
import com.lowdragmc.lowdraglib.gui.graphprocessor.data.custom.ICustomPortBehaviorDelegate;
import com.lowdragmc.lowdraglib.syncdata.IPersistedSerializable;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.utils.Position;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

@Getter
public abstract class BaseNode implements IPersistedSerializable, ILDLRegister, IConfigurable {
    /**
     * Name of the node, it will be displayed in the title section
     */
    @Persisted
    @Setter
    protected String displayName = name();
    /**
     * The accent color of the node title
     */
    protected int titleColor = ColorPattern.GRAY.color;
    /**
     * The GUID of the node, which is used to identify the node
     */
    @Persisted
    private String GUID;
    /**
     * The process order of the node in the graph, it should be updated by the graph.
     */
    @Persisted
    protected int computeOrder = -1;
    /**
     * Visualization configs
     */
    @Persisted
    @Setter
    public Position position;
    @Persisted
    @Setter
    public boolean expanded = true;
    @Persisted
    @Setter
    public boolean canBeRemoved = true;
    /**
     * Tell whether the node can be processed.
     * <br>
     * Do not check anything from inputs because this step happens before inputs are sent to the node
     */
    protected boolean canProcess = true;
    protected BaseGraph graph;
    /**
     * Container of input ports
     */
    public final NodePortContainer.NodeInputPortContainer inputPorts = new NodePortContainer.NodeInputPortContainer(this);
    /**
     * Container of output ports
     */
    public final NodePortContainer.NodeOutputPortContainer outputPorts = new NodePortContainer.NodeOutputPortContainer(this);
    /**
     * Triggered after the node was processed
     */
    public Runnable onProcessed;
    /**
     * Triggered after an edge was connected on the node
     */
    public Consumer<PortEdge> onAfterEdgeConnected;
    /**
     * Triggered after an edge was disconnected on the node
     */
    public Consumer<PortEdge> onAfterEdgeDisconnected;
    /**
     * Triggered after a single/list of port(s) is updated, the parameter is the field name
     */
    public Consumer<String> onPortsUpdated;

    private final Map<String, NodeFieldInformation> nodeFields = new LinkedHashMap<>();


    private record PortUpdate(List<String> fieldNames, BaseNode node) {
    }

    // Used in port update algorithm
    private final Stack<PortUpdate> fieldsToUpdate = new Stack<>();
    private final HashSet<PortUpdate> updatedFields = new HashSet<>();

    protected BaseNode() {
        InitializeInOutDatas();
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        var tag = IPersistedSerializable.super.serializeNBT(provider);
        tag.putString("_type", name());
        return tag;
    }

    public static BaseNode createFromTag(HolderLookup.Provider provider, CompoundTag tag) {
        var type = tag.getString("_type");
        var wrapper = AnnotationDetector.REGISTER_GP_NODES.get(type);
        if (wrapper == null) {
            LDLib.LOGGER.error("Cannot find node type: " + type);
            return null;
        }
        var node = wrapper.creator().get();
        node.deserializeNBT(provider, tag);
        return node;
    }

    public BaseNode copy() {
        var newNode = createFromTag(Platform.getFrozenRegistry(), serializeNBT(Platform.getFrozenRegistry()));
        newNode.GUID = null;
        return newNode;
    }

    /**
     * Create a node of a certain type at a certain position
     *
     * @param nodeType type of the node
     * @param position position in the graph in pixels
     */
    @Nullable
    public static <T extends BaseNode> T createFromType(Class<T> nodeType, Position position) {
        if (!BaseNode.class.isAssignableFrom(nodeType))
            return null;
        try {
            var node = nodeType.getConstructor().newInstance();
            node.position = position;
            return node;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Get the minimum width of the node in the editor
     */
    public int getMinWidth() {
        return 50;
    }

    /**
     * Called only when the node is created, not when instantiated
     */
    public void newGuid(BaseGraph graph) {
        GUID = graph.newGUID().toString();
    }

    public void disableInternal() {
        // port containers are initialized in the OnEnable
        inputPorts.clear();
        outputPorts.clear();
        disable();
    }

    public void destroyInternal() {
        destroy();
    }

    public Field[] getAllFields() {
        return getClass().getFields();
    }

    public Method[] getAllMethods() {
        return getClass().getMethods();
    }

    /**
     * Detect input and output fields of the node
     */
    protected void InitializeInOutDatas() {
        var fields = getAllFields();
        var methods = getAllMethods();

        var fieldInformations = new ArrayList<NodeFieldInformation>();
        for (var field : fields) {
            var inputPort = field.isAnnotationPresent(InputPort.class) ? field.getAnnotation(InputPort.class) : null;
            var outputPort = field.isAnnotationPresent(OutputPort.class) ? field.getAnnotation(OutputPort.class) : null;
            var isMultiple = false;
            var input = false;
            var name = field.getName();
            var color = -1;
            var priority = 0;
            String[] tooltips = null;

            if (inputPort == null && outputPort == null) continue;
            if (inputPort != null && outputPort != null) {
                LDLib.LOGGER.error("Field " + field.getName() + " cannot be both input and output");
                continue;
            }

            //check if field is a collection type
            input = inputPort != null;
            isMultiple = input ? inputPort.allowMultiple() : outputPort.allowMultiple();
            tooltips = input ? inputPort.tips() : outputPort.tips();

            if (input) {
                name = inputPort.name().isEmpty() ? name : inputPort.name();
                color = inputPort.color();
                priority = inputPort.priority();
            } else {
                name = outputPort.name().isEmpty() ? name : outputPort.name();
                color = outputPort.color();
                priority = outputPort.priority();
            }

            // By default we set the behavior to null, if the field have a custom behavior, it will be set in the loop just below
            fieldInformations.add(new NodeFieldInformation(field, name, color, input, isMultiple, tooltips, priority, null));
        }

        fieldInformations.sort(Comparator.comparingInt(f -> f.priority));
        fieldInformations.forEach(f -> nodeFields.put(f.info.getName(), f));

        for (var method : methods) {
            if (method.isAnnotationPresent(CustomPortBehavior.class)) {
                var customPortBehavior = method.getAnnotation(CustomPortBehavior.class);
                var fieldName = customPortBehavior.field();
                if (nodeFields.containsKey(fieldName)) {
                    var fieldInfo = nodeFields.get(fieldName);
                    method.setAccessible(true);
                    fieldInfo.behavior = edges -> {
                        try {
                            return (List<PortData>) method.invoke(this, edges);
                        } catch (Exception e) {
                            throw new RuntimeException("Error while invoking custom port behavior", e);
                        }
                    };
                } else {
                    LDLib.LOGGER.error("Invalid field name for custom port behavior: " + method + ", " + customPortBehavior.field());
                }
            }
        }
    }

    public void onEdgeConnected(PortEdge edge) {
        var input = edge.inputNode == this;
        var portCollection = (input) ? inputPorts : outputPorts;
        portCollection.add(edge);
        updateAllPorts();
        if (onAfterEdgeConnected != null)
            onAfterEdgeConnected.accept(edge);
    }

    protected boolean canResetPort(NodePort port) {
        return true;
    }

    public void onEdgeDisconnected(PortEdge edge) {  /* ported stub */ }

    public void onProcess() {
        inputPorts.PullDatas();
        process();
        if (onProcessed != null) {
            onProcessed.run();
        }
        outputPorts.PushDatas();
    }

    /**
     * Reset the node to its default state
     */
    public void resetNode() {
    }

    /**
     * called by the BaseGraph when the node is added to the graph
     */
    public void initialize(BaseGraph graph) {
        this.graph = graph;
        if (GUID == null) {
            GUID = graph.newGUID().toString();
        } else {
            this.graph.addGUID(GUID);
        }
        enable();
        InitializePorts();
    }

    /**
     * Use this function to initialize anything related to ports generation in your node
     * This will allow the node creation menu to correctly recognize ports that can be connected between nodes
     */
    public void InitializePorts() {  /* ported stub */ }

    /**
     * Update all ports of the node
     */
    public boolean updateAllPorts() {
        var changed = false;
        for (var entry : nodeFields.entrySet()) {
            changed |= updatePortsForField(entry.getValue().fieldName);
        }
        return changed;
    }

    /**
     * Update all ports of the node without updating the connected ports.
     * <br>
     * Only use this method when you need to update all the nodes ports in your graph.
     */
    public boolean UpdateAllPortsLocal() {
        var changed = false;
        for (var entry : nodeFields.entrySet()) {
            changed |= updatePortsForFieldLocal(entry.getValue().fieldName);
        }
        return changed;
    }

    public boolean updatePortsForFieldLocal(String fieldName) {
        return updatePortsForFieldLocal(fieldName, true);
    }

    /**
     * Update the ports related to one java property field and all connected nodes in the graph
     */
    public boolean updatePortsForFieldLocal(String fieldName, boolean sendPortUpdatedEvent) { return false; /* ported stub */ }

    private boolean addPortData(List<NodePort> nodePorts, NodeFieldInformation fieldInfo, List<String> finalPorts, String fieldName, PortData portData) { return false; /* ported stub */ }

    protected boolean hasCustomBehavior(NodeFieldInformation info) {
        return info.behavior != null;
    }

    public boolean updatePortsForField(String fieldName) {
        return updatePortsForField(fieldName, true);
    }

    /**
     * Update the ports related to one java property field and all connected nodes in the graph
     */
    public boolean updatePortsForField(String fieldName, boolean sendPortUpdatedEvent) { return false; /* ported stub */ }

    /**
     * Add a port
     */
    public void addPort(boolean input, String fieldName, PortData portData) {
        // Fixup port data info if needed:
        if (portData.displayType == null)
            portData.displayType = nodeFields.get(fieldName).info.getType();

        if (input) {
            try {
                inputPorts.add(new NodePort(this, fieldName, portData));
            } catch (NoSuchFieldException e) {
                LDLib.LOGGER.error("Error while adding input port field:{}, data:{}", fieldName, portData, e);
            }
        }
        else {
            try {
                outputPorts.add(new NodePort(this, fieldName, portData));
            } catch (NoSuchFieldException e) {
                LDLib.LOGGER.error("Error while adding output port field:{}, data:{}", fieldName, portData, e);
            }
        }
    }

    /**
     * Remove a port
     */
    public void removePort(boolean input, NodePort port) {
        if (input)
            inputPorts.remove(port);
        else
            outputPorts.remove(port);
    }

    /**
     * Remove port(s) from field name
     */
    public void removePort(boolean input, String fieldName) {
        if (input)
            inputPorts.removeIf(p -> Objects.equals(p.fieldName, fieldName));
        else
            outputPorts.removeIf(p -> Objects.equals(p.fieldName, fieldName));
    }

    /**
     * Get all the nodes connected to the input ports of this node
     */
    public List<BaseNode> getInputNodes() { return null; /* ported stub */ }

    /**
     * Get all the nodes connected to the output ports of this node
     */
    public List<BaseNode> GetOutputNodes() { return null; /* ported stub */ }

    /**
     * Return a node matching the condition in the dependencies of the node
     */
    public BaseNode findInDependencies(Predicate<BaseNode> condition) {
        Stack<BaseNode> dependencies = new Stack<>();
        dependencies.push(this);
        var depth = 0;
        while (!dependencies.isEmpty()) {
            var node = dependencies.pop();

            // Guard for infinite loop (faster than a HashSet based solution)
            depth++;
            if (depth > 2000)
                break;

            if (condition.test(node))
                return node;

            for (var dep : node.getInputNodes())
                dependencies.push(dep);
        }
        return null;
    }

    /**
     * Get the port from field name and identifier
     */
    @Nullable
    public NodePort	getPort(String fieldName, String identifier) {
        var ports = new ArrayList<>(inputPorts);
        ports.addAll(outputPorts);
        return ports.stream().filter(p -> {
            var bothNull = (identifier == null || identifier.isEmpty()) && (p.portData.identifier == null || p.portData.identifier.isEmpty());
            return Objects.equals(p.fieldName, fieldName) && (bothNull || Objects.equals(identifier, p.portData.identifier));
        }).findFirst().orElse(null);
    }

    @Nullable
    public NodePort	getPort(String fieldName) {
        return getPort(fieldName, null);
    }

    /**
     * Return all the ports of the node
     */
    public List<NodePort> getAllPorts() {
        var ports = new ArrayList<>(inputPorts);
        ports.addAll(outputPorts);
        return ports;
    }

    /**
     * Return all the connected edges of the node
     */
    public List<PortEdge> getAllEdges() { return null; /* ported stub */ }

    /**
     * Is the field an input field
     */
    public boolean isFieldInput(String fieldName){
        return nodeFields.get(fieldName).input;
    }


    /**
     * Called when the node is enabled
     */
    protected void enable() {
    }

    /**
     * Called when the node is disabled
     */
    protected void disable() {
    }

    /**
     * Called when the node is removed
     */
    protected void destroy() {
    }

    /**
     * Override this method to implement custom processing
     */
    protected void process() {
    }

    public static class NodeFieldInformation {
        public String name;
        public String fieldName;
        public Field info;
        public boolean input;
        public int color;
        public boolean isMultiple;
        public String[] tooltips;
        @Nullable
        public ICustomPortBehaviorDelegate behavior;
        public int priority;

        public NodeFieldInformation(Field info, String name, int color, boolean input, boolean isMultiple, String[] tooltips,
                                    int priority, @Nullable ICustomPortBehaviorDelegate behavior) {
            this.input = input;
            this.color = color;
            this.isMultiple = isMultiple;
            this.info = info;
            this.name = name;
            this.fieldName = info.getName();
            this.behavior = behavior;
            this.tooltips = tooltips;
            this.priority = priority;
        }
    }
}
