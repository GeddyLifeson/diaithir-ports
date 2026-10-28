package com.lowdragmc.lowdraglib.gui.graphprocessor.data.parameter;

import com.lowdragmc.lowdraglib.LDLib;
import com.lowdragmc.lowdraglib.gui.editor.annotation.LDLRegister;
import com.lowdragmc.lowdraglib.gui.graphprocessor.annotation.CustomPortBehavior;
import com.lowdragmc.lowdraglib.gui.graphprocessor.annotation.InputPort;
import com.lowdragmc.lowdraglib.gui.graphprocessor.annotation.OutputPort;
import com.lowdragmc.lowdraglib.gui.graphprocessor.data.BaseNode;
import com.lowdragmc.lowdraglib.gui.graphprocessor.data.PortData;
import com.lowdragmc.lowdraglib.gui.graphprocessor.data.PortEdge;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

@LDLRegister(name = "parameter", group = "graph_processor.node.parameter")
public class ParameterNode extends BaseNode {

    @InputPort
    public Object input;
    @OutputPort
    public Object output;
    /**
     * We serialize the GUID of the exposed parameter in the graph so we can retrieve the true ExposedParameter from the graph
     */
    @Persisted
    public String parameterIdentifier;
    @Nullable
    public ExposedParameter<?> parameter;

    public ParameterNode() {
        expanded = false;
    }

    @Override
    public void setExpanded(boolean expanded) {  /* ported stub */ }

    @Override
    public String getDisplayName() { return null; /* ported stub */ }

    @Override
    public void setDisplayName(String displayName) {  /* ported stub */ }

    @Override
    protected void enable() {  /* ported stub */ }

    @CustomPortBehavior(field = "output")
    public List<PortData> getOutputPort(List<PortEdge> edges) {
        if (parameter != null && parameter.getAccessor() == ExposedParameter.ParameterAccessor.Get) {
            return List.of(new PortData()
                    .identifier("output")
                    .displayName("Value")
                    .displayType(parameter.type)
                    .tooltip(parameter.getTips())
                    .acceptMultipleEdges(true));
        }
        return Collections.emptyList();
    }

    @CustomPortBehavior(field = "input")
    public List<PortData> getInputPort(List<PortEdge> edges) {
        if (parameter != null && parameter.getAccessor() == ExposedParameter.ParameterAccessor.Set) {
            return List.of(new PortData()
                    .identifier("input")
                    .displayName("Value")
                    .displayType(parameter.type)
                    .tooltip(parameter.getTips()));
        }
        return Collections.emptyList();
    }

    @Override
    protected void process() {  /* ported stub */ }

}


