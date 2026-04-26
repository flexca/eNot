package io.github.flexca.enot.core.serializer.context;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
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

    @Override
    protected void toBytes(OutputStream out) throws IOException {
        if (items != null) {
            String[] keys = items.keySet().toArray(new String[0]);
            Arrays.sort(keys);
            for(String sortedKey : keys) {
                String keyToWrite = sortedKey == null ? "null" : sortedKey;
                out.write(("key:" + keyToWrite + ":").getBytes(StandardCharsets.UTF_8));
                ContextNode subNode = items.get(sortedKey);
                if (subNode == null) {
                    out.write("null".getBytes(StandardCharsets.UTF_8));
                } else {
                    subNode.toBytes(out);
                }
            }
        }
    }

}
