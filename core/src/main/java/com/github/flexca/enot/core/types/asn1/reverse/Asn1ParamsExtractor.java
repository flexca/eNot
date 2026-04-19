package com.github.flexca.enot.core.types.asn1.reverse;

import com.github.flexca.enot.core.element.EnotElement;
import com.github.flexca.enot.core.exception.EnotInvalidArgumentException;
import com.github.flexca.enot.core.types.asn1.Asn1Tag;
import com.github.flexca.enot.core.types.asn1.Asn1TypeSpecification;
import com.github.flexca.enot.core.types.asn1.attribute.Asn1Attribute;
import com.github.flexca.enot.core.types.system.SystemKind;
import com.github.flexca.enot.core.types.system.SystemTypeSpecification;
import com.github.flexca.enot.core.types.system.attribute.SystemAttribute;
import com.github.flexca.enot.core.util.DateTimeUtils;
import com.github.flexca.enot.core.util.PlaceholderUtils;
import org.bouncycastle.asn1.ASN1BMPString;
import org.bouncycastle.asn1.ASN1BitString;
import org.bouncycastle.asn1.ASN1Boolean;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1GeneralizedTime;
import org.bouncycastle.asn1.ASN1IA5String;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1Null;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1PrintableString;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1Set;
import org.bouncycastle.asn1.ASN1TaggedObject;
import org.bouncycastle.asn1.ASN1UTCTime;
import org.bouncycastle.asn1.ASN1UTF8String;
import org.bouncycastle.asn1.ASN1VisibleString;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Extracts placeholder parameter values from an ASN.1 binary structure by matching it
 * against an eNot template.
 *
 * <p>This is the inverse of serialization: given a template that produced some DER-encoded
 * binary and the actual binary, this extractor walks both trees in parallel and collects
 * the concrete values at every placeholder position. The result is a {@code Map<String, Object>}
 * that can be passed directly to
 * {@link com.github.flexca.enot.core.serializer.context.SerializationContext.Builder#withParams(Map)}.</p>
 *
 * <h2>Supported template constructs</h2>
 * <ul>
 *   <li><b>ASN.1 containers</b> (SEQUENCE, SET) — children are matched positionally.</li>
 *   <li><b>ASN.1 leaf types</b> (strings, OID, INTEGER, BOOLEAN, times, BIT_STRING, OCTET_STRING)
 *       — the concrete binary value is extracted and stored under the placeholder name.</li>
 *   <li><b>TAGGED_OBJECT</b> — the tagged wrapper is unwrapped and the body is matched recursively.</li>
 *   <li><b>OCTET_STRING with nested ASN.1 body</b> — the octets are re-parsed as ASN.1 and the
 *       body template is matched recursively; falls back to hex extraction if re-parsing fails.</li>
 *   <li><b>LOOP</b> — each iteration of the loop body is matched against consecutive binary
 *       children; results are collected as a {@code List} stored under the {@code items_name} key.
 *       Iteration stops when no more binary elements can be matched by the loop body template.</li>
 *   <li><b>GROUP</b> — a logical grouping construct; body elements are matched at the same binary
 *       offset and results are stored in a nested map under the {@code group_name} key.</li>
 *   <li><b>CONDITION</b> — matched optimistically; if the first expected binary tag is absent
 *       the condition body is skipped (treated as "was false during serialization").</li>
 * </ul>
 *
 * <h2>Literal (non-placeholder) bodies</h2>
 * Elements whose body is a hard-coded literal (e.g. a fixed OID) consume one binary element
 * but do not produce any param entry; their value is simply discarded.
 *
 * <h2>Optional elements</h2>
 * When an optional element's expected tag is not present in the binary at the current offset,
 * the element is silently skipped and produces no param entry.
 *
 * <p>Instances are stateless and thread-safe.</p>
 */
public class Asn1ParamsExtractor {

    /**
     * Decodes {@code base64Asn1} to DER bytes and delegates to
     * {@link #extractParams(List, byte[])}.
     *
     * @param template    parsed eNot template elements
     * @param base64Asn1  standard Base64-encoded DER binary
     * @return map of placeholder name to extracted value
     * @throws EnotInvalidArgumentException if Base64 decoding or ASN.1 parsing fails
     */
    public Map<String, Object> extractParams(List<EnotElement> template, String base64Asn1) {
        byte[] binary;
        try {
            binary = Base64.getDecoder().decode(base64Asn1);
        } catch (Exception e) {
            throw new EnotInvalidArgumentException("failure during base64 decoding: " + e.getMessage(), e);
        }
        return extractParams(template, binary);
    }

    /**
     * Parses the raw DER binary and matches it against {@code template}, returning
     * all extracted placeholder values.
     *
     * @param template    parsed eNot template elements
     * @param asn1Binary  raw DER-encoded binary
     * @return map of placeholder name to extracted value
     * @throws EnotInvalidArgumentException if ASN.1 parsing fails or the binary structure
     *                                      does not match the template
     */
    public Map<String, Object> extractParams(List<EnotElement> template, byte[] asn1Binary) {
        ASN1Encodable root;
        try {
            root = ASN1Primitive.fromByteArray(asn1Binary);
        } catch (Exception e) {
            throw new EnotInvalidArgumentException("failure during ASN.1 parsing: " + e.getMessage(), e);
        }
        Map<String, Object> result = new LinkedHashMap<>();
        matchElements(template, List.of(root), 0, result);
        return result;
    }

    // -------------------------------------------------------------------------
    // Core matching logic
    // -------------------------------------------------------------------------

    /**
     * Matches a list of template elements against the binary list starting at {@code offset}.
     * Returns the total number of binary elements consumed.
     */
    private int matchElements(List<EnotElement> templateElements, List<ASN1Encodable> binary, int offset,
                              Map<String, Object> dest) {
        int consumed = 0;
        for (EnotElement element : templateElements) {
            consumed += matchElement(element, binary, offset + consumed, dest);
        }
        return consumed;
    }

    /**
     * Dispatches a single template element to the appropriate handler.
     */
    private int matchElement(EnotElement element, List<ASN1Encodable> binary, int offset, Map<String, Object> dest) {
        if (Asn1TypeSpecification.TYPE_NAME.equals(element.getType())) {
            return matchAsn1Element(element, binary, offset, dest);
        } else if (SystemTypeSpecification.TYPE_NAME.equals(element.getType())) {
            return matchSystemElement(element, binary, offset, dest);
        }
        return 0;
    }

    /**
     * Matches an ASN.1-typed template element against the binary element at {@code offset}.
     */
    private int matchAsn1Element(EnotElement element, List<ASN1Encodable> binary, int offset, Map<String, Object> dest) {
        if (offset >= binary.size()) {
            if (element.isOptional()) {
                return 0;
            }
            throw new EnotInvalidArgumentException(
                    "expected binary element at position " + offset + " but binary is exhausted");
        }

        ASN1Encodable current = binary.get(offset);
        Asn1Tag tag = getTag(element);
        if (tag == null) {
            return 0;
        }

        switch (tag) {
            case SEQUENCE -> {
                if (!(current instanceof ASN1Sequence sequence)) {
                    if (element.isOptional()) return 0;
                    throw tagMismatch("SEQUENCE", current, offset);
                }
                List<ASN1Encodable> children = Arrays.asList(sequence.toArray());
                matchBody(element.getBody(), children, 0, dest);
                return 1;
            }
            case SET -> {
                if (!(current instanceof ASN1Set set)) {
                    if (element.isOptional()) return 0;
                    throw tagMismatch("SET", current, offset);
                }
                List<ASN1Encodable> children = Arrays.asList(set.toArray());
                matchBody(element.getBody(), children, 0, dest);
                return 1;
            }
            case TAGGED_OBJECT -> {
                if (!(current instanceof ASN1TaggedObject taggedObject)) {
                    if (element.isOptional()) return 0;
                    throw tagMismatch("TAGGED_OBJECT", current, offset);
                }
                Object body = element.getBody();
                if (body instanceof EnotElement bodyElement) {
                    ASN1Encodable inner = taggedObject.getBaseObject().toASN1Primitive();
                    matchElement(bodyElement, List.of(inner), 0, dest);
                }
                return 1;
            }
            case OCTET_STRING -> {
                if (!(current instanceof ASN1OctetString octetString)) {
                    if (element.isOptional()) return 0;
                    throw tagMismatch("OCTET_STRING", current, offset);
                }
                Object body = element.getBody();
                if (body instanceof EnotElement bodyElement) {
                    try {
                        ASN1Encodable inner = ASN1Primitive.fromByteArray(octetString.getOctets());
                        matchElement(bodyElement, List.of(inner), 0, dest);
                    } catch (Exception e) {
                        // Octets are not valid ASN.1 or body element is a placeholder — fall through
                        if (PlaceholderUtils.isPlaceholder(body)) {
                            assignPlaceholder(body, HexFormat.of().formatHex(octetString.getOctets()), dest);
                        }
                    }
                } else if (PlaceholderUtils.isPlaceholder(body)) {
                    assignPlaceholder(body, HexFormat.of().formatHex(octetString.getOctets()), dest);
                }
                return 1;
            }
            default -> {
                if (!tagMatchesEncodable(tag, current)) {
                    if (element.isOptional()) return 0;
                    throw tagMismatch(tag.getName().toUpperCase(), current, offset);
                }
                Object value = extractLeafValue(current, tag);
                if (PlaceholderUtils.isPlaceholder(element.getBody())) {
                    assignPlaceholder(element.getBody(), value, dest);
                }
                return 1;
            }
        }
    }

    /**
     * Matches template body content (which may be a single element, a list, or a primitive)
     * against the binary list starting at {@code offset}. Returns binary elements consumed.
     */
    @SuppressWarnings("unchecked")
    private int matchBody(Object templateBody, List<ASN1Encodable> binary, int offset, Map<String, Object> dest) {
        if (templateBody == null) {
            return 0;
        }
        if (templateBody instanceof List<?> listBody) {
            List<EnotElement> elements = (List<EnotElement>) (List<?>) listBody.stream()
                    .filter(e -> e instanceof EnotElement)
                    .toList();
            return matchElements(elements, binary, offset, dest);
        }
        if (templateBody instanceof EnotElement element) {
            return matchElement(element, binary, offset, dest);
        }
        return 0;
    }

    // -------------------------------------------------------------------------
    // System element handlers
    // -------------------------------------------------------------------------

    private int matchSystemElement(EnotElement element, List<ASN1Encodable> binary, int offset, Map<String, Object> dest) {
        SystemKind kind = getKind(element);
        if (kind == null) {
            return 0;
        }
        return switch (kind) {
            case LOOP -> matchLoopElement(element, binary, offset, dest);
            case GROUP -> matchGroupElement(element, binary, offset, dest);
            case CONDITION -> matchConditionElement(element, binary, offset, dest);
            default -> 0;
        };
    }

    /**
     * Matches a LOOP element: greedily consumes binary elements for each iteration
     * as long as the iteration body's expected first tag matches. Results are stored
     * as a {@code List<Map<String, Object>>} under the loop's {@code items_name} key.
     */
    private int matchLoopElement(EnotElement loopElement, List<ASN1Encodable> binary, int offset,
                                 Map<String, Object> dest) {
        Object itemsNameAttr = loopElement.getAttribute(SystemAttribute.ITEMS_NAME);
        if (!(itemsNameAttr instanceof String itemsName)) {
            throw new EnotInvalidArgumentException("loop element is missing the items_name attribute");
        }

        List<Object> items = new ArrayList<>();
        int totalConsumed = 0;

        while (offset + totalConsumed < binary.size()) {
            ASN1Encodable candidate = binary.get(offset + totalConsumed);
            if (!canStartIteration(loopElement.getBody(), candidate)) {
                break;
            }
            Map<String, Object> iterResult = new LinkedHashMap<>();
            int consumed = matchBody(loopElement.getBody(), binary, offset + totalConsumed, iterResult);
            if (consumed == 0) {
                break;
            }
            items.add(iterResult);
            totalConsumed += consumed;
        }

        dest.put(itemsName, items);
        return totalConsumed;
    }

    /**
     * Matches a GROUP element: body elements are matched at the same binary offset;
     * the extracted params are stored in a sub-map under the group's {@code group_name} key.
     */
    private int matchGroupElement(EnotElement groupElement, List<ASN1Encodable> binary, int offset,
                                  Map<String, Object> dest) {
        Object groupNameAttr = groupElement.getAttribute(SystemAttribute.GROUP_NAME);
        if (!(groupNameAttr instanceof String groupName)) {
            throw new EnotInvalidArgumentException("group element is missing the group_name attribute");
        }
        Map<String, Object> subDest = new LinkedHashMap<>();
        int consumed = matchBody(groupElement.getBody(), binary, offset, subDest);
        dest.put(groupName, subDest);
        return consumed;
    }

    /**
     * Matches a CONDITION element: attempts to match the condition body optimistically.
     * If the first expected tag is absent, the condition block is treated as absent
     * (its body was not emitted during serialization) and 0 is returned.
     */
    private int matchConditionElement(EnotElement conditionElement, List<ASN1Encodable> binary, int offset,
                                      Map<String, Object> dest) {
        if (offset >= binary.size()) {
            return 0;
        }
        ASN1Encodable candidate = binary.get(offset);
        if (!canStartIteration(conditionElement.getBody(), candidate)) {
            return 0;
        }
        try {
            return matchBody(conditionElement.getBody(), binary, offset, dest);
        } catch (Exception e) {
            return 0;
        }
    }

    // -------------------------------------------------------------------------
    // Loop-start detection
    // -------------------------------------------------------------------------

    /**
     * Returns {@code true} if {@code candidate} could be the first binary element
     * produced by one iteration of the loop/condition body. Uses a fast tag check on
     * the first non-optional required template element.
     */
    private boolean canStartIteration(Object body, ASN1Encodable candidate) {
        Asn1Tag expected = getFirstRequiredTag(body);
        if (expected == null) {
            return true;
        }
        return tagMatchesEncodable(expected, candidate);
    }

    /**
     * Traverses the template body tree to find the first ASN.1 tag that a non-optional
     * element would produce. System wrapper elements (LOOP, GROUP, CONDITION) are traversed
     * recursively. Returns {@code null} if no definite tag can be determined.
     */
    private Asn1Tag getFirstRequiredTag(Object body) {
        if (body instanceof EnotElement element) {
            if (Asn1TypeSpecification.TYPE_NAME.equals(element.getType())) {
                return element.isOptional() ? null : getTag(element);
            }
            if (SystemTypeSpecification.TYPE_NAME.equals(element.getType())) {
                return element.isOptional() ? null : getFirstRequiredTag(element.getBody());
            }
        } else if (body instanceof List<?> listBody) {
            for (Object item : listBody) {
                if (item instanceof EnotElement element && !element.isOptional()) {
                    Asn1Tag tag = getFirstRequiredTag(element);
                    if (tag != null) {
                        return tag;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Returns {@code true} when the concrete ASN.1 class of {@code encodable} matches
     * the BouncyCastle type that corresponds to {@code expected}.
     */
    private boolean tagMatchesEncodable(Asn1Tag expected, ASN1Encodable encodable) {
        return switch (expected) {
            case SEQUENCE -> encodable instanceof ASN1Sequence;
            case SET -> encodable instanceof ASN1Set;
            case OBJECT_IDENTIFIER -> encodable instanceof ASN1ObjectIdentifier;
            case UTF8_STRING -> encodable instanceof ASN1UTF8String;
            case PRINTABLE_STRING -> encodable instanceof ASN1PrintableString;
            case IA5_STRING -> encodable instanceof ASN1IA5String;
            case VISIBLE_STRING -> encodable instanceof ASN1VisibleString;
            case BMP_STRING -> encodable instanceof ASN1BMPString;
            case BOOLEAN -> encodable instanceof ASN1Boolean;
            case INTEGER -> encodable instanceof ASN1Integer;
            case NULL -> encodable instanceof ASN1Null;
            case OCTET_STRING -> encodable instanceof ASN1OctetString;
            case BIT_STRING -> encodable instanceof ASN1BitString;
            case UTC_TIME -> encodable instanceof ASN1UTCTime;
            case GENERALIZED_TIME -> encodable instanceof ASN1GeneralizedTime;
            case TAGGED_OBJECT -> encodable instanceof ASN1TaggedObject;
        };
    }

    // -------------------------------------------------------------------------
    // Value extraction
    // -------------------------------------------------------------------------

    /**
     * Extracts the Java value from a leaf ASN.1 element. String types return a {@code String},
     * INTEGER returns a decimal {@code String}, BOOLEAN returns a {@code Boolean},
     * BIT_STRING and OCTET_STRING fall through to hex-encoded strings, date/time types return
     * a formatted {@code String}, NULL returns {@code null}.
     */
    private Object extractLeafValue(ASN1Encodable encodable, Asn1Tag tag) {
        if (encodable instanceof ASN1ObjectIdentifier oid) return oid.getId();
        if (encodable instanceof ASN1Boolean bool) return bool.isTrue();
        if (encodable instanceof ASN1Integer integer) return integer.getValue().toString(10);
        if (encodable instanceof ASN1UTF8String utf8) return utf8.getString();
        if (encodable instanceof ASN1PrintableString ps) return ps.getString();
        if (encodable instanceof ASN1IA5String ia5) return ia5.getString();
        if (encodable instanceof ASN1VisibleString vs) return vs.getString();
        if (encodable instanceof ASN1BMPString bmp) return bmp.getString();
        if (encodable instanceof ASN1BitString bs) return HexFormat.of().formatHex(bs.getOctets());
        if (encodable instanceof ASN1OctetString os) return HexFormat.of().formatHex(os.getOctets());
        if (encodable instanceof ASN1GeneralizedTime gt) {
            try {
                Date date = gt.getDate();
                return DateTimeUtils.format(DateTimeUtils.toZonedDateTime(date));
            } catch (Exception e) {
                return null;
            }
        }
        if (encodable instanceof ASN1UTCTime ut) {
            try {
                Date date = ut.getDate();
                return DateTimeUtils.format(DateTimeUtils.toZonedDateTime(date));
            } catch (Exception e) {
                return null;
            }
        }
        if (encodable instanceof ASN1Null) return null;
        return null;
    }

    /**
     * If {@code body} is a placeholder expression, extracts the variable name and stores
     * {@code value} in {@code dest}. System variables ({@code system.*}) are skipped.
     */
    private void assignPlaceholder(Object body, Object value, Map<String, Object> dest) {
        PlaceholderUtils.extractPlaceholder(body).ifPresent(name -> {
            if (!PlaceholderUtils.isSystemVariable(name)) {
                dest.put(name, value);
            }
        });
    }

    // -------------------------------------------------------------------------
    // Attribute helpers
    // -------------------------------------------------------------------------

    private Asn1Tag getTag(EnotElement element) {
        Object tagValue = element.getAttributes().get(Asn1Attribute.TAG);
        if (tagValue instanceof String tagStr) {
            return Asn1Tag.fromString(tagStr);
        }
        return null;
    }

    private SystemKind getKind(EnotElement element) {
        Object kindValue = element.getAttributes().get(SystemAttribute.KIND);
        if (kindValue instanceof String kindStr) {
            return SystemKind.fromString(kindStr);
        }
        return null;
    }

    private EnotInvalidArgumentException tagMismatch(String expected, ASN1Encodable actual, int offset) {
        return new EnotInvalidArgumentException(
                "expected " + expected + " at position " + offset + " but got " + actual.getClass().getSimpleName());
    }
}
