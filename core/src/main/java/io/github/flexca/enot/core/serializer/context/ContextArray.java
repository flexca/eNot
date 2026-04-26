package io.github.flexca.enot.core.serializer.context;

import org.apache.commons.collections4.CollectionUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
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

    @Override
    protected void toBytes(OutputStream out) throws IOException {
        if (CollectionUtils.isNotEmpty(items)) {
            out.write(String.valueOf(items.size()).getBytes(StandardCharsets.UTF_8));
            for(ContextNode item : items) {
                out.write(";".getBytes(StandardCharsets.UTF_8));
                item.toBytes(out);
            }
        }
    }
}
