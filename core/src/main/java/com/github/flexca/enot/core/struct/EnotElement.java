package com.github.flexca.enot.core.struct;

import com.github.flexca.enot.core.struct.attribute.EnotAttribute;
import lombok.Data;

import java.util.*;

/**
 * Basic brick of eNot - EnotElement.
 */
@Data
public class EnotElement {

    private String type;

    private Map<EnotAttribute, Object> attributes;

    private Object body;

    public EnotElement copy() {

        EnotElement clone = new EnotElement();
        clone.setType(type);
        Map<EnotAttribute, Object> cloneAttributes = new HashMap<>(attributes);
        clone.setAttributes(cloneAttributes);
        if (body instanceof EnotElement elementBody) {
            EnotElement cloneBody = elementBody.copy();
            clone.setBody(cloneBody);
        } else if (body instanceof Collection<?> collectionBody) {
            List<Object> cloneBody = new ArrayList<>();
            for(Object child : collectionBody) {
                if(child instanceof EnotElement enotChild) {
                    cloneBody.add(enotChild.copy());
                } else {
                    cloneBody.add(child);
                }
            }
            clone.setBody(cloneBody);
        } else {
            clone.setBody(body);
        }
        return clone;
    }
}
