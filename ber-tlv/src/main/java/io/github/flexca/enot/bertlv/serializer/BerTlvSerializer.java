package io.github.flexca.enot.bertlv.serializer;

import io.github.flexca.enot.bertlv.BerTlvValueType;
import io.github.flexca.enot.bertlv.attribute.BerTlvAttribute;
import io.github.flexca.enot.bertlv.model.BerTlvElement;
import io.github.flexca.enot.bertlv.model.BerTlvLeafElement;
import io.github.flexca.enot.bertlv.model.BerTlvNodeElement;
import io.github.flexca.enot.core.element.EnotElement;
import io.github.flexca.enot.core.element.value.CommonEnotValueType;
import io.github.flexca.enot.core.exception.EnotSerializationException;
import io.github.flexca.enot.core.parser.EnotJsonError;
import io.github.flexca.enot.core.parser.EnotParser;
import io.github.flexca.enot.core.registry.EnotBinaryConverter;
import io.github.flexca.enot.core.serializer.ElementSerializationResult;
import io.github.flexca.enot.core.serializer.EnotSerializer;
import io.github.flexca.enot.core.serializer.SimpleElementSerializer;
import io.github.flexca.enot.core.util.BinaryUtils;
import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HexFormat;
import java.util.List;

/**
 * Serializer for {@code ber-tlv} elements.
 * <p>
 * After the eNot framework has serialized an element's body, this serializer:
 * <ol>
 *   <li>Separates body results into nested {@link BerTlvElement} objects and raw binary values.</li>
 *   <li>Creates a {@link BerTlvLeafElement} (all-binary body) or a
 *       {@link BerTlvNodeElement} (all-BER-TLV body).
 *       Mixing the two types in the same body is not permitted.</li>
 *   <li>Reads and applies the {@link BerTlvAttribute#TAG},
 *       {@link BerTlvAttribute#MIN_LENGTH}, {@link BerTlvAttribute#MAX_LENGTH},
 *       and {@link BerTlvAttribute#INDEFINITE_FORM} attributes.</li>
 *   <li>Validates runtime length constraints against the actual encoded value length.</li>
 *   <li>Returns the result wrapped in an {@link ElementSerializationResult} carrying
 *       value type {@link BerTlvValueType#BER_TLV_ELEMENT}.</li>
 * </ol>
 * A single shared instance is created by {@link BerTlvEnotTypeSpecification} and reused for
 * all {@code ber-tlv} elements.
 */
public class BerTlvSerializer extends SimpleElementSerializer {

