package com.github.flexca.enot.core.struct;

import com.github.flexca.enot.core.struct.attribute.CommonEnotAttribute;
import com.github.flexca.enot.core.struct.attribute.EnotAttribute;
import com.github.flexca.enot.core.struct.type.EnotElementType;
import lombok.Data;

import java.util.*;

@Data
public class EnotElement {

    private EnotElementType type;

    private Map<EnotAttribute, Object> attributes;

    private Object body;

    public String getTag() {
        Object optional = attributes.get(CommonEnotAttribute.TAG);
        if(optional instanceof String stringOptional) {
            return stringOptional;
        } else {
            return null;
        }
    }

    public boolean isOptional() {
        Object optional = attributes.get(CommonEnotAttribute.OPTIONAL);
        if(optional instanceof String stringOptional) {
            return Boolean.parseBoolean(stringOptional);
        } else {
            return Boolean.TRUE.equals(optional);
        }
    }

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
