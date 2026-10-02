package io.github.flexca.enot.core.types.system.serializer;

import io.github.flexca.enot.core.element.EnotElement;
import io.github.flexca.enot.core.element.value.CommonEnotValueType;
import io.github.flexca.enot.core.exception.EnotSerializationException;
import io.github.flexca.enot.core.parser.EnotJsonError;
import io.github.flexca.enot.core.parser.EnotParser;
import io.github.flexca.enot.core.serializer.ElementSerializationResult;
import io.github.flexca.enot.core.serializer.EnotSerializer;
import io.github.flexca.enot.core.serializer.SimpleElementSerializer;
import io.github.flexca.enot.core.util.IpAddressUtils;
import org.apache.commons.collections4.CollectionUtils;

import java.net.InetAddress;
import java.util.Collections;
import java.util.List;

public class SystemIpAddressToBytesSerializer extends SimpleElementSerializer {

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
                    + "/" + EnotParser.ENOT_ELEMENT_BODY_NAME, "ip_address_to_bytes element can serialize only single text input"));
        }

        ElementSerializationResult toSerialize = serializedBody.get(0);

        if (toSerialize.getData() instanceof String textBody) {
            // reject hostnames up front so getByName never performs DNS resolution
            if (!IpAddressUtils.isValidIpAddress(textBody)) {
                throw new EnotSerializationException(EnotSerializer.COMMON_ERROR_MESSAGE, EnotJsonError.of( jsonPath
                        + "/" + EnotParser.ENOT_ELEMENT_BODY_NAME, "body of ip_address_to_bytes element must be a literal IPv4 or IPv6 address"));
            }
            try {
                byte[] result = InetAddress.getByName(textBody).getAddress();
                return Collections.singletonList(ElementSerializationResult.of(CommonEnotValueType.BINARY, result));
            } catch(Exception e) {
                throw new EnotSerializationException(EnotSerializer.COMMON_ERROR_MESSAGE, EnotJsonError.of( jsonPath
                        + "/" + EnotParser.ENOT_ELEMENT_BODY_NAME, "failure during IP address to binary conversion, reason: "
                        + e.getMessage()), e);
            }
        } else {
            throw new EnotSerializationException(EnotSerializer.COMMON_ERROR_MESSAGE, EnotJsonError.of( jsonPath
                    + "/" + EnotParser.ENOT_ELEMENT_BODY_NAME, "body of ip_address_to_bytes element must be string"));
        }
    }
}

