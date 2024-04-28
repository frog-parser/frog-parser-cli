package com.frogparser.cli.domain;

import com.frogparser.flow.domain.runtime_variable.AbstractRunTimeVariable;
import com.frogparser.flow.domain.runtime_variable.RunTimeVariableType;
import com.frogparser.flow.executor.domain.FlowExecution;

public class RunTimeVariableDB {

    private Long id;
    private FlowExecution flowExecution;
    private String name;
    private RunTimeVariableType variableType;
    private AbstractRunTimeVariable<?> value;

    public Long getId() {
        return id;
    }

    public RunTimeVariableDB setId(Long id) {
        this.id = id;
        return this;
    }

    public FlowExecution getFlowExecution() {
        return flowExecution;
    }

    public RunTimeVariableDB setFlowExecution(FlowExecution flowExecution) {
        this.flowExecution = flowExecution;
        return this;
    }

    public String getName() {
        return name;
    }

    public RunTimeVariableDB setName(String name) {
        this.name = name;
        return this;
    }

    public RunTimeVariableType getVariableType() {
        return variableType;
    }

    public RunTimeVariableDB setVariableType(RunTimeVariableType variableType) {
        this.variableType = variableType;
        return this;
    }

    public AbstractRunTimeVariable<?> getValue() {
        return value;
    }

    public RunTimeVariableDB setValue(AbstractRunTimeVariable<?> value) {
        this.value = value;
        return this;
    }
}
