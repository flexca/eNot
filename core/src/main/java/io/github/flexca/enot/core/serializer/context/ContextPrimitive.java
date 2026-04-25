package io.github.flexca.enot.core.serializer.context;

public class ContextPrimitive extends ContextNode {

    private Object value;

    public void setValue(Object value) {
        this.value = value;
    }

    @Override
    public Object getValue() {
        return value;
    }
}
