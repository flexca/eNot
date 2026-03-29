package com.github.flexca.enot.core.asn1.serializer;

import com.github.flexca.enot.core.element.EnotElement;
import com.github.flexca.enot.core.registry.EnotRegistry;
import com.github.flexca.enot.core.serializer.ElementSerializer;
import org.apache.commons.collections4.CollectionUtils;
import org.bouncycastle.asn1.DERUTF8String;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class Utf8StringSerializer implements ElementSerializer {

    @Override
    public List<Object> serialize(EnotElement element, List<Object> input, Map<String, Object> parameters,
                                  String parametersPath, EnotRegistry enotRegistry) {

        if (CollectionUtils.isEmpty(input)) {
            return Collections.emptyList();
        }
        if(input.get(0) instanceof String stringInput) {
            DERUTF8String output = new DERUTF8String(stringInput);
            return Collections.singletonList(output);
        }
        return Collections.emptyList();
    }
}
