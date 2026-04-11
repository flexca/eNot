package com.github.flexca.enot.core.serializer.context;

public abstract class ContextNode {

    private ContextNode parent;

    public abstract Object getValue();

    public void setParent(ContextNode parent) {
        this.parent = parent;
    }

    public ContextNode getParent() {
        return parent;
    }
}