    @Override
    protected List<ElementSerializationResult> serialize(EnotElement element, List<ElementSerializationResult> serializedBody,
                                                         String jsonPath) throws EnotSerializationException {

        if (CollectionUtils.isEmpty(serializedBody)) {
            if (element.isOptional()) {
                return Collections.emptyList();
            } else {
                throw new EnotSerializationException(EnotSerializer.COMMON_ERROR_MESSAGE, EnotJsonError.of(jsonPath + "/" + EnotParser.ENOT_ELEMENT_BODY_NAME,
                        "missing required body for non optional element"));
            }
        }

        String attributesPath = jsonPath + "/" + EnotParser.ENOT_ELEMENT_ATTRIBUTES_NAME;
        byte[] tag = extractTag(element, attributesPath);

        List<BerTlvElement<?>> elements = new ArrayList<>();
        List<byte[]> binaries = new ArrayList<>();
        for (ElementSerializationResult item : serializedBody) {
            if (BerTlvValueType.BER_TLV_ELEMENT.equals(item.getValueType())) {
                if (item.getData() instanceof BerTlvElement<?> berTlvElement) {
                    elements.add(berTlvElement);
                } else {
                    throw new EnotSerializationException(EnotSerializer.COMMON_ERROR_MESSAGE, EnotJsonError.of(jsonPath
                            + "/" + EnotParser.ENOT_ELEMENT_BODY_NAME, "expected body element " + BerTlvValueType.BER_TLV_ELEMENT));
                }
            } else if (item.getValueType().haveSuper(CommonEnotValueType.BINARY)) {
                EnotBinaryConverter binaryConverter = item.getValueType().getBinaryConverter();
                if (binaryConverter == null) {
                    throw new EnotSerializationException(EnotSerializer.COMMON_ERROR_MESSAGE, EnotJsonError.of(jsonPath
                            + "/" + EnotParser.ENOT_ELEMENT_BODY_NAME, "binary convertor not found for type " + item.getValueType().getName()));
                }
                binaries.add(binaryConverter.toBinary(item.getData()));
            } else {
                throw new EnotSerializationException(EnotSerializer.COMMON_ERROR_MESSAGE, EnotJsonError.of(jsonPath
                        + "/" + EnotParser.ENOT_ELEMENT_BODY_NAME, "unsupported body type, expecting binary of BER-TLV element"));
            }
        }

        if (!elements.isEmpty() && !binaries.isEmpty()) {
            throw new EnotSerializationException(EnotSerializer.COMMON_ERROR_MESSAGE, EnotJsonError.of(jsonPath
                    + "/" + EnotParser.ENOT_ELEMENT_BODY_NAME, "mixing of BER-TLV elements with binaries is not allowed"));
        }

        BerTlvElement<?> result;
        if (elements.isEmpty()) {
            BerTlvLeafElement leafElement = new BerTlvLeafElement();
            leafElement.setValue(BinaryUtils.concatenateBinary(binaries));
            result = leafElement;
        } else {
            BerTlvNodeElement nodeElement = new BerTlvNodeElement();
            nodeElement.setValue(elements);
            result = nodeElement;
        }
        result.setTag(tag);

        Integer minValue = getMinLength(element, attributesPath);
        Integer maxValue = getMaxLength(element, attributesPath);

        if(minValue != null && maxValue != null) {
            if (minValue > maxValue) {
                throw new EnotSerializationException(EnotSerializer.COMMON_ERROR_MESSAGE, EnotJsonError.of(attributesPath,
                        "attribute " + BerTlvAttribute.MIN_LENGTH.getName() + " must be less or equals "
                                + BerTlvAttribute.MAX_LENGTH.getName()));
            }
        }

        if(minValue != null || maxValue != null) {
            int valueLength = result.getValueLength();
            String bodyPath = jsonPath + "/" + EnotParser.ENOT_ELEMENT_BODY_NAME;
            if(minValue != null && valueLength < minValue) {
                throw new EnotSerializationException(EnotSerializer.COMMON_ERROR_MESSAGE, EnotJsonError.of(bodyPath,
                        "element value length " + valueLength + " is less than required "
                                + BerTlvAttribute.MIN_LENGTH.getName() + " (" + minValue + ")"));
            }
            if(maxValue != null && valueLength > maxValue) {
                throw new EnotSerializationException(EnotSerializer.COMMON_ERROR_MESSAGE, EnotJsonError.of(bodyPath,
                        "element value length " + valueLength + " exceeds "
                                + BerTlvAttribute.MAX_LENGTH.getName() + " (" + maxValue + ")"));
            }
        }

        boolean indefiniteForm = extractIndefiniteForm(element, attributesPath);
        result.setIndefiniteForm(indefiniteForm);

        return Collections.singletonList(ElementSerializationResult.of(BerTlvValueType.BER_TLV_ELEMENT, result));
    }

