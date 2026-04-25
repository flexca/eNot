package io.github.flexca.enot.core.types.system.serializer;

import io.github.flexca.enot.core.element.EnotElement;
import io.github.flexca.enot.core.element.value.CommonEnotValueType;
import io.github.flexca.enot.core.exception.EnotSerializationException;
import io.github.flexca.enot.core.parser.EnotJsonError;
import io.github.flexca.enot.core.parser.EnotParser;
import io.github.flexca.enot.core.serializer.ElementSerializationResult;
import io.github.flexca.enot.core.serializer.EnotSerializer;
import io.github.flexca.enot.core.serializer.SimpleElementSerializer;
import io.github.flexca.enot.core.types.system.attribute.BitOrder;
import io.github.flexca.enot.core.types.system.attribute.ByteOrder;
import io.github.flexca.enot.core.types.system.attribute.SystemAttribute;
import org.apache.commons.collections4.CollectionUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class SystemBitMapSerializer extends SimpleElementSerializer {

    @Override
    protected List<ElementSerializationResult> serialize(EnotElement element, List<ElementSerializationResult> serializedBody,
                                                         String jsonPath) throws EnotSerializationException {

        if (CollectionUtils.isEmpty(serializedBody)) {
            if (element.isOptional()) {
                return Collections.emptyList();
            } else {
                throw new EnotSerializationException(EnotSerializer.COMMON_ERROR_MESSAGE, EnotJsonError.of(jsonPath
                        + "/" + EnotParser.ENOT_ELEMENT_BODY_NAME, "empty body for non optional element"));
            }
        }

        ByteOrder byteOrder = resolveByteOrder(element);
        if (byteOrder == null) {
            throw new EnotSerializationException(EnotSerializer.COMMON_ERROR_MESSAGE, EnotJsonError.of(jsonPath
                    + "/" + SystemAttribute.BYTE_ORDER, "empty or unsupported byte_order attribute, use one of: "
                    + Arrays.toString(ByteOrder.values())));
        }

        BitOrder bitOrder = resolveBitOrder(element);
        if (bitOrder == null) {
            throw new EnotSerializationException(EnotSerializer.COMMON_ERROR_MESSAGE, EnotJsonError.of(jsonPath
                    + "/" + SystemAttribute.BIT_ORDER, "empty or unsupported bit_order attribute, use one of: "
                    + Arrays.toString(BitOrder.values())));
        }

        byte[] result = inputToBytes(serializedBody, byteOrder, bitOrder, jsonPath);
        return Collections.singletonList(ElementSerializationResult.of(CommonEnotValueType.BINARY, result));
    }

    private ByteOrder resolveByteOrder(EnotElement element) {

        Object byteOrderObject = element.getAttribute(SystemAttribute.BYTE_ORDER);
        ByteOrder byteOrder = null;
        if (byteOrderObject instanceof String byteOrderString) {
            byteOrder = ByteOrder.fromName(byteOrderString);
        }
        return byteOrder;
    }

    private BitOrder resolveBitOrder(EnotElement element) {

        Object bitOrderObject = element.getAttribute(SystemAttribute.BIT_ORDER);
        BitOrder bitOrder = null;
        if (bitOrderObject instanceof String bitOrderString) {
            bitOrder = BitOrder.fromName(bitOrderString);
        }
        return bitOrder;
    }

    private byte[] inputToBytes(List<ElementSerializationResult> serializedBody, ByteOrder byteOrder, BitOrder bitOrder,
                                String jsonPath) throws EnotSerializationException {

        int bytesLength = serializedBody.size() / 8;
        if (serializedBody.size() % 8 != 0) {
            bytesLength++;
        }
        byte[] bytes = new byte[bytesLength];
        int bitCount = 0;
        int byteValue = 0;
        int byteCount = 0;
        for (ElementSerializationResult item : serializedBody) {
            if (item.getData() instanceof Boolean booleanItem) {
                int shift = BitOrder.LSB_FIRST.equals(bitOrder) ? bitCount : 7 - bitCount;
                byteValue |= (booleanItem ? 1 : 0) << shift;
            } else {
                throw new EnotSerializationException(EnotSerializer.COMMON_ERROR_MESSAGE, EnotJsonError.of(jsonPath
                        + "/" + EnotParser.ENOT_ELEMENT_BODY_NAME, "system element bit_map expect array of boolean values"));
            }
            bitCount++;
            if (bitCount >= 8) {
                int index = ByteOrder.BIG_ENDIAN.equals(byteOrder) ? byteCount : (bytesLength - byteCount - 1);
                bytes[index] = (byte) (byteValue & 0xFF);
                byteValue = 0;
                bitCount = 0;
                byteCount++;
            }
        }
        if (serializedBody.size() % 8 != 0) {
            int index = ByteOrder.BIG_ENDIAN.equals(byteOrder) ? byteCount : (bytesLength - byteCount - 1);
            bytes[index] = (byte) (byteValue & 0xFF);
        }
        return bytes;
    }
}
