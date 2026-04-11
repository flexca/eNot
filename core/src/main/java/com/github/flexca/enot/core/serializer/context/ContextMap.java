package com.github.flexca.enot.core.serializer.context;

import java.util.Map;

public class ContextMap extends ContextNode {

    private Map<String, ContextNode> items;

    public void setItems(Map<String, ContextNode> items) {
        this.items = items;
    }

    public Map<String, ContextNode> getItems() {
        return items;
    }

    public ContextNode get(String name) {
        return items == null ? null : items.get(name);
    }

    @Override
    public Object getValue() {
        return items;
    }
}