    private byte[] extractTag(EnotElement element, String jsonPath) throws EnotSerializationException {

        Object tagObject = element.getAttribute(BerTlvAttribute.TAG);
        String tagPath = jsonPath + "/" + BerTlvAttribute.TAG.getName();
        if (tagObject == null) {
            throw new EnotSerializationException(EnotSerializer.COMMON_ERROR_MESSAGE, EnotJsonError.of(tagPath,
                    "missing required attribute [" + BerTlvAttribute.TAG.getName() + "] for BER-TLV element"));
        }
        if (!(tagObject instanceof String)) {
            throw new EnotSerializationException(EnotSerializer.COMMON_ERROR_MESSAGE, EnotJsonError.of(tagPath,
                    "attribute [" + BerTlvAttribute.TAG.getName() + "] of BER-TLV element must be hex string"));
        }

        try {
            return HexFormat.of().parseHex((String) tagObject);
        } catch(Exception e) {
            throw new EnotSerializationException(EnotSerializer.COMMON_ERROR_MESSAGE, EnotJsonError.of(tagPath,
                    "attribute [" + BerTlvAttribute.TAG.getName() + "] of BER-TLV element must be hex string"));
        }
    }

    private Integer getMinLength(EnotElement element, String currentJsonPath) throws EnotSerializationException {

        Object minLengthObject = element.getAttribute(BerTlvAttribute.MIN_LENGTH);
        if (minLengthObject == null) {
            return null;
        }

        if (minLengthObject instanceof Number minLengthNumber) {
            int minValue = minLengthNumber.intValue();
            if (minValue < 0) {
                throw new EnotSerializationException(EnotSerializer.COMMON_ERROR_MESSAGE, EnotJsonError.of(currentJsonPath
                        + "/" + BerTlvAttribute.MIN_LENGTH.getName(), "attribute [" + BerTlvAttribute.MIN_LENGTH.getName()
                        + "] of BER-TLV must be positive integer"));
            }
            return minValue;
        } else {
            throw new EnotSerializationException(EnotSerializer.COMMON_ERROR_MESSAGE, EnotJsonError.of(currentJsonPath
                            + "/" + BerTlvAttribute.MIN_LENGTH.getName(), "attribute [" + BerTlvAttribute.MIN_LENGTH.getName()
                    + "] of BER-TLV must be positive integer"));
        }
    }

    private Integer getMaxLength(EnotElement element, String currentJsonPath) throws EnotSerializationException {

        Object maxLengthObject = element.getAttribute(BerTlvAttribute.MAX_LENGTH);
        if (maxLengthObject == null) {
            return null;
        }

        if (maxLengthObject instanceof Number maxLengthNumber) {
            int maxValue = maxLengthNumber.intValue();
            if (maxValue < 0) {
                throw new EnotSerializationException(EnotSerializer.COMMON_ERROR_MESSAGE, EnotJsonError.of(currentJsonPath
                        + "/" + BerTlvAttribute.MAX_LENGTH.getName(), "attribute [" + BerTlvAttribute.MAX_LENGTH.getName()
                        + "] of BER-TLV must be positive integer"));
            }
            return maxValue;
        } else {
            throw new EnotSerializationException(EnotSerializer.COMMON_ERROR_MESSAGE, EnotJsonError.of(currentJsonPath
                    + "/" + BerTlvAttribute.MAX_LENGTH.getName(), "attribute [" + BerTlvAttribute.MAX_LENGTH.getName()
                    + "] of BER-TLV must be positive integer"));
        }
    }

    private boolean extractIndefiniteForm(EnotElement element, String currentPath) throws EnotSerializationException {

        Object indefiniteFormObject = element.getAttribute(BerTlvAttribute.INDEFINITE_FORM);
        if (indefiniteFormObject == null) {
            return false;
        }

        if (indefiniteFormObject instanceof Boolean indefiniteForm) {
            return indefiniteForm;
        } else {
            throw new EnotSerializationException(EnotSerializer.COMMON_ERROR_MESSAGE, EnotJsonError.of(currentPath
                    + "/" + BerTlvAttribute.INDEFINITE_FORM.getName(), "attribute [" + BerTlvAttribute.INDEFINITE_FORM.getName()
                    + "] of BER-TLV must be boolean"));
        }
    }
}
