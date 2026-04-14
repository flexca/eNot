package com.github.flexca.enot.core.types.system.serializer;

import com.github.flexca.enot.core.element.EnotElement;
import com.github.flexca.enot.core.element.value.CommonEnotValueType;
import com.github.flexca.enot.core.exception.EnotSerializationException;
import com.github.flexca.enot.core.parser.EnotJsonError;
import com.github.flexca.enot.core.parser.EnotParser;
import com.github.flexca.enot.core.serializer.ElementSerializationResult;
import com.github.flexca.enot.core.serializer.EnotSerializer;
import com.github.flexca.enot.core.serializer.SimpleElementSerializer;
import org.apache.commons.collections4.CollectionUtils;

import java.util.Collections;
import java.util.HexFormat;
import java.util.List;

public class SystemBinToHexSerializer extends SimpleElementSerializer {

    @Override
    protected List<ElementSerializationResult> serialize(EnotElement element, List<ElementSerializationResult> serializedBody, String jsonPath) throws EnotSerializationException {

        if (CollectionUtils.isEmpty(serializedBody)) {
            if(element.isOptional()) {
                return Collections.emptyList();
            } else {
                throw new EnotSerializationException(EnotSerializer.COMMON_ERROR_MESSAGE, EnotJsonError.of( jsonPath
                        + "/" + EnotParser.ENOT_ELEMENT_BODY_NAME, "empty body for non optional element"));
            }
        }

        if (serializedBody.size() != 1) {
            throw new EnotSerializationException(EnotSerializer.COMMON_ERROR_MESSAGE, EnotJsonError.of( jsonPath
                    + "/" + EnotParser.ENOT_ELEMENT_BODY_NAME, "bin_to_hex can serialize only single binary input"));
        }

        ElementSerializationResult toSerialize = serializedBody.get(0);

        if (CommonEnotValueType.BINARY.canConsume(toSerialize.getValueType())) {
            try {
                byte[] input = toSerialize.getValueType().getBinaryConverter().toBinary(toSerialize.getData());
                String result = HexFormat.of().formatHex(input);
                return Collections.singletonList(ElementSerializationResult.of(CommonEnotValueType.TEXT, result));
            } catch(Exception e) {
                throw new EnotSerializationException(EnotSerializer.COMMON_ERROR_MESSAGE, EnotJsonError.of( jsonPath
                        + "/" + EnotParser.ENOT_ELEMENT_BODY_NAME, "failure during bin to hex conversion, reason: " + e.getMessage()), e);
            }
        } else {
            throw new EnotSerializationException(EnotSerializer.COMMON_ERROR_MESSAGE, EnotJsonError.of( jsonPath
                    + "/" + EnotParser.ENOT_ELEMENT_BODY_NAME, "body of bin_to_hex element must be binary"));
        }
    }
}
