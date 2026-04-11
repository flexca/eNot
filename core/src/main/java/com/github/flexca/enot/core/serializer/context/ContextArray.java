package com.github.flexca.enot.core.serializer.context;

import java.util.List;

public class ContextArray extends ContextNode {

    private List<ContextNode> items;

    public void setItems(List<ContextNode> items) {
        this.items = items;
    }

    public List<ContextNode> getItems() {
        return items;
    }

    @Override
    public Object getValue() {
        return items;
    }
}
